package com.qx.alliance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceProtos;
import qxmobile.protobuf.AllianceProtos.AgreeApply;
import qxmobile.protobuf.AllianceProtos.AgreeApplyResp;
import qxmobile.protobuf.AllianceProtos.AllianceHaveResp;
import qxmobile.protobuf.AllianceProtos.AllianceNonResp;
import qxmobile.protobuf.AllianceProtos.ApplicantInfo;
import qxmobile.protobuf.AllianceProtos.ApplyAlliance;
import qxmobile.protobuf.AllianceProtos.ApplyAllianceResp;
import qxmobile.protobuf.AllianceProtos.CancelJoinAlliance;
import qxmobile.protobuf.AllianceProtos.CancelJoinAllianceResp;
import qxmobile.protobuf.AllianceProtos.CheckAllianceName;
import qxmobile.protobuf.AllianceProtos.CheckAllianceNameResp;
import qxmobile.protobuf.AllianceProtos.CreateAlliance;
import qxmobile.protobuf.AllianceProtos.CreateAllianceResp;
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
import qxmobile.protobuf.AllianceProtos.LookApplicantsResp;
import qxmobile.protobuf.AllianceProtos.LookMembersResp;
import qxmobile.protobuf.AllianceProtos.MemberInfo;
import qxmobile.protobuf.AllianceProtos.NonAllianceInfo;
import qxmobile.protobuf.AllianceProtos.OpenApply;
import qxmobile.protobuf.AllianceProtos.OpenApplyResp;
import qxmobile.protobuf.AllianceProtos.RefuseApply;
import qxmobile.protobuf.AllianceProtos.RefuseApplyResp;
import qxmobile.protobuf.AllianceProtos.TransferAlliance;
import qxmobile.protobuf.AllianceProtos.TransferAllianceResp;
import qxmobile.protobuf.AllianceProtos.UpTitle;
import qxmobile.protobuf.AllianceProtos.UpTitleResp;
import qxmobile.protobuf.AllianceProtos.UpdateNotice;
import qxmobile.protobuf.AllianceProtos.UpdateNoticeResp;
import qxmobile.protobuf.AllianceProtos.immediatelyJoin;
import qxmobile.protobuf.AllianceProtos.immediatelyJoinResp;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.LianMeng;
import com.manu.dynasty.template.LianmengEvent;
import com.manu.dynasty.template.LianmengIcon;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.account.AccountManager;
import com.qx.account.FunctionOpenMgr;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.guojia.GuoJiaMgr;
import com.qx.guojia.ResourceGongJin;
import com.qx.huangye.HYMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pvp.PVPConstant;
import com.qx.pvp.PvpBean;
import com.qx.pvp.PvpMgr;
import com.qx.ranking.RankingMgr;
import com.qx.timeworker.FunctionID;
import com.qx.util.TableIDCreator;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class AllianceMgr extends EventProc{
	public Logger logger = LoggerFactory.getLogger(AllianceMgr.class);
	
	public static AllianceMgr inst;
	public List<LianmengIcon> iconList;
	/** 联盟等级 配置信息<level, LianMeng> **/
	public Map<Integer, LianMeng> lianMengMap;
	public Map<Integer, LianmengEvent> lianmengEventMap;

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
	/** 招募等级设置最低下限 **/
	public static final int APPLY_LEVEL_MIN = 20;
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
	public static final String EXIT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	/** 联盟事件前缀 */
	public static final String ALLIANCE_EVENT = "alliance_event_";
	
	
	/** 联盟建设值锁 **/
	public static final Object buildLock = new Object();
	public static final Object reputationLock  = new Object();

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
			lianMengMap.put(lm.getLv(), lm);
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

		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (member == null || member.lianMengId <= 0) {
			processNonAlliance(cmd, session, junZhu.id, junZhu.guoJiaId);
		} else {// 有联盟
			processHaveAlliance(junZhu, cmd, session, member);
		}
	}

	protected void processHaveAlliance(JunZhu junZhu, int cmd, IoSession session,
			AlliancePlayer member) {
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class,
				member.lianMengId);
		if (alncBean == null) {
			logger.error("找不到联盟，id:{}", member.lianMengId);
			return;
		}
		AllianceHaveResp.Builder response = AllianceHaveResp.newBuilder();
		fillAllianceResponse(alncBean, response, member);
		session.write(response.build());
	}

	protected void processNonAlliance(int cmd, IoSession session, long junzhuId,
			int country) {
		List<AllianceBean> alncList = HibernateUtil.list(AllianceBean.class,
				" where isAllow=" + OPEN_APPLY);
		AllianceNonResp.Builder resp = AllianceNonResp.newBuilder();
		AllianceApply alncApply = HibernateUtil.find(AllianceApply.class,
				junzhuId);
		for (AllianceBean alncBean : alncList) {
			if (alncBean.creatorId % 1000 != GameServer.serverId)
				continue;// 过滤掉不是本服务器的联盟//正式服务器数据库分开就没有这个问题。
			if (country != alncBean.country) {// 过滤不是同一个国家的
				continue;
			}
			NonAllianceInfo.Builder alncInfo = NonAllianceInfo.newBuilder();
			fillNonAllianceInfo(alncInfo, alncBean, alncApply);
			resp.addAlincInfo(alncInfo);
		}
		resp.setNeedYuanbao(CanShu.LIANMENG_CREATE_COST);
		session.write(resp.build());
	}

	protected NonAllianceInfo.Builder fillNonAllianceInfo(
			NonAllianceInfo.Builder alncInfos, AllianceBean alncBean,
			AllianceApply alncApply) {
		alncInfos.setId((int) alncBean.id);
		alncInfos.setName(alncBean.name);
		alncInfos.setIcon(alncBean.icon);
		alncInfos.setLevel(alncBean.level);
		alncInfos.setReputation(alncBean.reputation);
		alncInfos.setMembers(alncBean.members);
		int memberMax = getAllianceMemberMax(alncBean.level);
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
		return alncInfos;
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
		if (BigSwitch.inst.accMgr.isBadName(alncName)) {
			response.setCode(2);
			logger.error("联盟名字中不能有敏感、非法词汇");
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
		
		boolean isOpen = FunctionOpenMgr.inst.isFunctionOpen(FunctionOpenMgr.TYPE_ALLIANCE, junZhu.id, junZhu.level);
		if(!isOpen) {
			logger.error("创建联盟失败-联盟功能未开放");
			return;
		}
		
		if (allianceName == null || allianceName.equals("")) {
			logger.error("创建联盟失败-联盟名不能为空");
			return;
		}
		
		allianceName = allianceName.trim();
		if (BigSwitch.inst.accMgr.isBadName(allianceName)) {
			logger.error("创建联盟失败-有非法字符 name:{}", allianceName);
			sendCreateAllianceResp(session, 3, "有奇怪的文字混进来了...再推敲一下吧！", null);
			return;
		}
		
		if(allianceName.length() > NAME_LENGTH_MAX) {
			logger.error("创建联盟失败-联盟名字太长 name:{}", allianceName);
			sendCreateAllianceResp(session, 4, "联盟名字太长", null);
			return;
		}
		
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (member != null && member.lianMengId > 0) {
			logger.error("创建联盟失败-玩家:{}已经有联盟id:{}！", junZhu.name, member.lianMengId);
			return;
		}
		
		boolean nameExist = isAllianceNameExist(allianceName);
		if (nameExist) {
			logger.error("创建联盟失败-联盟名:{}已被占用！", allianceName);
			sendCreateAllianceResp(session, 1, "您起的联盟名字已被占用...换个更好的吧！", null);
			return;
		}
		Redis.getInstance().sadd(ALLIANCE_NAMES, allianceName);

		LianmengIcon iconCfg = null;
		for (LianmengIcon li : iconList) {
			if (li.getIcon() == icon) {
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
		alncBean.country = junZhu.guoJiaId;
		alncBean.isAllow = OPEN_APPLY;
		alncBean.members = 1;
		alncBean.minApplyLevel = APPLY_LEVEL_MIN;
		alncBean.minApplyJunxian = 1;// 军衔保存
		alncBean.isShenPi = 1;		// 默认不需要审批
		alncBean.status = AllianceConstants.STATUS_NORMAL;
		HibernateUtil.insert(alncBean);

		// 插入联盟成员表
		if (member == null) {
			member = new AlliancePlayer();
			initAlliancePlayerInfo(junZhu.id, alncBean.id, member, TITLE_LEADER);
			initAllianceGongXianRecord(junZhu.id);
			HibernateUtil.insert(member);
		} else {
			member.lianMengId = alncBean.id;
			member.title = TITLE_LEADER;
			member.getTitleTime = date;
			member.joinTime = date;
			HibernateUtil.save(member);
		}
		
		YuanBaoMgr.inst.diff(junZhu, -CanShu.LIANMENG_CREATE_COST, 0,
				CanShu.LIANMENG_CREATE_COST, YBType.YB_CHUANGJIAN_LIANGMENG,
				"创建联盟");
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);

		AllianceHaveResp.Builder alncInfo = AllianceHaveResp.newBuilder();
		fillAllianceResponse(alncBean, alncInfo, member);
		// 移除申请过的联盟
		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, junZhu.id);
		if (applyer != null) {
			clearApplyAllianceCache(applyer);
			HibernateUtil.save(applyer);
		}
		logger.info("junzhu:{}在时间:{}创建了联盟id:{},花费元宝:{}", junZhu.name, date,
				alncBean.id, CanShu.LIANMENG_CREATE_COST);
		ActLog.log.Guild(junZhu.id, junZhu.name, ActLog.vopenid, "CREATE", alncBean.id, alncBean.name, alncBean.level, "");
		sendCreateAllianceResp(session, 0, "联盟创建成功", alncInfo);
		
		Redis.getInstance().sadd(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id, member.junzhuId + "");
		String eventStr = lianmengEventMap.get(1).str.replaceFirst("%d", junZhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30013);
		if (mailConfig != null) {
			EmailMgr.INSTANCE.sendMail(junZhu.name, mailConfig.content, "",
					mailConfig.sender, mailConfig, "");
		}
		EventMgr.addEvent(ED.Join_LM, new Object[] { junZhu.id, alncBean.id });
		RankingMgr.inst.resetLianMengLevelRedis(alncBean.id, 1);
		EventMgr.addEvent(ED.LIANMENG_RANK_REFRESH, new Integer(alncBean.id));
		EventMgr.addEvent(ED.LIANMENG_DAY_RANK_REFRESH, new Object[]{alncBean,0});
		EventMgr.addEvent(ED.LIANMENG_WEEK_RANK_REFRESH, new Object[]{alncBean,0});
	
		// 加入联盟，重新开始计算贡金  add 20150915
		GuoJiaMgr.inst.calculateGongJinJoinAlliance(junZhu.id);
	}

	protected boolean isAllianceNameExist(String allianceName) {
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

	protected void sendCreateAllianceResp(IoSession session, int result,
			String msg, AllianceHaveResp.Builder alncInfo) {
		CreateAllianceResp.Builder response = CreateAllianceResp.newBuilder();
		response.setCode(result);
		response.setMsg(msg);
		if (alncInfo != null) {
			response.setAllianceInfo(alncInfo.build());
		}
		session.write(response.build());
	}

	public int getAllianceMemberMax(int level) {
		LianMeng lmConfig = lianMengMap.get(level);
		if (lmConfig == null) {
			logger.error("未发现联盟等级为:{}的配置信息", level);
			return 1;
		}
		return lmConfig.getRenshuMax();
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
	}

	protected synchronized void initAllianceGongXianRecord(long junzhuId) {
		AllianceGongXianRecord gxRecord = HibernateUtil.find(AllianceGongXianRecord.class, junzhuId);
		if(gxRecord != null) {
			return;
		}
		gxRecord = new AllianceGongXianRecord();
		gxRecord.setJunZhuId(junzhuId);
		gxRecord.setCurMonthFirstTime(new Date());
		HibernateUtil.insert(gxRecord);
	}
	
	protected void fillAllianceResponse(AllianceBean alncBean,
			AllianceHaveResp.Builder alncInfo, AlliancePlayer alncPlayer) {
		int title = alncPlayer == null ? -1 : alncPlayer.title;
		int gongXian = alncPlayer == null ? 0 : alncPlayer.gongXian;
		LianMeng lmCfg = lianMengMap.get(alncBean.level);
		int curExp = alncBean.exp;
		int nextLevelExp = lmCfg.getExp();
		if (alncBean.level >= lianMengMap.size()) {// 表示已经满级
			nextLevelExp = lianMengMap.get(alncBean.level - 1).getExp();
			curExp = nextLevelExp;
		}
		alncInfo.setName(alncBean.name);
		alncInfo.setId((int) alncBean.id);
		alncInfo.setLevel(alncBean.level);
		alncInfo.setExp(curExp);
		alncInfo.setNeedExp(nextLevelExp);
		alncInfo.setBuild(alncBean.build);
		alncInfo.setMembers(alncBean.members);
		int memberMax = getAllianceMemberMax(alncBean.level);
		alncInfo.setMemberMax(memberMax);
		alncInfo.setContribution(gongXian);
		alncInfo.setNotice(alncBean.notice);
		alncInfo.setIcon(alncBean.icon);
		alncInfo.setIdentity(title);
		alncInfo.setCountry(alncBean.country);
		alncInfo.setShengWang(alncBean.reputation);
		JunZhu mengZhuJZ = HibernateUtil.find(JunZhu.class, alncBean.creatorId);
		alncInfo.setMengzhuName(mengZhuJZ.name);
		if(alncBean.members >= memberMax) {
			alncBean.isAllow = CLOSE_APPLY;
			HibernateUtil.save(alncBean);
			alncInfo.setIsAllow(CLOSE_APPLY);
		} else {
			alncInfo.setIsAllow(alncBean.isAllow);
		}
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
		ResourceGongJin gjBean = HibernateUtil.find(ResourceGongJin.class, alncPlayer.junzhuId);
		int gongJin = GuoJiaMgr.inst.getGongJin(gjBean, PvpMgr.getJunxianLevel(alncPlayer.junzhuId));
		if(GuoJiaMgr.inst.isCanShangjiao(alncPlayer.junzhuId)){
			GuoJiaMgr.inst.pushCanShangjiao(alncPlayer.junzhuId);
		}
		alncInfo.setGongJin(gongJin);
		
		List<MemberInfo> allMemberInfo = getAllMemberInfoList(alncBean.id);
		alncInfo.addAllMemberInfo(allMemberInfo);
	}

	public void findAlliance(int cmd, IoSession session, Builder builder) {
		FindAlliance.Builder request = (qxmobile.protobuf.AllianceProtos.FindAlliance.Builder) builder;
		int lianmengId = request.getId();

		FindAllianceResp.Builder response = FindAllianceResp.newBuilder();
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, lianmengId);
		// 过滤掉不是本服务器的联盟, 正式服务器数据库分开就没有这个问题。
		if (alncBean == null || alncBean.creatorId % 1000 != GameServer.serverId) {
			logger.error("查找联盟失败，未找到对应的联盟信息，id:{}", lianmengId);
			response.setCode(1);
			response.setMsg("很遗憾，找不到这个联盟...");
			session.write(response.build());
			return;
		}
		int memberMax = getAllianceMemberMax(alncBean.level);
		if(alncBean.members >= memberMax) {
			alncBean.isAllow = CLOSE_APPLY;
			HibernateUtil.save(alncBean);
		}
		NonAllianceInfo.Builder alncInfo = NonAllianceInfo.newBuilder();
		fillNonAllianceInfo(alncInfo, alncBean, null);
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

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, lianmengId);
		if (alncBean == null) {
			sendApplyAllianceResponse(session, 2, lianmengId, "很遗憾，找不到这个联盟...");
			logger.error("联盟申请失败，未找到对应的联盟信息，id:{}", lianmengId);
			return;
		}
		
		if(!checkInterval(junZhu.id, lianmengId)) {
			sendApplyAllianceResponse(session, 9, lianmengId, "距上次离开该联盟冷却时间未到");
			logger.error("联盟申请失败，距上次离开该联盟冷却时间未到");
			return;
		}
		
		if (alncBean.country != junZhu.guoJiaId) {
			sendApplyAllianceResponse(session, 8, lianmengId, "不能申请其他国家的联盟");
			logger.error("联盟申请失败，玩家-{}:国家-{}, 联盟-{}:国家-{}", junZhu.id,
					junZhu.guoJiaId, alncBean.id, alncBean.country);
			return;
		}
		
		if (alncBean.isAllow == CLOSE_APPLY) {
			logger.error("联盟申请失败，要加入的联盟:{}未开启招募!", lianmengId);
			sendApplyAllianceResponse(session, 7, lianmengId, "申请的联盟关闭的招募");
			return;
		}
		
		int memberMax = getAllianceMemberMax(alncBean.level);
		if (alncBean.members >= memberMax) {
			logger.error("联盟申请失败，该联盟人数已满，id:{}", lianmengId);
			sendApplyAllianceResponse(session, 3, lianmengId, "该联盟人数已满");
			return;
		}
		
		if (junZhu.level < alncBean.minApplyLevel) {
			sendApplyAllianceResponse(session, 5, lianmengId, "");
			logger.error("联盟申请失败，君主:{}等级:{}不符合入盟要求等级:{}", junZhu.name, junZhu.level,alncBean.minApplyLevel);
			return;
		}
		
		PvpBean pvpBean = HibernateUtil.find(PvpBean.class, junZhu.id);
		int junXianLevel = pvpBean == null ? PVPConstant.XIAO_ZU_JI_BIE
				: pvpBean.junXianLevel;
		if (junXianLevel < alncBean.minApplyJunxian) {
			sendApplyAllianceResponse(session, 4, lianmengId, "");
			logger.error("联盟申请失败，君主:{}军衔等级:{}不符合入盟要求军衔等级:{}", junZhu.name,junXianLevel, alncBean.minApplyJunxian);
			return;
		}

		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, junZhu.id);
		if (applyer == null) {
			applyer = new AllianceApply();
			applyer.junzhuId = junZhu.id;
			applyer.addAllianceId(alncBean.id);
			HibernateUtil.insert(applyer);
		} else {
			if (applyer.isAllianceExist(alncBean.id)) {
				sendApplyAllianceResponse(session, 1, lianmengId, "已经申请了该联盟");
				logger.error("联盟申请失败，君主:{}已经申请了联盟:{}}", junZhu.id, alncBean.id);
				return;
			}
			if (applyer.getAllianceNum() >= AllianceConstants.APPPLY_NUM_MAX) {
				sendApplyAllianceResponse(session, 6, lianmengId, "联盟申请数量已满");
				logger.error("联盟申请失败，君主:{}申请了联盟数量已经满了数量:{}", junZhu.id, applyer.getAllianceNum());
				return;
			}
			applyer.addAllianceId(alncBean.id);
			HibernateUtil.save(applyer);
		}
		Redis.getInstance().sadd(CACHE_APPLYERS_OF_ALLIANCE + alncBean.id,
				"" + applyer.junzhuId);
		logger.info("联盟申请成功，{}申请加入联盟{}", junZhu.id, alncBean.id);
		sendApplyAllianceResponse(session, 0, lianmengId, "申请成功");

		List<AlliancePlayer> members = HibernateUtil.list(AlliancePlayer.class,
				" where lianMengId= " + lianmengId + " and title in (" + TITLE_LEADER
						+ "," + TITLE_DEPUTY_LEADER + ")");
		for (AlliancePlayer member : members) {
			IoSession isession = AccountManager.getIoSession(member.junzhuId);
			if (isession != null) {
				isession.write(PD.ALLIANCE_HAVE_NEW_APPLYER);
				FunctionID.pushCanShangjiao(member.junzhuId, isession, FunctionID.ALLIANCE_NEW_APPLYER);
			}
		}
	}

	protected void sendApplyAllianceResponse(IoSession session, int result,
			int allianceId, String msg) {
		ApplyAllianceResp.Builder response = ApplyAllianceResp.newBuilder();
		response.setCode(result);
		response.setId(allianceId);
		response.setMsg(msg);
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

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, lianmengId);
		if (alncBean == null) {
			logger.error("取消联盟申请失败，未找到对应的联盟信息，id:{}", lianmengId);
			sendCancelJoinAllianceResp(session, 1, lianmengId, "很遗憾，找不到这个联盟...");
			return;
		}

		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, junZhu.id);
		if (applyer == null) {
			logger.error("取消联盟申请失败，未找到对应的联盟申请信息，junzhu:{},联盟id:{}", junZhu.name, lianmengId);
		} else {
			applyer.removeAlliance(alncBean.id);
			HibernateUtil.save(applyer);
		}
		Redis.getInstance().sremove(CACHE_APPLYERS_OF_ALLIANCE + alncBean.id,
				"" + junZhu.id);
		logger.info("取消联盟申请成功，junzhu:{}取消了联盟id:{}的加入申请", junZhu.name, lianmengId);
		sendCancelJoinAllianceResp(session, 0, lianmengId, "取消联盟申请成功");
	}

	protected void sendCancelJoinAllianceResp(IoSession session, int result,
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
		AlliancePlayer exitMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (exitMember == null || exitMember.lianMengId <= 0) {
			logger.error("退出联盟失败，君主:{}还不是联盟成员", cmd);
			sendExitAllianceResp(session, 2);
			return;
		}

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, exitMember.lianMengId);
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
		GuoJiaMgr.inst.calculateGongJinBeforeQuitAlliance(exitMember.junzhuId);

		resetAlliancePlayer(alncBean, exitMember);
		HibernateUtil.save(exitMember);
		alncBean.members -= 1;
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
		ActLog.log.Guild(junZhu.id, junZhu.name, ActLog.vopenid, "OUT", alncBean.id, alncBean.name, alncBean.level, "自主退出");
		sendExitAllianceResp(session, 0);
		JunZhuMgr.inst.sendMainInfo(session);
		// 记录玩家退出的联盟及时间
		Redis.getInstance().hset(ALLIANCE_EXIT + junZhu.id, String.valueOf(alncBean.id),
				DateUtils.date2Text(date, EXIT_TIME_FORMAT));
		changeGongXianRecord(junZhu.id, 0);
		String eventStr = lianmengEventMap.get(4).str.replaceFirst("%d", junZhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30020);
		if (mailConfig != null) {
			EmailMgr.INSTANCE.sendMail(junZhu.name, mailConfig.content, "",
					mailConfig.sender, mailConfig, "");
		}
		EventMgr.addEvent(ED.Leave_LM, new Object[] { junZhu.id, alncBean.id,
				alncBean.name, alncBean.level });
		// 2015-7-22 16:13 刷新联盟榜
		EventMgr.addEvent(ED.LIANMENG_RANK_REFRESH, new Integer(alncBean.id));
	}

	protected void sendExitAllianceResp(IoSession session, int code) {
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
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class,
				junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("junzhu: {}，未加入联盟", junZhu.name);
			return;
		}

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class,
				mgrMember.lianMengId);
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
	public List<AlliancePlayer> getAllianceMembers(int alncId) {
		List<AlliancePlayer> memberList = new ArrayList<AlliancePlayer>();
		Set<String> idsSet = Redis.getInstance().sget(CACHE_MEMBERS_OF_ALLIANCE + alncId);
		if (idsSet == null || idsSet.size() == 0) {
			memberList = HibernateUtil.list(AlliancePlayer.class, " where lianMengId=" + alncId);
		} else {
			for (String id : idsSet) {
				Long memberId = Long.parseLong(id);
				AlliancePlayer alncPlayer = HibernateUtil.find(AlliancePlayer.class, memberId);
				if (alncPlayer == null) {
					logger.error("联盟id:{},未找到成员id:{}", alncId, memberId);
					continue;
				}
				memberList.add(alncPlayer);
			}
		}
		return memberList;
	}
	
	protected List<MemberInfo> getAllMemberInfoList(int allianceId) {
		List<AllianceProtos.MemberInfo> memberInfoRespList = new ArrayList<AllianceProtos.MemberInfo>();
		List<AlliancePlayer> memberList = getAllianceMembers(allianceId);
		for (AlliancePlayer member : memberList) {
			MemberInfo.Builder memberInfo = MemberInfo.newBuilder();
			long junzhuId = member.junzhuId;
			JunZhu jz = HibernateUtil.find(JunZhu.class, junzhuId);
			PvpBean pvpBean = HibernateUtil.find(PvpBean.class, junzhuId);
			fillMemberInfo(member, memberInfo, jz, pvpBean);
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
	protected void fillMemberInfo(AlliancePlayer member,
			MemberInfo.Builder memberInfo, JunZhu jz, PvpBean pvpBean) {
		memberInfo.setLevel(jz.level);
		memberInfo.setName(jz.name);
		memberInfo.setContribution(member.gongXian);
		memberInfo.setJunXian(pvpBean == null ? PVPConstant.XIAO_ZU_JI_BIE
				: pvpBean.junXianLevel);
		memberInfo.setIdentity(member.title);
		memberInfo.setJunzhuId(jz.id);
		memberInfo.setIsBaoming(member.isBaoming);
		memberInfo.setIsVoted(member.isVoted);
		memberInfo.setVoteNum(member.voteNum);
		memberInfo.setRoleId(jz.roleId);
		ResourceGongJin gjBean = HibernateUtil.find(ResourceGongJin.class, member.junzhuId);
		int gongJin = GuoJiaMgr.inst.getGongJin(gjBean, PvpMgr.getJunxianLevel(member.junzhuId));
		if(GuoJiaMgr.inst.isCanShangjiao(member.junzhuId)){
			GuoJiaMgr.inst.pushCanShangjiao(member.junzhuId);
		}
		memberInfo.setGongJin(gongJin);
		int zhanLi = JunZhuMgr.inst.getZhanli(jz);
		memberInfo.setZhanLi(zhanLi);
		if(SessionManager.inst.isOnline(jz.id)) {
			memberInfo.setOfflineTime(-1);
		} else {
			int offlineTime = SessionManager.inst.getOfflineTime(jz.id);
			memberInfo.setOfflineTime(offlineTime);
		}
		memberInfo.setCurMonthGongXian(getCurMonthGongXian(member.junzhuId));
	}

	public synchronized void fireMember(int cmd, IoSession session, Builder builder) {
		FireMember.Builder request = (qxmobile.protobuf.AllianceProtos.FireMember.Builder) builder;
		long memberId = request.getJunzhuId();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mengZhu = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mengZhu == null || mengZhu.lianMengId <= 0) {
			logger.error("开除联盟成员失败，君主:{}未加入联盟", junZhu.id);
			sendError(cmd, session, "你不是联盟成员");
			return;
		}
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mengZhu.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mengZhu)) {
			logger.error("开除联盟成员失败，君主:{}没有权限，职位:{}", junZhu.id, mengZhu.title);
			return;
		}
		if (memberId == junZhu.id) {
			logger.error("开除联盟成员失败，不能开除自己", junZhu.id, mengZhu.title);
			sendError(cmd, session, "盟主不能开除自己");
			return;
		}
		AlliancePlayer target = HibernateUtil.find(AlliancePlayer.class, memberId);
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
		GuoJiaMgr.inst.calculateGongJinBeforeQuitAlliance(target.junzhuId);

		resetAlliancePlayer(alncBean, target);
		HibernateUtil.save(target);
		alncBean.members -= 1;
		HibernateUtil.save(alncBean);
		Redis.getInstance().sremove(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id,
				"" + target.junzhuId);
		Date date = new Date();
		logger.info("开除联盟成员成功，{}开除成员{},时间:{}", mengZhu.junzhuId, target.junzhuId, date);
		ActLog.log.Guild(target.junzhuId, "", ActLog.vopenid, "OUT", alncBean.id, alncBean.name, alncBean.level, "开除");
		ActLog.log.GuildOut(mengZhu.junzhuId, "", ActLog.vopenid, alncBean.id, alncBean.name, ActLog.vopenid, target.junzhuId,  "");
		sendFireMemberResp(session, 0, alncBean.id, memberId);
		// 发送邮件通知
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30004);
		JunZhu firedJunzhu = HibernateUtil.find(JunZhu.class, memberId);
		EmailMgr.INSTANCE.sendMail(firedJunzhu.name, mailConfig.content, "",
				mailConfig.sender, mailConfig, "");
		
		// 记录玩家离开联盟的时间
		Redis.getInstance().hset(ALLIANCE_EXIT + target.junzhuId, String.valueOf(alncBean.id),
				DateUtils.date2Text(date, EXIT_TIME_FORMAT));
		changeGongXianRecord(memberId, 0);
		String eventStr = lianmengEventMap.get(3).str.replaceFirst("%d", firedJunzhu.name)
													.replaceFirst("%d", junZhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		
		// 向被开除并在线的玩家发送通知
		SessionUser su = SessionManager.inst.findByJunZhuId(target.junzhuId);
		if (su != null && su.session != null) {
			su.session.write(PD.ALLIANCE_FIRE_NOTIFY);
			su.session.removeAttribute(SessionAttKey.LM_NAME);
		}
		EventMgr.addEvent(ED.Leave_LM, new Object[] { target.junzhuId,
				alncBean.id, alncBean.name, alncBean.level });
		// 联盟榜刷新
		EventMgr.addEvent(ED.LIANMENG_RANK_REFRESH, alncBean.id);
	}

	protected void sendFireMemberResp(IoSession session, int result,
			int lianmengId, long memberId) {
		FireMemberResp.Builder response = FireMemberResp.newBuilder();
		response.setResult(result);
		response.setId(lianmengId);
		response.setJunzhuId(memberId);
		session.write(response.build());
	}

	/**
	 * 验证盟主操作
	 * 
	 */
	protected boolean verifyIllegalOper(int cmd, IoSession session,
			AllianceBean alncBean, AlliancePlayer mengZhu) {
		if (alncBean == null) {
			sendError(cmd, session, "该联盟不存在");
			return false;
		}

		if (mengZhu == null) {
			sendError(cmd, session, "非法操作102");
			return false;
		}
		if (mengZhu.title != TITLE_LEADER) {
			sendError(cmd, session, "非法操作103");
			return false;
		}
		if (mengZhu.lianMengId != alncBean.id) {
			sendError(cmd, session, "非法操作104");
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
		
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, curMgr.id);
		if (mgrMember == null) {
			logger.error("联盟成员升职失败，君主:{}未加入联盟", curMgr.name);
			return;
		}

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mgrMember)) {
			logger.error("联盟成员升职失败，君主:{}没有权限1，职位:{}", curMgr.name, mgrMember.title);
			return;
		}

		AlliancePlayer target = HibernateUtil.find(AlliancePlayer.class, memberId);
		if (target == null || target.lianMengId != mgrMember.lianMengId) {
			logger.error("联盟成员升职失败，将要升职的君主id:{}已经退出联盟:{}", memberId, alncBean.id);
			sendUpTitleResp(session, 2, mgrMember.lianMengId, memberId, -1);
			return;
		}
		int title = target.title;
		if (title == TITLE_DEPUTY_LEADER) {
			logger.error("联盟成员升职失败，成员:{}不能再升职1，职位:{}", memberId, target.title);
			sendError(cmd, session, "副盟主不能再升职");
			return;
		} else if (title != TITLE_MEMBER) {
			logger.error("联盟成员升职失败，成员:{}不能再升职2，职位:{}", memberId, target.title);
			sendError(cmd, session, "非法操作");
			return;
		}
		// 验证副盟主数量
		LianMeng lmCfg = lianMengMap.get(alncBean.level);
		if (lmCfg == null) {
			logger.error("联盟成员升职失败，找不到联盟配置文件。联盟等级:{}", alncBean.level);
			sendError(cmd, session, "配置文件出错");
			return;
		}
		
		if (alncBean.deputyLeaderNum >= lmCfg.getFumeng()) {
			logger.error("联盟成员升职失败，联盟:{}副盟主数量已达:{}", alncBean.name, lmCfg.getFumeng());
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
	}

	protected void sendUpTitleResp(IoSession session, int result, int lianmengId,
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
		
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, curMgr.id);
		if (mgrMember == null) {
			logger.error("联盟成员降职失败，君主:{}未加入联盟", curMgr.name, cmd);
			return;
		}

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mgrMember)) {
			logger.error("联盟成员降职失败，君主id:{}没有权限1，职位:{}", memberId, mgrMember.title);
			return;
		}

		AlliancePlayer target = HibernateUtil.find(AlliancePlayer.class, memberId);
		if (target == null || target.lianMengId != mgrMember.lianMengId) {
			logger.error("联盟成员降职失败，将要降职的君主id:{}已经退出联盟:{}", memberId, alncBean.id);
			sendDownTitleResp(session, 1, mgrMember.lianMengId, memberId, -1);
			return;
		}
		
		if (target.title == TITLE_MEMBER) {
			logger.error("联盟成员降职失败，成员id:{}不能再降职1，职位:{}", memberId, target.title);
			sendError(cmd, session, "成员不能再降职");
			return;
		} else if (target.title != TITLE_DEPUTY_LEADER) {
			logger.error("联盟成员降职失败，成员id:{}不能再降职2，职位:{}", memberId, target.title);
			sendError(cmd, session, "操作异常");
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
	}

	protected void sendDownTitleResp(IoSession session, int result,
			int lianmengId, long junzhuId, int title) {
		DownTitleResp.Builder response = DownTitleResp.newBuilder();
		response.setCode(result);
		response.setId(lianmengId);
		response.setJunzhuId(junzhuId);
		response.setTitle(title);
		session.write(response.build());
	}
	
	/**
	 * 间隔时间是否可以加入联盟
	 * @param junzhuId
	 * @param lianMengId
	 * @return true-可以加入，false-不可以加入
	 */
	public boolean checkInterval(long junzhuId, int lianMengId) {
		String exitTime = Redis.getInstance().hget(ALLIANCE_EXIT + junzhuId, String.valueOf(lianMengId));
		if(exitTime == null || "".equals(exitTime)) {
			return true;
		}
		Date exitDate = DateUtils.text2Date(exitTime, EXIT_TIME_FORMAT);
		Date curDate = new Date();
		long interval = curDate.getTime() - exitDate.getTime();
		if(interval >= JOIN_SAME_INTEVAL) {
			Redis.getInstance().hdel(ALLIANCE_EXIT + junzhuId, String.valueOf(lianMengId));
			return true;
		}
		return false;
	}
	
	
	public void lookApplicants(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mgrMember == null) {
			logger.error("查看联盟申请列表失败，找不到君主:{}", junZhu.id);
			return;
		}
		int lianmengId = mgrMember.lianMengId;
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, lianmengId);
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

		LookApplicantsResp.Builder response = LookApplicantsResp.newBuilder();
		List<AllianceApply> applyList = getApplyers(alncBean.id);
		for (AllianceApply alncApply : applyList) {
			ApplicantInfo.Builder info = ApplicantInfo.newBuilder();
			long jzId = alncApply.junzhuId;
			JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
			PvpBean pvpBean = HibernateUtil.find(PvpBean.class, jzId);
			info.setJunzhuId(jzId);
			info.setLevel(jz.level);
			info.setName(jz.name);
			info.setRoleId(jz.roleId);
			int zhanLi = JunZhuMgr.inst.getZhanli(jz);
			info.setZhanLi(zhanLi);
			if (pvpBean == null) {
				info.setJunXian(PVPConstant.XIAO_ZU_JI_BIE);
				info.setRank(Short.MAX_VALUE);// FIXME 默认rank
			} else {
				info.setJunXian(pvpBean.junXianLevel);
				info.setRank(PvpMgr.inst.getPvpRankById(pvpBean.junZhuId));
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
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mgrMember == null) {
			logger.error("拒绝联盟申请失败，找不到君主:{}", junZhu.id);
			return;
		}
		int lianmengId = mgrMember.lianMengId;
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, lianmengId);
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
		if (applyer == null || !applyer.isAllianceExist(alncBean.id)) {
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
		AlliancePlayer mengZhu = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mengZhu == null) {
			logger.error("同意联盟申请失败，君主:{}不是联盟成员", junZhu.id);
			return;
		}
		int lianmengId = mengZhu.lianMengId;
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, lianmengId);
		if (alncBean == null) {
			logger.error("同意联盟申请失败，联盟:{}不存在", lianmengId);
			return;
		}
		int memberMax = getAllianceMemberMax(alncBean.level);
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
		if (applyer == null || !applyer.isAllianceExist(alncBean.id)) {
			logger.error("同意联盟申请失败，目标申请不存在 jzId {} LM id {}", applyJzId, lianmengId);
			sendAgreeApplyResp(session, 3, applyJzId, lianmengId, null);
			return;
		}

		Date date = new Date();
		AlliancePlayer alncPlayer = HibernateUtil.find(AlliancePlayer.class, applyJzId);
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
		HibernateUtil.save(alncPlayer);
		initAllianceGongXianRecord(applyJzId);
		
		// 加入联盟，重新开始计算贡金  add 20150915
		GuoJiaMgr.inst.calculateGongJinJoinAlliance(applyJzId);

		// 批准成功操作，删除申请的其他联盟
		clearApplyAllianceCache(applyer);
		HibernateUtil.save(applyer);

		Redis.getInstance().sadd(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id,
				"" + alncPlayer.junzhuId);
		alncBean.members += 1;
		if(alncBean.members >= memberMax) {
			alncBean.isAllow = CLOSE_APPLY;
		}
		HibernateUtil.save(alncBean);

		MemberInfo.Builder memberInfo = MemberInfo.newBuilder();
		JunZhu memberJunzhu = HibernateUtil.find(JunZhu.class, applyJzId);
		PvpBean pvpBean = HibernateUtil.find(PvpBean.class, applyJzId);
		fillMemberInfo(alncPlayer, memberInfo, memberJunzhu, pvpBean);
		sendAgreeApplyResp(session, 0, applyJzId, lianmengId, memberInfo);
		EventMgr.addEvent(ED.Join_LM, new Object[] { applyJzId, alncBean.id });
		logger.info("同意联盟申请成功，{}批准{}:{}加入联盟{}:{}", junZhu.id, applyJzId, memberJunzhu.name,
				alncBean.id, alncBean.name);
		ActLog.log.Guild(applyJzId, memberJunzhu.name, ActLog.vopenid, "JOIN", alncBean.id, alncBean.name, alncBean.level, "");
		String eventStr = lianmengEventMap.get(2).str.replaceFirst("%d", memberJunzhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		// 2015-7-22 16:15 刷新联盟榜
		EventMgr.addEvent(ED.LIANMENG_RANK_REFRESH, alncBean.id);
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30017);
		if (mailConfig != null) {
			String content = mailConfig.content.replace("***", alncBean.name);
			EmailMgr.INSTANCE.sendMail(junZhu.name, content, "",
					mailConfig.sender, mailConfig, "");
		}
		// 向被批准并在线的玩家发送通知
		SessionUser su = SessionManager.inst.findByJunZhuId(applyJzId);
		if (su != null && su.session != null) {
			JunZhuMgr.inst.sendMainInfo(su.session);
			su.session.write(PD.ALLIANCE_ALLOW_NOTIFY);
			su.session.setAttribute(SessionAttKey.LM_NAME, alncBean.name);
		}
	}

	protected void sendAgreeApplyResp(IoSession session, int result, long jzId,
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
	protected void clearApplyAllianceCache(AllianceApply applyer) {
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
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mgrMember == null || mgrMember.lianMengId <= 0) {
			logger.error("修改联盟公告失败-君主:{}未加入联盟", junZhu.id);
			return;
		}

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
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
		if (notice.length() > 200) {
			logger.error("修改联盟公告失败-公告长度太长 notice长度:{}", notice.length());
			response.setCode(1);
			session.write(response.build());
			return;
		}
		if (BigSwitch.inst.accMgr.isBadString(notice)) {
			logger.error("修改联盟公告失败-有非法字符 notice:{}", notice);
			response.setCode(2);
			session.write(response.build());
			return;
		}

		alncBean.notice = notice;
		HibernateUtil.save(alncBean);
		logger.info("修改联盟公告成功-君主:{}设置了联盟:{}的公告{}", mgrMember.junzhuId, mgrMember.lianMengId, notice);
		response.setCode(0);
		session.write(response.build());
		
		String eventStr = "";
		if(mgrMember.title == TITLE_LEADER) {
			eventStr = lianmengEventMap.get(8).str.replaceFirst("%d", junZhu.name);
		} else if(mgrMember.title == TITLE_DEPUTY_LEADER){
			eventStr = lianmengEventMap.get(16).str.replaceFirst("%d", junZhu.name);
		}
		addAllianceEvent(alncBean.id, eventStr);
	}

	public void dismissAlliance(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mgrMember == null) {
			logger.error("解散联盟失败，君主:{}未加入联盟", junZhu.id);
			return;
		}
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mgrMember)) {
			logger.error("解散联盟失败，君主:{}没有权限，职位:{}", junZhu.id, mgrMember.title);
			return;
		}

		int lianmengId = mgrMember.lianMengId;
		logger.info("解散联盟成功，[{}:{}]解散了联盟:{},id:{},时间:{}", junZhu.id, junZhu.name,
				alncBean.name, alncBean.id, new Date());
		ActLog.log.GuildBreak(junZhu.id, junZhu.name, ActLog.vopenid, alncBean.id, alncBean.name, alncBean.level, alncBean.exp, alncBean.build);
		List<AlliancePlayer> memberList = getAllianceMembers(lianmengId);
		for (AlliancePlayer member : memberList) {
			// resetAlliancePlayer 之前，总结计算一次贡金 add 20150915
			GuoJiaMgr.inst.calculateGongJinBeforeQuitAlliance(member.junzhuId);

			resetAlliancePlayer(alncBean, member);
			HibernateUtil.save(member);
			logger.info("解散联盟成功，联盟{}解散，删除成员 {}", alncBean.id, member.junzhuId);
			ActLog.log.Guild(member.junzhuId, "", ActLog.vopenid, "OUT", alncBean.id, alncBean.name, alncBean.level, "解散");
			String jzCacheKey = HYMgr.CACHE_HYSTORE_APPLY + member.lianMengId + "_" + member.junzhuId;
			Redis.getInstance().del(jzCacheKey);
			SessionUser su = SessionManager.inst.findByJunZhuId(member.junzhuId);
			if (su != null && su.session != null) {
				su.session.write(PD.ALLIANCE_DISMISS_NOTIFY);
				su.session.removeAttribute(SessionAttKey.LM_NAME);
				su.session.removeAttribute(SessionAttKey.LM_ZHIWU);
			}
			// 触发事件，房屋管理用到。增加传送联盟等级
			EventMgr.addEvent(ED.Leave_LM, new Object[] { member.junzhuId,
					alncBean.id, alncBean.name, alncBean.level });
		}
		RankingMgr.inst.remLianmeng(alncBean.id);// 联盟榜中删除数据
		HibernateUtil.delete(alncBean);
		Redis.getInstance().del(CACHE_MEMBERS_OF_ALLIANCE + lianmengId);
		Redis.getInstance().del(CACHE_APPLYERS_OF_ALLIANCE + lianmengId);
		
		EventMgr.addEvent(ED.DISMISS_ALLIANCE, alncBean.id);
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

	protected void dismissAllianceProcess(Event param) {
		int lianMengId = (Integer)param.param;
		// 联盟排行榜中移除次数据
		RankingMgr.inst.removeLianmeng(lianMengId);
		// 获取所有申请该联盟的玩家，移除该联盟
		List<AllianceApply> applyList = getApplyers(lianMengId);
		for (AllianceApply applyer : applyList) {
			applyer.removeAlliance(lianMengId);
			HibernateUtil.save(applyer);
		}
		// 20150909 去掉荒野资源点
//		HYMgr.inst.delHYInfo(lianMengId);
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
			if (applyer != null) {
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
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mgrMember == null) {
			logger.error("打开联盟招募失败-君主:{}还未加入联盟", junZhu.id);
			return;
		}
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mgrMember)) {
			logger.error("打开联盟招募失败-君主:{}没有权限，职位:{}", junZhu.id, mgrMember.title);
			return;
		}
		OpenApplyResp.Builder response = OpenApplyResp.newBuilder();
		int memberMax = getAllianceMemberMax(alncBean.level);
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
		if (BigSwitch.inst.accMgr.isBadString(attach)) {
			logger.error("打开联盟招募失败-有非法字符，招募公告:{}", attach);
			response.setCode(3);
			session.write(response.build());
			return;
		}
		if (attach == null) {
			attach = "";
		}

		alncBean.minApplyLevel = Math.max(APPLY_LEVEL_MIN, levelMin);
		alncBean.minApplyJunxian = junXianMin;
		alncBean.isShenPi = isExamine;
		alncBean.isAllow = OPEN_APPLY;
		alncBean.attach = attach;
		HibernateUtil.save(alncBean);
		response.setCode(0);
		session.write(response.build());
		logger.info("打开联盟招募成功，联盟:{}", alncBean.id);
	}

	public void closeApply(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mgrMember == null) {
			logger.error("关闭联盟招募失败-君主:{}还未加入联盟", junZhu.id);
			return;
		}
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
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

		AlliancePlayer mengZhu = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mengZhu == null) {
			logger.error("转让联盟失败-君主:{}还未加入联盟", junZhu.id);
			return;
		}

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mengZhu.lianMengId);
		if (!verifyIllegalOper(cmd, session, alncBean, mengZhu)) {
			logger.error("转让联盟失败-君主:{}没有权限1，职位:{}", junZhu.id, mengZhu.title);
			return;
		}

		if (mengZhu.title != TITLE_LEADER) {
			logger.error("转让联盟失败-君主:{}没有权限2，职位:{}", junZhu.id, mengZhu.title);
			sendError(cmd, session, "非法操作");
			return;
		}

		AlliancePlayer fuMengZhu = HibernateUtil.find(AlliancePlayer.class, fuMengzhuId);
		if (fuMengZhu == null || fuMengZhu.lianMengId != mengZhu.lianMengId) {
			sendTransferAllianceResp(session, mengZhu.lianMengId, mengZhu.junzhuId, 1);
			logger.error("转让联盟失败-将要降职的君主id:{}已经退出联盟:{}", fuMengzhuId, alncBean.id);
			return;
		}
		if (fuMengZhu.title != TITLE_DEPUTY_LEADER) {
			logger.error("转让联盟失败-将要降职的君主id:{}职位:{}", fuMengzhuId, fuMengZhu.title);
			sendError(cmd, session, "要转让的对象职位太低");
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
		ActLog.log.GuildTransfer(fuMengZhu.junzhuId, targetJz.name, ActLog.vopenid, alncBean.id, alncBean.name, ActLog.vopenid, junZhu.id, junZhu.name);
		sendTransferAllianceResp(session, mengZhu.lianMengId, fuMengzhuId, 0);
		String eventStr = lianmengEventMap.get(5).str.replaceFirst("%d", junZhu.name)
													.replaceFirst("%d", targetJz.name);
		addAllianceEvent(alncBean.id, eventStr);
		
		List<AlliancePlayer> members = getAllianceMembers(alncBean.id);
		// 发给除了盟主和要转给的副盟主的其它盟员
		for (AlliancePlayer member : members) {
			if (member.junzhuId != mengZhu.junzhuId && member.junzhuId != fuMengzhuId) {
				Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(30016);
				JunZhu memberJunzhu = HibernateUtil.find(JunZhu.class, member.junzhuId);
				String content = mailConfig.content
						.replace("***", junZhu.name)
						.replace("XXX", targetJz.name);
				EmailMgr.INSTANCE.sendMail(memberJunzhu.name, content, 
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
		// 发给要转给的副盟主
		mailConfig = EmailMgr.INSTANCE.getMailConfig(30015);
		content = mailConfig.content.replace("***", junZhu.name);
		EmailMgr.INSTANCE.sendMail(targetJz.name, content, "",
				mailConfig.sender, mailConfig, "");
	}
	
	public void sendTransferAllianceResp(IoSession session, int lianMengId, 
			long mengZhuId, int result) {
		TransferAllianceResp.Builder response = TransferAllianceResp.newBuilder();
		response.setResult(result);
		response.setId(lianMengId);
		response.setJunzhuId(mengZhuId);
		session.write(response.build());
	}

	public synchronized void immidiatelyJoin(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		immediatelyJoin.Builder request = (qxmobile.protobuf.AllianceProtos.immediatelyJoin.Builder) builder;
		int lianMengId = request.getLianMengId();

		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, lianMengId);
		if (alncBean == null) {
			logger.error("立刻加入联盟失败，要加入的联盟:{}不存在!", lianMengId);
			sendImmediatelyJoinResp(session, 2, null);
			return;
		}
		
		if(!checkInterval(junZhu.id, lianMengId)) {
			sendImmediatelyJoinResp(session, 9, null);
			logger.error("立刻加入联盟失败，距上次离开该联盟冷却时间未到");
			return;
		}
		
		if (alncBean.country != junZhu.guoJiaId) {
			sendImmediatelyJoinResp(session, 8, null);
			logger.error("立刻加入联盟失败，玩家与联盟不是同一个国家，玩家-{}:国家-{}, 联盟-{}:国家-{}", junZhu.id,
					junZhu.guoJiaId, alncBean.id, alncBean.country);
			return;
		}
		if (alncBean.isAllow == CLOSE_APPLY) {
			logger.error("立刻加入联盟失败，要加入的联盟:{}未开启招募!", lianMengId);
			sendImmediatelyJoinResp(session, 4, null);
			return;
		}
		if (alncBean.isShenPi != AllianceConstants.NO_NEED_SHENPI) {
			logger.error("立刻加入联盟失败，联盟:{}需要审批!", lianMengId);
			sendImmediatelyJoinResp(session, 1, null);
			return;
		}
		int memberMax = getAllianceMemberMax(alncBean.level);
		if (alncBean.members >= memberMax) {
			logger.error("立刻加入联盟失败，联盟:{}人数已满，等级:{}", lianMengId, alncBean.level);
			sendImmediatelyJoinResp(session, 5, null);
			return;
		}

		if (junZhu.level < alncBean.minApplyLevel) {
			logger.error("立刻加入联盟失败，等级不满足要求，联盟:{}要求君主等级不小于:{}，君主等级:{}", lianMengId, alncBean.minApplyLevel, junZhu.level);
			sendImmediatelyJoinResp(session, 6, null);
			return;
		}

		PvpBean pvpBean = HibernateUtil.find(PvpBean.class, junZhu.id);
		int junXianLevel = pvpBean == null ? PVPConstant.XIAO_ZU_JI_BIE
				: pvpBean.junXianLevel;
		if (junXianLevel < alncBean.minApplyJunxian) {
			logger.error("立刻加入联盟失败，等级不满足要求，联盟:{}要求军衔等级不小于:{}，君主等级:{}", lianMengId, alncBean.minApplyJunxian, junXianLevel);
			sendImmediatelyJoinResp(session, 7, null);
			return;
		}
		AllianceApply applyer = HibernateUtil.find(AllianceApply.class, junZhu.id);
		if (applyer != null && applyer.getAllianceNum() >= AllianceConstants.APPPLY_NUM_MAX) {
			logger.error("立刻加入联盟失败，申请的联盟数量已满，申请了{}个， 最大申请{}个",applyer.getAllianceNum(), AllianceConstants.APPPLY_NUM_MAX);
			sendImmediatelyJoinResp(session, 3, null);
			return;
		}
		Date date = new Date();
		AlliancePlayer alncPlayer = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (alncPlayer == null) {
			alncPlayer = new AlliancePlayer();
			initAlliancePlayerInfo(junZhu.id, alncBean.id, alncPlayer, TITLE_MEMBER);
			HibernateUtil.insert(alncPlayer);
		} else if(alncPlayer.lianMengId > 0) {
			logger.error("立刻加入联盟失败，申请者 jzId:{},已经加入了联盟:{}", junZhu.id, alncPlayer.lianMengId);
			return;
		}
		initAllianceGongXianRecord(junZhu.id);

		alncPlayer.lianMengId = alncBean.id;
		alncPlayer.joinTime = date;
		HibernateUtil.save(alncPlayer);
		Redis.getInstance().sadd(CACHE_MEMBERS_OF_ALLIANCE + alncBean.id,
				"" + alncPlayer.junzhuId);
		logger.info("立刻加入联盟成功，junzhu:{}，在时间:{},加入联盟:{}", junZhu.name, date, alncBean.name);
		session.setAttribute(SessionAttKey.LM_NAME, alncBean.name);
		ActLog.log.Guild(junZhu.id, junZhu.name, ActLog.vopenid, "JOIN", alncBean.id, alncBean.name, alncBean.level, "");
		alncBean.members = alncBean.members + 1;
		HibernateUtil.save(alncBean);
		// 批准成功操作，删除申请的其他联盟
		if (applyer != null) {
			clearApplyAllianceCache(applyer);
			HibernateUtil.save(applyer);
		}
		JunZhuMgr.inst.sendMainInfo(session);
		// 触发事件，房屋管理用到。
		EventMgr.addEvent(ED.Join_LM, new Object[] { junZhu.id, alncBean.id });
		AllianceHaveResp.Builder alncInfo = AllianceHaveResp.newBuilder();
		fillAllianceResponse(alncBean, alncInfo, alncPlayer);
		sendImmediatelyJoinResp(session, 0, alncInfo);
		String eventStr = lianmengEventMap.get(2).str.replaceFirst("%d", junZhu.name);
		addAllianceEvent(alncBean.id, eventStr);
		// 联盟榜刷新
		EventMgr.addEvent(ED.LIANMENG_RANK_REFRESH, alncBean.id);

		// 加入联盟，重新开始计算贡金  add 20150915
		GuoJiaMgr.inst.calculateGongJinJoinAlliance(junZhu.id);
	}

	protected void sendImmediatelyJoinResp(IoSession session, int result,
			AllianceHaveResp.Builder alncInfo) {
		immediatelyJoinResp.Builder response = immediatelyJoinResp.newBuilder();
		response.setCode(result);
		if (alncInfo != null) {
			response.setAlncInfo(alncInfo.build());
		}
		session.write(response.build());
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
	}

	/**
	 * 获取君主所在的联盟(信息)
	 * 
	 * @Title: getAllianceById
	 * @Description:
	 * @param id
	 * @return : null 表示君主没有联盟
	 */
	public AllianceBean getAllianceByJunZid(long jzId) {
		// 判断君主是否有联盟
		AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (player == null || player.lianMengId <= 0) {
			logger.info("获取玩家联盟失败，玩家:{}没有联盟", jzId);
			return null;
		}
		AllianceBean guild = HibernateUtil.find(AllianceBean.class, player.lianMengId);
		if (guild == null) {
			logger.error("获取玩家联盟失败，联盟{}不存在", player.lianMengId);
		}
		return guild;
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
			int upgradeNeedExp = lianMengCfg.getExp();
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
				upgradeNeedExp = lianMengCfg.getExp();
			}
			HibernateUtil.save(alliance);
			if (upgrade) {
				String eventStr = lianmengEventMap.get(10).str.replaceFirst("%d", String.valueOf(alliance.level));
				addAllianceEvent(alliance.id, eventStr);
				List<AlliancePlayer> memberList = getAllianceMembers(alliance.id);
				for (AlliancePlayer member : memberList) {
					boolean isOnline = SessionManager.inst.isOnline(member.junzhuId);
					if(isOnline) {
						SessionUser su = SessionManager.inst.findByJunZhuId(member.junzhuId);
						if (su != null && su.session != null) {
							su.session.write(PD.ALLIANCE_LEVEL_UP_NOTIFY);
						}
					}
				}
				// 联盟等级排行提升
				RankingMgr.inst.resetLianMengLevelRedis(alliance.id, alliance.level);
				EventMgr.addEvent(ED.LIANMENG_RANK_REFRESH, new Integer(alliance.id));
			}
			
			logger.info("联盟增加经验成功，增加经验之前等级:{}-经验:{},联盟:{}增加了经验:{},当前等级:{}-经验:{}",
					beforeLevel, beforeExp, alliance.id,exp, alliance.level, alliance.exp);
		}
	}

	/*
	 * changeValue可以小于0
	 */
	public void changeAlianceBuild(AllianceBean alliance, int changeValue) {
		if(alliance == null){
			logger.error("联盟建设值增加失败，传参联盟为null"); 
			return;
		}
		synchronized (buildLock) {
			int beforeBuild = alliance.build;
			alliance.build += changeValue;
			if (alliance.build <= 0) {
				alliance.build = 0;
			}
			HibernateUtil.save(alliance);
			logger.info("联盟建设值增加成功，联盟:{},增加前:{},增加了:{},当前:{}", 
					alliance.id, beforeBuild, changeValue, alliance.build);
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
			EventMgr.addEvent(ED.LIANMENG_RANK_REFRESH, new Integer(alliance.id));
			EventMgr.addEvent(ED.LIANMENG_DAY_RANK_REFRESH, new Object[]{alliance,changeValue});
			EventMgr.addEvent(ED.LIANMENG_WEEK_RANK_REFRESH, new Object[]{alliance,changeValue});
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
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mgrMember == null) {
			logger.error("虎符捐献失败，君主:{}未加入联盟", junZhu.id);
			return;
		}
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
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
		BagMgr.inst.removeItem(bag, AwardMgr.ITEM_HU_FU_ID, count, "联盟虎符捐献",junZhu.level);
		BagMgr.inst.sendBagInfo(cmd, session, builder);
		ItemTemp itemTempCfg = TempletService.getInstance().getItemTemp(AwardMgr.ITEM_HU_FU_ID);
		int addGongxian = count * itemTempCfg.effectId;
		logger.info("虎符捐献成功，玩家:{}捐献虎符:{}个，获得联盟贡献:{}，联盟:{}建设值增加:{}", 
				junZhu.id, count, addGongxian, alncBean.id, addGongxian);
		ActLog.log.GuildDonate(junZhu.id, junZhu.name, ActLog.vopenid, alncBean.id, alncBean.name, count, addGongxian);
		mgrMember.gongXian += addGongxian;
		alncBean.build += addGongxian;
		addAllianceExp(addGongxian, alncBean);
		HibernateUtil.save(mgrMember);
		HibernateUtil.save(alncBean);
		sendDonateHuFuResp(session, 0, addGongxian, addGongxian);
		changeGongXianRecord(junZhu.id, addGongxian);
		String eventStr = lianmengEventMap.get(9).str.replaceFirst("%d", junZhu.name)
				.replaceFirst("%d", String.valueOf(count)).replaceFirst("%d", String.valueOf(addGongxian));
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
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
				junZhu.id);
		if (member == null || member.lianMengId <= 0) {
			return "无";
		} else {
			AllianceBean alnc = HibernateUtil.find(AllianceBean.class,
					member.lianMengId);
			return alnc == null ? "无" : alnc.name;
		}

	}
	
	protected void sendDonateHuFuResp(IoSession session, int result,
			int gongxian, int build) {
		DonateHuFuResp.Builder response = DonateHuFuResp.newBuilder();
		response.setResult(result);
		response.setGongxian(gongxian);
		response.setBuild(build);
		session.write(response.build());
	}
	
	/**
	 * 
	 * @param junzhuId
	 * @param allianceId
	 * @param gongXian
	 */
	public void changeGongXianRecord(long junzhuId, int gongXian) {
		AlliancePlayer alliancePlayer = HibernateUtil.find(AlliancePlayer.class, junzhuId);
		AllianceGongXianRecord gxRecord = HibernateUtil.find(AllianceGongXianRecord.class, junzhuId);
		if(gxRecord == null) {
			logger.error("个人联盟贡献值变更，玩家:{}的联盟贡献记录数据 丢失", junzhuId);
			return;
		} 
		Date date = new Date();
		if(alliancePlayer == null || alliancePlayer.lianMengId <= 0) {
			gxRecord.setCurMonthGongXian(0);
			logger.info("个人联盟贡献值变更，玩家:{},时间:{}离开联盟，将本月贡献清零", junzhuId, date);
		} else {
			boolean sameMonth = DateUtils.isSameMonth(date, gxRecord.getCurMonthFirstTime());
			int beforeValue = gxRecord.getCurMonthGongXian();
			if(sameMonth) {
				gxRecord.setCurMonthGongXian(gxRecord.getCurMonthGongXian() + gongXian);
				logger.info("个人联盟贡献值变更，由于是在同一个月，玩家:{}之前:{}，增加了:{}，当前:{}",
						junzhuId, gongXian, beforeValue, gxRecord.getCurMonthGongXian());
			} else {
				gxRecord.setCurMonthGongXian(gongXian);
				gxRecord.setCurMonthFirstTime(date);
				logger.info("个人联盟贡献值变更，由于月份变更，玩家:{}之前:{}，增加了:{}，当前:{}",
						junzhuId, gongXian, beforeValue, gxRecord.getCurMonthGongXian());
			}
		}
		HibernateUtil.save(gxRecord);
	}
	
	protected int getCurMonthGongXian(long junzhuId) {
		AllianceGongXianRecord gxRecord = HibernateUtil.find(AllianceGongXianRecord.class, junzhuId);
		if(gxRecord == null) {
			logger.error("玩家:{}的联盟贡献记录数据 丢失", junzhuId);
			return 0;
		} 
		return gxRecord.getCurMonthGongXian();
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
		
		List<AlliancePlayer> memberList = getAllianceMembers(lianMengId);
		for (AlliancePlayer member : memberList) {
			boolean isOnline = SessionManager.inst.isOnline(member.junzhuId);
			if(isOnline) {
				SessionUser su = SessionManager.inst.findByJunZhuId(member.junzhuId);
				if (su != null && su.session != null) {
					FunctionID.pushCanShangjiao(member.junzhuId, su.session, FunctionID.ALLIANCE_EVENT);
				}
			}
		}
	}
	
	@Override
	public void proc(Event event) {
		switch (event.id) {
			case ED.DISMISS_ALLIANCE:
				dismissAllianceProcess(event);
				break;
			case ED.Leave_LM:
//				broadCastAllianceInfos(event);
				break;
			case ED.Join_LM:
//				broadCastAllianceInfos(event);
				break;
			case ED.REFRESH_TIME_WORK:
				pushRedPoint(event);
				
				break;
		}
	}
	
	private void pushRedPoint(Event event) {
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
			logger.info("君主：{}--联盟的功能---未开启,不推送",jz.id);
			return;
		}
		AlliancePlayer alliancePlayer = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if(alliancePlayer == null) {
			return;
		}
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, alliancePlayer.lianMengId);
		if(alliance == null) {
			return;
		}
		Set<String> applyList = Redis.getInstance().sget(CACHE_APPLYERS_OF_ALLIANCE + alliance.id);
		if(applyList.size() <= 0) {
			return;
		}
		if(alliancePlayer.title == TITLE_LEADER || alliancePlayer.title == TITLE_DEPUTY_LEADER) {
			FunctionID.pushCanShangjiao(jz.id, session, FunctionID.alliance);
			FunctionID.pushCanShangjiao(jz.id, session, FunctionID.ALLIANCE_NEW_APPLYER);
		}
	}

