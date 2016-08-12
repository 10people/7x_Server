package com.qx.equip.jewel;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.Fuwen;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.network.SessionManager;
import com.qx.account.FunctionOpenMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.equip.domain.UserEquip;
import com.qx.equip.domain.UserEquipDao;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.timeworker.FunctionID;

import qxmobile.protobuf.JewelProtos.EquipOperationReq;
import qxmobile.protobuf.JewelProtos.EquipOperationResp;
import qxmobile.protobuf.JewelProtos.JewelInfo;
import qxmobile.protobuf.JewelProtos.JewelList;

/**
 *宝石信息存储方式： 
 * 背包中的宝石，存在BagGrid表中：如果经验是初始经验，instId 填-1，如果不是，instId填经验
 * 装备上的宝石，存在UserEquip表中，5个栏位对应5个孔：
 * 存储方式为itemId强制转换为long类型，然后左移32位+经验
 * 根据itemId和exp生成存储信息：long info = (((long)itemId)<<32) + exp
 * 根据存储信息获取itemId ：int itemId = (int)( info>>32 )
 * 根据存储信息获取经验 ：int exp = (int)( info& Integer.MAX_VALUE )
 * */

public class JewelMgr extends EventProc{
	/**宝石的道具tip为7*/
	public static int Jewel_Type_Id = 7 ;
	/**一件装备最多5个插槽*/
	public static int Max_Jewel_Num_One_Equip = 5 ;
	/**功能开启等级*/
	public static int Function_Open_Id = 1213;
	
	public static Logger log = LoggerFactory.getLogger(JewelMgr.class);	
	public static JewelMgr inst = null;
	//功能相关配置用map
	public Map<Integer, Fuwen> jewelMap = null ;//存储宝石配置信息，由于功能改动，所以从Fuwen.xml中获取
	public Map<Integer ,int[] > equipMap = null ; //以长度为5的数组记录装备的孔信息，01234对应5个孔
	public Map<Integer, Integer> itemMap = null ;//用于存储每一种宝石的最大堆叠数
	
	public JewelMgr(){
		inst = this;
		inst.init();
	}
	//初始化功能相关配置blablabla
	public void init(){
		List<Fuwen> fuwenList = TempletService.listAll(Fuwen.class.getSimpleName());
		List<ZhuangBei> zhuangbeiList = TempletService.listAll(ZhuangBei.class.getSimpleName());
		List<ItemTemp> itemList = TempletService.listAll(ItemTemp.class.getSimpleName()) ;
		
		Map<Integer, Fuwen>  jewelmap = new HashMap<Integer, Fuwen>() ;
		Map<Integer, int[]>  equipmap = new HashMap<Integer, int[]>() ;
		Map<Integer, Integer> itemmap = new HashMap<Integer, Integer>() ;
		
		for(Fuwen fuwen: fuwenList){
			if(fuwen.type == Jewel_Type_Id){
				jewelmap.put(fuwen.fuwenID , fuwen);
			}
		}
		for(ZhuangBei zhuangbei : zhuangbeiList ){
			int[] holesInfo = new int[Max_Jewel_Num_One_Equip ] ;
			int i ;
			for( i = 0 ; i < zhuangbei.holeNum ; i++ ){
				holesInfo[i] = zhuangbei.inlayColor;
			}
			for( ; i < holesInfo.length ; i++){
				holesInfo[i] = -1 ;
			}
			equipmap.put(zhuangbei.id, holesInfo );
		}
		for(ItemTemp item : itemList ){
			if(item.itemType == Jewel_Type_Id){
				itemmap.put(item.id, item.repeatNum) ;
			}
		}
		
		jewelMap = jewelmap;
		equipMap = equipmap;
		itemMap = itemmap ;
		
		//TODO 继续初始化相关功能
	}
	
	//内部分发宝石相关请求
	public void handle(int id, IoSession session, Builder builder){
		if(builder instanceof EquipOperationReq.Builder){
			switch(((EquipOperationReq.Builder) builder).getType()){
			case 1:
				getZhuangbeiInfo(id, session, builder);
				break;
			case 2:
				xiangQianAllJewel(id, session, builder);
				break;
			case 3:
				xieXiaAllJewel(id, session, builder);
				break;
			case 4:
				xiangQianOneJewel(id, session, builder);
				break;
			case 5:
				xieXiaOneJewel(id, session, builder);
				break;
			case 6:
				hechengJewel(id, session, builder);
				break;
			default:
				log.error("宝石功能EquipOperationReq请求 type 不合法");
				break;
			}
		}
	}
	

	
	
	/**响应客户端请求获取装备上的宝石信息*/
	public void getZhuangbeiInfo(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			log.error("获取装备宝石信息失败，未找到君主信息");
			return ;
		}
		
		if(!FunctionOpenMgr.inst.isFunctionOpen(Function_Open_Id, junZhu.id, junZhu.level)){
			log.error("君主{}尝试获取装备宝石信息失败，等级{}尚未开启镶嵌功能",junZhu.id , junZhu.level);
			return ;
		}
		//协议获取请求装备的dbid
		EquipOperationReq.Builder req = (EquipOperationReq.Builder)builder;
		long equipDbId = req.getEqulpId()  ;	
		
		//根据传入的装备ID，获取装备信息	
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhu.id);
		EquipGrid equip = findGridByDBId(equips, equipDbId);
		if (equip == null) {
			log.error("无法获取玩家{}的装备{}", junZhu.id , equipDbId );
			return ;	
		}
		
		//获取装备上的孔信息
		int[] holesInfo = equipMap.get(equip.itemId) ;
		if(holesInfo == null ){
			log.error("获取装备上的孔配置失败");
			return;
		}
		
		//获取玩家的装备强化信息
		UserEquip equipinfo = getUserEquipInfo(equip, junZhu.id);
