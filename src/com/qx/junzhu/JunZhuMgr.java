package com.qx.junzhu;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.ActLog;
import log.CunLiangLog;
import log.OurLog;

import org.apache.commons.collections.map.LRUMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.FuWen.JunzhuAttr;
import qxmobile.protobuf.JunZhuProto.JunZhuAddPointReq;
import qxmobile.protobuf.JunZhuProto.JunZhuAttPointRet;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoSpecifyReq;
import qxmobile.protobuf.Ranking.JunZhuInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ChongZhi;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.HeroGrow;
import com.manu.dynasty.template.JunzhuShengji;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MibaoStar;
import com.manu.dynasty.template.QiangHua;
import com.manu.dynasty.template.Talent;
import com.manu.dynasty.template.TalentAttribute;
import com.manu.dynasty.template.TaoZhuang;
import com.manu.dynasty.template.VIP;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.activity.XianShiActivityMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.bag.Bag;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.equip.domain.UserEquip;
import com.qx.equip.web.UEConstant;
import com.qx.equip.web.UserEquipAction;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.fuwen.FuwenMgr;
import com.qx.guojia.GuoJiaMgr;
import com.qx.hero.HeroMgr;
import com.qx.hero.WuJiang;
import com.qx.huangye.HYResourceNpc;
import com.qx.huangye.HYTreasure;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.purchase.PurchaseMgr;
import com.qx.pve.BuZhenMibaoBean;
import com.qx.pvp.PVPConstant;
import com.qx.pvp.PvpBean;
import com.qx.ranking.RankingMgr;
import com.qx.robot.RobotSession;
import com.qx.vip.PlayerVipInfo;
import com.qx.vip.VipMgr;
import com.qx.vip.VipRechargeRecord;
import com.qx.world.GameObject;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

/**
 * @author 康建虎
 * 
 */
public class JunZhuMgr extends EventProc {
	public static JunZhuMgr inst;
	public static int TiLiMax = 999;
	public static Logger log = LoggerFactory.getLogger(JunZhuMgr.class.getSimpleName());
	/*
	 * 根据封测需求
	 */
	public static final boolean isInitVip = true;
	public static boolean useJzInfoCache = false;
	public static Map<Long, JunZhuInfoRet.Builder> jzInfoCache = Collections.synchronizedMap(new LRUMap(5000));

	public static Map<Integer, TaoZhuang> taoZhuangMap = new HashMap<Integer, TaoZhuang>();
	public JunZhuMgr() {
		inst = this;

		List<TaoZhuang> listTa = TempletService.listAll(TaoZhuang.class.getSimpleName());
		for(TaoZhuang t: listTa){
			taoZhuangMap.put(t.condition, t);
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
		sendMainInfo(session);
		// 天赋的显示通知
		TalentMgr.instance.noticeTalentCanLevUp(Long.valueOf(junZhuId));
		// 符文的显示通知
		JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId.longValue());
		EventMgr.addEvent(ED.FUSHI_PUSH, jz);
	}

	public void sendMainInfo(IoSession session) {
		JunZhu junzhu = getJunZhu(session);
		if (junzhu == null)
			return;
		JunZhuInfoRet.Builder b = buildMainInfo(junzhu,session);
		session.write(b.build());
		jzInfoCache.put(junzhu.id, b);
		log.info("send junzhu info {} hp {}", junzhu.name, b.getShengMing());
		HibernateUtil.save(junzhu);
	}

