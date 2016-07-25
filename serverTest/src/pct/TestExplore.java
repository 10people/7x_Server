package pct;

import java.util.List;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.VIP.RechargeReq;

public class TestExplore extends TestBase{
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		ExploreResp.Builder resp = (ExploreResp.Builder)builder;
		int type = resp.getSuccess();
		if(type != 0){
			RechargeReq.Builder req = RechargeReq.newBuilder();
			req.setType(9);
			req.setAmount(648);
			ProtobufMsg msg = new ProtobufMsg(PD.C_RECHARGE_REQ, req);
			cl.session.write(msg);
			cl.session.write(PD.C_TaskReq);
			cl.session.write(PD.C_BUY_TongBi);
		}else{
			//十连判断，十连为了抽将魂升星
			List<qxmobile.protobuf.Explore.Award.Builder> awardList = resp.getAwardsListBuilderList();
			if(awardList.size() >1){
				cl.session.write(PD.C_MIBAO_INFO_REQ);
				System.out.println("完成十连，请求将魂信息");
			}
		}
		
	}
}
