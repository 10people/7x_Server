package com.qx.bag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import log.OurLog;
import log.parser.ReasonMgr;

import org.apache.commons.collections.map.LRUMap;
import org.apache.mina.core.session.IoSession;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.BagOperProtos.BagInfo;
import qxmobile.protobuf.BagOperProtos.BagItem;
import qxmobile.protobuf.BagOperProtos.EquipInfo;
import qxmobile.protobuf.BagOperProtos.EquipInfoOtherReq;
import qxmobile.protobuf.BagOperProtos.YuJueHeChengResult;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.Explore.TypeInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.Fuwen;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.Jiangli;
import com.manu.dynasty.template.QiangHua;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.award.AwardMgr;
import com.qx.award.DailyAwardMgr;
import com.qx.equip.domain.UserEquip;
import com.qx.equip.jewel.JewelMgr;
import com.qx.equip.web.UEConstant;
import com.qx.equip.web.UserEquipAction;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.explore.TanBaoData;
import com.qx.hero.HeroMgr; 
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.purchase.PurchaseMgr;

/**
 * 背包管理器
 * @author 康建虎
 *
 */
public class BagMgr {
	public static Logger log = LoggerFactory.getLogger(BagMgr.class);
	public static int maxGridCount = 100;
	public static int spaceFactor = 1000;
	public static int a_suit_of_gu_juan = 5; // 一套古卷的个数
	public static BagMgr inst;
	public BagMgr(){
		inst = this;
	}
	
	public Bag<BagGrid> loadBag(long pid){
		final long start = pid * spaceFactor;
		final long end = start + maxGridCount - 1;
		String bagCntKey = "BagCnt#"+pid;
		Object mcO = MemcachedCRUD.getMemCachedClient().get(bagCntKey);
		List<BagGrid> list = null;
		if(mcO == null){ 
			//MC中没有bag信息
			list = HibernateUtil.list(BagGrid.class, "where dbId >= "+start+" and dbId<="+end);
			long max = 0;
			for(BagGrid bg : list){
				max = Math.max(max, bg.dbId);
				boolean ret = MC.add(bg, bg.dbId);
				//log.info("add ret {}", ret);
			}
			Integer bagCnt = max==0 ? 0 : (int)((max-start)+1);//从0开始的，所以加1.
			MC.addKeyValue(bagCntKey, bagCnt);
		}else{
			Integer bagCnt = Integer.valueOf(mcO.toString());
			list = new ArrayList<BagGrid>(bagCnt);
			if(bagCnt>0){
				String[] keys = new String[bagCnt];
				for(int i=0; i<bagCnt; i++){
					keys[i] = "BagGrid#"+(start+i);
				}
				Object[] mcArr = MemcachedCRUD.getMemCachedClient().getMultiArray(keys);
				for(Object o : mcArr){
					if(o==null){
						continue;
					}
					list.add((BagGrid)o);
				}
			}
		}
		Bag<BagGrid> bag = new Bag<BagGrid>();
		bag.ownerId = pid;
		bag.grids = list;
		return bag;
	}
	
	/**
	 * 获取玩家背包某物品的总数量
	 * 
	 * @param bag		玩家背包对象
	 * @param itemId	物品itemId
	 * @return
	 */
	public int getItemCount(Bag<BagGrid> bag, int itemId){
		List<BagGrid> gridList = bag.grids;
		int count = 0;
		for(BagGrid grid : gridList){
			if(grid.itemId == itemId){
				count += grid.cnt;
			}
		}
		return count;
	}

