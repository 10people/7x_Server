package com.qx.alliance.building;

import java.util.List;

import groovy.servlet.TemplateServlet;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.JianZhu.JianZhuInfo;
import qxmobile.protobuf.JianZhu.JianZhuList;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.LianMengKeZhan;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliance.MoBaiBean;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;

/**
 * 联盟建筑管理器
 * @author 康建虎
 *
 */
public class JianZhuMgr {
	public static JianZhuMgr inst;
	public static Logger log = LoggerFactory.getLogger(JianZhuMgr.class.getSimpleName());

	public void getInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		JianZhuLvBean bean = HibernateUtil.find(JianZhuLvBean.class, member.lianMengId);
		if(bean == null){
			bean = new JianZhuLvBean();
			bean.keZhanLv=bean.shuYuanLv=bean.shangPuLv=bean.zongMiaoLv=bean.tuTengLv=1;
		}
		JianZhuList.Builder ret = JianZhuList.newBuilder();
		int[] lvArr = {bean.keZhanLv,bean.shuYuanLv,bean.shangPuLv,bean.zongMiaoLv,bean.tuTengLv};
		for(int lv:lvArr){
			JianZhuInfo.Builder info = JianZhuInfo.newBuilder();
			info.setLv(lv);
			ret.addList(info);
		}
		session.write(ret.build());
	}
	
	public void sendError(int cmd, IoSession session, String msg) {
		if (session == null) {
			log.warn("session is null: {}", msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	@SuppressWarnings("unused")
	public void up(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		JianZhuLvBean bean = HibernateUtil.find(JianZhuLvBean.class, member.lianMengId);
		if(bean == null){
			bean = new JianZhuLvBean();
			bean.keZhanLv=bean.shuYuanLv=bean.shangPuLv=bean.zongMiaoLv=bean.tuTengLv=1;
			HibernateUtil.insert(bean);
		}
		AllianceBean lmBean = HibernateUtil.find(AllianceBean.class, member.lianMengId);
		if(lmBean == null){
			sendError(3,session,"没有联盟数据");
			return;
		}
		//1客栈；2书院；3图腾；4商铺；5宗庙
		ErrorMessage.Builder req = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		int code = req.getErrorCode();
		switch(code){
		case 1:{
			//upKeZhan();
			List<LianMengKeZhan> list = TempletService.listAll(LianMengKeZhan.class.getSimpleName());
			if(list == null){
				sendError(10, session, "配置缺失。");
				return;
			}
			if(bean.keZhanLv>=list.size()){
				sendError(20, session, "已满级");
				return;
			}
			if(bean.keZhanLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
			}
			LianMengKeZhan conf = list.get(bean.keZhanLv);
			//FIXME 消耗数值
			bean.keZhanLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "升级成功");
			break;
		}
		}
	}
	
}
