package com.manu.network;

import java.util.LinkedList;
import java.util.List;

import log.parser.ReasonMgr;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import xg.push.XG;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.chat.ChatMgr;
//import com.manu.dynasty.pvp.interfaces.NationalWarMgrInterface;
import com.manu.dynasty.template.RobotInitData;
import com.manu.dynasty.util.BaseException;
import com.manu.network.msg.AbstractMessage;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.account.FunctionOpenMgr;
import com.qx.account.SettingsMgr;
import com.qx.achievement.AchievementMgr;
import com.qx.activity.ActivityMgr;
import com.qx.activity.GrowthFundMgr;
import com.qx.activity.LevelUpGiftMgr;
import com.qx.activity.MonthCardMgr;
import com.qx.activity.QiandaoMgr;
import com.qx.activity.ShouchongMgr;
import com.qx.activity.StrengthGetMgr;
import com.qx.activity.XianShiActivityMgr;
import com.qx.alliance.AllianceJuanXianMgr;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AllianceVoteMgr;
//import com.qx.alliance.FengShanMgr;
import com.qx.alliance.HouseMgr;
import com.qx.alliance.MoBaiMgr;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.alliancefight.AllianceFightMgr;
import com.qx.alliancefight.BidMgr;
import com.qx.alliancefight.CdTimeMgr;
import com.qx.award.AwardMgr;
import com.qx.award.DailyAwardMgr;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipMgr;
import com.qx.buff.BuffMgr;
import com.qx.card.CardMgr;
import com.qx.cdkey.CDKeyMgr;
import com.qx.chonglou.ChongLouMgr;
import com.qx.email.EmailMgr;
import com.qx.equip.jewel.JewelMgr;
import com.qx.equip.web.UserEquipAction;
import com.qx.event.EventMgr;
import com.qx.explore.ExploreMgr;
import com.qx.explore.treasure.BaoXiangScene;
import com.qx.explore.treasure.ExploreTreasureMgr;
import com.qx.fight.FightMgr;
import com.qx.friends.FriendMgr;
import com.qx.friends.GreetMgr;
import com.qx.fuwen.FuwenMgr;
import com.qx.guojia.GuoJiaMgr;
import com.qx.hero.HeroMgr;
import com.qx.hero.WuJiangKeJiMgr;
import com.qx.huangye.HYMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.jinengpeiyang.JiNengPeiYangMgr;
import com.qx.jingmai.JmMgr;
import com.qx.junzhu.ChenghaoMgr;
import com.qx.junzhu.GrowUpMgr;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.KeJiMgr;
import com.qx.junzhu.TalentMgr;
import com.qx.liefu.LieFuMgr;
import com.qx.log.LogMgr;
import com.qx.mibao.MibaoMgr;
import com.qx.mibao.v2.MiBaoV2Mgr;
import com.qx.notice.NoticeMgr;
import com.qx.persistent.Cache;
import com.qx.prompt.PromptMsgMgr;
import com.qx.purchase.PurchaseMgr;
import com.qx.pve.PveGuanQiaMgr;
import com.qx.pve.PveMgr;
import com.qx.pvp.LveDuoMgr;
import com.qx.pvp.PvpMgr;
import com.qx.quartz.SchedulerMgr;
import com.qx.ranking.RankingMgr;
import com.qx.robot.RobotProtoType;
import com.qx.secure.AntiCheatMgr;
import com.qx.task.DailyTaskMgr;
import com.qx.task.GameTaskMgr;
import com.qx.timeworker.TimeWorkerMgr;
import com.qx.vip.VipMgr;
import com.qx.world.BroadcastMgr;
import com.qx.world.Scene;
import com.qx.world.SceneMgr;
import com.qx.yabiao.YBRobotMgr;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRestoreMgr;
import com.qx.yabiao.YaBiaoRobotProduceMgr;
import com.qx.youxia.YouXiaMgr;
import com.qx.yuanbao.TXQueryMgr;
import com.qx.yuanbao.YuanBaoMgr;
import com.yy.YYMgr;

/**
 * 网络包分发，适用于可以多线程处理的网络包。
 * 
 * @author 康建虎
 * 
 */
public class BigSwitch {
	public static Logger log = LoggerFactory.getLogger(BigSwitch.class.getSimpleName());
	/**
	 * 服务器启动逻辑中保证初始化。
	 */
	public static BigSwitch inst;
	public PvpMgr pvpMgr;
	public YaBiaoHuoDongMgr ybMgr;
	public PromptMsgMgr ptmgr;
	public GuoJiaMgr gjMgr;
	public LveDuoMgr lveDuoMgr;
	public YBRobotMgr ybrobotMgr;
	// public NationalWarMgrInterface warMgrProxy;
	public YuanBaoMgr yuanbaoMgr;
	public Scene scene;
	public SceneMgr scMgr;
	public PveMgr pveMgr;
	public AccountManager accMgr;
	public YouXiaMgr youXiaMgr;
	public AwardMgr awardMgr;
	public JunZhuMgr junZhuMgr;
	public EventMgr eventMgr;
	public KeJiMgr keJiMgr;
	public BagMgr bagMgr;
	public EquipMgr equipMgr;
	public EmailMgr mailMgr;
	public HeroMgr heroMgr;
	public JmMgr jmMgr;
	public WuJiangKeJiMgr wjKeJiMgr;
	public DailyAwardMgr dailyAwardMgr;
	public TimeWorkerMgr timeWorkerMgr;
	public AchievementMgr achievementMgr;
	public PurchaseMgr pMgr;
	public VipMgr vipMgr;
	public DailyTaskMgr dailyTaskMgr;
	public GameTaskMgr gameTaskMgr;
	public MibaoMgr mibaoMgr;
	public ExploreMgr exploreMgr;
//	public PawnshopMgr pawnshopMgr;
	public AllianceMgr allianceMgr;
//	public FengShanMgr fengshanMgr;
	public AllianceVoteMgr allianceVoteMgr;
	public UserEquipAction userEquipAction;
	public HYMgr hyMgr;
	public ShopMgr shopMgr;
	// 好友
	public FriendMgr friendMgr;
	// 活动
	public QiandaoMgr qiandaoMgr;
	public ActivityMgr activityMgr;
	public ShouchongMgr shouchongMgr;
	// 排行榜
	public RankingMgr rankMgr;
	public LogMgr logMgr;
	// 限时活动
	public XianShiActivityMgr xsActivityMgr;
	public TalentMgr talentMgr;
	public static PveGuanQiaMgr pveGuanQiaMgr;
	public CardMgr cardMgr;
	public AntiCheatMgr antiCheatMgr;
	public volatile MoBaiMgr moBaiMgr;
	public volatile SettingsMgr settingsMgr;
	public volatile HouseMgr houseMgr;
	// 公告
	public NoticeMgr noticeMgr;
	public FuwenMgr fuwenMgr;
	public AllianceFightMgr allianceFightMgr;
	public FightMgr fightMgr;
	public CdTimeMgr cdTimeMgr;
	public BuffMgr buffMgr;
	// cdkey
	public CDKeyMgr cdKeyMgr;
	// 技能培养
	public JiNengPeiYangMgr jiNengPeiYangMgr;
	//场景互动
	public GreetMgr 	greetMgr;
	public ChongLouMgr chongLouMgr;
	public LieFuMgr lieFuMgr;
	public JewelMgr jewelMgr ;
	public MonthCardMgr monthCardMgr;
	public GrowthFundMgr growthFundMgr;
	public StrengthGetMgr strengthGetMgr;
	public LevelUpGiftMgr levelUpGiftMgr;
	public AllianceJuanXianMgr juanXianMgr;
	public ChatMgr chatMgr;
	public ChenghaoMgr chenghaoMgr;
	public BidMgr bidMgr;
	public static BigSwitch getInst() {
		if (inst == null) {
			new BigSwitch();
		}
		return inst;
	}