//2015年9月22日成员离开联盟广播
	public void broadCastAllianceInfos(Event param) {
		Object[] oa = (Object[]) param.param;
		Long jzId = (Long) oa[0];
		Integer lmId = (Integer) oa[1];
		logger.info("君主--{},加入或者离开联盟--id--{}",jzId,lmId);
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class,lmId);
		if (alncBean == null) {
			logger.error("找不到联盟，id:{}", lmId);
			return;
		}
		List<AlliancePlayer> memberList = getAllianceMembers(lmId);
		for (AlliancePlayer member : memberList) {
			if(member.junzhuId == lmId) {
				continue;
			}
			SessionUser su = SessionManager.inst.findByJunZhuId(member.junzhuId);
			if(su!=null){
				requestAllianceInfo(0, su.session, null);
			}
		}
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.DISMISS_ALLIANCE, this);		
		//2015年9月21日 离开联盟是重新广播联盟信息
		EventMgr.regist(ED.Leave_LM, this);
		//2015年9月22日 加入联盟是重新广播联盟信息
		EventMgr.regist(ED.Join_LM, this);
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}

	public void eventListRequest(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if (mgrMember == null) {
			logger.error("联盟事件列表请求失败，君主:{}不是联盟成员", junZhu.id);
			return;
		}
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
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
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, allianceId);
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

}
