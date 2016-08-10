package com.qx.alliance;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.LianMeng;
import com.manu.dynasty.template.LianMengKeZhan;
import com.manu.dynasty.template.LianmengEvent;
import com.manu.dynasty.template.LianmengIcon;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.building.JianZhuLvBean;
import com.qx.alliance.building.JianZhuLvBeanDao;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.friends.GreetMgr;
import com.qx.guojia.GuoJiaMgr;
import com.qx.huangye.HYMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.prompt.PromptMsgMgr;
import com.qx.prompt.SuBaoConstant;
import com.qx.purchase.PurchaseMgr;
import com.qx.pvp.LveDuoMgr;
import com.qx.pvp.PvpMgr;
import com.qx.ranking.RankingGongJinMgr;
import com.qx.ranking.RankingMgr;
import com.qx.timeworker.FunctionID;
import com.qx.util.TableIDCreator;
import com.qx.world.FightScene;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import log.ActLog;
import qxmobile.protobuf.AllianceProtos;
import qxmobile.protobuf.AllianceProtos.AgreeApply;
import qxmobile.protobuf.AllianceProtos.AgreeApplyResp;
import qxmobile.protobuf.AllianceProtos.AgreeInvite;
import qxmobile.protobuf.AllianceProtos.AllianceHaveResp;
import qxmobile.protobuf.AllianceProtos.AllianceInvite;
import qxmobile.protobuf.AllianceProtos.AllianceInviteResp;
import qxmobile.protobuf.AllianceProtos.AllianceNonResp;
import qxmobile.protobuf.AllianceProtos.AllianceTargetResp;
import qxmobile.protobuf.AllianceProtos.ApplicantInfo;
import qxmobile.protobuf.AllianceProtos.ApplyAlliance;
import qxmobile.protobuf.AllianceProtos.ApplyAllianceResp;
import qxmobile.protobuf.AllianceProtos.CancelJoinAlliance;
import qxmobile.protobuf.AllianceProtos.CancelJoinAllianceResp;
import qxmobile.protobuf.AllianceProtos.ChangeAllianceCountry;
import qxmobile.protobuf.AllianceProtos.ChangeAllianceCountryResp;
import qxmobile.protobuf.AllianceProtos.CheckAllianceName;
import qxmobile.protobuf.AllianceProtos.CheckAllianceNameResp;
import qxmobile.protobuf.AllianceProtos.CreateAlliance;
import qxmobile.protobuf.AllianceProtos.CreateAllianceResp;
import qxmobile.protobuf.AllianceProtos.DismissAllianceResp;
import qxmobile.protobuf.AllianceProtos.DonateHuFu;
import qxmobile.protobuf.AllianceProtos.DonateHuFuResp;
import qxmobile.protobuf.AllianceProtos.DownTitle;
import qxmobile.protobuf.AllianceProtos.DownTitleResp;
import qxmobile.protobuf.AllianceProtos.EventListResp;
import qxmobile.protobuf.AllianceProtos.ExitAllianceResp;
import qxmobile.protobuf.AllianceProtos.FindAlliance;
import qxmobile.protobuf.AllianceProtos.FindAllianceResp;
import qxmobile.protobuf.AllianceProtos.FireMember;
import qxmobile.protobuf.AllianceProtos.FireMemberResp;
import qxmobile.protobuf.AllianceProtos.GetAllianceLevelAward;
import qxmobile.protobuf.AllianceProtos.GetAllianceLevelAwardResp;
import qxmobile.protobuf.AllianceProtos.InviteAllianceInfo;
import qxmobile.protobuf.AllianceProtos.InviteList;
import qxmobile.protobuf.AllianceProtos.LookApplicantsResp;
import qxmobile.protobuf.AllianceProtos.LookMembersResp;
import qxmobile.protobuf.AllianceProtos.MemberInfo;
import qxmobile.protobuf.AllianceProtos.NonAllianceInfo;
import qxmobile.protobuf.AllianceProtos.OpenApply;
import qxmobile.protobuf.AllianceProtos.OpenApplyResp;
import qxmobile.protobuf.AllianceProtos.PlayerAllianceState;
import qxmobile.protobuf.AllianceProtos.RefuseApply;
import qxmobile.protobuf.AllianceProtos.RefuseApplyResp;
import qxmobile.protobuf.AllianceProtos.RefuseInvite;
import qxmobile.protobuf.AllianceProtos.RefuseInviteResp;
import qxmobile.protobuf.AllianceProtos.TransferAlliance;
import qxmobile.protobuf.AllianceProtos.TransferAllianceResp;
import qxmobile.protobuf.AllianceProtos.UpTitle;
import qxmobile.protobuf.AllianceProtos.UpTitleResp;
import qxmobile.protobuf.AllianceProtos.UpdateNotice;
import qxmobile.protobuf.AllianceProtos.UpdateNoticeResp;
import qxmobile.protobuf.AllianceProtos.UpgradeLevelInfoResp;
import qxmobile.protobuf.AllianceProtos.UpgradeLevelResp;
import qxmobile.protobuf.AllianceProtos.UpgradeLevelSpeedUpResp;
import qxmobile.protobuf.AllianceProtos.immediatelyJoin;
import qxmobile.protobuf.AllianceProtos.immediatelyJoinResp;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;

public class AllianceMgr extends EventProc{
	public Logger logger = LoggerFactory.getLogger(AllianceMgr.class);
	public static AllianceApply defaultAllianceApply = new AllianceApply();
	public static AllianceMgr inst;
	public List<LianmengIcon> iconList;
	/** 联盟等级 配置信息<level, LianMeng> **/
	public Map<Integer, LianMeng> lianMengMap;
	public Map<Integer, LianmengEvent> lianmengEventMap;
	
	public static final int NONE_ALLIANCE_ID = 0;

	/** 盟主 **/
	public static final int TITLE_LEADER = 2;
	/** 副盟主 **/
	public static final int TITLE_DEPUTY_LEADER = 1;
	/** 成员 **/
	public static final int TITLE_MEMBER = 0;

	/** 报名联盟战的权限 */
	public static int[] SIGN_UP_ALLIANCEFIGHT = {TITLE_LEADER, TITLE_DEPUTY_LEADER};

	/** 打开招募 **/
	public static final int OPEN_APPLY = 1;
	/** 关闭招募 **/
	public static final int CLOSE_APPLY = 0;
	/** 加入同一联盟从离开到下次间隔时间，单位-毫秒*/
	public static long JOIN_SAME_INTEVAL = 24L * 60 * 60 * 1000;
	/** 联盟名最大长度 */
	public static int NAME_LENGTH_MAX = 7;
	/** 事件记录最大数量 */
	public static int EVENT_MAX_NUM = 100;
	/** 事件记录每页大小 */
	public static int EVENT_PAGE_SIZE = 20;

	/** 联盟成员的主键缓存的key的前缀 **/
	public String CACHE_MEMBERS_OF_ALLIANCE = "MembersOfAlliance:id:";
	/** 联盟申请成员的主键缓存的key的前缀 **/
	public String CACHE_APPLYERS_OF_ALLIANCE = "ApplyersOfAlliance:id:";
	/** 联盟名字集合*/
	public static final String ALLIANCE_NAMES = "ALLIANCE_NAMES";
	/** 退出联盟玩家id前缀*/
	public static final String ALLIANCE_EXIT = "alliance_exit_";
	public static final String ALLIANCE_CHANGE_GUOJIA = "alliance_change_guojia_";
	public static final String EXIT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String INVITE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	/** 联盟事件前缀 */
	public static final String ALLIANCE_EVENT = "alliance_event_";
	
	
	/** 联盟建设值锁 **/
	public static final Object buildLock = new Object();
	public static final Object reputationLock  = new Object();
	public static final Object hufuLock = new Object();
	
	/** 联盟等级奖励默认开始等级 **/
	public static final int LEVEL_AWARD_DEFAULT = 2;

	public AllianceMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		iconList = TempletService.getInstance().listAll(LianmengIcon.class.getSimpleName());
		List<LianMeng> lianMengList = TempletService.getInstance().listAll(
				LianMeng.class.getSimpleName());
		Map<Integer, LianMeng> lianMengMap = new HashMap<Integer, LianMeng>();
		for (LianMeng lm : lianMengList) {
			lianMengMap.put(lm.lv, lm);
		}
		this.lianMengMap = lianMengMap;

