package pct;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.main.GameClient;

public class TestBase {
	public long preMS;
	public void req(GameClient cl){
		preMS = System.currentTimeMillis();
	}
	public void handle(int id,IoSession session, Builder builder,GameClient cl){
		long cur = System.currentTimeMillis();
		long diff = cur - preMS;
		if(diff>100){
			System.out.print("----响应慢----");
			System.out.print(getClass().getSimpleName());
			System.out.print(",diff:");
			System.out.println(diff);
		}
		req(cl);
	}
	
	public void send(GameClient cl, short pid,Builder b ){
		Object obj = cl.session.getAttribute("STARUP");
		if(obj != null && pid == PD.C_MIBAO_LEVELUP_REQ){
			return ;
		}
		cl.session.write(new ProtobufMsg(pid, b));
	}
}