	public BigSwitch() {
		setInst();
		initProxy();
	}

	public void initProxy() {
		Cache.init();
		new ReasonMgr();
		new MiBaoV2Mgr();
		new TXQueryMgr().start();
		awardMgr = new AwardMgr();
		eventMgr = new EventMgr();
		yuanbaoMgr = new YuanBaoMgr();
		settingsMgr = new SettingsMgr();
		gameTaskMgr = new GameTaskMgr();
		dailyAwardMgr = new DailyAwardMgr();
		pvpMgr = new PvpMgr();
		chatMgr = new ChatMgr();

		jmMgr = new JmMgr();
		equipMgr = new EquipMgr();
		bagMgr = new BagMgr();
		keJiMgr = new KeJiMgr();
		houseMgr = new HouseMgr();
		youXiaMgr = new YouXiaMgr();

		junZhuMgr = new JunZhuMgr();
		scene = new Scene("Default");
		scMgr = new SceneMgr();
		scMgr.lmCities.put(0, scene);
		scene.startMissionThread();
		pveMgr = new PveMgr();
		accMgr = new AccountManager();
		pveGuanQiaMgr = new PveGuanQiaMgr();
		userEquipAction = new UserEquipAction();
		List list = TempletService.listAll(RobotInitData.class.getSimpleName());// TODO
		RobotProtoType robot = new RobotProtoType(list, scene);
		new Thread(robot, "robot").start();
		mailMgr = new EmailMgr();
		heroMgr = new HeroMgr();
		heroMgr.startMissionThread();
		wjKeJiMgr = new WuJiangKeJiMgr();
		cardMgr = new CardMgr();
		cardMgr.startMissionThread();
		timeWorkerMgr = new TimeWorkerMgr();
		achievementMgr = new AchievementMgr();
		pMgr = new PurchaseMgr();
		vipMgr = new VipMgr();
		dailyTaskMgr = new DailyTaskMgr();
		mibaoMgr = new MibaoMgr();
		exploreMgr = new ExploreMgr();
//		pawnshopMgr = new PawnshopMgr();
		allianceMgr = new AllianceMgr();
//		fengshanMgr=new FengShanMgr();
		allianceVoteMgr = new AllianceVoteMgr();
		moBaiMgr = new MoBaiMgr();
		JianZhuMgr.inst = new JianZhuMgr();
		hyMgr = new HYMgr();
		shopMgr = new ShopMgr();
		rankMgr = new RankingMgr();
		friendMgr = new FriendMgr();
		qiandaoMgr = new QiandaoMgr();
		activityMgr = new ActivityMgr();
		shouchongMgr = new ShouchongMgr();
		talentMgr = new TalentMgr();
		antiCheatMgr = new AntiCheatMgr();
		logMgr = new LogMgr();
		ybMgr = new YaBiaoHuoDongMgr();
		ptmgr = new PromptMsgMgr();
		gjMgr = new GuoJiaMgr();
		ybrobotMgr = new YBRobotMgr();
		xsActivityMgr=new XianShiActivityMgr();
		noticeMgr = new NoticeMgr();
		lveDuoMgr = new LveDuoMgr(); // 掠夺管理类实例化
		chenghaoMgr = new ChenghaoMgr();
		new GrowUpMgr();
		fuwenMgr = new FuwenMgr();
		allianceFightMgr = new AllianceFightMgr();
		fightMgr = new FightMgr();
		cdTimeMgr = new CdTimeMgr();
		cdTimeMgr.start();
		buffMgr = new BuffMgr();
		buffMgr.startWork();
		cdKeyMgr = new CDKeyMgr();
		jiNengPeiYangMgr = new JiNengPeiYangMgr();
		greetMgr=new GreetMgr();
		jewelMgr = new JewelMgr();
		YYMgr.getInst();
		new ExploreTreasureMgr().makeScene();
		new YaBiaoRobotProduceMgr();
		// 添加服务器定时任务
		new SchedulerMgr().doSchedule();
		//	 恢复押镖场景的玩家马车
		YaBiaoRestoreMgr.getInstance().backUpYaBiaoData();
		chongLouMgr = new ChongLouMgr();
		lieFuMgr = new LieFuMgr();
		monthCardMgr = new MonthCardMgr();
		growthFundMgr = new GrowthFundMgr();
		strengthGetMgr = new StrengthGetMgr();
		levelUpGiftMgr = new LevelUpGiftMgr();
		juanXianMgr = new AllianceJuanXianMgr();
		bidMgr = new BidMgr();
	}

	public void loadModuleData() {
		allianceMgr.inst.initData();
//		fengshanMgr.inst.initData();
		mibaoMgr.inst.initData();
//		pawnshopMgr.inst.initData();
		exploreMgr.inst.initData();
		vipMgr.INSTANCE.initData();
		pMgr.inst.initData();
		achievementMgr.instance.initData();
		wjKeJiMgr.inst.initData();
		heroMgr.inst.initData();
		awardMgr.inst.initData();
		userEquipAction.initData();
		accMgr.initData();
		pveMgr.inst.initData();
		gameTaskMgr.inst.initData();
		pvpMgr.inst.initData();
		ybMgr.inst.initData();
		gjMgr.inst.initData();
		dailyTaskMgr.INSTANCE.initData();
		hyMgr.inst.initData();
		mailMgr.initData();
		friendMgr.initData();
		qiandaoMgr.initData();
		activityMgr.initData();
		shouchongMgr.initData();
		youXiaMgr.initData();
		logMgr.initData();
		noticeMgr.initData();
		allianceFightMgr.initData();
		chongLouMgr.initData();
		lieFuMgr.initData();
		chenghaoMgr.init();
		bidMgr.init();
		growthFundMgr.init();
		levelUpGiftMgr.init();
		strengthGetMgr.init();
	}

	public void setInst() {
		inst = this;
	}

	public void route(AbstractMessage msg) {
		switch (msg.id) {
		}
	}