		Map<Integer, LianmengEvent> lianmengEventMap = new HashMap<Integer, LianmengEvent>();
		List<LianmengEvent> eventList = TempletService.getInstance().listAll(LianmengEvent.class.getSimpleName());
		for(LianmengEvent event : eventList) {
			lianmengEventMap.put(event.ID, event);
		}
		this.lianmengEventMap = lianmengEventMap;
	}

	public Map<Integer, LianMeng> getLianMengMap() {
		return lianMengMap;
	}

	public void requestAllianceInfo(int cmd, IoSession session, Builder builder) {
		// 检查君主
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}

		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (member == null || member.lianMengId <= 0) {
			processNonAlliance(cmd, session, junZhu.id, junZhu.guoJiaId);
		} else {// 有联盟
			processHaveAlliance(junZhu, cmd, session, member);
			BigSwitch.inst.houseMgr.isCanLingqufangwuExp(junZhu, session);		// 为了联盟小屋小红点提示
		}
	}

	public void processHaveAlliance(JunZhu junZhu, int cmd, IoSession session,
			AlliancePlayer member) {
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(member.lianMengId);
		if (alncBean == null) {
			logger.error("找不到联盟，id:{}", member.lianMengId);
			return;
		}
		AllianceHaveResp.Builder response = AllianceHaveResp.newBuilder();
		fillAllianceResponse(alncBean, response, member);
		session.write(response.build());
	}
	/**
	 * 联盟信息变化时发送
	 * 
	 * @param junZhu
	 * @param session
	 * @param member 可以为 null
	 * @param alncBean 可以为 null
	 */
	public void sendAllianceInfo(JunZhu junZhu, IoSession session,
			AlliancePlayer member, AllianceBean alncBean){
		if(junZhu == null) return;
		if(member == null){
			member = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		}
		if(member == null || member.lianMengId <= 0) return;
		if(alncBean == null){
			alncBean = AllianceBeanDao.inst.getAllianceBean(member.lianMengId);
		}
		if(alncBean == null) return;
		AllianceHaveResp.Builder response = AllianceHaveResp.newBuilder();
		fillAllianceResponse(alncBean, response, member);
		session.write(response.build());
	}

	public void processNonAlliance(int cmd, IoSession session, long junzhuId,
			int country) {
		List<AllianceBean> alncList = HibernateUtil.list(AllianceBean.class,
				" where isAllow=" + OPEN_APPLY);
		AllianceNonResp.Builder resp = AllianceNonResp.newBuilder();
		AllianceApply alncApply = HibernateUtil.find(AllianceApply.class,
				junzhuId);
		for (AllianceBean alncBean : alncList) {
			if (alncBean.creatorId % 1000 != GameServer.serverId)
				continue;// 过滤掉不是本服务器的联盟//正式服务器数据库分开就没有这个问题。
//			if (country != alncBean.country) {// 过滤不是同一个国家的
//				continue;
//			}
			NonAllianceInfo.Builder alncInfo = NonAllianceInfo.newBuilder();
			fillNonAllianceInfo(alncInfo, alncBean, alncApply, junzhuId);
			resp.addAlincInfo(alncInfo);
		}
		resp.setNeedYuanbao(CanShu.LIANMENG_CREATE_COST);
		session.write(resp.build());
	}

	public NonAllianceInfo.Builder fillNonAllianceInfo(
			NonAllianceInfo.Builder alncInfos, AllianceBean alncBean,
			AllianceApply alncApply, long junzhuId) {
		alncInfos.setId((int) alncBean.id);
		alncInfos.setName(alncBean.name);
		alncInfos.setIcon(alncBean.icon);
		alncInfos.setLevel(alncBean.level);
		alncInfos.setReputation(alncBean.reputation);
		alncInfos.setMembers(alncBean.members);
		int memberMax = getAllianceMemberMax(alncBean.id);
		alncInfos.setMemberMax(memberMax);
		alncInfos.setApplyLevel(alncBean.minApplyLevel);
		alncInfos.setJunXian(alncBean.minApplyJunxian);
		alncInfos.setAttchCndition(alncBean.attach);
		alncInfos.setIsApplied(alncApply == null ? false : alncApply.isAllianceExist(alncBean.id));
		JunZhu mengZhuJZ = HibernateUtil.find(JunZhu.class, alncBean.creatorId);
		alncInfos.setCreatorName(mengZhuJZ.name);
		alncInfos.setExp(alncBean.exp);
		alncInfos.setIsShenPi(alncBean.isShenPi);
		alncInfos.setCountry(alncBean.country);
		alncInfos.setIsCanApply(canApply(junzhuId, alncBean.id));
		int cityCount = getCaptureCityCount(alncBean.id);
		alncInfos.setCityCount(cityCount);
		return alncInfos;
	}

	public boolean canApply(long junzhuId, int lianMengId) {
		int remainTime = getCanJoinRemainTime(junzhuId, lianMengId);
		if(remainTime <= 0) {
			Redis.getInstance().hdel(ALLIANCE_EXIT + junzhuId, String.valueOf(lianMengId));
			return true;
		}
		return false;
	}

	public int getCaptureCityCount(int lianMengId) {
		String sql = "select count(*) from CityBean where lmId=" + lianMengId;
		int count = HibernateUtil.getCount(sql);
		return count <= -1 ? 0 : count;
	}

	public void sendError(int cmd, IoSession session, String msg) {
		if (session == null) {
			logger.warn("session is null: {}", msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	public void checkAllianceName(int id, IoSession session, Builder builder) {
		CheckAllianceName.Builder request = (qxmobile.protobuf.AllianceProtos.CheckAllianceName.Builder) builder;
		String alncName = request.getName();
		if (alncName == null || alncName.equals("")) {
			logger.error("联盟名不能为空");
			return;
		}
		
		CheckAllianceNameResp.Builder response = CheckAllianceNameResp.newBuilder();
		if (BigSwitch.inst.accMgr.hasSensitiveWord(alncName)) {
			response.setCode(2);
			logger.error("联盟名字中不能有敏感词汇");
			session.write(response.build());
			return;
		}
		
		if(alncName.length() > NAME_LENGTH_MAX) {
			response.setCode(2);
			session.write(response.build());
			logger.error("联盟名字太长");
			return;
		}
		
		boolean nameExist = isAllianceNameExist(alncName);
		if (nameExist) {
			response.setCode(1);
			logger.error("联盟名字:{}已被占用", alncName);
			session.write(response.build());
			return;
		}
		response.setCode(0);
		response.setMsg("名字可用");
		session.write(response.build());
	}

	public void createAlliance(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("创建联盟失败-未发现君主，cmd:{}", cmd);
			return;
		}

		CreateAlliance.Builder request = (qxmobile.protobuf.AllianceProtos.CreateAlliance.Builder) builder;
		String allianceName = request.getName();
		int icon = request.getIcon();
		int guoJia = request.getGuoJia();
		
		boolean guoJiaExist = GuoJiaMgr.inst.verifyGuoJiaExist((byte) guoJia);
		if(!guoJiaExist) {
			logger.error("创建联盟失败，选择的国家id:{}", guoJia);
			sendCreateAllianceResp(session, 330, "请选择一个国家！", null);
			return;
		}
		
		boolean isOpen = FunctionOpenMgr.inst.isFunctionOpen(FunctionOpenMgr.TYPE_ALLIANCE, junZhu.id, junZhu.level);
		if(!isOpen) {
			logger.error("创建联盟失败-联盟功能未开放");
			sendCreateAllianceResp(session, 7, "联盟功能未开放", null);
			return;
		}
		
		if (allianceName == null || allianceName.equals("")) {
			logger.error("创建联盟失败-联盟名不能为空");
			sendCreateAllianceResp(session, 8, "联盟名不能为空", null);
			return;
		}
		
		allianceName = allianceName.trim();
		if (BigSwitch.inst.accMgr.hasSpecial(allianceName)) {
			logger.error("创建联盟失败-有非法字符 name:{}", allianceName);
			sendCreateAllianceResp(session, 3, "仅限使用中/英文以及数字！", null);
			return;
		}
		if (BigSwitch.inst.accMgr.hasSensitiveWord(allianceName)) {
			logger.error("创建联盟失败-有敏感字符 name:{}", allianceName);
			sendCreateAllianceResp(session, 6, "输入的名称包含敏感词！", null);
			return;
		}
		
		if(allianceName.length() > NAME_LENGTH_MAX) {
			logger.error("创建联盟失败-联盟名字太长 name:{}", allianceName);
			sendCreateAllianceResp(session, 4, "输入的名称过长！", null);
			return;
		}
		
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (member != null && member.lianMengId > 0) {
			logger.error("创建联盟失败-玩家:{}已经有联盟id:{}！", junZhu.name, member.lianMengId);
			sendCreateAllianceResp(session, 9, "已经有联盟", null);
			return;
		}
		
		boolean nameExist = isAllianceNameExist(allianceName);
		if (nameExist) {
			logger.error("创建联盟失败-联盟名:{}已被占用！", allianceName);
			sendCreateAllianceResp(session, 1, "该名称已被其他联盟使用！", null);
			return;
		}
		Redis.getInstance().sadd(ALLIANCE_NAMES, allianceName);

		LianmengIcon iconCfg = null;
		for (LianmengIcon li : iconList) {
			if (li.icon == icon) {
				iconCfg = li;
				break;
			}
		}
		if (iconCfg == null) {
			logger.error("创建联盟失败-联盟图标选择有误，iconId:{}", icon);
			sendCreateAllianceResp(session, 5, "找不到所选的图标", null);
			return;
		}

		Date date = new Date();
		LianMeng lmConfig = lianMengMap.get(1);
		if (lmConfig == null) {
			logger.error("创建联盟失败-联盟配置文件有误，等级:{}", 1);
			Redis.getInstance().sremove(ALLIANCE_NAMES, allianceName);
			return;
		}

		if (junZhu.yuanBao < CanShu.LIANMENG_CREATE_COST) {
			logger.error("创建联盟失败-元宝不足，君主:{}有元宝:{},需要:{}", junZhu.id, junZhu.yuanBao, CanShu.LIANMENG_CREATE_COST);
			Redis.getInstance().sremove(ALLIANCE_NAMES, allianceName);
			sendCreateAllianceResp(session, 2, "元宝不足", null);
			return;
		}

		AllianceBean alncBean = new AllianceBean();
		alncBean.id = ((int) TableIDCreator.getTableID(AllianceBean.class, 10000L));
		alncBean.name = allianceName;
		alncBean.icon = icon;
		alncBean.level = 1;
		alncBean.creatorId = junZhu.id;
		alncBean.createTime = date;
		alncBean.upgradeCurLevelTime = date;
		alncBean.country = guoJia;
		alncBean.isAllow = OPEN_APPLY;
		alncBean.members = 1;
		alncBean.minApplyLevel = CanShu.JION_ALLIANCE_LV_MINI;
		alncBean.minApplyJunxian = 1;// 军衔保存
		alncBean.isShenPi = 1;		// 默认不需要审批
		alncBean.status = AllianceConstants.STATUS_NORMAL;
		AllianceBeanDao.inst.save(alncBean);
//		HibernateUtil.insert(alncBean);

		// 插入联盟成员表
		if (member == null) {
			member = new AlliancePlayer();
			initAlliancePlayerInfo(junZhu.id, alncBean.id, member, TITLE_LEADER);
			HibernateUtil.insert(member);
		} else {
			member.lianMengId = alncBean.id;
			member.title = TITLE_LEADER;
			member.getTitleTime = date;
			member.joinTime = date;
			HibernateUtil.save(member);
		}
		AlliancePlayerDao.inst.joinAlliance(member, alncBean.id);
		YuanBaoMgr.inst.diff(junZhu, -CanShu.LIANMENG_CREATE_COST, 0,
				CanShu.LIANMENG_CREATE_COST, YBType.YB_CHUANGJIAN_LIANGMENG,
				"创建联盟");
		junZhu.guoJiaId = guoJia;
		HibernateUtil.update(junZhu);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		AllianceHaveResp.Builder alncInfo = AllianceHaveResp.newBuilder();
		// 移除申请过的联盟
		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, junZhu.id);
		if (applyer != null && applyer != defaultAllianceApply) {
			clearApplyAllianceCache(applyer);
			HibernateUtil.save(applyer);
		}
		logger.info("junzhu:{}在时间:{}创建了联盟id:{},花费元宝:{},国家{}", junZhu.name, date,
				alncBean.id, CanShu.LIANMENG_CREATE_COST,guoJia);
		ActLog.log.Guild(junZhu.id, junZhu.name, "CREATE", alncBean.id, alncBean.name, alncBean.level, "");
		session.setAttribute(SessionAttKey.LM_NAME, allianceName);
		session.setAttribute(SessionAttKey.LM_INFO, member);
		fillAllianceResponse(alncBean, alncInfo, member);
		sendCreateAllianceResp(session, 0, "联盟创建成功", alncInfo);
		
		Redis.getInstance().sadd(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id, member.junzhuId + "");
		String eventStr = lianmengEventMap.get(1).str.replaceFirst("%d", junZhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30013);
		if (mailConfig != null) {
			EmailMgr.INSTANCE.sendMail(junZhu.name, mailConfig.content, "",
					mailConfig.sender, mailConfig, "");
		}
		EventMgr.addEvent(junZhu.id, ED.Join_LM, new Object[] { junZhu.id, alncBean.id, alncBean.name, member.title});
		//刷新相关红点
		EventMgr.addEvent(junZhu.id, ED.REFRESH_TIME_WORK, session);
		RankingMgr.inst.resetLianMengLevelRedis(alncBean.id, 1);
		EventMgr.addEvent(junZhu.id, ED.LIANMENG_RANK_REFRESH, new Integer(alncBean.id));
		EventMgr.addEvent(junZhu.id, ED.LIANMENG_DAY_RANK_REFRESH, new Object[]{alncBean,0});
		EventMgr.addEvent(junZhu.id, ED.LIANMENG_WEEK_RANK_REFRESH, new Object[]{alncBean,0});
	
//		GuoJiaMgr.inst.calculateGongJinJoinAlliance(junZhu.id);
		
		RankingGongJinMgr.inst.firstSetGongJin(junZhu.id, alncBean.id);
		RankingGongJinMgr.inst.firstSetAllianceGongJin(junZhu.id, junZhu.level,alncBean.id);
		
	}

	public boolean isAllianceNameExist(String allianceName) {
		boolean nameExist = false;
		// 先从缓存判断
		boolean cacheExist = false;
		Set<String> names = Redis.getInstance().sget(ALLIANCE_NAMES);
		if(names != null) {
			for(String name : names) {
				if(name.equalsIgnoreCase(allianceName)) {
					cacheExist = true;
					nameExist = true;
				}
			}
		}
		// 缓存不存在从数据库里查询
		if(!cacheExist) {
			AllianceBean alliance = HibernateUtil.find(AllianceBean.class, " where name='" + allianceName + "'", false);
			if(alliance != null) {
				nameExist = true;
				Redis.getInstance().sadd(ALLIANCE_NAMES, allianceName);
			}
		}  
		return nameExist;
	}

	public void sendCreateAllianceResp(IoSession session, int result,
			String msg, AllianceHaveResp.Builder alncInfo) {
		CreateAllianceResp.Builder response = CreateAllianceResp.newBuilder();
		response.setCode(result);
		response.setMsg(msg);
		if (alncInfo != null) {
			response.setAllianceInfo(alncInfo.build());
		}
		session.write(response.build());
	}

	public int getAllianceMemberMax(int lianMengId) {
		JianZhuLvBean bean = JianZhuLvBeanDao.inst.getJianZhuBean(lianMengId);
		if(bean == null) {
			bean = JianZhuMgr.inst.insertJianZhuLvBean(lianMengId, bean);
		}
		final int keZhanLv = bean.keZhanLv;
		List<LianMengKeZhan> list = TempletService.listAll(LianMengKeZhan.class.getSimpleName());
		Optional<LianMengKeZhan> optional = list.stream().filter(item -> item.keZhanLevel == keZhanLv).findFirst();
		LianMengKeZhan conf = optional.get();
		if(conf == null) {
			logger.error("获取联盟最大人数错误，找不到联盟客栈等级为:{}的配置", bean.keZhanLv);
			return 50;
		}
		return conf.renshuMax;
	}
	
	/**
	 * 初始化联盟成员信息
	 * @param junZhuId			君主id
	 * @param lianMengId		联盟id，如果是在没有联盟的情况进行初始化，则 lianMengId <= 0
	 * @param alncPlayer		联盟成员对象
	 * @param title				职位
	 */
	public void initAlliancePlayerInfo(long junZhuId, int lianMengId, AlliancePlayer alncPlayer, int title) {
		Date date = new Date();
		alncPlayer.junzhuId = junZhuId;
		alncPlayer.lianMengId = lianMengId;
		alncPlayer.title = title;
		alncPlayer.getTitleTime = date;
		alncPlayer.gongXian = 0;
		alncPlayer.isBaoming = AllianceConstants.BAOMING_FALSE;
		alncPlayer.isVoted = AllianceConstants.VOTED_FALSE;
		alncPlayer.joinTime = date;
		alncPlayer.curMonthFirstTime = date;
		alncPlayer.curMonthGongXian = 0;
		IoSession session = AccountManager.getIoSession(junZhuId);
		if(session != null) {
			session.setAttribute(SessionAttKey.LM_INFO, alncPlayer);
		}
	}
	
	public void fillAllianceResponse(AllianceBean alncBean,
			AllianceHaveResp.Builder alncInfo, AlliancePlayer alncPlayer) {
		int title = alncPlayer == null ? -1 : alncPlayer.title;
		int gongXian = alncPlayer == null ? 0 : alncPlayer.gongXian;
		LianMeng lmCfg = lianMengMap.get(alncBean.level);
		int curExp = alncBean.exp;
		int nextLevelExp = lmCfg.exp;
		if (alncBean.level >= lianMengMap.size()) {// 表示已经满级
			nextLevelExp = lianMengMap.get(alncBean.level - 1).exp;
			curExp = nextLevelExp;
		}
		// 根据升级时间计算等级
		int upgradeRemainTime = -1;
		Date nowDate = new Date();
		if(alncBean.upgradeTime != null) {
			if(!nowDate.before(alncBean.upgradeTime)) {
				alncBean.level += 1;
				alncBean.upgradeTime = null;
				alncBean.upgradeCurLevelTime = nowDate;
				alncBean.upgradeUsedTimes = 0;
				changeAllianceApplyState(alncBean);
				HibernateUtil.save(alncBean);
				logger.info("联盟:{}升级成功，等级从:{}升级到:{}",alncBean.id, alncBean.level-1, alncBean.level);
				EventMgr.addEvent(alncBean.id,ED.LIANMENG_UPGRADE_LEVEL,  new Integer(alncBean.id));
				EventMgr.addEvent(alncBean.id,ED.LIANMENG_RANK_REFRESH, new Object[]{alncBean.id, alncBean.country});
			} else {
				upgradeRemainTime = (int) ((alncBean.upgradeTime.getTime() - nowDate.getTime()) / 1000);
			}
		} 
		if(alncBean.lastTimesUpgradeSpeedTime != null && 
				DateUtils.isTimeToReset(alncBean.lastTimesUpgradeSpeedTime, CanShu.REFRESHTIME_PURCHASE)) {
			alncBean.todayUpgradeSpeedTimes = 0;
			HibernateUtil.save(alncBean);
		}
		alncInfo.setUpgradeRemainTime(upgradeRemainTime);
		alncInfo.setName(alncBean.name);
		alncInfo.setId((int) alncBean.id);
		alncInfo.setLevel(alncBean.level);
		alncInfo.setExp(curExp);
		alncInfo.setNeedExp(nextLevelExp);
		alncInfo.setBuild(alncBean.build);
		alncInfo.setMembers(alncBean.members);
		int memberMax = getAllianceMemberMax(alncBean.id);
		alncInfo.setMemberMax(memberMax);
		alncInfo.setContribution(gongXian);
		alncInfo.setNotice(alncBean.notice);
		alncInfo.setIcon(alncBean.icon);
		alncInfo.setIdentity(title);
		alncInfo.setCountry(alncBean.country);
		alncInfo.setShengWang(alncBean.reputation);
		JunZhu mengZhuJZ = HibernateUtil.find(JunZhu.class, alncBean.creatorId);
		alncInfo.setMengzhuName(mengZhuJZ.name);
		alncInfo.setMengZhuOfflineTime(SessionManager.inst.getOfflineTime(mengZhuJZ.id));
		alncInfo.setIsAllow(alncBean.isAllow);
		alncInfo.setApplyLevel(alncBean.minApplyLevel);
		alncInfo.setJunXian(alncBean.minApplyJunxian);
		alncInfo.setAttchCndition(alncBean.attach);
		alncInfo.setIsShenPi(alncBean.isShenPi);
		alncInfo.setStatus(alncBean.status);
		if (alncPlayer != null) {
			alncInfo.setIsBaoming(alncPlayer.isBaoming);
			alncInfo.setIsVoted(alncPlayer.isVoted);
			alncInfo.setVoteJunzhuId(alncPlayer.voteJunzhuId);
			alncInfo.setIsVoteDialog(alncPlayer.isVoteDialog);
			if (alncPlayer.isVoteDialog == AllianceConstants.VOTE_DIALOG) {
				alncPlayer.isVoteDialog = AllianceConstants.VOTE_DIALOG_NOT;
				HibernateUtil.save(alncPlayer);
			}
		}
		int jiFen = RankingGongJinMgr.inst.getJunZhuGongJin(alncPlayer.junzhuId);
		alncInfo.setJiFen(jiFen);
		int gongXun = ShopMgr.inst.getMoney(ShopMgr.Money.gongXun, alncPlayer.junzhuId, null);
		alncInfo.setGongXun(gongXun);
		List<MemberInfo> allMemberInfo = getMemberInfo(alncPlayer.junzhuId);
		alncInfo.addAllMemberInfo(allMemberInfo);
		//AllianceLevelAward levelAward = getAllianceLevelAward(alncPlayer.junzhuId);//没有联盟目标了，等级默认发1
		alncInfo.setLmTargetLevel(1);
		alncInfo.setHufuNum(alncBean.hufuNum);
		alncInfo.setOnlineNum(getLianMengOnlineNum(alncBean.id));
		int remainSpeedUpTimes = lmCfg.speedUpTimes - alncBean.upgradeUsedTimes;
		alncInfo.setSpeedUpRemainTimes(remainSpeedUpTimes <= 0 ? 0 : remainSpeedUpTimes);
	}

	/**
	 * 获取联盟在线人数数量
	 * @param lianMengId
	 * @return
	 */
	public int getLianMengOnlineNum(int lianMengId) {
		List<SessionUser> list = SessionManager.inst.getAllSessions();
		int count = 0;
		for(SessionUser su : list) {
			if(su.session == null) {
				continue;
			}
			AlliancePlayer alliancePlayer = (AlliancePlayer) su.session.getAttribute(SessionAttKey.LM_INFO);
			if(alliancePlayer != null && alliancePlayer.lianMengId == lianMengId) {
				count += 1;
			}
		}
		return count;
	}

	public void findAlliance(int cmd, IoSession session, Builder builder) {
		long junzhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		FindAlliance.Builder request = (qxmobile.protobuf.AllianceProtos.FindAlliance.Builder) builder;
		String name = request.getName();

		FindAllianceResp.Builder response = FindAllianceResp.newBuilder();
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, " where name= '" + name + "'");
		// 过滤掉不是本服务器的联盟, 正式服务器数据库分开就没有这个问题。
		if (alncBean == null || alncBean.creatorId % 1000 != GameServer.serverId) {
			logger.error("查找联盟失败，未找到名字为:{} 的联盟信息", name);
			response.setCode(1);
			response.setMsg("很遗憾，找不到这个联盟...");
			session.write(response.build());
			return;
		}
		int memberMax = getAllianceMemberMax(alncBean.id);
		if(alncBean.members >= memberMax) {
			alncBean.isAllow = CLOSE_APPLY;
			HibernateUtil.save(alncBean);
		}
		NonAllianceInfo.Builder alncInfo = NonAllianceInfo.newBuilder();
		fillNonAllianceInfo(alncInfo, alncBean, null, junzhuId);
		response.setCode(0);
		response.setIsAllow(alncBean.isAllow);
		response.setAllianceInfo(alncInfo.build());
		session.write(response.build());
	}

	public void applyAlliance(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		// 0-成功，1-已经申请，2-没有此联盟，3-联盟人数已满，4-军衔等级不够，5-君主等级不够 ，6-可以申请的联盟数量已满，
		// 7-联盟未开启招募
		ApplyAlliance.Builder request = (qxmobile.protobuf.AllianceProtos.ApplyAlliance.Builder) builder;
		int lianmengId = request.getId();
		
		int remainTime = getCanJoinRemainTime(junZhu.id, lianmengId);
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(lianmengId);
		if (alncBean == null) {
			sendApplyAllianceResponse(session, 2, lianmengId, "很遗憾，找不到这个联盟...", remainTime);
			logger.error("联盟申请失败，未找到对应的联盟信息，id:{}", lianmengId);
			return;
		}
		
		if(remainTime <= 0) {
			Redis.getInstance().hdel(ALLIANCE_EXIT + junZhu.id, String.valueOf(lianmengId));
		} else {
			sendApplyAllianceResponse(session, 9, lianmengId, "距上次离开该联盟冷却时间未到", remainTime);
			logger.error("联盟申请失败，距上次离开该联盟冷却时间未到");
			return;
		}
		
		/*if (alncBean.country != junZhu.guoJiaId) {
			sendApplyAllianceResponse(session, 8, lianmengId, "不能申请其他国家的联盟", remainTime);
			logger.error("联盟申请失败，玩家-{}:国家-{}, 联盟-{}:国家-{}", junZhu.id,
					junZhu.guoJiaId, alncBean.id, alncBean.country);
			return;
		}*/
		
		if (alncBean.isAllow == CLOSE_APPLY) {
			logger.error("联盟申请失败，要加入的联盟:{}未开启招募!", lianmengId);
			sendApplyAllianceResponse(session, 7, lianmengId, "申请的联盟关闭的招募", remainTime);
			return;
		}
		
		int memberMax = getAllianceMemberMax(alncBean.id);
		if (alncBean.members >= memberMax) {
			logger.error("联盟申请失败，该联盟人数已满，id:{}", lianmengId);
			sendApplyAllianceResponse(session, 3, lianmengId, "该联盟人数已满", remainTime);
			return;
		}
		
		if (junZhu.level < alncBean.minApplyLevel) {
			sendApplyAllianceResponse(session, 5, lianmengId, "", remainTime);
			logger.error("联盟申请失败，君主:{}等级:{}不符合入盟要求等级:{}", junZhu.name, junZhu.level,alncBean.minApplyLevel);
			return;
		}
		
		int junXianLevel = PvpMgr.inst.getJunxianLevel(junZhu.id);
		if (junXianLevel < alncBean.minApplyJunxian) {
			sendApplyAllianceResponse(session, 4, lianmengId, "", remainTime);
			logger.error("联盟申请失败，君主:{}军衔等级:{}不符合入盟要求军衔等级:{}", junZhu.name,junXianLevel, alncBean.minApplyJunxian);
			return;
		}

		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, junZhu.id);
		if (applyer == null || applyer == defaultAllianceApply) {
			applyer = new AllianceApply();
			applyer.junzhuId = junZhu.id;
			applyer.addAllianceId(alncBean.id);
			Cache.allianceApplyCache.put(junZhu.id, applyer);
			HibernateUtil.insert(applyer);
		} else {
			if (applyer.isAllianceExist(alncBean.id)) {
				sendApplyAllianceResponse(session, 1, lianmengId, "已经申请了该联盟", remainTime);
				logger.error("联盟申请失败，君主:{}已经申请了联盟:{}}", junZhu.id, alncBean.id);
				return;
			}
			if (applyer.getAllianceNum() >= AllianceConstants.APPPLY_NUM_MAX) {
				sendApplyAllianceResponse(session, 6, lianmengId, "联盟申请数量已满", remainTime);
				logger.error("联盟申请失败，君主:{}申请了联盟数量已经满了数量:{}", junZhu.id, applyer.getAllianceNum());
				return;
			}
			applyer.addAllianceId(alncBean.id);
			HibernateUtil.save(applyer);
		}
		Redis.getInstance().sadd(CACHE_APPLYERS_OF_ALLIANCE + alncBean.id,
				"" + applyer.junzhuId);
		logger.info("联盟申请成功，{}申请加入联盟{}", junZhu.id, alncBean.id);
		sendApplyAllianceResponse(session, 0, lianmengId, "申请成功", remainTime);

		List<AlliancePlayer> members = HibernateUtil.list(AlliancePlayer.class,
				" where lianMengId= " + lianmengId + " and title in (" + TITLE_LEADER
						+ "," + TITLE_DEPUTY_LEADER + ")");
		for (AlliancePlayer member : members) {
			IoSession isession = AccountManager.getIoSession(member.junzhuId);
			if (isession != null) {
				isession.write(PD.ALLIANCE_HAVE_NEW_APPLYER);
				FunctionID.pushCanShowRed(member.junzhuId, isession, FunctionID.LianMengShenQing);
			}
		}
	}

	public void sendApplyAllianceResponse(IoSession session, int result,
			int allianceId, String msg, int remainTime) {
		ApplyAllianceResp.Builder response = ApplyAllianceResp.newBuilder();
		response.setCode(result);
		response.setId(allianceId);
		response.setMsg(msg);
		response.setRemainTime(remainTime);
		session.write(response.build());
	}

	public void cancelJoinAlliance(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}

		CancelJoinAlliance.Builder request = (qxmobile.protobuf.AllianceProtos.CancelJoinAlliance.Builder) builder;
		int lianmengId = request.getId();

		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(lianmengId);
		if (alncBean == null) {
			logger.error("取消联盟申请失败，未找到对应的联盟信息，id:{}", lianmengId);
			sendCancelJoinAllianceResp(session, 1, lianmengId, "很遗憾，找不到这个联盟...");
			return;
		}

		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, junZhu.id);
		if (applyer == null || applyer == defaultAllianceApply) {
			logger.error("取消联盟申请失败，未找到对应的联盟申请信息，junzhu:{},联盟id:{}", junZhu.name, lianmengId);
		} else {
			applyer.removeAlliance(alncBean.id);
			HibernateUtil.save(applyer);
		}
		Redis.getInstance().sremove(CACHE_APPLYERS_OF_ALLIANCE + alncBean.id,
				"" + junZhu.id);
		logger.info("取消联盟申请成功，junzhu:{}取消了联盟id:{}的加入申请", junZhu.name, lianmengId);
		sendCancelJoinAllianceResp(session, 0, lianmengId, "取消联盟申请成功");
		
		// 若联盟申请列表为空了，则发送申请信息，以配合前台去掉申请信息的红点
		List<AllianceApply> applyList = getApplyers(alncBean.id);
		if(applyList.size() == 0) {
			List<AlliancePlayer> members = HibernateUtil.list(AlliancePlayer.class,
					" where lianMengId= " + lianmengId + 
					" and title in (" + TITLE_LEADER + "," + TITLE_DEPUTY_LEADER + ")");
			for (AlliancePlayer member : members) {
				IoSession isession = AccountManager.getIoSession(member.junzhuId);
				if (isession != null) {
					sendLookApplicantsResp(session, applyList);
				}
			}
		}
	}

	public void sendCancelJoinAllianceResp(IoSession session, int result,
			int lianmengId, String msg) {
		CancelJoinAllianceResp.Builder response = CancelJoinAllianceResp.newBuilder();
		response.setCode(result);
		response.setId(lianmengId);
		response.setMsg(msg);
		session.write(response.build());
	}

	public synchronized void exitAlliance(int cmd, IoSession session, Builder builder) {
		// code:0-成功，1-失败:没有该联盟，2-失败:不是该联盟成员,3-失败:盟主不能退盟
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer exitMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (exitMember == null || exitMember.lianMengId <= 0) {
			logger.error("退出联盟失败，君主:{}还不是联盟成员", cmd);
			sendExitAllianceResp(session, 2);
			return;
		}

		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean( exitMember.lianMengId);
		if (alncBean == null) {
			logger.error("退出联盟失败，找不到联盟:{}", exitMember.lianMengId);
			sendExitAllianceResp(session, 1);
			return;
		}
		if (alncBean.creatorId == junZhu.id) {
			logger.error("退出联盟失败，盟主不能退出联盟:{} 君主:{}", exitMember.lianMengId, junZhu.id);
			sendExitAllianceResp(session, 3);
			return;
		}
		if (alncBean.id != exitMember.lianMengId) {
			logger.error("退出联盟失败，君主:{}不是联盟:{}的成员，属于联盟:{}", junZhu.id, alncBean.id, exitMember.lianMengId);
			sendExitAllianceResp(session, 2);
			return;
		}
		// resetAlliancePlayer 之前，总结计算一次贡金 add 20150915
//		GuoJiaMgr.inst.calculateGongJinBeforeQuitAlliance(exitMember.junzhuId);
		// 退出联盟。贡金为0
		RankingGongJinMgr.inst.setGongJinTo0(exitMember.junzhuId, alncBean.id);

		resetAlliancePlayer(alncBean, exitMember);
		AlliancePlayerDao.inst.exitAlliance(exitMember.junzhuId, alncBean.id);
		HibernateUtil.save(exitMember);
		alncBean.members -= 1;
		changeAllianceApplyState(alncBean);
		HibernateUtil.save(alncBean);
		Redis.getInstance().sremove(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id,
				exitMember.junzhuId + "");
		// 移除在荒野奖励库的申请
		String jzCacheKey = HYMgr.CACHE_HYSTORE_APPLY + alncBean.id + "_" + junZhu.id;
		String site = Redis.getInstance().get(jzCacheKey);
		if(site != null) {
			Redis.getInstance().del(jzCacheKey);
			Redis.getInstance().del(jzCacheKey);
			Redis.getInstance().lrem(
					HYMgr.CACHE_HYSTORE_APPLY + alncBean.id + "_" + site,
					0, "" + junZhu.id);
		}
		Date date = new Date();
		logger.info("退出联盟成功，君主:{} 退出联盟:{},时间:{}", junZhu.name, alncBean.name, date);
		session.removeAttribute(SessionAttKey.LM_NAME);
		session.removeAttribute(SessionAttKey.LM_ZHIWU);
		session.setAttribute(SessionAttKey.LM_INFO, exitMember);
		ActLog.log.Guild(junZhu.id, junZhu.name, "OUT", alncBean.id, alncBean.name, alncBean.level, "自主退出");
		sendExitAllianceResp(session, 0);
		int breforeGuoJia = junZhu.guoJiaId;
		junZhu.guoJiaId = 0;
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		// 记录玩家退出的联盟及时间
		Redis.getInstance().hset(ALLIANCE_EXIT + junZhu.id, String.valueOf(alncBean.id),
				DateUtils.date2Text(date, EXIT_TIME_FORMAT));
		String eventStr = lianmengEventMap.get(4).str.replaceFirst("%d", junZhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30020);
		if (mailConfig != null) {
			EmailMgr.INSTANCE.sendMail(junZhu.name, mailConfig.content, "",
					mailConfig.sender, mailConfig, "");
		}

		EventMgr.addEvent(junZhu.id, ED.Leave_LM, new Object[] { junZhu.id, alncBean.id,
				"***", alncBean.level, exitMember.title });
		refreshJunZhuPerRank(junZhu, breforeGuoJia);
	}

	public void sendExitAllianceResp(IoSession session, int code) {
		ExitAllianceResp.Builder response = ExitAllianceResp.newBuilder();
		response.setCode(0);
		session.write(response.build());
	}

	public void lookMembers(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("junzhu: {}，未加入联盟", junZhu.name);
			return;
		}

		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			logger.error("联盟没有找到，id {}", mgrMember.lianMengId);
			sendError(cmd, session, "该联盟不存在");
			return;
		}
		if (alncBean.id != mgrMember.lianMengId) {
			logger.error("君主:{}不在联盟:{}内，不能查看成员信息", junZhu.name, alncBean.name);
			return;
		}

		LookMembersResp.Builder response = LookMembersResp.newBuilder();
		List<MemberInfo> allMemberInfo = getAllMemberInfoList(alncBean.id);
		if (alncBean.members != allMemberInfo.size()) {
			alncBean.members = allMemberInfo.size();
			HibernateUtil.save(alncBean);
		}
		response.addAllMemberInfo(allMemberInfo);
		session.write(response.build());
	}

	/**
	 * 获取联盟所有的成员
	 * 
	 * @param alncId
	 * @return
	 */
	public Set<AlliancePlayer> getAllianceMembers(int alncId) {
		return AlliancePlayerDao.inst.getMembers(alncId);
	}
	
	public List<MemberInfo> getMemberInfo(long junzhuId) {
		AlliancePlayer alliancePlayer = getAlliancePlayer(junzhuId);
		if(alliancePlayer == null) {
			return null;
		}
		Set<AlliancePlayer> memberList = new HashSet<>(1);
		memberList.add(alliancePlayer);
		return fillMemberInfoList(memberList);
	}
	
	
	public List<MemberInfo> getAllMemberInfoList(int allianceId) {
		Set<AlliancePlayer> memberList = getAllianceMembers(allianceId);
		return fillMemberInfoList(memberList);
	}

	public List<MemberInfo> fillMemberInfoList(Set<AlliancePlayer> memberList) {
		List<AllianceProtos.MemberInfo> memberInfoRespList = new ArrayList<AllianceProtos.MemberInfo>();
		String ids =	memberList.stream()
				.map(m->String.valueOf(m.junzhuId))
				.collect(Collectors.joining(","));
		String hql = "select id,name,level,roleId,logoutTime"
				+ " from JunZhu jz join PlayerTime pt"
				+ " on jz.id=pt.junzhuId"
				+ " where id in ("+ids+")";
		List<Object[]> list = (List<Object[]>) HibernateUtil.querySql(hql);
		Map<Long, Object[]> jzMap = list.stream()
				.collect(Collectors.toMap(arr->((BigInteger)arr[0]).longValue(),
						arr->{BigInteger id = (BigInteger) arr[0];
							arr[0] = id.longValue();
							return arr;}
						));
		for (AlliancePlayer member : memberList) {
			MemberInfo.Builder memberInfo = MemberInfo.newBuilder();
			long junzhuId = member.junzhuId;
			//JunZhu jz = HibernateUtil.find(JunZhu.class, junzhuId);
			//PvpBean pvpBean = HibernateUtil.find(PvpBean.class, junzhuId);
			Object[] jzInfo = jzMap.get(junzhuId);
			fillMemberInfo(member, memberInfo, jzInfo);
			memberInfoRespList.add(memberInfo.build());
		}
		return memberInfoRespList;
	}

	/**
	 * 填充联盟成员信息
	 * 
	 * @param member
	 * @param memberInfo
	 * @param jz
	 * @param pvpBean
	 */
	public void fillMemberInfo(AlliancePlayer member,
			MemberInfo.Builder memberInfo,Object[] jzInfo) {
		memberInfo.setLevel((Integer)jzInfo[2]);
		memberInfo.setName((String)jzInfo[1]);
		memberInfo.setContribution(member.gongXian);
		memberInfo.setJunXian(1);//PvpMgr.getJunxianLevel(jz.id));
		memberInfo.setIdentity(member.title);
		memberInfo.setJunzhuId((Long)jzInfo[0]);
		memberInfo.setIsBaoming(member.isBaoming);
		memberInfo.setIsVoted(member.isVoted);
		memberInfo.setVoteNum(member.voteNum);
		memberInfo.setRoleId((Integer)jzInfo[3]);
		int gongJin = RankingGongJinMgr.inst.getJunZhuGongJin(member.junzhuId);
//		int gongJin = GuoJiaMgr.inst.getGongJin(gjBean, PvpMgr.getJunxianLevel(member.junzhuId));
//		if(GuoJiaMgr.inst.isCanShangjiao(member.junzhuId)){
//			GuoJiaMgr.inst.pushCanShangjiao(member.junzhuId);
//		}
		memberInfo.setGongJin(gongJin);
		long jzId = memberInfo.getJunzhuId();
		JunZhuInfoRet.Builder jzB = JunZhuMgr.jzInfoCache.get(jzId);
		int zhanLi = jzB==null?0:jzB.getZhanLi();
		memberInfo.setZhanLi(zhanLi);
		if(SessionManager.inst.isOnline(jzId)) {
			memberInfo.setOfflineTime(-1);
		} else {
			int offlineTime = getOfflineTime(jzInfo);
			memberInfo.setOfflineTime(offlineTime);
		}
		memberInfo.setCurMonthGongXian(member.curMonthGongXian);
	}
	public int getOfflineTime(Object[] jzInfo) {
		if(jzInfo == null) {
			//log.error("找不到君主:{}的playerTime信息", junZhuId);
			return 0;
		}
		Date date = new Date();
		Date logoutDate = (Date)jzInfo[4];
		if(logoutDate == null) {
			return 0;
		}
		if(logoutDate.after(date)) {
			return 0;
		}
		int offlineTime = (int) ((date.getTime() - logoutDate.getTime()) / 1000);
		return offlineTime;
	}
	public synchronized void fireMember(int cmd, IoSession session, Builder builder) {
		FireMember.Builder request = (qxmobile.protobuf.AllianceProtos.FireMember.Builder) builder;
		long memberId = request.getJunzhuId();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mengZhu = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mengZhu == null || mengZhu.lianMengId <= 0) {
			logger.error("开除联盟成员失败，君主:{}未加入联盟", junZhu.id);
			sendError(cmd, session, "你不是联盟成员");
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mengZhu.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mengZhu)) {
			logger.error("开除联盟成员失败，君主:{}没有权限，职位:{}", junZhu.id, mengZhu.title);
			return;
		}
		if (memberId == junZhu.id) {
			logger.error("开除联盟成员失败，不能开除自己", junZhu.id, mengZhu.title);
			sendError(cmd, session, "盟主不能开除自己");
			return;
		}
		AlliancePlayer target = AllianceMgr.inst.getAlliancePlayer(memberId);
		if (target == null) {
			logger.error("开除联盟成员失败，君主成员Id:{}不在联盟:{}中不能开除操作", memberId, alncBean.name);
			sendFireMemberResp(session, 1, alncBean.id, memberId);
			return;
		}
		if (target.lianMengId != mengZhu.lianMengId) {
			logger.error("开除联盟成员失败，该成员id:{}已经退出联盟:{}", memberId, alncBean.id);
			sendFireMemberResp(session, 1, alncBean.id, memberId);
			return;
		}
		// resetAlliancePlayer 之前，总结计算一次贡金 add 20150915
	//	GuoJiaMgr.inst.calculateGongJinBeforeQuitAlliance(target.junzhuId);
		RankingGongJinMgr.inst.setGongJinTo0(target.junzhuId, alncBean.id);

		resetAlliancePlayer(alncBean, target);
		AlliancePlayerDao.inst.exitAlliance(target.junzhuId, alncBean.id);
		HibernateUtil.save(target);
		alncBean.members -= 1;
		changeAllianceApplyState(alncBean);
		HibernateUtil.save(alncBean);
		Redis.getInstance().sremove(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id,
				"" + target.junzhuId);
		Date date = new Date();
		logger.info("开除联盟成员成功，{}开除成员{},时间:{}", mengZhu.junzhuId, target.junzhuId, date);
		ActLog.log.Guild(target.junzhuId, "", "OUT", alncBean.id, alncBean.name, alncBean.level, "开除");
		ActLog.log.GuildOut(mengZhu.junzhuId, "", alncBean.id, alncBean.name, target.junzhuId,  "");
		sendFireMemberResp(session, 0, alncBean.id, memberId);
		// 发送邮件通知
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30004);
		JunZhu firedJunzhu = HibernateUtil.find(JunZhu.class, memberId);
		EmailMgr.INSTANCE.sendMail(firedJunzhu.name, mailConfig.content, "",
				mailConfig.sender, mailConfig, "");
		
		// 记录玩家离开联盟的时间
		Redis.getInstance().hset(ALLIANCE_EXIT + target.junzhuId, String.valueOf(alncBean.id),
				DateUtils.date2Text(date, EXIT_TIME_FORMAT));
		String eventStr = lianmengEventMap.get(3).str.replaceFirst("%d", firedJunzhu.name)
													.replaceFirst("%d", junZhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		
		// 向被开除并在线的玩家发送通知
		IoSession su = AccountManager.sessionMap.get(target.junzhuId);
		int beforeGuojia = firedJunzhu.guoJiaId;
		firedJunzhu.guoJiaId = 0;
		if (su != null) {
			su.write(PD.ALLIANCE_FIRE_NOTIFY);
			su.removeAttribute(SessionAttKey.LM_NAME);
			su.removeAttribute(SessionAttKey.LM_ZHIWU);
			su.setAttribute(SessionAttKey.LM_INFO, target);
			JunZhuMgr.inst.sendMainInfo(su,firedJunzhu);
		}else{
			HibernateUtil.update(firedJunzhu);
		}

		EventMgr.addEvent(target.junzhuId, ED.Leave_LM, new Object[] { target.junzhuId,
				alncBean.id, "***", alncBean.level, target.title });
		refreshJunZhuPerRank(firedJunzhu, beforeGuojia);
	}

	public void sendFireMemberResp(IoSession session, int result,
			int lianmengId, long memberId) {
		FireMemberResp.Builder response = FireMemberResp.newBuilder();
		response.setResult(result);
		response.setId(lianmengId);
		response.setJunzhuId(memberId);
		session.write(response.build());
	}
	
	/**
	 * 改变联盟招募状态， 调用此方法，需要在保存 alliance对象之前
	 * @param alliance
	 * @param lianMengCfg
	 */
	public void changeAllianceApplyState(AllianceBean alliance) {
		int maxMember = getAllianceMemberMax(alliance.id);
		if(alliance.members >= maxMember && alliance.isAllow != CLOSE_APPLY) {
			alliance.isAllow = CLOSE_APPLY;
		} else if(alliance.isAllow != OPEN_APPLY) {
			alliance.isAllow = OPEN_APPLY;
		}
	}
	
	
	/**
	 * 验证盟主操作
	 * 
	 */
	public boolean verifyIllegalOper(int cmd, IoSession session,
			AllianceBean alncBean, AlliancePlayer mengZhu) {
		if (alncBean == null) {
			logger.error("联盟不存在");
			return false;
		}

		if (mengZhu == null) {
			logger.error("联盟操作验证：非法操作102");
			return false;
		}
		if (mengZhu.title != TITLE_LEADER) {
			logger.error("联盟操作验证：非法操作103");
			return false;
		}
		if (mengZhu.lianMengId != alncBean.id) {
			logger.error("联盟操作验证：非法操作104");
			return false;
		}
		return true;
	}

	public void upTitle(int cmd, IoSession session, Builder builder) {
		UpTitle.Builder request = (qxmobile.protobuf.AllianceProtos.UpTitle.Builder) builder;
		long memberId = request.getJunzhuId();

		JunZhu curMgr = JunZhuMgr.inst.getJunZhu(session);
		if (curMgr == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		JunZhu targetJz = HibernateUtil.find(JunZhu.class, memberId);
		if (targetJz == null) {
			logger.error("联盟成员升职失败，未发现君主:{}", memberId);
			return;
		}
		
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(curMgr.id);
		if (mgrMember == null) {
			logger.error("联盟成员升职失败，君主:{}未加入联盟", curMgr.name);
			return;
		}

		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mgrMember)) {
			logger.error("联盟成员升职失败，君主:{}没有权限1，职位:{}", curMgr.name, mgrMember.title);
			return;
		}

		AlliancePlayer target = AllianceMgr.inst.getAlliancePlayer(memberId);
		if (target == null || target.lianMengId != mgrMember.lianMengId) {
			logger.error("联盟成员升职失败，将要升职的君主id:{}已经退出联盟:{}", memberId, alncBean.id);
			sendUpTitleResp(session, 2, mgrMember.lianMengId, memberId, -1);
			return;
		}
		int title = target.title;
		if (title == TITLE_DEPUTY_LEADER) {
			logger.error("联盟成员升职失败，成员:{}不能再升职1，职位:{}", memberId, target.title);
			//sendError(cmd, session, "副盟主不能再升职");
			return;
		} else if (title != TITLE_MEMBER) {
			logger.error("联盟成员升职失败，成员:{}不能再升职2，职位:{}", memberId, target.title);
			//sendError(cmd, session, "非法操作");
			return;
		}
		
		if(isAllianceInFight(alncBean.id)) {
			logger.error("联盟成员升职失败，有盟员正在参加联盟战");
			sendUpTitleResp(session, 3, mgrMember.lianMengId, memberId, -1);
			return;
		}
		// 验证副盟主数量
		LianMeng lmCfg = lianMengMap.get(alncBean.level);
		if (lmCfg == null) {
			logger.error("联盟成员升职失败，找不到联盟配置文件。联盟等级:{}", alncBean.level);
			sendError(cmd, session, "配置文件出错");
			return;
		}
		
		if (alncBean.deputyLeaderNum >= lmCfg.fumeng) {
			logger.error("联盟成员升职失败，联盟:{}副盟主数量已达:{}", alncBean.name, lmCfg.fumeng);
			sendUpTitleResp(session, 1, mgrMember.lianMengId, memberId, target.title);
			return;
		}

		target.title = TITLE_DEPUTY_LEADER;
		target.getTitleTime = new Date();
		HibernateUtil.save(target);
		
		alncBean.deputyLeaderNum += 1;
		HibernateUtil.save(alncBean);
		logger.info("联盟成员升职成功，盟主:{}升职了成员:{}职位为:{}", mgrMember.junzhuId, target.junzhuId, target.title);
		sendUpTitleResp(session, 0, mgrMember.lianMengId, memberId, target.title);
		String eventStr = lianmengEventMap.get(6).str.replaceFirst("%d", targetJz.name)
				.replaceFirst("%d", "成员").replaceFirst("%d", "副盟主");
		addAllianceEvent(alncBean.id, eventStr);
		IoSession targetSession = SessionManager.getInst().getIoSession(targetJz.id);
		if(targetSession != null) {
			targetSession.setAttribute(SessionAttKey.LM_INFO, target);
			sendAllianceInfo(targetJz, targetSession, target, alncBean);
		}
		
		EventMgr.addEvent(targetJz.id,ED.LM_TITLE_CHANGE, new Object[] {targetJz.id, alncBean.id, alncBean.name, target.title});

	}

	public void sendUpTitleResp(IoSession session, int result, int lianmengId,
			long memberId, int title) {
		UpTitleResp.Builder response = UpTitleResp.newBuilder();
		response.setCode(result);
		response.setJunzhuId(memberId);
		response.setTitle(title);
		response.setId(lianmengId);
		session.write(response.build());
	}

	public void downTitle(int cmd, IoSession session, Builder builder) {
		DownTitle.Builder request = (qxmobile.protobuf.AllianceProtos.DownTitle.Builder) builder;
		long memberId = request.getJunzhuId();

		JunZhu curMgr = JunZhuMgr.inst.getJunZhu(session);
		if (curMgr == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}

		JunZhu targetJz = HibernateUtil.find(JunZhu.class, memberId);
		if (targetJz == null) {
			logger.error("联盟成员降职失败，找不到君主:{}", memberId);
			return;
		}
		
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(curMgr.id);
		if (mgrMember == null) {
			logger.error("联盟成员降职失败，君主:{}未加入联盟", curMgr.name, cmd);
			return;
		}

		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean( mgrMember.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mgrMember)) {
			logger.error("联盟成员降职失败，君主id:{}没有权限1，职位:{}", memberId, mgrMember.title);
			return;
		}

		AlliancePlayer target = AllianceMgr.inst.getAlliancePlayer(memberId);
		if (target == null || target.lianMengId != mgrMember.lianMengId) {
			logger.error("联盟成员降职失败，将要降职的君主id:{}已经退出联盟:{}", memberId, alncBean.id);
			sendDownTitleResp(session, 1, mgrMember.lianMengId, memberId, -1);
			return;
		}
		
		if (target.title == TITLE_MEMBER) {
			logger.error("联盟成员降职失败，成员id:{}不能再降职1，职位:{}", memberId, target.title);
			return;
		} else if (target.title != TITLE_DEPUTY_LEADER) {
			logger.error("联盟成员降职失败，成员id:{}不能再降职2，职位:{}", memberId, target.title);
			return;
		}
		
		if(isAllianceInFight(alncBean.id)) {
			logger.error("联盟成员降职失败，有盟员正在参加联盟战");
			sendDownTitleResp(session, 2, mgrMember.lianMengId, memberId, -1);
			return;
		}
		target.title = TITLE_MEMBER;
		target.getTitleTime = new Date();
		HibernateUtil.save(target);
		
		alncBean.deputyLeaderNum -= 1;
		alncBean.deputyLeaderNum = Math.max(0, alncBean.deputyLeaderNum);
		HibernateUtil.save(alncBean);
		
		logger.info("联盟成员降职成功，盟主:{}降职了成员:{}", mgrMember.junzhuId, target.junzhuId);
		sendDownTitleResp(session, 0, mgrMember.lianMengId, memberId, target.title);
		String eventStr = lianmengEventMap.get(7).str.replaceFirst("%d", targetJz.name)
				.replaceFirst("%d", "副盟主").replaceFirst("%d", "成员");
		addAllianceEvent(alncBean.id, eventStr);
		IoSession targetSession = SessionManager.getInst().getIoSession(targetJz.id);
		if(targetSession != null) {
			targetSession.setAttribute(SessionAttKey.LM_INFO, target);
			sendAllianceInfo(targetJz, targetSession, target, alncBean);
		}
		EventMgr.addEvent(targetJz.id, ED.LM_TITLE_CHANGE, new Object[] {targetJz.id, alncBean.id, alncBean.name, target.title});
	}

	public void sendDownTitleResp(IoSession session, int result,
			int lianmengId, long junzhuId, int title) {
		DownTitleResp.Builder response = DownTitleResp.newBuilder();
		response.setCode(result);
		response.setId(lianmengId);
		response.setJunzhuId(junzhuId);
		response.setTitle(title);
		session.write(response.build());
	}
	
	/**
	 * 获取可以加入该联盟的剩余时间，单位-秒
	 * @param junzhuId
	 * @param lianMengId
	 * @return <=0表示可以加入了， >表示剩余时间
	 */
	public int getCanJoinRemainTime(long junzhuId, int lianMengId) {
		String exitTime = Redis.getInstance().hget(ALLIANCE_EXIT + junzhuId, String.valueOf(lianMengId));
		if(exitTime == null || "".equals(exitTime)) {
			return 0;
		}
		Date exitDate = DateUtils.text2Date(exitTime, EXIT_TIME_FORMAT);
		Date curDate = new Date();
		return (int) ((JOIN_SAME_INTEVAL + exitDate.getTime() - curDate.getTime()) / 1000);
	}
	
	
	public void lookApplicants(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null) {
			logger.error("查看联盟申请列表失败，找不到君主:{}", junZhu.id);
			return;
		}
		int lianmengId = mgrMember.lianMengId;
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(lianmengId);
		if (alncBean == null) {
			logger.error("查看联盟申请列表失败，找不到联盟:{}", lianmengId);
			sendError(cmd, session, "该联盟不存在");
			return;
		}
		if (mgrMember.title != TITLE_LEADER && mgrMember.title != TITLE_DEPUTY_LEADER) {
			logger.error("查看联盟申请列表失败，君主:{}没有权限，职位:{}", junZhu.id, mgrMember.title);
			sendError(cmd, session, "没有权限");
			return;
		}

		List<AllianceApply> applyList = getApplyers(alncBean.id);
		sendLookApplicantsResp(session, applyList);
	}

	public void sendLookApplicantsResp(IoSession session, List<AllianceApply> applyList) {
		LookApplicantsResp.Builder response = LookApplicantsResp.newBuilder();
		for (AllianceApply alncApply : applyList) {
			ApplicantInfo.Builder info = ApplicantInfo.newBuilder();
			long jzId = alncApply.junzhuId;
			JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
			info.setJunzhuId(jzId);
			info.setLevel(jz.level);
			info.setName(jz.name);
			info.setRoleId(jz.roleId);
			int zhanLi = JunZhuMgr.inst.getZhanli(jz);
			info.setZhanLi(zhanLi);
			int junxianLevel = PvpMgr.inst.getJunxianLevel(jz.id);
			info.setJunXian(junxianLevel);
			if (junxianLevel == -1) {
				info.setRank(Short.MAX_VALUE);// FIXME 默认rank
			} else {
				info.setRank(PvpMgr.inst.getPvpRankById(jz.id));
			}
			response.addApplicanInfo(info.build());
		}
		session.write(response.build());
	}

	public void refuseApply(int cmd, IoSession session, Builder builder) {
		RefuseApply.Builder request = (qxmobile.protobuf.AllianceProtos.RefuseApply.Builder) builder;
		long jzId = request.getJunzhuId();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		
		JunZhu applyJunzhu = HibernateUtil.find(JunZhu.class, jzId);
		if (applyJunzhu == null) {
			logger.error("拒绝联盟申请失败，未找到申请者，jzID:{}", jzId);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null) {
			logger.error("拒绝联盟申请失败，找不到君主:{}", junZhu.id);
			return;
		}
		int lianmengId = mgrMember.lianMengId;
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(lianmengId);
		if (alncBean == null) {
			logger.error("拒绝联盟申请失败，找不到联盟:{}", junZhu.id);
			sendError(cmd, session, "该联盟不存在");
			return;
		}

		int title = mgrMember.title;
		if (title != TITLE_LEADER && title != TITLE_DEPUTY_LEADER) {
			logger.error("拒绝联盟申请失败，君主:{}没有权限，职位:{}", junZhu.id, mgrMember.title);
			sendError(cmd, session, "没有权限");
			return;
		}
		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, jzId);
		if (applyer == null ||applyer == defaultAllianceApply ||!applyer.isAllianceExist(alncBean.id)) {
			logger.error("拒绝联盟申请失败，申请数据未找到 jzId {} LMid {}", jzId, lianmengId);
			sendError(cmd, session, "该玩家没有在联盟申请列表中");
			return;
		}
		applyer.removeAlliance(alncBean.id);
		HibernateUtil.save(applyer);
		Redis.getInstance().sremove(CACHE_APPLYERS_OF_ALLIANCE + alncBean.id,
				"" + applyer.junzhuId);
		logger.info("拒绝联盟申请成功，君主:{}拒绝了玩家:{}的申请", junZhu.id, applyJunzhu.id);
		RefuseApplyResp.Builder response = RefuseApplyResp.newBuilder();
		response.setResult(0);
		response.setId(lianmengId);
		response.setJunzhuId(jzId);
		session.write(response.build());
		
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30018);
		if (mailConfig != null) {
			String content = mailConfig.content.replace("***", alncBean.name);
			EmailMgr.INSTANCE.sendMail(applyJunzhu.name, content, "",
					mailConfig.sender, mailConfig, "");
		}
	}

	public synchronized void agreeApply(int cmd, IoSession session, Builder builder) {
		AgreeApply.Builder request = (qxmobile.protobuf.AllianceProtos.AgreeApply.Builder) builder;
		long applyJzId = request.getJunzhuId();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		// 0-成功，1-失败，没有权限，2-联盟人数已满，3-该玩家加入其他联盟或取消申请
		// 权限验证
		AlliancePlayer mengZhu = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mengZhu == null) {
			logger.error("同意联盟申请失败，君主:{}不是联盟成员", junZhu.id);
			return;
		}
		int lianmengId = mengZhu.lianMengId;
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(lianmengId);
		if (alncBean == null) {
			logger.error("同意联盟申请失败，联盟:{}不存在", lianmengId);
			return;
		}
		int memberMax = getAllianceMemberMax(alncBean.id);
		if (alncBean.members >= memberMax) {
			logger.error("同意联盟申请失败，联盟:{}人数已满,数量:{}等级:{}", alncBean.id, alncBean.members, alncBean.level);
			sendAgreeApplyResp(session, 2, applyJzId, lianmengId, null);
			return;
		}

		int title = mengZhu.title;
		if (title != TITLE_LEADER && title != TITLE_DEPUTY_LEADER) {
			logger.error("同意联盟申请失败，君主:{}没有权限，职位:{}", junZhu.id, title);
			sendError(cmd, session, "非法操作:没有权限");
			return;
		}
		// 申请人验证，不再申请列表表示已经取消申请
		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, applyJzId);
		if (applyer == null || applyer == defaultAllianceApply || !applyer.isAllianceExist(alncBean.id)) {
			logger.error("同意联盟申请失败，目标申请不存在 jzId {} LM id {}", applyJzId, lianmengId);
			sendAgreeApplyResp(session, 3, applyJzId, lianmengId, null);
			return;
		}

		Date date = new Date();
		AlliancePlayer alncPlayer = AllianceMgr.inst.getAlliancePlayer(applyJzId);
		if (alncPlayer == null) {
			alncPlayer = new AlliancePlayer();
			initAlliancePlayerInfo(applyJzId, alncBean.id, alncPlayer, TITLE_MEMBER);
			HibernateUtil.insert(alncPlayer);
		} else if (alncPlayer.lianMengId > 0) {
			logger.error("同意联盟申请失败，申请者 jzId {} 已经加入其他联盟", applyJzId, alncPlayer.lianMengId);
			sendError(cmd, session, "该玩家已加入其他联盟。");
			return;
		} 
		alncPlayer.lianMengId = lianmengId;
		alncPlayer.getTitleTime = date;
		alncPlayer.joinTime = date;
		alncPlayer.title = TITLE_MEMBER;
		AlliancePlayerDao.inst.joinAlliance(alncPlayer, alncBean.id);
		HibernateUtil.save(alncPlayer);
		
