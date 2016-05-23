package com.qx.bag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.BagOperProtos.EquipAddReq;
import qxmobile.protobuf.BagOperProtos.EquipDetail;
import qxmobile.protobuf.BagOperProtos.EquipDetailReq;
import qxmobile.protobuf.BagOperProtos.EquipRemoveReq;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.JiNengPeiYang;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.equip.domain.UserEquip;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.jinengpeiyang.JiNengPeiYangMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.LRUCache;
import com.qx.persistent.MC;

/**
 * 管理武器的穿上，卸下。
 * @author 康建虎
 *
 */
public class EquipMgr extends EventProc{
	public static Logger log = LoggerFactory.getLogger(EquipMgr.class);
	public static int maxGridCount = 9;
	public static int spaceFactor = 100;
	public static EquipMgr inst;
	/** 装备部位：头部*/
	public static final byte EQUIP_HEAD = 11;
	public EquipMgr(){
		inst = this;
	}
	public Bag<EquipGrid> loadEquips(long pid){
		long base = pid * spaceFactor;
		long start = base;
		long end = start + maxGridCount - 1;
		String[] keys = getKeys(pid);
		if(keys==null){
			Bag<EquipGrid> bag = new Bag<EquipGrid>();
//			bag.grids = Collections.EMPTY_LIST;
			bag.ownerId = pid;
			return bag;
		}
		Object[] mcArr = MemcachedCRUD.getMemCachedClient().getMultiArray(keys);
		List<EquipGrid> list = null; 
		if(mcArr[4] == null){//第一个取不到，认为MC里没有。
			long time = System.currentTimeMillis();
			list = loadFromDB(base, start, end);
			log.info("11------loadEquipFrom Db -----{}", System.currentTimeMillis() - time);
			for(EquipGrid eg : list){//逐个放入MC
				if(eg != null){
					MC.add(eg, eg.dbId);
				}
			}
		}else{
			//FIXME 避免总是创建ArrayList，考虑内存缓存。
			list = new ArrayList<EquipGrid>(mcArr.length);
			for(Object o : mcArr){
				list.add((EquipGrid)o);
			}
		}
		Bag<EquipGrid> bag = new Bag<EquipGrid>();
		bag.grids = list;
		bag.ownerId = pid;
		return bag;
	}
	public Bag<EquipGrid> initEquip(long pid){
		long base = pid * spaceFactor;
		List<EquipGrid> list = new ArrayList<EquipGrid>(9);
		for(int i=0; i<9; i++){
			int giveId = -1;
			if(i==3){
				giveId = (int)CanShu.CHUSHIHUA_CHUANDAIZHUANGBEI_1;
			}else if(i==4){
				giveId = (int)CanShu.CHUSHIHUA_CHUANDAIZHUANGBEI_2;
			}else if(i==5){
				giveId = (int)CanShu.CHUSHIHUA_CHUANDAIZHUANGBEI_3;
			}else{
				giveId = -1;
			}
			if(giveId>0){
				EquipGrid eg = new EquipGrid();
				eg.dbId = base + i;
				eg.instId = -1;
				eg.itemId = giveId;
				HibernateUtil.insert(eg);
				MC.add(eg, eg.dbId);
				list.add(eg);
				log.info("新手送装备 dbId:{},itemId:{}", eg.dbId, eg.itemId);
			}else{
				list.add(null);
			}
		}
		Bag<EquipGrid> bag = new Bag<EquipGrid>();
		bag.grids = list;
		bag.ownerId = pid;
		return bag;
	}
	public List<EquipGrid> loadFromDB(long base, long start, long end) {
		List<EquipGrid> list = HibernateUtil.list(EquipGrid.class, "where dbId >= "+start+" and dbId<="+end);
		return list;
	}
	public String[] getKeys(long pid) {
		//FIXME 使用LRUMap缓存
		long base = pid * spaceFactor;
		String [ ] arr = new String[maxGridCount];
		String head = "EquipGrid#";
		for(int i=0; i<maxGridCount; i++){
			arr[i] = head+(base+i);
		}
		return arr;
	}
	public void equipRemove(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null junZhu id");
			return;
		}
		EquipRemoveReq.Builder b = (qxmobile.protobuf.BagOperProtos.EquipRemoveReq.Builder) builder;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhuId);
		Bag<EquipGrid> equips = loadEquips(junZhuId);
		equipRemove(session,equips, bag, b.getGridIndex());
	}
	public void equipAdd(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null junZhu id");
			return;
		}
		EquipAddReq.Builder b = (qxmobile.protobuf.BagOperProtos.EquipAddReq.Builder) builder;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhuId);
		Bag<EquipGrid> equips = loadEquips(junZhuId);
		equipAdd(session, equips, bag, b.getGridIndex());
	}
	public void equipRemove(IoSession session, Bag<EquipGrid> equips, Bag<BagGrid> bag, int indexInEquip){
		if(indexInEquip<0 || indexInEquip>= equips.grids.size()){
			log.error("数据错误，bagIndex {}", indexInEquip);
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, bag.ownerId);
		if(jz == null) return;
		EquipGrid eg = equips.grids.get(indexInEquip);
		if(eg == null){
			log.error("equip grid is null at {}", indexInEquip);
			return;
		}
		if(isBagFull(bag)){
			log.error("背包满，不能脱下装备。");
			return;
		}
		if(eg.itemId<=0){
			log.error("此部位没有装备。");
			return;
		}
		//===========
		int preTid = eg.itemId;
		long instId = eg.instId;
		eg.itemId = -1;
		eg.instId = -1;
		HibernateUtil.save(eg);
		log.info("remove equip {} instId {} from {}",preTid,instId, bag.ownerId);
		BagMgr.inst.addItem(bag, preTid, 1, instId,jz.level,"脱下装备");
		log.info("add  {} instId {} to bag {}",preTid, instId, equips.ownerId);
		BagMgr.inst.sendBagInfo(session, bag);
		BagMgr.inst.sendEquipInfo(session, equips);
		JunZhuMgr.inst.sendMainInfo(session,jz);
		// 刷新君主榜
		EventMgr.addEvent(ED.JUN_RANK_REFRESH, jz);
	}
	public boolean isBagFull(Bag<BagGrid> bag){
		return bag.grids.size()>=BagMgr.maxGridCount;
	}
	/**
	 * 请勿直接调用此方法，请在外层做好数据检查。
	 * @param session 
	 * @param equips
	 * @param bag
	 * @param indexInBag
	 */
	public void equipAdd(IoSession session, Bag<EquipGrid> equips, Bag<BagGrid> bag, int indexInBag){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			return;
		}
		if(indexInBag<0 || indexInBag>= bag.grids.size()){
			log.error("数据错误，bagIndex {}", indexInBag);
			return;
		}
		BagGrid bg = bag.grids.get(indexInBag);
		if(bg == null){
			log.error("grid is null at {},空的格1", indexInBag);
//			sendError(session, "空的格子，号码"+indexInBag);
			return;
		}
		if(bg.cnt<=0){
			log.error("grid is empty cnt {} at {},空的格子2", bg.cnt, indexInBag);
//			sendError(session, "空的格子，号码"+indexInBag);
			return;
		}
		//===========
		BaseItem o = TempletService.itemMap.get(bg.itemId);
		if(o == null){
			log.error("物品没有找到 {}",bg.itemId);
			return;
		}
		BaseItem.TYPE_BAO_XIANG = 101;
		if(o.getType() == BaseItem.TYPE_BAO_XIANG 
				|| o.getType() == BaseItem.TYPE_TONG_BI_TAN_BAO
				|| o.getType() == BaseItem.TYPE_YUAN_BAO_TAN_BAO
				){
			BagMgr.inst.useItem(session,bag,indexInBag,o, junZhu);
			return;
		}
		if(o.getType() != BaseItem.TYPE_EQUIP){
//			sendError(session, "不是装备："+o.getName());
			log.error("位置{}不是装备{}", indexInBag, HeroService.getNameById(o.getName()));
			return;
		}
		ZhuangBei zb = (ZhuangBei) o;
		int slot = -1;
		switch(zb.getBuWei()){
		case 1:slot=3;break;//刀
		case 2:slot=4;break;//枪
		case 3:slot=5;break;//弓
		case 11:slot=0;break;//头盔
		case 12:slot=8;break;//肩膀
		case 13:slot=1;break;//铠甲
		case 14:slot=7;break;//手套
		case 15:slot=2;break;//裤子
		case 16:slot=6;break;//鞋子
		}
		if(slot<0){
//			sendError(session, "装备部位不对："+o.getName());
			log.error( "装备部位不对：-{}"+o.getName());
			return;
		}
		List<EquipGrid> list = equips.grids;
		EquipGrid preEg = null;
		if(slot<list.size()){
			preEg = list.get(slot);
		}
		EquipGrid eg = null;
		if(preEg == null){
			eg = new EquipGrid();
			eg.dbId = bag.ownerId * spaceFactor + slot;
			eg.instId = bg.instId;
			eg.itemId = bg.itemId;
			MC.add(eg, eg.dbId);
			HibernateUtil.insert(eg);
			equips.grids.set(slot, eg);
			
			bg.instId = bg.itemId = bg.cnt = 0;
			HibernateUtil.save(bg);
			log.info("remove {} {} from {}",eg.itemId, eg.instId, bag.ownerId);
		}else if(preEg.itemId<=0){
			第一次得到弓配合刘畅播放语音(junZhu, zb);
			preEg.instId = bg.instId;
			preEg.itemId = bg.itemId;
			
			bg.instId = bg.itemId = bg.cnt = 0;
			HibernateUtil.save(bg);
			
			log.info("remove {} {} from {}",bg.itemId, bg.instId, bag.ownerId);
			eg = preEg;
		}else{// 表示装备替换
			if(!isChangeEquip(preEg.itemId, bg.itemId)) {
				log.error( "装备替换失败1：reEg.itemId-{}，bg.itemId--{}" ,preEg.itemId, bg.itemId);
//				sendError(session, "装备替换失败1");
				return;
			}
			
			int qhTotalExp = getQiangHuaTotalExp(preEg.itemId, preEg.instId);
			ZhuangBei targetZb = TempletService.equipMaps.get(bg.itemId);
			if(targetZb == null) {
				log.error( "装备替换失败2：targetZb == null,reEg.itemId-{}，bg.itemId--{}" ,preEg.itemId, bg.itemId);
//				sendError(session, "装备替换失败2");
				return;
			}
			List<ExpTemp> expTemps = TempletService.getInstance().getExpTemps(targetZb.getExpId());
			Collections.sort(expTemps);
			int afterLevel = 0; // 初始等級為0
			int afterExp = qhTotalExp;
			for (ExpTemp temp : expTemps) {
				if (temp.getNeedExp() == -1) {// 表示满级
					break;
				}
				if (afterExp >= temp.getNeedExp()) {
					afterLevel += 1;
					afterExp -= temp.getNeedExp();
				}
			}
			// 获取强化洗练信息
			UserEquip dbUe = null;
			if(preEg.instId > 0){
				dbUe = HibernateUtil.find(UserEquip.class, preEg.instId);
				if(dbUe == null){
					log.error( "已经强化的数据丢失了,preEg.instId-{}" ,preEg.instId);
//					sendError(session, "已经强化的数据丢失了。");
					return ;
				} 
				//记录旧的进阶经验，并且加上被替换的装备的进阶经验
				int jinjieExp = dbUe.JinJieExp+TempletService.getInstance().getZhuangBei(preEg.itemId).exp;
				dbUe.setTemplateId(targetZb.getId());
				dbUe.setLevel(afterLevel);	
				dbUe.setExp(afterExp);
				dbUe.JinJieExp = jinjieExp ;
				HibernateUtil.save(dbUe);
			}else{
				dbUe = new UserEquip();
				int jinjieExp = TempletService.getInstance().getZhuangBei(preEg.itemId).exp;
				dbUe.setUserId(junZhu.id);
				dbUe.setTemplateId(targetZb.getId());
				dbUe.setLevel(afterLevel);	
				dbUe.setExp(afterExp);
				dbUe.JinJieExp = jinjieExp ;
				HibernateUtil.insert(dbUe);
				MC.add(dbUe, dbUe.getIdentifier());
			}
			
			/* 之前是替换的代码
			int preTid = preEg.itemId;
			long preInstId = preEg.instId;
			eg = preEg;
			eg.instId = bg.instId;
			eg.itemId = bg.itemId;
			
			log.info("之前有装备 {} {} at pid{}",bg.itemId, bg.instId, bag.ownerId);
			bg.itemId = preTid;
			bg.instId = preInstId;
			*/
			int beforeZbId = preEg.itemId;
			eg = preEg;
			eg.itemId = targetZb.getId();
			log.info("之前有装备 {} 替换成 {} , 当前强化等级:{} 经验:{}", beforeZbId, targetZb.getId(), afterLevel, afterExp);
			BagMgr.inst.removeItem(bag, bg.itemId, 1, "装备替换", junZhu.level);
		}
		HibernateUtil.save(eg);
		log.info("add equip {} {} to {}",eg.itemId, eg.instId, equips.ownerId);
		BagMgr.inst.sendBagInfo(session, bag);
		BagMgr.inst.sendEquipInfo(session, equips);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		// 事件管理中添加穿装备事件
		EventMgr.addEvent(ED.EQUIP_ADD, new Object[]{equips.ownerId, zb.getId(), equips});
		// 刷新君主榜
		EventMgr.addEvent(ED.JUN_RANK_REFRESH, junZhu);
	}
	public void 第一次得到弓配合刘畅播放语音(JunZhu junZhu, ZhuangBei zb) {
		try{
			if(zb.getBuWei() == 3) {
				List<JiNengPeiYang> jiNengPeiYangList = TempletService
						.listAll(JiNengPeiYang.class.getSimpleName());
				Optional<JiNengPeiYang> p = jiNengPeiYangList.stream().filter(t->t.id==3100).findFirst();// 3100
				if(p.isPresent()){
					JiNengPeiYangMgr.inst.forceAddNewJn(junZhu, p.get());
				}
			}
		}catch(Exception e){
			log.error("配合出错", e);
		}
	}
	
	protected boolean isChangeEquip(int curItemId, int targetItemId) {
		ZhuangBei curZb = TempletService.equipMaps.get(curItemId);
		if(curZb == null) {
			log.error("找不到zhuangbei配置，zhuangBeiId:{}", curItemId);
			return false;
		}
		ZhuangBei targetZb = TempletService.equipMaps.get(targetItemId);
		if(targetZb == null) {
			log.error("找不到zhuangbei配置，zhuangBeiId:{}", curItemId);
			return false;
		}
		if(targetZb.buWei != curZb.buWei) {
			log.error("替换装备部位不一致");
			return false;
		}
		if(targetZb.pinZhi <= curZb.pinZhi) {
			log.error("将要替换装备品质小于等于当前装备品质");
			return false;
		}
		return true;
	}
	
	
	protected int getQiangHuaTotalExp(int zhuangBeiId, long instId){
		ZhuangBei zbCfg = TempletService.equipMaps.get(zhuangBeiId);
		if(zbCfg == null) {
			log.error("找不到zhuangbei配置，zhuangBeiId:{}", zhuangBeiId);
			return 0;
		}
		UserEquip ue = HibernateUtil.find(UserEquip.class, instId);
		if(ue == null) {
			return 0;
		}
		int totalExp = 0;
		List<ExpTemp> expCfgList = TempletService.getInstance().getExpTemps(zbCfg.getExpId());
		if(expCfgList == null || expCfgList.size() == 0) {
			log.error("找不到ExpTemp id为{}的配置信息", zbCfg.getExpId());
			return 0;
		}
		int curQHLevel = ue.getLevel();
		for(ExpTemp et : expCfgList) {
			if(et.getLevel() < curQHLevel) {
				totalExp += et.getNeedExp();
			}
		}
		totalExp += ue.getExp();
		return totalExp;
	}
	
	public void sendEquipDetail(int id, IoSession session, Builder builder) {
		EquipDetailReq.Builder req = (qxmobile.protobuf.BagOperProtos.EquipDetailReq.Builder) builder;
		int itemId = req.getItemId();
		long instId = req.getInstId();
		BaseItem it = TempletService.itemMap.get(itemId);
		if(it == null){
			log.error("装备模板没有找到 {}",itemId);
			String msg = "没有找到装备"+itemId;
			sendError(session, msg);
			return;
		}else if(it instanceof ZhuangBei == false){
			log.error("不是装备 {}",itemId);
			String msg = "不是装备："+itemId;
			sendError(session, msg);
			return;
		}
		ZhuangBei zb = (ZhuangBei) it;
		EquipDetail.Builder ret = EquipDetail.newBuilder();
		ret.setItemId(itemId);
		ret.setInstId(instId);
		ret.setName(HeroService.getNameById(zb.getName()));
		ret.setDesc(HeroService.getNameById(zb.getFunDesc()));
		
		ret.setGongJi(zb.getGongji());
		ret.setFangYu(zb.getFangyu());
		ret.setShengMing(zb.getShengming());
		ret.setMouLi(zb.getMouli());
		ret.setTongShuai(zb.getTongli());
		ret.setWuYi(zb.getWuli());
		
		ret.setNeedLv(zb.getDengji());
		ret.setQiangHuaLv(zb.getQianghuaMaxLv());
		ret.setPinZhi(zb.getPinZhi());
		session.write(ret.build());
		log.debug("send equip detail");
	}
	public void sendError(IoSession session, String msg) {
		if(session == null){
			log.warn("session is null: {}",msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(1);
		test.setErrorDesc(msg);
		session.write(test.build());		
	}
	@Override
	public void proc(Event param) {
		switch(param.id){
		case ED.PVE_GUANQIA:
//			Object[] data = (Object[]) param.param;
//			removeInitailWeapon(data);//2015年12月25日11:49:48，不要这个功能了。
			break;
		}
	}
	/**
	 * 移除初始化的那个武器。
	 * @param data
	 */
	public void removeInitailWeapon(Object[] data) {
		Integer guanQiaId = (Integer) data[1];
		if(guanQiaId != 100103){
			return;
		}
		Long pid = (Long) data[0];
		Bag<EquipGrid> bag = loadEquips(pid);
		for(EquipGrid eg : bag.grids){
			if(eg == null || eg.itemId<=0){
				continue;
			}
			if(eg.itemId == CanShu.CHUSHIHUA_CHUANDAIZHUANGBEI_2){
				int preID = eg.itemId;
				eg.itemId = 0;
				HibernateUtil.save(eg);
				log.info("移除初始新手装备 {} at {}",
						pid, preID);
				break;
			}
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(pid);
		if(su != null && su.session!=null && su.session.isConnected()==true){
			BagMgr.inst.sendEquipInfo(0, su.session, null);
		}
	}
	
	public List<Integer> getEquipCfgIdList(JunZhu junzhu) {
		List<Integer> zbIdList = new ArrayList<Integer>();
		List<EquipGrid> equipList = loadEquips(junzhu.id).grids;
		for(EquipGrid eg : equipList){
			if(eg == null || eg.itemId<=0){
				continue;
			}
			zbIdList.add(eg.itemId);
		}
		return zbIdList;
	}
	
	@Override
	protected void doReg() {
		EventMgr.regist(ED.PVE_GUANQIA, this);
	}
	
//	public void putOn(Bag<EquipGrid> grids)
}
