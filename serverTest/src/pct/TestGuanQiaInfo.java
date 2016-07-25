package pct;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.PveLevel.GuanQiaInfo;
import qxmobile.protobuf.PveLevel.GuanQiaInfoRequest;

public class TestGuanQiaInfo extends TestBase{
	@Override
	public void req(GameClient cl) {
		super.req(cl);
//		sendReq(cl);
	}
	public void sendReq(GameClient cl, int gId , boolean isChuanQi) {
		//请求关卡信息
		GuanQiaInfoRequest.Builder req = GuanQiaInfoRequest.newBuilder();
		req.setGuanQiaId(gId);
		req.setType(isChuanQi ? 2 : 0);
		cl.session.write(new ProtobufMsg(PD.PVE_GuanQia_Request, req));
		System.out.println("请求关卡信息");
	}
	
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		GuanQiaInfo.Builder ret = (GuanQiaInfo.Builder)builder;
		int tili = ret.getTili();
		boolean isChuanQi = ( tili > 6 ) ; 
		new TestEnterPVE().req(cl, ret.getGuanQiaId() , isChuanQi );
	}
}
