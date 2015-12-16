package com.qx.account;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Settings.ChangeGuojiaReq;
import qxmobile.protobuf.Settings.ChangeGuojiaResp;
import qxmobile.protobuf.Settings.ChangeName;
import qxmobile.protobuf.Settings.ChangeNameBack;
import qxmobile.protobuf.Settings.ConfGet;
import qxmobile.protobuf.Settings.ConfSave;
import xg.push.XGTagTask;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.template.DangpuCommon;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AlliancePlayer;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.guojia.GuoJiaMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

/**
 * 客户端设置管理器
 * 
 * @author 康建虎
 * 
 */
public class SettingsMgr {
	public static Logger log = LoggerFactory.getLogger(SettingsMgr.class);
	public static int changeNameCost = 100;
	public static int ZHUANGUOLING = 910001;
	public static int SUCCESS = 0;// 转国成功
	public static int ERROR_NO_CARD = 102;// 转国失败，没有转国卡
	public static int ERROR_IN_LIANMENG = 101;// 转国失败，在联盟中
	public static int ERROR_IN_YUANBAO = 103;//  103-元宝不足
	public static int ERROR_GUOJIA_NOT_EXIST = 104;//  104-国家不存在

	public void get(int id, IoSession session, Builder builder) {
		Long v = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (v == null) {
			return;
		}
		SettingsBean bean = HibernateUtil.find(SettingsBean.class, v);
		ConfGet.Builder ret = ConfGet.newBuilder();
		if (bean == null) {
			ret.setJson("{}");
		} else {
			ret.setJson(bean.str);
		}
		session.write(ret.build());
		log.info("{}请求设置{}", v, ret.getJson());
	}

	public void save(int id, IoSession session, Builder builder) {
		Long v = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (v == null) {
			return;
		}
		ConfSave.Builder req = (qxmobile.protobuf.Settings.ConfSave.Builder) builder;
		SettingsBean bean = HibernateUtil.find(SettingsBean.class, v);
		if (bean == null) {
			bean = new SettingsBean();
			bean.id = v;
		}
		bean.str = req.getJson();
		HibernateUtil.save(bean);
		log.info("{}保存设置{}", v, bean.str);
		new Thread(new XGTagTask(session, bean.str),"XGTAG:"+v).start();
	}

	public void changeName(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		ChangeName.Builder req = (qxmobile.protobuf.Settings.ChangeName.Builder) builder;
		ChangeNameBack.Builder ret = ChangeNameBack.newBuilder();
		ret.setName(req.getName());
		boolean open = BigSwitch.inst.vipMgr.isVipPermit(4, jz.vipLevel);
		if (!open) {
			ret.setCode(-400);
			ret.setMsg("VIP等级不足");
			return;
		}
		if (jz.yuanBao < changeNameCost) {
			ret.setCode(changeNameCost);
			ret.setMsg("您的元宝不足，需要" + changeNameCost);
			session.write(ret.build());
			return;
		}
		if (BigSwitch.inst.accMgr.isBadName(req.getName())) {
			ret.setCode(-300);
			ret.setMsg("这个名字不可用");
			session.write(ret.build());
			return;
		}
		String oldName = jz.name;
		boolean mcCheck = MemcachedCRUD.getMemCachedClient().add(
				"JunZhu:" + req.getName(), jz.id);
		if (mcCheck == false) {
			ret.setCode(-200);
			ret.setMsg("名称已被占用");
			session.write(ret.build());
			return;
		}
		jz.name = req.getName();
		// jz.yuanBao -= changeNameCost;
		YuanBaoMgr.inst.diff(jz, -changeNameCost, 0, changeNameCost,
				YBType.YB_MOD_NAME, "修改名字");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);
		log.info("{}花费{}元宝将名字从{}改为{}", jz.id, oldName, req.getName());
		ActLog.log.KingChange(jz.id, oldName, jz.name, ActLog.vopenid);
		ret.setCode(0);
		ret.setMsg("改名成功");
		session.write(ret.build());
	}

	/**
	 * 转换国家
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void changeGuojia(int id, IoSession session, Builder builder) {
		ChangeGuojiaReq.Builder request = (qxmobile.protobuf.Settings.ChangeGuojiaReq.Builder) builder;
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		ChangeGuojiaResp.Builder response = ChangeGuojiaResp.newBuilder();
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (member != null && member.lianMengId > 0) {
			// 联盟不为空，不能转换国家
			response.setResult(ERROR_IN_LIANMENG);
			log.info("{}转国失败，已在联盟中", jz.id);
			writeByProtoMsg(session, PD.S_ZHUANGGUO_RESP, response);
			return;
		}
		// 转国
		int guojiaId = request.getGuojiaId();
		int useType = request.getUseType();
		boolean guoJiaExist = GuoJiaMgr.inst.verifyGuoJiaExist((byte) guojiaId);
		if(!guoJiaExist) {
			response.setResult(ERROR_GUOJIA_NOT_EXIST);
			log.info("{}转国失败，选择的国家不存在", jz.id, guojiaId);
			writeByProtoMsg(session, PD.S_ZHUANGGUO_RESP, response);
			return;
		}
		
		if(0 == useType) {
			Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
			int cnt = BagMgr.inst.getItemCount(bag, ZHUANGUOLING);
			if (cnt <= 0) {
				// 没有转国令，不能转换国家
				response.setResult(ERROR_NO_CARD);
				log.info("{}转国失败，没有转国令", jz.id);
				writeByProtoMsg(session, PD.S_ZHUANGGUO_RESP, response);
				return;
			}
			BagMgr.inst.removeItem(bag, ZHUANGUOLING, 1, "使用转国令转换国家",jz.level);
			BagMgr.inst.sendBagInfo(0, session, null);
			// 消耗转国卡
			log.info("{}使用一张转国令成功转换国家到{}", jz.id, jz.guoJiaId);
			
		} else if(1 == useType) {
			DangpuCommon dangpuCommon = ShopMgr.inst.getDangpuCommon(1003);
			int needYuanBao = dangpuCommon.needNum/dangpuCommon.itemNum;
			if(jz.yuanBao < needYuanBao) {
				response.setResult(ERROR_IN_YUANBAO);
				log.info("{}转国失败，元宝不足:{}", jz.id, needYuanBao);
				writeByProtoMsg(session, PD.S_ZHUANGGUO_RESP, response);
				return;
			}
			YuanBaoMgr.inst.diff(jz, -needYuanBao, 0, needYuanBao, YBType.YB_CHANGE_COUNTRY, "转国花费元宝");
		} else {
			log.error("转国失败，使用转国的方式:{}", useType);
			return;
		}
		int oldGjId = jz.guoJiaId;
		jz.guoJiaId = guojiaId;
		int newGjId = jz.guoJiaId;
		HibernateUtil.save(jz);
		// 2015-7-31 9:58 添加排行榜国家榜刷新
		EventMgr.addEvent(ED.CHANGE_GJ_RANK_REFRESH, new Object[]{jz.id,oldGjId,newGjId});
		response.setResult(SUCCESS);
		writeByProtoMsg(session, PD.S_ZHUANGGUO_RESP, response);
		JunZhuMgr.inst.sendMainInfo(session);
		return;
	}

	/**
	 * 发送指定协议号的消息
	 * 
	 * @param session
	 * @param prototype
	 * @param response
	 * @return
	 */
	protected void writeByProtoMsg(IoSession session, int prototype,
			Builder response) {
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = prototype;
		msg.builder = response;
		log.info("发送协议号为：{}", prototype);
		session.write(msg);
	}
}