//		// 加入联盟，重新开始计算贡金  add 20150915
//		GuoJiaMgr.inst.calculateGongJinJoinAlliance(applyJzId);

		// 批准成功操作，删除申请的其他联盟
		clearApplyAllianceCache(applyer);
		HibernateUtil.save(applyer);

		Redis.getInstance().sadd(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id,
				"" + alncPlayer.junzhuId);
		alncBean.members += 1;
		changeAllianceApplyState(alncBean);
		HibernateUtil.save(alncBean);

		MemberInfo.Builder memberInfo = MemberInfo.newBuilder();
		JunZhu memberJunzhu = HibernateUtil.find(JunZhu.class, applyJzId);
		Object[] jzInfo = {memberJunzhu.id,memberJunzhu.name,
				memberJunzhu.level,};
		fillMemberInfo(alncPlayer, memberInfo, jzInfo);//FIXME
		sendAgreeApplyResp(session, 0, applyJzId, lianmengId, memberInfo);
		EventMgr.addEvent(applyJzId, ED.Join_LM, new Object[] { applyJzId, alncBean.id, alncBean.name, alncPlayer.title});
		logger.info("同意联盟申请成功，{}批准{}:{}加入联盟{}:{}", junZhu.id, applyJzId, memberJunzhu.name,
				alncBean.id, alncBean.name);
		ActLog.log.Guild(applyJzId, memberJunzhu.name, "JOIN", alncBean.id, alncBean.name, alncBean.level, "");
		String eventStr = lianmengEventMap.get(2).str.replaceFirst("%d", memberJunzhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		// 2015-7-22 16:15 刷新联盟榜
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30017);
		if (mailConfig != null) {
			String content = mailConfig.content.replace("***", alncBean.name);
			EmailMgr.INSTANCE.sendMail(memberJunzhu.name, content, "",
					mailConfig.sender, mailConfig, "");
		}
		// 向被批准并在线的玩家发送通知
		RankingGongJinMgr.inst.firstSetGongJin(memberJunzhu.id, alncBean.id);
		IoSession su = AccountManager.sessionMap.get(applyJzId);
		int beforeGuoJia = memberJunzhu.guoJiaId;
		memberJunzhu.guoJiaId = alncBean.country;
		HibernateUtil.update(memberJunzhu);
		if (su != null) {
			JunZhuMgr.inst.sendMainInfo(su,memberJunzhu);
			su.write(PD.ALLIANCE_ALLOW_NOTIFY);
			su.setAttribute(SessionAttKey.LM_NAME, alncBean.name);
			su.setAttribute(SessionAttKey.LM_ZHIWU, alncPlayer.title);
			session.setAttribute(SessionAttKey.LM_INFO, alncPlayer);
			sendAllianceInfo(memberJunzhu, su, alncPlayer, alncBean);
			//刷新相关红点
			EventMgr.addEvent(memberJunzhu.id, ED.REFRESH_TIME_WORK, su);
		}
		refreshJunZhuPerRank(memberJunzhu, beforeGuoJia);
	}

	public void sendAgreeApplyResp(IoSession session, int result, long jzId,
			int lianmengId, MemberInfo.Builder memberInfo) {
		AgreeApplyResp.Builder response = AgreeApplyResp.newBuilder();
		response.setResult(result);
		response.setId(lianmengId);
		response.setJunzhuId(jzId);
		if (memberInfo != null) {
			response.setMemberInfo(memberInfo.build());
		}
		session.write(response.build());
	}

	/**
	 * 清空该玩家申请的所有联盟缓存，并从对应的联盟申请列表里移除 （暂使用时机：联盟批准入盟、立刻加入某联盟、自己创建联盟）
	 * 
	 * @param applyer
	 */
	public void clearApplyAllianceCache(AllianceApply applyer) {
		if (applyer == null) {
			return;
		}
		Set<Integer> applyAlncIds = applyer.getAllianceIdSet();
		for (Integer id : applyAlncIds) {
			Redis.getInstance().sremove(CACHE_APPLYERS_OF_ALLIANCE + id,
					"" + applyer.junzhuId);
		}
		// 必须放在最后
		applyer.removeAllAlliance();
	}

	public void updateNotice(int cmd, IoSession session, Builder builder) {
		UpdateNotice.Builder request = (qxmobile.protobuf.AllianceProtos.UpdateNotice.Builder) builder;
		String notice = request.getNotice();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("修改联盟公告失败-君主:{}未加入联盟", junZhu.id);
			return;
		}

		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			sendError(cmd, session, "没有该联盟");
			logger.error("修改联盟公告失败-找不到联盟:{}", mgrMember.lianMengId);
			return;
		}
		if (mgrMember.title != TITLE_LEADER && mgrMember.title != TITLE_DEPUTY_LEADER) {
			sendError(cmd, session, "不是盟主或副盟主，没有权限");
			logger.error("修改联盟公告失败-君主:{}没有权限，职位:{}", junZhu.id, mgrMember.title);
			return;
		}
		UpdateNoticeResp.Builder response = UpdateNoticeResp.newBuilder();
		notice = notice == null ? "" : notice;
		if (notice.length() > 50) {
			logger.error("修改联盟公告失败-公告长度太长 notice长度:{}", notice.length());
			response.setCode(1);
			response.setNotice(notice);
			session.write(response.build());
			return;
		}
		notice = ChatMgr.inst.replaceIllegal(notice);
		alncBean.notice = notice;
		HibernateUtil.save(alncBean);
		logger.info("修改联盟公告成功-君主:{}设置了联盟:{}的公告:{}", mgrMember.junzhuId, mgrMember.lianMengId, notice);
		response.setCode(0);
		response.setNotice(notice);
		session.write(response.build());
		
		LianmengEvent lmEvent = null;
		if(mgrMember.title == TITLE_LEADER) {
			lmEvent = lianmengEventMap.get(8);
		} else if(mgrMember.title == TITLE_DEPUTY_LEADER) {
			lmEvent = lianmengEventMap.get(16);
		}
		if(lmEvent != null) {
			String eventStr = lmEvent.str == null ? "" : lmEvent.str;
			eventStr = eventStr.replaceFirst("%d", junZhu.name);
			addAllianceEvent(alncBean.id, eventStr);
		}
	}

	public void dismissAlliance(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("解散联盟失败，君主:{}未加入联盟", junZhu.id);
			sendDismissAllianceResp(session, 0, 2);
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mgrMember)) {
			logger.error("解散联盟失败，君主:{}没有权限，职位:{}", junZhu.id, mgrMember.title);
			sendDismissAllianceResp(session, alncBean.id, 3);
			return;
		}
		int lianmengId = mgrMember.lianMengId;
		if(isAllianceInFight(lianmengId)) {
			logger.error("解散联盟失败，有盟员正在参加联盟战");
			sendDismissAllianceResp(session, alncBean.id, 1);
			return;
		}
		Set<AlliancePlayer> memberList = getAllianceMembers(lianmengId);
		logger.info("解散联盟成功，[{}:{}]解散了联盟:{},id:{},时间:{}", junZhu.id, junZhu.name,
				alncBean.name, alncBean.id, new Date());
		ActLog.log.GuildBreak(junZhu.id, junZhu.name, alncBean.id, alncBean.name, alncBean.level, alncBean.exp, alncBean.build);
		for (AlliancePlayer member : memberList) {
			resetAlliancePlayer(alncBean, member);
			HibernateUtil.save(member);
			logger.info("解散联盟成功，联盟{}解散，删除成员 {}", alncBean.id, member.junzhuId);
			ActLog.log.Guild(member.junzhuId, "", "OUT", alncBean.id, alncBean.name, alncBean.level, "解散");
			String jzCacheKey = HYMgr.CACHE_HYSTORE_APPLY + member.lianMengId + "_" + member.junzhuId;
			Redis.getInstance().del(jzCacheKey);
			JunZhu jz = HibernateUtil.find(JunZhu.class, member.junzhuId);
			int beforeGuojia = jz.guoJiaId;
			jz.guoJiaId = 0;
			HibernateUtil.update(jz);
			IoSession su = SessionManager.inst.findByJunZhuId(member.junzhuId);
			if (su != null) {
				su.write(PD.ALLIANCE_DISMISS_NOTIFY);
				su.removeAttribute(SessionAttKey.LM_NAME);
				su.removeAttribute(SessionAttKey.LM_ZHIWU);
				su.setAttribute(SessionAttKey.LM_INFO, member);
				JunZhuMgr.inst.sendMainInfo(su,jz);
			}
			// 触发事件，房屋管理用到。增加传送联盟等级
			EventMgr.addEvent(member.junzhuId, ED.Leave_LM, new Object[] { member.junzhuId,
					alncBean.id, "***", member.title });

			// 联盟解散，贡金被设置为-1
			RankingGongJinMgr.inst.setGongJinTo0(member.junzhuId, -1);
			refreshJunZhuPerRank(jz, beforeGuojia);
		}
		AlliancePlayerDao.inst.dismissAlliance(lianmengId);
		HibernateUtil.delete(alncBean);
		Redis.getInstance().del(CACHE_MEMBERS_OF_ALLIANCE + lianmengId);
		Redis.getInstance().del(CACHE_APPLYERS_OF_ALLIANCE + lianmengId);
		
		EventMgr.addEvent(alncBean.id, ED.DISMISS_ALLIANCE, new int[]{alncBean.id, alncBean.country});
		session.write(PD.DISMISS_ALLIANCE_OK);
		JunZhuMgr.inst.sendMainInfo(session);

		//TODO 发给盟主的邮件
		// 发送联盟解散邮件通知(除盟主)
		for (AlliancePlayer member : memberList) {
			Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30005);
			if(member.junzhuId == junZhu.id) {
				continue;
			}
			JunZhu memberJunzhu = HibernateUtil.find(JunZhu.class, member.junzhuId);
			EmailMgr.INSTANCE.sendMail(memberJunzhu.name, mailConfig.content,
					"", mailConfig.sender, mailConfig, "");
		}
	}

	public void sendDismissAllianceResp(IoSession session, int lianMengId, int result) {
		DismissAllianceResp.Builder response = DismissAllianceResp.newBuilder();
		response.setResult(result);
		response.setId(lianMengId);
		session.write(response.build());
	}

	public void dismissAllianceProcess(Event param) {
		int[] ids = (int[])param.param;
		int lianMengId = ids[0];
		int contryid = ids[1];
		try{
			// 联盟榜中删除数据
			RankingMgr.inst.remLianmeng(lianMengId, contryid);
		}catch(Exception e){
			e.printStackTrace();
		}
		// 获取所有申请该联盟的玩家，移除该联盟
		List<AllianceApply> applyList = getApplyers(lianMengId);
		for (AllianceApply applyer : applyList) {
			applyer.removeAlliance(lianMengId);
			HibernateUtil.save(applyer);
		}
		HYMgr.inst.delHYInfo(lianMengId);
	}

	public List<AllianceApply> getApplyers(int allianceId) {
		Set<String> applyerIds = Redis.getInstance().sget(
				CACHE_APPLYERS_OF_ALLIANCE + allianceId);
		if (applyerIds == null) {
			return Collections.EMPTY_LIST;
		}
		List<AllianceApply> applyList = new ArrayList<AllianceApply>();
		for (String id : applyerIds) {
			AllianceApply applyer = HibernateUtil.find(AllianceApply.class, Long.parseLong(id));
			if (applyer != null && applyer != defaultAllianceApply) {
				applyList.add(applyer);
			}
		}
		return applyList;
	}

	public void openApply(int cmd, IoSession session, Builder builder) {
		OpenApply.Builder request = (qxmobile.protobuf.AllianceProtos.OpenApply.Builder) builder;
		int levelMin = request.getLevelMin();
		int junXianMin = request.getJunXianMin();
		int isExamine = request.getIsExamine();
		String attach = request.getAttach();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		OpenApplyResp.Builder response = OpenApplyResp.newBuilder();
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("打开联盟招募失败-君主:{}还未加入联盟", junZhu.id);
			response.setCode(4);
			session.write(response.build());
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if(alncBean == null){
			return;
		}

		int memberMax = getAllianceMemberMax(alncBean.id);
		if (alncBean.members >= memberMax) {
			logger.error("打开联盟招募失败-当前联盟人数已满，等级:{}，人数:{}", alncBean.level, alncBean.members);
			response.setCode(1);
			session.write(response.build());
			return;
		}
		if (attach != null && attach.length() > 70) {
			logger.error("打开联盟招募失败-招募公告太长，长度:{}", attach.length());
			response.setCode(2);
			session.write(response.build());
			return;
		} 
		if (attach == null) {
			attach = "";
		} else {
			attach = ChatMgr.inst.replaceIllegal(attach);
		}
		if((mgrMember.title != TITLE_LEADER && mgrMember.title != TITLE_DEPUTY_LEADER)&& 
				(alncBean.minApplyLevel != levelMin || alncBean.minApplyJunxian != junXianMin ||
				alncBean.isShenPi != isExamine|| !alncBean.attach.equals(attach))) {
			logger.error("打开联盟招募失败，开启招募的成员:{}不是盟主，却修改了招募信息", junZhu.id);
			response.setCode(4);
			session.write(response.build());
			return;
		}

		alncBean.minApplyLevel = Math.max(CanShu.JION_ALLIANCE_LV_MINI, levelMin);
		alncBean.minApplyJunxian = junXianMin;
		alncBean.isShenPi = isExamine;
		alncBean.isAllow = OPEN_APPLY;
		alncBean.attach = attach;
		HibernateUtil.save(alncBean);
		response.setCode(0);
		response.setAttach(attach);
		session.write(response.build());
		logger.info("打开联盟招募成功，联盟:{}", alncBean.id);
	}

	public void closeApply(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null) {
			logger.error("关闭联盟招募失败-君主:{}还未加入联盟", junZhu.id);
			return;
		}
		AllianceBean alncBean =AllianceBeanDao.inst.getAllianceBean( mgrMember.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mgrMember)) {
			logger.error("关闭联盟招募失败-君主:{}没有权限，职位:{}", junZhu.id, mgrMember.title);
			return;
		}
		alncBean.isAllow = CLOSE_APPLY;
		HibernateUtil.save(alncBean);
		session.write(PD.CLOSE_APPLY_OK);
		logger.info("关闭联盟招募成功，联盟:{}", alncBean.id);
	}

	public void transferAlliance(int cmd, IoSession session, Builder builder) {
		TransferAlliance.Builder request = (qxmobile.protobuf.AllianceProtos.TransferAlliance.Builder) builder;
		long fuMengzhuId = request.getJunzhuId();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		JunZhu targetJz = HibernateUtil.find(JunZhu.class, fuMengzhuId);
		if(targetJz == null) {
			logger.error("转让联盟失败，未发现君主:{}", fuMengzhuId);
			return;
		}

		AlliancePlayer mengZhu = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mengZhu == null) {
			logger.error("转让联盟失败-君主:{}还未加入联盟", junZhu.id);
			return;
		}

		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean( mengZhu.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mengZhu)) {
			logger.error("转让联盟失败-君主:{}没有权限1，职位:{}", junZhu.id, mengZhu.title);
			return;
		}

		if (mengZhu.title != TITLE_LEADER) {
			logger.error("转让联盟失败-君主:{}没有权限2，职位:{}", junZhu.id, mengZhu.title);
			//sendError(cmd, session, "非法操作");
			return;
		}
		if(isAllianceInFight(alncBean.id)) {
			logger.error("转让联盟失败，有盟员正在参加联盟战");
			sendTransferAllianceResp(session, mengZhu.lianMengId, mengZhu.junzhuId, 2);
			return;
		}
		AlliancePlayer fuMengZhu = AllianceMgr.inst.getAlliancePlayer(fuMengzhuId);
		if (fuMengZhu == null || fuMengZhu.lianMengId != mengZhu.lianMengId) {
			sendTransferAllianceResp(session, mengZhu.lianMengId, mengZhu.junzhuId, 1);
			logger.error("转让联盟失败-将要降职的君主id:{}已经退出联盟:{}", fuMengzhuId, alncBean.id);
			return;
		}
		if (fuMengZhu.title != TITLE_DEPUTY_LEADER) {
			logger.error("转让联盟失败-将要降职的君主id:{}职位:{}", fuMengzhuId, fuMengZhu.title);
			//sendError(cmd, session, "要转让的对象职位太低");
			return;
		}

		alncBean.creatorId = fuMengZhu.junzhuId;
		HibernateUtil.save(alncBean);

		Date date = new Date();
		mengZhu.title = TITLE_DEPUTY_LEADER;
		mengZhu.getTitleTime = date;
		HibernateUtil.save(mengZhu);

		fuMengZhu.title = TITLE_LEADER;
		fuMengZhu.getTitleTime = date;
		HibernateUtil.save(fuMengZhu);

		logger.info("转让联盟成功-盟主:{}将联盟:{}盟主职位转交给副盟主id:{},时间:{}", junZhu.name,
				alncBean.name, fuMengZhu.junzhuId, date);
		ActLog.log.GuildTransfer(fuMengZhu.junzhuId, targetJz.name, alncBean.id, alncBean.name, junZhu.id, junZhu.name);
		sendTransferAllianceResp(session, mengZhu.lianMengId, fuMengzhuId, 0);
		String eventStr = lianmengEventMap.get(5).str.replaceFirst("%d", junZhu.name)
													.replaceFirst("%d", targetJz.name);
		addAllianceEvent(alncBean.id, eventStr);
