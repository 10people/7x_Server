package com.qx.hero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.WuJiangProtos.WuJiangTech;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechLevelup;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechLevelupReq;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechSpeedupResp;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechnologyDate;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.KeJiInfo;
import com.manu.dynasty.template.Keji;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.achievement.AchievementCondition;
import com.qx.achievement.AchievementConstants;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class WuJiangKeJiMgr {
	private static final int TONGBI = 900001;
	private static final int GOLDEN_JINGPO = 900007;
	public static Logger log = LoggerFactory.getLogger(WuJiangKeJiMgr.class);
	public static WuJiangKeJiMgr inst;
	/** 初始化的武将科技信息 **/
	private List<KeJiInfo> initTech;
	/** 武将科技信息表， <id,Keji> **/
	public Map<Integer, Keji> id2Keji;

	public WuJiangKeJiMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		// // List list =
		// TempletService.listAll(KeJiInfo.class.getSimpleName());
		// List<KeJiInfo> initTech = new ArrayList<KeJiInfo>();
		// if (list == null || list.size() == 0) {
		// log.error("武将科技初始化错误，没有相关数据.................");
		// }else{
		// for(Object o : list){
		// KeJiInfo keji = (KeJiInfo)o;
		// initTech.add(keji);
		// }
		// }
		//
		// list = TempletService.listAll(Keji.class.getSimpleName());
		// Map<Integer, Keji> id2Keji = new HashMap<Integer, Keji>();
		// if (list == null || list.size() == 0) {
		// log.error("武将科技数据错误.............");
		// }else{
		// for(Object o : list){
		// Keji keji = (Keji)o;
		// id2Keji.put(keji.getId(), keji);
		// }
		// }
		//
		// this.initTech = initTech;
		// this.id2Keji = id2Keji;
	}

	/**
	 * 创建角色后的武将科技初始化。
	 * 
	 * @param junZhuId
	 */
	public void addWuJiangTech(long junZhuId) {
		WjKeJi techs = new WjKeJi();
		int cdTime = 0;
		techs.setJunZhuId(junZhuId);
		for (KeJiInfo tmp : initTech) {
			int kejiId = tmp.getKejiId();
			switch (tmp.getKejiType()) {
			case HeroMgr.ATTACK:
				techs.setAttack(kejiId);
				cdTime += getKeJi(kejiId).getCostTime();
				break;
			case HeroMgr.DEFENSE:
				techs.setDefense(kejiId);
				cdTime += getKeJi(kejiId).getCostTime();
				break;
			case HeroMgr.HP:
				techs.setHp(kejiId);
				cdTime += getKeJi(kejiId).getCostTime();
				break;
			case HeroMgr.ZHIMOU:
				techs.setZhiMou(kejiId);
				break;
			case HeroMgr.WUYI:
				techs.setWuYi(kejiId);
				break;
			case HeroMgr.TONGSHUAI:
				techs.setTongShuai(kejiId);
				break;

			default:
				break;
			}
		}
		techs.setCD(System.currentTimeMillis() + cdTime * 1000);
		MC.add(techs, junZhuId);
		Throwable t = HibernateUtil.insert(techs);
		if (t != null) {
			log.error(t.toString());
		}
	}

	public void exec(int code, IoSession session, Builder builder) {
		switch (code) {
		case PD.WUJIANG_TECHINFO_REQ:
			getTechInfo(code, session, builder);
			break;
		case PD.WUJIANG_TECHLEVELUP_REQ:
			levelUpTech(code, session, builder);
			break;
		default:
			log.error("unkown operation code {}", code);
			error(session, code, "unkonw operation code " + code);
			break;
		}
	}

	/**
	 * 升级科技，目前没有材料消耗的计算和限制。
	 * 
	 * @param code
	 * @param session
	 * @param builder
	 */
	private void levelUpTech(int code, IoSession session, Builder builder) {
		WuJiangTechLevelupReq.Builder req = (WuJiangTechLevelupReq.Builder) builder;
		long junZhuId = getJunZhuIdBySession(session);
		int kejiId = req.getTechType();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("找不到君主，junZhuId：{}", junZhuId);
			return;
		}

		WjKeJi wjKeJi = HibernateUtil.find(WjKeJi.class, junZhuId);
		if (wjKeJi == null) {
			wjKeJi = createDefaultBean(junZhuId);
			MC.add(wjKeJi, junZhuId);
			HibernateUtil.insert(wjKeJi);
		}
		// 获取该科技配置信息
		Keji kejiCfg = id2Keji.get(kejiId);
		if (kejiCfg == null) {
			log.error("武将科技配置文件错误，id:{}", kejiId);
			error(session, code, "技能出现问题");
			return;
		}
		// 检查是否已达到最高等级
		if (kejiCfg.posId == 0) {
			log.error("科技等级已经满级，type:{}", kejiCfg.kejiType);
			error(session, code, "该科技等级已满级");
			return;
		}
		// 检查升级该科技，所需条件是否达到
		int length = kejiCfg.preIds.length;
		for (int i = 0; i < length; i++) {
			int preId = kejiCfg.preIds[i];
			if (preId <= 0) {
				continue;
			}
			Keji kj = id2Keji.get(preId);
			int dbKjId = wjKeJi.getTechIdByType(kj.kejiType);
			if (dbKjId < kj.getId()) {
				log.error("升级科技type:{},id:{}时，科技type:{},未达到条件需要大于id:{}",
						kejiCfg.kejiType, kejiId, kj.kejiType, preId);
				error(session, code, "升级该科技，有条件未满足");
				return;
			}
		}

		int kejiType = kejiCfg.kejiType;
		WuJiangTechnologyDate.Builder data = WuJiangTechnologyDate.newBuilder();
		boolean isSuccess = false;
		switch (kejiType) {
		case HeroMgr.ATTACK:
		case HeroMgr.DEFENSE:
		case HeroMgr.HP:
			isSuccess = upgradeLowKeji(kejiType, kejiCfg, wjKeJi, session,
					code, junZhu);
			break;
		case HeroMgr.ZHIMOU:
		case HeroMgr.WUYI:
		case HeroMgr.TONGSHUAI:
			isSuccess = upgradeAdvancedKeji(kejiType, wjKeJi, session, code,
					kejiCfg, data);
			break;
		default:
			break;
		}
		if (!isSuccess) {
			return;
		}
		HibernateUtil.save(wjKeJi);
		data.setTechType(wjKeJi.getTechIdByType(kejiType));
		WuJiangTechLevelup.Builder resp = WuJiangTechLevelup.newBuilder();
		resp.setTechDate(data);
		session.write(resp.build());
	}

	/**
	 * 升级高级科技
	 * 
	 * @param kejiType
	 * @param wjKeJi
	 * @param session
	 * @param code
	 * @param kejiCfg
	 * @param data
	 * @return
	 */
	private boolean upgradeAdvancedKeji(int kejiType, WjKeJi wjKeJi,
			IoSession session, int code, Keji kejiCfg,
			WuJiangTechnologyDate.Builder data) {
		int goldenJingpo = wjKeJi.getGoldenJingPo();
		if (goldenJingpo == 0) {
			log.error("高级科技升级失败，没有可用的金色精魄");
			error(session, code, "没有可用的金色精魄");
			return false;
		}
		int length = kejiCfg.items.length;
		for (int i = 0; i < length; i++) {
			if (kejiCfg.items[i] == GOLDEN_JINGPO) {
				if (kejiCfg.nums[i] <= 0) {
					log.error("高级科技升级消耗配置错误:" + kejiCfg.id);
					continue;
				}
				if (goldenJingpo < kejiCfg.nums[i]) {
					log.error("金色精魄不足，数量:{}，不能升级该高级科技,当前等级id:{}", goldenJingpo,
							kejiCfg.getId());
					return false;
				}
				wjKeJi.setGoldenJingPo(goldenJingpo - kejiCfg.nums[i]);
				updateTechInfo(wjKeJi, kejiType, kejiCfg.posId);
				log.info("{}升级高级武将科技{}到{} 消耗 {}", wjKeJi.getJunZhuId(),
						kejiCfg.id, kejiCfg.posId, kejiCfg.nums[i]);
			}
		}
		// data.setExp(exp);
		EventMgr.addEvent(ED.ACHIEVEMENT_PROCESS, new AchievementCondition(
				wjKeJi.getJunZhuId(), AchievementConstants.type_keji_low, 1));
		return true;
	}

	/**
	 * 升级低级科技
	 * 
	 * @param kejiType
	 * @param kejiCfg
	 * @param wjKeJi
	 * @param session
	 * @param code
	 * @param junZhu
	 * @return
	 */
	private boolean upgradeLowKeji(int kejiType, Keji kejiCfg, WjKeJi wjKeJi,
			IoSession session, int code, JunZhu junZhu) {
		// 检查君主等级是否达到升级科技的条件
		if (junZhu.level < kejiCfg.getLimitLevel()) {
			log.error("君主等级不够，不能继续升级该技能");
			error(session, code, "君主等级不够，不能继续升级该技能");
			return false;
		}

		int levelLimit = (kejiCfg.getLevel() / 5) * kejiCfg.getLevel();
		int other1level = 0;
		int other2level = 0;
		switch (kejiType) {
		case HeroMgr.ATTACK:
			other1level = id2Keji.get(wjKeJi.getTechIdByType(HeroMgr.DEFENSE))
					.getLevel();
			other2level = id2Keji.get(wjKeJi.getTechIdByType(HeroMgr.HP))
					.getLevel();
			break;
		case HeroMgr.DEFENSE:
			other1level = id2Keji.get(wjKeJi.getTechIdByType(HeroMgr.ATTACK))
					.getLevel();
			other2level = id2Keji.get(wjKeJi.getTechIdByType(HeroMgr.HP))
					.getLevel();
			break;
		case HeroMgr.HP:
			other1level = id2Keji.get(wjKeJi.getTechIdByType(HeroMgr.DEFENSE))
					.getLevel();
			other2level = id2Keji.get(wjKeJi.getTechIdByType(HeroMgr.ATTACK))
					.getLevel();
			break;
		}
		// 检查其他两个技能是否满足 升级该科技技能的条件
		if (other1level < levelLimit || other2level < levelLimit) {
			log.error("其他技能未达到条件");
			error(session, code, "另外两个低级科技技能等级未达到条件");
			return false;
		}
		// 检查升级冷却时间是否满足升级条件
		long span = wjKeJi.getCD() - System.currentTimeMillis();
		if (span <= 0) {
			wjKeJi.setCD(System.currentTimeMillis());
		} else if (span > 1 * 3600 * 1000) {
			log.error("冷却时间太长，不能升级科技");
			error(session, code, "冷却时间太长，不能升级科技");
			return false;
		}
		// 检查铜币
		int needTongbi = 0;
		int length = kejiCfg.items.length;
		for (int i = 0; i < length; i++) {
			if (kejiCfg.items[i] == TONGBI) {
				if (kejiCfg.nums[i] <= 0) {
					log.error("低级科技升级消耗配置错误:" + kejiCfg.id);
					continue;
				}
				needTongbi = kejiCfg.nums[i];
				if (junZhu.tongBi < needTongbi) {
					log.error("铜币不足，数量:{}，不能升级该低级科技,当前等级id:{}", needTongbi,
							kejiCfg.getId());
					error(session, 0, "铜币不足");
					return false;
				}
			}
		}
		// if(!checkConditions(wjKeJi, session, kejiCfg)){
		// return false;
		// }
		wjKeJi.setCD(wjKeJi.getCD() + kejiCfg.getCostTime());
		updateTechInfo(wjKeJi, kejiType, kejiCfg.posId);
		log.info("{}升级低级武将科技{}到{} 消耗铜币: {}", wjKeJi.getJunZhuId(), kejiCfg.id,
				kejiCfg.posId, needTongbi);
		junZhu.tongBi = junZhu.tongBi - needTongbi;
		HibernateUtil.update(junZhu);
		EventMgr.addEvent(ED.ACHIEVEMENT_PROCESS, new AchievementCondition(
				junZhu.id, AchievementConstants.type_keji_low, 1));
		JunZhuMgr.inst.sendMainInfo(session,junZhu,false);
		return true;
	}

	public void updateTechInfo(WjKeJi wjKeJi, int type, int techId) {
		switch (type) {
		case HeroMgr.ATTACK:
			wjKeJi.setAttack(techId);
			break;
		case HeroMgr.DEFENSE:
			wjKeJi.setDefense(techId);
			break;
		case HeroMgr.HP:
			wjKeJi.setHp(techId);
			break;
		case HeroMgr.ZHIMOU:
			wjKeJi.setZhiMou(techId);
			break;
		case HeroMgr.WUYI:
			wjKeJi.setWuYi(techId);
			break;
		case HeroMgr.TONGSHUAI:
			wjKeJi.setTongShuai(techId);
			break;
		default:
			log.error("{}武将科技升级失败{}{}{}", wjKeJi.getJunZhuId(), type, techId);
			break;
		}
	}

	private boolean checkConditions(WjKeJi tech, IoSession session, Keji keji) {
		boolean ret = false;
		switch (keji.getKejiType()) {
		case HeroMgr.ATTACK:
		case HeroMgr.DEFENSE:
		case HeroMgr.HP:
			ret = checkTongBi(session, keji);
			break;
		default:
			log.error("位置的类型id", keji.getKejiType());
			break;
		}
		return ret;
	}

	private boolean checkTongBi(IoSession session, Keji keji) {
		boolean ret = false;
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		for (int i = 0; i < keji.items.length; i++) {
			switch (keji.items[i]) {
			case TONGBI:
				ret = junZhu.tongBi >= keji.nums[i];
				break;
			default:
				log.error("unkown consume resource");
				break;
			}
		}
		return ret;
	}

	/**
	 * 获取武将科技信息。
	 * 
	 * @param code
	 * @param session
	 * @param builder
	 */
	private void getTechInfo(int code, IoSession session, Builder builder) {
		long junZhuId = getJunZhuIdBySession(session);
		WjKeJi techs = HibernateUtil.find(WjKeJi.class, junZhuId);
		if (techs == null) {
			techs = createDefaultBean(junZhuId);
		}

		WuJiangTech.Builder resp = WuJiangTech.newBuilder();

		WuJiangTechnologyDate.Builder tech = WuJiangTechnologyDate.newBuilder();

		tech.setTechType(techs.getAttack());
		resp.addTechnologyList(tech);

		tech = WuJiangTechnologyDate.newBuilder();
		tech.setTechType(techs.getDefense());
		resp.addTechnologyList(tech);

		tech = WuJiangTechnologyDate.newBuilder();
		tech.setTechType(techs.getHp());
		resp.addTechnologyList(tech);

		tech = WuJiangTechnologyDate.newBuilder();
		tech.setTechType(techs.getZhiMou());
		tech.setExp(techs.getZhiMouExp());
		resp.addTechnologyList(tech);

		tech = WuJiangTechnologyDate.newBuilder();
		tech.setTechType(techs.getWuYi());
		tech.setExp(techs.getWuYiExp());
		resp.addTechnologyList(tech);

		tech = WuJiangTechnologyDate.newBuilder();
		tech.setTechType(techs.getTongShuai());
		tech.setExp(techs.getTongShuaiExp());
		resp.addTechnologyList(tech);

		// 初始化该数值为0，但是应该是改为插入记录时的时间。为了兼容之前的旧账号。
		if (techs.getCD() == 0) {
			techs.setCD(System.currentTimeMillis());
		}
		long timeSpan = System.currentTimeMillis() - techs.getCD();
		resp.setGoldJingPo(techs.getGoldenJingPo());
		resp.setCold(timeSpan >= 0 ? 0 : -timeSpan);

		session.write(resp.build());
	}

	public WjKeJi createDefaultBean(long junZhuId) {
		WjKeJi techs;
		techs = new WjKeJi();
		techs.setAttack(1000);
		techs.setDefense(2000);
		techs.setHp(3000);
		techs.setWuYi(4000);
		techs.setZhiMou(5000);
		techs.setTongShuai(6000);
		techs.setJunZhuId(junZhuId);
		return techs;
	}

	private long getJunZhuIdBySession(IoSession session) {
		Long id = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (id == null) {
			return -1;
		}
		return id;
	}

	/**
	 * 错误处理。（完成）
	 * 
	 * @param session
	 * @param code
	 * @param msg
	 */
	private void error(IoSession session, int code, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(code);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	/**
	 * 获取配置表中科技数据。
	 * 
	 * @param id
	 * @return
	 */
	public Keji getKeJi(int id) {
		return id2Keji.get(id);
	}

	/**
	 * 加速武将科技CD时间
	 * 
	 * @param id
	 * @param session
	 */
	public void wuJiangTechSpeedUpCold(int id, IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("junzhu not found");
			return;
		}
		WjKeJi wjKeJi = HibernateUtil.find(WjKeJi.class, junZhu.id);
		if (wjKeJi == null) {
			error(session, 0, "你尚未开启武将科技。");
			return;
		}
		int remainSeconds = (int) ((wjKeJi.getCD() - System.currentTimeMillis()) / 1000);
		if (remainSeconds == 0) {
			log.error("低级武将科技升级剩余cd时间为0，不需要加速");
			return;
		}
		int needYuanBao = (int) ((remainSeconds + 179) / 180);
		if (junZhu.yuanBao < needYuanBao) {
			log.error("加速武将科技cd时间失败，剩余元宝不足");
			return;
		}
		YuanBaoMgr.inst.diff(junZhu, -needYuanBao, 0, needYuanBao,
				YBType.YB_JIASU_WJGKEJI_CD, "加速武将科技CD时间");
		wjKeJi.setCD(System.currentTimeMillis());
		HibernateUtil.save(junZhu);
		HibernateUtil.save(wjKeJi);
		WuJiangTechSpeedupResp.Builder resp = WuJiangTechSpeedupResp
				.newBuilder();
		resp.setYuanbaoNum(junZhu.yuanBao);
		session.write(resp.build());
	}
}
