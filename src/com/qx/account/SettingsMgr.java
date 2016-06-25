package com.qx.account;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.CanShu;
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
import com.qx.guojia.GuoJiaMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.vip.VipData;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import log.ActLog;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Settings.ChangeGuojiaReq;
import qxmobile.protobuf.Settings.ChangeGuojiaResp;
import qxmobile.protobuf.Settings.ChangeName;
import qxmobile.protobuf.Settings.ChangeNameBack;
import qxmobile.protobuf.Settings.ConfGet;
import qxmobile.protobuf.Settings.ConfSave;
import xg.push.XGTagTask;

/**
 * 客户端设置管理器
 * 
 * @author 康建虎
 * 
 */
public class SettingsMgr {
	public static int buyModelPrice = 500;
	public static long changeModelCD = 3600*24*1000*5;
	public static Logger log = LoggerFactory.getLogger(SettingsMgr.class);
	public static int changeNameCost = 100;
	public static int ZHUANGUOLING = 910001;
	public static int SUCCESS = 0;// 转国成功
	public static int ERROR_NO_CARD = 102;// 转国失败，没有转国卡
	public static int ERROR_IN_LIANMENG = 101;// 转国失败，在联盟中
	public static int ERROR_IN_YUANBAO = 103;//  103-元宝不足
	public static int ERROR_GUOJIA_NOT_EXIST = 104;//  104-国家不存在
	public static final String Key  = "CHANGE_NAME" + GameServer.serverId;

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

	public synchronized void changeName(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		ChangeName.Builder req = (qxmobile.protobuf.Settings.ChangeName.Builder) builder;
		ChangeNameBack.Builder ret = ChangeNameBack.newBuilder();
		ret.setName(req.getName());
		boolean open = BigSwitch.inst.vipMgr.isVipPermit(VipData.change_name, jz.vipLevel);	
		long currentTime = System.currentTimeMillis();
		String record = Redis.getInstance().get(Key+jz.id) ;
		long remainCdTime = 0 ;
		if(record != null ){
			long lastCurrentTime = Long.parseLong(record);
			remainCdTime = (CanShu.CHANGE_NAME_CD*60*60*1000L) - (currentTime - lastCurrentTime) ;
		}
		String newName = req.getName();
		remainCdTime = remainCdTime < 0 ? 0 : remainCdTime ;
		if(newName == null || newName.length() == 0){
			if(!open){
				return;
			}
			ret.setCode(-700);
			ret.setMsg(""+(remainCdTime));
			session.write(ret.build());
			return;
		}
		if(remainCdTime > 0 ){
			ret.setCode(-600);
			ret.setMsg(""+(remainCdTime));
			session.write(ret.build());
			return;
		}
		if (!open) {
			ret.setCode(-400);
			ret.setMsg("VIP等级不足");
			session.write(ret.build());
			return;
		}
		if (jz.yuanBao < changeNameCost) {
			ret.setCode(changeNameCost);
			ret.setMsg("您的元宝不足，需要" + changeNameCost);
			session.write(ret.build());
			return;
		}
		if (BigSwitch.inst.accMgr.hasSensitiveWord(req.getName())) {
			ret.setCode(-500);
			ret.setMsg("输入的名称包含敏感词！");
			session.write(ret.build());
			return;
		}
		if (BigSwitch.inst.accMgr.hasSpecial(req.getName())) {
			ret.setCode(-200);
			ret.setMsg("仅限使用中/英文以及数字！");
			session.write(ret.build());
			return;
		}
		if (req.getName().length() > AccountManager.NAME_LENGTH_JUNZHU_MAX){
			ret.setCode(-100);
			ret.setMsg("输入的名称过长！");
			session.write(ret.build());
			return;
		}
		String oldName = jz.name;
		JunZhu junZhu = HibernateUtil.find(JunZhu.class,  " where name='" + req.getName() +"'", false);
		if (junZhu != null) {
			ret.setCode(-300);
			ret.setMsg("该名称已被其他玩家使用！");
			session.write(ret.build());
			return;
		}
		
		jz.name = req.getName();
		Redis.getInstance().set(Key+jz.id, currentTime+"");
		YuanBaoMgr.inst.diff(jz, -changeNameCost, 0, changeNameCost, YBType.YB_MOD_NAME, "修改名字");
		HibernateUtil.update(jz);
		JunZhuMgr.inst.sendMainInfo(session,jz);
		log.info("君主修改名字成功，君主:{}花费{}元宝将名字从{}改为{}", jz.id, changeNameCost, oldName, req.getName());
		ActLog.log.KingChange(jz.id, oldName, jz.name, ActLog.vopenid);
		ret.setCode(0);
		ret.setMsg("改名成功");
		session.write(ret.build());
		EventMgr.addEvent(ED.JUNZHU_CHANGE_NAME, new Object[]{oldName, jz.id});
		//同步玩家名字
		do{
			Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
			if(scene == null) {
				break;
			}
			Player player = scene.getPlayerByJunZhuId(jz.id);
			if(player == null) {
				break;
			}
			player.name = jz.name;
			qxmobile.protobuf.Scene.EnterScene.Builder info = scene.buildEnterInfo(player);
			ProtobufMsg msg = new ProtobufMsg(PD.S_HEAD_INFO, info);
			scene.broadCastEvent(msg, 0/*player.userId*/);
		}while(false);
	}