//		List<AlliancePlayer> members = getAllianceMembers(alncBean.id);
		List<Object[]> aList = LveDuoMgr.inst.getAllAllianceMberName(alncBean.id);
		// 发给除了盟主和要转给的副盟主的其它盟员
		/*for (AlliancePlayer member : members) {
			if (member.junzhuId != mengZhu.junzhuId && member.junzhuId != fuMengzhuId) {
				Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30016);
				JunZhu memberJunzhu = HibernateUtil.find(JunZhu.class, member.junzhuId);
				String content = mailConfig.content
						.replace("***", junZhu.name)
						.replace("XXX", targetJz.name);
				EmailMgr.INSTANCE.sendMail(memberJunzhu.name, content, 
						"", mailConfig.sender, mailConfig, "");
			}
		}*/
		for(Object[] a:aList){
			String mName = (String) a[0];
			long jzId = ((BigInteger)a[1]).longValue();
			if (jzId != mengZhu.junzhuId && jzId != fuMengzhuId) {
				Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30016);
				String content = mailConfig.content
						.replace("***", junZhu.name)
						.replace("XXX", targetJz.name);
				EmailMgr.INSTANCE.sendMail(mName, content, 
						"", mailConfig.sender, mailConfig, "");
			}
		}
		
		
		
		
		// 发给原盟主
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30014);
		String content = mailConfig.content
				.replace("***", targetJz.name)
				.replace("***", targetJz.name);
		EmailMgr.INSTANCE.sendMail(junZhu.name, content, "", mailConfig.sender,
				mailConfig, "");
		JunZhuMgr.inst.sendMainInfo(session);
		session.setAttribute(SessionAttKey.LM_INFO, mengZhu);
		sendAllianceInfo(junZhu, session, mengZhu, alncBean);
		// 发给要转给的副盟主
		IoSession fuMengZhuSession = SessionManager.inst.getIoSession(targetJz.id);
		if(fuMengZhuSession != null){
			JunZhuMgr.inst.sendMainInfo(fuMengZhuSession);
			fuMengZhuSession.setAttribute(SessionAttKey.LM_INFO, fuMengZhu);
			sendAllianceInfo(targetJz, fuMengZhuSession, fuMengZhu, alncBean);
		}
		mailConfig = EmailMgr.INSTANCE.getMailConfig(30015);
		content = mailConfig.content.replace("***", junZhu.name);
		EmailMgr.INSTANCE.sendMail(targetJz.name, content, "",
				mailConfig.sender, mailConfig, "");
		EventMgr.addEvent(targetJz.id,ED.LM_TITLE_CHANGE, new Object[] {targetJz.id, alncBean.id, alncBean.name, fuMengZhu.title});
		EventMgr.addEvent(junZhu.id,ED.LM_TITLE_CHANGE, new Object[] {junZhu.id, alncBean.id, alncBean.name, mengZhu.title});
	}
	
	public void sendTransferAllianceResp(IoSession session, int lianMengId, 
			long mengZhuId, int result) {
		TransferAllianceResp.Builder response = TransferAllianceResp.newBuilder();
		response.setResult(result);
		response.setId(lianMengId);
		response.setJunzhuId(mengZhuId);
		session.write(response.build());
	}
	
	public boolean isAllianceInFight(int lianMengId) {
		Set<AlliancePlayer> memberList = getAllianceMembers(lianMengId);
		for (AlliancePlayer member : memberList) {
			IoSession su = SessionManager.inst.findByJunZhuId(member.junzhuId);
			if (su != null ) {
				Object scene = su.getAttribute(SessionAttKey.Scene);
				if(scene instanceof FightScene) {
					return true;
				}
			}
		}
		return false;
	}
	
	public synchronized void immidiatelyJoin(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		immediatelyJoin.Builder request = (qxmobile.protobuf.AllianceProtos.immediatelyJoin.Builder) builder;
		int lianMengId = request.getLianMengId();

		agreeInviteAndImmiJoinAlliance(PD.IMMEDIATELY_JOIN_RESP, session, junZhu, lianMengId);
	}

	public synchronized boolean agreeInviteAndImmiJoinAlliance(int responseCmd, IoSession session, JunZhu junZhu, int lianMengId) {
		String logHead = "";
		if(responseCmd == PD.IMMEDIATELY_JOIN_RESP) {
			logHead = "立刻加入联盟";
		} else if(responseCmd == PD.S_ALLIANCE_INVITE_AGREE) {
			logHead = "同意联盟邀请";
		}
		
		int remainTime = getCanJoinRemainTime(junZhu.id, lianMengId);
		AllianceBean alncBean =AllianceBeanDao.inst.getAllianceBean( lianMengId);
		int guojia = 1;
		if (alncBean == null) {
			logger.error("{}失败，要加入的联盟:{}不存在!", logHead, lianMengId);
			sendImmediatelyJoinResp(session, 2, null, responseCmd,lianMengId,guojia, remainTime);
			if(responseCmd == PD.S_ALLIANCE_INVITE_AGREE) {
				AllianceInviteBean inviteBean = AllianceInviteBeanDao.inst.getInviteBean(junZhu.id, lianMengId);
				AllianceInviteBeanDao.inst.removeInviteBean(junZhu.id, inviteBean);
			}
			return false;
		}
		guojia = alncBean.country;
		
		if(remainTime <= 0) {
			Redis.getInstance().hdel(ALLIANCE_EXIT + junZhu.id, String.valueOf(lianMengId));
		} else {
			sendImmediatelyJoinResp(session, 9, null, responseCmd,lianMengId,guojia, remainTime);
			logger.error("{}失败，距上次离开该联盟冷却时间未到", logHead);
			return false;
		}
		/*
		if (alncBean.country != junZhu.guoJiaId) {
			sendImmediatelyJoinResp(session, 8, null, responseCmd,lianMengId,guojia);
			logger.error("{}失败，玩家与联盟不是同一个国家，玩家-{}:国家-{}, 联盟-{}:国家-{}", logHead,junZhu.id,
					junZhu.guoJiaId, alncBean.id, alncBean.country);
			return false;
		}
		*/
		if(responseCmd == PD.IMMEDIATELY_JOIN_RESP){
			if (alncBean.isAllow == CLOSE_APPLY) {
				logger.error("{}失败，要加入的联盟:{}未开启招募!", logHead, lianMengId);
				sendImmediatelyJoinResp(session, 4, null, responseCmd,lianMengId,guojia,remainTime);
				return false;
			}
			if (alncBean.isShenPi != AllianceConstants.NO_NEED_SHENPI) {
				logger.error("{}失败，联盟:{}需要审批!", logHead, lianMengId);
				sendImmediatelyJoinResp(session, 1, null, responseCmd,lianMengId,guojia,remainTime);
				return false;
			}
			if (junZhu.level < alncBean.minApplyLevel) {
				logger.error("{}失败，等级不满足要求，联盟:{}要求君主等级不小于:{}，君主等级:{}", logHead, lianMengId, alncBean.minApplyLevel, junZhu.level);
				sendImmediatelyJoinResp(session, 6, null, responseCmd,lianMengId,guojia,remainTime);
				return false;
			}
			int junXianLevel = PvpMgr.inst.getJunxianLevel(junZhu.id);
			if (junXianLevel < alncBean.minApplyJunxian) {
				logger.error("{}失败，等级不满足要求，联盟:{}要求军衔等级不小于:{}，君主等级:{}", logHead, lianMengId, alncBean.minApplyJunxian, junXianLevel);
				sendImmediatelyJoinResp(session, 7, null, responseCmd,lianMengId,guojia,remainTime);
				return false;
			}
			
		}
		int memberMax = getAllianceMemberMax(alncBean.id);
		if (alncBean.members >= memberMax) {
			logger.error("{}失败，联盟:{}人数已满，等级:{}", logHead, lianMengId, alncBean.level);
			sendImmediatelyJoinResp(session, 5, null, responseCmd,lianMengId,guojia,remainTime);
			return false;
		}

		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, junZhu.id);
		Date date = new Date();
		AlliancePlayer alncPlayer = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (alncPlayer == null) {
			alncPlayer = new AlliancePlayer();
			initAlliancePlayerInfo(junZhu.id, alncBean.id, alncPlayer, TITLE_MEMBER);
			HibernateUtil.insert(alncPlayer);
		} else if(alncPlayer.lianMengId > 0) {
			logger.error("{}失败，申请者 jzId:{},已经加入了联盟:{}", logHead,junZhu.id, alncPlayer.lianMengId);
			return false;
		}

		alncPlayer.lianMengId = alncBean.id;
		alncPlayer.joinTime = date;
		alncPlayer.title = TITLE_MEMBER;
		AlliancePlayerDao.inst.joinAlliance(alncPlayer, alncBean.id);
		HibernateUtil.save(alncPlayer);
		Redis.getInstance().sadd(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id,
				"" + alncPlayer.junzhuId);
		logger.info("{}成功，junzhu:{}，在时间:{},加入联盟:{}", logHead, junZhu.name, date, alncBean.name);
		session.setAttribute(SessionAttKey.LM_NAME, alncBean.name);
		session.setAttribute(SessionAttKey.LM_ZHIWU, alncPlayer.title);
		session.setAttribute(SessionAttKey.LM_INFO, alncPlayer);
		ActLog.log.Guild(junZhu.id, junZhu.name, "JOIN", alncBean.id, alncBean.name, alncBean.level, "");
		alncBean.members = alncBean.members + 1;
		changeAllianceApplyState(alncBean);
		HibernateUtil.save(alncBean);
		// 批准成功操作，删除申请的其他联盟
		if (applyer != null && applyer != defaultAllianceApply) {
			clearApplyAllianceCache(applyer);
			HibernateUtil.save(applyer);
		}
		int breforeGuoJia = junZhu.guoJiaId;
		junZhu.guoJiaId = alncBean.country;
		JunZhuMgr.inst.sendMainInfo(session, junZhu);
		// 触发事件，房屋管理用到。
		EventMgr.addEvent(junZhu.id,ED.Join_LM, new Object[] {junZhu.id, alncBean.id, alncBean.name, alncPlayer.title});
		//刷新相关红点
		EventMgr.addEvent(junZhu.id,ED.REFRESH_TIME_WORK, session);
		AllianceHaveResp.Builder alncInfo = AllianceHaveResp.newBuilder();
		fillAllianceResponse(alncBean, alncInfo, alncPlayer);
		sendImmediatelyJoinResp(session, 0, alncInfo, responseCmd,lianMengId,guojia,remainTime);
		String eventStr = lianmengEventMap.get(2).str.replaceFirst("%d", junZhu.name);
		addAllianceEvent(alncBean.id, eventStr);
