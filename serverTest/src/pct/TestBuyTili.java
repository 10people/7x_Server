package pct;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.ZhanDou.ZhanDouInitError;

public class TestBuyTili extends TestBase{
	
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		ZhanDouInitError.Builder resp = (ZhanDouInitError.Builder)builder;
		if("体力不足，无法进入战斗".equals(resp.getResult())){
			cl.session.write(PD.C_BUY_TiLi);
		}
		cl.session.write(PD.C_TaskReq);
	}
}