	public void getModelInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		UnlockModel bean = HibernateUtil.find(UnlockModel.class, jz.id);
		if(bean == null){
			bean = new UnlockModel();
			bean.jzId = jz.id;
			bean.ids = String.valueOf(jz.roleId);
			HibernateUtil.insert(bean);
		}
		long cd = calcChangeModelCD(jz.id)/1000;
		ErrorMessage.Builder ret = ErrorMessage.newBuilder();
		ret.setCmd(0);//需要铜币
		ret.setErrorCode(buyModelPrice);//需要元宝
		String str = cd+"#"+bean.ids;//CD时间（秒）#已解锁形象
		ret.setErrorDesc(str);
		ProtobufMsg m = new ProtobufMsg(PD.MODEL_INFO, ret);
		session.write(m);
	}

	public long calcChangeModelCD(long jzId) {
		String preChangeTime = Redis.getInstance().get("ChangeModeTime:"+jzId);
		long cd = 0;
		if(preChangeTime != null){
			long l = Long.parseLong(preChangeTime);
			cd = l + changeModelCD - System.currentTimeMillis();
			if(cd<0){
				cd=0;
			}
		}
		return cd;
	}
	
	public void clearChangNMCD(long jzId) {
		Redis.getInstance().del(Key+jzId);
	}
	public long getLastChangNameTM(long jzId) {
		String time = Redis.getInstance().get(Key+jzId);
		long lastChangeNameTM = 0;
		if(null != time){
			lastChangeNameTM = Long.valueOf(time);
		}
		return lastChangeNameTM;
	}
	
	public void changeLastChangNMTM(long jzId,String lastChangNameTM) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		Date date = null;
 		try {
			date = formatter.parse(lastChangNameTM);
			Long time = date.getTime();
			Redis.getInstance().set(Key+jzId, String.valueOf(time));
		} catch (ParseException e) {
			log.info("{}日期格式错误", jzId);
		}
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
			BagMgr.inst.sendBagInfo(session, bag);
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
		HibernateUtil.update(jz);
		// 2015-7-31 9:58 添加排行榜国家榜刷新
		EventMgr.addEvent(ED.CHANGE_GJ_RANK_REFRESH, new Object[]{jz.id,oldGjId,newGjId, jz.level});
		response.setResult(SUCCESS);
		writeByProtoMsg(session, PD.S_ZHUANGGUO_RESP, response);
		JunZhuMgr.inst.sendMainInfo(session,jz);
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

	public void changeModel(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		UnlockModel bean = HibernateUtil.find(UnlockModel.class, jz.id);
		if(bean == null){
			//获取信息时应该创建了。 
			return;
		}		
		ErrorMessage.Builder req = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		int targetId = req.getErrorCode();
		String v = String.valueOf(targetId);
		if(bean.ids.contains(v)==false){
			log.error("未解锁");
			return;
		}
		if(targetId<1 || targetId>4){
			log.error("id错误 {}", targetId);
			return;
		}
		long cd = calcChangeModelCD(jz.id);
		if(cd>0){
			log.error("CD中");
//			return;
		}
		final int preId = jz.roleId;
		jz.roleId = targetId;
		HibernateUtil.update(jz);
		Redis.getInstance().set("ChangeModeTime:"+jz.id,String.valueOf(System.currentTimeMillis()));
		log.info("{} change model from {} to {}", jz.id, preId, targetId);
		ErrorMessage.Builder ret = ErrorMessage.newBuilder();
		ret.setErrorCode(targetId);//
		ret.setErrorDesc(String.valueOf(jz.id));
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		Player p = sc.getPlayerByJunZhuId(jz.id);
		if(sc != null && p != null){
			ret.setCmd(p.userId);
			ProtobufMsg m = new ProtobufMsg(PD.BD_CHANGE_MODEL, ret);
			sc.broadCastEvent(m, 0);
		}
		JunZhuMgr.inst.sendMainInfo(session, jz);
	}

	public void unlockModel(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		UnlockModel bean = HibernateUtil.find(UnlockModel.class, jz.id);
		if(bean == null){
			//获取信息时应该创建了。 
			return;
		}
		ErrorMessage.Builder req = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		int targetId = req.getErrorCode();
		String v = String.valueOf(targetId);
		if(bean.ids.contains(v)){
			log.error("之前已解锁");
			return;
		}
		int tongBi = 1234;
		int yuanBao = buyModelPrice;
		/*if(jz.tongBi>=tongBi){
			jz.tongBi -= tongBi;
			log.info("扣除 {} 铜币 {} 解锁  {}",jz.id,tongBi,targetId);
		}else */if(jz.yuanBao>=yuanBao){
			YuanBaoMgr.inst.diff(jz, -yuanBao, 0, 0, 0, "解锁模型");
			log.info("扣除 {} 元宝 {} 解锁  {}",jz.id,yuanBao,targetId);
		}else{
			log.error("{}铜币元宝都不足",jz.id);
			return;
		}
		HibernateUtil.update(jz);
		bean.ids+=","+targetId;
		HibernateUtil.update(bean);
		session.write(PD.UNLOCK_MODEL);//解锁成功
		getModelInfo(0, session, null);
	}
}
