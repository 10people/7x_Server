package com.qx.junzhu;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections.map.LRUMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.Fuwen;
import com.manu.dynasty.template.FuwenTab;
import com.manu.dynasty.template.HeroGrow;
import com.manu.dynasty.template.JiNengPeiYang;
import com.manu.dynasty.template.JunzhuShengji;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MiBaoExtraAttribute;
import com.manu.dynasty.template.MiBaoNew;
import com.manu.dynasty.template.MibaoStar;
import com.manu.dynasty.template.Mishu;
import com.manu.dynasty.template.QiangHua;
import com.manu.dynasty.template.Talent;
import com.manu.dynasty.template.TalentAttribute;
import com.manu.dynasty.template.TaoZhuang;
import com.manu.dynasty.template.VIP;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.activity.XianShiActivityMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceBeanDao;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.alliance.building.LMKJBean;
import com.qx.alliance.building.LMKJBeanDao;
import com.qx.alliance.building.LMKJJiHuo;
import com.qx.bag.Bag;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.equip.domain.UserEquip;
import com.qx.equip.domain.UserEquipDao;
import com.qx.equip.jewel.JewelMgr;
import com.qx.equip.web.UEConstant;
import com.qx.equip.web.UserEquipAction;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.fuwen.FuWenBean;
import com.qx.fuwen.FuWenDao;
import com.qx.fuwen.FuwenMgr;
import com.qx.guojia.GuoJiaMgr;
import com.qx.hero.HeroMgr;
import com.qx.hero.WuJiang;
import com.qx.huangye.HYResourceNpc;
import com.qx.huangye.HYTreasure;
import com.qx.huangye.HYTreasureDao;
import com.qx.jinengpeiyang.JNBean;
import com.qx.jinengpeiyang.JiNengPeiYangMgr;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MibaoMgr;
import com.qx.mibao.v2.MiBaoV2Mgr;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.purchase.PurchaseMgr;
import com.qx.pve.BuZhenMibaoBean;
import com.qx.pvp.PVPConstant;
import com.qx.pvp.PvpBean;
import com.qx.ranking.RankingMgr;
import com.qx.robot.RobotSession;
import com.qx.timeworker.FunctionID;
import com.qx.vip.PlayerVipInfo;
import com.qx.vip.VipMgr;
import com.qx.world.GameObject;

import log.ActLog;
import log.CunLiangLog;
import log.OurLog;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.FuWen.JunzhuAttr;
import qxmobile.protobuf.JunZhuProto.ActivateTaoZhReq;
import qxmobile.protobuf.JunZhuProto.ActivateTaoZhResp;
import qxmobile.protobuf.JunZhuProto.JunZhuAddPointReq;
import qxmobile.protobuf.JunZhuProto.JunZhuAttPointRet;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoSpecifyReq;
import qxmobile.protobuf.JunZhuProto.TaoZhuangResp;
import qxmobile.protobuf.MibaoProtos.MibaoInfo;
import qxmobile.protobuf.MibaoProtos.MibaoInfoResp;
import qxmobile.protobuf.Ranking.JunZhuInfo;

/**
 * @author 康建虎
 * 
 */
public class JunZhuMgr extends EventProc {
	public static JunZhuMgr inst;
	public static int TiLiMax = CanShu.TILI_JILEI_SHANGXIAN;
	public static Logger log = LoggerFactory.getLogger(JunZhuMgr.class.getSimpleName());
	/*
	 * 根据封测需求
	 */
	public static final boolean isInitVip = true;
	public static boolean useJzInfoCache = false;
	public static Map<Long, JunZhuInfoRet.Builder> jzInfoCache = Collections.synchronizedMap(new LRUMap(5000));

	public static Map<Integer, TaoZhuang> taoZhuangMap = new HashMap<Integer, TaoZhuang>();

	public static final int taoZhuangType = 1;
	public static final int taoZhuangQiangHuaType = 2;
	public static int minTaoZhuangId = 100;
	public static int minTaoZhuangQHId = 100;
	
	public JunZhuMgr() {
		inst = this;

		List<TaoZhuang> listTa = TempletService.listAll(TaoZhuang.class.getSimpleName());
		for(TaoZhuang t: listTa){
			taoZhuangMap.put(t.id, t);
			if(t.type == taoZhuangType){
				minTaoZhuangId = MathUtils.getMin(t.id, minTaoZhuangId);
			}else if(t.type == taoZhuangQiangHuaType){
				minTaoZhuangQHId = MathUtils.getMin(t.id, minTaoZhuangQHId);
			}
		}
	}

	public void sendMainInfo(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		if(session.removeAttribute("needSendChengHao") != null){
			ChenghaoMgr.inst.sendCur(0, session, null);
		}
		//客户端的请求，总是使用缓存
		if(useJzInfoCache){
			qxmobile.protobuf.JunZhuProto.JunZhuInfoRet.Builder b = jzInfoCache.get(junZhuId);
			if(b!=null){
				session.write(b.build());
				log.info("use cache info for {}", junZhuId);
				return;
			}
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId.longValue());
		sendMainInfo(session,jz);
		// 天赋的显示通知
//		TalentMgr.instance.noticeTalentCanLevUp(jz);
		// 符文的显示通知
		EventMgr.addEvent(jz.id,ED.FUSHI_PUSH, jz);
	}

	public void sendMainInfo(IoSession session) {
		JunZhu junzhu = getJunZhu(session);
		sendMainInfo(session, junzhu);
	}
	public void sendMainInfo(IoSession session, JunZhu junzhu) {
		sendMainInfo(session, junzhu, true);
	}
	public void sendMainInfo(IoSession session, JunZhu junzhu,boolean calcAtt) {
		if (junzhu == null)
			return;
		JunZhuInfoRet.Builder b = buildMainInfo(junzhu,session,calcAtt);
		session.write(b.build());
		jzInfoCache.put(junzhu.id, b);
		log.info("send junzhu info {} hp {}", junzhu.name, b.getShengMing());
//		HibernateUtil.update(junzhu);
	}

	public JunZhuInfoRet.Builder buildMainInfo(JunZhu junzhu, IoSession session,boolean calcAtt) {
		// 以后需要缓存，不用总是计算
		if(calcAtt){
			calcJunZhuTotalAtt(junzhu);
		}
		JunZhuInfoRet.Builder b = JunZhuInfoRet.newBuilder();
		b.setExp(junzhu.exp);
		b.setGender(junzhu.gender);
		b.setId(junzhu.id);
		b.setLevel(junzhu.level);
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(junzhu.id);
		b.setLianMengId(member == null ? 0 : member.lianMengId);
		session.setAttribute(SessionAttKey.LM_ZHIWU, member == null ? 0 : member.title);
		b.setName(junzhu.name);
		b.setGongJi(junzhu.gongJi);
		b.setFangYu(junzhu.fangYu);
		b.setShengMing(junzhu.shengMingMax);
		b.setWqSH(junzhu.wqSH);
		b.setWqJM(junzhu.wqJM);
		b.setWqBJ(junzhu.wqBJ);
		b.setWqRX(junzhu.wqRX);// 武器暴击加深
		b.setWqBJL(20);// FIXME
		b.setJnSH(junzhu.jnSH);// 技能伤害加深
		b.setJnJM(junzhu.jnJM);// 技能伤害加深
		b.setJnBJ(junzhu.jnBJ);// 技能伤害加深
		b.setJnRX(junzhu.jnRX);// 技能伤害加深
		b.setJnBJL(20);// FIXME
		//TODO 以下 1.1可能不加入
		//5个属性
		//TODO 以上 1.1可能不加入
		ExpTemp expTemp = TempletService.getInstance().getExpTemp(1,
				junzhu.level);
		b.setExpMax(expTemp == null ? Integer.MAX_VALUE : expTemp.needExp);
		b.setJinBi(junzhu.tongBi);
		b.setYuanBao(junzhu.yuanBao);
		b.setTili(junzhu.tiLi);
		b.setTiLipurchaseTime(PurchaseMgr.inst.getTiLiTime(junzhu));
		b.setTiLiMax(junzhu.tiLiMax);
		b.setJunXian(PVPConstant.XIAO_ZU_JI_BIE);
		{
			PvpBean bean = HibernateUtil.find(PvpBean.class, junzhu.id);
			if (bean != null) {
				// BaiZhan bz = PvpMgr.inst.baiZhanMap.get(bean.junXianLevel);
				// String jxStr = bz == null ? "???" :
				// HeroService.getNameById(bz.name);
				b.setJunXian(bean.junXianLevel);
			}
		}
		b.setVipLv(junzhu.vipLevel);
		b.setGuoJiaId(junzhu.guoJiaId);
		b.setNuQiValue(MibaoMgr.inst.getShowChuShiNuQi(junzhu.id));
		TalentAttr ta = HibernateUtil.find(TalentAttr.class, junzhu.id);
		b.setWuYiJingQi(ta == null?0: ta.wuYiJingQi);
		b.setTiPoJingQi(ta == null?0: ta.tiPoJingQi);
		/*
		// FIXME 先这样改
		int zhanLiAddMibaoAttrBefore = 0; //getZhanli(junzhu);
//		calcMibaoAttr(junzhu, b);
		int zhanLiAddMibaoAttrAfter = getZhanli(junzhu);
		// 没有加秘宝属性前的战力值
		b.setZhanLiMibao(zhanLiAddMibaoAttrAfter - zhanLiAddMibaoAttrBefore);
		// 加了秘宝属性之后的总战力
		b.setZhanLi(zhanLiAddMibaoAttrAfter);
		*/
		int zhanli = getZhanli(junzhu);
		b.setZhanLi(zhanli);
		// 向redis中添加玩家战力变化情况
		resetZhanliRedis(junzhu, zhanli);
		/*
		 * 没有使用的协议字段
		 */
		b.setGongjiMibao(0);
		b.setFangYuMibao(0);
		b.setShengMingMibao(0);
		b.setZhanLiMibao(0);

		return b;
	}

//	private void calcMibaoAttr(JunZhu junzhu,
//			qxmobile.protobuf.JunZhuProto.JunZhuInfoRet.Builder b) {
//		List<MiBaoDB> mibaoList = HibernateUtil.list(MiBaoDB0.class,
//				" where ownerId=" + junzhu.id);
//		int gongjiMibao = 0;
//		int fangyuMibao = 0;
//		int shengmingMibao = 0;
//		for (MiBaoDB mibaoDB : mibaoList) {
//			if (mibaoDB.getLevel() <= 0) {
//				continue;
//			}
//			MiBao mibaoConf = MibaoMgr.mibaoMap.get(mibaoDB.getMiBaoId());
//			if (mibaoConf == null) {
//				log.error("找不到秘宝配置，mibaoId:{}", mibaoDB.getMiBaoId());
//				continue;
//			}
////			boolean lock = MibaoMgr.inst.isLock(mibaoConf.unlockType,
////					mibaoConf.unlockValue, junzhu);
////			if (lock) {
////				continue;
////			}
//			MibaoStar mibaoStar = MibaoMgr.inst.mibaoStarMap.get(mibaoDB
//					.getStar());
//			if (mibaoStar == null) {
//				log.error("找不到对应的秘宝星级配置，mibaoDbId:{}, mibaoStar:{}",
//						mibaoDB.getDbId(), mibaoDB.getStar());
//				continue;
//			}
//			float chengZhang = mibaoStar.getChengzhang();
//			int level = mibaoDB.getLevel();
//			int gongji = MibaoMgr.inst.clacMibaoAttr(chengZhang,
//					mibaoConf.getGongji(), mibaoConf.gongjiRate, level);
//			int fangyu = MibaoMgr.inst.clacMibaoAttr(chengZhang,
//					mibaoConf.getFangyu(), mibaoConf.fangyuRate, level);
//			int shengming = MibaoMgr.inst.clacMibaoAttr(chengZhang,
//					mibaoConf.getShengming(), mibaoConf.shengmingRate,
//					level);
//			junzhu.gongJi += gongji;
//			junzhu.fangYu += fangyu;
//			junzhu.shengMingMax += shengming;
//			/*
//			 * 秘宝等级达到一定程度，会有额外属性加成
//			 */
//			List<MiBaoExtraAttribute> mibaoexA = MibaoMgr.inst.
//					getAddAttrList(mibaoDB.getMiBaoId(), mibaoDB.getLevel());
//			for(MiBaoExtraAttribute m: mibaoexA){
//				addAttrToX(m.shuxing, m.Num, junzhu);
//			}
//		
//			gongjiMibao += gongji;
//			fangyuMibao += fangyu;
//			shengmingMibao += shengming;
//
//			b.setGongJi(b.getGongJi() + gongji);
//			b.setFangYu(b.getFangYu() + fangyu);
//			b.setShengMing(b.getShengMing() + shengming);
//		}
//		b.setGongjiMibao(gongjiMibao);
//		b.setFangYuMibao(fangyuMibao);
//		b.setShengMingMibao(shengmingMibao);
//	}