//		if( equipinfo == null ){
//			log.error("数据错误，无法获取装备的强化信息");
//			return;
//		}
		
		//根据装备信息创建返回协议
		JewelList.Builder list = JewelList.newBuilder();
		list.setEqulpId(equipDbId);
		ZhuangBei equipPeiZhi = TempletService.getInstance().getZhuangBei(equip.itemId);
		if(equipPeiZhi == null ){
			log.error("君主{}的装备配置不存在，装备ID{}",junZhu.id,equip.itemId);
		}
		int equipHolesNum =equipPeiZhi==null ? 0 : equipPeiZhi.holeNum;
		list.setJewelNum(equipHolesNum);
		makeEquipInfo(junZhu, equipinfo, list );
		
		EquipOperationResp.Builder resp = EquipOperationResp.newBuilder() ;
		resp.setType(1);
		resp.setJewelList(list);	
		makeRedPointInfo(resp, junZhu.id);
		session.write(resp.build());
	}
	
	/**响应客户端请求镶嵌一颗宝石操作*/
	public void xiangQianOneJewel(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			log.error("镶嵌宝石失败，未找到君主信息");
			return ;
		}
		//判断功能开启等级
		if(!FunctionOpenMgr.inst.isFunctionOpen(Function_Open_Id, junZhu.id, junZhu.level)){
			log.error("君主{}尝试镶嵌宝石失败，等级{}尚未开启镶嵌功能",junZhu.id , junZhu.level);
			return ;
		}
		
		
		EquipOperationReq.Builder req = (EquipOperationReq.Builder)builder;
		long equipDbId = req.getEqulpId();
		long jewelDbId = req.getJewelId() ;
		int possionId = req.getPossionId();
		
		//请求的镶嵌孔位置大于等于最大孔数（从0开始计算位置）
		if(possionId >= Max_Jewel_Num_One_Equip){
			log.error("君主{}修改宝石镶嵌协议操作，请求的镶嵌孔ID不合法，",junZhu.id );
			return ;
		}
		//加载玩家背包
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		
		//根据传入协议获取装备信息,对装备进行检查
		//目前实现为仅可在已穿上的装备上镶嵌宝石
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhu.id);
		EquipGrid equip = findGridByDBId(equips, equipDbId);
		
		if (equip == null) {
			log.error("无法获取玩家{}的装备{}", junZhu.id , equipDbId );
			return ;	
		}	
		UserEquip equipInfo = getUserEquipInfo(equip , junZhu.id) ;
		if(equipInfo == null ){
			equipInfo = initUserEquipInfo(equip , junZhu.id);
		}
		
		//根据配置获取装备孔的信息，确认镶嵌请求合法性
		int[] holesInfo = equipMap.get(equip.itemId);
		if(holesInfo == null ){
			log.error("无法获取装备的配置信息，装备id：{}" , equipDbId );
			return ;
		}
		if( holesInfo[possionId] < 0 ){
			log.error("装备{}未开启玩家请求镶嵌的孔{}",equip.itemId , possionId);
			return;
		}
		
//		//检查装备孔上是否镶嵌宝石2016-05-23需求变更，可以直接替换
		List<Long> jewelList = getJewelOnEquip( equipInfo );
