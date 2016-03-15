package com.qx.explore.treasure;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
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
			break;
		case PD.C_GET_BAO_XIANG:
			getBaoXiang(session,builder);
			break;
		default:
			super.completeMission(mission);
			break;
		}
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
		ExitScene.Builder exitSc = ExitScene.newBuilder();
		exitSc.setUid(uid);
		broadCastEvent(exitSc,0);
		//有返回，表示拾取成功。
		req.setCmd(0);
		req.setErrorCode(cnt);
		req.setErrorDesc("OK");
		session.write(new ProtobufMsg(PD.C_GET_BAO_XIANG, req));
		log.info("{}拾取{}成功，移除 {}",jz.id,bx.name,uid);
		JunZhuMgr.inst.sendMainInfo(session);
	}
}
