package com.qx.achievement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AchievementProtos.AcheFinishInform;
import qxmobile.protobuf.AchievementProtos.AcheGetRewardRequest;
import qxmobile.protobuf.AchievementProtos.AcheGetRewardResponse;
import qxmobile.protobuf.AchievementProtos.AcheInfo;
import qxmobile.protobuf.AchievementProtos.AcheListResponse;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.Chengjiu;
import com.manu.network.SessionAttKey;
import com.qx.account.AccountManager;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

/**
 * 
 * @author lizhaowen
 * 
 */
public class AchievementMgr extends EventProc {

	private Logger logger = LoggerFactory.getLogger(Achievement.class);
	public static AchievementMgr instance;

	/** 成就列表 <成就类型, 该成就类型List<Chengjiu>> **/
	private Map<Integer, List<Chengjiu>> type2chengjiu;

	/** 需要功能开放才显示的成就类型 **/
	private int[] needOpenConditionAcheType = { 2, 3, 4, 12, 14, 15, 16, 17,
			19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 };

	public AchievementMgr() {
		instance = this;
		initData();
	}

	public void initData() {
		// 初始化成就配置文件列表
		List<Chengjiu> list = TempletService.listAll(Chengjiu.class
				.getSimpleName());
		if (list == null || list.size() == 0) {
			logger.error("找不到成就配置文件........");
			return;
		}
		Map<Integer, List<Chengjiu>> type2chengjiu = new HashMap<Integer, List<Chengjiu>>();
		for (Chengjiu chengjiu : list) {
			List<Chengjiu> typeList = type2chengjiu.get(chengjiu.getType());
			if (typeList == null) {
				typeList = new ArrayList<Chengjiu>();
				type2chengjiu.put(chengjiu.getType(), typeList);
			}
			typeList.add(chengjiu);
		}
		this.type2chengjiu = type2chengjiu;
	}