	/**
	 *  获取玩家背包中 某一套东东，当前已经获得了其中的几类
	 *  比如，分别为金木水火土的 神农残卷 为一套。
	 * @param bag
	 * @param type
	 * @param itemId
	 * @return
	 */
	public int getItemTypeCountOfSameSuit(Bag<BagGrid> bag, int type, int itemId){
		List<BagGrid> gridList = bag.grids;
		ArrayList<Integer> idList = new ArrayList<Integer>();
		idList.add(itemId);
		int count = 1; // 当前这个算一种类
		for(BagGrid grid : gridList){
			int nextId = grid.itemId;
			if(!idList.contains(nextId) && grid.type == type){
				count += 1;
				idList.add(nextId);
			}
		}
		return count;
	}
	/**
	 * 可自动堆叠 2015/1/28
	 * @param bag
	 * @param itemId
	 * @param cnt
	 * @param instId
	 */
	public void addItem(Bag<BagGrid> bag, int itemId, int cnt, long instId, int jzLevel, String reason){
		if(bag.ownerId<=0){
			throw new IllegalArgumentException("Bag的ownerId不正确！"+bag.ownerId);
		}
		BaseItem bi = TempletService.itemMap.get(itemId);
		if(bi == null){
			log.error("没有这个道具，id："+itemId);
			return;
		}
		if(bi.getRepeatNum()<=0){
			log.error("不属于背包的物品， itemId {} ,cnt {},ownerid {}",itemId,cnt,bag.ownerId);
			//throw new IllegalArgumentException("不属于背包的物品，id："+itemId);
			return;
		}
		synchronized (bag) {
			final long start = bag.ownerId * spaceFactor;
			long slot = start;
			final long end = start + maxGridCount - 1;
			int remainNum = cnt;
			if(bag.grids.size()>0){
				// 先往已有该物品的位置叠加数量
				for(BagGrid gg : bag.grids){
					slot = Math.max(slot, gg.dbId);
					if(gg.itemId >0 && gg.itemId == itemId){
						if(gg.type == JewelMgr.Jewel_Type_Id || gg.type == 8){
							if (gg.instId != instId){
								continue ;
							}
						}
						int total = gg.cnt + remainNum;
						if(total <= bi.getRepeatNum()) {
							gg.cnt += remainNum;
							remainNum = 0;
							HibernateUtil.save(gg);
							break;
						} else if(gg.cnt < bi.getRepeatNum()){
							int addCnt = bi.getRepeatNum() - gg.cnt;
							remainNum -= addCnt;
							gg.cnt += addCnt;
							HibernateUtil.save(gg);
						}
						gg.cnt = Math.max(0, gg.cnt);//防止个数是负数，修正为0。
					}
				}
				// 再去找可以重复利用的格子
				if (remainNum > 0){
					for(BagGrid gg : bag.grids){
						if(gg.itemId<=0 || gg.cnt<=0){//没有被使用的格子。
							int num = 0;
							if(remainNum > bi.getRepeatNum()) {
								remainNum -= bi.getRepeatNum();
								num = bi.getRepeatNum();
							} else {
								num = remainNum;
								remainNum = 0;
							}
							gg.itemId = itemId;
							gg.cnt += num;
							gg.instId = instId;
							gg.type = bi.getType();
							HibernateUtil.save(gg);
							slot = Math.max(slot, gg.dbId);
							if(remainNum <= 0) {
								break;
							}
						}
					}
				}
				slot += 1;
			}
			//已经有格子了，但是没有合适的，则创建一个新的。dbId为最大格子Id+1
			BagGrid bg = null;//new BagGrid();
			while(remainNum > 0){
				int num = 0;
				if(remainNum > bi.getRepeatNum()) {
					remainNum -= bi.getRepeatNum();
					num = bi.getRepeatNum();
				} else {
					num = remainNum;
					remainNum = 0;
				}
				bg = new BagGrid();
				bg.dbId =  slot;
				bg.itemId = itemId;
				bg.cnt = num;//直接放入一个格子，未考虑一次给予的数量需要多个格子才能放下。 
				bag.grids.add(bg);//放入列表
				bg.instId = instId;
				bg.type = bi.getType();
				MC.add(bg, bg.dbId);
				HibernateUtil.insert(bg);
				log.info("插入背包 dbId:{}, itemId{}, cnt{}", bg.dbId, bg.itemId, bg.cnt);
				String bagCntKey = "BagCnt#"+bag.ownerId;
				//Object mcO = MemcachedCRUD.getMemCachedClient().get(bagCntKey);
				MemcachedCRUD.getMemCachedClient().set(bagCntKey, Long.valueOf(slot-start).intValue()+1);
				slot += 1;
				if(slot>end){
					log.error("{}道具数量超过限制。{}剩余{}",bag.ownerId,itemId,remainNum);
					break;
				}
			}
		}
		log.info("添加物品 {} instId {} 个数 {} 给玩家 {}",
				new Object[]{itemId, instId,cnt, bag.ownerId});
		int iGoodsType=0;
		int AfterCount=0;
		int Reason=ReasonMgr.inst.getId(reason);
		int SubReason = 0;
		int iMoney = 0;
		int MONEYTYPE_pay1_free0 = 0;
		OurLog.log.ItemFlow(jzLevel, iGoodsType, itemId, cnt, AfterCount, Reason, SubReason, iMoney, MONEYTYPE_pay1_free0, 1,
				String.valueOf(bag.ownerId));
		// 添加符石推送检测 2015-9-22
		if(bi.getType() == AwardMgr.type_fuWen){
			JunZhu jz = HibernateUtil.find(JunZhu.class, bag.ownerId);
			EventMgr.addEvent(ED.FUSHI_PUSH, jz);
		}
		//添加宝石镶嵌推送检测 2016-05-19
		if(bi.getType() == JewelMgr.Jewel_Type_Id){
			JunZhu jz = HibernateUtil.find(JunZhu.class, bag.ownerId);
			EventMgr.addEvent(ED.get_BaoShi, jz);
		}
	}
	public String getItemName(int id){
		BaseItem o = TempletService.itemMap.get(id);
		if(o == null){
			return "miss"+id;
		}
		return HeroService.getNameById(o.getName());
	}

