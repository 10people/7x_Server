package com.qx.task;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.ActLog;
import log.OurLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.GameTask.GetTaskReward;
import qxmobile.protobuf.GameTask.GetTaskRwardResult;
import qxmobile.protobuf.GameTask.TaskInfo;
import qxmobile.protobuf.GameTask.TaskList;
import qxmobile.protobuf.GameTask.TaskProgress;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.Fuwen;
import com.manu.dynasty.template.JiNengPeiYang;
import com.manu.dynasty.template.TaoZhuang;
import com.manu.dynasty.template.XianshiHuodong;
import com.manu.dynasty.template.ZhuXian;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.account.FunctionOpenMgr;
import com.qx.activity.QianDaoPresent;
import com.qx.activity.QiandaoInfo;
import com.qx.activity.XianShiActivityMgr;
import com.qx.activity.XianShiConstont;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliance.HouseBean;
import com.qx.alliance.MoBaiBean;
import com.qx.alliance.building.ChouJiangBean;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.equip.domain.UserEquip;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.explore.ExploreMgr;
import com.qx.explore.ExploreMine;
import com.qx.explore.TanBaoData;
import com.qx.fuwen.FuwenMgr;
import com.qx.huangye.HYTreasureTimes;
import com.qx.huangye.shop.PublicShop;
import com.qx.huangye.shop.ShopMgr;
import com.qx.jinengpeiyang.JNBean;
import com.qx.jinengpeiyang.JiNengPeiYangMgr;
import com.qx.junzhu.AcitvitedTaoZhuang;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.TalentPoint;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MiBaoSkillDB;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.TiLi;
import com.qx.purchase.TongBi;
import com.qx.pve.PveRecord;
import com.qx.pve.SaoDangBean;
import com.qx.pvp.LveDuoBean;
import com.qx.pvp.PvpBean;
import com.qx.pvp.PvpMgr;
import com.qx.yabiao.YunBiaoHistory;
import com.qx.youxia.YouXiaBean;

/**
 * 任务进度，负数表示已完成，但未领奖；已领奖的任务客户端看不到。
 * 
 * 任务管理器
 * @author 康建虎
 *
 */
public class GameTaskMgr extends EventProc{
	public static int spaceFactor = 100;
	public static GameTaskMgr inst;
	public static Logger log = LoggerFactory.getLogger(GameTaskMgr.class);
	public Map<Integer, ZhuXian> zhuxianTaskMap;
	
	public static final int after_award_trigger_type = 100;
	public static final int after_renwu_trigger_type = 0;
	public static final int zhuXianType = 0;
	public static int maxZhuRenWuOrderIdx = 1;
	
	public static int first_task_id = 100000;
	/*
	 * 普通关卡的2-5关卡id
	 */
	public static final int guanQia_2_5 = 100205;
	public GameTaskMgr(){
		inst = this;
		initData();
	}
	
	public void initData() {
		List<ZhuXian> list = TempletService.listAll(ZhuXian.class.getSimpleName());
		Map<Integer, ZhuXian> zhuxianTaskMap = new HashMap<Integer, ZhuXian>();
		if(list == null || list.size() == 0){
			log.error("主线任务数据加载错误");
			return;
		}
		int orderIdx = 0;
		for(ZhuXian zhuXian : list){
			zhuXian.orderIdx = orderIdx;
			orderIdx ++;
			zhuxianTaskMap.put(zhuXian.getId(), zhuXian);
			maxZhuRenWuOrderIdx = Math.max(maxZhuRenWuOrderIdx, zhuXian.orderIdx);
		}
		if(zhuxianTaskMap.size() != list.size()){
			log.error("主线任务配置文件id有重复");
			return;
		}
		this.zhuxianTaskMap = zhuxianTaskMap;
	}

	/**
	 * 获取指定君主的指定任务。
	 * @param pid
	 * @param tid
	 * @return
	 */
	public WorkTaskBean getTask(long pid, int tid){
		long start = pid * spaceFactor;
		long end = start + spaceFactor;
		List<WorkTaskBean> list = HibernateUtil.list(WorkTaskBean.class, 
				"where dbId>="+start+" and dbId<"+end
				+" and tid="+tid);
		if(list.size()>1){
			log.error("{}有两个以上的任务 {}", pid, tid);
			return list.get(0);
		}else if(list.size()==0){
			return null;
		}else{
			return list.get(0);
		}
	}
	public List<WorkTaskBean> getTaskList(long pid){
		long start = pid * spaceFactor;
		long end = start + spaceFactor;
		List<WorkTaskBean> list = HibernateUtil.list(WorkTaskBean.class, "where dbId>="+start+" and dbId<"+end);
		return list;
	}