//		// 加入联盟，重新开始计算贡金  add 20150915
//		GuoJiaMgr.inst.calculateGongJinJoinAlliance(junZhu.id);
		RankingGongJinMgr.inst.firstSetGongJin(junZhu.id, alncBean.id);
		refreshJunZhuPerRank(junZhu, breforeGuoJia);
		return true;
	}

	public void sendImmediatelyJoinResp(IoSession session, int result,
			AllianceHaveResp.Builder alncInfo, int cmd, int lianMengId,
			int guojia, int remainTime) {
		immediatelyJoinResp.Builder response = immediatelyJoinResp.newBuilder();
		response.setCode(result);
		if (alncInfo != null) {
			response.setAlncInfo(alncInfo.build());
		}
		response.setLmId(lianMengId);
		response.setGuojiaId(guojia);
		response.setRemainTime(remainTime);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = cmd;
		msg.builder = response;
		session.write(msg);
	}

	/**
	 * 联盟成员退出联盟，仅在（退出联盟、被开除联盟、联盟解散）情况使用
	 * 
	 * @param alliance
	 * @param member
	 */
	public void resetAlliancePlayer(AllianceBean alliance,AlliancePlayer member) {
		member.lianMengId = 0;
		if(member.title == TITLE_DEPUTY_LEADER) {
			alliance.deputyLeaderNum -= 1;
			HibernateUtil.save(alliance);
		}
		member.title = AllianceMgr.TITLE_MEMBER;
		member.isBaoming = AllianceConstants.BAOMING_FALSE;
		member.isVoted = AllianceConstants.VOTED_FALSE;
		member.voteNum = 0;
		member.voteJunzhuId = 0;
		member.curMonthGongXian = 0;
	}

	public void addAllianceExp(int exp, AllianceBean alliance) {
		synchronized (this) {
			LianMeng lianMengCfg = lianMengMap.get(alliance.level);
			if (lianMengCfg == null) {
				logger.error("联盟增加经验失败，lianmeng.xml找不到等级为:{}的配置信息", alliance.level);
				return;
			}
			int beforeLevel = alliance.level;
			int beforeExp = alliance.exp;
			int upgradeNeedExp = lianMengCfg.exp;
			alliance.exp += exp;
			boolean upgrade = false;
			while (alliance.exp >= upgradeNeedExp) {
				// 联盟满级后，只是增长经验
				if (alliance.level >= lianMengMap.size()) {
					break;
				}
				// 下一级的联盟配置
				lianMengCfg = lianMengMap.get(alliance.level + 1);
				if (lianMengCfg == null) {
					logger.error("联盟增加经验失败，lianmeng.xml找不到等级为:{}的配置信息", alliance.level);
					break;
				}
				alliance.level += 1;
				alliance.exp -= upgradeNeedExp;
				upgrade = true;
				upgradeNeedExp = lianMengCfg.exp;
			}
			HibernateUtil.save(alliance);
			if (upgrade) {
				String eventStr = lianmengEventMap.get(10).str.replaceFirst("%d", String.valueOf(alliance.level));
				addAllianceEvent(alliance.id, eventStr);
				Set<AlliancePlayer> memberList = getAllianceMembers(alliance.id);
				for (AlliancePlayer member : memberList) {
					IoSession su = AccountManager.sessionMap.get(member.junzhuId);
					if (su != null) {
						su.write(PD.ALLIANCE_LEVEL_UP_NOTIFY);
					}
				}
				// 联盟等级排行提升
				RankingMgr.inst.resetLianMengLevelRedis(alliance.id, alliance.level);
				EventMgr.addEvent(alliance.id,ED.LIANMENG_RANK_REFRESH, new Integer(alliance.id));
				EventMgr.addEvent(alliance.id,ED.LIANMENG_UPGRADE_LEVEL, new Integer(alliance.id));
			}
			
			logger.info("联盟增加经验成功，增加经验之前等级:{}-经验:{},联盟:{}增加了经验:{},当前等级:{}-经验:{}",
					beforeLevel, beforeExp, alliance.id,exp, alliance.level, alliance.exp);
		}
	}
	
	public void upgradeLevel(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("升级联盟等级操作失败，未发现君主，cmd:{}", cmd);
			return;
		}
		int remainTime = -1;
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("升级联盟等级操作失败，君主:{}未加入联盟", junZhu.id);
			sendUpgradeLevelResp(session, 4, remainTime);
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			logger.error("升级联盟等级操作失败，找不到联盟:{}", mgrMember.lianMengId);
			sendUpgradeLevelResp(session, 4, remainTime);
			return;
		}
		if(mgrMember.title != TITLE_LEADER) {
			logger.error("升级联盟等级操作失败，君主:{}不是联盟:{}的盟主不能升级", junZhu.id, alncBean.id);
			sendUpgradeLevelResp(session, 2, remainTime);
			return;
		}
		LianMeng lianMengCfg = lianMengMap.get(alncBean.level);
		if (lianMengCfg == null) {
			logger.error("升级联盟等级操作失败，lianmeng.xml找不到等级为:{}的配置信息", alncBean.level);
			return;
		}
		if(alncBean.level == lianMengMap.size()) {
			logger.error("升级联盟等级操作失败，联盟:{}已经满级:{}", alncBean.id, alncBean.level);
			sendUpgradeLevelResp(session, 3, remainTime);
			return;
		}
		if(alncBean.build < lianMengCfg.cost) {
			logger.error("升级联盟等级操作失败，联盟建设值不足:{}", lianMengCfg.cost);
			sendUpgradeLevelResp(session, 1, remainTime);
			return;
		}
		Date date = new Date();
		Date upgrateDate = new Date(date.getTime() + 60L * 60 * 1000 * lianMengCfg.lvlupTime);
		alncBean.upgradeTime = upgrateDate;
		alncBean.build -= lianMengCfg.cost;
		HibernateUtil.save(alncBean);
		logger.error("升级联盟等级操作成功，联盟:{}会在时间:{}等级从{}升级为:{}", alncBean.id, upgrateDate, alncBean.level-1, alncBean.level);
		remainTime = (int) (60L * 60 * lianMengCfg.lvlupTime);
		sendUpgradeLevelResp(session, 0, remainTime);
		AllianceMgr.inst.processHaveAlliance(junZhu, PD.ALLIANCE_INFO_REQ, session, mgrMember);
	}

	public void sendUpgradeLevelResp(IoSession session, int result, int remainTime) {
		UpgradeLevelResp.Builder response = UpgradeLevelResp.newBuilder();
		response.setResult(result);
		response.setRemainTime(remainTime);
		session.write(response.build());
	}

	/*
	 * changeValue可以小于0
	 */
	public void changeAlianceBuild(AllianceBean alliance, int changeValue) {
		if(alliance == null){
			logger.error("联盟建设值修改失败，传参联盟为null"); 
			return;
		}
		synchronized (buildLock) {
			int beforeBuild = alliance.build;
			alliance.build += changeValue;
			if (alliance.build <= 0) {
				alliance.build = 0;
			}
			HibernateUtil.save(alliance);
			logger.info("联盟建设值修改成功，联盟:{},增加前:{},增加了:{},当前:{}", 
					alliance.id, beforeBuild, changeValue, alliance.build);
		}
	}
	
	/**
	 * 修改联盟的虎符数量
	 * @param alliance			联盟
	 * @param changeValue		更改值，要是扣除虎符数量传过来的值应为负数
	 */
	public void changeAlianceHufu(AllianceBean alliance, int changeValue) {
		if(alliance == null){
			logger.error("联盟虎符数量修改失败，传参联盟为null"); 
			return;
		}
		synchronized (hufuLock) {
			int beforeHufu = alliance.hufuNum;
			alliance.hufuNum += changeValue;
			if (alliance.hufuNum <= 0) {
				alliance.hufuNum = 0;
			}
			HibernateUtil.save(alliance);
			logger.info("联盟虎符数量修改成功，联盟:{},增加前:{},增加了:{},当前:{}", 
					alliance.id, beforeHufu, changeValue, alliance.hufuNum);
		}
	}

	/**
	 * 修改联盟的声望值
	 * @Title: changeAlianceReputation 
	 * @Description:
	 * @param alliance
	 * @param changeValue
	 */
	public int changeAlianceReputation(AllianceBean alliance, int changeValue) {
		if(alliance == null){
			return 0;
		}
		if(changeValue == 0){
			return alliance.reputation;
		}
		boolean change = false;
		synchronized (reputationLock) {
			int beforeValue = alliance.reputation;
			alliance.reputation += changeValue;
			if (alliance.reputation <= 0) {
				alliance.reputation = 0;
			}
			HibernateUtil.save(alliance);
			change = true;
			logger.info("联盟声望值增加成功，联盟:{},增加前:{},增加了:{},当前:{}", 
					alliance.id, beforeValue, changeValue, alliance.reputation);
		}
		if(change){
			// TODO 添加联盟声望榜刷新事件 @何金城
			EventMgr.addEvent(alliance.id,ED.LIANMENG_DAY_RANK_REFRESH, new Object[]{alliance,changeValue});
			EventMgr.addEvent(alliance.id,ED.LIANMENG_WEEK_RANK_REFRESH, new Object[]{alliance,changeValue});
		}
		return alliance.reputation;
	}
	
	public void donateHufu(int cmd, IoSession session, Builder builder) {
		DonateHuFu.Builder request = (qxmobile.protobuf.AllianceProtos.DonateHuFu.Builder) builder;
		int count = request.getCount();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("虎符捐献失败，君主:{}未加入联盟", junZhu.id);
			return;
		}
		AllianceBean alncBean =AllianceBeanDao.inst.getAllianceBean( mgrMember.lianMengId);
		if (alncBean == null) {
			logger.error("虎符捐献失败，找不到联盟:{}", mgrMember.lianMengId);
			sendError(cmd, session, "该联盟不存在");
			return;
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		int haveCount = BagMgr.inst.getItemCount(bag, AwardMgr.ITEM_HU_FU_ID);
		if (haveCount < count) {
			sendDonateHuFuResp(session, 1, 0, 0);
			logger.error("虎符捐献失败，君主:{}背包内的虎符有:{}不足:{}个", junZhu.id, haveCount, count);
			return;
		}
		BagMgr.inst.removeItem(session, bag, AwardMgr.ITEM_HU_FU_ID, count, "联盟虎符捐献",junZhu.level);
		//BagMgr.inst.sendBagInfo(session, bag);
		ItemTemp itemTempCfg = TempletService.getInstance().getItemTemp(AwardMgr.ITEM_HU_FU_ID);
		int addGongxian = count * itemTempCfg.effectId;
		logger.info("虎符捐献成功，玩家:{}捐献虎符:{}个，获得联盟贡献:{}，联盟:{}建设值增加:{}", 
				junZhu.id, count, addGongxian, alncBean.id, addGongxian);
		ActLog.log.GuildDonate(junZhu.id, junZhu.name, alncBean.id, alncBean.name, count, addGongxian);
		mgrMember.gongXian += addGongxian;
		alncBean.build += addGongxian;
		addAllianceExp(addGongxian, alncBean);
		changeGongXianRecord(mgrMember, addGongxian);
		HibernateUtil.save(mgrMember);
		HibernateUtil.save(alncBean);
		sendDonateHuFuResp(session, 0, addGongxian, addGongxian);
		String eventStr = lianmengEventMap.get(9).str.replaceFirst("%d", junZhu.name)
				.replaceFirst("%d", String.valueOf(count)).replaceFirst("%d", String.valueOf(addGongxian))
				.replaceFirst("%d", String.valueOf(addGongxian));
		addAllianceEvent(alncBean.id, eventStr);
	}
	

	/**
	 * @Title: getAlliance
	 * @Description: 获取帮会名称
	 * @return
	 * @return String
	 * @throws
	 */
	public String getAlliance(JunZhu junZhu) {
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (member == null || member.lianMengId <= 0) {
			return "";
		} else {
			AllianceBean alnc = AllianceBeanDao.inst.getAllianceBean(member.lianMengId);
			return alnc == null ? "" : alnc.name;
		}

	}
	
	public AlliancePlayer getAlliancePlayer(long junzhuId) {
		AlliancePlayer member = null;
		IoSession session = SessionManager.inst.getIoSession(junzhuId);
		if(session != null) {
			member = (AlliancePlayer) session.getAttribute(SessionAttKey.LM_INFO);
		} else {
			member = HibernateUtil.find(AlliancePlayer.class, junzhuId);
		}
		return member;
	}
	
	/**
	 * @Title: getAlliance
	 * @Description: 通过君主id获取帮会名称
	 * @return
	 * @return String
	 * @throws
	 */
	public String getAllianceByJzId(long jzId) {
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jzId);
		if (member == null || member.lianMengId <= 0) {
			return "";
		} else {
			AllianceBean alnc = AllianceBeanDao.inst.getAllianceBean(member.lianMengId);
			return alnc == null ? "" : alnc.name;
		}

	}
	
	/**
	 * 获取君主所在的联盟(信息)
	 * 
	 * @Title: getAllianceByJunZid
	 * @Description:
	 * @param jzId
	 * @return : null 表示君主没有联盟
	 */
	public AllianceBean getAllianceByJunZid(long jzId) {
		// 判断君主是否有联盟
		AlliancePlayer player = AllianceMgr.inst.getAlliancePlayer(jzId);
		if (player == null || player.lianMengId <= 0) {
//			logger.info("获取玩家联盟失败，玩家:{}没有联盟", jzId);
			return null;
		}
		AllianceBean guild = AllianceBeanDao.inst.getAllianceBean(player.lianMengId);
		if (guild == null) {
			logger.error("获取玩家联盟失败，联盟{}不存在", player.lianMengId);
		}
		return guild;
	}
	
	public int getAllianceId(long junZhuId) {
		AllianceBean alliance  = getAllianceByJunZid(junZhuId);
		if(alliance == null) {
			return NONE_ALLIANCE_ID;
		}
		return alliance.id;
	}
	
	public void sendDonateHuFuResp(IoSession session, int result,
			int gongxian, int build) {
		DonateHuFuResp.Builder response = DonateHuFuResp.newBuilder();
		response.setResult(result);
		response.setGongxian(gongxian);
		response.setBuild(build);
		session.write(response.build());
	}
	
	/**
	 * 调用此方法修改月联盟贡献值，需要调用完之后，保存 alliancePlayer对象
	 * @param alliancePlayer
	 * @param addGongXian
	 */
	public void changeGongXianRecord(AlliancePlayer alliancePlayer, int addGongXian) {
		if(alliancePlayer == null) {
			return;
		} 
		Date date = new Date();
		if(alliancePlayer.lianMengId <= 0) {
			alliancePlayer.curMonthGongXian = 0;
			logger.info("个人联盟贡献值变更，玩家:{},时间:{}离开联盟，将本月贡献清零", alliancePlayer.junzhuId, date);
		} else {
			boolean sameMonth = DateUtils.isSameMonth(date, alliancePlayer.curMonthFirstTime);
			int beforeValue = alliancePlayer.curMonthGongXian;
			if(sameMonth) {
				alliancePlayer.curMonthGongXian = alliancePlayer.curMonthGongXian + addGongXian;
				logger.info("个人联盟贡献值变更，由于是在同一个月，玩家:{}之前:{}，增加了:{}，当前:{}",
						alliancePlayer.junzhuId, addGongXian, beforeValue, alliancePlayer.curMonthGongXian);
			} else {
				alliancePlayer.curMonthGongXian = addGongXian;
				alliancePlayer.curMonthFirstTime = date;
				logger.info("个人联盟贡献值变更，由于月份变更，玩家:{}之前:{}，增加了:{}，当前:{}",
						alliancePlayer.junzhuId, addGongXian, beforeValue, alliancePlayer.curMonthGongXian);
			}
		}
	}
	
	public void addAllianceEvent(int lianMengId, String eventStr) {
		if(eventStr == null || eventStr.equals("")) {
			return;
		}
		if(Redis.getInstance().llen(ALLIANCE_EVENT + lianMengId) > EVENT_MAX_NUM) {
			Redis.getInstance().rpop(ALLIANCE_EVENT + lianMengId);
		}
		Date date = new Date();
		eventStr = DateUtils.date2Text(date, "yyyy-MM-dd HH:mm") + "#" + eventStr;
		Redis.getInstance().lpush_(ALLIANCE_EVENT + lianMengId, eventStr);
		logger.info("添加联盟事件，联盟id:{},事件:{}", lianMengId, eventStr);
		/*// 1.3去掉了联盟动态的红点
		List<AlliancePlayer> memberList = getAllianceMembers(lianMengId);
		for (AlliancePlayer member : memberList) {
			IoSession su = AccountManager.sessionMap.get(member.junzhuId);
			if (su != null) {
				//联盟客栈动态
				FunctionID.pushCanShowRed(member.junzhuId, su, FunctionID.LianMengKeZhanDongTai);
			}
		}*/
	}
	
	@Override
	public void proc(Event event) {
		switch (event.id) {
			case ED.DISMISS_ALLIANCE:
				dismissAllianceProcess(event);
				break;
			case ED.Leave_LM:
				playerAllianceStateChangeNotify(event, "退出联盟");
				break;
			case ED.Join_LM:
				playerAllianceStateChangeNotify(event, "加入联盟");
				break;
			case ED.LM_TITLE_CHANGE:
				playerAllianceStateChangeNotify(event, "职位变更");
				break;
			case ED.REFRESH_TIME_WORK:
				pushRedPoint(event);
				break;
			case ED.LIANMENG_UPGRADE_LEVEL:
				processUpgradeEvent(event);
				break;
			case ED.LIAN_MENG_CHANGE_GUOJIA:
				processChangeCountryEvent(event);
				break;
			case ED.JUNZHU_CHANGE_NAME:
				Object[] objs = (Object[]) event.param;
				String oldName = (String) objs[0];
				Long junzhuId = (Long) objs[1];
				JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId);
				if(junzhu == null) {
					return;
				}
				AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(junzhuId);
				if(member == null || member.lianMengId <= 0) {
					return;
				}
				AllianceBean bean =AllianceBeanDao.inst.getAllianceBean(member.lianMengId);
				if (bean == null) {
					logger.error("找不到联盟id为:{}", junzhuId);
					return;
				}
				LianmengEvent lmEvent = lianmengEventMap.get(34);
				if(lmEvent != null) {
					String eventStr = lmEvent.str.replace("%a", oldName)
											      .replace("%b", junzhu.name);
					addAllianceEvent(bean.id, eventStr);
				}
				break;
		}
	}

	public void processChangeCountryEvent(Event event) {
		Object[] objs = (Object[]) event.param;
		Integer lmId = (Integer) objs[0];
		Long mengZhuId = (Long) objs[1];
		AllianceBean alliance =AllianceBeanDao.inst.getAllianceBean(lmId);
		String guoJiaNam = HeroService.getNameById(alliance.country+"");
		
		Mail send2MengZhu = EmailMgr.INSTANCE.getMailConfig(30023);
		if(send2MengZhu != null) {
			String content = send2MengZhu.content.replace("*", guoJiaNam);
			JunZhu junzhu = HibernateUtil.find(JunZhu.class, mengZhuId);
			boolean sendOK = EmailMgr.INSTANCE.sendMail(junzhu.name, content,
					"", send2MengZhu.sender, send2MengZhu ,"");
			logger.info("联盟转国成功邮件通知，发送给盟主玩家：{}, 结果:{}", junzhu.id, sendOK);
		}
		
		Mail mailCfg = EmailMgr.INSTANCE.getMailConfig(30021);
		if(mailCfg != null){
			String content = mailCfg.content.replace("*", guoJiaNam);
		/*	List<AlliancePlayer> memberList = AllianceMgr.inst.getAllianceMembers(lmId);
			for(AlliancePlayer pla: memberList){
				if(pla.junzhuId == mengZhuId) {
					continue;
				}
				JunZhu junzhu = HibernateUtil.find(JunZhu.class, pla.junzhuId);
				boolean sendOK = EmailMgr.INSTANCE.sendMail(junzhu.name, content,
							"", mailCfg.sender, mailCfg ,"");
				logger.info("联盟转国成功邮件通知，发送给玩家：{}, 结果:{}", junzhu.id, sendOK);
			}*/
		 List<Object[]> aList = LveDuoMgr.inst.getAllAllianceMberName(lmId);
		 for(Object[] a:aList){
			 String mName = (String) a[0];
			 long jzId = ((BigInteger)a[1]).longValue();
			 if(jzId == mengZhuId) {
					continue;
				}
				boolean sendOK = EmailMgr.INSTANCE.sendMail(mName, content,
							"", mailCfg.sender, mailCfg ,"");
				logger.info("联盟转国成功邮件通知，发送给玩家：{}, 结果:{}", jzId, sendOK);
		 }
		}
	}

	public void processUpgradeEvent(Event event) {
		Integer allianceId = (Integer) event.param;
		if(allianceId == null) {
			return;
		}
//		AllianceBean bean = HibernateUtil.find(AllianceBean.class, allianceId);
		AllianceBean bean = AllianceBeanDao.inst.getAllianceBean(allianceId);
		if (bean == null) {
			logger.error("找不到联盟id为:{}", allianceId);
			return;
		}
		int mailCfgId = 0;
		switch(bean.level) {
			case 2:		mailCfgId = 30022;	break;
			case 3:		mailCfgId = 30024;	break;
			case 4:		mailCfgId = 30025;	break;
			case 5:		mailCfgId = 30026;	break;
			case 6:		mailCfgId = 30027;	break;
			case 7:		mailCfgId = 30028;	break;
			case 8:		mailCfgId = 30029;	break;
			case 9:		mailCfgId = 30030;	break;
			case 10:	mailCfgId = 30031;	break;
		}
		LianMeng lmCfg = lianMengMap.get(bean.level);
		if(lmCfg == null) {
			logger.error("找不到联盟等级为:{}的配置", bean.level);
			return;
		}
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(mailCfgId);
		if (mailConfig != null) {
			String content = mailConfig.content.replace("**", lmCfg.renshuMax+"");
		/*	List<AlliancePlayer> members = getAllianceMembers(allianceId);
			for(AlliancePlayer player : members) {
				JunZhu junZhu = HibernateUtil.find(JunZhu.class, player.junzhuId);
				if(junZhu != null) {
					EmailMgr.INSTANCE.sendMail(junZhu.name, content, "",
							mailConfig.sender, mailConfig, "");
				}
			}*/
		List<Object[]> aList = LveDuoMgr.inst.getAllAllianceMberName(allianceId);
		for(Object[] a:aList){
			String mName = (String) a[0];
			EmailMgr.INSTANCE.sendMail(mName, content, "",
					mailConfig.sender, mailConfig, "");
		}

		}
		
		LianmengEvent lmEvent = lianmengEventMap.get(10);
		if(lmEvent != null) {
			String eventStr = lmEvent.str.replaceFirst("%d", String.valueOf(bean.level));
			addAllianceEvent(allianceId, eventStr);
		}
	}
	
	public void playerAllianceStateChangeNotify(Event event, String reason) {
		Object[] params = (Object[]) event.param;
		Long junzhuId = (Long) params[0];
		Integer allianceId = (Integer) params[1];
		String allianceName = (String) params[2];
		Integer title = (Integer) params[3];
		IoSession session = SessionManager.inst.getIoSession(junzhuId);
		if(session != null) {
			Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
			if(scene != null) {
				Player player = scene.getPlayerByJunZhuId(junzhuId);
				if(player != null) {
					player.allianceId = allianceId;
					player.lmName = allianceName;
					player.zhiWu = title;
					
					logger.info("君主--{},"+  reason +",联盟id:{},名字:{}",junzhuId, allianceId, allianceName);
					//2016年4月12日11:34:36 康建虎 需要新的协议来同步名字、联盟。
					qxmobile.protobuf.Scene.EnterScene.Builder info = scene.buildEnterInfo(player);
					ProtobufMsg msg = new ProtobufMsg(PD.S_HEAD_INFO, info);
					scene.broadCastEvent(msg, 0/*player.userId*/);
					
					if(scene.name.contains("YB")) {
						PlayerAllianceState.Builder response = PlayerAllianceState.newBuilder();
						response.setJunzhuId(junzhuId);
						response.setAllianceId(allianceId);
						response.setAllianceName(allianceName);
						response.setTitle(title);
						for(Player p : scene.players.values()){
							p.session.write(response.build());
						}
					}
				}
			}
		}
	}

	public void pushRedPoint(Event event) {
		IoSession session=(IoSession) event.param;
		if(session==null){
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz==null){
			return;
		}
		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionOpenMgr.TYPE_ALLIANCE, jz.id, jz.level);
		if(!isOpen){
//			logger.info("君主：{}--联盟的功能---未开启,不推送",jz.id);
			return;
		}
		AlliancePlayer alliancePlayer = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if(alliancePlayer == null) {
			return;
		}
		AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean(alliancePlayer.lianMengId);
		if(alliance == null) {
			return;
		}
		Set<String> applyList = Redis.getInstance().sget(CACHE_APPLYERS_OF_ALLIANCE + alliance.id);
		if(applyList.size() <= 0) {
			return;
		}
		if(alliancePlayer.title == TITLE_LEADER || alliancePlayer.title == TITLE_DEPUTY_LEADER) {
//			FunctionID.pushCanShangjiao(jz.id, session, FunctionID.alliance);
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.LianMengShenQing);
		}
	}

