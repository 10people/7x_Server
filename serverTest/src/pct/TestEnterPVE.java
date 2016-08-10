package pct;



import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.PveTemp;
import com.manu.network.PD;
import com.qx.test.main.GameClient;
import com.qx.util.RandomUtil;

import qxmobile.protobuf.PveLevel.PveBattleOver;
import qxmobile.protobuf.ZhanDou.LevelType;
import qxmobile.protobuf.ZhanDou.PveZhanDouInitReq;

public class TestEnterPVE extends TestBase{
	@Override
	public void req(GameClient cl) {
	}
	public void req(GameClient cl, int cp ,boolean isChuanQi){
		if(cl.lasdPveId != 0){
			return;
		}
		PveZhanDouInitReq.Builder req = PveZhanDouInitReq.newBuilder();
		int zhangJieId = req.getChapterId();
		req.setChapterId(cp);
		req.setLevelType(isChuanQi ? LevelType.LEVEL_TALE : LevelType.LEVEL_NORMAL );
//		req.setLevelType(LevelType.LEVEL_NORMAL);
		send(cl,PD.ZHANDOU_INIT_PVE_REQ, req);
		System.out.println("请求进入关卡:"+req.getChapterId());
		cl.lasdPveId = cp;
	}
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		PveBattleOver.Builder request = PveBattleOver.newBuilder();
		request.setSPass(true);
		request.setSSection(1);
		request.setStar(111);
		request.setAchievement(111);
		send(cl,PD.PVE_BATTLE_OVER_REPORT, request);
		System.out.println("发送获胜。"+cl.lasdPveId);
		PveTemp pt = TestTask.pveMap.get(cl.lasdPveId);
		if(cl.lasdPveId%10>=7){
			cl.testTask.getZhangJieJL(cl, pt.bigId);
		}
		if(cl.lasdPveId==100307){
			cl.testTask.req(cl);
		}
		if(pt == null){
			cl.lasdPveId+=0;
		}
		if(cl.lasdPveId>0 && pt.star1>0){
			cl.testTask.lingQuStarAward(cl, cl.lasdPveId, pt.star1);
			cl.testTask.lingQuStarAward(cl, cl.lasdPveId, pt.star2);
			cl.testTask.lingQuStarAward(cl, cl.lasdPveId, pt.star3);
			// 扫荡此关卡一次
			//
			cl.session.write(new TestSaoDang().reqsaoDang(cl.lasdPveId, 1));
		}
		
		cl.testTask.tryIds.clear();
		cl.lasdPveId = 0;
	}
}
