package pct;

import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.main.GameClient;

public class TestChengJiuPage extends TestBase{
	@Override
	public void req(GameClient cl) {
		super.req(cl);
		//请求成就信息列表
		cl.session.write(PD.C_ACTIVITY_ACHIEVEMENT_INFO_REQ);
		System.out.println("请求成就列表信息");
	}
}