//2015年9月22日成员离开联盟广播
	public void broadCastAllianceInfos(Event param) {
		Object[] oa = (Object[]) param.param;
		Long jzId = (Long) oa[0];
		Integer lmId = (Integer) oa[1];
		logger.info("君主--{},加入或者离开联盟--id--{}",jzId,lmId);
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(lmId);
		if (alncBean == null) {
			logger.error("找不到联盟，id:{}", lmId);
			return;
		}
		Set<AlliancePlayer> memberList = getAllianceMembers(lmId);
		for (AlliancePlayer member : memberList) {
			if(member.junzhuId == lmId) {
				continue;
			}
			IoSession su = AccountManager.sessionMap.get(member.junzhuId);
			if(su!=null){
				requestAllianceInfo(0, su, null);
			}
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.DISMISS_ALLIANCE, this);		
		//2015年9月21日 离开联盟是重新广播联盟信息
		EventMgr.regist(ED.Leave_LM, this);
		//2015年9月22日 加入联盟是重新广播联盟信息
		EventMgr.regist(ED.Join_LM, this);
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
		EventMgr.regist(ED.LM_TITLE_CHANGE, this);
		EventMgr.regist(ED.LIANMENG_UPGRADE_LEVEL, this);
		EventMgr.regist(ED.LIAN_MENG_CHANGE_GUOJIA, this);
		EventMgr.regist(ED.JUNZHU_CHANGE_NAME, this);
	}

	public void eventListRequest(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null) {
			logger.error("联盟事件列表请求失败，君主:{}不是联盟成员", junZhu.id);
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			logger.error("联盟事件列表请求失败，联盟:{}不存在", mgrMember.lianMengId);
			return;
		}
		
		long count = Redis.getInstance().llen(ALLIANCE_EVENT + alncBean.id);
