package pct;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.qx.test.main.GameClient;
import com.qx.test.main.WuJiangTest;

import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;

public class TestJzInfo extends TestBase{
	public static int cnt = 0 ;
	@Override
	public void req(GameClient cl) {
		super.req(cl);
		cl.session.write(PD.JunZhuInfoReq);
	}
	
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
//		super.handle(id, session, builder, cl);
		JunZhuInfoRet.Builder jzInfo = WuJiangTest.readJunZhuInfo(session, builder);
		cl.jzId = jzInfo.getId();
		cl.session.setAttachment(jzInfo);
//		System.out.println("TestJzInfo调用"+TestJzInfo.cnt++);
	}
}