	public void calcLianMengKeJi(JunZhu jz){
		AlliancePlayer player = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (player == null || player.lianMengId <= 0) {
			//log.info("获取玩家联盟失败，玩家:{}没有联盟", jz.id);
			return;
		}
		AllianceBean guild = AllianceBeanDao.inst.getAllianceBean(player.lianMengId);
		if (guild == null) {
			//log.error("获取玩家联盟失败，联盟{}不存在", player.lianMengId);
			return;
		}
		int lmId = guild.id;
		LMKJBean bean =LMKJBeanDao.inst.getBean(lmId);
		if(bean == null){
			bean = new LMKJBean();
			JianZhuMgr.inst.fillDefaultLevel(lmId, bean);
		}
		List<LianMengKeJi> list = TempletService.listAll(LianMengKeJi.class.getSimpleName());
		if(list == null){
			return;
		}
		LMKJJiHuo lmkjJiHuo = null;
		// 联盟科技对于盟主或者副盟主，直接按科技研究的最高等级来进行属性加成计算
		if(player.title == AllianceMgr.TITLE_LEADER || player.title == AllianceMgr.TITLE_DEPUTY_LEADER) {
			lmkjJiHuo = new LMKJJiHuo();
			lmkjJiHuo.type_101 = Integer.MAX_VALUE;
			lmkjJiHuo.type_102 = Integer.MAX_VALUE;
			lmkjJiHuo.type_103 = Integer.MAX_VALUE;
			lmkjJiHuo.type_104 = Integer.MAX_VALUE;
			lmkjJiHuo.type_105 = Integer.MAX_VALUE;
			lmkjJiHuo.type_106 = Integer.MAX_VALUE;
			lmkjJiHuo.type_107 = Integer.MAX_VALUE;
			lmkjJiHuo.type_108 = Integer.MAX_VALUE;
			lmkjJiHuo.type_109 = Integer.MAX_VALUE;
			lmkjJiHuo.type_110 = Integer.MAX_VALUE;
			lmkjJiHuo.type_111 = Integer.MAX_VALUE;
			lmkjJiHuo.type_301 = Integer.MAX_VALUE;
			lmkjJiHuo.type_204 = Integer.MAX_VALUE;
			lmkjJiHuo.type_205 = Integer.MAX_VALUE;
		} else if(player.title == AllianceMgr.TITLE_MEMBER) {
			lmkjJiHuo = HibernateUtil.find(LMKJJiHuo.class, jz.id);
			if(lmkjJiHuo == null){
				lmkjJiHuo = new LMKJJiHuo();
				JianZhuMgr.inst.fillDefaultLMKJJiHuo(lmkjJiHuo);
			}
		}
		
		for(LianMengKeJi keJiCfg : list){
			switch(keJiCfg.type){
			case 101:
				if(keJiCfg.level == Math.min(bean.type_101, lmkjJiHuo.type_101)){
					jz.gongJi += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 102:
				if(keJiCfg.level == Math.min(bean.type_102, lmkjJiHuo.type_102)){
					jz.fangYu += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 103:
				if(keJiCfg.level == Math.min(bean.type_103, lmkjJiHuo.type_103)){
					jz.shengMingMax += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 104:
				if(keJiCfg.level == Math.min(bean.type_104, lmkjJiHuo.type_104)){
					jz.wqSH += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 105:
				if(keJiCfg.level == Math.min(bean.type_105, lmkjJiHuo.type_105)){
					jz.wqBJ += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 106:
				if(keJiCfg.level == Math.min(bean.type_106, lmkjJiHuo.type_106)){
					jz.jnSH += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 107:
				if(keJiCfg.level == Math.min(bean.type_107, lmkjJiHuo.type_107)){
					jz.jnBJ += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 108:
				if(keJiCfg.level == Math.min(bean.type_108, lmkjJiHuo.type_108)){
					jz.wqJM += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 109:
				if(keJiCfg.level == Math.min(bean.type_109, lmkjJiHuo.type_109)){
					jz.wqRX += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 110:
				if(keJiCfg.level == Math.min(bean.type_110, lmkjJiHuo.type_110)){
					jz.jnJM += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			case 111:
				if(keJiCfg.level == Math.min(bean.type_111, lmkjJiHuo.type_111)){
					jz.jnRX += keJiCfg.value1;
//					log.info("加了：type：{}， level：{}", k.type, k.level);
				}
				break;
			}
		}
	}
	
	public void calcTotalJunZhuAtt(JunZhu junzhu, Bag<EquipGrid> bag){
		//君主
		calcJunZhuLevel(junzhu);
		// 武器(包括套装：套装品质、套装强化)
		calcEquipAtt(junzhu, bag);
		// 秘宝
		calcMiBaoAtt(junzhu);
		// 添加天赋属性 20150616 by wangZhuan
		calcTalentAttr(junzhu);
		// 添加符文属性 20150909 by hejincheng
		calcFuwenAttr(junzhu);
		// 联盟科技
		calcLianMengKeJi(junzhu);
	}
	public void calcJunZhuTotalAtt(JunZhu junzhu) {
		//君主
		calcJunZhuLevel(junzhu);
		// 武器(包括套装：套装品质、套装强化)
		Bag<EquipGrid> bag = EquipMgr.inst.loadEquips(junzhu.id);
		calcEquipAtt(junzhu, bag);
		// 秘宝(包括秘宝高等级引起的额外属性加成)
		calcMiBaoAtt(junzhu);
		// 添加天赋属性 20150616 by wangZhuan
		calcTalentAttr(junzhu);
		// 添加符文属性 20150909 by hejincheng
		calcFuwenAttr(junzhu);
		// 联盟科技
		calcLianMengKeJi(junzhu);
		//
		calcNewMiBao(junzhu);
	}

	public void calcNewMiBao(JunZhu jz) {
		List<MiBaoNew> list = TempletService.listAll(MiBaoNew.class.getSimpleName());
		if(list==null){
			return;
		}
		IoSession session = AccountManager.sessionMap.get(jz.id);
		boolean showRed = false;
		AtomicBoolean optional = new AtomicBoolean(false);
		MibaoInfoResp.Builder resp = MiBaoV2Mgr.inst.getMibaoInfoResp(jz,optional);
		int miShuLv = resp.getLevelPoint();
		//当前秘术等级之前的秘宝，就是上一套秘宝，比如秘术等级是2，则品质是1的秘宝全部由作用。
		for(MiBaoNew conf : list){
			if(conf.pinzhi<=miShuLv){
				jz.gongJi += conf.gongji;
				jz.fangYu += conf.fangyu;
				jz.shengMingMax += conf.shengming;
			}
		}
		//
		List<Mishu> listMS = TempletService.listAll(Mishu.class.getSimpleName());
		if(listMS==null){
			listMS = Collections.emptyList();
		}
		//秘术，激活的秘术，没有时是0，然后是1...，配置里的品质是从1开始的。
		for(Mishu conf : listMS){
			if(conf.pinzhi<=miShuLv){
				jz.wqSH += conf.wqSH;
				jz.wqJM += conf.wqJM;
				jz.gongJi += conf.gongji;
				jz.fangYu += conf.fangyu;
				jz.shengMingMax += conf.shengming;
				//log.info("add mishu att");
			}
		}
		//当前收集中的秘宝，若激活了则增加属性。
		for(MibaoInfo inf : resp.getMiBaoListList()){
			if(inf.getStar()>0){//已激活
				MiBaoNew conf = MiBaoV2Mgr.inst.confMap.get(inf.getMiBaoId());
				if(conf == null)continue;
				jz.gongJi += conf.gongji;
				jz.fangYu += conf.fangyu;
				jz.shengMingMax += conf.shengming;
			}else if(inf.getNeedSuipianNum()<=inf.getSuiPianNum()){
				showRed = true;
			}
		}
		if(!showRed && optional.get()){
			showRed = true;
		}
		if(session != null && showRed){
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.MiBaoNEW);
		}
	}

	public int getAllMibaoProvideZhanli(JunZhu realJZ) {
		/*JunZhu cloneJZ = realJZ.clone();
		//君主
		calcJunZhuLevel(cloneJZ);
		// 武器(包括套装：套装品质、套装强化)
		Bag<EquipGrid> bag = EquipMgr.inst.loadEquips(cloneJZ.id);
		calcEquipAtt(cloneJZ, bag);
		// 添加天赋属性 20150616 by wangZhuan
		calcTalentAttr(cloneJZ);
		// 添加符文属性 20150909 by hejincheng
		calcFuwenAttr(cloneJZ);
		// 联盟科技
		calcLianMengKeJi(cloneJZ);

		int addMibaoBefore = getZhanli(cloneJZ);
		// 秘宝(包括秘宝高等级引起的额外属性加成)
		calcMiBaoAtt(cloneJZ);
		int addMibaoAfter = getZhanli(cloneJZ);
		return addMibaoAfter - addMibaoBefore;*/
		return 0;
	}
	
	public void calcFuwenAttr(JunZhu jz) {
		for(Map.Entry<Integer, FuwenTab> entry : FuwenMgr.inst.fuwenTabMap.entrySet()) {
			if(jz.level < entry.getValue().level) {
				continue;
			}
			List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenByTab(jz.id, entry.getKey());
			JunzhuAttr.Builder attr = FuwenMgr.inst.getFuwenAttr(fuWenBeanList);
			jz.gongJi += attr.getGongji();
			jz.fangYu += attr.getFangyu();
			jz.shengMingMax += attr.getShengming();
			jz.wqSH += attr.getWqSH();
			jz.wqJM += attr.getWqJM();
			jz.wqBJ += attr.getWqBJ();
			jz.wqRX += attr.getWqRX();
			jz.jnSH += attr.getJnSH();
			jz.jnJM += attr.getJnJM();
			jz.jnBJ += attr.getJnBJ();
			jz.jnRX += attr.getJnRX();
		}
	}

	public void calcTalentAttr(JunZhu junzhu) {
//		String where = "where junZhuId = " + junzhu.id;
		List<TalentPoint> listT = TalentMgr.instance.getTalentPointList(junzhu.id);
//				HibernateUtil.list(TalentPoint.class, where);
		Talent tData = null;
		TalentAttribute taAttrData = null;
		for (TalentPoint t : listT) {
			tData = TalentMgr.talentMap.get(t.point);
			if (tData == null) {
				continue;
			}
			int attributeId = tData.attributeId;
			taAttrData = TalentMgr.instance.getTalentAttrItem(attributeId,
					t.level);
			if (taAttrData == null) {
				continue;
			}
			junzhu.gongJi += taAttrData.gongji;
			junzhu.fangYu += taAttrData.fangyu;
			junzhu.shengMingMax += taAttrData.shengming;
			junzhu.wqSH += taAttrData.wqSH;
			junzhu.wqJM += taAttrData.wqJM;
			junzhu.wqBJ += taAttrData.wqBJ;
			junzhu.wqRX += taAttrData.wqRX;
			junzhu.jnBJ += taAttrData.jnBJ;
			junzhu.jnJM += taAttrData.jnJM;
			junzhu.jnRX += taAttrData.jnRX;
			junzhu.jnSH += taAttrData.jnSH;
		}
	}

	public void calcJunZhuLevel(JunZhu junzhu){
		if (junzhu.level <= 0) {
			log.error("君主等级错误{}", junzhu.level);
			return;
		}
		long time = System.currentTimeMillis();
		JunzhuShengji conf = getJunzhuShengjiByLevel(junzhu.level);
		if (conf == null) {
			log.error("junZhuShengji配置有误 ,找不到君主等级{}的配置信息", junzhu.level);
			return;
		}
		junzhu.tiLiMax = conf.tilicao;
		junzhu.gongJi = conf.gongji;
		junzhu.fangYu = conf.fangyu;
		junzhu.wqSH = conf.wqSH;
		junzhu.wqJM = conf.wqJM;
		junzhu.wqBJ = conf.wqBJ;
		junzhu.wqRX = conf.wqRX;
		junzhu.jnSH = conf.jnSH;
		junzhu.jnJM = conf.jnJM;
		junzhu.jnBJ = conf.jnBJ;
		junzhu.jnRX = conf.jnRX;
		junzhu.shengMingMax = conf.shengming;
	}
//	public void calcAtt(JunZhu junzhu, Bag<EquipGrid> bag) {
//		if (junzhu.level <= 0) {
//			log.error("君主等级错误{}", junzhu.level);
//			return;
//		}
//		long time = System.currentTimeMillis();
//		JunzhuShengji conf = getJunzhuShengjiByLevel(junzhu.level);
//		if (conf == null) {
//			log.error("junZhuShengji配置有误 ,找不到君主等级{}的配置信息", junzhu.level);
//			return;
//		}
//		junzhu.tiLiMax = conf.tilicao;
//		junzhu.gongJi = conf.gongji;
//		junzhu.fangYu = conf.fangyu;
//		junzhu.wqSH = conf.wqSH;
//		junzhu.wqJM = conf.wqJM;
//		junzhu.wqBJ = conf.wqBJ;
//		junzhu.wqRX = conf.wqRX;
//		junzhu.jnSH = conf.jnSH;
//		junzhu.jnJM = conf.jnJM;
//		junzhu.jnBJ = conf.jnBJ;
//		junzhu.jnRX = conf.jnRX;
//		junzhu.shengMingMax = conf.shengming;
////		log.info("------calcBase-----{}", System.currentTimeMillis() - time);
//		// 装备
//		time = System.currentTimeMillis();
//		calcEquipAtt(junzhu, bag);
////		log.info("------calcEquipAtt-----{}", System.currentTimeMillis() - time);
//	}

	// 计算秘宝的属性
	public void calcMiBaoAtt(JunZhu junzhu){//, List<MiBaoDB> mbs) {
		List<MiBaoDB> mbs = MibaoMgr.inst.getActiveMibaosFromDB(junzhu.id);
		if (mbs != null && mbs.size() != 0) {
			for (MiBaoDB mid : mbs) {
				if (mid.level <= 0) {
					continue;
				}
				MiBao mi = MibaoMgr.mibaoMap.get(mid.miBaoId);
				if (mi == null) {
					log.error("找不到秘宝配置，mibaoId:{}", mid.miBaoId);
					continue;
				}
				MibaoStar mibaoStar = MibaoMgr.inst.mibaoStarMap.get(mid
						.star);
				if (mibaoStar == null) {
					log.error("找不到对应的秘宝星级配置，mibaoDbId:{}, mibaoStar:{}",
							mid.dbId, mid.star);
					continue;
				}
				float chengZhang = mibaoStar.chengzhang;
				int level = mid.level;
				int gongji = MibaoMgr.inst.clacMibaoAttr(chengZhang,
						mibaoStar.gongji, mi.gongjiRate, level);
				int fangyu = MibaoMgr.inst.clacMibaoAttr(chengZhang,
						mibaoStar.fangyu, mi.fangyuRate, level);
				int shengming = MibaoMgr.inst.clacMibaoAttr(chengZhang,
						mibaoStar.shengming, mi.shengmingRate, level);
				junzhu.gongJi += gongji;
				junzhu.fangYu += fangyu;
				junzhu.shengMingMax += shengming;
				
				/*
				 * 秘宝等级达到一定程度，会有额外属性加成
				 */
				List<MiBaoExtraAttribute> mibaoexA = MibaoMgr.inst.
						getAddAttrList(mid.miBaoId, mid.level);
				for(MiBaoExtraAttribute m: mibaoexA){
					addAttrToX(m.shuxing, m.Num, junzhu);
				}
			}
		}
	}
	
	public void addAttrToX(String x1, int addValue, JunZhu jz){
		char x = x1.charAt(0);
		switch(x){
		case 'A':
			 jz.wqSH += addValue;
			 return;
		case 'B':
			jz.wqJM += addValue;
			return;
		case 'C':
			jz.wqBJ += addValue;
			return;
		case 'D':
			jz.wqRX += addValue;
			return;
		case 'E':
			jz.jnSH += addValue;
			return;
		case 'F':
			jz.jnJM += addValue;
			return;
		case 'G':
			jz.jnBJ += addValue;
			return;
		case 'H':
			jz.jnRX += addValue;
			return;
		default:
			log.error("没有参数是：{}的 属性", x1);
			break;
		}
	}
	public void calcEquipAtt(JunZhu junzhu, Bag<EquipGrid> bag) {
		List<EquipGrid> list = bag.grids;
		int size = list.size();
		int cnt = Math.min(9, size);// list.size();
		TempletService template = TempletService.getInstance();
		int minPinZhi = 100;
		int minQHlv = 100;
		for (int i = 0; i < cnt; i++) {
			EquipGrid gd = list.get(i);
			if (gd == null || gd.itemId <= 0) {
				minPinZhi = 0;
				minQHlv = 0;
				continue;
			}
			BaseItem equip = TempletService.itemMap.get(gd.itemId);
			if ((equip==null)||equip.getType() != BaseItem.TYPE_EQUIP) {
				continue;
			}
			// 装备基础属性
			ZhuangBei zb = (ZhuangBei) equip;
			minPinZhi = Math.min(minPinZhi, zb.pinZhi);
			junzhu.gongJi += zb.getGongji();
			junzhu.fangYu += zb.getFangyu();
			junzhu.shengMingMax += zb.getShengming();
			//以下1.0版本改变洗练逻辑
			UserEquip ue = gd.instId>0 ? UserEquipDao.find(junzhu.id, gd.instId) : null;
			if (UserEquipAction.instance.hasEquipTalent(ue,zb.getId(),UEConstant.wqSH)) {
				junzhu.wqSH += zb.getWqSH();
			}
			if (UserEquipAction.instance.hasEquipTalent(ue,zb.getId(),UEConstant.wqJM)) {
				junzhu.wqJM += zb.getWqJM();
			}
			if (UserEquipAction.instance.hasEquipTalent(ue,zb.getId(),UEConstant.wqBJ)) {
				junzhu.wqBJ += zb.getWqBJ();
			}
			if (UserEquipAction.instance.hasEquipTalent(ue,zb.getId(),UEConstant.wqRX)) {
				junzhu.wqRX += zb.getWqRX();
			}
			if (UserEquipAction.instance.hasEquipTalent(ue,zb.getId(),UEConstant.jnSH)) {
				junzhu.jnSH += zb.getJnSH();
			}
			if (UserEquipAction.instance.hasEquipTalent(ue,zb.getId(),UEConstant.jnJM)) {
				junzhu.jnJM += zb.getJnJM();
			}
			if (UserEquipAction.instance.hasEquipTalent(ue,zb.getId(),UEConstant.jnBJ)) {
				junzhu.jnBJ += zb.getJnBJ();
			}
			if (UserEquipAction.instance.hasEquipTalent(ue,zb.getId(),UEConstant.jnRX)) {
				junzhu.jnRX += zb.getJnRX();
			}
		
			if (gd.instId <= 0) {
				minQHlv = 0;
				continue;
			}
			if (ue == null) {
				minQHlv = 0;
				continue;
			}
			// 洗练
			junzhu.wqSH += ue.wqSH;
			junzhu.wqJM += ue.wqJM;
			junzhu.wqBJ += ue.wqBJ;
			junzhu.wqRX += ue.wqRX;
			junzhu.jnSH += ue.jnSH;
			junzhu.jnJM += ue.jnJM;
			junzhu.jnBJ += ue.jnBJ;
			junzhu.jnRX += ue.jnRX;
			//TODO战力计算规则 会因为新增5个洗练属性改变
			//以上1.0版本改变洗练逻辑
			
			//宝石
			List<Long> jewelList = JewelMgr.inst.getJewelOnEquip(ue);
			for(long jewelInfo : jewelList){
				if(jewelInfo >0){
					int jewelId = (int)(jewelInfo >> 32);
					Fuwen jewelPeiZhi = JewelMgr.inst.jewelMap.get(jewelId);
					if (jewelPeiZhi != null ){
						int shuXingType = jewelPeiZhi.shuxing;
						//宝石属性目前只有三种，如有改变，增加case ; 2016-05-24
						switch (shuXingType){
							case 1 :
								junzhu.gongJi += jewelPeiZhi.shuxingValue;
								break;
							case 2 :
								junzhu.fangYu += jewelPeiZhi.shuxingValue;
								break;
							case 3 :
								junzhu.shengMingMax +=jewelPeiZhi.shuxingValue;
								break;
							default:
								log.info("宝石增加属性添加了新的类型{}",shuXingType);
								break;
						}
					}
				}
			}
			
			// 强化
			UserEquip userEquip = ue;
			int lv = userEquip.level;
			if (lv <= 0) {
				minQHlv = 0;
				continue;
			}
			minQHlv = Math.min(minQHlv, lv);
			int qianghuaId = zb.getQianghuaId();
			QiangHua qianghua = template.getQiangHua(qianghuaId, lv);
			if (qianghua == null) {
				log.error("强化配置没有找到 id {} lv {}", qianghuaId, lv);
			} else {
				junzhu.gongJi += qianghua.gongji;
				junzhu.fangYu += qianghua.fangyu;
				junzhu.shengMingMax += qianghua.shengming;
			}

		}
		/*
		try {
			AcitvitedTaoZhuang taozhuang = 
					HibernateUtil.find(AcitvitedTaoZhuang.class, junzhu.id);
			if(taozhuang == null){
				taozhuang = new AcitvitedTaoZhuang();
				taozhuang.jId = junzhu.id;
			}
			calcTaoZhuang(junzhu, taozhuang);
			calcTaoZhuangQianHua(junzhu, taozhuang);
		} catch (Exception e) {
			log.error("计算套装出错", e);
		}
		*/
	}

	/**
	 * 计算套装（强化）
	 * 
	 * @param junzhu
	 * @param minColor
	 */
	public void calcTaoZhuangQianHua(JunZhu junzhu, AcitvitedTaoZhuang taozhuang) {
		if(junzhu == null) return;
//		int idx;
//		if (minQHlv >= 100) {
//			idx = 8;
//		} else if (minQHlv >= 80) {
//			idx = 7;
//		} else if (minQHlv >= 60) {
//			idx = 6;
//		} else if (minQHlv >= 30) {
//			idx = 5;
//		} else {
//			return;
//		}
//		idx -= 1;// 5678->4567
//		List list = TempletService.listAll(TaoZhuang.class.getSimpleName());
//		if (list == null) {
//			log.error("没有套装配置");
//			return;
//		}
//		if (idx >= list.size()) {
//			return;
//		}
//		TaoZhuang t = (TaoZhuang) list.get(idx);
		if(taozhuang == null || taozhuang.maxActiQiangHuaId <= 0){
			return;
		}
		int[] fv = new int[4];
		TaoZhuang t = null;
		for(int id = minTaoZhuangQHId; id <= taozhuang.maxActiQiangHuaId; id++){
			t = taoZhuangMap.get(id);
			if(t == null || t.type != taoZhuangQiangHuaType){
				continue;
			}
			int[] key = { t.shuxing1, t.shuxing2, t.shuxing3 };
			int[] v = { t.num1, t.num2, t.num3 };
			for (int i = 0; i < 3; i++) {
				switch (key[i]) {
				case 1:
					junzhu.gongJi += v[i];
					fv[0] += v[i];
					break;
				case 2:
					junzhu.fangYu  += v[i];
					fv[1] += v[i];
					break;
				case 3:
					junzhu.shengMingMax += v[i];
					fv[2] += v[i];
					break;
				default:
					log.error("未处理的套装强化属性类型{}", key[i]);
					break;
				}
			}
		}
		log.info("[{}]强化套装加成{},{},{}", fv[0], fv[1], fv[2]);
	}
//		int[] key = { t.shuxing1, t.shuxing2, t.shuxing3 };
//		int[] v = { t.num1, t.num2, t.num3 };
//		int[] fv = new int[4];
//		for (int i = 0; i < 3; i++) {
//			switch (key[i]) {
//			case 4:
//				junzhu.wqSH += v[i];
//				fv[0] = v[i];
//				break;// 武器伤害加深
//			case 5:
//				junzhu.wqJM += v[i];
//				fv[1] = v[i];
//				break;// 武器伤害抵抗
//			case 6:
//				junzhu.jnSH += v[i];
//				fv[2] = v[i];
//				break;// 技能伤害加深
//			case 7:
//				junzhu.jnJM += v[i];
//				fv[3] = v[i];
//				break;// 技能伤害抵抗
//			default:
//				log.error("未处理的套装强化属性类型{}", key[i]);
//				break;
//			}
//		}
//		log.info("[{}]强化套装加成{},{},{},{}", fv[0], fv[1], fv[2], fv[3]);
	

	/**
	 * 计算套装（品质）
	 * 
	 * @param junzhu
	 */
	public void calcTaoZhuang(JunZhu junzhu, AcitvitedTaoZhuang taozhuang) {
		if(junzhu == null){
			return;
		}
//		// 小于2的 不加成属性
//		if (minPinZhi < 2) {
//			return;
//		}
		if( taozhuang == null || taozhuang.maxActiId <= 0){
			return;
		}
//		// 角色穿了套装进行的属性加成 是套装等级一下的所有套装等级都加上
		int gongji = 0;
		int fangyu = 0;
		int shengming = 0;
		for(int i = minTaoZhuangId; i <= taozhuang.maxActiId; i++){
			TaoZhuang t =  taoZhuangMap.get(i);
			if (t != null && t.type == taoZhuangType) {
				log.info("junzhuId {}, 加成了 套装condition：{}", junzhu.id, t.condition);
				junzhu.gongJi += t.num1;
				junzhu.fangYu += t.num2;
				junzhu.shengMingMax += t.num3;
				gongji += t.num1;
				fangyu += t.num2;
				shengming += t.num3;
			}
		}
		log.info("[{}]套装总加成:{},{},{}", junzhu.name, gongji, fangyu, shengming);
	//	log.info("junzhuId :{} minPinZhi is {} ", junzhu.id, minPinZhi);
//		// 最小的品质从2开始
//		for (int condition = 2; condition <= minPinZhi; condition++) {
//			// TODO @wangzhuan 20160115
////			TaoZhuang t =  taoZhuangMap.get(condition);
////			if (t != null) {
////				log.info("junzhuId {}, 加成了 套装condition：{}", junzhu.id, condition);
////				junzhu.gongJi += t.num1;
////				junzhu.fangYu += t.num2;
////				junzhu.shengMingMax += t.num3;
////				gongji += t.num1;
////				fangyu += t.num2;
////				shengming += t.num3;
////			}
//		}
		
	}

	public void addExp(JunZhu jz, final int v) {
		if (jz == null) {
			return;
		}
		if (v <= 0) {
			return;
		}
		if (jz.level == 0) {
			jz.level = 1;
		}
		final int beforeLv = jz.level;
		jz.exp += v;
		boolean levelChange = false;
		do {
			ExpTemp expTemp = TempletService.getInstance().getExpTemp(1,
					jz.level);
			if (expTemp == null) {
				log.error("没有找到经验配置 等级{}", jz.level);
				return;
			}
			if (expTemp.needExp <= 0) {
				log.error("错误的等级经验{}", expTemp.id);
				return;
			}
			if (jz.exp >= expTemp.needExp) {
				ExpTemp nextExpTemp = TempletService.getInstance().getExpTemp(
						1, jz.level + 1);
				if (nextExpTemp == null) {
					log.error("没有找到君主经验配置 等级{} ", jz.level);
					break;
				}
				jz.exp -= expTemp.needExp;
				jz.level += 1;
				log.info("{} 升级至{}", jz.name, jz.level);
				ActLog.log.KingLvup(jz.id, jz.name, jz.level, jz.exp);
				CunLiangLog.inst.levelChange(jz.id, jz.level);
				// EventMgr.addEvent(ED.ACHIEVEMENT_PROCESS, new
				// AchievementCondition(
				// jz.id, AchievementConstants.type_junzhu_level, 1));
				EventMgr.addEvent(jz.id,ED.junzhu_level_up, new Object[] { jz.id,
						jz.level, jz });//第三个参数是后来加的 2015年9月16日14:54:48
				JunzhuShengji shengji = getJunzhuShengjiByLevel(jz.level);
				updateTiLi(jz, shengji.addTili, "升级");
				if(jz.level==RankingMgr.BAIZHAN_JUNZHU_MIN_LEVEL){// 等级达到上榜条件，添加君主到百战榜，过关榜
					EventMgr.addEvent(jz.id,ED.BAIZHAN_RANK_REFRESH, new Object[]{jz, jz.guoJiaId});
				}
				if(jz.level == RankingMgr.GUOGUAN_JUNZHU_MIN_LEVEL) {
					EventMgr.addEvent(jz.id,ED.GUOGUAN_RANK_REFRESH, new Object[]{jz, jz.guoJiaId});
				}
				if(jz.level == RankingMgr.CHONGLOU_JUNZHU_MIN_LEVEL) {
					EventMgr.addEvent(jz.id,ED.CHONGLOU_RANK_REFRESH, new Object[]{jz, jz.guoJiaId});
				}
				// 2015-9-8 10:86 检查符文解锁事件
				EventMgr.addEvent(jz.id,ED.CHECK_FUWEN_UOLOCK, jz);
				// 2015-9-22 12:04 符石推送红点
				EventMgr.addEvent(jz.id,ED.FUSHI_PUSH, jz);
				log.info("{}升级增加体力{}达到{}", jz.name, shengji.addTili, jz.tiLi);
				levelChange = true;
				// 判断君主天赋是否要进行升级提示 20151010//用事件去做
//				TalentMgr.instance.noticeTalentCanLevUp(jz);
				// 君主等级榜刷新
				EventMgr.addEvent(jz.id,ED.JUNZHU_LEVEL_RANK_REFRESH, jz);
				IoSession user = SessionManager.inst.findByJunZhuId(jz.id);
				if(user != null){
					//2016年8月9日11:49:52 放这里就太频繁了。
					//EventMgr.addEvent(jz.id,ED.REFRESH_TIME_WORK, user);
				}
			} else {
				break;
			}
		} while (true);
//		if (levelChange) {
//			calcJunZhuTotalAtt(jz);
//		}
		HibernateUtil.update(jz);
		//不在这里发，谁用谁在外面发。
//		IoSession su = SessionManager.inst.findByJunZhuId(jz.id);
//		if (su != null) {
//			sendMainInfo(su,jz, levelChange);
//		}
		int Time = 0;//升级所用时间
		int Reason = 0;
		int SubReason = 0;
		OurLog.log.PlayerExpFlow(v, beforeLv, jz.level, Time, Reason, SubReason,
				String.valueOf(jz.id));
		ActLog.log.GetExp(jz.id, jz.name, ActLog.Reason, v, jz.exp);
	}

	/**
	 * 修改君主体力值
	 * 
	 * @param junzhu
	 *            君主对象
	 * @param changeValue
	 *            修改值，可以为负数
	 */
	public void updateTiLi(JunZhu junzhu, int changeValue, String reason) {
		int preV = junzhu.tiLi;
		junzhu.tiLi += changeValue;
		if (junzhu.tiLi < 0) {
			junzhu.tiLi = 0;
		} else {
			junzhu.tiLi = Math.min(junzhu.tiLi, TiLiMax);
		}
		ActLog.log.PhysicalPower(junzhu.id, junzhu.name, preV, changeValue, junzhu.tiLi, reason);
	}

	public JunZhu getJunZhu(IoSession session) {
		if (session == null)
			return null;
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			if (session instanceof RobotSession == false)
				log.error("null junZhu id");
			return null;
		}
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
		if (junzhu == null) {
			return null;
		}
		return junzhu;
	}

	public void addPoint(int id, IoSession session, Builder builder) {
		JunZhu junzhu = getJunZhu(session);
		if (junzhu == null)
			return;
		JunZhuAddPointReq.Builder req = (qxmobile.protobuf.JunZhuProto.JunZhuAddPointReq.Builder) builder;
		int sum = req.getWuLiAdd() + req.getZhiLiAdd() + req.getZhengZhiAdd();
		if (sum > junzhu.attPoint) {
			ErrorMessage.Builder test = ErrorMessage.newBuilder();
			test.setErrorCode(1);
			test.setErrorDesc("点数不足。");
			session.write(test.build());
			return;
		}
		if (req.getWuLiAdd() < 0 || req.getZhiLiAdd() < 0
				|| req.getZhengZhiAdd() < 0) {
			ErrorMessage.Builder test = ErrorMessage.newBuilder();
			test.setErrorCode(1);
			test.setErrorDesc("点数不能小于0");
			session.write(test.build());
			return;
		}
		junzhu.attPoint -= sum;
		junzhu.wuLiAdd += req.getWuLiAdd();
		junzhu.zhiLiAdd += req.getZhiLiAdd();
		junzhu.zhengZhiAdd += req.getZhengZhiAdd();
		HibernateUtil.save(junzhu);
		log.info("pid {} 执行加点 {} {} {}", junzhu.id, req.getWuLiAdd(),
				req.getZhiLiAdd(), req.getZhengZhiAdd());
		sendAttPointInfo(session, junzhu);
	}

	public void sendAttPoint(int id, IoSession session, Builder builder) {
		JunZhu junzhu = getJunZhu(session);
		if (junzhu == null)
			return;
		sendAttPointInfo(session, junzhu);
	}

	protected void sendAttPointInfo(IoSession session, JunZhu junzhu) {
		JunZhuAttPointRet.Builder ret = JunZhuAttPointRet.newBuilder();
		ret.setAttPoint(junzhu.attPoint);
		ret.setWuLiAdd(junzhu.wuLiAdd);
		ret.setZhengZhiAdd(junzhu.zhengZhiAdd);
		ret.setZhiLiAdd(junzhu.zhiLiAdd);
		session.write(ret.build());
		log.debug("send att point");
	}

	@Override
	public void proc(Event evt) {
		Date date = new Date();
		Long jzId = 0L;
		PlayerTime playerTime = null;
		switch (evt.id) {
		case ED.ACC_LOGIN:
			// 参数改为junzhuId
			// fixCreateJunZhu((Long)evt.param);//在创建角色时处理。2014年10月17日11:10:54
			// 登陆成功发送
			// 2015年8月26日 Cache记录junZhuId+上线时间，挪动到登录事件中
			jzId = (Long) evt.param;
			if (jzId == null) {
				return;
			}
			Redis.getInstance().set(AccountManager.CACHE_ONLINETIME + jzId,
					String.valueOf(date.getTime()));
			break;
		case ED.ACC_LOGOUT:// 2015-8-26 将在线时间统计挪动到下线事件中
			jzId = (Long) evt.param;
			if (jzId == null) {
				return;
			}
				String loginV = Redis.getInstance().get(
						AccountManager.CACHE_ONLINETIME + jzId);
				if(loginV == null){
					return;
				}
				long loginTime = Long.valueOf(loginV);
				long logoutTime = Long.valueOf(date.getTime());
				playerTime = HibernateUtil.find(PlayerTime.class, jzId);
				if (playerTime == null) {
					playerTime = new PlayerTime(jzId);
					playerTime.junzhuId = jzId;
					playerTime.logoutTime = date;
					playerTime.totalOnlineTime = logoutTime - loginTime;
					Cache.playerTimeCache.put(jzId, playerTime);
					HibernateUtil.insert(playerTime);
					// Redis中在线时间统计清空
					Redis.getInstance().set(
							AccountManager.CACHE_ONLINETIME + jzId,"0");
				} else {
					playerTime.logoutTime = date;
					long onlineTime = playerTime.totalOnlineTime;
					if (0 == onlineTime) {
						playerTime.totalOnlineTime = logoutTime - loginTime;
					} else {
						playerTime.totalOnlineTime = playerTime.totalOnlineTime
								+ (logoutTime - loginTime);
					}
					HibernateUtil.update(playerTime);
					// Redis中在线时间统计清空
					Redis.getInstance().set(
							AccountManager.CACHE_ONLINETIME + jzId,"0");
				}
			break;
		case ED.JUNZHU_LOGIN:{
			JunZhu junZhu = (JunZhu) evt.param;;
			//更新君主登录天数
			XianShiActivityMgr.instance.updateLoginDate(junZhu.id);
			playerTime = HibernateUtil.find(PlayerTime.class, junZhu.id);
			if (playerTime == null) {
				playerTime = new PlayerTime(junZhu.id);
				playerTime.junzhuId = junZhu.id;
				playerTime.loginTime = date;
				Cache.playerTimeCache.put(junZhu.id, playerTime);
				HibernateUtil.insert(playerTime);
			} else {
				playerTime.loginTime = date;
				HibernateUtil.update(playerTime);
			}
			List<HYTreasure> treasureList = HYTreasureDao.inst.getByJunZhuId(junZhu.id);
			for (HYTreasure t : treasureList) {
				t.battleJunzhuId = 0;
				HibernateUtil.save(t);
			}
			/*
			 * 君主登陆，是否发送贡金周奖励
			 */
			GuoJiaMgr.inst.sendWeekRankWard(junZhu);
			break;
		}
		// 创建角色成功
		case ED.CREATE_JUNZHU_SUCCESS:
			JunZhu junzhu = (JunZhu) evt.param;
			if (junzhu == null)
				return;
			playerTime = HibernateUtil.find(PlayerTime.class, junzhu.id);
			date = new Date();
			if (playerTime == null) {
				playerTime = new PlayerTime(junzhu.id);
				playerTime.junzhuId = junzhu.id;
				playerTime.createRoleTime = date;
				Cache.playerTimeCache.put(junzhu.id, playerTime);
				HibernateUtil.insert(playerTime);
			} else {
				playerTime.createRoleTime = date;
				HibernateUtil.update(playerTime);
			}
			/*
			 * 封测需求：初始君主vip等级和月卡 20150626
			 */
//			initJunZhuVip(junzhu);
			break;
		/*
	case ED.QIANG_HUA_FINISH: //强化结束
			Object[] params = (Object[]) evt.param;
			jzId = (Long)params[0];
			SessionUser user = SessionManager.inst.findByJunZhuId(jzId);
			activitedQiangHuaTaoZhuang(jzId, user);
			break;
			*/
		}
	}

	public void initJunZhuVip(JunZhu jun) {
		/*
		 * vip初始等级
		 */
		int initVipLev = CanShu.VIPLV_ININT;
		PlayerVipInfo vipInfo = null;
		long jid = jun.id;
		if (initVipLev > 0) {
			VIP vip = VipMgr.vipTemp.get(initVipLev);
			if (vip == null) {
				log.error("vip.xml对应的:{}, 配置数据不存在", initVipLev);
				return;
			}
			vipInfo = new PlayerVipInfo();
			vipInfo.accId = jid;
			vipInfo.sumAmount = 0;
			vipInfo.level = initVipLev;
			vipInfo.vipExp = vip.needNum;
			Cache.playerVipInfoCaChe.put(vipInfo.accId, vipInfo);
			HibernateUtil.save(vipInfo);

			jun.vipLevel = initVipLev;
			HibernateUtil.save(jun);
			log.info("初始玩家：{}，的vip等级为:{}", jid, initVipLev);
		}
		/*
		 * 是否初始默认配置一张月卡
		 */
		int isYuekaInit = CanShu.IS_YUEKA_INIT;
		if (isYuekaInit == 1) { // 确定要初始月卡
			//这个不要了。再需要时，用事件去做
		}
	}

	public JunZhu fixCreateJunZhu(long junZhuId, String name, int roleId,
			int guoJiaId) {
		long time = System.currentTimeMillis();
		/*
		 * // 进行创建角色。2014年10月17日11:10:50 JunZhu junzhu = null; //
		 * 确定是要创建；HibernateUtil.find(JunZhu.class, junZhuId);
		 * log.info("junzhu :"+junzhu); log.info("------find junzh-----{}",
		 * System.currentTimeMillis() - time); time =
		 * System.currentTimeMillis();
		 */

		JunZhu junzhu = new JunZhu();
		junzhu.id = junZhuId;
		junzhu.name = name;
		junzhu.level = 1;
		JunzhuShengji conf = getJunzhuShengjiByLevel(junzhu.level);
		if (conf == null) {
			log.error("junZhuShengji配置有误 ,找不到君主等级{}的配置信息", junzhu.level);
			junzhu.tiLi = 1;// 配置有误时容错处理，让程序继续跑。
		} else {
			junzhu.tiLi = conf.addTili;
		}
		junzhu.roleId = roleId;
		junzhu.guoJiaId = 0;//guoJiaId;
		JunZhu pre = Cache.jzCache.putIfAbsent(junZhuId, junzhu);
		if(pre != null){
			log.warn("之前已创建 {}",junZhuId);
			return pre;
		}
		Bag<EquipGrid> equips = EquipMgr.inst.initEquip(junZhuId);
		log.info("------init equip-----{}", System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		calcTotalJunZhuAtt(junzhu, equips);
//		log.info("------calcAtt-----{}", System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		MC.add(junzhu, junzhu.id);
		Throwable e = HibernateUtil.insert(junzhu);
//		log.info("------add junzhu-----{}", System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		if (e == null) {
			log.info("创建君主成功 id {} name {}", junzhu.id, junzhu.name);
		} else {
			log.info("创建君主失败", e);
		}
		BagMgr.inst.addCache(junzhu.id);
		time = System.currentTimeMillis();
		if(2>1){//关闭武将
			HeroMgr heroMgr = HeroMgr.getInstance();
			HeroGrow hg = HeroMgr.id2HeroGrow.get(31010);
			if (hg != null) {
				WuJiang wuJiang = heroMgr.createWuJiangBean(hg, (int) junzhu.id);
				if (wuJiang != null) {
					wuJiang.setCombine(true);
					HeroMgr.inst.addNewWuJiang(wuJiang, junzhu.id);
					log.info("新获得武将{}", 31010);
				}
			}
//		log.info("------fix hero-----{}", System.currentTimeMillis() - time);
		}
		return junzhu;
		/*
		 * else if(junzhu.level == 0 || junzhu.shengMingMax == 0 ||
		 * junzhu.tiLiMax == 0){ junzhu.level = Math.max(1, junzhu.level);
		 * calcAtt(junzhu); HibernateUtil.save(junzhu); log.info("修改君主等级和血上限。");
		 * }
		 */
	}

	/**
	 * 根据君主等级获取对应的等级配置信息
	 * 
	 * @param level
	 * @return
	 */
	public JunzhuShengji getJunzhuShengjiByLevel(int level) {
		List<JunzhuShengji> list = TempletService.listAll(JunzhuShengji.class
				.getSimpleName());
		if (list == null) {
			log.error("配置没有找到");
			return null;
		}
		JunzhuShengji junzhuShengji = null;
		for (JunzhuShengji shengji : list) {
			if (shengji.lv == level) {
				junzhuShengji = shengji;
				break;
			}
		}
		return junzhuShengji;
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);
		EventMgr.regist(ED.ACC_LOGOUT, this);
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
		// 建立君主角色成功
		EventMgr.regist(ED.CREATE_JUNZHU_SUCCESS, this);
		//强化
		EventMgr.regist(ED.QIANG_HUA_FINISH, this);
		
	}
	public int calcMibaoZhanLi(int gongJi, int fangYu,
			int shengMing, int mibaoid, int mibaolevel) {
		JunZhu jz = new JunZhu();
		jz.fangYu = fangYu;
		jz.shengMingMax = shengMing;
		jz.gongJi = gongJi;
		jz.id = -999; //区别真实JunZhu对象,因为getZhanli方法中对真实君主有不一样的数据处理
		/*
		 * 秘宝等级达到一定程度，会有额外属性加成
		 */
		List<MiBaoExtraAttribute> mibaoexA = MibaoMgr.inst.
				getAddAttrList(mibaoid, mibaolevel);
		for(MiBaoExtraAttribute m: mibaoexA){
			addAttrToX(m.shuxing, m.Num, jz);
		}
		int zhanLi = getZhanli(jz);
		return zhanLi;
	}

//	public int calcMibaoZhanLi(GameObject gameObj, int gongJi, int fangYu,
//			int shengMing) {
//		// 取得原来属性值
//		int fangyuPre = gameObj.getFangyu();
//		int gongjiPre = gameObj.getGongji();
//		int shengmingPre = gameObj.getShengming();
//		// 设为当前秘宝属性值
//		gameObj.setFangyu(gongJi);
//		gameObj.setGongji(fangYu);
//		gameObj.setShengming(shengMing);
//		int zhanLi = getZhanli(gameObj);
//		// 计算完战力后， 重置原来的属性值
//		gameObj.setFangyu(fangyuPre);
//		gameObj.setGongji(gongjiPre);
//		gameObj.setShengming(shengmingPre);
//		return zhanLi;
//	}

	/**
	 * 获取战力
	 * 
	 * @Title: getZhanli
	 * @Description:
	 * @param gameOb
	 * @param mbs
	 * @return
	 */
	public int getZhanli(GameObject gameOb) {
		int gongji = gameOb.getGongji();
		int shengming = gameOb.getShengming();
		int fangyu = gameOb.getFangyu();
		int wqshjs = gameOb.getWqSH();
		int wqshdk = gameOb.getWqJM();
		int wqbjjs = gameOb.getWqBJ();
		int wqbjdk = gameOb.getWqRX();
		int jnshjs = gameOb.getJnSH();
		int jnshdk = gameOb.getJnJM();
		int jnbjjs = gameOb.getJnBJ();
		int jnbjdk = gameOb.getJnRX();
//		log.info("参与战力值计算的各项属性值:[gongji=" + gongji + ", fangyu=" + fangyu
//				+ ", shengming=" + shengming + ", wqshjs=" + wqshjs
//				+ ", wqshdk=" + wqshdk + ", wqbjjs=" + wqbjjs + ", wqbjdk="
//				+ wqbjdk + ", jnshjs=" + jnshjs + ", jnshdk=" + jnshdk
//				+ ", jnbjjs=" + jnbjjs + ", jnbjdk=" + jnbjdk + "]");
		double m = CanShu.ZHANLI_M;
		double c = CanShu.ZHANLI_C;
		double r = CanShu.ZHANLI_R;
		double k1 = CanShu.ZHANLI_K1;
		double k2 = CanShu.ZHANLI_K2;
		double m1 = CanShu.ZHANLI_M1;
		double m2 = CanShu.ZHANLI_M2;
		double puGongQuan = CanShu.JUNZHU_PUGONG_QUANZHONG;
		double puGongBei = CanShu.JUNZHU_PUGONG_BEISHU;
		double jiNengQuan = CanShu.JUNZHU_JINENG_QUANZHONG;
		double jiNengBei = CanShu.JUNZHU_JINENG_BEISHU;
		double zhanliL = CanShu.ZHANLI_L;
		double baoJiLv = 0.2;
		double w = gongji + fangyu + shengming / c;
		
		double wqk = 0; // 默认值设为0
		
		double jnk = 0; // 默认值设为1
		
		double mb = 1; // 设置默认值设为1
	
		if(gameOb instanceof JunZhu){
			JunZhu ju = (JunZhu) gameOb;
			if(ju.id > 0){
				JNBean bean = HibernateUtil.find(JNBean.class, ju.id);
				if(bean != null){
					JiNengPeiYang p = null;
					// 重武器普攻突破等级
					if(bean.wq1_1 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq1_1);
					}
					int quality1 = p == null? 0: p.quality;
					// 轻武器普攻突破等级
					p = null;
					if(bean.wq2_1 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq2_1);
					}
					int quality2 = p == null? 0:p.quality;
					// 弓箭普攻突破等级
					p = null;
					if(bean.wq3_1 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq3_1);
					}
					int quality3 = p == null? 0:p.quality;
					 // 计算wq
					wqk = k1 * (quality1 + quality2+ quality3);
					
					p = null;
					if(bean.wq1_2 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq1_2);
					}
					int quality4 = p == null? 0:p.quality;
					
					p = null;
					if(bean.wq1_3 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq1_3);
					}
					int quality5 = p == null? 0:p.quality;
					
					p = null;
					if(bean.wq2_2 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq2_2);
					}
					int quality6 = p == null? 0:p.quality;
					
					p = null;
					if(bean.wq2_3 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq2_3);
					}
					int quality7 = p == null? 0:p.quality;
					
					p = null;
					if(bean.wq3_2 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq3_2);
					}
					int quality8 = p == null? 0:p.quality;
					
					p = null;
					if(bean.wq3_3 != 0){
						p = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq3_3);
					}
					int quality9 = p == null? 0:p.quality;
					
					jnk = k2 *(quality4 + quality5 +quality6 + quality7 + quality8 + quality9);
					
				}
				// 秘宝技能个数
				int mibaoSkillNum = MibaoMgr.inst.getSkillDBList(ju.id).size();
				mb = 1 + MathUtils.getMin(1, mibaoSkillNum) * (m1 + m2 * mibaoSkillNum);
			}
		}
		
		double wq = puGongQuan * puGongBei * (1 + wqk) * mb;
		
		double wz = (1 + baoJiLv + wqshjs / zhanliL + wqbjjs / zhanliL)
				* (1 + baoJiLv + wqshdk / zhanliL + wqbjdk / zhanliL);
		
		double jN = jiNengQuan * jiNengBei *(1 + jnk) * mb;
		
		double jZ = (1 + baoJiLv + jnshjs / zhanliL + jnbjjs / zhanliL)
				* (1 + baoJiLv + jnshdk / zhanliL + jnbjdk / zhanliL);
		double result = m * w * Math.pow(wq * wz + jN * jZ, (1 / r));
		int zhanli = (int) Math.round(result);
