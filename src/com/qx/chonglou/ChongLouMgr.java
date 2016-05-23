package com.qx.chonglou;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ChonglouNpcTemp;
import com.manu.dynasty.template.ChonglouPveTemp;
import com.manu.dynasty.template.EnemyTemp;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.GuanQiaJunZhu;
import com.manu.dynasty.template.YouxiaOpenTime;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pve.PveMgr;
import com.qx.youxia.YouXiaMgr;

import qxmobile.protobuf.BattlePveResult;
import qxmobile.protobuf.BattlePveResult.AwardItem;
import qxmobile.protobuf.BattlePveResult.BattleResult;
import qxmobile.protobuf.ChongLouPve.ChongLouBattleInit;
import qxmobile.protobuf.ChongLouPve.ChongLouBattleResult;
import qxmobile.protobuf.ChongLouPve.ChongLouBattleResultResp;
import qxmobile.protobuf.ChongLouPve.ChongLouSaoDangAward;
import qxmobile.protobuf.ChongLouPve.ChongLouSaoDangResp;
import qxmobile.protobuf.ChongLouPve.MainInfoResp;
import qxmobile.protobuf.ZhanDou.DroppenItem;
import qxmobile.protobuf.ZhanDou.Group;
import qxmobile.protobuf.ZhanDou.Node;
import qxmobile.protobuf.ZhanDou.NodeProfession;
import qxmobile.protobuf.ZhanDou.NodeType;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;

public class ChongLouMgr {
	public static ChongLouMgr inst;
	public Logger logger = LoggerFactory.getLogger(ChongLouMgr.class); 
	public Map<Integer, ChonglouPveTemp> pveTempMap = null;
	public String CACHE_CHONGLOU_HIGHEST_LAYER = "chonglou_highest_layer:";
	
	/** 重楼关卡ncp，<npcId, list<>> **/
	public Map<Integer, List<ChonglouNpcTemp>> npcTempMap = null;
	
	public Map<Long, Map<Integer, List<AwardTemp>>> dropAwardMapBefore = new HashMap<Long, Map<Integer,List<AwardTemp>>>();
	
	public ChongLouMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		Map<Integer, ChonglouPveTemp> pveTempMap = new HashMap<Integer, ChonglouPveTemp>();
		List<ChonglouPveTemp> pveTemplist = TempletService.listAll(ChonglouPveTemp.class.getSimpleName());
		for(ChonglouPveTemp pt : pveTemplist) {
			pveTempMap.put(pt.layer, pt);
		}
		this.pveTempMap = pveTempMap;
		
