package com.qx.explore.treasure;

import java.util.Calendar;
import java.util.Date;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.robot.RobotSession;
import com.qx.world.Mission;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Scene.ExitScene;

public class BaoXiangScene extends Scene{

	public BaoXiangScene(String key) {
		super(key);
	}

	@Override
	public void completeMission(Mission mission) {
		int code = mission.code;
		final Builder builder = mission.builer;
		final IoSession session = mission.session;
		switch (code) {
		case PD.Enter_TBBXScene:
			mission.code = PD.Enter_Scene;
			super.completeMission(mission);
			ExploreTreasureMgr.inst.sendPickedInfo(session);
			ExploreTreasureMgr.inst.palyerCntChange();
			break;
		case PD.C_GET_BAO_XIANG:
			getBaoXiang(session,builder);
			break;
		default:
			super.completeMission(mission);
			break;
		}
	}
	
	@Override
	public void savePosInfo(Player ep) {
		//宝箱场景不保存坐标
		ExploreTreasureMgr.inst.palyerCntChange();
	}


	/**
	 * 玩家请求拾取宝箱
	 * @param session
	 * @param builder
	 */
	public synchronized void getBaoXiang(IoSession session, Builder builder) {
		ErrorMessage.Builder req = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		int uid = req.getErrorCode();
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		Player bx = players.get(uid);
		if(bx == null){
			log.info("{}想要拾取宝箱不存在的宝箱{}",jz.id, uid);
			return;
		}
		if(bx.session instanceof RobotSession==false){
			return;
		}
		BXRecord pickedBean = HibernateUtil.find(BXRecord.class, jz.id);
		if(pickedBean != null && pickedBean.bxCnt>=ExploreTreasureMgr.pickCntLimit){
			log.info("{} 拾取次数不被允许  {}>={}",
					jz.id,pickedBean.bxCnt,ExploreTreasureMgr.pickCntLimit);
			return;
		}
		
		players.remove(uid);
		ExploreTreasureMgr.inst.baoXiangPicked();
		//
		Integer cnt = (Integer) bx.session.getAttribute("YuanBaoCnt");
		if(cnt == null){
			log.error("宝箱元宝数量有误");
			return;
		}
		YuanBaoMgr.inst.diff(jz, cnt, 0, 0, YBType.ShiLianBaoXiang,
				"十连副本开宝箱");
		HibernateUtil.update(jz);
		ExitScene.Builder exitSc = ExitScene.newBuilder();
		exitSc.setUid(uid);
		broadCastEvent(exitSc.build(),0);
		//有返回，表示拾取成功。
		Integer pickerUid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene, 0);
		Player p = players.get(pickerUid);
		String desc = "";
		if(p != null){
			desc = p.name;
		}else{
			desc = "有玩家";
		}
		req.setCmd(pickerUid);
		req.setErrorCode(cnt);
		req.setErrorDesc(desc);
		ProtobufMsg info = new ProtobufMsg(PD.C_GET_BAO_XIANG, req);
//		session.write(info);
		broadCastEvent(info, 0);
		log.info("{}拾取{}成功，移除 {}，元宝{}",jz.id,bx.name,uid,cnt);
		JunZhuMgr.inst.sendMainInfo(session,jz);
//		BXRecord pickedBean = HibernateUtil.find(BXRecord.class, jz.id);
		if(pickedBean != null){//进入场景时就应该发送过信息了，那里会创建bean
			pickedBean.bxCnt +=1 ;
			pickedBean.yuanBao += cnt;
			HibernateUtil.update(pickedBean);
		}
		ExploreTreasureMgr.inst.sendPickedInfo(session);
		
	}
}