	public void sendTaskList(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId==null){
			log.warn("junZhuId not found.");
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			log.error("junzhu not found");
			return;
		}
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if(list.size()==0 && jz.level<=1){
			WorkTaskBean newT = addTask(junZhuId, first_task_id, null);
			list.add(newT);
		}
		/*
		 *  之前的配置任务都已完成
		 *  根据策划向后新添加的任务配置，接着做任务
		 */
		WorkTaskBean onceLast = null;
		for(WorkTaskBean b: list){
			if(b.progress == -2){
				onceLast = b;
				break;
			}
		}
		if(onceLast != null && list.size() == 1){
			fireNextTrigger100Task(junZhuId, onceLast.tid);
			list = getTaskList(junZhuId);
		}
		if(onceLast != null){
			list.remove(onceLast);
		}
		TaskList.Builder  ret = TaskList.newBuilder();
		for(WorkTaskBean b : list){
			TaskInfo.Builder t = TaskInfo.newBuilder();
			t.setId(b.tid);
			t.setProgress(b.progress);
			ret.addList(t);
		}
		session.write(ret.build());
	}

	public WorkTaskBean addTask(Long junZhuId, int firstTaskId, ZhuXian zx) {
		WorkTaskBean b = new WorkTaskBean();
		b.dbId = junZhuId * spaceFactor;
		long end = b.dbId+spaceFactor;

		long id = HibernateUtil.getColumnValueMaxOnWhere(WorkTaskBean.class, "dbId",
				" where dbId>="+b.dbId+" and dbId<"+end);
		if(id >= b.dbId){
			b.dbId = id+1;
		}
		/*
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if (list != null && list.size() != 0){
			for (WorkTaskBean task: list){
				if (task.dbId > id){
					id = task.dbId;
				}
			}
			b.dbId = id + 1;
		}
		*/
		b.tid = firstTaskId;
		b.progress = 0;
		// 首个任务默认完成
		if(firstTaskId == first_task_id){
			b.progress = -1;
		}
		if(zx != null && zx.getDoneCond() != null){
			byte type = zx.getDoneType();
			switch(type){
				case TaskData.tanbao_oneTimes:
					int donCond = Integer.parseInt(zx.getDoneCond());
					ExploreMine mine = ExploreMgr.inst.getMineByType(junZhuId, TanBaoData.yuanBao_type);
					if(mine != null && mine.danChouClickNumber >= donCond){
						b.progress = -1;
					}
					break;
				case TaskData.tanbao_tenTimes:
					donCond = Integer.parseInt(zx.getDoneCond());
					mine = ExploreMgr.inst.getMineByType(junZhuId, TanBaoData.yuanBao_type);
					if(mine != null && mine.tenChouClickNumber >= donCond){
						b.progress = -1;
					}
					break;
				case TaskData.tongbi_oneTimes:
					donCond = Integer.parseInt(zx.getDoneCond());
					mine = ExploreMgr.inst.getMineByType(junZhuId, TanBaoData.tongBi_type);
					if(mine != null && mine.danChouClickNumber >= donCond){
						b.progress = -1;
					}
					break;
				case TaskData.tongbi_tenTimes:
					donCond = Integer.parseInt(zx.getDoneCond());
					mine = ExploreMgr.inst.getMineByType(junZhuId,  TanBaoData.tongBi_type);
					if(mine != null && mine.tenChouClickNumber >= donCond){
						b.progress = -1;
					}
					break;
				case TaskData.active_taozhuang:
					donCond = Integer.parseInt(zx.getDoneCond());
					AcitvitedTaoZhuang taozhuang = HibernateUtil.find(AcitvitedTaoZhuang.class, junZhuId);
					if(taozhuang != null){
						TaoZhuang conf = JunZhuMgr.taoZhuangMap.get(taozhuang.maxActiId);
						if(conf != null && conf.condition >= donCond){
							b.progress = -1;
							log.info("君主：{}提前完成主线激活套装的任务，加载该任务，并设置为完成", junZhuId);
						}
					}
					break;
				case TaskData.get_achieve:
					List<XianshiHuodong> xsList=XianShiActivityMgr.bigActivityMap.get(XianShiConstont.TANBAO_ONETIMES);
					if(xsList != null){
						for(XianshiHuodong d: xsList){
							boolean isExist = Redis.getInstance().lexist(
									(XianShiActivityMgr.XIANSHIYILING_KEY + junZhuId), d.getId()+"");
							if(isExist){
								b.progress = -1;
								break;
							}
						}
					}
					break;
				case TaskData.jibai:
					ChouJiangBean cb = HibernateUtil.find(ChouJiangBean.class, junZhuId);
					if(cb != null && cb.historyAll > 0){
						b.progress = -1;
					}
					break;
				/*
				 * 君主的等级任务是否已经完成
				 */
				case TaskData.junzhu_level_up:
					donCond = Integer.parseInt(zx.getDoneCond());
					JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId);
					if(jz != null && jz.level >= donCond){
						b.progress = -1;
					}
					break;
				/*
				 * 获取特定秘宝 不管解锁与否
				 */
				case TaskData.MIBAO_HECHENG:
					donCond = Integer.parseInt(zx.getDoneCond());
					List<MiBaoDB> lm = MibaoMgr.inst.getActiveMibaosFromDB(junZhuId);
					for(MiBaoDB d: lm){
						if( d.getMiBaoId() == donCond){
							b.progress = -1;
							break;
						}
					}
				break;
				/*
				 * 秘宝个数是否达到
				 */
				case TaskData.get_x_mibao:
					donCond = Integer.parseInt(zx.getDoneCond());
					int number =  MibaoMgr.inst.getActivateMiBaoCount(junZhuId);
					if (number >= donCond) {
						b.progress = -1;
					}
					break;
				/*
				 * 秘宝星级是否已经达到
				 */
				case TaskData.mibao_shengStar_x:
					donCond = Integer.parseInt(zx.getDoneCond());
					List<MiBaoDB> listmibao = MibaoMgr.inst.getActiveMibaosFromDB(junZhuId);
					for(MiBaoDB d: listmibao){
						if(d.getStar() >= donCond){
								b.progress = -1;
								break;
						}
					}
					break;
				/*
				 * 秘宝等级是否达到
				 */
				case TaskData.mibao_shengji_x:
					donCond = Integer.parseInt(zx.getDoneCond());
					listmibao = MibaoMgr.inst.getActiveMibaosFromDB(junZhuId);
					for(MiBaoDB d: listmibao){
						if(d.getLevel() >= donCond){
								b.progress = -1;
								break;
						}
					}
					break;
				/*
				 * 对于普通关卡判断是否首次已经通关
				 */
				case TaskData.PVE_GUANQIA:
					donCond = Integer.parseInt(zx.getDoneCond());
					if(donCond != guanQia_2_5){
						PveRecord r = HibernateUtil.find(PveRecord.class,
								"where guanQiaId=" + donCond + " and uid=" + junZhuId);
						if(r != null){
							b.progress = -1;
						}
					}
					break;
				/*
				 * 对于传奇关卡判断是否首次已经通关
				 */
				case TaskData.FINISH_CHUANQI:
					donCond = Integer.parseInt(zx.getDoneCond());
					PveRecord r = HibernateUtil.find(PveRecord.class,
							"where guanQiaId=" + donCond + " and uid=" + junZhuId);
					if(r != null && r.cqPassTimes >= 1){
						b.progress = -1;
					}
					break;
				/*
				 * 购买铜币是否历史已经买过
				 */
				case TaskData.buy_tongbi_1_times:
					TongBi tongBi = HibernateUtil.find(TongBi.class, junZhuId);
					if(tongBi != null){
						b.progress = -1;
					}
					break;
				/*
				 * 是否历史购买过体力
				 */
				case TaskData.buy_tili_1_times:
					TiLi tiLi = HibernateUtil.find(TiLi.class, junZhuId);
					if(tiLi != null){
						b.progress = -1;
					}
					break;
				/*
				 * 百战打了n场
				 */
				case TaskData.FINISH_BAIZHAN_N:
					donCond = Integer.parseInt(zx.getDoneCond());
					PvpBean bean = HibernateUtil.find(PvpBean.class, junZhuId);
					if(bean != null && bean.allBattleTimes >= donCond){
						b.progress = -1;
					}
					break;
				/*
				 * 百战胜利n场
				 */
				case TaskData.SUCCESS_BAIZHAN_N:
					donCond = Integer.parseInt(zx.getDoneCond());
					bean = HibernateUtil.find(PvpBean.class, junZhuId);
					if(bean != null && bean.allWin >= donCond){
						b.progress = -1;
					}
					break;
				/*
				 * 百战达到多少名次是否已经完成
				 */
				case TaskData.baizhan_rank_n:
					donCond = Integer.parseInt(zx.getDoneCond());
					int rank = PvpMgr.inst.getPvpRankById(junZhuId);
					if(rank != -1 && rank <= donCond){
						b.progress = -1;
					}
					break;
					/*
					 * 天赋等级是否已经达到
					 */
				case TaskData.tianfu_level_x:
					donCond = Integer.parseInt(zx.getDoneCond());
					String where = "where junZhuId=" + junZhuId;
					List<TalentPoint> listT = HibernateUtil.list(TalentPoint.class, where);
					for(TalentPoint t: listT){
						if(t.level >= donCond){
							b.progress = -1;
							break;
						}
					}
					break;
				/*
				 * 0.99  强化到任意等级
				 */
				case TaskData.qianghua_level_x:
					donCond = Integer.parseInt(zx.getDoneCond());
					where = "where userId = " + junZhuId;
					List<UserEquip> equipList = HibernateUtil.list(UserEquip.class, where);
					for(UserEquip e: equipList){
						if(e.getLevel() >= donCond){
							b.progress = -1;
							break;
						}
					}
					break;
				// n个装备的品质大于等于固定品质，任务类型
				case TaskData.N_quality_ok:
					String[] condis = zx.getDoneCond().split(":");
					if(condis != null && condis.length == 2){
						Bag<EquipGrid> equipBag = EquipMgr.inst.loadEquips(junZhuId);
						boolean yes = isFinishTask40(equipBag, Integer.parseInt(condis[0]),
								Integer.parseInt(condis[1]));
						if(yes){
							b.progress = -1;
						}
					}
					break;
				// 指定部位装备的品质大于等于固定品质，任务类型
				case TaskData.one_quality_ok:
					condis = zx.getDoneCond().split(":");
					if(condis != null && condis.length == 2){
						Bag<EquipGrid> equipBag = EquipMgr.inst.loadEquips(junZhuId);
						boolean yes = isFinishTask41(equipBag, Integer.parseInt(condis[0]), 
								Integer.parseInt(condis[1]));
						if(yes){
							b.progress = -1;
						}
					}
					break;
				/*
				 * 部位11的装备 是否有参加过强化的，认为任务已经完成
				 */
				case TaskData.ONE_QIANG_HAU:
					where = "where userId = " + junZhuId;
					equipList = HibernateUtil.list(UserEquip.class, where);
					for(UserEquip e: equipList){
						if(e.getLevel() == 0 && e.getExp() == 0){
							continue;
						}
						ZhuangBei zb = (ZhuangBei)TempletService.itemMap.get(e.getTemplateId());
						if(zb != null && zb.buWei == EquipMgr.EQUIP_HEAD){ //头盔(部位)
							b.progress = -1;
							break;
						}
					}
					break;
				/*
				 * 创建或者加入一个联盟
				 */
				case TaskData.join_lianmeng:
					AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, junZhuId);
					if (player != null && player.lianMengId > 0) {
						b.progress = -1;
						break;
					}
					break;
				/////////以下1.0版本添加  20150917/////////////
				/*
				 * 是否激活过一次秘宝技能
				 */
				case TaskData.active_mibao_skill:
					List<MiBaoSkillDB> skillD = MibaoMgr.inst.getSkillDBList(junZhuId);
					if(skillD != null && skillD.size() != 0){
						b.progress = -1;
					}
					break;
//					for(MiBaoSkillDB d: skillD){
//						if(d.hasClear){
//							b.progress = -1;
//							break;
//						}
//					}
				/*
				 *  是否参与过膜拜
				 */
				case TaskData.mobai:
					MoBaiBean mo = HibernateUtil.find(MoBaiBean.class, junZhuId);
					if(mo != null){
						b.progress = -1;
					}
					break;
//				/*
//				 * 是否捐献过贡金
//				 */
//				case TaskData.give_gongjin:
//					ResourceGongJin gongjinBean = HibernateUtil.find(ResourceGongJin.class, junZhuId);
//					if(gongjinBean != null && gongjinBean.juanXianTime != null){
//						b.progress = -1;
//					}
//					break;
				/*
				 * 是否参加过掠夺
				 */
				case TaskData.lve_duo:
					LveDuoBean lve = HibernateUtil.find(LveDuoBean.class, junZhuId);
					if(lve != null && lve.lastBattleTime != null){
						b.progress = -1;
					}
					break;
					/*
					 * 是否领取过威望
					 */
				case TaskData.get_produce_weiWang:
					PvpBean d = HibernateUtil.find(PvpBean.class, junZhuId);
					if(d!= null && d.getProduceWeiWangTimes >= 1){
						b.progress = -1;
					}
					break;
					/*
					 * 是否花威望购买过东西
					 */
				case TaskData.pay_weiWang:
					PublicShop s = HibernateUtil.find(PublicShop.class, 
							junZhuId * ShopMgr.shop_space + ShopMgr.baizhan_shop_type);
					if(s!=null && s.buyGoodTimes >= 1){
						b.progress = -1;
					}
					break;
					/*
					 * 是否扫荡过
					 */
				case TaskData.saoDang :
					SaoDangBean sd = HibernateUtil.find(SaoDangBean.class, junZhuId);
					if(sd != null && sd.jyAllSaoDangTimes >= 1){
						b.progress = -1;
					}
					break;
					/*
					 * 当前玩家身上是否装备了符石
					 */
				case TaskData.wear_fushi:
					List<Fuwen> listF = FuwenMgr.inst.getFushiEquiped(junZhuId);
					if(listF != null && listF.size() >= 1){
						b.progress = -1;
						log.info("玩家：{}装备符石任务提前完成", junZhuId);
					}
					break;
					/*
					 * 玩家是否装修过房屋
					 */
				case TaskData.fix_house:
					HouseBean selfBean = HibernateUtil.find(HouseBean.class, junZhuId);
					if (selfBean != null && selfBean.preUpTime != null){
						b.progress = -1;
					}
					break;
					/*
					 * 是否领取过房屋经验
					 */
				case TaskData.get_house_exp:
					selfBean = HibernateUtil.find(HouseBean.class, junZhuId);
					if (selfBean != null && selfBean.preGainExpTime != null){
						b.progress = -1;
					}
					break;
					/*
					 * 是否集齐一套古卷
					 */
				case TaskData.have_total_guJuan:
					Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhuId);
					int baseType = BaseItem.TYPE_GU_JUAN_1;
					for(; baseType<=BaseItem.TYPE_GU_JUAN_5; baseType++){
						List<BagGrid> gridList = bag.grids;
						ArrayList<Integer> idList = new ArrayList<Integer>();
						int count = 0; // 当前这个算一种类
						for(BagGrid grid : gridList){
							int nextId = grid.itemId;
							if(!idList.contains(nextId) && grid.type == type){
								count += 1;
								idList.add(nextId);
							}
						}
						if(count >= BagMgr.a_suit_of_gu_juan){
							b.progress = -1;
							log.info("君主：{}，提前完成收集古卷的任务", junZhuId);
							break;
						}
					}
					break;
					/*
					 * 是否打过荒野
					 */
				case TaskData.battle_huang_ye:
					HYTreasureTimes hyTimes = HibernateUtil.find(HYTreasureTimes.class, junZhuId);
					if(hyTimes != null && hyTimes.allBattleTimes >= 1){
							b.progress = -1;
					}
					break;
					/*
					 * 是否对指定秘宝升星一次
					 */
				case TaskData.mibao_shengStar:
					donCond = Integer.parseInt(zx.getDoneCond());
					MiBaoDB miBaoDB = HibernateUtil.find(MiBaoDB.class,
							" where ownerId=" + junZhuId + " and mibaoId=" + donCond);
					if(miBaoDB != null && miBaoDB.hasShengXing) {
						b.progress = -1;
					}
					break;
				case TaskData.zhuangBei_x_qiangHua_N:
					condis = zx.getDoneCond().split(":");
					if(condis != null && condis.length == 2){
						boolean yes = isQiangHua_X_N(junZhuId,
								Integer.parseInt(condis[0]),
								Integer.parseInt(condis[1]));
						if(yes){
							b.progress = -1;
						}
					}
					break;
				case TaskData.miabao_x_star_n:
					condis = zx.getDoneCond().split(":");
					if(condis != null && condis.length == 2){
						boolean yes = MibaoMgr.inst.isMibaoStarOk(junZhuId,
								Integer.parseInt(condis[0]),
								Integer.parseInt(condis[1]));
						if(yes){
							b.progress = -1;
						}
					}
					break;
					/*
					 * 是否进阶过角色技能一次
					 */
				case TaskData.jinJie_jueSe_jiNeng:
					JNBean jn = HibernateUtil.find(JNBean.class, junZhuId);
					if(jn != null){
						b.progress = -1;
					}
					break;
					/*
					 * X个角色技能达到N级
					 * 完成条件填字符串 x:n
					 */
				case TaskData.jueSe_x_jiNeng_n:
					condis = zx.getDoneCond().split(":");
					if(condis != null && condis.length == 2){
						boolean yes = isJueSeJiNengOk(junZhuId,
								Integer.parseInt(condis[0]),
								Integer.parseInt(condis[1]));
						if(yes){
							b.progress = -1;
						}
					}
					break;
					/*
					 * 是否领取过任意通章 奖励
					 */
				case TaskData.get_pass_PVE_zhang_award:
					List<PveRecord> lr = HibernateUtil.list(PveRecord.class,
							" where uid =" + junZhuId);
					for(PveRecord re: lr){
						if(re.isGetAward){
							b.progress = -1;
							break;
						}
					}
					break;
				case TaskData.finish_youxia_x:
					donCond = Integer.parseInt(zx.getDoneCond());
					List<YouXiaBean> youXiaInfoList = HibernateUtil.list(YouXiaBean.class,
							" where junzhuId = " +junZhuId);
					int allwin = 0;
					for(YouXiaBean you: youXiaInfoList){
						allwin += you.allWinTimes;
					}
					if(allwin >= donCond){
						b.progress = -1;
					}
					break;
				case TaskData.go_youxia:
					donCond = Integer.parseInt(zx.getDoneCond());
					youXiaInfoList = HibernateUtil.list(YouXiaBean.class,
							" where junzhuId = " +junZhuId);
					int allBattle = 0;
					for(YouXiaBean you: youXiaInfoList){
						allBattle += you.allBattleTimes;
					}
					if(allBattle >= donCond){
						b.progress = -1;
					}
					break;
				case TaskData.battle_shiLian_II:
					String donCondstring = zx.getDoneCond();
					YouXiaBean yxBean = HibernateUtil.find(YouXiaBean.class,
					" where junzhuId=" + junZhuId + " and type =" + donCondstring);
					if(yxBean != null && yxBean.lastBattleTime != null){
						b.progress = -1;
						log.info("君主：{}提前完成攻打试练II难度1次（拿符石），加载任务，并设置为完成", junZhuId);
					}
					break;
				case TaskData.qiandao:
					QiandaoInfo qiandaoInfo = HibernateUtil.find(QiandaoInfo.class,junZhuId);
					if(qiandaoInfo != null && qiandaoInfo.historyQianDao > 0){
						b.progress = -1;
						log.info("君主：{}提前完成主线签到任务，加载签到任务，并设置为完成", junZhuId);
					}
					break;
				case TaskData.qiandao_get_v:
					QianDaoPresent pre = HibernateUtil.find(QianDaoPresent.class, junZhuId);
					if(pre != null &&
							(pre.isGet1 || pre.isGet2|| pre.isGet3 ||
									pre.isGet4|| pre.isGet5 || pre.isGet6 || pre.isGet7)){
						b.progress = -1;
						log.info("君主：{}提前完成主线领取签到v特权奖励，加载该任务，并设置为完成", junZhuId);
					}
					break;
					/*
					 * 运镖一次
					 */
				case TaskData.finish_yunbiao_x:
					YunBiaoHistory histo = HibernateUtil.find(YunBiaoHistory.class, junZhuId);
					if(histo != null && histo.historyYB > 0){
						b.progress = -1;
						log.info("君主：{}提前完成主线运镖一次，加载该任务，并设置为完成", junZhuId);
					}
					break;
				/*
				 * 劫镖一次
				 */
				case TaskData.finish_jiebiao_x:
					YunBiaoHistory histo2 = HibernateUtil.find(YunBiaoHistory.class, junZhuId);
					if(histo2 != null && histo2.historyJB > 0){
						b.progress = -1;
						log.info("君主：{}提前完成主线劫镖一次，加载该任务，并设置为完成", junZhuId);
					}
					break;
					
			}
		}
		HibernateUtil.save(b);
		log.info("{}增加任务:{}, dbid:{}, progress:{}",junZhuId,b.tid, b.dbId, b.progress);
		OurLog.log.RoundFlow(ActLog.vopenid, b.tid, 1, 0, 0, 1, String.valueOf(junZhuId));
		if(b.progress == -1){
			OurLog.log.RoundFlow(ActLog.vopenid, b.tid, 1, 0, 0, 2, String.valueOf(junZhuId));
		}
		return b;
	}

	public void getReward(int id, IoSession session, Builder builder) {
		GetTaskReward.Builder request = (qxmobile.protobuf.GameTask.GetTaskReward.Builder) builder;
		int taskId = request.getTaskId();

		GetTaskRwardResult.Builder response = GetTaskRwardResult.newBuilder();
		response.setTaskId(taskId);
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			response.setMsg("fail");
			session.write(response.build());
			log.error("junZhuId为null");
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId);
		WorkTaskBean taskBean = getTask(junZhuId, taskId);
		/*
		List<WorkTaskBean> taskList0 = getTaskList(junZhuId);
		for(WorkTaskBean bean : taskList){
			if(bean.tid == taskId){
				taskBean = bean;
				break;
			}
		}
		*/
		if(taskBean == null || taskBean.progress == -2){
			response.setMsg("hasGet");
			session.write(response.build());
			log.error("该玩家没有此条任务，junzhuId:{},taskId:{}", junZhuId, taskId);
			return;
		}
		ZhuXian zhuXian = zhuxianTaskMap.get(taskId);
		if(zhuXian == null){
			response.setMsg("fail");
			session.write(response.build());
			log.error("找不到对应的主线任务信息，taskId:{}", taskId);
			return;
		}
		// 保留最后一条主线任务
		if(zhuXian.orderIdx != maxZhuRenWuOrderIdx){
			log.info("正确删除了任务id是：{}",taskBean.tid );
			HibernateUtil.delete(taskBean);
		}else{
			taskBean.progress = -2;
			HibernateUtil.save(taskBean);
		}
		String awardStr = zhuXian.getAward();
		StringBuffer mess = new StringBuffer();
		boolean allSucc = true;
		if(awardStr != null && !awardStr.equals("")){
			String[] goodsList = awardStr.split("#");
			for(String goods : goodsList){
				String[] goodsInfo = goods.split(":");
				AwardTemp awardTemp = new AwardTemp();
				int itemType = Integer.parseInt(goodsInfo[0]);
				int itemId = Integer.parseInt(goodsInfo[1]);
				int itemNum = Integer.parseInt(goodsInfo[2]);
				awardTemp.setItemType(itemType);
				awardTemp.setItemId(itemId);
				awardTemp.setItemNum(itemNum);
				boolean success = AwardMgr.inst.giveReward(session, awardTemp, jz);
				// 暂时这样做
				if (!success){
					mess.append("领取奖励: " + itemId + "失败;");
					allSucc = false;
				}else{
					mess.append("领取奖励: " + itemId +"成功;");
				}				
			}
			response.setMsg(allSucc?"success":"fail");
			log.info("{},完成主线任务id:{},并且成功领取奖励： {}" ,junZhuId, taskId, mess);
		}else {
			response.setMsg("fail");
			log.error("{} 主线任务id:{},但是领取奖励失败", junZhuId, taskId);
		}
		
		/*
		 * 领奖之后添加下一条任务  20150508
		 */
		fireNextTrigger100Task(junZhuId, taskId);
		sendTaskList(0, session, null);
		
		session.write(response.build());
		// 只记录主线的完成，记录领奖
		if(zhuXian.type == zhuXianType){
			MemcachedCRUD.getMemCachedClient().set(FunctionOpenMgr.awardRenWuOverIdKey+junZhuId, taskId);
			log.info("君主：{}AwardRenWuOverId是：{}", junZhuId, taskId);
		}
	}

	/**
	 * 客户端汇报进度，对话任务。
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void clientUpdateProgress(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("junZhuId为null");
			return;
		}
		TaskProgress.Builder req = (TaskProgress.Builder)builder;
		TaskInfo reqInfo = req.getTask();
		WorkTaskBean b = getTask(junZhuId, reqInfo.getId());
		if(b == null){
			log.error("task not found for pid {} tid {}",junZhuId, reqInfo.getId());
			return;
		}
		/*
		List<WorkTaskBean> list = getTaskList(junZhuId);
		for(WorkTaskBean b : list){
			if(b.tid == reqInfo.getId()){
			*/
				//目前只有对话任务，进度为1 就完成了。
				if(b.progress == 0 && reqInfo.getProgress() == 1){
					b.progress = -1;
					HibernateUtil.save(b);
					log.info("{}完成任务{}",junZhuId, b.tid);
					OurLog.log.RoundFlow(ActLog.vopenid, b.tid, 1, 0, 0, 2, String.valueOf(junZhuId));
					fireNextOutTrigger100Task(junZhuId, b.tid);
					ZhuXian zhuXian = zhuxianTaskMap.get(b.tid);
					if(zhuXian.type == zhuXianType){
						MemcachedCRUD.getMemCachedClient().set(FunctionOpenMgr.REN_WU_OVER_ID+junZhuId, b.tid);
					}
				}
				/*
				break;
			}
		}
		*/
		sendTaskList(id, session, builder);
	}

	/*
	 * 任务完成就调用
	 */
	public void fireNextOutTrigger100Task(Long junZhuId, int tid) {
		ZhuXian cur = zhuxianTaskMap.get(tid);
		if(cur == null){
			log.error("任务配置未找到{}",tid);
			return;
		}
		List<Integer> nextIds = cur.getBranchRenWuIds();
		if(nextIds == null){
			return;
		}
		ZhuXian data = null;
		for(Integer id: nextIds){
			data = zhuxianTaskMap.get(id);
			if(data == null){
				continue;
			}
			if(data.getTriggerType() == after_renwu_trigger_type){
				addTask(junZhuId, id, data);
			}
		}
	}

	/*
	 * 任务领完奖励调用
	 */
	public void fireNextTrigger100Task(Long junZhuId, int tid) {
		ZhuXian cur = zhuxianTaskMap.get(tid);
		if(cur == null){
			log.error("任务配置未找到{}",tid);
			return;
		}
		List<Integer> nextIds = cur.getBranchRenWuIds();
		if(nextIds == null){
			log.error("这个是空，需要程序员检查一下配置！！！tid is :{}", tid);
			return;
		}
		ZhuXian data = null;
		for(Integer id: nextIds){
			data = zhuxianTaskMap.get(id);
			if(data == null){
				continue;
			}
			if(data.getTriggerType() == after_award_trigger_type){
				addTask(junZhuId, id, data);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void proc(Event event) {
		if (event.param != null && event.param instanceof Object[]){
		   Object[] obs = (Object[])event.param;
		   long junZhuId = (Long)obs[0];
		   log.info("junzhuId：{}主线任务事件处理调用", junZhuId);
		   switch (event.id) {
		   case ED.EQUIP_ADD:
			    // 增加判断穿装备任务 和 进阶品质任务是否完成
				zhuangBeiTask(junZhuId, (Integer)obs[1] + "", (Bag<EquipGrid>)obs[2], 1);
			/*
			 * 0.98版本
			 *  Integer zhuangbeiId = (Integer)obs[1];			   
	 		 *  recordTaskProcess(junZhuId, TaskData.EQUIP_ADD, zhuangbeiId + "");
			 */
				break;
				// 攻打关卡任务完成处理
		   case ED.PVE_GUANQIA:
			   Integer guanQiaId = (Integer)obs[1];
			   pveGuanQiaRecord(junZhuId, TaskData.PVE_GUANQIA, guanQiaId);	
			   break;
		   case ED.JUNZHU_KEJI_PROMOTE:
			   Integer kejiId = (Integer)obs[1];			   
			   recordTaskProcess(junZhuId, TaskData.JUNZHU_KEJI_PROMOTE, kejiId + "");
			   break;
			   // 获取物品
		   case ED.get_item_finish:
			   Integer itemid = (Integer)obs[1];			   
			   recordTaskProcess(junZhuId, TaskData.get_item, itemid + "");
			   break;
			   // 完成一次强化
		   case ED.QIANG_HUA_FINISH:
			     // 0.99增加强化等级的任务
			     Integer qianhuaLev = (Integer)obs[1];
			     qianghua_Task(junZhuId, qianhuaLev);
			 /*		0.98版本
			  *   recordTaskProcess(junZhuId, TaskData.ONE_QIANG_HAU, 1+ "");
			  */
			   break;
		   case ED.GET_START_PVE_AWARD:
			   Integer guanqiaId = (Integer)obs[1];
			   log.info("领取精英关卡奖励完成, 关卡id 是: {}", guanqiaId);
			   recordTaskProcess(junZhuId, TaskData.GET_PVE_AWARD, guanqiaId + "");
			   break;
		   case ED.CHUANQI_GUANQIA_SUCCESS:
			   Integer chuanqiId = (Integer)obs[1];
			   log.info("支线任务完成：攻打一次传奇关卡并胜利, 关卡id 是: {}", chuanqiId);
			   recordTaskProcess(junZhuId, TaskData.FINISH_CHUANQI, chuanqiId + "");
			   break;
		   case ED.FINISH_BAIZHAN_N:
			   Integer times = (Integer)obs[1];
			   log.info("主线任务完成，完成：{}次百战，无论输赢", times);
			   recordTaskProcess(junZhuId, TaskData.FINISH_BAIZHAN_N, times+"");
			   break;
		   case ED.XILIAN_ONE_GONG:
//			   Integer xilianX = (Integer)obs[1];
//			   log.info("主线任务完成，第:{}次完成洗练", xilianX);
			   recordTaskProcess(junZhuId, TaskData.XILIAN_ONE_GONG, 1+"");
			   break;
		   case ED.JINJIE_ONE_GONG:
			   Integer wuQiId = (Integer)obs[1];
			   log.info("主线任务完成，一次弓进阶，弓id：{}", wuQiId);
			   /*
			    * 0.98版本
			   recordTaskProcess(junZhuId, TaskData.JINJIE_ONE_GONG, wuQiId+"");
			   */
			    // 0.99 添加 检查进阶任务 和 品质任务
			    zhuangBeiTask(junZhuId, wuQiId+"", (Bag<EquipGrid>)obs[2], 2);
			   break;
		   case ED.MIBAO_HECHENG:
			   Integer mibaoID = (Integer)obs[1];
			   log.info("主线任务完成，完成一次秘宝合成的任务,秘宝id：{}", mibaoID);
			   recordTaskProcess(junZhuId, TaskData.MIBAO_HECHENG, mibaoID+"");
			   break;
//		   case ED.MIBAO_SHENGJI:
//			   Integer mibaoId = (Integer)obs[1];
//			   log.info("主线任务完成，完成一次秘宝升级的任务,秘宝id：{}", mibaoId);
//			   recordTaskProcess(junZhuId, TaskData.MIBAO_SHENGJI, mibaoId+"");
//			   break;
		   case ED.MIBAO_SEHNGXING:
			   Integer mibaoid = (Integer)obs[1];
			   log.info("主线任务完成，完成一次秘宝升星的任务，秘宝id：{}", mibaoid);
			   recordTaskProcess(junZhuId, TaskData.MIBAO_SEHNGXING, mibaoid+"");
			   break;
		   case ED.SUCCESS_BAIZHAN_N:
			   Integer timesN = (Integer)obs[1];
			   log.info("主线任务完成，胜利：{}次百战", timesN);
			   recordTaskProcess(junZhuId, TaskData.SUCCESS_BAIZHAN_N, timesN+"");
			   break;
		   case ED.mibao_shengStar_x:
			   Integer starXTimes = (Integer)obs[1];
			   log.info("主线任务完成,任意秘宝升级星级到：{}", starXTimes);
			   recordTaskProcess(junZhuId, TaskData.mibao_shengStar_x, starXTimes+"");
			   break;
		   case ED.mibao_shengji_x:
			   Integer shengjiXTimes = (Integer)obs[1];
			   log.info("主线任务完成,任意秘宝升级等级到：{}", shengjiXTimes);
			   recordTaskProcess(junZhuId, TaskData.mibao_shengji_x, shengjiXTimes+"");
			   break;
		   case ED.get_x_mibao:
			   Integer mibaoX = (Integer)obs[1];
			   log.info("主线任务完成,获取秘宝：{}个", mibaoX);
			   recordTaskProcess(junZhuId, TaskData.get_x_mibao, mibaoX+"");
			   break;
		   case ED.baizhan_rank_n:
			   Integer rankX = (Integer)obs[1];
			   log.info("主线任务完成,百战名次达到：{}", rankX);
			   recordTaskProcess(junZhuId, TaskData.baizhan_rank_n, rankX+"");
			   break;
		   case ED.Join_LM:
			   Integer lianmengId = (Integer)obs[1];
			   log.info("主线任务完成,加入联盟成功：{}", lianmengId);
			   recordTaskProcess(junZhuId, TaskData.join_lianmeng, "-1");
			   break;
		   case ED.buy_tongbi_1_times:
			   log.info("主线任务完成,购买铜币1次完成");
			   recordTaskProcess(junZhuId, TaskData.buy_tongbi_1_times, "1");
			   break;
		   case ED.buy_tili_1_times:
			   log.info("主线任务完成,购买体力1次完成");
			   recordTaskProcess(junZhuId, TaskData.buy_tili_1_times, "1");
			   break;
		   case ED.junzhu_level_up:
			   Integer levelU = (Integer)obs[1];
			   log.info("支线任务完成：等级升级到:{}", levelU);
			   recordTaskProcess(junZhuId, TaskData.junzhu_level_up, levelU+"");
			   break;
		   case ED.finish_yunbiao_x:
			   recordTaskProcess(junZhuId, TaskData.finish_yunbiao_x, 1+"");
			   break;
		   case ED.finish_jiebiao_x:
			   recordTaskProcess(junZhuId, TaskData.finish_jiebiao_x, 1+"");
			   break;
		   case ED.go_youxia:
			   Integer allBattleTimes = (Integer)obs[1];
			   Integer pveBigId = (Integer)obs[2];
			   isYouXiaOk(junZhuId, allBattleTimes, pveBigId);
			   break;
		   case ED.finish_youxia_x:
			   log.info("君主：{},主线任务完成游侠活动",junZhuId);
			   recordTaskProcess(junZhuId, TaskData.finish_youxia_x, 1+"");
			   break;
		   case ED.tianfu_level_up_x:
			    //  0.99增加天赋等级的任务
			    Integer pointLevel = (Integer)obs[1];
			    log.info("君主：{},主线任务完成一次天赋升级, 并升级到：{}", junZhuId, pointLevel);
			    tianfu_Task(junZhuId, pointLevel);
			   /*
			    * 0.98 版本
			   log.info("君主：{},主线任务完成一次天赋升级", junZhuId);
			   recordTaskProcess(junZhuId, TaskData.tianfu_level_up, 1+"");
			   */
			   break;
		   case ED.get_produce_weiWang:
			   recordTaskProcess(junZhuId, TaskData.get_produce_weiWang, 1+"");
			   break;
		   case ED.pay_weiWang:
			   recordTaskProcess(junZhuId, TaskData.pay_weiWang, 1+"");
			   break;
		   case ED.active_mibao_skill:
			   recordTaskProcess(junZhuId, TaskData.active_mibao_skill, 1+"");
			   break;
		   case ED. saoDang:
			   recordTaskProcess(junZhuId, TaskData. saoDang, 1+"");
			   break;
		   case ED.wear_fushi:
			   recordTaskProcess(junZhuId, TaskData.wear_fushi, 1+"");
			   break;
		   case ED.mobai:
			   recordTaskProcess(junZhuId, TaskData.mobai, 1+"");
			   break;
		   case ED.jibai:
			   recordTaskProcess(junZhuId, TaskData.jibai, 1+"");
			   break;
		   case ED.fix_house:
			   recordTaskProcess(junZhuId, TaskData.fix_house, 1+"");
			   break;
		   case ED.get_house_exp:
			   recordTaskProcess(junZhuId, TaskData.get_house_exp, 1+"");
			   break;
		   case ED.have_total_guJuan:
			   recordTaskProcess(junZhuId, TaskData.have_total_guJuan, 1+"");
			   break;
		   case ED.battle_huang_ye:
			   recordTaskProcess(junZhuId, TaskData.battle_huang_ye, 1+"");
			   break;
		   case ED.lve_duo:
			   recordTaskProcess(junZhuId, TaskData.lve_duo, 1+"");
			   break;
		   case ED.pawnshop_buy:
			   recordTaskProcess(junZhuId, TaskData.pawnshop_buy, 1+"");
			   break;
		   case ED.mibao_shengStar:
			   Integer mbid = (Integer)obs[1];
			   recordTaskProcess(junZhuId, TaskData.mibao_shengStar, mbid+"");
			   break;
		   case ED.get_pass_PVE_zhang_award:
			   recordTaskProcess(junZhuId, TaskData.get_pass_PVE_zhang_award, 1+"");
			   break;
		   /*
		    * 判断角色技能相关任务是否完成
		    */
		   case ED.jinJie_jueSe_jiNeng:
			   jueSe_jiNeng_task(junZhuId);
			   break;
		   case ED.qiandao: // 签到
			   recordTaskProcess(junZhuId, TaskData.qiandao, 1+"");
			   break;
		   case ED.qiandao_get_v: // 领取签到特权奖励
			   recordTaskProcess(junZhuId, TaskData.qiandao_get_v, 1+"");
			   break;
		   case ED.miabao_x_star_n:
			   isMiBaoStarOk(junZhuId);
			   break;
		   case ED.get_achieve: // 领取限时活动奖励一次
			   recordTaskProcess(junZhuId, TaskData.get_achieve, 1+"");
			   break;
		   case ED.tanbao_oneTimes:
			   Integer yuanbaoDanTimes0 = (Integer)obs[1];
			   recordTaskProcess(junZhuId, TaskData.tanbao_oneTimes, yuanbaoDanTimes0+"");
			   break;
		   case ED.tanbao_tenTimes:
			   Integer yuanbaoDanTimes1 = (Integer)obs[1];
			   recordTaskProcess(junZhuId, TaskData.tanbao_tenTimes, yuanbaoDanTimes1+"");
			   break;
		   case ED.tongbi_oneTimes:
			   Integer yuanbaoDanTimes2 = (Integer)obs[1];
			   recordTaskProcess(junZhuId, TaskData.tongbi_oneTimes, yuanbaoDanTimes2+"");
			   break;
		   case ED.tongbi_tenTimes:
			   Integer yuanbaoDanTimes3 = (Integer)obs[1];
			   recordTaskProcess(junZhuId, TaskData.tongbi_tenTimes, yuanbaoDanTimes3+"");
			   break;
		   case ED.active_taozhuang:
			   Integer taozhuangPinZhi = (Integer)obs[1];
			   recordTaskProcess(junZhuId, TaskData.active_taozhuang, taozhuangPinZhi+"");
			   break;
		   default:
			   break;
		   }
	   }
	}

	public void isYouXiaOk(long junZhuId, int allBattleTimes, int pveBigId){
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if(list == null){
			return;
		}
		ZhuXian task = null;
		boolean toSend = false;
		for (WorkTaskBean b: list){
			task = zhuxianTaskMap.get(b.tid);
			if(task == null){
				continue;
			}
			if(b.progress != 0){
				continue;
			}
			String condi = task.getDoneCond();
			if(condi == null){
				continue;
			}
			byte type = task.getDoneType();
			if(type == TaskData.go_youxia){
				if(allBattleTimes >= Integer.parseInt(condi)){
					dealTask(junZhuId, b, type, task);
					log.info("君主：{}，去玩任意X次游侠任务完成", junZhuId);
					toSend = true;
					continue;
				}
			}else if(type == TaskData.battle_shiLian_II){
				if(condi.equals(pveBigId+"")){
					dealTask(junZhuId, b, type, task);
					log.info("君主：{}，攻打试练II难度1次（拿符石）任务完成", junZhuId);
					toSend = true;
					continue;
				}
			}
		}
		toSendTask(toSend, junZhuId);
	}
	public void isMiBaoStarOk(long junZhuId){
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if(list == null){
			return;
		}
		ZhuXian task = null;
		boolean toSend = false;
		for (WorkTaskBean b: list){
			task = zhuxianTaskMap.get(b.tid);
			if(task == null){
				continue;
			}
			if(b.progress != 0){
				continue;
			}
			byte type = task.getDoneType();
			if(type == TaskData.miabao_x_star_n){
				String[] condis = task.getDoneCond().split(":");
				if(condis != null && condis.length == 2){
					boolean yes = MibaoMgr.inst.isMibaoStarOk(junZhuId, 
							Integer.parseInt(condis[0]),
							Integer.parseInt(condis[1]));
					if(yes){
						dealTask(junZhuId, b, type, task);
						log.info("君主：{}，x个秘宝到n星级的主线任务完成", junZhuId);
						toSend = true;
						break;
					}
				}
			}
		}
		toSendTask(toSend, junZhuId);
	}
	public void jueSe_jiNeng_task(long junZhuId){
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if(list == null){
			return;
		}
		ZhuXian task = null;
		boolean toSend = false;
		for (WorkTaskBean b: list){
			task = zhuxianTaskMap.get(b.tid);
			if(task == null){
				continue;
			}
			if(b.progress != 0){
				continue;
			}
			byte type = task.getDoneType();
			switch(type){
				case TaskData.jinJie_jueSe_jiNeng:
					dealTask(junZhuId, b, type, task);
					toSend = true;
					break;
				case TaskData.jueSe_x_jiNeng_n:
					String[] condis = task.getDoneCond().split(":");
					if(condis != null && condis.length == 2){
						boolean yes = isJueSeJiNengOk(junZhuId,
								Integer.parseInt(condis[0]),
								Integer.parseInt(condis[1]));
						if(yes){
							dealTask(junZhuId, b, type, task);
							toSend = true;
							log.info("君主：{}，x个技能升级到n等级的主线任务完成", junZhuId);
						}
					}
					break;
			}
			if(toSend){
				break;
			}
		}
		toSendTask(toSend, junZhuId);
	}
	
	public boolean isJueSeJiNengOk(long jid, int numberCondition, int levelCondition){
		JNBean jn = HibernateUtil.find(JNBean.class, jid);
		int number = 0;
		if(jn != null){
			Field[] fs = jn.getClass().getDeclaredFields();
			for(int i=0; i < fs.length ; i++){
				Field f = fs[i];
				f.setAccessible(true);
				String type = f.getType().toString();//得到此属性的类型  
				if (type.equals("int")) {
					try {
						int va = f.getInt(jn);
						if(va == 0){
							continue;
						}
						JiNengPeiYang peizhi = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(va);
						if(peizhi != null){
							if(peizhi.quality >= levelCondition){
								number ++ ;
							}
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(number >= numberCondition){
			return true;
		}
		return false;
	}
	
	public void tianfu_Task(Long junZhuId, int pointLevel){
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if(list == null){
			return;
		}
		ZhuXian task = null;
		boolean toSend = false;
		for (WorkTaskBean b: list){
			task = zhuxianTaskMap.get(b.tid);
			if(task == null){
				continue;
			}
			if(b.progress != 0){
				continue;
			}
			byte type = task.getDoneType();
			switch(type){
				case TaskData.tianfu_level_up:
					dealTask(junZhuId, b, type, task);
					toSend = true;
					break;
				case TaskData.tianfu_level_x:
					if(pointLevel >= Integer.parseInt(task.getDoneCond())){
						dealTask(junZhuId, b, type, task);
						toSend = true;
					}
					break;
			}
			if(toSend){
				break;
			}
		}
		toSendTask(toSend, junZhuId);
	}
	
	public void qianghua_Task(Long junZhuId, int qianghuaLevel){
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if(list == null){
			return;
		}
		ZhuXian task = null;
		boolean toSend = false;
		for (WorkTaskBean b: list){
			task = zhuxianTaskMap.get(b.tid);
			if(task == null){
				continue;
			}
			if(b.progress != 0){
				continue;
			}
			byte type = task.getDoneType();
			switch(type){
				case TaskData.ONE_QIANG_HAU:
					dealTask(junZhuId, b, type, task);
					toSend = true;
					break;
				case TaskData.qianghua_level_x:
					//2015年11月2日王转+
					// 一键强化传递的参数，所以获取当前玩家强化装备最大等级
					if(qianghuaLevel == -1){
						String where = "where userId = " + junZhuId;
						List<UserEquip> equipList = HibernateUtil.list(UserEquip.class, where);
						for(UserEquip e: equipList){
							if(e != null){
								qianghuaLevel = Math.max(e.getLevel(), qianghuaLevel);
							}
						}
					}
					//2015年11月2日王转+
					if(qianghuaLevel >= Integer.parseInt(task.getDoneCond())){
						dealTask(junZhuId, b, type, task);
						toSend = true;
					}
					break;
				case TaskData.zhuangBei_x_qiangHua_N:
					String[] condis = task.getDoneCond().split(":");
					if(condis != null && condis.length == 2){
						boolean yes = isQiangHua_X_N(junZhuId,
								Integer.parseInt(condis[0]),
								Integer.parseInt(condis[1]));
						if(yes){
							dealTask(junZhuId, b, type, task);
							toSend = true;
						}
					}
					break;
			}
		}
		toSendTask(toSend, junZhuId);
	}

	public void toSendTask(boolean toSend, long junZhuId){
		if(toSend){
			SessionUser su  =SessionManager.inst.findByJunZhuId(junZhuId);
			if(su!=null){
				IoSession session = su.session;
				sendTaskList(0, session, null);
			}
		}
	}
	
	public void dealTask(long junZhuId, WorkTaskBean b, byte type, ZhuXian task){
		b.progress = -1;
		HibernateUtil.save(b);
		log.info("君主：{}完成type：{}的主线(支线)任务id:{}， progress被赋值为-1(完成)", junZhuId, type, b.tid);
		OurLog.log.RoundFlow(ActLog.vopenid, b.tid, 1, 0, 0, 2, String.valueOf(junZhuId));
		fireNextOutTrigger100Task(junZhuId, b.tid);
		if(task.type == zhuXianType){
			MemcachedCRUD.getMemCachedClient().set(FunctionOpenMgr.REN_WU_OVER_ID+junZhuId, b.tid);
			log.info("君主：{}RenWuOverId是：{}", junZhuId, b.tid);
		}
	}
	/**
	 * 
	 * @Title: recordTaskProcess  
	 * @Description: 记录任务完成进度并且将下一步任务记录到数据库
	 * @param pId
	 * @param type
	 * @param conditionInfo 任務完成條件
	 * @return void
	 * @throws  
	 * @author wangZhuan
	 */
	public void recordTaskProcess(Long junZhuId, byte type, String conditionInfo){
		log.info("recordTaskProcess被调用,参数是：junZhuId:{}, 和type:{}, condittionInfo:{}",
				junZhuId, type, conditionInfo);
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if(list == null){
			return;
		}
		ZhuXian task = null;
		boolean toSend = false;
		for (WorkTaskBean b: list){
			task = zhuxianTaskMap.get(b.tid);
			if(task == null){
				continue;
			}
			if(task.getDoneType() != type){
				continue;
			}
			// 百战名次要求超过x名次，就算完成任务
			if(type == TaskData.baizhan_rank_n){
				if(task.getDoneCond() != null && conditionInfo != null &&
						Integer.parseInt(task.getDoneCond()) >= Integer.parseInt(conditionInfo))
				{
					conditionInfo = task.getDoneCond();
				}
			}
			// 获得的秘宝个数要求超过x个，就算完成任务
			// 君主等级升级大于等于x级,就算任务完成
			// 秘宝星级大于等于x级，就算完成任务
			// 秘宝等级大于等于X级，就算完成任务
			if(type == TaskData.get_x_mibao || 
					type == TaskData.junzhu_level_up ||
					type == TaskData.mibao_shengStar_x ||
					type == TaskData.mibao_shengji_x){
				if(task.getDoneCond() != null && conditionInfo != null &&
						Integer.parseInt(conditionInfo) >= Integer.parseInt(task.getDoneCond()))
				{
					conditionInfo = task.getDoneCond();
				}
			}
			if (task.getDoneCond().equals(conditionInfo) && b.progress == 0)
			{
				dealTask(junZhuId, b, type, task);
				toSend = true;
				break;	
			}
		}
		toSendTask(toSend, junZhuId);
	}

	/**
	 *  攻打关卡任务事件完成进行记录并且将下一步任务记录到数据库
	 */
	private void pveGuanQiaRecord(Long junZhuId, byte type, Integer gqId){
		recordTaskProcess(junZhuId, type, gqId + "");
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.EQUIP_ADD, this);
		EventMgr.regist(ED.PVE_GUANQIA, this);
		EventMgr.regist(ED.JUNZHU_KEJI_PROMOTE, this);
		EventMgr.regist(ED.get_item_finish, this);
		EventMgr.regist(ED.QIANG_HUA_FINISH, this);
		EventMgr.regist(ED.GET_START_PVE_AWARD, this);
		EventMgr.regist(ED.CHUANQI_GUANQIA_SUCCESS, this);
		EventMgr.regist(ED.FINISH_BAIZHAN_N, this);
		EventMgr.regist(ED.XILIAN_ONE_GONG, this);
		EventMgr.regist(ED.JINJIE_ONE_GONG, this);
		EventMgr.regist(ED.MIBAO_HECHENG, this);
		EventMgr.regist(ED.MIBAO_SEHNGXING, this);
		EventMgr.regist(ED.SUCCESS_BAIZHAN_N, this);
		/*
		 * 20150508
		 */
		EventMgr.regist(ED.get_x_mibao, this);
		EventMgr.regist(ED.mibao_shengji_x, this);
		EventMgr.regist(ED.mibao_shengStar_x, this);
		EventMgr.regist(ED.baizhan_rank_n, this);
		EventMgr.regist(ED.Join_LM, this);
		EventMgr.regist(ED.buy_tili_1_times, this);
		EventMgr.regist(ED.buy_tongbi_1_times, this);
		EventMgr.regist(ED.junzhu_level_up, this);
		
		/*
		 * 主线任务：20150630
		 */
		EventMgr.regist(ED.finish_youxia_x, this);
		EventMgr.regist(ED.finish_yunbiao_x, this);
		EventMgr.regist(ED.finish_jiebiao_x, this);
		EventMgr.regist(ED.tianfu_level_up_x, this);
		
		/*
		 *20150916 
		 */
		EventMgr.regist(ED.get_produce_weiWang, this);
		EventMgr.regist(ED.pay_weiWang, this);
		EventMgr.regist(ED.active_mibao_skill, this);
		EventMgr.regist(ED.saoDang, this);
		EventMgr.regist(ED.wear_fushi, this);
		EventMgr.regist(ED.mobai, this);
		EventMgr.regist(ED.jibai, this);
		EventMgr.regist(ED.fix_house, this);
		EventMgr.regist(ED.get_house_exp, this);
		EventMgr.regist(ED.have_total_guJuan, this);
		EventMgr.regist(ED.battle_huang_ye, this);
		EventMgr.regist(ED.lve_duo, this);
		EventMgr.regist(ED.pawnshop_buy, this);
		EventMgr.regist(ED.zhuangBei_x_qiangHua_N, this);
		EventMgr.regist(ED.get_pass_PVE_zhang_award, this);
		EventMgr.regist(ED.jinJie_jueSe_jiNeng, this);
		EventMgr.regist(ED.mibao_shengStar, this);
		EventMgr.regist(ED.go_youxia, this);
		EventMgr.regist(ED.qiandao, this);
		EventMgr.regist(ED.qiandao_get_v, this);
		EventMgr.regist(ED.miabao_x_star_n, this);
		EventMgr.regist(ED.get_achieve, this);
		
		/*
		 * 元宝探宝单， 十; 铜币探宝 单， 十
		 */
		EventMgr.regist(ED.tanbao_oneTimes, this);
		EventMgr.regist(ED.tanbao_tenTimes, this);
		EventMgr.regist(ED.tongbi_oneTimes, this);
		EventMgr.regist(ED.tongbi_oneTimes, this);
		EventMgr.regist(ED.active_taozhuang, this);
	}
	
	public void zhuangBeiTask(Long junZhuId, String param1, Bag<EquipGrid> equips, int which){
		List<WorkTaskBean> list = getTaskList(junZhuId);
		if(list == null){
			return;
		}
		ZhuXian task = null;
		boolean toSend = false;
		for (WorkTaskBean b: list){
			task = zhuxianTaskMap.get(b.tid);
			if(task == null){
				continue;
			}
			byte type = task.getDoneType();
			boolean yes = false;
			if(type == TaskData.EQUIP_ADD && which == 1){
				yes = task.getDoneCond().equals(param1);
			}else if( type == TaskData.one_quality_ok || type == TaskData.N_quality_ok){
				equips =  EquipMgr.inst.loadEquips(junZhuId);
				yes = isPinZhiOk(type, task.getDoneCond(), equips);
			}else if(type == TaskData.JINJIE_ONE_GONG && which == 2){
				yes = task.getDoneCond().equals(param1);
			}
			if(yes && b.progress == 0){
				dealTask(junZhuId, b, type, task);
				toSend = true;
			}
		}
		toSendTask(toSend, junZhuId);
	}

	public boolean isPinZhiOk(byte type, String doneCond, Bag<EquipGrid> equips){
		boolean yes = false;
		String[] condis = doneCond.split(":");
		if(condis == null){
			return false;
		}
		if(condis == null || condis.length != 2){
			return false;
		}
		switch(type){
			case TaskData.N_quality_ok:
				yes = isFinishTask40(equips, Integer.parseInt(condis[0]),
						Integer.parseInt(condis[1]));
				return yes;
			case TaskData.one_quality_ok:
				yes = isFinishTask41(equips, Integer.parseInt(condis[0]), 
						Integer.parseInt(condis[1]));
				return yes;
		}
		return false;
	}
	public boolean isFinishTask40(Bag<EquipGrid> equips, int number, int minPinzhi){
		List<EquipGrid> list = equips.grids;
		int count = 0;
		for(EquipGrid eg: list){
			if(eg == null){
				continue;
			}
			ZhuangBei zb = (ZhuangBei)TempletService.itemMap.get(eg.itemId);
			if(zb == null){
				continue;
			}
			// x件装备x品质以上
			if(zb.color >= minPinzhi){
				count ++;
			}
		}
		if(count >= number){
			return true;
		}
		return false;
	}
	
	public boolean isFinishTask41(Bag<EquipGrid> equips, int buwei, int pinzhi){
		List<EquipGrid> list = equips.grids;
		int value = -1;
		for(EquipGrid eg: list){
			if(eg == null){
				continue;
			}
			ZhuangBei zb = (ZhuangBei)TempletService.itemMap.get(eg.itemId);
			if(zb == null){
				continue;
			}
			if(zb.buWei == buwei){
				value = Math.max(zb.color, value);
			}
		}
		if(value >= pinzhi){
			return true;
		}
		return false;
	}
	
	public boolean isQiangHua_X_N(long jzId, int xCount, int nLevel){
		String where = "where userId = " + jzId;
		int realCount = 0;
		List<UserEquip> equipList = HibernateUtil.list(UserEquip.class, where);
		for(UserEquip e: equipList){
			if(e != null && e.getLevel() >= nLevel){
				realCount ++;
			}
		}
		if(realCount >= xCount){
			return true;
		}
		return false;
	}
}
