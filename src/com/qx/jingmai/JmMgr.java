package com.qx.jingmai;

import java.util.Arrays;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.JingMaiProto.JingMaiReq;
import qxmobile.protobuf.JingMaiProto.JingMaiRet;
import qxmobile.protobuf.JingMaiProto.XueWeiUpReq;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.Jingmai;
import com.manu.network.SessionAttKey;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;

/**
 * 静脉管理。
 * @author 康建虎
 *
 */
public class JmMgr {
	public static Logger log = LoggerFactory.getLogger(JmMgr.class);
	public static JmMgr inst;
	public JmMgr(){
		inst = this;
	}
	public void sendInfo(IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null junZhuId id");
			return ;
		}
		JingMaiReq.Builder req = (qxmobile.protobuf.JingMaiProto.JingMaiReq.Builder) builder;
		JmBean bean = HibernateUtil.find(JmBean.class, junZhuId);
		if(bean == null){
			bean = new JmBean();
			bean.dbId = junZhuId;
			bean.daMai =req.getDaMaiHao();
			bean.zhouTian = 1;
			bean.xueWei = new int[20];
			// 添加到缓存中
			MC.add(bean, junZhuId);
			HibernateUtil.insert(bean);
		}
		sendInfo(session, bean);
	}
	public void sendInfo(IoSession session, JmBean bean) {
		JingMaiRet.Builder ret = JingMaiRet.newBuilder();
		ret.setDaMaiHao(bean.daMai);
		ret.setZhouTian(bean.zhouTian);
		for(int i : bean.xueWei){
			ret.addXueWei(i);
		}
		Jingmai effect = calcEffect(bean);
		ret.setGongji(effect.gongji);
		ret.setFangyu(effect.fangyu);
		ret.setShengming(effect.shengming);
		ret.setTongli(effect.tongli);
		ret.setMouli(effect.mouli);
		ret.setYongli(effect.yongli);
		ret.setPoint(bean.point);
		session.write(ret.build());
	}
	public void xueWeiUp(IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null junZhuId id");
			return ;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		XueWeiUpReq.Builder req = (qxmobile.protobuf.JingMaiProto.XueWeiUpReq.Builder) builder;
		JmBean bean = HibernateUtil.find(JmBean.class, junZhuId);
		if(bean == null || bean.point<1){
			sendError(session, "您的经脉点数不足，请培养武将来获得更多经脉点数。");
			return;
		}
		List<Jingmai> list = TempletService.listAll(Jingmai.class.getSimpleName());
		int mai = req.getDaMaiHao();
		int xue = req.getXueWei();
		Jingmai conf = null;
		for(Jingmai m : list){
			if(m.xuewei == xue && m.jingmai == mai){
				conf = m;
				break;
			}
		}
		if(conf == null){
			sendError(session, "找不到配置，经脉"+mai+"穴位"+xue);
			return;
		}
		bean.point -= 1;
		HibernateUtil.save(bean);
		bean.xueWei[conf.xuewei] += 1;
		//最有一个（10）穴位到了12周天，则此大脉完毕。
		if(conf.xuewei == 10 && bean.xueWei[conf.xuewei] == 12 && bean.daMai<8){
			bean.daMai += 1;
			Arrays.fill(bean.xueWei, 0);
		}
		HibernateUtil.save(bean);
		log.info("pid {} 经脉 {} 穴位 {} 加1点 到了{}",
				junZhuId, mai, xue, bean.xueWei[conf.xuewei]);
		sendInfo(session, bean);		
	}
	/**
	 * 计算经脉加成。
	 * @return
	 */
	public Jingmai calcEffect(JmBean bean){
		Jingmai ret = new Jingmai();
		if(bean == null){
			return ret;
		}
		List<Jingmai> list = TempletService.listAll(Jingmai.class.getSimpleName());
		int[] arr = bean.xueWei;
		for(Jingmai m : list){
			if(m.getJingmai() > bean.daMai){
				//配置是有序的，大脉超出，则后面的没开启。
				break;
			}
			if(arr[m.xuewei]>=m.level){
				ret.gongji += m.gongji;
				ret.fangyu += m.fangyu;
				ret.shengming += m.shengming;
				ret.tongli += m.tongli;
				ret.yongli += m.yongli;
				ret.mouli += m.mouli;
			}
		}
		return ret;
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
		log.debug("sent keji info");
	}
}