	public void route(int id, Builder builder, IoSession session) {
		try {
			boolean patch = PatchMgr.inst.route(id, builder, session);
			if(patch){
				return;
			}
			// log.info("客户端请求服务端，发送的协议号是{}",id);
			switch (id) {
			/* 百战千军的协议处理 */
			case PD.BAIZHAN_INFO_REQ:
			case PD.CHALLENGE_REQ:
			case PD.CONFIRM_EXECUTE_REQ:
			case PD.BAIZHAN_RESULT:
//			case PD.PLAYER_STATE_REQ:
			case PD.ZHANDOU_INIT_PVP_REQ:
//			case PD.C_ZHANDOU_INIT_YB_REQ:// 2015年6月11日处理押镖战斗数据初始化
				// 请求此刻之前的战斗记录列表
			case PD.ZHAN_DOU_RECORD_REQ:
				pvpMgr.addMission(id, session, builder);
				break;
			// 百战end
			case PD.C_XG_TOKEN:
				XG.inst.clientReportToken(id ,session, builder);
				break;
			case PD.GET_QQ_INFO:
				YYMgr.getInst().getQQ(id, session, builder);
				break;
			case PD.GET_LV_INFO:
				YYMgr.getInst().getLv(id, session, builder);
				break;
			case PD.GET_REQ_INFO:
				YYMgr.getInst().getFAQ(id, session, builder);
				break;
			case PD.GET_LV_REWARD:
				YYMgr.getInst().lingQu(id, session, builder);
				break;
			// 押镖协议处理
			case PD.C_YABIAO_INFO_REQ:
			case PD.C_YABIAO_ENEMY_RSQ:
			case PD.C_YABIAO_HISTORY_RSQ:
			case PD.C_YABIAO_MENU_REQ:
			case PD.C_YABIAO_REQ:
			case PD.C_SETHORSE_REQ:
//			case PD.C_JIEBIAO_INFO_REQ:
			case PD.C_BIAOCHE_INFO:
			case PD.C_YABIAO_BUY_RSQ:
			case PD.C_YABIAO_HELP_RSQ:
			case PD.C_ANSWER_YBHELP_RSQ:
			case PD.C_TICHU_YBHELP_RSQ:
			case PD.C_CHECK_YABIAOHELP_RSQ:
//			case PD.C_YABIAO_XIEZHU_TIMES_RSQ:
//			case PD.C_MOVE2BIAOCHE_REQ:
			case PD.C_GETMABIANTYPE_REQ:
			case PD.C_CARTJIASU_REQ:
			case PD.C_BUYHORSEBUFF_REQ:
			case PD.C_YABIAO_XIEZHUS_REQ:
			case PD.C_BUYXUEPING_REQ:
			case PD.C_YABIAO_MOREINFO_RSQ:
				ybMgr.addMission(id, session, builder);
				break;
			// 押镖处理结束
			//掠夺
			case PD.LVE_DUO_INFO_REQ:
			case PD.LVE_GO_LVE_DUO_REQ:
			case PD.ZHANDOU_INIT_LVE_DUO_REQ:
			case PD.LVE_BATTLE_RECORD_REQ:
			case PD.LVE_CONFIRM_REQ:
			case PD.LVE_BATTLE_END_REQ:
			case PD.LVE_NEXT_ITEM_REQ:
			case PD.LVE_HELP_REQ:
				lveDuoMgr.addMission(id, session, builder);
				break;
			// 掠夺end
			case PD.C_CHECK_CHARGE:
				YuanBaoMgr.inst.checkCharge(id,session,builder);
				break;
			case PD.mainSimpleInfoReq:
				FunctionOpenMgr.getFunctionInfo(session, builder);
				break;
			case PD.C_PVE_Reset_CQ:
				pveGuanQiaMgr.resetChuanQiTimes(id, builder, session);
				break;
			case PD.C_YuanJun_List_Req:
				pveGuanQiaMgr.sendYuanJunList(id, builder, session);
				break;
			case PD.C_EquipDetailReq:
				equipMgr.sendEquipDetail(id, session, builder);
				break;
			case PD.DEBUG_PROTO_WITHOUT_CONTENT:
				// 这个不要删，已经用作联网检查了。
				log.debug("receive test empty protocol");
				session.write(PD.DEBUG_PROTO_WITHOUT_CONTENT_RET);
				break;
			case PD.C_TEST_DELAY:
				if(builder==null){
					log.error("builder is null when test delay");
					break;
				}
				ProtobufMsg msg4delay = new ProtobufMsg();
				msg4delay.id = PD.S_TEST_DELAY;
				msg4delay.builder = builder;
				session.write(msg4delay);
				break;
			case PD.C_DROP_CONN:
				log.info("sid {} drop con.",session.getId());
				break;
			case PD.C_GET_CHAT_CONF:
			case PD.C_Send_Chat:
				ChatMgr.getInst().addMission(id, session, builder);
				break;
			case PD.C_Get_Chat_Log:
				ChatMgr.getInst().sendChatLog(id, builder, session);
				break;
			case PD.C_SETTING_CHAT:
				ChatMgr.getInst().setChatSettings(id, builder, session);
				break;
			case PD.C_CHAT_SETTING_REQ:
				ChatMgr.getInst().getChatSettings(id, builder, session);
				break;
			case PD.C_SHOW_WU_QI:
			case PD.Exit_Scene:
			case PD.Enter_Scene:
			case PD.Enter_HouseScene:
			case PD.Exit_HouseScene:
			case PD.Enter_Scene_Confirm:
			case PD.Spirite_Move:
			case PD.PLAYER_SOUND_REPORT:
			case PD.PLAYER_STATE_REPORT:
			case PD.C_APP_SLEEP:
			case PD.C_APP_WAKEUP:
			case PD.ENTER_FIGHT_SCENE:
			case PD.EXIT_FIGHT_SCENE:
			case PD.Enter_YBScene:
			case PD.Exit_YBScene:
			case PD.FIGHT_ATTACK_REQ:
				// scene.exec(id, session, builder);
				scMgr.route(id, session, builder);
				break;
			case PD.C_CITYWAR_OPERATE_REQ:
				BidMgr.inst.cityWarOperate(id,session,builder);
				break;
			case PD.C_ALLIANCE_CITYFIGHTINFO_REQ:
				BidMgr.inst.cityFightInfo(id,session,builder);
				break;
			case PD.C_CITYWAR_REWARD_REQ:
				BidMgr.inst.cityWarRewardInfo(id,session,builder);
				break;
			case PD.C_CITYWAR_GRAND_REQ:
				BidMgr.inst.cityWarGrandInfo(id,session,builder);
				break;
			case PD.C_CITYWAR_BID_REQ:
				BidMgr.inst.cityWarBidPageInfo(id,session,builder);
				break;
			case PD.C_CITYWAR_SCORE_RESULT_REQ:
				BidMgr.inst.getScoreResult(id,session,builder);
				break;
			case PD.SKILL_PREPARE:
			case PD.POS_JUMP:
			case PD.AOE_SKILL:
				scMgr.route(id, session, builder);
				break;
			case PD.LMZ_BUY_XueP:
			case PD.LMZ_BUY_ZhaoHuan:
			case PD.LMZ_ZhaoHuan:
			case PD.LMZ_CMD_LIST:
			case PD.LMZ_SCORE_LIST:
			case PD.LMZ_FuHuo:
			case PD.LMZ_CMD_ONE:
			case PD.LMZ_fenShen:
				scMgr.route(id, session, builder);
				break;
			case PD.C_ENTER_LMZ:
				scMgr.enterFight(id, session, builder);
				break;
			case PD.Enter_TBBXScene:{
				Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
				//客户端有bug，重复发送了这个协议，所以判断下
				//http://192.168.0.250:81/index.php/bug/42282
				if(scene instanceof BaoXiangScene){
					break;
				}
				BigSwitch.inst.scMgr.playerExitScene(session);
				//就是不要break
			}
			case PD.C_GET_BAO_XIANG:
				ExploreTreasureMgr.inst.scene.exec(id,session,builder);
				break;
			case PD.Exit_TBBXScene:
				ExploreTreasureMgr.inst.scene.exec(PD.Exit_Scene,session,builder);
				break;
			case PD.Battle_Pve_Init_Req:
				pveMgr.enQueueReq(id, session, builder);
				break;
			case PD.C_OPEN_CREATE_ROLE:
				accMgr.enterCreateRole(id,session,builder);
				break;
			case PD.ACC_LOGIN:
				accMgr.login0(id, session, builder);
				break;
			case PD.CREATE_ROLE_REQUEST:
				accMgr.createRole(id, session, builder);
				break;
			case PD.ROLE_NAME_REQUEST:
				accMgr.roleNameRequest0(id, session, builder);
				break;
			case PD.PVE_PAGE_REQ:
				pveGuanQiaMgr.getPageInfo(id, session, builder);
				break;
			case PD.PVE_GuanQia_Request:
				pveGuanQiaMgr.sendGuanQiaInfo(id, session, builder);
				break;
			case PD.C_BuZhen_Report:
				pveGuanQiaMgr.setBuZhen(id, session, builder);
				break;
			case PD.C_MIBAO_SELECT:
				pveGuanQiaMgr.setMibaoSelect(id, session, builder);
				break;
			case PD.C_GET_CUR_CHENG_HAO:
				ChenghaoMgr.inst.sendCur(id,session,builder);
				break;
			case PD.C_LIST_CHENG_HAO:
				ChenghaoMgr.inst.sendList(id,session,builder);
				break;
			case PD.C_USE_CHENG_HAO:
				ChenghaoMgr.inst.use(id,session,builder);
				break;
			case PD.DuiHuan_CHENGHAO:
				ChenghaoMgr.inst.duiHuan(id,session,builder);
				break;
			case PD.C_GET_UPACTION_DATA:
				GrowUpMgr.inst.getPageInfo(id,session,builder);
				break;
			case PD.C_InitProc:
				antiCheatMgr.initProc(id, session, builder);
				break;
			case PD.C_klwhy_1:
			case PD.C_klwhy_2:
			case PD.C_klwhy_3:
			case PD.C_klwhy_4:
			case PD.C_klwhy_5:
			case PD.C_klwhy_6:
			case PD.C_klwhy_7:
			case PD.C_klwhy_8:
			case PD.C_klwhy_9:
			case PD.C_klwhy_10:
				antiCheatMgr.stepProc(id, session, builder);
				break;
			case PD.C_zlgdlc:
				antiCheatMgr.stopProc(id, session, builder);
				break;
			case PD.BattlePveResult_Req:// 废弃，这里处理放在batteOver里
				// awardMgr.getAward(session, builder);
				break;
			case PD.has_get_zhangJie_award_req:
				pveGuanQiaMgr.hasGetPassZhangJieAward( session, builder);
				break;
			case PD.get_passZhangJie_award_req:
				pveGuanQiaMgr.getPassZhangJieAward(id, session, builder);
				break;
			case PD.PVE_BATTLE_OVER_REPORT:
				pveMgr.battleOver(id, session, builder);
				break;
			case PD.PVE_STAR_REWARD_INFO_REQ:
				pveGuanQiaMgr.queryStartRewards(id, session, builder);
				break;
			case PD.PVE_STAR_REWARD_GET:
				pveGuanQiaMgr.lingQuStartRewards(id, session, builder);
				break;
			case PD.C_Report_battle_replay:
				pveGuanQiaMgr.saveReplay(id, session, builder);
				break;
			case PD.C_Request_battle_replay:
				pveGuanQiaMgr.sendReplay(id, session, builder);
				break;
			case PD.C_get_sound:
				ChatMgr.getInst().getSound(id, session, builder);
				break;
			case PD.JunZhuAddPointReq:
				junZhuMgr.addPoint(id, session, builder);
				break;
			case PD.JunZhuAttPointReq:
				junZhuMgr.sendAttPoint(id, session, builder);
				break;
			case PD.JunZhuInfoReq:
				junZhuMgr.sendMainInfo(id, session, builder);
				break;
			case PD.JUNZHU_INFO_SPECIFY_REQ:
				junZhuMgr.requestSpecifyJunzhuInfo(id, session, builder);
				break;
			case PD.C_KeJiUp:
				keJiMgr.keJiShengJi(id, session, builder);
				break;
			case PD.C_KeJiInfo:
				keJiMgr.sendKeJiInfo(id, session, builder);
				break;

			case PD.C_EQUIP_UPGRADE:
				UserEquipAction.getInstance().doUpgradeEquip(id, session,
						builder);
				break;
				//一键强化
			case PD.C_EQUIP_UPALLGRADE:
				UserEquipAction.getInstance().doUpAllgradeEquips(id, session, builder);
				break;
			case PD.C_EQUIP_XiLian:
				UserEquipAction.getInstance().exec(id, session, builder);
				break;
			case PD.C_EQUIP_JINJIE:
				UserEquipAction.getInstance().newEquipJinJie(id, session, builder);
				break;
			case PD.C_EquipRemove:
				equipMgr.equipRemove(id, session, builder);
				break;
			case PD.C_EquipAdd:
				equipMgr.equipAdd(id, session, builder);
				break;
			case PD.C_GET_HighLight_item_ids:
				bagMgr.sendHighlightItemIdsInBag(session);
				break;
			case PD.C_BagInfo:
				bagMgr.sendBagInfo(id, session, builder);
				break;
			case PD.C_EquipInfo:
				bagMgr.sendEquipInfo(id, session, builder);
				break;
			case PD.C_EquipInfoOtherReq:
				bagMgr.sendEquipInfoOther(id, session, builder);
				break;
			case PD.C_YuJueHeChengRequest:
				bagMgr.yuJueHeCheng(id, session, builder);
				break;
			case PD.C_REQ_MAIL_LIST:
				mailMgr.requestMailList(id, session, builder);
				break;
			case PD.C_MAIL_GET_REWARD:
				mailMgr.getRewardRequest(id, session, builder);
				break;
			case PD.HERO_INFO_REQ:
			case PD.WUJIANG_LEVELUP_REQ:
				heroMgr.exec(id, session, builder);
				break;
			case PD.C_JingMai_info:
				jmMgr.sendInfo(session, builder);
				break;
			case PD.C_JingMai_up:
				jmMgr.xueWeiUp(session, builder);
				break;
			case PD.C_get_daily_award:
				dailyAwardMgr.sendAward(id, session, builder);
				break;
			case PD.C_get_daily_award_info:
				dailyAwardMgr.sendInfo(id, session, builder);
				break;
			case PD.C_PVE_SAO_DANG:
				pveGuanQiaMgr.saoDang(id, session, builder);
				break;
			case PD.GET_UNION_INFO_REQ:
			case PD.GET_UNION_FRIEND_INFO_REQ:
			case PD.UNION_EDIT_REQ:
			case PD.UNION_INNER_EDIT_REQ:
			case PD.UNION_OUTER_EDIT_REQ:
			case PD.CREATE_UNION_REQ:
			case PD.UNION_LEVELUP_REQ:
			case PD.UNION_APPLY_REQ:
			case PD.UNION_INVITE_REQ:
			case PD.UNION_INVITED_AGREE_REQ:
			case PD.UNION_INVITED_REFUSE_REQ:
			case PD.UNION_QUIT_REQ:
			case PD.UNION_DISMISS_REQ:
			case PD.UNION_TRANSFER_REQ:
			case PD.UNION_ADVANCE_REQ:
			case PD.UNION_DEMOTION_REQ:
			case PD.UNION_REMOVE_REQ:
			case PD.UNION_APPLY_JION_REQ:
			case PD.UNION_DETAIL_INFO_REQ:
				break;
			case PD.WUJIANG_TECHINFO_REQ:
			case PD.WUJIANG_TECHLEVELUP_REQ:
				wjKeJiMgr.exec(id, session, builder);
				break;
			case PD.BUY_CARDBAG_REQ:
			case PD.OPEN_CARDBAG_REQ:
				cardMgr.enqueue(id, session, builder);
				break;
			case PD.C_BUY_TIMES_REQ:
				pMgr.sendInfo(id, session, builder);
				break;
			case PD.C_BUY_TiLi:
				pMgr.buyTiLi(id, session, builder);
				break;
			case PD.C_BUY_TongBi_Data:
				pMgr.sendTongBiData(id, session, builder);
				break;
			case PD.C_BUY_TongBi:
				pMgr.buyTongBi(id, session, builder);
				break;
			case PD.C_BUY_TongBi_LiXu:
				pMgr.buyTongBiLianXu(id, session, builder);
				break;
			case PD.C_BUY_MIBAO_POINT:
				pMgr.buyMibaoPoint(id, session, builder);
				break;
			case PD.C_TIMEWORKER_INTERVAL:
				TimeWorkerMgr.instance.exec(id, session, builder);
				break;
			case PD.HERO_ACTIVE_REQ:
				HeroMgr.inst.activateHero(id, session, builder);
				break;
			case PD.JINGPO_REFRESH_REQ:
				// HeroMgr.inst.convertGoldenJingPo(id, session);
				break;
			case PD.WUJIANG_TECH_SPEEDUP_REQ:
				WuJiangKeJiMgr.inst.wuJiangTechSpeedUpCold(id, session);
				break;
			case PD.ZHANDOU_INIT_PVE_REQ:
				pveMgr.PVEDataInfoRequest(id, session, builder);
				break;
			case PD.C_BuZhen_Hero_Req:
				heroMgr.sendBuZhenHeroList(id, session, builder);
				break;
			case PD.C_YOUXIA_INIT_REQ:
				youXiaMgr.battleInitRequest(id, session, builder);
				break;
			case PD.C_YOUXIA_BATTLE_OVER_REQ:
				youXiaMgr.battleOverReport(id, session, builder);
				break;
			case PD.C_YOUXIA_GUANQIA_REQ:
				youXiaMgr.requestGuanQiaInfo(id, session, builder);
				break;
			case PD.C_YOUXIA_TYPE_INFO_REQ:
				youXiaMgr.requestTypePassInfo(id, session, builder);
				break;
			case PD.C_YOUXIA_SAO_DANG_REQ:
				youXiaMgr.saoDang(id, session, builder);
				break;
			case PD.C_ACHE_LIST_REQ:
				AchievementMgr.instance.acheListRequest(id, session);
				break;
			case PD.C_ACHE_GET_REWARD_REQ:
				AchievementMgr.instance.getAcheReward(id, session, builder);
				break;
			case PD.C_DAILY_TASK_LIST_REQ:
				DailyTaskMgr.INSTANCE.taskListRequest(id, session);
				break;
			case PD.C_DAILY_TASK_GET_REWARD_REQ:
				DailyTaskMgr.INSTANCE.getTaskReward(id, session, builder);
				break;
			case PD.dailyTask_get_huoYue_award_req: // 获取每日任务活跃度奖励
				DailyTaskMgr.INSTANCE.getHuoYueDuAward(id, session, builder);
				break;
//			case PD.BUY_TREASURE_INFOS_REQ:
//				PurchaseMgr.inst.sendTreasureInfos(id, session, builder);
//				break;
//			case PD.BUY_TREASURE:
//				PurchaseMgr.inst.buyTreasure(id, session, builder);
//				break;
			case PD.C_TaskReq:
				gameTaskMgr.sendTaskList(id, session, builder);
				break;
			case PD.C_GetTaskReward:
				gameTaskMgr.getReward(id, session, builder);
				break;
			case PD.C_TaskProgress:
				gameTaskMgr.clientUpdateProgress(id, session, builder);
				break;
//				// 秘宝攒齐星星，宝箱领奖
//			case PD.GET_FULL_STAR_AWARD_REQ:
//				mibaoMgr.getAwardWhenFullStar(session, id);
//				break;
			case PD.C_CLOSE_TAN_BAO_UI:{
				List<String> broadList = (List<String>) session.getAttribute("MiBaoBDCache");
				if(broadList != null){
					for( String template : broadList ){
						if(template != null){
							BroadcastMgr.inst.send(template);
						}
					}
					broadList.removeAll(broadList);
				}
			}
				break;
			case PD.C_MIBAO_ACTIVATE_REQ:
				mibaoMgr.mibaoActivate(id, session, builder);
				break;
			case PD.C_MIBAO_INFO_REQ:
				mibaoMgr.mibaoInfosRequest(id, session);
				break;
			case PD.C_MIBAO_INFO_OTHER_REQ:
				mibaoMgr.mibaoInfosOtherRequest(id, session, builder);
				break;
			case PD.C_MIBAO_STARUP_REQ:
				mibaoMgr.starUpgrade(id, session, builder);
				break;
			case PD.C_MIBAO_LEVELUP_REQ:
				mibaoMgr.levelUpgrade(id, session, builder);
				break;
			case PD.MIBAO_DEAL_SKILL_REQ:
				mibaoMgr.doMiBaoDealSkillReq(session, builder);
				break;
			case PD.EXPLORE_INFO_REQ:
				exploreMgr.sendExploreMineInfo(id, session, builder);
				break;
			case PD.EXPLORE_REQ:
				exploreMgr.toExplore(id, session, builder);
				break;
//			case PD.PAWN_SHOP_GOODS_LIST_REQ:
//				pawnshopMgr.getGoodsList(id, session);
//				break;
//			case PD.PAWN_SHOP_GOODS_BUY:
//				pawnshopMgr.buyGoods(id, session, builder);
//				break;
			case PD.PAWN_SHOP_GOODS_SELL:
				shopMgr.sellGoods(id, session, builder);
				break;
//			case PD.PAWN_SHOP_GOODS_REFRESH:
//				pawnshopMgr.refreshPawnshop(id, session, builder);
//				break;
			case PD.C_JIAN_ZHU_INFO:
				JianZhuMgr.inst.getInfo(id,session,builder);
				break;
			case PD.C_JIAN_ZHU_UP:
				JianZhuMgr.inst.up(id,session,builder);
				break;
			case PD.C_LMKJ_UP:
				JianZhuMgr.inst.upLMKJ(id,session,builder);
				break;
			case PD.C_LMKJ_INFO:
				JianZhuMgr.inst.sendLMKJInfo(id,session,builder);
				break;
			case PD.C_LM_CHOU_JIANG_1:
				JianZhuMgr.inst.jiBai(id,session,builder);
				break;
			case PD.C_LM_CHOU_JIANG_N:
				JianZhuMgr.inst.chouJiang_N(id,session,builder);
				break;
			case PD.C_LM_CHOU_JIANG_INFO:
				JianZhuMgr.inst.sendChouJiangInfo(id,session,builder);
				break;
			case PD.C_GET_MOBAI_AWARD:
				moBaiMgr.getStepAward(id,session,builder);
				break;
			case PD.C_MoBai:
				moBaiMgr.moBai(id, session, builder);
				break;
			case PD.C_MoBai_Info:
				moBaiMgr.sendMoBaiInfo(id, session, builder);
				break;
			case PD.ALLIANCE_INFO_REQ:
				allianceMgr.requestAllianceInfo(id, session, builder);
				break;
			case PD.CHECK_ALLIANCE_NAME:
				allianceMgr.checkAllianceName(id, session, builder);
				break;
			case PD.CREATE_ALLIANCE:
				allianceMgr.createAlliance(id, session, builder);
				break;
			case PD.FIND_ALLIANCE:
				allianceMgr.findAlliance(id, session, builder);
				break;
			case PD.APPLY_ALLIANCE:
				allianceMgr.applyAlliance(id, session, builder);
				break;
			case PD.CANCEL_JOIN_ALLIANCE:
				allianceMgr.cancelJoinAlliance(id, session, builder);
				break;
			case PD.EXIT_ALLIANCE:
				allianceMgr.exitAlliance(id, session, builder);
				break;
			case PD.LOOK_MEMBERS:
				allianceMgr.lookMembers(id, session, builder);
				break;
			case PD.FIRE_MEMBER:
				allianceMgr.fireMember(id, session, builder);
				break;
			case PD.UP_TITLE:
				allianceMgr.upTitle(id, session, builder);
				break;
			case PD.DOWN_TITLE:
				allianceMgr.downTitle(id, session, builder);
				break;
			case PD.LOOK_APPLICANTS:
				allianceMgr.lookApplicants(id, session, builder);
				break;
			case PD.REFUSE_APPLY:
				allianceMgr.refuseApply(id, session, builder);
				break;
			case PD.AGREE_APPLY:
				allianceMgr.agreeApply(id, session, builder);
				break;
			case PD.UPDATE_NOTICE:
				allianceMgr.updateNotice(id, session, builder);
				break;
			case PD.DISMISS_ALLIANCE:
				allianceMgr.dismissAlliance(id, session, builder);
				break;
			case PD.OPEN_APPLY:
				allianceMgr.openApply(id, session, builder);
				break;
			case PD.CLOSE_APPLY:
				allianceMgr.closeApply(id, session, builder);
				break;
			case PD.TRANSFER_ALLIANCE:
				allianceMgr.transferAlliance(id, session, builder);
				break;
			case PD.MENGZHU_APPLY:
				allianceVoteMgr.mengzhuApply(id, session);
				break;
			case PD.MENGZHU_VOTE:
				allianceVoteMgr.mengzhuVote(id, session, builder);
				break;
			case PD.GIVEUP_VOTE:
				allianceVoteMgr.giveUpVote(id, session);
				break;
			case PD.C_ALLIANCE_UPGRADE_LEVEL:
				allianceMgr.upgradeLevel(id, session, builder);
				break;
			case PD.C_ALLIANCE_UPGRADELEVEL_SPEEDUP:
				allianceMgr.upgradeLevelSpeedUp(id, session, builder);
				break;
			case PD.C_ALLIANCE_UPINFO_SPEEDUP:
				allianceMgr.upgradeLevelInfoRequest(id, session, builder);
				break;
			case PD.ALLIANCE_HUFU_DONATE:
				allianceMgr.donateHufu(id, session, builder);
				break;
			case PD.IMMEDIATELY_JOIN:
				allianceMgr.immidiatelyJoin(id, session, builder);
				break;
			case PD.C_ALLIANCE_FENGSHAN_REQ:
				juanXianMgr.getJuanXianInfo(id, session, builder);
				break;
			case PD.C_DO_ALLIANCE_FENGSHAN_REQ:
				juanXianMgr.doJuanXian(id, session, builder);
				break;
			case PD.C_JOIN_BLACKLIST:
				ChatMgr.inst.joinBlacklist(id, session, builder, true);
				break;
			case PD.C_GET_BALCKLIST:
				ChatMgr.inst.getBlackList(id, session);
				break;
			case PD.C_GET_RECENT_CONTACTS:
				ChatMgr.inst.getRecentContacts(id, session, builder);
				break;
			case PD.C_CANCEL_BALCK:
				ChatMgr.inst.cancelBlack(id, session, builder);
				break;
			case PD.C_SETTINGS_GET:
				settingsMgr.get(id, session, builder);
				break;
			case PD.C_SETTINGS_SAVE:
				settingsMgr.save(id, session, builder);
				break;
			case PD.C_LM_CHANGE_COUNTRY:
				allianceMgr.changeCountry(id, session, builder);
				break;
			case PD.C_change_name:
				settingsMgr.changeName(id, session, builder);
				break;
			case PD.C_ZHUANGGUO_REQ:// 转国家
				settingsMgr.changeGuojia(id, session, builder);
				break;
			case PD.C_LM_HOUSE_INFO:
			case PD.C_Set_House_state:
			case PD.C_HOUSE_EXCHANGE_RQUEST:
			case PD.C_EHOUSE_EXCHANGE_RQUEST:
			case PD.C_HOUSE_APPLY_LIST:
			case PD.C_AnswerExchange:
			case PD.C_CANCEL_EXCHANGE:
			case PD.C_EnterOrExitHouse:
			case PD.C_ShotOffVisitor:
			case PD.C_GET_BIGHOUSE_EXP:
			case PD.C_GetHouseVInfo:
			case PD.C_get_house_exp:
			case PD.C_get_house_info:
			case PD.C_huan_wu_info:
			case PD.C_huan_wu_Oper:
			case PD.C_huan_wu_list:
			case PD.C_huan_wu_exchange:
			case PD.C_ExCanJuanJiangLi:
			case PD.C_up_house:
			case PD.C_Pai_big_house:
				houseMgr.addMission(id, session, builder);
				break;
			case PD.C_SEND_EAMIL:
				mailMgr.sendEmail(id, session, builder);
				break;
			case PD.C_READED_EAMIL:
				mailMgr.markReadedEmail(id, session, builder);
				break;
			case PD.C_EMAIL_RESPONSE:
				mailMgr.emailResponse(id, session, builder);
				break;
			case PD.C_OPEN_HUANGYE:
				hyMgr.openHuangYe(id, session, builder);
				break;
//			case PD.C_OPEN_FOG:
//				hyMgr.openFog(id, session, builder);
//				break;
			case PD.C_OPEN_TREASURE:
				hyMgr.openTreasurePoint(id, session, builder);
				break;
//			case PD.C_REQ_REWARD_STORE:
//				hyMgr.reqRewardStore(id, session, builder);
//				break;
//			case PD.C_APPLY_REWARD:
//				hyMgr.applyReward(id, session, builder);
//				break;
//			case PD.C_CANCEL_APPLY_REWARD:
//				hyMgr.cancelApplyReward(id, session, builder);
//				break;
//			case PD.C_GIVE_REWARD:
//				hyMgr.giveReward(id, session, builder);
//				break;
			case PD.C_HUANGYE_PVE:
				hyMgr.pveDataInfoReq(id, session, builder);
				break;
			case PD.C_HUANGYE_PVE_OVER:
				hyMgr.pveOverProcess(id, session, builder);
				break;
//			case PD.C_HUANGYE_PVP_OVER:
//				hyMgr.pvpOverProcess(id, session, builder);
//				break;
			case PD.C_HYTREASURE_BATTLE:
				hyMgr.battleTreasureReq(id, session, builder);
				break;
//			case PD.C_HYRESOURCE_BATTLE:
//				hyMgr.battleResouceReq(id, session, builder);
//				break;
//			case PD.C_HUANGYE_PVP:
//				hyMgr.pvpDataInfoReq(id, session, builder);
//				break;
//			case PD.C_HYRESOURCE_CHANGE:
//				hyMgr.resourceChangeReq(id, session, builder);
//				break;
			case PD.HY_SHOP_REQ:
				shopMgr.dealGetShopInfoReq(id, builder, session);
				break;
			case PD.HY_BUY_GOOD_REQ:
				shopMgr.dealBuyGoodReq(id, builder, session);
				break;
			case PD.ACTIVE_TREASURE_REQ:
				hyMgr.activeTreasurePoint(id, session, builder);
				break;
			case PD.MAX_DAMAGE_RANK_REQ:
				hyMgr.getMaxDamageRank(id, session, builder);
				break;
			case PD.HY_BUY_BATTLE_TIMES_REQ:
				hyMgr.dealHyBuyBattleTimesReq(id, builder, session);
				break;
			case PD.C_WUBEIFANG_INFO:
				ShopMgr.inst.requestWuBeiFangInfo(id, builder, session);
				break;
			case PD.C_WUBEIFANG_BUY:
				ShopMgr.inst.buyWuBeiFang(id, builder, session);
				break;
			// end 荒野
			case PD.RANKING_REP:
				rankMgr.getRank(id, session, builder);
				break;
			case PD.RANKING_ALLIANCE_PLAYER_REQ:
				rankMgr.getRankAlliancePlayer(id, session, builder);
				break;
			case PD.GET_RANK_REQ:
				rankMgr.getRankById(id,session,builder);
				break;
			case PD.C_VIPINFO_REQ:
				vipMgr.getVipInfo(id, session, builder);
				break;
			case PD.C_VIP_GET_GIFTBAG_REQ:
				vipMgr.getVipGiftbag(id, session, builder);
				break;
			case PD.C_RECHARGE_REQ:
				vipMgr.recharge(id, session, builder);
				break;
//			case PD.C_PVE_MIBAO_ZHANLI:
//				junZhuMgr.getPVEMiBaoZhanLi(session);
//				break;
			case PD.C_FRIEND_ADD_REQ:
				friendMgr.addFriend(id, session, builder);
				break;
			case PD.C_FRIEND_REMOVE_REQ:
				friendMgr.removeFriend(id, session, builder);
				break;
			case PD.C_GET_FRIEND_IDS:
				friendMgr.getFriendIds(id,session,builder);
				break;
			case PD.C_FRIEND_REQ:
				friendMgr.getFriendList(id, session, builder);
				break;
			case PD.C_GET_QIANDAO_REQ:
				qiandaoMgr.getQiandao(id, session, builder);
				break;
			case PD.C_QIANDAO_REQ:
				qiandaoMgr.qiandao(id, session, builder);
				break;
			case PD.C_GET_QIANDAO_DOUBLE_REQ:
				qiandaoMgr.getVipDouble(id, session, builder);
				break;
			case PD.qianDao_get_vip_present_req:
				qiandaoMgr.getVipPresent(session, builder);
				break;
			/*
			 * 套装
			 */
			case PD.tao_zhuang_Req:
				JunZhuMgr.inst.getTaoZhuangInfo(id, session);
				break;
			case PD.activate_tao_zhuang_req:
				JunZhuMgr.inst.activitedTaoZhuang(id, session, builder);
				break;
			/*
			 * 天赋
			 */
			case PD.TALENT_INFO_REQ:
				talentMgr.sendTalentInfo(session);
				break;
			case PD.TALENT_UP_LEVEL_REQ:
				talentMgr.doTalentUpLevel(session, builder);
				break;
			case PD.ACTIVITY_FUNCTIONLIST_INFO_REQ:
				activityMgr.getActivityList(id, session);
				break;
			case PD.ACTIVITY_FIRST_CHARGE_REWARD_REQ:
				shouchongMgr.getShouchong(id, session, builder);
				break;
			case PD.ACTIVITY_FIRST_CHARGE_GETREWARD_REQ:
				shouchongMgr.shouchongAward(id, session, builder);
				break;
			case PD.C_YOUXIA_INFO_REQ:
				youXiaMgr.youxiaInfoRequest(id, session, builder);
				break;
			case PD.C_YOUXIA_TIMES_INFO_REQ:
				youXiaMgr.timesInfoRequest(id, session, builder);
				break;
			case PD.C_YOUXIA_TIMES_BUY_REQ:
				youXiaMgr.buyTimes(id, session, builder);
				break;
			// 限时活动
			case PD.C_XINSHOU_XIANSHI_INFO_REQ:
				xsActivityMgr.getXinShouXianShiInfo(id, builder, session);
				break;
			case PD.C_XINSHOU_XIANSHI_AWARD_REQ:
				xsActivityMgr.gainXinShouXianShiAward(id, builder, session);
				break;
			case PD.C_XIANSHI_INFO_REQ:
				xsActivityMgr.getOtherXianShiInfo(id, builder, session);
				break;
			case PD.C_XIANSHI_AWARD_REQ:
				xsActivityMgr.getOtherXianShiAward(id, builder, session);
				break;
			case PD.C_XIANSHI_REQ:
				xsActivityMgr.getOpenXianShiHuoDong(id, builder, session);
				break;
			case PD.C_FULIINFO_REQ:
				xsActivityMgr.getFuLiInfo(id, builder, session);
				break;
			case PD.C_HONGBAONFO_REQ:
				xsActivityMgr.getGanEnHongBao(id, builder, session);
				break;
			case PD.C_FULIINFOAWARD_REQ:
				xsActivityMgr.gainFuLiAward(id, builder, session);
				break;
			case PD.C_ACTIVITY_ACHIEVEMENT_INFO_REQ:
				xsActivityMgr.getAchievementList(id,session,builder);
				break;
			case PD.C_ACTIVITY_ACHIEVEMENT_GET_REQ:
				xsActivityMgr.getAchievementAward(id,session,builder);
				break;
			case PD.C_GET_VERSION_NOTICE_REQ:
				noticeMgr.getVersionNotice(id, session, builder);
				break;
			case PD.PVE_MAX_ID_REQ:
				pveGuanQiaMgr.getPveMaxId(id, session, builder);
				break;
			/**符石**/
			case PD.C_QUERY_FUWEN_REQ:
				fuwenMgr.queryFuwen(id, session, builder);
				break;
			case PD.C_OPERATE_FUWEN_REQ:
				fuwenMgr.operateFuwen(id, session, builder);
				break;
			case PD.C_LOAD_FUWEN_IN_BAG:
				fuwenMgr.loadFuwenInBag(id, session, builder);
				break;
			case PD.C_FUWEN_RONG_HE:
				fuwenMgr.rongHeFuwen(id, session, builder);
				break;
			case PD.C_FUWEN_DUI_HUAN:
				fuwenMgr.duiHuanFuwen(id, session, builder);
				break;
			case PD.C_FUWEN_EQUIP_ALL:
				fuwenMgr.equipFuwenAll(id, session, builder);
				break;
			case PD.C_FUWEN_UNLOAD_ALL:
				fuwenMgr.unloadFuwenAll(id, session, builder);
				break;
			/**国家上缴和领奖*/
			case PD.C_GET_JUANXIAN_GONGJIN_REQ://请求捐献贡金
			case PD.C_GET_JUANXIAN_DAYAWARD_REQ://请求捐献贡金 日奖励
			case PD.GUO_JIA_MAIN_INFO_REQ://请求国家主页
//			case PD.C_ISCAN_JUANXIAN_REQ://请求判断贡金是否可以上缴 2015年9月17日废弃
				gjMgr.addMission(id, session, builder);
				break;
			case PD.ALLINACE_EVENT_REQ:
				allianceMgr.eventListRequest(id, session, builder);
				break;
			case PD.ALLIANCE_FIGHT_INFO_REQ:
				allianceFightMgr.requestFightInfo(session);
				break;
			case PD.ALLIANCE_FIGHT_APPLY:
				allianceFightMgr.applyFight(session);
				break;
				/* 挪到场景里去处理
			case PD.FIGHT_ATTACK_REQ:
				fightMgr.activeFight(id, session, builder);
				break;
				*/
			case PD.ALLIANCE_BATTLE_FIELD_REQ:
				allianceFightMgr.requestBattlefieldInfo(session);
				break;
			case PD.ALLIANCE_FIGHT_HISTORY_REQ:
				allianceFightMgr.requestFightHistory(session);
				break;
			case PD.ALLIANCE_FIGTH_LASTTIME_RANK:
				allianceFightMgr.requestFightLasttimeRank(session);
				break;
			case PD.C_GET_CDKETY_AWARD_REQ:
				cdKeyMgr.getCDKeyAward(id, session, builder); 
				break;
			case PD.C_GET_JINENG_PEIYANG_QUALITY_REQ:
				jiNengPeiYangMgr.getJiNengPeiYangQuality(id, session, builder);
				break;
			case PD.C_UPGRADE_JINENG_REQ:
				jiNengPeiYangMgr.upgradeJiNeng(id, session, builder);
				break;
			case PD.PLAYER_REVIVE_REQUEST:
				fightMgr.reviveRequest(id, session, builder);
				break;
			case PD.C_MengYouKuaiBao_Req:
			case PD.Prompt_Action_Req:
			case PD.alliance_junQing_req: //联盟军情消息请求
			case PD.qu_zhu_battle_end_req: //联盟军情之 （掠夺）驱逐
			case PD.go_qu_zhu_req:
			case PD.qu_zhu_req:
				ptmgr.addMission(id, session, builder);
				break;
			case PD.C_BUY_REVIVE_TIMES_REQ:
				YaBiaoHuoDongMgr.inst.buyReviveAllLifeTimes(id, session, builder);
				break;
			case PD.C_NOT_GET_AWART_ZHANGJIE_REQ:
				pveGuanQiaMgr.notGetAwardZhangJieRequest(id, session, builder);
				break;
			case PD.C_ALLIANCE_TARGET_INFO:
				allianceMgr.requestAllianceTargetInfo(id, session, builder);
				break;
			case PD.C_GET_ALLIANCEL_LEVEL_AWARD:
				allianceMgr.getAllianceLevelAward(id, session, builder);
				break;
			case PD.C_LMKEJI_JIHUO:
				JianZhuMgr.inst.jiHuoLMKJ(id, session, builder);
				break;
			case PD.C_ALLIANCE_INVITE:
				allianceMgr.inviteJoinAlliance(id, session, builder);
				break;
			case PD.C_ALLIANCE_INVITE_LIST:
				allianceMgr.seeInviteList(id, session, builder);
				break;
			case PD.C_ALLIANCE_INVITE_REFUSE:
				allianceMgr.refuseInvite(id, session, builder);
				break;
			case PD.C_ALLIANCE_INVITE_AGREE:
				allianceMgr.agreeInvite(id, session, builder);
				break;
			case PD.C_GREET_REQ://向某人打招呼请求
				greetMgr.GreetAndAddFriend(id, session, builder);
				break;
			case PD.C_CHOOSE_SCENE://登陆前选择主城副本
				scMgr.choseScene(id, session, builder);
				break;
			case PD.C_SCENE_GETALL://登陆前选择主城副本
				scMgr.getAllScene(id, session, builder);
				break;
			case PD.MODEL_INFO:
				settingsMgr.getModelInfo(id, session, builder);
				break;
			case PD.CHANGE_MODEL:
				settingsMgr.changeModel(id,session,builder);
				break;
			case PD.UNLOCK_MODEL:
				settingsMgr.unlockModel(id,session,builder);
				break;
			case PD.CHONG_LOU_INFO_REQ:
				chongLouMgr.mainInfoRequest(id,session,builder);
				break;
			case PD.CHONG_LOU_SAO_DANG_REQ:
				chongLouMgr.saoDang(id,session,builder);
				break;
			case PD.CHONG_LOU_BATTLE_INIT:
				chongLouMgr.enterBattle(id,session,builder);
				break;
			case PD.CHONG_LOU_BATTLE_REPORT:
				chongLouMgr.battleResultReport(id,session,builder);
				break;
			case PD.LieFu_Action_Info_Req:
				lieFuMgr.actionInfoRequest(id,session,builder);
				break;
			case PD.LieFu_Action_req:
				lieFuMgr.doAction(id,session,builder);
				break;
			case PD.C_EQUIP_BAOSHI_REQ:
				jewelMgr.handle(id, session, builder);
				break;
			case PD.NEW_MIBAO_JIHUO:
				MiBaoV2Mgr.inst.jiHuo(id,session,builder);
				break;
			case PD.NEW_MIBAO_INFO:
				MiBaoV2Mgr.inst.sendMainInfo(id,session,builder);
				break;
			case PD.NEW_MISHU_JIHUO:
				MiBaoV2Mgr.inst.miShuJiHuo(id,session,builder);
				break;
			case PD.S_SEND_MIBAO_INFO:
				MiBaoV2Mgr.inst.sendMiBaoInfo(id,session,builder);
				break;
			case PD.ACTIVITY_MONTH_CARD_INFO_REQ:
				MonthCardMgr.inst.monthCardInfo(id,session,builder);
				break;
			case PD.ACTIVITY_MONTH_CARD_REWARD_REQ:
				MonthCardMgr.inst.monthCardGetReward(id,session,builder);
				break;
			case PD.ACTIVITY_GROWTHFUND_BUY_REQ:
				GrowthFundMgr.inst.buyGrowthFund(id,session,builder);
				break;
			case PD.ACTIVITY_GROWTHFUND_GETREWARD_REQ:
				GrowthFundMgr.inst.getGrowthFundReward(id,session,builder);
				break;
			case PD.ACTIVITY_GROWTHFUND_INFO_REQ:
				GrowthFundMgr.inst.getGrowthFundInfo(id,session,builder);
				break;
			case PD.ACTIVITY_STRENGTH_INFO_REQ:
				StrengthGetMgr.inst.strengthGetInfo(id,session,builder);
				break;
			case PD.ACTIVITY_STRENGTH_GET_REQ:
				StrengthGetMgr.inst.strengthGetReward(id,session,builder);
				break;
			case PD.ACTIVITY_LEVEL_GET_REQ:
				LevelUpGiftMgr.inst.levelUpGiftReward(id,session,builder);
				break;
			case PD.ACTIVITY_LEVEL_INFO_REQ:
				LevelUpGiftMgr.inst.getLevelUpGiftInfo(id,session,builder);
				break;
			case PD.C_YOUXIA_CLEAR_COOLTIME:
				youXiaMgr.clearCooltime(id,session,builder);
				break;
			default:
				log.error("未处理的协议 {} {}", id, builder);
				break;
			}
		} catch (Exception e) {
			log.error("error", e);
			ErrorMessage.Builder errorMsg = ErrorMessage.newBuilder();
			errorMsg.setCmd(id);
			if (e instanceof BaseException) {
				BaseException be = (BaseException) e;
				errorMsg.setErrorCode(be.getErrCode());
				errorMsg.setErrorDesc(be.getErrMsg());
			} else {
				errorMsg.setErrorCode(-10001);
				errorMsg.setErrorDesc(e.toString());
			}
			session.write(errorMsg.build());
		}

	}

}