	/**
	 * 请求成就列表信息
	 * 
	 * @param cmd
	 * @param session
	 */
	public void acheListRequest(int cmd, IoSession session) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			logger.error("null junZhuId cmd:{}", cmd);
			return;
		}
		Map<Integer, Achievement> initAcheMap = getAchvList(junZhuId);
		if (initAcheMap.size() == 0 || initAcheMap == null) {
			logger.error("初始化成就信息出错，成就配置文件错误");
			return;
		}
		AcheListResponse.Builder response = AcheListResponse.newBuilder();
		for (Map.Entry<Integer, Achievement> entry : initAcheMap.entrySet()) {
			Achievement ache = entry.getValue();
			AcheInfo.Builder acheInfo = AcheInfo.newBuilder();
			acheInfo.setAchvId(ache.getChengjiuId());
			acheInfo.setIsFinish(ache.isFinish());
			acheInfo.setIsGet(ache.isGetReward());
			acheInfo.setJindu(ache.getJindu());
			response.addAcheInfo(acheInfo.build());
		}
		session.write(response.build());
	}

	/**
	 * 获取成就信息列表
	 * 
	 * @param junZhuId
	 * @return
	 */
	public Map<Integer, Achievement> getAchvList(long junZhuId) {
		Map<Integer, Achievement> initAcheList = new LinkedHashMap<Integer, Achievement>();
		Map<Integer, Achievement> dbAchesMap = getDBAchvs(junZhuId);
		// 获取每种成就类型第一个成就
		List<Chengjiu> chengjiuList = new ArrayList<Chengjiu>();
		for (Map.Entry<Integer, List<Chengjiu>> entry : type2chengjiu
				.entrySet()) {
			List<Chengjiu> list = entry.getValue();
			for (Chengjiu cj : list) {
				// 配置表preId为0，表示是该类中的第一个成就
				if (cj.getPreId() == 0) {
					chengjiuList.add(cj);
				}
			}
		}
		for (Chengjiu cj : chengjiuList) {
			Achievement achievement = new Achievement();
			achievement.setChengjiuId(cj.getId());
			achievement.setJindu(0);
			achievement.setGetReward(false);
			achievement.setJunZhuId(junZhuId);
			achievement.setFinish(false);
			achievement.setType(cj.getType());
			initAcheList.put(cj.getType(), achievement);
		}

		// 移除未开放的成就
		List<Integer> rmtype = new ArrayList<Integer>();
		for (Map.Entry<Integer, Achievement> entry : initAcheList.entrySet()) {
			int type = entry.getKey();
			// 判断需要开放条件的成就，是否在数据库里，不在数据库里则从列表中移除
			if (isHaveOpenCondition(type)) {
				if (!dbAchesMap.containsKey(type)) {
					rmtype.add(type);
				} else {
					initAcheList.put(type, dbAchesMap.get(type));
				}
			}
		}
		for (Integer key : rmtype) {
			initAcheList.remove(key);
		}
		return initAcheList;
	}

	/**
	 * 获取数据库里的成就列表
	 * 
	 * @param junZhuId
	 *            君主id
	 * @return
	 */
	private Map<Integer, Achievement> getDBAchvs(long junZhuId) {
		List<Achievement> dbAcheList = HibernateUtil.list(Achievement.class,
				" where junZhuId =" + junZhuId);
		if (dbAcheList == null) {
			dbAcheList = new ArrayList<Achievement>();
		}
		Map<Integer, Achievement> dbAchvs = new HashMap<Integer, Achievement>();
		for (Achievement ache : dbAcheList) {
			dbAchvs.put(ache.getType(), ache);
		}
		return dbAchvs;
	}

	/**
	 * 获取该类型成就是否需要开放条件
	 * 
	 * @param type
	 * @return true-需要,false-不需要
	 */
	private boolean isHaveOpenCondition(int type) {
		for (int element : needOpenConditionAcheType) {
			if (element == type) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACHIEVEMENT_PROCESS, this);
	}

	@Override
	public void proc(Event event) {
		// 2014年10月30日11:56:15，暂时关闭成就系统。
		if (true) {
			return;
		}
		if (event.param == null
				|| !(event.param instanceof AchievementCondition)) {
			logger.error("event param type is not Class--AchievementCondition");
			return;
		}
		AchievementCondition achvCondition = (AchievementCondition) event.param;
		switch (event.id) {
		case ED.ACHIEVEMENT_PROCESS:
			acheProcess(achvCondition);
			break;
		default:
			break;
		}
	}

	/**
	 * 成就处理
	 * 
	 * @param achvCondition
	 */
	public void acheProcess(AchievementCondition achvCondition) {
		long junZhuId = achvCondition.getJunzhuId();
		int achvType = achvCondition.getType();

		Achievement achievement = HibernateUtil.find(Achievement.class,
				" where junZhuId = " + junZhuId + " and type=" + achvType,
				false);
		List<Chengjiu> list = type2chengjiu.get(achvType);
		if (list == null) {
			logger.error("没有在配置文件中找到该成就类型，achvType:{}", achvType);
			return;
		}
		if (achievement == null) {
			for (Chengjiu chengjiu : list) {
				if (chengjiu.getPreId() == 0) {
					achievement = new Achievement();
					achievement.setChengjiuId(chengjiu.getId());
					achievement.setJunZhuId(junZhuId);
					achievement.setType(achvType);
					break;
				}
			}
		}
		if (achievement == null) {
			logger.error("沒有在配置文件中找到該成就類型的初始成就，achvType:{}", achvType);
			return;
		}
		int condition = achievement.getJindu() + achvCondition.getJinduAdd();
		achievement.setJindu(condition);
		Chengjiu chengjiu = null;
		for (Chengjiu cj : list) {
			if (achievement.getChengjiuId() == cj.getId()) {
				chengjiu = cj;
				break;
			}
		}
		if (chengjiu == null) {
			logger.error("在配置文件中找不到该成就。chengjiuId:{}",
					achievement.getChengjiuId());
			return;
		}
		// 判断是否完成
		if (condition >= chengjiu.getCondition()) {
			achievement.setFinish(true);
		}
		HibernateUtil.save(achievement);

		IoSession session = AccountManager.getIoSession(junZhuId);
		if (session != null) {
			AcheFinishInform.Builder response = AcheFinishInform.newBuilder();
			AcheInfo.Builder acheInfo = AcheInfo.newBuilder();
			acheInfo.setAchvId(achievement.getChengjiuId());
			acheInfo.setIsGet(achievement.isGetReward());
			acheInfo.setJindu(condition);
			acheInfo.setIsFinish(achievement.isFinish());
			response.setAcheInfo(acheInfo.build());
			session.write(response.build());
		} else {
			logger.error("找不到相对应的 IoSession，junzhuId:{}, 可能是从jsp页面上访问该方法",
					junZhuId);
		}
	}

	/**
	 * 领取成就奖励
	 * 
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void getAcheReward(int cmd, IoSession session, Builder builder) {
		AcheGetRewardRequest.Builder request = (qxmobile.protobuf.AchievementProtos.AcheGetRewardRequest.Builder) builder;
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			logger.error("null junZhuId， cmd:{}", cmd);
			return;
		}
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		int chengjiuId = request.getAcheId();
		Achievement achievement = HibernateUtil.find(Achievement.class,
				" where chengjiuId=" + chengjiuId + "and junZhuId=" + junZhuId);
		if (achievement == null) {
			logger.error("成就数据保存发生错误。chengjiuId:{},junzhuId:{}", chengjiuId,
					junZhuId);
			return;
		}
		if (!achievement.isFinish()) {
			logger.error("还未完成成就，chengjiuId:{}", chengjiuId);
			return;
		}
		Chengjiu chengjiu = null;
		List<Chengjiu> list = type2chengjiu.get(achievement.getType());
		for (Chengjiu cj : list) {
			if (chengjiuId == cj.getId()) {
				chengjiu = cj;
				break;
			}
		}
		if (chengjiu == null) {
			logger.error("在成就配置文件中找不到该条成就信息, chengjiuId:{}", chengjiuId);
			return;
		}
		int getYuanBao = chengjiu.getYuanbao();
		YuanBaoMgr.inst.diff(junzhu, getYuanBao, 0, 0, YBType.YB_GET_REWARD,
				"领取成就奖励");
		achievement.setGetReward(true);

		AcheGetRewardResponse.Builder response = AcheGetRewardResponse
				.newBuilder();
		response.setAcheId(chengjiuId);
		response.setStatus(true);
		response.setMsg("领取成功");
		response.setYuanbao(getYuanBao);
		// 判断是否还有下一个成就
		if (chengjiu.getNextId() != 0) {
			achievement.setChengjiuId(chengjiu.getNextId());
			achievement.setFinish(false);
			achievement.setGetReward(false);
			AcheInfo.Builder acheInfo = AcheInfo.newBuilder();
			acheInfo.setAchvId(chengjiu.getNextId());
			acheInfo.setJindu(achievement.getJindu());
			acheInfo.setIsFinish(achievement.isFinish());
			acheInfo.setIsGet(achievement.isGetReward());
			response.setAcheInfo(acheInfo.build());
		}
		session.write(response.build());
		HibernateUtil.save(achievement);
		HibernateUtil.save(junzhu);
	}
}
