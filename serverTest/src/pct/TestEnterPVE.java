package pct;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.PveLevel.PveBattleOver;
import qxmobile.protobuf.ZhanDou.LevelType;
import qxmobile.protobuf.ZhanDou.PveZhanDouInitReq;

public class TestEnterPVE extends TestBase{
	@Override
	public void req(GameClient cl) {
	}
	public void req(GameClient cl, int cp ,boolean isChuanQi){
		PveZhanDouInitReq.Builder req = PveZhanDouInitReq.newBuilder();
		int zhangJieId = req.getChapterId();
		req.setChapterId(cp);
		req.setLevelType(isChuanQi ? LevelType.LEVEL_TALE : LevelType.LEVEL_NORMAL );
		System.out.println("PveZhanDouInitReq，LevelType" + req.getChapterId());
//		req.setLevelType(LevelType.LEVEL_NORMAL);
		send(cl,PD.ZHANDOU_INIT_PVE_REQ, req);
		System.out.println("请求进入关卡");
		lastId = cp;
	}
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		PveBattleOver.Builder request = PveBattleOver.newBuilder();
		request.setSPass(true);
		request.setSSection(1);
		request.setStar(111);
		request.setAchievement(111);
		send(cl,PD.PVE_BATTLE_OVER_REPORT, request);
		System.out.println("发送获胜。");
		cl.session.write(PD.C_TaskReq);
	}
	public static int lastId;
}