//		log.info("name 是 :{}的君主或者其他npc的综合战力是:{}", gameOb.getName(), zhanli);
		return zhanli;
	}

	public void resetZhanliRedis(GameObject o, int zhanli) {
		if (o instanceof JunZhu) {
			JunZhu jz = (JunZhu) o;
			RankingMgr.inst.resetZhanliRankRedis(jz, zhanli);
		}
	}

	/*
	 * 去掉 pve战力，所有的战力统一为 getJunZhuZhanliFinally(junzhu)  20150831
	 */
//	public void getPVEMiBaoZhanLi(IoSession session) {
////		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
////		sendPveMibaoZhanli(jz, session);
//	}

	/*
	 * 去掉 pve战力，所有的战力统一为 getJunZhuZhanliFinally(junzhu)  20150831
	 */
	public void sendPveMibaoZhanli(JunZhu jz, IoSession session) {
//		if(jz == null)return;//jsp防错
//		List<MiBaoDB> mbs = HibernateUtil.list(MiBaoDB0.class, " where ownerId="
//				+ jz.id);
//		cacMiBaoAtt(jz, mbs);
//		int zhanli = getZhanli(jz);
//		PveMiBaoZhanLi.Builder resp = PveMiBaoZhanLi.newBuilder();
//		resp.setZhanli(zhanli);
//		session.write(resp.build());
	}

	/*
	 * 去掉 pve战力，所有的战力统一为 getJunZhuZhanliFinally(junzhu)  20150831
	 */
	public void sendPveMibaoZhanli_2(JunZhu jz, IoSession session,
			BuZhenMibaoBean bean) {
//		List<MiBaoDB> mbs = null;
//		if (bean != null) {
//			List<Long> ids = new ArrayList<Long>(3);
//			ids.add(bean.pos1);
//			ids.add(bean.pos2);
//			ids.add(bean.pos3);
//			mbs = MibaoMgr.inst.getMiBaoDBs(ids);
//		}
//		cacMiBaoAtt(jz, mbs);
//		int zhanli = getZhanli(jz);
//		PveMiBaoZhanLi.Builder resp = PveMiBaoZhanLi.newBuilder();
//		resp.setZhanli(zhanli);
//		session.write(resp.build());
	}

	public int getJunZhuZhanliFinally(JunZhu jz) {
		/*
		 * 获取战力不影响原来的 君主属性
		 */
		log.info("克隆之前 jz 的地址是 ：" + jz);
		JunZhu juClone = jz.clone();
		log.info("克隆之后 juClone 的地址是： " + juClone);
		log.info("克隆之后 jz 的地址是： " + jz);
	
		calcJunZhuTotalAtt(juClone);
		int zhanli = getZhanli(juClone);
		return zhanli;
	}

	public void requestSpecifyJunzhuInfo(int id, IoSession session, Builder builder) {
		JunZhuInfoSpecifyReq.Builder request = (qxmobile.protobuf.JunZhuProto.JunZhuInfoSpecifyReq.Builder) builder;
		long junzhuId = request.getJunzhuId();
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId);
		if(junzhu == null) {
			log.error("找不到君主，junzhuId:{}", junzhuId);
			return;
		}
		
		JunZhuInfo.Builder response = JunZhuInfo.newBuilder();
		RankingMgr.inst.getJunZhuDetail(response, junzhu);
		ProtobufMsg protobufMsg = new ProtobufMsg();
		protobufMsg.id = PD.JUNZHU_INFO_SPECIFY_RESP;
		protobufMsg.builder = response;
		session.write(protobufMsg);
	}
	

	/**
	 * 套装当前激活的pinzhi / qianghua level
	 * @param id
	 * @param session
	 */
	public void getTaoZhuangInfo(int id, IoSession session ){
		JunZhu junzhu = getJunZhu(session);
		if (junzhu == null) return;
		TaoZhuangResp.Builder resp = TaoZhuangResp.newBuilder();
		AcitvitedTaoZhuang taozhuang = null;// 
//				HibernateUtil.find(AcitvitedTaoZhuang.class, junzhu.id);
		if(taozhuang == null){
			resp.setMaxActiZhuang(0);
			resp.setMaxQiangHuaZh(0);
			session.write(resp.build());
			return;
		}
		resp.setMaxQiangHuaZh(taozhuang.maxActiQiangHuaId);
		resp.setMaxActiZhuang(taozhuang.maxActiId);
		session.write(resp.build());
	}
	
	public void activitedQiangHuaTaoZhuang(long jid, SessionUser user){
		List<TaoZhuang> listt = TempletService.listAll(TaoZhuang.class.getSimpleName());
		int actId = 0;
		AcitvitedTaoZhuang taozhuang = HibernateUtil.find(AcitvitedTaoZhuang.class, jid);
		if(taozhuang == null){
			taozhuang = new AcitvitedTaoZhuang();
			taozhuang.jId = jid;
		}
		//id 不是从1开始的
		if(taozhuang.maxActiQiangHuaId == 0){
			taozhuang.maxActiQiangHuaId = minTaoZhuangQHId-1;
		}
		boolean send = false;
		// 加载身上的装备
		List<EquipGrid> list = EquipMgr.inst.loadEquips(jid).grids;
		for(TaoZhuang conf: listt){
			if(conf.type != taoZhuangQiangHuaType){
				continue;
			}
			actId = conf.id;
			if(actId <= taozhuang.maxActiQiangHuaId){
				// 已经激活过了
				continue;
			}
			//一级一级升级
			if(actId != taozhuang.maxActiQiangHuaId + 1){
				// 激活过度
				continue;
			}
			/*
			 * 20160229 之后 ： 9件装备中只要有conf.neededNum件装备pinZhi >= conf.condition, 就可以激活
			 */
			if(getNum(jid, conf, list) < conf.neededNum){
				continue;
			}
			// 激活成功
			taozhuang.maxActiQiangHuaId = actId;
			HibernateUtil.save(taozhuang);
			log.info("君主:{}强化套装激活成功，激活id是：{}", jid,taozhuang.maxActiQiangHuaId);
			send = true;
		}
		if(send && user!=null) sendMainInfo(user.session);
	}
	public int getNum(long jid, TaoZhuang conf, List<EquipGrid> list){
		int cnt = list.size();
		cnt = Math.min(9, cnt);
		int number = 0;
		for (int i = 0; i < cnt; i++) {
			EquipGrid gd = list.get(i);
			if (gd == null || gd.instId <= 0) {
				continue;
			}
			UserEquip userEquip = UserEquipDao.find(jid, gd.instId);
			if (userEquip == null) {
				continue;
			}
			if(userEquip.level >= conf.condition){
				number ++;
			}
		}
		return number;
	}
	public void activitedTaoZhuang(int id, IoSession session, Builder builder ){
		JunZhu junzhu = getJunZhu(session);
		if (junzhu == null) return;
		ActivateTaoZhReq.Builder req = ( ActivateTaoZhReq.Builder) builder;
		int activatedType = req.getActivatedType();
		int reqActId = req.getActivatedId();
		AcitvitedTaoZhuang taozhuang = 
				HibernateUtil.find(AcitvitedTaoZhuang.class, junzhu.id);
		if(taozhuang == null){
			taozhuang = new AcitvitedTaoZhuang();
			taozhuang.jId = junzhu.id;
		}
		ActivateTaoZhResp.Builder resp = ActivateTaoZhResp.newBuilder();
		switch(activatedType){
		case taoZhuangType: // 套装激活
		{
			resp.setActivatedType(1);
			resp.setActivatedId(reqActId);
			if(reqActId <= taozhuang.maxActiId){
				// 已经激活过了
				resp.setSuccess(1);
				session.write(resp.build());
				return;
			}
			if(taozhuang.maxActiId + 1 != reqActId){
				// 激活过度
				resp.setSuccess(2);
				session.write(resp.build());
				return;
			}
			TaoZhuang conf = taoZhuangMap.get(reqActId);
			if(conf == null){
				// 没有配置
				resp.setSuccess(3);
				session.write(resp.build());
				return;
			}
			// 加载身上的装备
			List<EquipGrid> list = EquipMgr.inst.loadEquips(junzhu.id).grids;
			
			/*
			 * 20160229 之前 ： 9件装备minPinZhi >= conf.condition, 就可以激活
			 */
			/*
			int cnt = list.size();
			cnt = Math.min(9, cnt);
			int minPinZhi = 100;
			for (int i = 0; i < cnt; i++) {
				EquipGrid gd = list.get(i);
				if (gd == null || gd.itemId <= 0) {
					minPinZhi = 0;
					continue;
				}
				BaseItem equip = TempletService.itemMap.get(gd.itemId);
				if ((equip==null)||equip.getType() != BaseItem.TYPE_EQUIP) {
					continue;
				}
				// 装备基础属性
				ZhuangBei zb = (ZhuangBei) equip;
				minPinZhi = Math.min(minPinZhi, zb.pinZhi);
			}
			//品质不够
			if(minPinZhi < conf.condition){
				resp.setSuccess(4);
				session.write(resp.build());
				return;
			}
			*/
			/*
			 * 20160229 之后 ： 9件装备中只要有conf.neededNum件装备pinZhi >= conf.condition, 就可以激活
			 */
			int cnt = list.size();
			cnt = Math.min(9, cnt);
			int number = 0;
			for (int i = 0; i < cnt; i++) {
				EquipGrid gd = list.get(i);
				if (gd == null || gd.itemId <= 0) {
					continue;
				}
				BaseItem equip = TempletService.itemMap.get(gd.itemId);
				if ((equip==null)||equip.getType() != BaseItem.TYPE_EQUIP) {
					continue;
				}
				// 装备基础属性
				ZhuangBei zb = (ZhuangBei) equip;
				if(zb.pinZhi >= conf.condition){
					number++;
				}
			}
			if(number < conf.neededNum){
				resp.setSuccess(4); // 品质不够
				session.write(resp.build());
				return;
			}
			// 激活成功
			taozhuang.maxActiId = conf.id;
			HibernateUtil.save(taozhuang);
			resp.setSuccess(0);
			session.write(resp.build());
			log.info("君主:{}套装激活成功，激活id是：{}", junzhu.id, taozhuang.maxActiId);
			// 参数： 激活套装的品质
			EventMgr.addEvent(junzhu.id,ED.active_taozhuang, new Object[]{junzhu.id, conf.condition});
			sendMainInfo(session, junzhu);
			return;
		}
		case taoZhuangQiangHuaType:
		{
			resp.setActivatedType(2);
			resp.setActivatedId(reqActId);
			if(reqActId <= taozhuang.maxActiQiangHuaId){
				// 已经激活过了
				resp.setSuccess(1);
				session.write(resp.build());
				return;
			}
			if(taozhuang.maxActiQiangHuaId + 1 != reqActId){
				// 激活过度
				resp.setSuccess(2);
				session.write(resp.build());
				return;
			}
			TaoZhuang conf = taoZhuangMap.get(reqActId);
			if(conf == null){
				// 没有配置
				resp.setSuccess(3);
				session.write(resp.build());
				return;
			}
			// 加载身上的装备
			List<EquipGrid> list = EquipMgr.inst.loadEquips(junzhu.id).grids;
			/*
			 * 20160229 之前 ： 9件装备minPinZhi >= conf.condition, 就可以激活
			 */
			/*
			int cnt = list.size();
			cnt = Math.min(9, cnt);
			int minQHlv = 100;
			for (int i = 0; i < cnt; i++) {
				EquipGrid gd = list.get(i);
				if (gd == null || gd.instId <= 0) {
					minQHlv = 0;
					continue;
				}
				UserEquip userEquip = HibernateUtil.find(UserEquip.class, gd.instId);
				if (userEquip == null) {
					minQHlv = 0;
					continue;
				}
				int lv = userEquip.getLevel();
				if (lv <= 0) {
					minQHlv = 0;
					continue;
				}
				minQHlv = Math.min(minQHlv, lv);
			}
			//强化等级不够
			if(minQHlv < conf.condition){
				resp.setSuccess(4);
				session.write(resp.build());
				return;
			}
			*/
			/*
			 * 20160229 之后 ： 9件装备中只要有conf.neededNum件装备pinZhi >= conf.condition, 就可以激活
			 */
			int cnt = list.size();
			cnt = Math.min(9, cnt);
			int number = 0;
			for (int i = 0; i < cnt; i++) {
				EquipGrid gd = list.get(i);
				if (gd == null || gd.instId <= 0) {
					continue;
				}
				UserEquip userEquip = UserEquipDao.find(junzhu.id, gd.instId);
				if (userEquip == null) {
					continue;
				}
				if(userEquip.level >= conf.condition){
					number ++;
				}
			}
			if(number < conf.neededNum){
				resp.setSuccess(4); //强化等级不够
				session.write(resp.build());
				return;
			}
			// 激活成功
			taozhuang.maxActiQiangHuaId = conf.id;
			HibernateUtil.save(taozhuang);
			resp.setSuccess(0);
			session.write(resp.build());
			log.info("君主:{}强化套装激活成功，激活id是：{}", junzhu.id,taozhuang.maxActiQiangHuaId);
			sendMainInfo(session,junzhu);
			return;
		}
		}
	}
}
