package pct;

import java.util.List;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.LieFuProto.LieFuActionInfo;
import qxmobile.protobuf.LieFuProto.LieFuActionInfoResp;
import qxmobile.protobuf.LieFuProto.LieFuActionReq;

public class TestLieFu extends TestBase{
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
			LieFuActionInfoResp.Builder response = (LieFuActionInfoResp.Builder)builder;
			List<LieFuActionInfo.Builder > liefuList =  response.getLieFuActionInfoBuilderList();
			int type = 1 ;
			for(LieFuActionInfo.Builder info : liefuList ){
				if(info.getState() == 1){
					type = Math.max(type, info.getType());
				}
			}
			LieFuActionReq.Builder req = LieFuActionReq.newBuilder();
			req.setType(type);
			cl.session.write(req);
			cl.session.write(PD.C_TaskReq);
	}

}