//		if( jewelList.get(possionId) > 0  ){
//			log.error("玩家{}请求的镶嵌孔{}上已经镶嵌了宝石",junZhu.id , possionId);
//			return ;
//		}
		
		//根据传入的协议内容获取宝石信息进行检查
		
		BagGrid jewelinBag =  findGridByDBId(bag, jewelDbId);
		//背包指定位置是否存在道具
		if(jewelinBag == null){
			log.error("无法获取君主{}指定的宝石{}",junZhu.id , jewelDbId);
			return;
		}
		//背包指定位置的道具是否是宝石，并获取配置信息
		Fuwen jewelPeiZhi = jewelMap.get(jewelinBag.itemId);
		if(jewelPeiZhi == null ){
			log.error("无法获取指定宝石的相关配置，宝石DBID:{} ，道具ID：{}" ,jewelDbId , jewelinBag.itemId);
			return;
		}
		
		//查看宝石的颜色是否符合镶嵌孔的颜色
		if(holesInfo[possionId] != jewelPeiZhi.inlayColor){
			log.error("玩家{}请求镶嵌的宝石颜色和孔的颜色不符",junZhu.id);
			return ;
		}
		
		//装备和宝石的判断均通过，开始镶嵌操作
		
		
		boolean xiangqianSucces = doXiangQian(session, bag, jewelinBag, equipInfo, possionId) ;		
		//如果镶嵌操作成功，存储镶嵌操作装备的信息，推送背包信息，返回协议
		if(xiangqianSucces){
			HibernateUtil.save(equipInfo) ;
			//BagMgr.inst.sendBagInfo(session, bag);
			JunZhuMgr.inst.sendMainInfo(session);
			BagMgr.inst.sendEquipInfo(session, equips);
		}else{
			return ;
		}
		//读取已镶嵌的宝石信息
		jewelList = getJewelOnEquip( equipInfo );
		long jewelInfo = jewelList.get(possionId);
		int jewelId = (int)(jewelInfo>>32);
		int jewelExp = (int)(jewelInfo & Integer.MAX_VALUE);
		//创建返回协议，协议中写入装备ID和孔的位置，宝石的经验需要特殊处理
		log.info("君主{}镶嵌宝石成功，装备：{}，孔：{}" ,junZhu.id,equipDbId,possionId);
		JewelInfo.Builder b = JewelInfo.newBuilder();
		b.setEqulpId( equipDbId );
		b.setPossionId( possionId );
		b.setItemId( jewelId );
		b.setExp( jewelExp );
		EquipOperationResp.Builder resp = EquipOperationResp.newBuilder();
		resp.setType(4);
		resp.setOneJewel(b);
		makeRedPointInfo(resp, junZhu.id);
		session.write(resp.build());
		int[] jewelEvent =getJewelEvent(junZhu.id);
		EventMgr.addEvent(junZhu.id,ED.use_baoshi, new Object[]{junZhu.id ,jewelEvent[0],jewelEvent[1]});
	}
	
	/**响应客户端请求卸下一颗宝石操作*/
	public void xieXiaOneJewel(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			log.error("卸下宝石失败，未找到君主信息");
			return ;
		}
		//判断功能开启等级
		if(!FunctionOpenMgr.inst.isFunctionOpen(Function_Open_Id, junZhu.id, junZhu.level)){
			log.error("君主{}尝试卸下宝石失败，等级{}尚未开启镶嵌功能",junZhu.id , junZhu.level);
			return ;
		}
		
		EquipOperationReq.Builder req = (EquipOperationReq.Builder)builder;
		long equipDbId = req.getEqulpId();
		int possionId = req.getPossionId();
		
		//请求的镶嵌孔位置大于等于最大孔数（从0开始计算位置）
		if(possionId >= Max_Jewel_Num_One_Equip){
			log.error("君主{}修改宝石镶嵌协议操作，请求的镶嵌孔ID不合法，",junZhu.id );
			return ;
		}
		
		//根据协议内容，获取装备强化信息
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhu.id);
		EquipGrid equip = findGridByDBId(equips, equipDbId);
		
		if (equip == null) {
			log.error("无法获取玩家{}的装备{}格子信息", junZhu.id , equipDbId );
			return ;	
		}	
		UserEquip equipInfo = getUserEquipInfo(equip , junZhu.id) ;
		if(equipInfo == null ){
			equipInfo = initUserEquipInfo(equip,  junZhu.id);
		}
		
		//根据装备强化信息，获取装备上镶嵌的宝石列表
		List<Long> jewelList = getJewelOnEquip(equipInfo);		
		long jewelInfo = jewelList.get(possionId) ;
		//指定位置没有镶嵌宝石
		if(	jewelInfo == -1 || jewelInfo == 0 ){
			log.info("玩家{}请求卸下的装备：{}位置：{}没有宝石记录，怀疑篡改协议",junZhu.id,equipDbId,possionId);
			return ;
		}
		
		//检查完成，开始拆卸宝石操作
		//加载玩家背包
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		
		//获取宝石ID，背包中加入宝石
		int jewelId= (int) (jewelInfo >> 32);
		int jewelExp = (int)(jewelInfo & Integer.MAX_VALUE);
		
		//放入背包前判断一下宝石的经验是否为初始经验
		jewelExp = jewelExp == 0 ? -1 : jewelExp ;
		
		BagMgr.inst.addItem(session, bag, jewelId, 1, jewelExp , junZhu.level, "玩家卸下宝石");
			
		//更新装备信息，推送背包信息
		equipInfo = updateEquipHole(equipInfo, possionId, -1);
		HibernateUtil.save(equipInfo);
		//BagMgr.inst.sendBagInfo(session, bag);
		JunZhuMgr.inst.sendMainInfo(session);
		BagMgr.inst.sendEquipInfo(session, equips);
		EventMgr.addEvent(junZhu.id,ED.get_BaoShi, junZhu);
		log.info("君主{}卸下宝石成功，装备：{}，孔：{}" ,junZhu.id,equipDbId,possionId);
		//构建返回协议
		EquipOperationResp.Builder resp = EquipOperationResp.newBuilder();
		JewelInfo.Builder j = JewelInfo.newBuilder();
		j.setItemId(-1);
		j.setExp(-1);
		j.setPossionId(possionId);
		j.setEqulpId(equipDbId);
		resp.setType(5);
		resp.setOneJewel(j);
		makeRedPointInfo(resp, junZhu.id);
		session.write(resp.build());
	}
	
	/**响应客户端一键镶嵌请求*/
	public void xiangQianAllJewel(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			log.error("一键镶嵌宝石失败，未找到君主信息");
			return ;
		}
		//判断功能开启等级
		if(!FunctionOpenMgr.inst.isFunctionOpen(Function_Open_Id, junZhu.id, junZhu.level)){
			log.error("君主{}尝试一键镶嵌宝石失败，等级{}尚未开启镶嵌功能",junZhu.id , junZhu.level);
			return ;
		}
		
		EquipOperationReq.Builder req = (EquipOperationReq.Builder)builder;
		long equipDbId = req.getEqulpId();
		//加载玩家背包
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		
		//根据传入协议获取装备信息,对装备进行检查
		//目前实现为仅可在已穿上的装备上镶嵌宝石
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhu.id);
		EquipGrid equip = findGridByDBId(equips, equipDbId);
		if (equip == null) {
			log.error("无法获取玩家{}的装备{}", junZhu.id , equipDbId );
			return ;	
		}	
		UserEquip equipInfo = getUserEquipInfo(equip , junZhu.id) ;
		if(equipInfo == null ){
			equipInfo = initUserEquipInfo(equip,  junZhu.id);
		}
		
		//根据配置获取装备孔的信息，确认镶嵌请求合法性
		int[] holesInfo = equipMap.get(equip.itemId);
		if(holesInfo == null ){
			log.error("无法获取装备的镶嵌孔配置信息，装备id：{}" , equipDbId );
			return ;
		}
		//获取装备上的镶嵌信息
		List<Long> jewelList = getJewelOnEquip( equipInfo );
		
		//遍历宝石列表，逐一卸下
		for(int possionId = 0 ; possionId< jewelList.size() ; possionId ++ ){
			long jewelInfo = jewelList.get(possionId);
			if(jewelInfo <= 0){
				//无宝石，跳过
				continue;
			}
			//获取宝石信息，背包中放入宝石
			int jewelId= (int) (jewelInfo >> 32);
			int jewelExp = (int)(jewelInfo & Integer.MAX_VALUE);
			//放入背包前判断一下宝石的经验是否为初始经验
			jewelExp = jewelExp == 0 ? -1 : jewelExp ;
			BagMgr.inst.addItem(session, bag, jewelId, 1, jewelExp, junZhu.level, "玩家卸下宝石");
			
			//更新装备强化信息，因为做了缓存，所以不用每次变更都保存
			equipInfo = updateEquipHole(equipInfo, possionId, -1);
		}
		HibernateUtil.save(equipInfo);
		//重新加载背包
		bag = BagMgr.inst.loadBag(junZhu.id);
		
		//获取玩家背包中的所有宝石
		List<BagGrid> allJewelInBag = getAllJewel( bag ) ;
		//重新加载镶嵌信息
		jewelList = getJewelOnEquip( equipInfo );
		//开始遍历装备空镶嵌孔，逐一尝试镶嵌
		int xiangQianCnt = 0 ;
		for(int i = 0 ; i < jewelList.size() ; i++){
			int holeColour = holesInfo[i];
			int jewelIdBefore = jewelList.get(i) <=0 ? -1 : (int)(jewelList.get(i)>>32) ;
			boolean succes = false ;
			//遍历玩家宝石列表，尝试镶嵌
			for(BagGrid bg : allJewelInBag){
				if(succes){
					break;//如果镶嵌成功，结束宝石的遍历
				}
				Fuwen jewelPeiZhi = jewelMap.get(bg.itemId);
				if( jewelPeiZhi == null ){
					continue;
				}
				//颜色相同，并且宝石的宝石等级高于身上的宝石等级
				if( jewelPeiZhi.inlayColor == holeColour && jewelIdBefore <=0){
					//装备上的孔未镶嵌
					//因为不确定镶嵌能不能成功，所以不从这里跳出循环，只是修改成功标记
					succes = doXiangQian(session, bag, bg, equipInfo, i );
				}
			}
			if(succes){
				//如果镶嵌成功，记录+1
				xiangQianCnt ++ ;
			}
		}
		
		//装备空格子遍历结束，一键镶嵌完成，开始构建返回协议
		EquipOperationResp.Builder resp = EquipOperationResp.newBuilder();
		resp.setType(2);
		if( xiangQianCnt > 0 ){
			HibernateUtil.save(equipInfo);
			//一键镶嵌成功（只要镶上一颗宝石就算成功）
			log.info("玩家{}一键镶嵌宝石成功，装备：{}",junZhu.id , equipDbId);
			resp.setSucces(true);
			JewelList.Builder list = JewelList.newBuilder();
			list.setEqulpId(equipDbId);
			ZhuangBei equipPeiZhi = TempletService.getInstance().getZhuangBei(equip.itemId);
			if(equipPeiZhi == null ){
				log.error("君主{}的装备配置不存在，装备ID{}",junZhu.id,equip.itemId);
			}
			int equipHolesNum =equipPeiZhi==null ? 0 : equipPeiZhi.holeNum;
			list.setJewelNum(equipHolesNum);
			makeEquipInfo(junZhu, equipInfo, list);
			resp.setJewelList(list);
			//更新背包信息
			JunZhuMgr.inst.sendMainInfo(session);
			//BagMgr.inst.sendBagInfo(session, bag);
			BagMgr.inst.sendEquipInfo(session, equips);
			EventMgr.addEvent(junZhu.id,ED.get_BaoShi, junZhu);
		}else{
			//一颗宝石都没镶上，无需更新背包信息
			resp.setSucces(false);
		}
		makeRedPointInfo(resp, junZhu.id);
		session.write(resp.build());
		int[] jewelEvent =getJewelEvent(junZhu.id);
		EventMgr.addEvent(junZhu.id,ED.use_baoshi, new Object[]{junZhu.id ,jewelEvent[0],jewelEvent[1]});
	}
	
	/**响应客户端一键卸下请求*/
	public void xieXiaAllJewel(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			log.error("一键卸下宝石失败，未找到君主信息");
			return ;
		}
		//判断功能开启等级
		if(!FunctionOpenMgr.inst.isFunctionOpen(Function_Open_Id, junZhu.id, junZhu.level)){
			log.error("君主{}尝试一键卸下宝石失败，等级{}尚未开启镶嵌功能",junZhu.id , junZhu.level);
			return ;
		}
		EquipOperationReq.Builder req = (EquipOperationReq.Builder)builder;
		//获取装备ID
		long equipDbId = req.getEqulpId();
		//加载玩家背包
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		
		//根据协议内容，获取装备强化信息
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhu.id);
		EquipGrid equip = findGridByDBId(equips, equipDbId);
		
		if (equip == null) {
			log.error("无法获取玩家{}的装备{}格子信息", junZhu.id , equipDbId );
			return ;	
		}	
		UserEquip equipInfo = getUserEquipInfo(equip , junZhu.id) ;
		if(equipInfo == null ){
			equipInfo = initUserEquipInfo(equip,  junZhu.id);
		}
		
		//根据配置获取装备孔的信息，返回协议要用
		int[] holesInfo = equipMap.get(equip.itemId);
		if(holesInfo == null ){
			log.error("无法获取装备的镶嵌孔配置信息，装备id：{}" , equipDbId );
			return ;
		}
		
		//根据装备强化信息，获取装备上镶嵌的宝石列表
		List<Long> jewelList = getJewelOnEquip(equipInfo);	
		//遍历宝石列表，逐一卸下
		int xieXiaCnt = 0 ;
		for(int possionId = 0 ; possionId< jewelList.size() ; possionId ++ ){
			long jewelInfo = jewelList.get(possionId);
			if(jewelInfo == -1 || jewelInfo == 0){
				//无宝石，跳过
				continue;
			}
			//获取宝石信息，背包中放入宝石
			int jewelId= (int) (jewelInfo >> 32);
			int jewelExp = (int)(jewelInfo & Integer.MAX_VALUE);
			//放入背包前判断一下宝石的经验是否为初始经验
			jewelExp = jewelExp == 0 ? -1 : jewelExp ;
			BagMgr.inst.addItem(session, bag, jewelId, 1, jewelExp, junZhu.level, "玩家卸下宝石");
			
			equipInfo = updateEquipHole(equipInfo, possionId, -1);
			xieXiaCnt ++ ;
		}
		HibernateUtil.save(equipInfo);
		log.info("玩家{}一键卸下宝石成功，装备：{}",junZhu.id , equipDbId);
		//遍历结束，推送背包信息
		//BagMgr.inst.sendBagInfo(session, bag);
		JunZhuMgr.inst.sendMainInfo(session);
		BagMgr.inst.sendEquipInfo(session, equips);
		EventMgr.addEvent(junZhu.id,ED.get_BaoShi, junZhu);
		//发送返回协议
		EquipOperationResp.Builder resp = EquipOperationResp.newBuilder();
		resp.setType(3);
		resp.setSucces(true);
		JewelList.Builder list = JewelList.newBuilder();
		list.setEqulpId(equipDbId);
		ZhuangBei equipPeiZhi = TempletService.getInstance().getZhuangBei(equip.itemId);
		if(equipPeiZhi == null ){
			log.error("君主{}的装备配置不存在，装备ID{}",junZhu.id,equip.itemId);
		}
		int equipHolesNum =equipPeiZhi==null ? 0 : equipPeiZhi.holeNum;
		list.setJewelNum(equipHolesNum);
		makeEquipInfo(junZhu, equipInfo, list);
		resp.setJewelList(list);
		makeRedPointInfo(resp, junZhu.id);
		session.write(resp.build());
	}
	
	/**响应客户端宝石合成请求*/
	public void hechengJewel(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			log.error("宝石合成失败，未找到君主信息");
			return ;
		}
		//判断功能开启等级
		if(!FunctionOpenMgr.inst.isFunctionOpen(Function_Open_Id, junZhu.id, junZhu.level)){
			log.error("君主{}尝试宝石合成失败，等级{}尚未开启镶嵌功能",junZhu.id , junZhu.level);
			return ;
		}
		EquipOperationReq.Builder req = (EquipOperationReq.Builder)builder;
		long equipDbId = req.getEqulpId();
		int possionId = req.getPossionId();
		//协议获取合成目标装备+合成目标镶嵌孔，检查合成目标合法性
		//获取装备信息
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhu.id);
		EquipGrid equip = findGridByDBId(equips, equipDbId);
		
		if (equip == null) {
			log.error("无法获取玩家{}的装备{}", junZhu.id , equipDbId );
			return ;	
		}	
		UserEquip equipInfo = getUserEquipInfo(equip , junZhu.id) ;
		if(equipInfo == null ){
			equipInfo = initUserEquipInfo(equip,  junZhu.id);
		}
		
		//请求的镶嵌孔位置大于等于最大孔数（从0开始计算位置）
		if(possionId >= Max_Jewel_Num_One_Equip){
			log.error("君主{}修改宝石镶嵌协议操作，请求的镶嵌孔ID不合法，",junZhu.id );
			return ;
		}
		
		//获取目标镶嵌孔的宝石信息，存入jewelInfo
		List<Long> jewelList = getJewelOnEquip( equipInfo );
		long jewelInfo = jewelList.get(possionId);
		if( jewelInfo <= 0){
			log.error("合成失败，玩家合成目标位置没有宝石");
			return;
		}
		
		int jewelId = (int)(jewelInfo>>32);
		int jewelExp = (int)(jewelInfo & Integer.MAX_VALUE);
		
		Fuwen jewelPeiZhi = jewelMap.get(jewelId);
		if(jewelPeiZhi == null ){
			log.error("合成失败，玩家合成目标位置镶嵌的宝石获取不到配置");
			return;
		}
		if(jewelPeiZhi.fuwenLevel == jewelPeiZhi.levelMax){
			log.error("合成失败，合成的目标位置镶嵌的宝石等级是最高级");
			return;
		}
		//协议获取合成材料的宝石列表
		List<Long> cailiaoList = req.getCailiaoListList();
		//对宝石列表进行遍历，按照dbid->请求数量的格式存入reqCailiaoMap
		Map<Long, Integer> reqCailiaoMap = new HashMap<Long, Integer>();
		for(long cailiao : cailiaoList){
			//reqCailiaoMap中获取格子的数量，如果没有，初始化为1，如果有，数量++
			Integer cnt = reqCailiaoMap.get(cailiao);
			if(cnt == null){
				cnt = 1;
			}else{
				cnt++;
			}
			reqCailiaoMap.put(cailiao, cnt);
		}
		
		//加载玩家背包
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		//过滤请求的材料合法性
		List<BagGrid> legalCailiao = legalCheck(reqCailiaoMap, bag , jewelId);
		//计算合法材料的总经验，并扣除材料
		int totalExp = 0 ;//总经验
		for(BagGrid bg : legalCailiao ){
			int addExp = 0 ;//一格宝石的经验
			int cailiaoNum = reqCailiaoMap.get(bg.dbId); //请求的格子宝石数量
			int perExp = jewelMap.get(bg.itemId).exp + (bg.instId > 0 ?(int)bg.instId : 0);//一颗宝石的经验
			//加入总经验，扣除道具
			addExp = perExp * cailiaoNum ;
			totalExp += addExp;
			BagMgr.inst.removeItemByBagdbId(session, bag, "宝石合成", bg.dbId, cailiaoNum, junZhu.level);
		}
		if( totalExp <= 0 ){
			log.error("合成失败，请求道具列表全部不合法");
			return ;
		}
		//在之前获取的宝石信息中添加经验
		jewelExp += totalExp ;
		
		//宝石升级判断
		while( jewelExp >= jewelPeiZhi.lvlupExp ){	
			jewelId = jewelPeiZhi.fuwenNext;
			jewelExp -= jewelPeiZhi.lvlupExp;
			jewelPeiZhi = jewelMap.get(jewelId);
			if( jewelPeiZhi.lvlupExp == -1 ){
				break;
			}
		}
		//升级完成，存储装备镶嵌孔信息
		jewelInfo = jewelId;
		jewelInfo = jewelInfo<<32;
		jewelInfo= jewelInfo | jewelExp ;
		updateEquipHole(equipInfo, possionId, jewelInfo);
		
		HibernateUtil.save(equipInfo);
		log.info("玩家{}宝石合成成功，装备：{}，孔：{}",junZhu.id , equipDbId , possionId);
		//创建返回协议，协议中写入装备ID和孔的位置，宝石的经验需要特殊处理
		JewelInfo.Builder b = JewelInfo.newBuilder();
		b.setEqulpId( equipDbId );
		b.setPossionId(possionId);
		b.setItemId(jewelId);
		b.setExp(jewelExp);
		EquipOperationResp.Builder resp = EquipOperationResp.newBuilder();
		resp.setType(6);
		resp.setOneJewel(b);
		//推送背包信息
		//BagMgr.inst.sendBagInfo(session, bag);
		JunZhuMgr.inst.sendMainInfo(session);
		BagMgr.inst.sendEquipInfo(session, equips);
		//返回协议
		session.write(resp.build());
		//添加镶嵌事件
		int[] jewelEvent =getJewelEvent(junZhu.id);
		EventMgr.addEvent(junZhu.id,ED.use_baoshi, new Object[]{junZhu.id ,jewelEvent[0],jewelEvent[1]});
	}
	
	
	
	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.get_BaoShi:{
			if(event.param instanceof JunZhu){
				JunZhu junZhu = (JunZhu) event.param;
				xiangQianRedPointPush(junZhu.id);
			}
		}
		break;
		case ED.JINJIE_ONE_GONG:{
			if(event.param instanceof Object[]){
				Object[] objs = (Object[])event.param;
				long junZhuId = (long)objs[0];
				xiangQianRedPointPush(junZhuId);
			}
		}
		break;
		case ED.EQUIP_ADD:{
			if(event.param instanceof Object[]){
				Object[] objs = (Object[])event.param;
				long junZhuId = (long)objs[0];
				xiangQianRedPointPush(junZhuId);	
			}
		}
		break;
		case ED.REFRESH_TIME_WORK:{
			if(event.param instanceof IoSession){
				JunZhu junZhu = JunZhuMgr.inst.getJunZhu((IoSession)event.param);
				if(junZhu != null ){
					xiangQianRedPointPush(junZhu.id);	
				}
			}
		}
		break;
		case ED.JUNZHU_LOGIN:{
			if(event.param instanceof JunZhu){
				JunZhu junZhu = (JunZhu)event.param;
				xiangQianRedPointPush(junZhu.id);	
			}
		}
		break;
		}
		
	}
	
	@Override
	public void doReg() {
		EventMgr.regist(ED.get_BaoShi, this);					//监听获取宝石事件
		EventMgr.regist(ED.JINJIE_ONE_GONG, this);		//监听装备进阶事件
		EventMgr.regist(ED.EQUIP_ADD, this);					//监听更换装备事件
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);	//监听定时刷新事件
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
	}
	
	
	
	/**从背包中获取指定位置的背包格子信息*/
	public <T> T  findGridByDBId(Bag<T> bag, long dbid) {		
		for( T bg : bag.grids){
			if(bg == null){
				continue;
			}
			if(bg instanceof BagGrid){
				if(((BagGrid) bg).dbId == dbid){
					return bg;
				}
			}
			if(bg instanceof EquipGrid){
				if(((EquipGrid) bg).dbId == dbid){
					return bg;
				}
			}
		}
		return null;
	}
	
	/**获取装备上的宝石列表，长度为5的数组，对应5个位置 */
	public List<Long> getJewelOnEquip(UserEquip equipInfo){
		List<Long> res  = new ArrayList<Long>(Max_Jewel_Num_One_Equip);
		if(equipInfo == null ){
			for(int i = 0 ; i < Max_Jewel_Num_One_Equip ; i++){
				res.add(-1L);
			}
		}else{
			res.add(0, equipInfo.Jewel0);
			res.add(1, equipInfo.Jewel1);
			res.add(2, equipInfo.Jewel2);
			res.add(3, equipInfo.Jewel3);
			res.add(4, equipInfo.Jewel4);
		}
		return res ;
	}
	
	/**获取玩家装备栏位装备的强化信息 */
	//装备上的宝石信息存储在装备强化信息表格里
	public UserEquip getUserEquipInfo(EquipGrid equip , long jzid) {
		UserEquip res = null ;
		if(equip.instId <= 0){
			return null;
		}else{
			res  = UserEquipDao.find(jzid, equip.instId);
			if(res == null){
				log.error("数据出错，无法找到玩家{}的装备信息，装备ID：{}" , jzid , equip.dbId );
			}
		}		
		return res;
	}
	/**初始化玩家的装备强化信息并保存、返回*/
	public UserEquip initUserEquipInfo(EquipGrid equip , long jzid ){
		UserEquip equipInfo = new UserEquip();
		equipInfo.userId = jzid;
		equipInfo.templateId = equip.itemId;
		UserEquipDao.insert(equipInfo);
		MC.add(equipInfo, equipInfo.getIdentifier());
		equip.instId = equipInfo.getIdentifier() ;
		HibernateUtil.save(equip);
		log.info("玩家{}创建装备强化信息，装备ID：{}", jzid , equip.dbId );
		return equipInfo;
	}
	
	/**更改指定装备指定位置的宝石信息，未存储，调用后自行存储变更
	 * 卸下宝石时，jewelId传入-1即可
	 * 调用之前必须检查操作合法性*/
	public UserEquip updateEquipHole(UserEquip equipInfo , int possionId , long jewelId ){
		switch(possionId){
		case 0 :
			equipInfo.Jewel0 = jewelId ;
			break;
		case 1 :
			equipInfo.Jewel1 = jewelId ;
			break;
		case 2 :
			equipInfo.Jewel2 = jewelId ;
			break;
		case 3 :
			equipInfo.Jewel3 = jewelId ;
			break;
		case 4 :
			equipInfo.Jewel4 = jewelId ;
			break;
		default:
			log.error("指定宝石镶嵌孔错误");
			break;
		}
		return equipInfo ; 
	}

	
	/**按照宝石的itemId，经验进行排序的背包宝石列表*/
	public List<BagGrid> getAllJewel( Bag<BagGrid> bag){
		List<BagGrid> res = new LinkedList<BagGrid>() ;
		List<BagGrid> bagGrids = new LinkedList<BagGrid>(bag.grids);
		for(BagGrid bg : bagGrids){
			if(bg == null){
				continue ;
			}
			if(bg.itemId <= 0){
				continue ;
			}
			if(bg.type == Jewel_Type_Id){
				Boolean isPutIn = false ;
				for(int i=0 ; i < res.size() ; i++){
					BagGrid bl = res.get(i);
					if(jewelMap.get(bg.itemId).fuwenLevel > jewelMap.get(bl.itemId).fuwenLevel ){
						//宝石等级判断，防坑爹配置，所以由配置中读取等级进行比较
						//等级高，插入列表
						res.add(i, bg);
						isPutIn = true ; 
						break;
					}else if( jewelMap.get(bg.itemId).fuwenLevel == jewelMap.get(bl.itemId).fuwenLevel ){
						//等级相同，判断经验，插入列表
						 if(bg.instId > bl.instId){
							res.add(i, bg);
							isPutIn = true ; 
							break;
						}
					}
					
				}
				//遍历完了还没加进列表，说明这个格子的宝石是最差的，加入列表末尾
				if (!isPutIn){
					res.add(bg);
				}
			}
		}
		return res ;
	}
	
	/**镶嵌操作，镶嵌成功返回true，失败返回false
	 * 操作之后需要自行存储equipInfo变更*/
	public boolean doXiangQian(IoSession session, Bag<BagGrid> bag ,BagGrid jewelinBag , UserEquip equipInfo , int possionId){
		boolean xiangqianSucces = false ;
		
		
		int jewelId = jewelinBag.itemId;
		int jewelExp = jewelinBag.instId > 0 ? (int)jewelinBag.instId : 0 ;
		boolean isRemoved = BagMgr.inst.removeItemByBagdbId(session, bag, "宝石镶嵌", jewelinBag.dbId, 1, 0);
		if(isRemoved){
			//移除成功，则更新镶嵌孔信息
			List<Long> jewelList = getJewelOnEquip( equipInfo );
			long jewelInfoBefore = jewelList.get(possionId) ;
			if ( jewelInfoBefore > 0 ){
				int jewelIdBefore = (int)( jewelInfoBefore>> 32);
				int jewelExpBefore = (int)(jewelInfoBefore&Integer.MAX_VALUE);
				BagMgr.inst.addItem(session, bag, jewelIdBefore, 1, jewelExpBefore == 0 ? -1:jewelExpBefore , 1, "宝石卸下");
			}
			
			long jewelInfo = jewelId;
			jewelInfo = jewelInfo<<32;
			jewelInfo= jewelInfo | jewelExp ;
			equipInfo = updateEquipHole(equipInfo, possionId, jewelInfo);
			xiangqianSucces = true ;
		}else{
			log.error("镶嵌失败：原因背包中扣除宝石失败");
			return xiangqianSucces ;
		}			
		
		return xiangqianSucces ;
	}

	/**根据传入的equipinfo 在传入的协议中添加装备上镶嵌的宝石的信息*/
	public JewelList.Builder makeEquipInfo(JunZhu junzhu , UserEquip equipinfo , JewelList.Builder bulider ){
		//根据装备信息获取装备上的宝石列表
		List<Long> jewelList = getJewelOnEquip( equipinfo );
		//掉坑：因为不同镶嵌孔的宝石信息可能相同，所以不能用indexof来获取镶嵌孔ID
		for(int i = 0 ; i < jewelList.size();i++  ){
			Long jewelInfo = jewelList.get(i);
			if(jewelInfo <= 0 ){
				continue;
			}
			int jewelId = (int) (jewelInfo >> 32) ;
			int jewelExp = (int)(jewelInfo&Integer.MAX_VALUE );
			JewelInfo.Builder b = JewelInfo.newBuilder();
			b.setExp(jewelExp);
			b.setItemId(jewelId);
			b.setPossionId( i );						
			bulider.addList(b);
		}
		return bulider;
	}
	
	/**传入的宝石合成材料map和目标宝石的道具ID， 返回合法的的BagGrid列表*/
	public List<BagGrid> legalCheck(Map<Long, Integer> cailiaoMap , Bag<BagGrid> bag , int itemId ){
		List<BagGrid> res = new ArrayList<BagGrid>();
		for(long bagId : cailiaoMap.keySet()){
			BagGrid bg = findGridByDBId(bag, bagId);		
			if(bg == null){
				//没有道具信息记录，跳过
				continue;
			}
			if(bg.itemId < 0){
				continue;
			}
			if(bg.type != Jewel_Type_Id){
				//道具不是宝石，跳过
				continue;
			}
			if(jewelMap.get(bg.itemId).shuxing != jewelMap.get(itemId).shuxing){
				//属性不同，跳过
				//FIXME 风险：如果道具的type是宝石，但是不在宝石表里，会报空指针，可以去找策划改表
				continue;
			}
			if(bg.cnt < cailiaoMap.get(bagId)){
				//请求的数量大于道具的数量，跳过
				continue;
			}
			res.add(bg);
		}
		return res;
	}
	
	/**根据君主ID获取宝石事件信息，int 数组存储：0：已镶嵌宝石数量；1：已镶嵌最高宝石品质*/
	public int[] getJewelEvent(long junZhuId){
		int[] res = new int[]{0,0} ;
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhuId);
		for(EquipGrid eg : equips.grids ){
			if( eg != null && eg.instId > 0){
				UserEquip ue = UserEquipDao.find(junZhuId, eg.instId);
				if(ue != null ){
					List<Long> jewelList = getJewelOnEquip(ue);
					for( long jewelInfo : jewelList){
						if(jewelInfo > 0){
							res[0] ++;
							int jewelId = (int)(jewelInfo >> 32);
							res[1] = Math.max(jewelMap.get(jewelId).fuwenLevel, res[1] );
						}
					}
				}
			}
		}
		return res ;
	}
	
	/**传入玩家ID，获取玩家的镶嵌镶嵌相关我要变强
	 * 返回一个Object[]
	 * 第0位：总进度(int[2]{总宝石等级  ,  总镶嵌孔数量*9})；
	 * 第1位：最差的三件装备(int[3]{装备1itemId , 装备2itemId , 装备3itemId})
	 * */
	public Object[] getXiangQianTuiJian(long junZhuId){
		Object[] res= new  Object[2] ;
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhuId);
		float sumProg = 0;
		for (EquipGrid eg : equips.grids ){
			if(eg != null){
				
				int equipJewelLv = 0 ;//记录单件装备的宝石等级之和
				ZhuangBei equipPeiZhi = TempletService.getInstance().getZhuangBei(eg.itemId);
				if(equipPeiZhi == null ){
					log.error("君主{}的装备配置不存在，装备ID{}",junZhuId,eg.itemId);
					continue;
				}
				int equipHolesNum =equipPeiZhi.holeNum; //单件装备的孔数量				
				if(eg.instId > 0){
					//获取玩家的装备强化信息
					UserEquip equipInfo = UserEquipDao.find(eg.dbId/EquipMgr.spaceFactor, eg.instId);
					if(equipInfo == null ){
						log.error("无法获取玩家{}的装备强化信息，装备{}",junZhuId , eg.dbId);
						continue;
					}else{
						//获取玩家的宝石镶嵌列表
						List<Long> jewelList = getJewelOnEquip(equipInfo);
						for(long jewelInfo : jewelList){
							if(jewelInfo > 0 ){
								int jewelId = (int)(jewelInfo>>32);
								equipJewelLv += jewelMap.get(jewelId).fuwenLevel;//宝石等级加入单件装备宝石等级之和
							}
						}
					}
				}
//				a)	一个装备镶嵌进度=该装备已镶嵌的所有宝石等级和/（该装备孔的个数*9）
	//			b)	总体进度=(所有装备镶嵌进度之和/9)*100%
				float p = 0;
				if(equipHolesNum>0){
					p = equipJewelLv * 1.0f / (equipHolesNum*9);
					sumProg += p;
				}
			}
		}
		// 装备遍历完成，生成总镶嵌进度
		int[] totalProgress = new int[]{ Math.round(sumProg * 100/9) , 100};
		res[0] = totalProgress;
		
		// 获取镶嵌最差的3件装备
		List<EquipGrid> equipList = new LinkedList<EquipGrid>(equips.grids) ;
		int[] worstXiangQians = new int[3];
		int retIdx = 0;
		for( int i = 0 ; i < 9 ;i++ ){
			EquipGrid worstXQ = getWorstXiqnagQian(equipList);
			equipList.remove(worstXQ);
			if(worstXQ == null){
				continue;
			}
			worstXiangQians[retIdx++] = worstXQ.itemId;
			if(retIdx>=3){
				break;
			}
		}
		res[1] = worstXiangQians;
		return res ;
	}
	
	/**根据传入的装备列表，返回镶嵌最差的一件装备*/
	public EquipGrid getWorstXiqnagQian(List<EquipGrid> equipList){
		EquipGrid res = null ;
		int minJewelLevel = 9 ;//最低宝石等级记录，先设置为最大等级
		//家在玩家的装备列表
		for (EquipGrid eg : equipList ){
			if(eg != null){
				if(eg.instId <=0){
					//装备无强化信息，则没有镶嵌宝石，返回此装备
					//FIXME 风险：如果装备设计无孔，在没有强化的时候，同样会返回此装备
					return eg;
				}else{
					UserEquip equipInfo = UserEquipDao.find(eg.dbId/EquipMgr.spaceFactor, eg.instId);
					if(equipInfo != null ){
						//获取宝石信息和装备镶嵌孔信息
						List<Long> jewelList = getJewelOnEquip(equipInfo);
						int[] holesInfo = equipMap.get(eg.itemId);
						if(holesInfo == null) {
							log.error("获取装备镶嵌孔信息错误，找不到装备，itemId:{}", eg.itemId);
							continue;
						}
						for(int i= 0 ; i <  jewelList.size() ; i++){
							long jewelInfo = jewelList.get(i);
							if(jewelInfo > 0 ){
								//孔镶嵌了宝石，获取宝石等级，看是否是最低的
								int jewelId = (int)(jewelInfo>>32);
								if(minJewelLevel > jewelMap.get(jewelId).fuwenLevel){
									//如果宝石等级低于最低等级记录，返回结果指向此装备，更新最低等级记录
									minJewelLevel = jewelMap.get(jewelId).fuwenLevel;
									res = eg;
								}
							}else if(holesInfo[i] > 0){
								//孔已开，宝石信息为<=0 ，则没有镶嵌宝石，返回此装备
								return eg;
							}
						}
					}else{
						log.error("获取玩家的装备：{}强化信息失败",eg.dbId);
					}
				}
			}
		}
		
		return res ;
	}
	
	/**定时发送宝石镶嵌红点推送信息*/
	public void xiangQianRedPointPush(long junZhuId){
		
		IoSession session = SessionManager.getInst().getIoSession(junZhuId);
		if(session == null ){
			return;//君主不在线，结束
		}
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			return;//找不到君主，结束
		}
		if(! FunctionOpenMgr.inst.isFunctionOpen(Function_Open_Id, junZhuId, junZhu.level)){
			return;//镶嵌功能没开启，结束
		}
		//加载背包
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhuId);
		//获取玩家背包中的所有宝石
		List<BagGrid> allJewelInBag = getAllJewel( bag ) ;
		if(allJewelInBag == null || allJewelInBag.size() == 0 ){
			//如果背包中没有宝石，结束
			return;
		}
		//加载装备
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhuId);
		List<UserEquip> ueList = UserEquipDao.list(junZhuId);
		Map<Long , UserEquip> ueMap = new HashMap<Long , UserEquip>();
		for(UserEquip ue : ueList ){
			ueMap.put(ue.equipId, ue);
		}
		for(EquipGrid eg: equips.grids){
			if(eg == null ){
				continue;
			}
			//获取装备的强化信息
			UserEquip equipInfo = ueMap.get(eg.instId);
			//获取装备的镶嵌孔配置
			int[] holesInfo = equipMap.get(eg.itemId);
			if(holesInfo == null) {
				log.error("获取装备镶嵌孔信息错误，找不到装备，itemId:{}", eg.itemId);
				continue;
			}
			//获取装备的镶嵌孔信息
			List<Long> jewelList = getJewelOnEquip(equipInfo); 
			//对装备的镶嵌孔进行遍历
			for(int i = 0 ; i < jewelList.size() ; i++){
				//没有宝石，但是孔已开
				int addExp = 0;
				if(holesInfo[i] > 0){
					if(jewelList.get(i) <= 0){
						//遍历玩家宝石列表
						for(BagGrid bg : allJewelInBag){
							Fuwen jewelPeiZhi = jewelMap.get(bg.itemId);
							if( jewelPeiZhi == null ){
								continue;
							}
							if( jewelPeiZhi.inlayColor == holesInfo[i] ){
								//判断背包中有宝石可以镶嵌，推送红点
//								log.info("君主{}有可镶嵌宝石，发送镶嵌推送",junZhuId);
								FunctionID.pushCanShowRed(junZhuId, session, FunctionID.XiangQian);
								return;
							}
						}
					}else{
						//已镶嵌：获取镶嵌的宝石
						int jewelId = (int)(jewelList.get(i)>>32);
						Fuwen jewelOnEquip = jewelMap.get(jewelId);
						//遍历宝石列表，查看有没有更好的宝石
						for(BagGrid bg : allJewelInBag){
							Fuwen jewelPeiZhi = jewelMap.get(bg.itemId);
							if( jewelPeiZhi == null ){
								continue;
							}
							if( jewelPeiZhi.inlayColor == holesInfo[i]  ){
								//镶嵌红点判断：颜色相同，计算经验
								//取巧：背包宝石等级高于镶嵌宝石，同样会触发经验足以合成的的判断
								addExp += (jewelPeiZhi.exp +(bg.instId>0? bg.instId:0)) *bg.cnt;
								if(addExp >= jewelOnEquip.lvlupExp && jewelOnEquip.fuwenLevel < jewelOnEquip.levelMax){
									//发现经验足以升级，并且不是9级宝石，直接推送红点，结束
									log.info("君主{}有可镶嵌宝石，发送镶嵌推送",junZhuId);
									FunctionID.pushCanShowRed(junZhuId, session, FunctionID.XiangQian);
									return;
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**编辑每件装备的宝石的红点信息*/
	public void makeRedPointInfo(EquipOperationResp.Builder resp , long junZhuId ){
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhuId);
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhuId);
		//获取玩家背包中的所有宝石
		List<BagGrid> allJewelInBag = getAllJewel( bag ) ;
		if(allJewelInBag == null || allJewelInBag.size() == 0 ){
			//如果背包中没有宝石，结束
			return;
		}
		List<UserEquip> ueList = UserEquipDao.list(junZhuId);
		Map<Long , UserEquip> ueMap = new HashMap<Long , UserEquip>();
		for(UserEquip ue : ueList ){
			ueMap.put(ue.equipId, ue);
		}
		for(EquipGrid eg: equips.grids){
			if(eg == null ){
				continue;
			}
			//获取装备的强化信息
			UserEquip equipInfo = ueMap.get(eg.instId);
			if(eg.itemId <= 0) {
				continue;
			}
			//获取装备的镶嵌孔配置
			int[] holesInfo = equipMap.get(eg.itemId);
			if(holesInfo == null) {
				log.error("获取装备镶嵌孔信息错误，找不到装备，itemId:{}", eg.itemId);
				continue;
			}
			//获取装备的镶嵌孔信息
			List<Long> jewelList = getJewelOnEquip(equipInfo); 
			
			//对装备的镶嵌孔进行遍历
			check:for(int i = 0 ; i < jewelList.size() ; i++){
				int addExp = 0;
				if( holesInfo[i] > 0){
					//宝石孔已开
					if(jewelList.get(i) <= 0){
						//未镶嵌宝石
						//遍历玩家宝石列表
						for(BagGrid bg : allJewelInBag){
							Fuwen jewelPeiZhi = jewelMap.get(bg.itemId);
							if( jewelPeiZhi == null ){
								continue;
							}
							if( jewelPeiZhi.inlayColor == holesInfo[i] ){
								//判断背包中有宝石可以镶嵌，装备id加入协议中
								resp.addRedPoint(eg.dbId);
								//只要有一颗宝石可以镶嵌，就结束此装备的检查
								break check;
							}
						}
					}else{
						//已镶嵌：获取镶嵌的宝石
						int jewelId = (int)(jewelList.get(i)>>32);
						Fuwen jewelOnEquip = jewelMap.get(jewelId);
						//遍历背包宝石列表，进行红点判断
						for(BagGrid bg : allJewelInBag){
							Fuwen jewelPeiZhi = jewelMap.get(bg.itemId);
							if( jewelPeiZhi == null ){
								continue;
							}
							if( jewelPeiZhi.inlayColor == holesInfo[i]  ){
								//镶嵌红点判断：颜色相同，计算经验
								//取巧：背包宝石等级高于镶嵌宝石，同样会触发经验足以合成的的判断
								addExp += (jewelPeiZhi.exp +(bg.instId>0? bg.instId:0)) *bg.cnt;
								if(addExp >= jewelOnEquip.lvlupExp && jewelOnEquip.fuwenLevel < jewelOnEquip.levelMax ){
									//添加经验足够升级，并且宝石等级不是最高，装备id加入协议，结束此装备的检查
									resp.addRedPoint(eg.dbId);
									break check;
								}
							}
						}
					}
				}
			}
		}
	}
	
	
}