//		int pageTotal = (int) (count / EVENT_PAGE_SIZE);
//		if(count % EVENT_PAGE_SIZE != 0) {
//			pageTotal += 1;
//		}
//      分页查询，暂时不需要
//		EventListReq.Builder request = (qxmobile.protobuf.AllianceProtos.EventListReq.Builder) builder;
//		int page = request.getPage();
//		page = page <= 0 ? 1 : page;
//		page = page > pageTotal ? pageTotal : page;
//		int end = page * EVENT_PAGE_SIZE - 1; 
//		int start = end - EVENT_PAGE_SIZE < 0 ? 0 : end - EVENT_PAGE_SIZE;
		List<String> eventList = Redis.getInstance().lrange4String(ALLIANCE_EVENT + alncBean.id, 0, -1);
		
		EventListResp.Builder response = EventListResp.newBuilder();
		response.setPage(0);
		for(String s : eventList) {
			response.addMsg(s);
		}
		session.write(response.build());
	}
	
	public static String getAllianceName(int allianceId) {
		if(allianceId <= 0) {
			return "";
		}
		AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean( allianceId);
		if(alliance == null) {
			return "";
		}
		return alliance.name;
	}
	
	/**
	 * 职位权限验证
	 * @param Title              玩家的职位
	 * @param verfiyTitle        需要验证的职位
	 * @return true 有权限  false 无权限
	 */
	public static boolean vilidateTitle(int title, int...verfiyTitle) {
		for(int t : verfiyTitle){
			if(title == t){
				return true;
			}
		}
		return false;
	}

	public void requestAllianceTargetInfo(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AllianceLevelAward levelAward = getAllianceLevelAward(junZhu.id);
		AllianceTargetResp.Builder response = AllianceTargetResp.newBuilder();
		response.setLevel(levelAward.curLevel);
		response.setGetAward(false);
		session.write(response.build());
	}

	public AllianceLevelAward getAllianceLevelAward(long junzhuId) {
		AllianceLevelAward levelAward = HibernateUtil.find(AllianceLevelAward.class, junzhuId);
		if(levelAward == null) {
			levelAward = new AllianceLevelAward();
			levelAward.junZhuId = junzhuId;
			levelAward.getAwardLevel = "";
			levelAward.curLevel = LEVEL_AWARD_DEFAULT;	//默认从等级2开始
			HibernateUtil.insert(levelAward);
		}
		return levelAward;
	}

	public void getAllianceLevelAward(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		// 判断君主是否有联盟
		AlliancePlayer player = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (player == null || player.lianMengId <= 0) {
			logger.info("领取联盟等级目标奖励失败，玩家:{}没有联盟", junZhu.id);
			return;
		}
		AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean(player.lianMengId);
		if (alliance == null) {
			logger.error("领取联盟等级目标奖励失败，联盟{}不存在", player.lianMengId);
			return;
		}
		
		GetAllianceLevelAward.Builder request = (qxmobile.protobuf.AllianceProtos.GetAllianceLevelAward.Builder) builder;
		int level = request.getLevel();
		if(level > alliance.level){
			logger.error("领取联盟等级目标奖励失败, 领取等级大于联盟等级");
			return;
		}
		
		AllianceLevelAward levelAward = getAllianceLevelAward(junZhu.id);
		if(levelAward.curLevel != level) {
			logger.error("领取联盟等级目标奖励失败, 领取等级与:{}当前可以领取等级:{}不符", level, levelAward.curLevel);
			return;
		}
		GetAllianceLevelAwardResp.Builder response = GetAllianceLevelAwardResp.newBuilder();	
		// 先判断是否已经领过
		if(levelAward.getAwardLevel.contains("" + level)) {
			logger.error("领取联盟等级目标奖励失败,君主:{}领取过联盟等级:{}的奖励", junZhu.id, level);
			response.setResult(1);
			response.setNextLevel(level);
			session.write(response.build());
			return;
		}
		
		// 再判断所在联盟等级是否达到
		if(alliance.level < level) {
			logger.error("领取联盟等级目标奖励失败,联盟:{}等级:{}未达到领取的等级:{}", junZhu.id, alliance.level, level);
			response.setResult(2);
			response.setNextLevel(level);
			session.write(response.build());
			return;
		}
		// 发放奖励
		LianMeng lianMengCfg = lianMengMap.get(level);
		if(lianMengCfg == null) {
			logger.error("领取联盟等级目标奖励失败，找不到联盟等级为:{}的配置", level);
			return;
		}
		if(!lianMengCfg.targetAward.equals("")) {
			List<AwardTemp> list = AwardMgr.inst.parseAwardConf(lianMengCfg.targetAward, "#", ":");
			for(AwardTemp a : list) {
				AwardMgr.inst.giveReward(session, a, junZhu);
			}
		}
		if(levelAward.getAwardLevel.equals("")) {
			levelAward.getAwardLevel = level + "";
		} else {
			levelAward.getAwardLevel = levelAward.getAwardLevel + "_" + level;
		}
		int nextLevel = level + 1;
		LianMeng nextLianMengCfg = lianMengMap.get(nextLevel);
		if(nextLianMengCfg == null) {
			nextLevel = -1;
		}
		levelAward.curLevel = nextLevel;
		HibernateUtil.save(levelAward);
		logger.info("领取联盟等级目标奖励成功，君主:{}领取了联盟:{}目标等级:{}的奖励", junZhu.id, alliance.id, level);
		response.setResult(0);
		response.setNextLevel(nextLevel);
		session.write(response.build());
	}
    /**
     * 邀请进入联盟(被邀请方 需开启联盟 且无联盟)
     * @param cmd
     * @param session
     * @param builder
     */
	
	public void inviteJoinAlliance(int cmd, IoSession session, Builder builder) {
		AllianceInvite.Builder request = (qxmobile.protobuf.AllianceProtos.AllianceInvite.Builder) builder;
		long invitedJzId = request.getJunzhuId();
		
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		
		JunZhu invitedJz = HibernateUtil.find(JunZhu.class, invitedJzId);
		if (invitedJz == null) {
			logger.error("未发现君主，cmd:{}, jzId:{}", cmd, invitedJzId);
			return;
		}

		// 判断君主是否有联盟
		AlliancePlayer player = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (player == null || player.lianMengId <= 0) {
			logger.info("邀请加入联盟失败，玩家:{}没有联盟", junZhu.id);
			return;
		}
		AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean(player.lianMengId);
		if (alliance == null) {
			logger.error("邀请加入联盟失败，联盟{}不存在", player.lianMengId);
			return;
		}
		if(player.title != TITLE_LEADER && player.title != TITLE_DEPUTY_LEADER) {
			logger.error("邀请加入联盟失败，君主:{},title:{}没有权限", junZhu.id, player.title);
			sendAllianceInviteResp(session, 5);
			return;
		}
		int maxMembers = getAllianceMemberMax(alliance.id);
		if(alliance.members >= maxMembers) {
			logger.error("邀请加入联盟失败，联盟{}人数已满", player.lianMengId);
			sendAllianceInviteResp(session, 2);
			return;
		}
		boolean isOpen = FunctionOpenMgr.inst.isFunctionOpen(FunctionOpenMgr.TYPE_ALLIANCE, invitedJzId, invitedJz.level);
		if(!isOpen) {
			logger.error("邀请加入联盟失败-被邀请的玩家联盟功能未开放");
			sendAllianceInviteResp(session, 3);
			return;
		}
		
		JunZhu invitedJZ = HibernateUtil.find(JunZhu.class, invitedJzId);
		if (invitedJZ == null) {
			sendAllianceInviteResp(session, 1);
			return;
		}
		AlliancePlayer invitedPlayer = HibernateUtil.find(AlliancePlayer.class, invitedJZ.id);
		if (invitedPlayer != null && invitedPlayer.lianMengId > 0) {
			if(invitedPlayer.lianMengId == alliance.id) {
				logger.info("邀请加入联盟失败，被邀请的玩家:{}已经加入了联盟:{}", invitedJZ.id, invitedPlayer.lianMengId);
				sendAllianceInviteResp(session, 6);
				return;
			} else {
				logger.info("邀请加入联盟失败，被邀请的玩家:{}已经加入了你的联盟:{}", invitedJZ.id, invitedPlayer.lianMengId);
				sendAllianceInviteResp(session, 4);
				return;
			}
		}
		
		Date date = new Date();
		AllianceInviteBean inviteBean = AllianceInviteBeanDao.inst.getInviteBean(invitedJzId, alliance.id);
		if(inviteBean != null) {
			inviteBean.date = date;
			HibernateUtil.save(inviteBean);
		} else {
			inviteBean = new AllianceInviteBean();
			inviteBean.id = TableIDCreator.getTableID(AllianceInviteBean.class, 1);
			inviteBean.allianceId = alliance.id;
			inviteBean.junzhuId = invitedJzId;
			inviteBean.date = date;
			AllianceInviteBeanDao.inst.addInviteBean(invitedJzId, inviteBean);
		}
		// 发送通知
		GreetMgr.inst.sendInvitePrompt(invitedJzId, junZhu.id, junZhu.name, alliance.id, alliance.name);
		logger.info("邀请加入联盟成功，玩家:{}邀请了玩家:{}加入联盟:{},时间:{}", junZhu.id, invitedJzId, alliance.id, date);
		sendAllianceInviteResp(session, 0);
	}
	
	public void sendAllianceInviteResp(IoSession session, int result) {
		AllianceInviteResp.Builder response = AllianceInviteResp.newBuilder();
		response.setResult(result);
		session.write(response.build());
	}

	public void seeInviteList(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		Date nowDate = new Date();
		InviteList.Builder response = InviteList.newBuilder();
		List<AllianceInviteBean> list = AllianceInviteBeanDao.inst.getList(junZhu.id);
		for(AllianceInviteBean inviteBean : list) {
			InviteAllianceInfo.Builder inviteInfo = InviteAllianceInfo.newBuilder();
			AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean(inviteBean.allianceId);
			if(alliance == null ||
					DateUtils.timeDistanceByHour(nowDate, inviteBean.date) >= 48) {
				AllianceInviteBeanDao.inst.removeInviteBean(junZhu.id, inviteBean);
				continue;
			}
			inviteInfo.setId(alliance.id);
			inviteInfo.setName(alliance.name);
			inviteInfo.setLevel(alliance.level);
			inviteInfo.setGuiJia(alliance.country);
			inviteInfo.setDate(DateUtils.date2Text(inviteBean.date, INVITE_TIME_FORMAT));
			response.addInviteInfo(inviteInfo);
		}
		session.write(response.build());
	}

	public void refuseInvite(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		
		RefuseInvite.Builder request = (qxmobile.protobuf.AllianceProtos.RefuseInvite.Builder) builder;
		int allianceId = request.getId();
		AllianceInviteBean inviteBean = AllianceInviteBeanDao.inst.getInviteBean(junZhu.id, allianceId);
		if(inviteBean != null) {
			 AllianceInviteBeanDao.inst.removeInviteBean(junZhu.id, inviteBean);
		}
		
		logger.info("拒绝联盟邀请成功，玩家:{}拒绝了联盟:{}的邀请", junZhu.id, allianceId);
		RefuseInviteResp.Builder response = RefuseInviteResp.newBuilder();
		response.setResult(0);
		response.setLianMengId(allianceId);
		session.write(response.build());
		PromptMsgMgr.inst.deleteMsgByEventIdAndLmId(junZhu.id, allianceId, SuBaoConstant.invite);
	}

	public void agreeInvite(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AgreeInvite.Builder request =  (qxmobile.protobuf.AllianceProtos.AgreeInvite.Builder) builder;
		int lianMengId = request.getId();

		agreeInvite(PD.S_ALLIANCE_INVITE_AGREE, session, junZhu, lianMengId);
	}

	public void agreeInvite(int responseCmd, IoSession session, JunZhu junZhu, int lianMengId) {
		AllianceInviteBean inviteBean = AllianceInviteBeanDao.inst.getInviteBean(junZhu.id, lianMengId);
		if(inviteBean == null) {
			return;
		}
		boolean succeed = agreeInviteAndImmiJoinAlliance(responseCmd, session, junZhu, lianMengId);
		if(succeed) {
			logger.info("同意联盟邀请成功，玩家:{}同意了联盟:{}的邀请", junZhu.id, lianMengId);
			AllianceInviteBeanDao.inst.removeInviteBean(junZhu.id, inviteBean);
		}
	}

	public void upgradeLevelSpeedUp(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("联盟升级加速操作失败，未发现君主，cmd:{}", cmd);
			return;
		}
		
		int remainTime = -1;
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("联盟升级加速操作失败，君主:{}未加入联盟", junZhu.id);
			sendUpgradeLevelSpeedUpResp(session, 4, remainTime);
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			logger.error("联盟升级加速操作失败，找不到联盟:{}", mgrMember.lianMengId);
			sendUpgradeLevelSpeedUpResp(session, 4, remainTime);
			return;
		}
		if(mgrMember.title != TITLE_LEADER) {
			logger.error("联盟升级加速操作失败，君主:{}不是联盟:{}的盟主不能升级加速", junZhu.id, alncBean.id);
			sendUpgradeLevelSpeedUpResp(session, 2, remainTime);
			return;
		}
		LianMeng lianMengCfg = lianMengMap.get(alncBean.level);
		if (lianMengCfg == null) {
			logger.error("联盟升级加速操作失败，lianmeng.xml找不到等级为:{}的配置信息", alncBean.level);
			return;
		}
		if(alncBean.upgradeTime == null) {
			logger.error("联盟升级加速操作失败，联盟:{}没有处于升级状态中", alncBean.id);
			sendUpgradeLevelSpeedUpResp(session, 3, remainTime);
			return;
		}
		if(alncBean.upgradeUsedTimes >= lianMengCfg.speedUpTimes){
			logger.error("联盟升级加速操作失败，联盟:{}升级加速次数用完", alncBean.id);
			sendUpgradeLevelSpeedUpResp(session, 5, remainTime);
			return;
		}
		int cost = PurchaseMgr.inst.getNeedYuanBao(29, alncBean.todayUpgradeSpeedTimes + 1);
		if(alncBean.build < cost) {
			sendUpgradeLevelSpeedUpResp(session, 1, remainTime);
			logger.error("联盟升级加速操作失败，联盟建设值不足:{}", lianMengCfg.cost);
			return;
		}
		Date nowDate = new Date();
		long reduceMilliTime = CanShu.LIANMENG_LVLUP_REDUCE * 60L * 60 * 1000;
		alncBean.upgradeTime.setTime(alncBean.upgradeTime.getTime() - reduceMilliTime);
		alncBean.build -= cost;
		alncBean.todayUpgradeSpeedTimes += 1;
		alncBean.lastTimesUpgradeSpeedTime = nowDate;
		alncBean.upgradeUsedTimes += 1;
		HibernateUtil.save(alncBean);
		remainTime = (int) (((alncBean.upgradeTime.getTime() - nowDate.getTime()) / 1000));
		sendUpgradeLevelSpeedUpResp(session, 0, remainTime);
		AllianceMgr.inst.processHaveAlliance(junZhu, PD.ALLIANCE_INFO_REQ, session, mgrMember);
		LianmengEvent lmEvent = lianmengEventMap.get(30);
		if(lmEvent != null) {
			String eventStr = lmEvent.str.replaceFirst("%a", junZhu.name);
			addAllianceEvent(alncBean.id, eventStr);
		}
		
	}

	public void sendUpgradeLevelSpeedUpResp(IoSession session, int result, int remainTime) {
		UpgradeLevelSpeedUpResp.Builder response = UpgradeLevelSpeedUpResp.newBuilder();
		response.setResult(result);
		response.setRemainTime(remainTime);
		session.write(response.build());
	}

	public void upgradeLevelInfoRequest(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("联盟升级信息请求失败，未发现君主，cmd:{}", cmd);
			return;
		}
		
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("联盟升级信息请求失败，君主:{}未加入联盟", junZhu.id);
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			logger.error("联盟升级信息请求失败，找不到联盟:{}", mgrMember.lianMengId);
			return;
		}
		if(mgrMember.title != TITLE_LEADER) {
			logger.error("联盟升级信息请求失败，君主:{}不是联盟:{}的盟主不能升级加速", junZhu.id, alncBean.id);
			return;
		}
		LianMeng lianMengCfg = lianMengMap.get(alncBean.level);
		if (lianMengCfg == null) {
			logger.error("联盟升级信息请求失败，lianmeng.xml找不到等级为:{}的配置信息", alncBean.level);
			return;
		}
		int cost = PurchaseMgr.inst.getNeedYuanBao(29, alncBean.upgradeUsedTimes + 1);
		UpgradeLevelInfoResp.Builder response = UpgradeLevelInfoResp.newBuilder();
		response.setMTime((int) (CanShu.LIANMENG_LVLUP_REDUCE * 60L * 60));
		response.setMBuild(cost);
		session.write(response.build());
	}

	public void changeCountry(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("联盟转国请求失败，未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("联盟转国请求失败，君主:{}未加入联盟", junZhu.id);
			sendChangeCountryResp(session, 3);
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			logger.error("联盟转国请求失败，找不到联盟:{}", mgrMember.lianMengId);
			sendChangeCountryResp(session, 3);
			return;
		}
		if(mgrMember.title != TITLE_LEADER) {
			logger.error("联盟转国请求失败，君主:{}不是联盟:{}的盟主不能转国操作", junZhu.id, alncBean.id);
			sendChangeCountryResp(session, 2);
			return;
		}
		if(alncBean.hufuNum < CanShu.CHANGE_COUNTRY_COST) {
			logger.error("联盟转国请求失败，联盟的虎符数不足", junZhu.id, alncBean.id);
			sendChangeCountryResp(session, 4);
			return;
		}
		// 记录玩家离开联盟的时间
		long curTime = System.currentTimeMillis();
		String lastChangeTime = Redis.getInstance().get(ALLIANCE_CHANGE_GUOJIA + alncBean.id);
		if(lastChangeTime != null) {
			long intreval = curTime - Long.parseLong(lastChangeTime);
			if(intreval < CanShu.CHANGE_COUNTRY_CD * 60L * 60 * 1000) {
				int remainTime = (int) ((CanShu.CHANGE_COUNTRY_CD * 60L * 60 * 1000 - intreval)/1000);
				sendChangeCountryResp(session, -remainTime);
				logger.error("联盟转国请求失败，现在处于cd时间，还需等待{}秒", remainTime);
				return;
			}
		}
		
		ChangeAllianceCountry.Builder request = (qxmobile.protobuf.AllianceProtos.ChangeAllianceCountry.Builder) builder;
		int country = request.getCountry();
		boolean guoJiaExist = GuoJiaMgr.inst.verifyGuoJiaExist((byte) country);
		if(!guoJiaExist) {
			sendChangeCountryResp(session, 1);
			logger.error("联盟转国请求失败，选择的国家id:{}", country);
			return;
		}
		int beforeCountry = alncBean.country;
		changeAlianceHufu(alncBean, -CanShu.CHANGE_COUNTRY_COST);
		alncBean.country = country;
		HibernateUtil.save(alncBean);
		logger.info("联盟转国请求成功，君主:{}将联盟国家从{} 转为{}", junZhu.id, beforeCountry, country);
		Redis.getInstance().set(ALLIANCE_CHANGE_GUOJIA + alncBean.id, curTime+"");
		sendChangeCountryResp(session, 0);

		LianmengEvent lmEvent = lianmengEventMap.get(31);
		if(lmEvent != null) {
			String guojiaName = HeroService.getNameById(alncBean.country+"");
			String eventStr = lmEvent.str.replaceFirst("%a", junZhu.name).replace("%b", guojiaName);
			addAllianceEvent(alncBean.id, eventStr);
		}
		processChangeCountry4Members(alncBean, beforeCountry);
		EventMgr.addEvent(alncBean.id,ED.LIAN_MENG_CHANGE_GUOJIA, new Object[]{alncBean.id, junZhu.id});
		EventMgr.addEvent(alncBean.id,ED.LIANMENG_RANK_REFRESH, new Object[]{alncBean.id, beforeCountry});
	}
	
	/**
	 * 联盟转国后，对盟员的信息处理
	 * @param alncBean
	 * @param beforeCountry
	 */
	public void processChangeCountry4Members(AllianceBean alncBean, int beforeCountry) {
		Set<AlliancePlayer> memberList = getAllianceMembers(alncBean.id);
		for (AlliancePlayer member : memberList) {
			IoSession su = SessionManager.inst.findByJunZhuId(member.junzhuId);
			if (su == null) {
				continue;
			}
			JunZhu memberJz = HibernateUtil.find(JunZhu.class, member.junzhuId);
			if(memberJz == null) {
				continue;
			}
			memberJz.guoJiaId = alncBean.country;
			HibernateUtil.save(memberJz);
			JunZhuMgr.inst.sendMainInfo(su);
			sendAllianceInfo(memberJz, su, member, alncBean);
			refreshJunZhuPerRank(memberJz, beforeCountry);
		}
	}
	
	/**
	 * 联盟转国后，刷新所有成员在排行榜的国家信息
	 * @param junZhu
	 * @param beforeCountry
	 */
	public void refreshJunZhuPerRank(JunZhu junZhu, int beforeCountry) {
		EventMgr.addEvent(junZhu.id,ED.BAIZHAN_RANK_REFRESH, new Object[]{junZhu, beforeCountry});
		EventMgr.addEvent(junZhu.id,ED.CHONGLOU_RANK_REFRESH, new Object[]{junZhu, beforeCountry});
		EventMgr.addEvent(junZhu.id,ED.GUOGUAN_RANK_REFRESH, new Object[]{junZhu, beforeCountry});
	}

	public void sendChangeCountryResp(IoSession session, int result) {
		ChangeAllianceCountryResp.Builder response = ChangeAllianceCountryResp.newBuilder();
		response.setResult(result);
		session.write(response.build());
	}
	
	public Map<Long, JunZhu> getAllAllianceMmbrJzInfo(Set<AlliancePlayer> memberList){
		String ids = memberList.stream().map(m -> String.valueOf(m.junzhuId)).collect(Collectors.joining(","));
		String where = " where id in ("+ ids +")";
		List<JunZhu> junzhuList = HibernateUtil.list(JunZhu.class, where);
		Map<Long, JunZhu> junzhuMap = new HashMap<Long,JunZhu>();
		for (JunZhu jz : junzhuList) {
			junzhuMap.put(jz.id,jz);
		}
		return junzhuMap;
	}
}