		Map<Integer, List<ChonglouNpcTemp>> npcTempMap = new HashMap<Integer, List<ChonglouNpcTemp>>();
		List<ChonglouNpcTemp> npcTemplist = TempletService.listAll(ChonglouNpcTemp.class.getSimpleName());
		for(ChonglouNpcTemp nt : npcTemplist) {
			List<ChonglouNpcTemp> npcList = npcTempMap.get(nt.npcId);
			if(npcList == null) {
				npcList = new ArrayList<>();
				npcTempMap.put(nt.npcId, npcList);
			}
			npcList.add(nt);
		}
		this.npcTempMap = npcTempMap;
	}

	public void mainInfoRequest(int id, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		
		ChongLouRecord record = getChongLouRecord(junZhu);
		boolean refreshTime = DateUtils.isTimeToReset(record.lastBattleTime, CanShu.REFRESHTIME);
		if(refreshTime) {
			record.currentLevel = 1;
			HibernateUtil.save(record);
		}
		
		MainInfoResp.Builder response = MainInfoResp.newBuilder();
		response.setCurrentLv(record.currentLevel);
		response.setHistoryHighestLv(record.highestLevel);
		YouxiaOpenTime openTimeCfg = YouXiaMgr.inst.youxiaOpenTimeMap.get(101);//101代表千重楼
		if(openTimeCfg == null) {
			logger.error("千重楼配置错误，youxiaOpenTime找不到千重楼配置，id=101");
		} else {
			if(junZhu.level < openTimeCfg.openLevel) {
				response.setResult(1);
				session.write(response.build());
				return;
			}
		}
		response.setResult(0);
		response.setZuheSkill(record.zuheSkillId);
		session.write(response.build());
	}

	public void saoDang(int id, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		
		
		ChongLouSaoDangResp.Builder response = ChongLouSaoDangResp.newBuilder();
		
		// TODO 玩法是否开启

		ChongLouRecord record = HibernateUtil.find(ChongLouRecord.class, junZhu.id);
		if(record == null) {
			logger.error("重楼扫荡失败，还未打过任何楼层，不能扫荡");
			record = insertChongLouRecord(junZhu);
			response.setResult(3);
			sendChongLouSaoDangResp(session, response);
			return;
		}
		int start = record.currentLevel;
		int end   = record.highestLevel;
		if(start > end) {
			logger.error("重楼扫荡失败，君主:{}当前挑战层数:{}超过历史最大层数:{}，不能扫荡", junZhu.id, start, end);
			response.setResult(2);
			sendChongLouSaoDangResp(session, response);
			return;
		}
		
		List<AwardTemp> getAwardList = new ArrayList<AwardTemp>();
		for(; start <= end; start++) {
			ChonglouPveTemp pveTemp = pveTempMap.get(start);
			if(pveTemp == null) {
				logger.error("重楼扫荡错误，找不到层数为:{}的配置", start);
				continue;
			}
			List<AwardTemp> awards = AwardMgr.inst.getHitAwardList(pveTemp.awardId, ",", "=");
			AwardMgr.inst.battleAwardCounting(getAwardList, awards);
		}
		
		if(record.highestLevel == pveTempMap.size()) {
			record.currentLevel = record.highestLevel;
		} else {
			record.currentLevel = record.highestLevel + 1;
		}
		record.lastBattleTime = new Date();
		HibernateUtil.save(record);
		
		response.setResult(0);
		for(AwardTemp award : getAwardList) {
			ChongLouSaoDangAward.Builder awardBuilder = ChongLouSaoDangAward.newBuilder();
			awardBuilder.setItemType(award.getItemType());
			awardBuilder.setItemId(award.getItemId());
			awardBuilder.setItemNum(award.getItemNum());
			response.addAwards(awardBuilder.build());
		}
		sendChongLouSaoDangResp(session, response);
		EventMgr.addEvent(ED.done_qianChongLou, new Object[]{junZhu.id});
	}

	public void sendChongLouSaoDangResp(IoSession session, ChongLouSaoDangResp.Builder response) {
		ProtobufMsg protobufMsg = new ProtobufMsg();
		protobufMsg.id = PD.CHONG_LOU_SAO_DANG_RESP;
		protobufMsg.builder = response;
		session.write(protobufMsg);
	}

	public ChongLouRecord insertChongLouRecord(JunZhu junZhu) {
		ChongLouRecord record = new ChongLouRecord();
		record.currentLevel = 1;
		record.highestLevel = 0;
		record.zuheSkillId  = -1;
		record.junzhuId     = junZhu.id;
		HibernateUtil.insert(record);
		Redis.getInstance().set(CACHE_CHONGLOU_HIGHEST_LAYER + junZhu.id, String.valueOf(record.highestLevel));
		return record;
	}
	
	public void enterBattle(int id, IoSession session, Builder builder) {
		ChongLouBattleInit.Builder request = (qxmobile.protobuf.ChongLouPve.ChongLouBattleInit.Builder) builder;
		int layer = request.getLayer();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			PveMgr.inst.sendZhanDouInitError(session, "找不到君主信息");
			return;
		}

		ChonglouPveTemp pveTemp = pveTempMap.get(layer);
		if (pveTemp == null) {
			logger.error("千重楼战斗信息请求失败，找不到{}层的配置", layer);
			PveMgr.inst.sendZhanDouInitError(session, "数据配置错误1");
			return;
		}
		
		List<ChonglouNpcTemp> npcList = npcTempMap.get(pveTemp.npcId);
		if (npcList == null || npcList.size() == 0) {
			logger.error("千重楼战斗信息请求失败，楼层:{}怪物配置为空, npcId:{}", layer, pveTemp.npcId);
			PveMgr.inst.sendZhanDouInitError(session, "数据配置错误3");
			return;
		}
		
		ChongLouRecord record = getChongLouRecord(junZhu);
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		resp.setZhandouId(PveMgr.battleIdMgr.incrementAndGet());// 战斗id 后台使用
		resp.setMapId(pveTemp.sceneId);
		resp.setLimitTime(pveTemp.time);

		// 填充敌方数据
		List<AwardTemp> getAwardList = new ArrayList<AwardTemp>();
		// 填充己方数据（战斗数据和秘宝信息数据）
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<Node>();
		int selfFlagId = 1;
		int zuheId = record.zuheSkillId;
		PveMgr.inst.fillJunZhuDataInfo(resp, session, selfs, junZhu, selfFlagId++, zuheId, selfTroop);
		
		
		int index = 0;
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<Node>();
		Map<Integer, List<AwardTemp>> npcDropAward = new HashMap<Integer, List<AwardTemp>>();
		for (ChonglouNpcTemp npcTemp : npcList) {
			Node.Builder node = Node.newBuilder();
			NodeType nodeType = NodeType.valueOf(npcTemp.type);
			if (nodeType == null) {
				logger.error("千重楼战斗信息请求失败，nodeType与npcTemp的type值不一致，npcTemp.type:{}", npcTemp.type);
				continue;
			}
			NodeProfession nodeProfession = NodeProfession.valueOf(npcTemp.profession);
			if (nodeProfession == null) {
				logger.error("千重楼战斗信息请求失败，" + "nodeProfession与npcTemp的Profession值不一致，npcTemp.Profession:{}",
						npcTemp.profession);
				continue;
			}

			if (nodeType == NodeType.PLAYER) { // 模拟玩家npc
				GuanQiaJunZhu guanQiaJunZhu = PveMgr.inst.id2GuanQiaJunZhu.get(npcTemp.enemyId);
				if (guanQiaJunZhu == null) {
					logger.error("找不到id:{}的GuanQiaJunZhu配置", npcTemp.enemyId);
					return;
				}
				if (npcTemp.ifTeammate == 1) {
					PveMgr.inst.fillNPCPlayerDataInfo(selfs, guanQiaJunZhu, selfFlagId++, npcTemp);
				} else {
					PveMgr.inst.fillNPCPlayerDataInfo(enemys, guanQiaJunZhu, npcTemp.position, npcTemp);
				}
			} else {
				EnemyTemp enemyTemp = PveMgr.inst.id2Enemy.get(npcTemp.enemyId);
				if (enemyTemp == null) {
					logger.error("千重楼战斗信息请求失败，enemy表未发现id为:{}的配置", npcTemp.enemyId);
					continue;
				}
				node.setModleId(npcTemp.modelId);// npc模型id
				node.setNodeType(nodeType);
				node.setNodeProfession(nodeProfession);
				node.setHp(enemyTemp.getShengming() * npcTemp.lifebarNum);
				node.setNodeName(npcTemp.name + "");
				node.setHpNum(npcTemp.lifebarNum);
				node.setAppearanceId(npcTemp.modelApID);
				node.setNuQiZhi(0);
				node.setMibaoCount(0);
				node.setMibaoPower(0);
				node.setArmor(npcTemp.armor);
				node.setArmorMax(npcTemp.armorMax);
				node.setArmorRatio(npcTemp.armorRatio);
				GongjiType gongjiType = PveMgr.inst.id2GongjiType.get(npcTemp.gongjiType);
				PveMgr.inst.fillDataByGongjiType(node, gongjiType);
				PveMgr.inst.fillGongFangInfo(node, enemyTemp);

				String skills = npcTemp.skills;
				if (skills != null && !skills.equals("")) {
					String[] skillList = skills.split(",");
					for (String s : skillList) {
						int skillId = Integer.parseInt(s);
						PveMgr.inst.addNodeSkill(node, skillId);
					}
				}
				List<AwardTemp> npcAwardList = AwardMgr.inst.getHitAwardList(npcTemp.award, ",", "=");
				npcDropAward.put(npcTemp.position, npcAwardList);
				int size = npcAwardList.size();
				for (int i = 0; i < size; i++) {
					AwardTemp awardTemp = npcAwardList.get(i);
					DroppenItem.Builder dropItem = DroppenItem.newBuilder();
					dropItem.setId(index);
					dropItem.setCommonItemId(awardTemp.getItemId());
					dropItem.setNum(awardTemp.getItemNum());
					node.addDroppenItems(dropItem);
					getAwardList.add(awardTemp);
					index++;
				}
				if (npcTemp.ifTeammate == 1) {
					node.addFlagIds(selfFlagId++);
					selfs.add(node.build());
				} else {
					node.addFlagIds(npcTemp.position);
					enemys.add(node.build());
				}
			}
		}

		dropAwardMapBefore.put(junZhu.id, npcDropAward);
		logger.info("千重楼战斗信息请求成功，君主:{}进入千重楼{}层,可获得奖励:{}", junZhu.name, layer, getAwardList);
		enemyTroop.addAllNodes(enemys);
		enemyTroop.setMaxLevel(999);
		resp.setEnemyTroop(enemyTroop);

		selfTroop.addAllNodes(selfs);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(junZhu.id));
		resp.setSelfTroop(selfTroop);
		session.write(resp.build());
	}

	public void saveMibao4ChongLou(int zuheId, JunZhu junZhu) {
		ChongLouRecord record = getChongLouRecord(junZhu);
		record.zuheSkillId = zuheId;
		HibernateUtil.update(record);
	}

	public void battleResultReport(int id, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			PveMgr.inst.sendZhanDouInitError(session, "找不到君主信息");
			return;
		}
		
		ChongLouBattleResult.Builder request = (qxmobile.protobuf.ChongLouPve.ChongLouBattleResult.Builder) builder;
		int layer = request.getLayer();
		boolean result = request.getResult();
		
		ChonglouPveTemp pveTemp = pveTempMap.get(layer);
		if (pveTemp == null) {
			logger.error("千重楼战斗信息请求失败，找不到{}层的配置", layer);
			PveMgr.inst.sendZhanDouInitError(session, "数据配置错误1");
			return;
		}
		ChongLouRecord record = HibernateUtil.find(ChongLouRecord.class, junZhu.id);
		List<AwardTemp> getAwardList = new ArrayList<AwardTemp>();
		BattleResult.Builder response = BattleResult.newBuilder();
		int getTongbi = 0;
		int getExp = 0;
		if(result) {
			if(layer > record.highestLevel) {
				List<AwardTemp> firstAwardList = AwardMgr.inst.getHitAwardList(pveTemp.firstAwardID, ",", "=");
				AwardMgr.inst.battleAwardCounting(getAwardList, firstAwardList);
				logger.info("千重楼战斗结束，君主:{}层数:{}第一次打通，给予首次奖励:{}",junZhu.id, layer, firstAwardList);
				record.highestLevel = layer;
				record.highestLevelFirstTime = new Date();
				Redis.getInstance().set(CACHE_CHONGLOU_HIGHEST_LAYER + junZhu.id, String.valueOf(record.highestLevel));
				EventMgr.addEvent(ED.CHONGLOU_RANK_REFRESH, new Object[]{junZhu, layer});
			}
			List<Integer> droppenList = request.getDropeenItemNpcsList();
			Map<Integer, List<AwardTemp>> npcDropAwardMap = dropAwardMapBefore.get(junZhu.id);
			for (Integer npcPos : droppenList) {
				List<AwardTemp> posNpcDropAward = npcDropAwardMap.get(npcPos);
				if(posNpcDropAward != null) {
					AwardMgr.inst.battleAwardCounting(getAwardList, posNpcDropAward);
				}
			}
			
			List<AwardTemp> guanQiaAwardList = AwardMgr.inst.getHitAwardList(pveTemp.awardId, ",", "=");
			AwardMgr.inst.battleAwardCounting(getAwardList, guanQiaAwardList);
			for (AwardTemp award : getAwardList) {
				if (award.getItemId() == AwardMgr.ITEM_TONGBI_ID) {
					getTongbi += award.getItemNum();
				} else if (award.getItemId() == AwardMgr.ITEM_EXP_ID) {
					getExp += award.getItemNum();
				} else {
					AwardItem.Builder awardBuilder = AwardItem.newBuilder();
					awardBuilder.setAwardItemType(award.getItemType());
					awardBuilder.setAwardId(award.getItemId());
					awardBuilder.setAwardNum(award.getItemNum());
					int iconId = AwardMgr.inst.getItemIconid(award.getItemType(), award.getItemId());
					awardBuilder.setAwardIconId(iconId);
					response.addAwardItems(awardBuilder.build());
				}
			}
			
			if(pveTempMap.get(layer+1) != null) {
				record.currentLevel = layer + 1;
			}
		}
		record.lastBattleTime = new Date();
		HibernateUtil.save(record);
		logger.info("千重楼战斗结束，君主:{}层数:{}结果:{}",junZhu.id, layer, result?"胜利":"失败");
		for (AwardTemp award : getAwardList) {
			logger.info("千重楼战斗结算奖励，君主:{} 得到奖励 awardId:{}, 类型:{},id:{},数量:{}", 
					junZhu.id, award.getAwardId(), 
					award.getItemType(), award.getItemId(), award.getItemNum());
			AwardMgr.inst.giveReward(session, award, junZhu, false);
		}
		response.setMoney(getTongbi);	
		response.setExp(getExp);
		ProtobufMsg protobufMsg = new ProtobufMsg();
		protobufMsg.builder = response;
		protobufMsg.id = PD.CHONG_LOU_BATTLE_REPORT_REQP;
		session.write(protobufMsg);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		EventMgr.addEvent(ED.done_qianChongLou, new Object[]{junZhu.id});
	}

	public ChongLouRecord getChongLouRecord(JunZhu jz) {
		ChongLouRecord record = HibernateUtil.find(ChongLouRecord.class, jz.id);
		if(record == null) {
			record = insertChongLouRecord(jz);
		}
		return record;
	}
	
	public int getChongLouHighestLayer(JunZhu junzhu) {
		String layer = Redis.getInstance().get(CACHE_CHONGLOU_HIGHEST_LAYER + junzhu.id);
		if(layer == null) {
			ChongLouRecord record = getChongLouRecord(junzhu);
			return record.highestLevel;
		}
		return Integer.parseInt(layer);
	}
	
}