	public JunZhuInfoRet.Builder buildMainInfo(JunZhu junzhu, IoSession session) {
		// 以后需要缓存，不用总是计算
		calcAtt(junzhu);
		JunZhuInfoRet.Builder b = JunZhuInfoRet.newBuilder();
		b.setExp(junzhu.exp);
		b.setGender(junzhu.gender);
		b.setId(junzhu.id);
		b.setLevel(junzhu.level);
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
				junzhu.id);
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
		b.setExpMax(expTemp == null ? Integer.MAX_VALUE : expTemp.getNeedExp());
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
		int zhanLiAddMibaoAttrBefore = getZhanli(junzhu);
		calcMibaoAttr(junzhu, b);
		int zhanLiAddMibaoAttrAfter = getZhanli(junzhu);
		// 没有加秘宝属性前的战力值
		b.setZhanLiMibao(zhanLiAddMibaoAttrAfter - zhanLiAddMibaoAttrBefore);
		// 加了秘宝属性之后的总战力
		b.setZhanLi(zhanLiAddMibaoAttrAfter);
		// 向redis中添加玩家战力变化情况
		resetZhanliRedis(junzhu, zhanLiAddMibaoAttrAfter);
		return b;
	}

	private void calcMibaoAttr(JunZhu junzhu,
			qxmobile.protobuf.JunZhuProto.JunZhuInfoRet.Builder b) {
		List<MiBaoDB> mibaoList = HibernateUtil.list(MiBaoDB.class,
				" where ownerId=" + junzhu.id);
		int gongjiMibao = 0;
		int fangyuMibao = 0;
		int shengmingMibao = 0;
		for (MiBaoDB mibaoDB : mibaoList) {
			if (mibaoDB.getLevel() <= 0) {
				continue;
			}
			MiBao mibaoConf = MibaoMgr.mibaoMap.get(mibaoDB.getMiBaoId());
			if (mibaoConf == null) {
				log.error("找不到秘宝配置，mibaoId:{}", mibaoDB.getMiBaoId());
				continue;
			}
			boolean lock = MibaoMgr.inst.isLock(mibaoConf.unlockType,
					mibaoConf.unlockValue, junzhu);
			if (lock) {
				continue;
			}
			MibaoStar mibaoStar = MibaoMgr.inst.mibaoStarMap.get(mibaoDB
					.getStar());
			if (mibaoStar == null) {
				log.error("找不到对应的秘宝星级配置，mibaoDbId:{}, mibaoStar:{}",
						mibaoDB.getDbId(), mibaoDB.getStar());
				continue;
			}
			float chengZhang = mibaoStar.getChengzhang();
			int level = mibaoDB.getLevel();
			int gongji = MibaoMgr.inst.clacMibaoAttr(chengZhang,
					mibaoConf.getGongji(), mibaoConf.getGongjiRate(), level);
			int fangyu = MibaoMgr.inst.clacMibaoAttr(chengZhang,
					mibaoConf.getFangyu(), mibaoConf.getFangyuRate(), level);
			int shengming = MibaoMgr.inst.clacMibaoAttr(chengZhang,
					mibaoConf.getShengming(), mibaoConf.getShengmingRate(),
					level);
			junzhu.gongJi += gongji;
			junzhu.fangYu += fangyu;
			junzhu.shengMingMax += shengming;
			gongjiMibao += gongji;
			fangyuMibao += fangyu;
			shengmingMibao += shengming;

			b.setGongJi(b.getGongJi() + gongji);
			b.setFangYu(b.getFangYu() + fangyu);
			b.setShengMing(b.getShengMing() + shengming);
		}
		b.setGongjiMibao(gongjiMibao);
		b.setFangYuMibao(fangyuMibao);
		b.setShengMingMibao(shengmingMibao);
	}

	public void calcAtt(JunZhu junzhu) {
		Bag<EquipGrid> bag = EquipMgr.inst.loadEquips(junzhu.id);
		calcAtt(junzhu, bag);
		// 添加天赋属性 20150616 by wangZhuan
		calcTalentAttr(junzhu);
		// 添加符文属性 20150909 by hejincheng
		calcFuwenAttr(junzhu);
	}

	public void calcFuwenAttr(JunZhu jz) {
		List<String> lanweiList = Redis.getInstance().lgetList(FuwenMgr.CACHE_FUWEN_LANWEI+jz.id);
		JunzhuAttr.Builder attr = FuwenMgr.inst.getFuwenAttr(lanweiList);
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

	public void calcTalentAttr(JunZhu junzhu) {
		String where = "where junZhuId = " + junzhu.id;
		List<TalentPoint> listT = HibernateUtil.list(TalentPoint.class, where);
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

	public void calcAtt(JunZhu junzhu, Bag<EquipGrid> bag) {
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
//		log.info("------calcBase-----{}", System.currentTimeMillis() - time);
		// 装备
		time = System.currentTimeMillis();
		calcEquipAtt(junzhu, bag);
//		log.info("------calcEquipAtt-----{}", System.currentTimeMillis() - time);
	}

	// 计算秘宝的属性
	public void cacMiBaoAtt(JunZhu junzhu, List<MiBaoDB> mbs) {
		// 君主和装备的
		calcAtt(junzhu);
		if (mbs != null && mbs.size() != 0) {
			for (MiBaoDB mid : mbs) {
				if (mid.getLevel() <= 0) {
					continue;
				}
				MiBao mi = MibaoMgr.mibaoMap.get(mid.getMiBaoId());
				if (mi == null) {
					log.error("找不到秘宝配置，mibaoId:{}", mid.getMiBaoId());
					continue;
				}
				boolean lock = !mid.isClear();
				if(lock){
					if(MibaoMgr.inst.isLock(mi.unlockType, mi.unlockValue, junzhu)){
						continue;
					}
				}
				MibaoStar mibaoStar = MibaoMgr.inst.mibaoStarMap.get(mid
						.getStar());
				if (mibaoStar == null) {
					log.error("找不到对应的秘宝星级配置，mibaoDbId:{}, mibaoStar:{}",
							mid.getDbId(), mid.getStar());
					continue;
				}
				float chengZhang = mibaoStar.getChengzhang();
				int level = mid.getLevel();
				if (mi != null) {
					int gongji = MibaoMgr.inst.clacMibaoAttr(chengZhang,
							mi.getGongji(), mi.getGongjiRate(), level);
					int fangyu = MibaoMgr.inst.clacMibaoAttr(chengZhang,
							mi.getFangyu(), mi.getFangyuRate(), level);
					int shengming = MibaoMgr.inst.clacMibaoAttr(chengZhang,
							mi.getShengming(), mi.getShengmingRate(), level);
					junzhu.gongJi += gongji;
					junzhu.fangYu += fangyu;
					junzhu.shengMingMax += shengming;
				}
			}
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
			if (equip.getType() != BaseItem.TYPE_EQUIP) {
				continue;
			}
			// 装备基础属性
			ZhuangBei zb = (ZhuangBei) equip;
			minPinZhi = Math.min(minPinZhi, zb.pinZhi);
			junzhu.gongJi += zb.getGongji();
			junzhu.fangYu += zb.getFangyu();
			junzhu.shengMingMax += zb.getShengming();
			//以下1.0版本改变洗练逻辑
			UserEquip ue = HibernateUtil.find(UserEquip.class, gd.instId);
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
			junzhu.wqSH += ue.getWqSH();
			junzhu.wqJM += ue.getWqJM();
			junzhu.wqBJ += ue.getWqBJ();
			junzhu.wqRX += ue.getWqRX();
			junzhu.jnSH += ue.getJnSH();
			junzhu.jnJM += ue.getJnJM();
			junzhu.jnBJ += ue.getJnBJ();
			junzhu.jnRX += ue.getJnRX();
			//TODO战力计算规则 会因为新增5个洗练属性改变
			//以上1.0版本改变洗练逻辑
			
			// 强化
			UserEquip userEquip = ue;
			int lv = userEquip.getLevel();
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
				junzhu.gongJi += qianghua.getGongji();
				junzhu.fangYu += qianghua.getFangyu();
				junzhu.shengMingMax += qianghua.getShengming();
			}
		}
		try {
			calcTaoZhuang(junzhu, minPinZhi);
			calcTaoZhuangQianHua(junzhu, minQHlv);
		} catch (Exception e) {
			log.error("计算套装出错", e);
		}
	}

	/**
	 * 计算套装（强化）
	 * 
	 * @param junzhu
	 * @param minColor
	 */
	public void calcTaoZhuangQianHua(JunZhu junzhu, int minQHlv) {
		int idx;
		if (minQHlv >= 100) {
			idx = 8;
		} else if (minQHlv >= 80) {
			idx = 7;
		} else if (minQHlv >= 60) {
			idx = 6;
		} else if (minQHlv >= 30) {
			idx = 5;
		} else {
			return;
		}
		idx -= 1;// 5678->4567
		List list = TempletService.listAll(TaoZhuang.class.getSimpleName());
		if (list == null) {
			log.error("没有套装配置");
			return;
		}
		if (idx >= list.size()) {
			return;
		}
		TaoZhuang t = (TaoZhuang) list.get(idx);
		int[] key = { t.shuxing1, t.shuxing2, t.shuxing3 };
		int[] v = { t.num1, t.num2, t.num3 };
		int[] fv = new int[4];
		for (int i = 0; i < 3; i++) {
			switch (key[i]) {
			case 4:
				junzhu.wqSH += v[i];
				fv[0] = v[i];
				break;// 武器伤害加深
			case 5:
				junzhu.wqJM += v[i];
				fv[1] = v[i];
				break;// 武器伤害抵抗
			case 6:
				junzhu.jnSH += v[i];
				fv[2] = v[i];
				break;// 技能伤害加深
			case 7:
				junzhu.jnJM += v[i];
				fv[3] = v[i];
				break;// 技能伤害抵抗
			default:
				log.error("未处理的套装强化属性类型{}", key[i]);
				break;
			}
		}
		log.info("[{}]强化套装加成{},{},{},{}", fv[0], fv[1], fv[2], fv[3]);
	}

	/**
	 * 计算套装（品质）
	 * 
	 * @param junzhu
	 */
	public void calcTaoZhuang(JunZhu junzhu, int minPinZhi) {
		if(junzhu == null){
			return;
		}
		// 小于2的 不加成属性
		if (minPinZhi < 2) {
			return;
		}
		// 角色穿了套装进行的属性加成 是套装等级一下的所有套装等级都加上
		int gongji = 0;
		int fangyu = 0;
		int shengming = 0;
		log.info("junzhuId :{} minPinZhi is {} ", junzhu.id, minPinZhi);
		// 最小的品质从2开始
		for (int condition = 2; condition <= minPinZhi; condition++) {
			TaoZhuang t =  taoZhuangMap.get(condition);
			if (t != null) {
				log.info("junzhuId {}, 加成了 套装condition：{}", junzhu.id, condition);
				junzhu.gongJi += t.num1;
				junzhu.fangYu += t.num2;
				junzhu.shengMingMax += t.num3;
				gongji += t.num1;
				fangyu += t.num2;
				shengming += t.num3;
			}
		}
		log.info("[{}]套装总加成:{},{},{}", junzhu.name, gongji, fangyu, shengming);
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
			if (expTemp.getNeedExp() <= 0) {
				log.error("错误的等级经验{}", expTemp.getId());
				return;
			}
			if (jz.exp >= expTemp.getNeedExp()) {
				ExpTemp nextExpTemp = TempletService.getInstance().getExpTemp(
						1, jz.level + 1);
				if (nextExpTemp == null) {
					log.error("没有找到君主经验配置 等级{} ", jz.level);
					break;
				}
				jz.exp -= expTemp.getNeedExp();
				jz.level += 1;
				log.info("{} 升级至{}", jz.name, jz.level);
				ActLog.log.KingLvup(jz.id, jz.name, ActLog.vopenid, jz.level, jz.exp);
				CunLiangLog.inst.levelChange(jz.id, jz.level);
				// EventMgr.addEvent(ED.ACHIEVEMENT_PROCESS, new
				// AchievementCondition(
				// jz.id, AchievementConstants.type_junzhu_level, 1));
				EventMgr.addEvent(ED.junzhu_level_up, new Object[] { jz.id,
						jz.level, jz });//第三个参数是后来加的 2015年9月16日14:54:48
				JunzhuShengji shengji = getJunzhuShengjiByLevel(jz.level);
				updateTiLi(jz, shengji.addTili, "升级");
				// 2015-7-22 16:01 刷新君主榜
				EventMgr.addEvent(ED.JUN_RANK_REFRESH, jz);
				if(jz.level==RankingMgr.RANK_MINLEVEL){// 等级达到上榜条件，添加君主到百战榜，过关榜
					EventMgr.addEvent(ED.BAIZHAN_RANK_REFRESH, jz);
					EventMgr.addEvent(ED.GUOGUAN_RANK_REFRESH, jz);
				}
				// 2015-9-8 10:86 检查符文解锁事件
				EventMgr.addEvent(ED.CHECK_FUWEN_UOLOCK, jz);
				// 2015-9-22 12:04 符石推送红点
				EventMgr.addEvent(ED.FUSHI_PUSH, jz);
				log.info("{}升级增加体力{}达到{}", jz.name, shengji.addTili, jz.tiLi);
				levelChange = true;
				// 判断君主天赋是否要进行升级提示 20151010
				TalentMgr.instance.noticeTalentCanLevUp(jz.id);
				// 君主等级榜刷新
				EventMgr.addEvent(ED.JUNZHU_LEVEL_RANK_REFRESH, jz);
			} else {
				break;
			}
		} while (true);
		if (levelChange) {
			calcAtt(jz);
		}
		HibernateUtil.save(jz);
		SessionUser su = SessionManager.inst.findByJunZhuId(jz.id);
		if (su != null) {
			sendMainInfo(su.session);
			// 主界面战力
			sendPveMibaoZhanli(jz, su.session);
		}
		int Time = 0;//升级所用时间
		int Reason = 0;
		int SubReason = 0;
		OurLog.log.PlayerExpFlow(v, beforeLv, jz.level, Time, Reason, SubReason,
				String.valueOf(jz.id));
		ActLog.log.GetExp(jz.id, jz.name, ActLog.vopenid, ActLog.Reason, v, jz.exp);
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
		ActLog.log.PhysicalPower(junzhu.id, junzhu.name, ActLog.vopenid, preV, changeValue, junzhu.tiLi, reason);
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
					playerTime.setJunzhuId(jzId);
					playerTime.setLogoutTime(date);
					playerTime.setTotalOnlineTime(logoutTime - loginTime);
					HibernateUtil.insert(playerTime);
					// Redis中在线时间统计清空
					Redis.getInstance().set(
							AccountManager.CACHE_ONLINETIME + jzId,"0");
				} else {
					playerTime.setLogoutTime(date);
					long onlineTime = playerTime.getTotalOnlineTime();
					if (0 == onlineTime) {
						playerTime.setTotalOnlineTime(logoutTime - loginTime);
					} else {
						playerTime.setTotalOnlineTime(playerTime.getTotalOnlineTime()
								+ (logoutTime - loginTime));
					}
					HibernateUtil.save(playerTime);
					// Redis中在线时间统计清空
					Redis.getInstance().set(
							AccountManager.CACHE_ONLINETIME + jzId,"0");
				}
			break;
		case ED.JUNZHU_LOGIN:{
			jzId = (Long) evt.param;
			JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzId);
			//更新君主登录天数
			XianShiActivityMgr.instance.updateLoginDate(jzId);
			playerTime = HibernateUtil.find(PlayerTime.class, jzId);
			if (playerTime == null) {
				playerTime = new PlayerTime(jzId);
				playerTime.setJunzhuId(jzId);
				playerTime.setLoginTime(date);
				HibernateUtil.insert(playerTime);
			} else {
				playerTime.setLoginTime(date);
				HibernateUtil.save(playerTime);
			}
			List<HYResourceNpc> resourceNpcList = HibernateUtil.list(
					HYResourceNpc.class, " where battleJZId=" + jzId);
			for (HYResourceNpc npc : resourceNpcList) {
				npc.battleJZId = 0;
				HibernateUtil.save(npc);
			}
			List<HYTreasure> treasureList = HibernateUtil.list(
					HYTreasure.class, " where battleJunzhuId=" + jzId);
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
				playerTime = new PlayerTime(jzId);
				playerTime.setJunzhuId(junzhu.id);
				playerTime.setCreateRoleTime(date);
				HibernateUtil.insert(playerTime);
			} else {
				playerTime.setCreateRoleTime(date);
				HibernateUtil.save(playerTime);
			}
			/*
			 * 封测需求：初始君主vip等级和月卡 20150626
			 */
			initJunZhuVip(junzhu);
			break;
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
			int yueKaValid = CanShu.YUEKA_TIME;
			int chongZhiId = VipMgr.yuekaid;
			ChongZhi data = VipMgr.chongZhiTemp.get(chongZhiId);
			if (data == null) {
				log.error("ChongZhi配置中未找到相关数据条目:{}", chongZhiId);
				return;
			}
			if (vipInfo == null) {
				vipInfo = new PlayerVipInfo();
				vipInfo.accId = jid;
				vipInfo.sumAmount = 0;
				vipInfo.level = 0;
				vipInfo.vipExp = 0;
			}
			int addYB = VipMgr.INSTANCE.getAddyuanbao(data, true);
			YuanBaoMgr.inst.diff(jun, addYB, 0, 0, YBType.YB_FENGCE_VIP_YUEKA,
					"封测初始玩家vip月卡充值");
			int vipExp = vipInfo.vipExp + data.addVipExp;
			int vip = VipMgr.INSTANCE.getVipLevel(vipInfo.level, vipExp);
			jun.vipLevel = vip;
			HibernateUtil.save(jun);

			vipInfo.level = vip;
			vipInfo.vipExp = vipExp;
			HibernateUtil.save(vipInfo);

			VipRechargeRecord r = new VipRechargeRecord(jid, 0, new Date(), 0,
					vip, chongZhiId, addYB, yueKaValid);
			HibernateUtil.save(r);
			log.info("玩家初始化君主：{}，月卡，当前vip等级：{}， 当前元宝数：{}， 月卡持续天数:{}", jid, vip,
					jun.yuanBao, yueKaValid);
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
		junzhu.guoJiaId = guoJiaId;
		Bag<EquipGrid> equips = EquipMgr.inst.initEquip(junZhuId);
		log.info("------init equip-----{}", System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		calcAtt(junzhu, equips);
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
			if (shengji.getLv() == level) {
				junzhuShengji = shengji;
				break;
			}
		}
		return junzhuShengji;
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);
		EventMgr.regist(ED.ACC_LOGOUT, this);
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
		// 建立君主角色成功
		EventMgr.regist(ED.CREATE_JUNZHU_SUCCESS, this);
	}

	public int calcMibaoZhanLi(GameObject gameObj, int gongJi, int fangYu,
			int shengMing) {
		// 取得原来属性值
		int fangyuPre = gameObj.getFangyu();
		int gongjiPre = gameObj.getGongji();
		int shengmingPre = gameObj.getShengming();
		// 设为当前秘宝属性值
		gameObj.setFangyu(gongJi);
		gameObj.setGongji(fangYu);
		gameObj.setShengming(shengMing);
		int zhanLi = getZhanli(gameObj);
		// 计算完战力后， 重置原来的属性值
		gameObj.setFangyu(fangyuPre);
		gameObj.setGongji(gongjiPre);
		gameObj.setShengming(shengmingPre);
		return zhanLi;
	}

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
		log.info("参与战力值计算的各项属性值:[gongji=" + gongji + ", fangyu=" + fangyu
				+ ", shengming=" + shengming + ", wqshjs=" + wqshjs
				+ ", wqshdk=" + wqshdk + ", wqbjjs=" + wqbjjs + ", wqbjdk="
				+ wqbjdk + ", jnshjs=" + jnshjs + ", jnshdk=" + jnshdk
				+ ", jnbjjs=" + jnbjjs + ", jnbjdk=" + jnbjdk + "]");
		double m = CanShu.ZHANLI_M;
		double c = CanShu.ZHANLI_C;
		double r = CanShu.ZHANLI_R;
		double puGongQuan = CanShu.JUNZHU_PUGONG_QUANZHONG;
		double puGongBei = CanShu.JUNZHU_PUGONG_BEISHU;
		double jiNengQuan = CanShu.JUNZHU_JINENG_QUANZHONG;
		double jiNengBei = CanShu.JUNZHU_JINENG_BEISHU;
		double zhanliL = CanShu.ZHANLI_L;
		double baoJiLv = 0.2;
		double w = gongji + fangyu + shengming / c;
		double wq = puGongQuan * puGongBei;
		double wz = (1 + baoJiLv + wqshjs / zhanliL + wqbjjs / zhanliL)
				* (1 + baoJiLv + wqshdk / zhanliL + wqbjdk / zhanliL);
		double jN = jiNengQuan * jiNengBei;
		double jZ = (1 + baoJiLv + jnshjs / zhanliL + jnbjjs / zhanliL)
				* (1 + baoJiLv + jnshdk / zhanliL + jnbjdk / zhanliL);
		double result = m * w * Math.pow(wq * wz + jN * jZ, (1 / r));
		int zhanli = (int) Math.round(result);
		log.info("name 是 :{}的君主或者其他npc的综合战力是:{}", gameOb.getName(), zhanli);
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
	public void getPVEMiBaoZhanLi(IoSession session) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		sendPveMibaoZhanli(jz, session);
	}

	/*
	 * 去掉 pve战力，所有的战力统一为 getJunZhuZhanliFinally(junzhu)  20150831
	 */
	public void sendPveMibaoZhanli(JunZhu jz, IoSession session) {
//		if(jz == null)return;//jsp防错
//		List<MiBaoDB> mbs = HibernateUtil.list(MiBaoDB.class, " where ownerId="
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
		List<MiBaoDB> mbs = HibernateUtil.list(MiBaoDB.class, " where ownerId="
				+ jz.id);
		/*
		 * 获取战力不影响原来的 君主属性
		 */
		log.info("克隆之前 jz 的地址是 ：" + jz);
		JunZhu juClone = jz.clone();
		log.info("克隆之后 juClone 的地址是： " + juClone);
		log.info("克隆之后 jz 的地址是： " + jz);
	
		cacMiBaoAtt(juClone, mbs);
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
}
