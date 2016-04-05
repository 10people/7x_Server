package com.qx.junzhu;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.UpActionProto.Page1Data;
import qxmobile.protobuf.UpActionProto.Page1ZhuangbeiData;
import qxmobile.protobuf.UpActionProto.Page2Data;
import qxmobile.protobuf.UpActionProto.Page3Data;
import qxmobile.protobuf.UpActionProto.UpAction_C_getData;
import qxmobile.protobuf.UpActionProto.UpAction_S_getData0;
import qxmobile.protobuf.UpActionProto.UpAction_S_getData1;
import qxmobile.protobuf.UpActionProto.UpAction_S_getData2;
import qxmobile.protobuf.UpActionProto.UpAction_S_getData3;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.Fuwen;
import com.manu.dynasty.template.MibaoStar;
import com.manu.dynasty.template.MibaoSuiPian;
import com.manu.dynasty.template.QiRiLiBao;
import com.manu.dynasty.template.Talent;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.dynasty.template.ZhuangBeiDiaoluo;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.equip.domain.UserEquip;
import com.qx.equip.web.UEConstant;
import com.qx.equip.web.UserEquipAction;
import com.qx.fuwen.FuwenMgr;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;

/**
 * 我要变强
 * @author 康建虎
 *
 */
public class GrowUpMgr {
	public static GrowUpMgr inst;
	public static Logger log = LoggerFactory.getLogger(GrowUpMgr.class.getSimpleName());
	public GrowUpMgr(){
		inst = this;
	}
	public void getPageInfo(int id, IoSession session, Builder builder) {
		UpAction_C_getData.Builder req = (qxmobile.protobuf.UpActionProto.UpAction_C_getData.Builder) builder;
		if(req == null){
			return;
		}
		//当前页数0-3 0君主 1装备 2密保 3符文
		switch(req.getPage()){
		case 0:
			sendJunZhu(session);
			break;
		case 1:
			sendZhuangBei(session);
			break;
		case 2:
			sendMiBao(session);
			break;
		case 3:
			sendFuWen(session);
			break;
		}
		log.info("sid {} get page {}", session.getId(), req.getPage());
	}
	public void sendFuWen(IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		UpAction_S_getData3.Builder ret = UpAction_S_getData3.newBuilder();
		{//宝石
			Page3Data.Builder b = Page3Data.newBuilder();
			b.setCurLevel(FuwenMgr.inst.getFushiCurLevel(jz.id, 2));
			b.setMaxLevel(FuwenMgr.inst.getFushiMaxLevel(jz.id, 2));
			{
				List<Fuwen> list = FuwenMgr.inst.getFuShiTuijian(jz.id, 2);
				for(Fuwen fw : list){
					b.addFuwenDataId(fw.getFuwenID());
				}
			}
			ret.addPageData(b.build());
			
		}
		{//符文
			Page3Data.Builder b = Page3Data.newBuilder();
			b.setCurLevel(FuwenMgr.inst.getFushiCurLevel(jz.id, 1));
			b.setMaxLevel(FuwenMgr.inst.getFushiMaxLevel(jz.id, 1));
			List<Fuwen> list = FuwenMgr.inst.getFuShiTuijian(jz.id, 1);
			for(Fuwen fw : list){
				b.addFuwenDataId(fw.getFuwenID());
			}
			ret.addPageData(b.build());
		}
		
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_UPACTION_DATA_3;
		msg.builder = ret;
		session.write(msg);
	}
	protected void sendMiBao(IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		List<MiBaoDB> mibaoDBList = HibernateUtil.list(MiBaoDB.class, " where ownerId = " + jz.id);
		UpAction_S_getData2.Builder ret = UpAction_S_getData2.newBuilder();
		{/////////秘宝等级计算
			int curLevel = 0;
			Page2Data.Builder b = Page2Data.newBuilder();
			//b)	进度=当前等级之和/君主等级/N ×100%（四舍五入）
			int sumLv = 0;
			int lvGt0Cnt = 0;
			for(MiBaoDB db : mibaoDBList){
				if(db.getLevel()<=0)continue;
//				if(db.isClear()==false)continue;
				lvGt0Cnt++;
				sumLv += db.getLevel();
			}
			if(lvGt0Cnt>0){
				curLevel = Math.round(sumLv*100.0f/lvGt0Cnt/jz.level);
			}
			//前端没有四舍五入，后端来模拟
			b.setCurLevel(curLevel);// = 1;//当前等级(进度)
			b.setMaxLevel(100/*jz.level*/);// = 2;//最大等级(进度)
			Collections.sort(mibaoDBList, new Comparator<MiBaoDB>(){

				@Override
				public int compare(MiBaoDB o1, MiBaoDB o2) {
					//没有激活的往后排
					return o1.getLevel()-o2.getLevel();
				}
				
			});
			{
				//mibaoDataId= 3;//每组3个密保的ID
				int tuiJian = 0;
				for(int i=0;i<mibaoDBList.size(); i++){
					if(i>=mibaoDBList.size())break;
					MiBaoDB d = mibaoDBList.get(i);
					if(d.getLevel() >= jz.level || d.getMiBaoId()<=0){
						continue;
//					}else if(d.isClear()==false){
//						continue;
					}
					b.addMibaoDataId(d.getMiBaoId());
					tuiJian++;
					if(tuiJian>=3)break;
				}
				if(mibaoDBList.size() == 0){//没有密保
					b.addMibaoDataId(301011);
				}
			}
			ret.addPageData(b);
		}
		{//计算秘宝升星
			//X为当前已有秘宝的星级数之和，Y为全部秘宝的星级数上限之和（v1.0版本为18×5=90
			Page2Data.Builder b = Page2Data.newBuilder();
			//b) 秘宝升级：	进度=当前等级之和/君主等级/N ×100%（四舍五入）
			// X/Y
			int sumStar = 0;
			for(MiBaoDB db : mibaoDBList){
				if(db.getMiBaoId()<=0)continue;
				sumStar += db.getStar();
			}
			b.setCurLevel(sumStar);// = 1;//当前等级(进度)
			b.setMaxLevel(105);// = 2;//最大等级(进度)
			Collections.sort(mibaoDBList, new Comparator<MiBaoDB>(){

				@Override
				public int compare(MiBaoDB o1, MiBaoDB o2) {
					int v1,v2=0;
					v1 = get(o1);
					v2 = get(o2);
					return v1-v2;
				}

				private int get(MiBaoDB o1) {
					if(o1.getMiBaoId()<=0){
						MibaoSuiPian mibaoSuiPian1 = MibaoMgr.inst.mibaoSuipianMap.get(o1.getTempId());
						return mibaoSuiPian1.getHechengNum() - o1.getSuiPianNum();
					}else{
						MibaoStar starConf = MibaoMgr.inst.mibaoStarMap.get(o1.getStar());
						return (starConf==null ? 900 : starConf.getNeedNum()) - o1.getSuiPianNum();
					}
				}
				
			});
			{
				//mibaoDataId= 3;//每组3个密保的ID
				for(int i=0;i<mibaoDBList.size(); i++){
					MiBaoDB d = mibaoDBList.get(i);
					//星星和等级无关
//					if(d.getLevel() >= jz.level ){
//						continue;
//					}
					if(d.getStar()>=5){//满了 
						continue;
					}
//					if(d.isClear()==false){
//						continue;//未解锁
//					}
					int miBaoId = d.getMiBaoId();
					if(miBaoId<=0){
						miBaoId = 301000+(d.getTempId()/100*10)+(d.getTempId()%10);
					}
					b.addMibaoDataId(miBaoId);
					log.info("add mibao {}", miBaoId);
					if(b.getMibaoDataIdCount()>=3){
						break;
					}
				}
				if(mibaoDBList.size() == 0){//没有密保
					b.addMibaoDataId(301011);
				}
			}
			ret.addPageData(b);
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_UPACTION_DATA_2;
		msg.builder = ret;
		session.write(msg);
	}
	
	public void sendZhuangBei(IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		List<ZhuangBeiDiaoluo> diaoLuoList = TempletService.listAll(ZhuangBeiDiaoluo.class.getSimpleName());
		if(diaoLuoList == null)diaoLuoList = Collections.EMPTY_LIST;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(jz.id);
		int sumQHlv = 0;
		int sumPinZhi = 0;
		UserEquip ues[] = new UserEquip[9];
		String qhSort[] = new String[9];
		String jinJieSort[] = new String[9];
		//龌龊的排序方法，前面是排序的值，后面是格子的下标
		Arrays.fill(qhSort, "99999:8888");
		Arrays.fill(jinJieSort, "99999:8888");
		int idx = -1;
		for(EquipGrid bg : equips.grids){
			idx++;
			if(bg == null || bg.itemId<=0){
				continue;
			}
			for(ZhuangBeiDiaoluo dl : diaoLuoList){
				if(dl.zhuangbeiId == bg.itemId){
					jinJieSort[idx] = String.format("%02d",dl.id)+":"+idx;
					break;
				}
			}
			BaseItem bi = TempletService.itemMap.get(bg.itemId);
			sumPinZhi += bi == null ? 0 : bi.getPinZhi();
			if(bg.instId<=0){
				qhSort[idx] = "0:"+idx;
				continue;
			}
			UserEquip ue = HibernateUtil.find(UserEquip.class, bg.instId);
			if(ue == null){
				qhSort[idx] = "0:"+idx;
				continue;
			}
			ues[idx] = ue;
			int ue_level = ue.getLevel();
			sumQHlv+=ue_level;
			if(ue_level<jz.level){
				qhSort[idx] = ue.getLevel()+":"+idx;
			}
		}
		Arrays.sort(qhSort);
		Arrays.sort(jinJieSort);
		//a)	等级成长进度=（（装备当前等级之和/9）/当前强化上限）*100%（四舍五入取整显示）
		int maxQH = jz.level;
		int qhLv = Math.round(sumQHlv / 9.0f );
		
		UpAction_S_getData1.Builder ret = UpAction_S_getData1.newBuilder();
		{
			Page1Data.Builder b = Page1Data.newBuilder();
			b.setCurLevel(qhLv);
			b.setMaxLevel(maxQH);
			//推荐强化装备
			for(int i=0; i<9; i++){
				String lv = qhSort[i];
				if(lv.startsWith("99999:"))break;//已经排序了，一旦不满足，后面的都不满足
				int gridIdx = Integer.parseInt(lv.substring(lv.indexOf(":")+1));
				Page1ZhuangbeiData.Builder zb = Page1ZhuangbeiData.newBuilder();
				zb.setId(equips.grids.get(gridIdx).itemId);
				zb.setText("身上装备");
				zb.setType(0);
				b.addZhuangbeiData(zb.build());				
			}
			ret.addPageData(b.build());
			///////////////////////////////////强化处理结束
		}
		TempletService template = TempletService.getInstance();
		{
			Page1Data.Builder b = Page1Data.newBuilder();
			b.clearZhuangbeiData();
			//进阶
			//X为当前所有部位品质之和，Y为所有部位装备品质之和（当前版本为72）
			b.setCurLevel(sumPinZhi);
			b.setMaxLevel(99);
			{//计算进阶推荐
				//a)	进阶材料足够但玩家没点进阶的装备优先级最高
				idx = -1;
				int daoId = 0;
				for(EquipGrid bg : equips.grids){
					idx++;
					if(bg == null || bg.itemId<=0){
						continue;
					}
					if(idx == 3)daoId = bg.itemId;
					ZhuangBei zhuangBeiTmp = template.getZhuangBei(bg.itemId);
					if(zhuangBeiTmp == null)continue;
					int jinJieLV = zhuangBeiTmp.getJinjieLv();
					if (jz.level < jinJieLV) continue;
					int jinJieZbId = zhuangBeiTmp.getJiejieId();
					if (jinJieZbId <= 0) continue;
					int jinJieItemId = zhuangBeiTmp.getJinjieItem();
					int jinJieNum = zhuangBeiTmp.getJinjieNum();
					int haveNum = BagMgr.inst.getItemCount(bag, jinJieItemId);
					if(haveNum<jinJieNum)continue;
					//
					Page1ZhuangbeiData.Builder zb = Page1ZhuangbeiData.newBuilder();
					zb.setId(bg.itemId);
					zb.setText("身上装备");
					zb.setType(0);
					b.addZhuangbeiData(zb.build());	
				}
				boolean 已推荐刀 = false;
				if(b.getZhuangbeiDataCount()<3){
					//b)	若当前装备中存在七日礼包奖励进阶的装备，将最快获得的装备推荐出来
					//峻宇说只计算大刀就可以了。
					List<QiRiLiBao> daoList = TempletService.listAll(QiRiLiBao.class.getSimpleName());
					for(QiRiLiBao dao : daoList){
						if(dao.zhuangbeiId>daoId){
							Page1ZhuangbeiData.Builder zb = Page1ZhuangbeiData.newBuilder();
							zb.setId(dao.zhuangbeiId);
							zb.setText("七日礼包中第XX天获得".replace("XX", String.valueOf(dao.day)));
							zb.setType(1);
							b.addZhuangbeiData(zb.build());	
							已推荐刀 = true;
							break;
						}
					}
				}
				int size = b.getZhuangbeiDataCount();
				{
					//c)	依据进阶材料掉落的精英关卡从先到后的顺序将当前装备排序，
					//把推荐装备补充到三个以上，达到满阶的装备不推荐；
					//装备掉落的关卡顺序读..\..\数值文档\ZhuangBeiDiaoluo.xlsx
					for(int i=0; i<9 && size<3; i++){
						String lv = jinJieSort[i];
						if(lv.startsWith("99999:"))break;//已经排序了，一旦不满足，后面的都不满足
						int gridIdx = Integer.parseInt(lv.substring(lv.indexOf(":")+1));
						Page1ZhuangbeiData.Builder zb = Page1ZhuangbeiData.newBuilder();
						int itemId = equips.grids.get(gridIdx).itemId;
						if(已推荐刀  && itemId == daoId){
							continue;
						}
						ZhuangBei zhuangBeiTmp = template.getZhuangBei(itemId);
						if(zhuangBeiTmp==null)continue;
						if(zhuangBeiTmp.getJiejieId()<=0)continue;
						zb.setId(itemId);
						zb.setText("身上装备");
						zb.setType(0);
						b.addZhuangbeiData(zb.build());	
						size++;
					}
				}
			}
			ret.addPageData(b.build());
		}
		{
			Page1Data.Builder b = Page1Data.newBuilder();
			//洗练
			b.clearZhuangbeiData();
			{//a)	一件装备的洗练进度= 该装备当前洗练的属性之和/该装备洗练上限属性之和（不包括跟洗练无关的基础属性，洗练可出属性也包括），按目前洗练规则
			//	b)	总体进度=所有装备洗练进度之和/9 ×100% （四舍五入，无装备部位或不可洗练的装备不参与计算）
				String names[] = {"wqSH","wqJM","wqBJ","wqRX",
						"jnSH","jnJM","jnBJ","jnRX"};
				Field[] fs = new Field[names.length];
				Method ms[] = new Method[fs.length];
				Method msUE[] = new Method[fs.length];
				
				for(int fIdx = 0; fIdx<names.length; fIdx++){
					Field f = null;
					try {
						f = UEConstant.class.getDeclaredField(names[fIdx]);
					} catch (NoSuchFieldException | SecurityException e1) {
						log.error("反射出错NN", e1);
						continue;
					}
					f.setAccessible(true);
					fs[fIdx]=f;
					String name = f.getName();
					String mName = "get"+name.substring(0,1).toUpperCase()+name.substring(1);
					Method m = null;
					Method mUE = null;
					try {
						m = ZhuangBei.class.getDeclaredMethod(mName);
						mUE = UserEquip.class.getDeclaredMethod(mName);
					} catch (Exception e) {
						log.error("反射出错",e);
					}
					ms[fIdx] = m;
					msUE[fIdx] = mUE;
					//
				}
				idx=-1;
				int sumMaxXiLian = 0;
				float sumXiLian = 0;
				String[] xlSort = new String[9];
				Arrays.fill(xlSort, "9999:0");
				for(EquipGrid bg : equips.grids){
					idx++;
					if(bg == null || bg.itemId<=0){
						continue;
					}
					ZhuangBei zhuangBeiTmp = template.getZhuangBei(bg.itemId);
					if(zhuangBeiTmp == null)continue;
					UserEquip dbUe = ues[idx];
					if(zhuangBeiTmp.getPinZhi()>1){//可洗练
						if(dbUe == null){
							xlSort[idx] = "0:"+bg.itemId;
						}else{
							xlSort[idx] = dbUe.getXianlianzhi()+":"+bg.itemId;
						}
					}
					int singleJinDu = 0;
					int singleMax = 0;//防止除0
					for(int ff=0; ff<fs.length; ff++){
						if(fs[ff]==null)continue;
//						String code;
//						try {
//							code = (String)fs[ff].get(null);
//						} catch (Exception e) {
//							log.error("反射出错B", e);
//							continue;
//						}
						//hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnSH)
						//if(UserEquipAction.instance.hasEquipTalent(dbUe,zhuangBeiTmp.getId(),code)){
							Integer v = 0;
							Integer v2 = 0;
							try {
								v = (Integer) ms[ff].invoke(zhuangBeiTmp);
								v2 = dbUe == null ? 0 : (Integer) msUE[ff].invoke(dbUe);
							} catch (Exception e) {
								log.error("反射出错C", e);
								continue;
							}
							int maxJnSH=UserEquipAction.instance.getXiLianMaxValue(v, zhuangBeiTmp, dbUe == null? 0 :dbUe.getLevel());
							singleMax += maxJnSH > 0 ? maxJnSH : 0;
							singleJinDu += Math.abs(v2);
							log.info("{},{},{}", ms[ff].getName(), maxJnSH, v2);
						//}
					}
					if(singleMax>0){
						sumXiLian += singleJinDu*100f/(singleMax/2);//有8条属性，实际之能洗出4条，所以总值除以2
					}
					
				}
				Arrays.sort(xlSort);
				b.setCurLevel(Math.round(sumXiLian/9));//四舍五入
				b.setMaxLevel(100);//sumMaxXiLian>0 ? sumMaxXiLian : Integer.MAX_VALUE);
				{//推荐洗练装备
					for(int n=0;n<3; n++){
						String lv = xlSort[n];
						if(lv.startsWith("9999:"))break;//已经排序了，一旦不满足，后面的都不满足
						Page1ZhuangbeiData.Builder zb = Page1ZhuangbeiData.newBuilder();
						int itemId = Integer.parseInt(lv.substring(lv.indexOf(":")+1));
						zb.setId(itemId);
						zb.setText("身上装备");
						zb.setType(0);
						b.addZhuangbeiData(zb.build());	
					}
				}
				log.info("洗练 {},{}", sumXiLian, sumMaxXiLian);
			}
			ret.addPageData(b.build());
		}
		
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_UPACTION_DATA_1;
		msg.builder = ret;
		session.write(msg);
	}
	public int maxLevel = 100;
	public void sendJunZhu(IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		UpAction_S_getData0.Builder ret = UpAction_S_getData0.newBuilder();
		
		ret.setCurHeroLevel(jz.level);//当前英雄等级(进度)
		ret.setMaxHeroLevel(maxLevel);// = 2;//最大英雄等级(进度)
		
		//X为当前玩家各天赋等级之和，Y为全部天赋等级上限之和（v1.0版为1002）
		int talentSum = getTalentSum(jz);
		ret.setCurTanfuLevel(talentSum);// = 3;//当前天赋总等级(进度)
		ret.setMaxTanfuLevel(1002);// = 4;//最大天赋总等级(进度)
		
		//a)	从已点亮的进攻天赋和防御天赋中分别推荐出1个当前等级最低的天赋(等级相同随机推荐一个)；
		//全点满情况下不做推荐
		String where = "where junZhuId = " + jz.id;
		List<TalentPoint> listT = HibernateUtil.list(TalentPoint.class, where);
		//计算开启了但是没加点的
		Map<Integer, TalentPoint> dbMap = new HashMap<Integer, TalentPoint>();
		TalentAttr ta = HibernateUtil.find(TalentAttr.class, jz.id);
		for(TalentPoint p : listT){
			if(ta == null)break;
			if(p.point<200 && ta.jinGongDianShu>0){
				dbMap.put(p.point, p);
			}else if(p.point>=200 && ta.fangShouDianShu>0){
				dbMap.put(p.point, p);
			}
		}
		for(Talent tc : TalentMgr.talentMap.values()){
			if(dbMap.containsKey(tc.point))continue;
			String[] ar = tc.frontPoint.split(",");
			for(String s : ar){
				TalentPoint pre = dbMap.get(Integer.parseInt(s));
				if(pre == null)continue;
				if(pre.level<tc.frontLv)continue;
				//
				TalentPoint fakeT = new TalentPoint();
				fakeT.point = tc.point;
				listT.add(fakeT);
			}
		}
		TalentPoint min100P = null;
		TalentPoint min200P = null;
		for(TalentPoint p : listT){
			if(p.point<200){//xml数据有规律 挖坑了
				if(min100P == null || p.level<min100P.level){
					min100P = p;
				}
			}else{
				if(min200P == null || p.level<min200P.level){
					min200P = p;
				}
			}
		}
		
		if(min100P == null){
			ret.addTianfuId(101);// tianfuId = 5;//每个天赋图标的ID
		}else if(min100P.point == 106 && min100P.level==1){//已满
		}else{
			ret.addTianfuId(min100P.point);
		}
		//
		if(min200P == null){
			ret.addTianfuId(201);// tianfuId = 5;//每个天赋图标的ID
		}else if(min200P.point==206 && min200P.level==1){//已满
		}else{
			ret.addTianfuId(min200P.point);
		}
		//
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_UPACTION_DATA_0;
		msg.builder = ret;
		session.write(msg);
	}
	protected int getTalentSum(JunZhu jz) {
		String sql = "select sum(level) from TalentPoint where junZhuId="+jz.id;
		int ret = HibernateUtil.getCount(sql);
		return ret;
	}
}
