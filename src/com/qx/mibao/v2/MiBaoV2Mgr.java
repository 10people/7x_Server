package com.qx.mibao.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.MiBaoNew;
import com.manu.dynasty.template.MiBaoNewSuiPian;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.msg.ProtobufMsg;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.MibaoProtos.MibaoInfo;
import qxmobile.protobuf.MibaoProtos.MibaoInfoResp;

public class MiBaoV2Mgr {
	public static Logger log = LoggerFactory.getLogger(MiBaoV2Mgr.class.getSimpleName());
	public static MiBaoV2Mgr inst;
	public Map<Integer, MiBaoNew> confMap;
	public MiBaoV2Mgr(){
		inst = this;
		initData();
	}
	public void initData() {
		List<MiBaoNew> list = TempletService.listAll(MiBaoNew.class.getSimpleName());
		Map<Integer, MiBaoNew> map = new HashMap<>(list.size());
		list.stream().forEach(c->map.put(c.id, c));
		confMap = map;
	}
	public static int[] initialIds = {210101,210102,210103,210104,210105,
			210106,210107,210108,210109};
	public void sendMainInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		AtomicBoolean optional = new AtomicBoolean(false);
		MibaoInfoResp.Builder resp = getMibaoInfoResp(jz, optional);
		
		if(session.removeAttribute("Calc4Grow")!=null){
			session.setAttribute("Calc4GrowRet", resp);
			return;
		}
		if(optional.get()){
			//标记可激活秘术。
			session.setAttribute("MiBaoV2MgrJiHuoMiShu", Boolean.TRUE);
		}
		ProtobufMsg msg = new ProtobufMsg(PD.NEW_MIBAO_INFO, resp);
		session.write(msg);
		EventMgr.addEvent(ED.get_miShu_pinZhi_y, new Object[]{jz.id , resp.getLevelPoint() });
	}
	
	public MibaoInfoResp.Builder getMibaoInfoResp(JunZhu jz, AtomicBoolean optional) {
		List<MiBaoV2Bean> list = HibernateUtil.list(MiBaoV2Bean.class, 
				"where ownerId="+jz.id+" and main=true");
		MibaoInfoResp.Builder resp = MibaoInfoResp.newBuilder();
		resp.setRemainTime(0);
		Map<Integer, MiBaoV2Bean> dbMap = new HashMap<>(list.size());
		list.stream().forEach(b->dbMap.put(b.miBaoId%100, b));
		boolean doJiHuo=true;//默认可激活秘术
		int minLv = -1;
		for(int i : initialIds){
			MibaoInfo.Builder mibaoInfo = MibaoInfo.newBuilder();
			int tempId = i%100;
			mibaoInfo.setZhanLi(0);
			MiBaoV2Bean miBaoDB = dbMap.get(tempId);
			MiBaoNew conf = confMap.get(i);
			if(miBaoDB == null){//秘宝不再DB中
				minLv = 1;//后面会-1
				doJiHuo = false;//缺秘宝，不能激活秘术
				mibaoInfo.setMiBaoId(i);
				mibaoInfo.setTempId(i);
				mibaoInfo.setDbId(-1);
				mibaoInfo.setStar(0);
				mibaoInfo.setLevel(0);
				mibaoInfo.setSuiPianNum(0);
				mibaoInfo.setNeedSuipianNum(conf.jinjieNum);
			}else{
				if(miBaoDB.miBaoId/1000==211){//满了
					miBaoDB.miBaoId -= 100;
					miBaoDB.active=true;
					mibaoInfo.setSuiPianNum(0);
					minLv = 10;//后面会-1
				} else {
					mibaoInfo.setSuiPianNum(miBaoDB.suiPianNum);
				}
				conf = confMap.get(miBaoDB.miBaoId);
				if(minLv<0)minLv = conf.pinzhi;
				if(miBaoDB.active==false)doJiHuo = false;//有未激活秘宝，不能激活秘术
				mibaoInfo.setDbId(miBaoDB.dbId);
				mibaoInfo.setTempId(miBaoDB.miBaoId);
				mibaoInfo.setMiBaoId(miBaoDB.miBaoId);
				mibaoInfo.setStar(miBaoDB.active?1:0);//是否已激活
				mibaoInfo.setLevel(0);
//				mibaoInfo.setIsLock(lock);
				
				mibaoInfo.setNeedSuipianNum(conf.jinjieNum);
				/*
				int gongJi = 0;//clacMibaoAttr(chengZhang, miBaoCfg.getGongji(), miBaoCfg.getGongjiRate(), level);
				int fangYu = 0;//clacMibaoAttr(chengZhang, miBaoCfg.getFangyu(), miBaoCfg.getFangyuRate(), level);
				int shengMing = 0;//clacMibaoAttr(chengZhang, miBaoCfg.getShengming(), miBaoCfg.getShengmingRate(), level);
				mibaoInfo.setGongJi(gongJi);
				mibaoInfo.setFangYu(fangYu);
				mibaoInfo.setShengMing(shengMing);
				*/
				int zhanLi = 0;//JunZhuMgr.inst.calcMibaoZhanLi(gongJi, fangYu, shengMing, 
						//miBaoDB.getMiBaoId(), miBaoDB.getLevel());
				mibaoInfo.setZhanLi(zhanLi);
			}
			mibaoInfo.setGongJi(conf.gongji);
			mibaoInfo.setFangYu(conf.fangyu);
			mibaoInfo.setShengMing(conf.shengming);
//			log.info("id is {}", mibaoInfo.getMiBaoId());
			resp.addMiBaoList(mibaoInfo);
		}
		int activeMiShuLv = minLv - 1;//秘术等级比秘宝等级低1级，因为要激活所有秘宝才能激活秘术.
		resp.setLevelPoint(activeMiShuLv);//已激活了几个秘术
		optional.set(doJiHuo);
		return resp;
	}
	
	/**不要调用直接添加秘宝的方法*/
	public void addMiBao(JunZhu jz, AwardTemp a) {
		MiBaoNew conf = confMap.get(a.getItemId());
		if(conf == null){
			log.error("未知整体{}x{} give to {}",a.getItemId(),
					a.getItemNum(), jz.id);
			return;
		}
		MiBaoV2Bean bean = HibernateUtil.find(MiBaoV2Bean.class, "where ownerId="+jz.id
				+" and miBaoId="+a.getItemId());
		if(bean == null){
			bean = new MiBaoV2Bean();
			bean.ownerId = jz.id;
			bean.miBaoId = a.getItemId();
			bean.suiPianNum=a.getItemNum()*conf.jinjieNum;
			bean.main = ( (bean.miBaoId / 100) % 10  ) == 1;//等级1的设置为主线.
			HibernateUtil.insert(bean);
		}else{
			//转换为碎片
			bean.suiPianNum+=a.getItemNum()*conf.jinjieNum;
			HibernateUtil.update(bean);
			EventMgr.addEvent(ED.get_mbSuiPian_x_y, new Object[]{jz.id});
		}
	}
	public void addMiBaoSuiPian(JunZhu jz,final AwardTemp a) {
		List<MiBaoNewSuiPian> list = TempletService.listAll(MiBaoNewSuiPian.class.getSimpleName());		
		Optional<MiBaoNewSuiPian> op = list.stream().filter(c->c.getId()==a.getItemId()).findAny();
		if(op.isPresent()==false){
			log.error("未知碎片{}x{} give to {}",a.getItemId(),
					a.getItemNum(), jz.id);
			return;
		}
		int mibaoID = op.get().mibaoID;
		
		MiBaoV2Bean bean = HibernateUtil.find(MiBaoV2Bean.class, "where ownerId="+jz.id
				+" and miBaoId="+mibaoID);
		if(bean == null){
			bean = new MiBaoV2Bean();
			bean.ownerId = jz.id;
			bean.miBaoId = mibaoID;
			bean.suiPianNum=a.getItemNum();
			bean.main = ( (bean.miBaoId / 100) % 10 ) == 1;//等级1的设置为主线.
			HibernateUtil.insert(bean);
		}else{
			//转换为碎片
			bean.suiPianNum+=a.getItemNum();
			HibernateUtil.update(bean);
		}
		IoSession session = SessionManager.inst.getIoSession(jz.id);
		if(session != null) {
			sendMainInfo(0, session, null);
		}
		EventMgr.addEvent(ED.get_mbSuiPian_x_y, new Object[]{jz.id});
		//实际是检查红点
		JunZhuMgr.inst.calcNewMiBao(jz);
	}
	public void jiHuo(int id, IoSession session, Builder builder) {
		ErrorMessage.Builder req = (ErrorMessage.Builder)builder;
		String str = req.getErrorDesc();
		long dbId = Long.parseLong(str);
		MiBaoV2Bean bean = HibernateUtil.find(MiBaoV2Bean.class, dbId);
		if(bean == null){
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		if(bean.ownerId != jz.id){
			return;
		}
		if(bean.active){
			return;
		}
		MiBaoNew conf = confMap.get(bean.miBaoId);
		if(conf == null){
			return;
		}
		if(bean.suiPianNum<conf.jinjieNum){
			return;
		}
		bean.suiPianNum-=conf.jinjieNum;
//		bean.miBaoId+=1;
		bean.active=true;
		//这里有问题，如有有提前已获得的高级秘宝
		HibernateUtil.update(bean);
		EventMgr.addEvent(ED.get_miBao_x_pinZhi_y, new Object[]{jz.id});
		JunZhuMgr.inst.sendMainInfo(session,jz);
	}
	public void miShuJiHuo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		Object o = session.removeAttribute("MiBaoV2MgrJiHuoMiShu");
		if(o==null){
			log.warn("没有激活秘术的标记{}",jz.id);
			return;
		}
		//条件里需要加上主线判断。
		List<MiBaoV2Bean> list = HibernateUtil.list(MiBaoV2Bean.class,
				"where ownerId="+jz.id+" and main=true");
		if(list.size() != 9){
			log.error("秘宝激活数据有误{},{}",jz.id,list.size());
			return;
		}
		for(MiBaoV2Bean bean : list){
			if(bean.miBaoId/1000==211){//满了
				break;
			}
			MiBaoNew conf = confMap.get(bean.miBaoId);
			//查下一等级的是否已经有了
			String hql = "where ownerId="+jz.id+" and miBaoId="+(bean.miBaoId+100) ;
			log.error(hql);
			MiBaoV2Bean nextBean = HibernateUtil.find(MiBaoV2Bean.class, 
					hql );
			if(nextBean == null){
				nextBean = new MiBaoV2Bean();
				nextBean.ownerId = jz.id;
				nextBean.main = true;
				nextBean.miBaoId = bean.miBaoId+100;
				HibernateUtil.insert(nextBean);
			}else{
				nextBean.main =  true;
				HibernateUtil.update(nextBean);
			}
			bean.main = false;
			HibernateUtil.update(bean);
		}
		JunZhuMgr.inst.sendMainInfo(session,jz);
	}
	/**传入玩家ID和秘宝品质，返回不低于秘宝品质的秘宝数量*/
	public int getMiBaoNum(int pinZhi , long junZhuId){
		int res = 0;
		String hql = "where ownerId = "+junZhuId +" and active=true";
		List<MiBaoV2Bean> miBaoList = HibernateUtil.list(MiBaoV2Bean.class, hql ) ;
		for(MiBaoV2Bean miBao :miBaoList){
			if(miBao != null){
				MiBaoNew miBaoTemp = confMap.get(miBao.miBaoId);
				if(miBaoTemp != null && miBaoTemp.pinzhi >= pinZhi){
					res ++ ;
				}
			}
		}
		return res ;
	}
	/**传入玩家ID，返回玩家的秘术品质*/
	public int getMaxMiShu(long junZhuId){
		JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId);
		if(jz == null){
			return 0;
		}
		MibaoInfoResp.Builder resp = getMibaoInfoResp(jz, new AtomicBoolean());
		return resp.getLevelPoint();
	}
	/**根据传入的君主id和秘宝id，返回君主拥有的秘宝碎片数量
	 * 如果已激活此秘宝，则返回碎片数量+激活需要的数量
	 * */
	public int getMiBaoSuiPianNum(long junZhuId , int miBaoId){
		int res = 0 ;
		String hql = "where ownerId = "+junZhuId +" and miBaoId = " + miBaoId ;
		MiBaoV2Bean bean =  HibernateUtil.find(MiBaoV2Bean.class, hql);
		if(bean != null){
			if(bean.active){
				res = confMap.get(bean.miBaoId).jinjieNum ;
				return res + bean.suiPianNum ;
			}else{
				return bean.suiPianNum ;
			}
		}
		return res ;
	}
	/**推送秘宝信息，主要是当前秘宝的碎片数量*/
	public void sendMiBaoInfo(int id, IoSession session, Builder builder){
		if(session == null ){
			return ;
		}
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			return;
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_SEND_MIBAO_INFO;
		AtomicBoolean optional = new AtomicBoolean(false);
		msg.builder = MiBaoV2Mgr.inst.getMibaoInfoResp(junZhu, optional);
		session.write(msg);
	}
}
