/*package com.xh.test;

import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.aspectj.apache.bcel.generic.Select;

import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;

import qxmobile.protobuf.AlliaceMemmber.AllianceMemmberList;
import qxmobile.protobuf.AlliaceMemmber.GetMemmbersReq;
import qxmobile.protobuf.FriendsProtos.GetFriendListResp;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;
import qxmobile.protobuf.JunzhuInfo.LevelReq;
import qxmobile.protobuf.JunzhuInfo.LevelResp;

public class TestJunzhuMgr {

	public void getJunzhuLevel(int id, IoSession session, Builder builder) {
		LevelReq.Builder req = (qxmobile.protobuf.JunzhuInfo.LevelReq.Builder) builder;
		long junzhuId = req.getJunzhuId();
		JunZhu jz = HibernateUtil.find(JunZhu.class, junzhuId);
		LevelResp.Builder resp = LevelResp.newBuilder();
		resp.setJunzhuId(junzhuId);
		if(null != jz){
			resp.setJunzhuLevel(jz.level);
		}
		else{//君主不存在
			resp.setJunzhuLevel(-1);
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_JUNZHU_LEVEL_RESP;
		msg.builder = resp;
		session.write(msg);
	}

	public void getMemmbers(int id, IoSession session, Builder builder) {
		GetMemmbersReq.Builder req = (qxmobile.protobuf.AlliaceMemmber.GetMemmbersReq.Builder) builder;
		int allianceId = req.getAllianceId();
		String sql = "select id,name,gender,level,exp from JunZhu where id in (select junzhuId from AlliancePlayer where lianMengId = "+allianceId+")";
	    List<Map<String, Object>> list = HibernateUtil.querySql(sql);
	    AllianceMemmberList.Builder response = AllianceMemmberList.newBuilder();
	    if(null != list && list.size()>0){
	    	for(Map<String, Object> a:list){
	    		JunZhuInfoRet.Builder memmber = JunZhuInfoRet.newBuilder();
	    		memmber.setId(Integer.parseInt(a.get("id").toString()));
	    		memmber.setName((String) a.get("name"));
	    		memmber.setGender(1);
	    		memmber.setLevel((int) a.get("level"));
	    		memmber.setExp(Long.valueOf(a.get("exp").toString()));
	    		memmber.setLianMengId(allianceId);
	    		response.addMember(memmber);
	    	}
	    	response.setSize(list.size());
	    }else{
	    	response.setSize(-1);
	    }
	    ProtobufMsg msg = new ProtobufMsg();
	    msg.id = PD.S_ALLIANCE_MEMMBERS_RESP;
	    msg.builder = response;
	    session.write(msg);
	}

}
*/