	public void sendBagInfo(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null acc id");
			return;
		}
		Bag<BagGrid> bag = loadBag(junZhuId);
		sendBagInfo(session, bag);
	}

	public void sendBagInfo(IoSession session, Bag<BagGrid> bag) {
		BagInfo.Builder b = buildBag(bag);
		session.write(b.build());
		log.debug("send bag info");
	}

	public BagInfo.Builder buildBag(Bag<BagGrid> bag) {
		BagInfo.Builder b = BagInfo.newBuilder();
		List<BagGrid> list = bag.grids;
		int cnt = list.size();
		for(int i=0; i<cnt; i++){
			BagGrid gd = list.get(i);
			BagItem.Builder item = BagItem.newBuilder();
			item.setDbId(gd == null ? -1 : gd.dbId);
			if(gd == null || gd.itemId<=0 || gd.cnt<=0){
				item.setItemId(-1);
			}else{
				BaseItem o = TempletService.itemMap.get(gd.itemId);
				if(o == null){
					log.error("背包中的物品ID错误 {}", gd.itemId);
					item.setItemId(-1);
				}else{
					item.setItemId(gd.itemId);
					item.setItemType(o.getType());
					item.setName(HeroService.getNameById(o.getName()));
					item.setPinZhi(o.getPinZhi());
					item.setInstId(gd.instId);
					item.setCnt(gd.cnt);
					if(item.getItemType() == BaseItem.TYPE_EQUIP){
						ZhuangBei zb = (ZhuangBei) o;
						item.setGongJi(zb.getGongji());
						item.setFangYu(zb.getFangyu());
						item.setShengMing(zb.getShengming());
						item.setBuWei(zb.getBuWei());
						//FIXME 需要计算强化加成。
						item.setTongShuai(zb.getTongli());
						item.setWuYi(zb.getWuli());
						item.setMouLi(zb.getMouli());
						UserEquip ue=null;
						if(gd.instId>0){
							ue = HibernateUtil.find(UserEquip.class, gd.instId);
							item.setQiangHuaLv(ue == null ? 0 : ue.getLevel());
							item.setJinJieExp(ue == null ? 0 :ue.JinJieExp);
						}else{
							item.setQiangHuaLv(0);
						}
						fillEquipAtt(item, zb,ue);
						//不发描述了，客户端查表
						//item.setDesc(HeroService.getNameById(zb.getFunDesc()));
						item.setQianghuaHighestLv(zb.getQianghuaMaxLv());
					//}else if(o instanceof ItemTemp){
						//item.setDesc(HeroService.getNameById(((ItemTemp)o).funDesc));
					}
					if(item.getItemType() == JewelMgr.Jewel_Type_Id){
						item.setQiangHuaExp((int)(gd.instId<0 ? 0 : gd.instId));
					}
				}
			}
			b.addItems(item);
		}
		return b;
	}

	public void sendEquipInfo(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null acc id");
			return;
		}
		Bag<EquipGrid> bag = EquipMgr.inst.loadEquips(junZhuId);
		sendEquipInfo(session, bag);
	}
	public void sendEquipInfo(IoSession session,Bag<EquipGrid> bag){
		EquipInfo.Builder b = getEquipInfo(bag);
		session.write(b.build());
		log.debug("send equip info");		
	}

	public EquipInfo.Builder getEquipInfo(Bag<EquipGrid> bag) {
		EquipInfo.Builder b = EquipInfo.newBuilder();
		List<EquipGrid> list = bag.grids;
		TempletService template = TempletService.getInstance();
		int size = list.size();
		int cnt = 9;//list.size();
		for(int i=0; i<cnt; i++){
			EquipGrid gd = null;
			if(i<size){
				gd = list.get(i);
			}
			BagItem.Builder item = BagItem.newBuilder();
			item.setDbId(gd == null ? -1 : gd.dbId);
			if(gd == null || gd.itemId<=0 ){
				item.setItemId(-1);
			}else{
				BaseItem o = TempletService.itemMap.get(gd.itemId);
				if(o == null){
					log.error("已穿上的装备模板ID错误 {}", gd.itemId);
					item.setItemId(-1);
				}else if(o.getType() != BaseItem.TYPE_EQUIP){
					log.error("已穿上的装备类型错误 {}", gd.itemId);
					item.setItemId(-1);
				}else{
					ZhuangBei zb = (ZhuangBei) o;
					item.setBuWei(zb.getBuWei());
					item.setItemId(gd.itemId);
					item.setItemType(BaseItem.TYPE_EQUIP);
					item.setName(getItemName(gd.itemId));
					item.setInstId(gd.instId);
					item.setPinZhi(zb.getPinZhi());
					item.setCnt(1);
					item.setGongJi(zb.getGongji());
					item.setFangYu(zb.getFangyu());
					item.setShengMing(zb.getShengming());
					item.setTongShuai(zb.getTongli());
					item.setWuYi(zb.getWuli());
					item.setMouLi(zb.getMouli());
					UserEquip ue = gd.instId > 0 ? HibernateUtil.find(UserEquip.class, gd.instId) : null;
					fillEquipAtt(item, zb,ue);//先把基础值放上，强化、洗练的值后面再加
					if(gd.instId>0){
						item.setQiangHuaLv(ue == null ? 0 : ue.getLevel());
						item.setQiangHuaExp(ue == null ? 0 : ue.getExp());
						if(ue != null){
							//洗练加成
							item.setMouLi(item.getMouLi()+ue.getMouli());
							item.setWuYi(item.getWuYi()+ue.getWuli());
							item.setTongShuai(item.getTongShuai()+ue.getTongli());
							//以下1.0版本改变洗练逻辑
							item.setWqSH(item.getWqSH() + ue.getWqSH());
							item.setWqJM(item.getWqJM() + ue.getWqJM());
							item.setWqBJ(item.getWqBJ() + ue.getWqBJ());
							item.setWqRX(item.getWqRX() + ue.getWqRX());
							item.setJnSH(item.getJnSH() + ue.getJnSH());
							item.setJnJM(item.getJnJM() + ue.getJnJM());
							item.setJnBJ(item.getJnBJ() + ue.getJnBJ());
							item.setJnRX(item.getJnRX() + ue.getJnRX());
							
							//TODO +5个属性 1.0暂时不做
//							item.setWqBJL(item.getWqBJL()+ ue.getWqBJL());  
//							item.setJnBJL(item.getJnBJL()+ ue.getJnBJL());  
//							item.setWqMBL(item.getWqMBL()+ ue.getWqMBL());  
//							item.setJnMBL(item.getJnMBL()+ ue.getJnMBL());  
							//以上1.0版本改变洗练逻辑
							
							//强化加成
							int qianghuaId = zb.getQianghuaId();
							if(ue.getLevel()>0){
								QiangHua qianghua = template.getQiangHua(qianghuaId, ue.getLevel());
								if(qianghua != null){
									item.setGongJi(item.getGongJi() + qianghua.getGongji());
									item.setFangYu(item.getFangYu()+ qianghua.getFangyu());
									item.setShengMing(item.getShengMing() + qianghua.getShengming());
								}
							}
							//宝石属性
							List<Long> jewelList = JewelMgr.inst.getJewelOnEquip(ue);
							for(long jewelInfo : jewelList){
								if(jewelInfo >0){
									int jewelId = (int)(jewelInfo >> 32);
									Fuwen jewelPeiZhi = JewelMgr.inst.jewelMap.get(jewelId);
									if (jewelPeiZhi != null ){
										int shuXingType = jewelPeiZhi.getShuxing();
										//宝石属性目前只有三种，如有改变，增加case ; 2016-05-24
										switch (shuXingType){
											case 1 :
												item.setGongJi(item.getGongJi() +  (int)jewelPeiZhi.getShuxingValue());
												break;
											case 2 :
												item.setFangYu(item.getFangYu()+ (int)jewelPeiZhi.getShuxingValue());
												break;
											case 3 :
												item.setShengMing(item.getShengMing() + (int)jewelPeiZhi.getShuxingValue());
												break;
											default:
												log.info("宝石增加属性添加了新的类型{}",shuXingType);
												break;
										}
									}
								}
							}
							//进阶经验
							item.setJinJieExp( ue.JinJieExp);
						}
					}else{
						item.setQiangHuaLv(0);
						item.setJinJieExp(0);
					}
					
					item.setDesc(HeroService.getNameById(zb.getFunDesc()));
					item.setQianghuaHighestLv(zb.getQianghuaMaxLv());
				}
			}
			b.addItems(item);
		}
		return b;
	}
	/**
	 * 请求别人装备信息
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void sendEquipInfoOther(int id, IoSession session, Builder builder) {
		EquipInfoOtherReq.Builder req = (qxmobile.protobuf.BagOperProtos.EquipInfoOtherReq.Builder)builder;
		if(req==null) return;
		long junZhuId = (long)req.getOwnerid();
		Bag<EquipGrid> bag = EquipMgr.inst.loadEquips(junZhuId);
		EquipInfo.Builder b = EquipInfo.newBuilder();
		List<EquipGrid> list = bag.grids;
		TempletService template = TempletService.getInstance();
		int size = list.size();
		int cnt = 9;//list.size();
		for(int i=0; i<cnt; i++){
			EquipGrid gd = null;
			if(i<size){
				gd = list.get(i);
			}
			BagItem.Builder item = BagItem.newBuilder();
			item.setDbId(gd == null ? -1 : gd.dbId);
			if(gd == null || gd.itemId<=0 ){
				item.setItemId(-1);
			}else{
				BaseItem o = TempletService.itemMap.get(gd.itemId);
				if(o == null){
					log.error("已穿上的装备模板ID错误 {}", gd.itemId);
					item.setItemId(-1);
				}else if(o.getType() != BaseItem.TYPE_EQUIP){
					log.error("已穿上的装备类型错误 {}", gd.itemId);
					item.setItemId(-1);
				}else{
					ZhuangBei zb = (ZhuangBei) o;
					switch (zb.buWei) {
					case HeroMgr.WEAPON_HEAVY:
					case HeroMgr.WEAPON_LIGHT:
					case HeroMgr.WEAPON_RANGED:
						item.setBuWei(zb.getBuWei());
						item.setItemId(gd.itemId);
						item.setItemType(BaseItem.TYPE_EQUIP);
						item.setName(getItemName(gd.itemId));
						item.setInstId(gd.instId);
						item.setPinZhi(zb.getPinZhi());
						item.setCnt(1);
						item.setGongJi(zb.getGongji());
						item.setFangYu(zb.getFangyu());
						item.setShengMing(zb.getShengming());
						item.setTongShuai(zb.getTongli());
						item.setWuYi(zb.getWuli());
						item.setMouLi(zb.getMouli());
						UserEquip ue = gd.instId>0 ? HibernateUtil.find(UserEquip.class, gd.instId) : null;
						fillEquipAtt(item, zb,ue);//先把基础值放上，强化、洗练的值后面再加
						if(gd.instId>0){
							item.setQiangHuaLv(ue == null ? 0 : ue.getLevel());
							if(ue != null){
								//洗练加成
								item.setMouLi(item.getMouLi()+ue.getMouli());
								item.setWuYi(item.getWuYi()+ue.getWuli());
								item.setTongShuai(item.getTongShuai()+ue.getTongli());
								
								//以下1.0版本改变洗练逻辑
								item.setWqSH(item.getWqSH() + ue.getWqSH());
								item.setWqJM(item.getWqJM() + ue.getWqJM());
								item.setWqBJ(item.getWqBJ() + ue.getWqBJ());
								item.setWqRX(item.getWqRX() + ue.getWqRX());
								item.setJnSH(item.getJnSH() + ue.getJnSH());
								item.setJnJM(item.getJnJM() + ue.getJnJM());
								item.setJnBJ(item.getJnBJ() + ue.getJnBJ());
								item.setJnRX(item.getJnRX() + ue.getJnRX());
								//TODO 5个新属性 1.0暂时不做
//								item.setWqBJL(item.getWqBJL()+ ue.getWqBJL());  
//								item.setJnBJL(item.getJnBJL()+ ue.getJnBJL());  
//								item.setWqMBL(item.getWqMBL()+ ue.getWqMBL());  
//								item.setJnMBL(item.getJnMBL()+ ue.getJnMBL());  
//								item.setJnCDReduce(item.getJnCDReduce() + ue.getJnCDReduce());   
 

								//以上1.0版本改变洗练逻辑
								
								//强化加成
								int qianghuaId = zb.getQianghuaId();
								if(ue.getLevel()>0){
									QiangHua qianghua = template.getQiangHua(qianghuaId, ue.getLevel());
									if(qianghua != null){
										item.setGongJi(item.getGongJi() + qianghua.getGongji());
										item.setFangYu(item.getFangYu()+ qianghua.getFangyu());
										item.setShengMing(item.getShengMing() + qianghua.getShengming());
									}
								}
								item.setJinJieExp(ue.JinJieExp);
							}
						}else{
							item.setQiangHuaLv(0);
							item.setJinJieExp(0);
						}
						item.setDesc(HeroService.getNameById(zb.getFunDesc()));
						item.setQianghuaHighestLv(zb.getQianghuaMaxLv());
						if(item.getItemType()==BaseItem.TYPE_EQUIP){// 装备
							b.addItems(item);
						}
						break;
					default:
						continue;
					}
				}
			}
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_EquipInfoOther;
		msg.builder = b;
		session.write(msg);
		log.debug("send other equip info");		
	}

	public void fillEquipAtt(BagItem.Builder item, ZhuangBei zb,UserEquip dbUe) {
		//TODO 2015年9月2日 修改加载属性 待验证
		//新增属性 2014年11月19日11:00:41
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zb.getId(),UEConstant.wqSH)) {
			item.setWqSH(zb.getWqSH());
		}else{
			item.setWqSH(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zb.getId(),UEConstant.wqJM)) {
			item.setWqJM(zb.getWqJM());
		}else{
			item.setWqJM(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zb.getId(),UEConstant.wqBJ)) {
			item.setWqBJ(zb.getWqBJ());
		}else{
			item.setWqBJ(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zb.getId(),UEConstant.wqRX)) {
			item.setWqRX(zb.getWqRX());
		}else{
			item.setWqRX(0);
		}
		
//		if (UserEquipAction.instance.getEquipTalent(dbUe,zb.getId(),UEConstant.wqBJL)) {
//			item.setWqBJL(zb.getWqBJL());
//		}else{
//			item.setWqBJL(0);
//		}
//		item.setWqBJL(11);//FIXME 假数据

		if (UserEquipAction.instance.hasEquipTalent(dbUe,zb.getId(),UEConstant.jnSH)) {
			item.setJnSH(zb.getJnSH());
		}else{
			item.setJnSH(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zb.getId(),UEConstant.jnJM)) {
			item.setJnJM(zb.getJnJM());
		}else{
			item.setJnJM(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zb.getId(),UEConstant.jnBJ)) {
			item.setJnBJ(zb.getJnBJ());
		}else{
			item.setJnBJ(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zb.getId(),UEConstant.jnRX)) {
			item.setJnRX(zb.getJnRX());
		}else{
			item.setJnRX(0);
		}
		
//		if (UserEquipAction.instance.getEquipTalent(dbUe,zb.getId(),UEConstant.jnBJL)) {
//			item.setJnBJL(zb.getJnBJL());
//		}else{
//			item.setJnBJL(0);
//		item.setJnBJL(11);//FIXME 假数据
//		}
	}

	public void yuJueHeCheng(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			return;
		}
		Jiangli awardConf = PurchaseMgr.inst.jiangliMap.get(300);
		if(awardConf == null){
			sendError(session, "合成奖励还没有准备好，请稍后再来！");
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		Bag<BagGrid> bag = loadBag(junZhuId);
		List<BagGrid> list = bag.grids;
		boolean match = true;
		//yuJueNeedCnt = 1;
		for(int yuJueId : yuJueIds){
			int cnt = getItemCount(bag, yuJueId);
			if(cnt<yuJueNeedCnt){
				match = false;
				break;
			}
		}
		if(!match){
			sendError(session, "所需的物品不足，请收集齐全后再来！");
			return;
		}
		for(int yuJueId : yuJueIds){
			removeItem(bag, yuJueId, yuJueNeedCnt, "玉玦合成",jz.level);
		}
		List<AwardTemp> awardList = DailyAwardMgr.inst.giveAward(session, awardConf, jz);
		YuJueHeChengResult.Builder ret = YuJueHeChengResult.newBuilder();
		for(AwardTemp a : awardList){
			BagItem.Builder bi = BagItem.newBuilder();
			bi.setItemId(a.getItemId());
			bi.setItemType(a.getItemType());
			bi.setCnt(a.getItemNum());
			bi.setDbId(0);
			ret.addItems(bi);
		}
		sendBagInfo(session, bag);
		session.write(ret.build());
	}
	/**
	 * 扣除前必须自行判断是否有足够数量的物品。
	 * @param bag
	 * @param itemId
	 * @param subNum
	 */
	public void removeItem(Bag<BagGrid> bag, int itemId, int subNum, String reason,int jzLevel){
		int Reason=ReasonMgr.inst.getId(reason);
		for(BagGrid grid : bag.grids){
			if(grid.itemId == itemId) {
				if(grid.cnt >= subNum){
					grid.cnt -= subNum;
					if(grid.cnt <= 0) {
						grid.itemId = -1;
						grid.cnt = 0;
						grid.instId = 0;
					}
					HibernateUtil.save(grid);
					log.info("从{}移除物品{}x{} 原因[{}]",
							bag.ownerId,itemId,subNum,reason);
					int SubReason=0;
					OurLog.log.ItemFlow(jzLevel, grid.type, itemId, subNum, grid.cnt, Reason, SubReason, 0, 0, 1,
							String.valueOf(bag.ownerId));
					break;
				} else {
					int curCnt = grid.cnt;
					subNum -= grid.cnt;
					grid.itemId = -1;
					grid.cnt = 0;
					grid.instId = 0;
					HibernateUtil.save(grid);
					log.info("从{}移除物品{}x{} 原因{}",
							bag.ownerId,itemId,curCnt,reason);
					int SubReason=0;
					OurLog.log.ItemFlow(jzLevel, grid.type, itemId, curCnt, 0, Reason, SubReason, 0, 0, 1,
							String.valueOf(bag.ownerId));
				}
			}
		}
	}
	
	/**
	 * 通过背包id来扣除物品
	 * @param bag
	 * @param dbId
	 * @param subNum
	 * @param reason
	 * @param jzLevel
	 */
	public boolean removeItemByBagdbId(Bag<BagGrid> bag, String reason, long dbId, int subNum, int jzLevel){
		int Reason=ReasonMgr.inst.getId(reason);
		for(BagGrid grid : bag.grids){
			if(grid.dbId == dbId) {
				if(grid.cnt < subNum) {
					log.error("扣除背包物品失败，dbId:{}的物品数量不足:{}", dbId, subNum);
					return false;
				}
				grid.cnt -= subNum;
				if(grid.cnt <= 0) {
					grid.itemId = -1;
					grid.cnt = 0;
					grid.instId = 0;
				}
				HibernateUtil.save(grid);
				log.info("从{}移除物品{}x{} 原因[{}]", bag.ownerId,grid.itemId,subNum,reason);
				int SubReason=0;
				OurLog.log.ItemFlow(jzLevel, grid.type, grid.itemId, subNum, grid.cnt, Reason, SubReason, 0, 0, 1,
						String.valueOf(bag.ownerId));
				return true;
			}
		}
		return false;
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
	int[] yuJueIds = new int[]{950001,950002,950003,950004,950005};
	int yuJueNeedCnt = 1;
	public  void useItem(IoSession session, Bag<BagGrid> bag,
			int indexInBag, BaseItem o, JunZhu junZhu) {
		if(o.getType() == BaseItem.TYPE_BAO_XIANG 
				){
			useItemBaoXiang(session, bag, indexInBag, o, junZhu);
		}else if(o.getType() == BaseItem.TYPE_YUAN_BAO_TAN_BAO){
			tanBao(session,bag,indexInBag,o, junZhu, TanBaoData.tongBi_normal_awardId);
		}else if(o.getType() == BaseItem.TYPE_TONG_BI_TAN_BAO){
			tanBao(session,bag,indexInBag,o, junZhu, TanBaoData.yuanBao_normal_awardId);
		}else{
			log.error("物品不能使用 ， id {}",o.getId());
		}
	}
	public void tanBao(IoSession session, Bag<BagGrid> bag, int indexInBag,
			BaseItem o, JunZhu junZhu, int awardId) {
		AwardTemp award = AwardMgr.inst.calcAwardTemp(awardId);
		if(award == null){
			log.error("使用物品探宝出现数据错误，物品id{}", o.getId());
			return;
		}
		ItemTemp it = (ItemTemp) o;
		BagGrid bg = bag.grids.get(indexInBag);
		final int cnt = bg.cnt;
		bg.cnt = 0;
		bg.itemId = 0;
		HibernateUtil.update(bg);
		log.info("{} 物品探宝 {} x {}，先删除该物品，下面给奖励",
				junZhu.id, it.id, cnt);
		AwardMgr.inst.giveReward(session, award, junZhu);
		log.info("{} 物品探宝 {} x {} 结束",
				junZhu.id, it.id, cnt);
		//
		ExploreResp.Builder ret = ExploreResp.newBuilder();
		ret.setSuccess(0);//
		//
		award2msg(ret,award);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_USE_ITEM;
		msg.builder = ret;
		session.write(msg);
		//
		sendBagInfo(session, bag);
	}

	public  void useItemBaoXiang(IoSession session, Bag<BagGrid> bag,
			int indexInBag, BaseItem o, JunZhu junZhu) {
		ItemTemp it = (ItemTemp) o;
		String drops = it.awardID;
		if(drops == null || drops.isEmpty()){
			log.error("conf error for item {}", o.getId());
			return;
		}
		int[] awardConf = TempletService.parseAwardString(drops);
		BagGrid bg = bag.grids.get(indexInBag);
		final int cnt = bg.cnt;
		bg.cnt = 0;
		bg.itemId = 0;
		HibernateUtil.update(bg);
		log.info("{} 开宝箱 {} x {}，先删除该物品，下面给奖励",
				junZhu.id, it.id, cnt);
		//
		ExploreResp.Builder ret = ExploreResp.newBuilder();
		ret.setSuccess(0);//
		//
		for(int i=0; i<cnt; i++){
			log.info("pid {}, id {} 第 {} 遍",junZhu.id,it.id, i+1);
			List<AwardTemp> hits = drop(session, junZhu, it, awardConf);
			awards2message(hits,ret);
		}
		log.info("{} 开宝箱 {} x {} 结束",
				junZhu.id, it.id, cnt);
		//
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_USE_ITEM;
		msg.builder = ret;
		session.write(msg);
		//
		sendBagInfo(session,bag);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);//同步铜币
	}

	public void awards2message(List<AwardTemp> hits,
			qxmobile.protobuf.Explore.ExploreResp.Builder ret) {
		int cnt = hits.size();
		for(int i=0; i<cnt; i++){
			AwardTemp o = hits.get(i);
			award2msg(ret, o);
		}
	}

	public void award2msg(qxmobile.protobuf.Explore.ExploreResp.Builder ret,
			AwardTemp o) {
		Award.Builder a = Award.newBuilder();
		a.setItemType(o.getItemType());
		a.setItemId(o.getItemId());
		a.setItemNumber(o.getItemNum());
		ret.addAwardsList(a);
	}

	public  List<AwardTemp> drop(IoSession session, JunZhu junZhu, ItemTemp it,
			int[] awardConf) {
		List<Integer> fistHitAwardIdList = AwardMgr.inst.getHitAwardId(awardConf, 0);
		List<AwardTemp> hitList = new ArrayList<AwardTemp>();
		for (Integer awardId : fistHitAwardIdList) {
			AwardTemp calcV = AwardMgr.inst.calcAwardTemp(awardId);
			if(calcV == null) {
				continue;
			}
			AwardMgr.inst.giveReward(session, calcV, junZhu,false,false);
			log.info("{} 开宝箱 {} 得到 {} x {}", junZhu.id,it.id,calcV.getId(),calcV.getItemNum());
			hitList.add(calcV);
		}
		return hitList;
	}
	
	
	public void sendHighlightItemIdsInBag(IoSession session){
		Long ll = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(ll == null){
			return;
		}
		long jzId = ll.longValue();
		final Set<String> confSet = TempletService.effectshowItemIds;
		if(confSet == null || confSet.isEmpty()){
			ProtobufMsg msg = new ProtobufMsg();
			msg.id = PD.S_GET_HighLight_item_ids;
			qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder builder = ErrorMessage.newBuilder();
			msg.builder = builder;
			builder.setCmd(0);
			builder.setErrorCode(0);
			builder.setErrorDesc("");
			session.write(msg);
			return;
		}
		confSet.add("180102");
		String key = "HighlightItemIdsInBag#"+jzId;
		final Set<String> showedSet = new HashSet<>();
		Set<String> redisData = Redis.getInstance().sget(key);
		if(redisData != null){
			showedSet.addAll(		redisData );
		}
		final Set<String> showedThisTime = new HashSet<>();
		
		final StringBuffer sb = new StringBuffer();
		sb.append("#");
		Bag<BagGrid> bag = loadBag(jzId);
		bag.grids.stream()
			.filter(gd->gd != null)
			.filter(gd->gd.itemId>0)
			.filter(gd->gd.cnt>0)
			.filter(gd->showedSet.contains(gd.itemId)==false)//没有展示过
			.filter(gd->confSet.contains(gd.itemId))//且配置了要展示
			.forEach(gd->{
				sb.append(gd.itemId);
				sb.append("#");
				showedThisTime.add(String.valueOf(gd.itemId));
			});
		
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_GET_HighLight_item_ids;
		qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder builder = ErrorMessage.newBuilder();
		msg.builder = builder;
		builder.setCmd(0);
		builder.setErrorCode(0);
		builder.setErrorDesc(sb.toString());
		session.write(msg);
		//
		if(showedThisTime.size()>0){
			String[] arr = new String[showedThisTime.size()];
			Redis.getInstance().sadd(key, showedThisTime.toArray(arr));
		}
	}
	
	/**
	 * @Description: 删除物品后推送背包信息给玩家
	 * @param jzId
	 */
	public  void sendBagAgain(Bag<BagGrid> bag) {
		SessionUser su = SessionManager.inst.findByJunZhuId(bag.ownerId);
		if (su != null) {
			log.info("从{}移除物品，推送背包信息给玩家", bag.ownerId);
			BagMgr.inst.sendBagInfo(su.session, bag);
		}
	}
}
