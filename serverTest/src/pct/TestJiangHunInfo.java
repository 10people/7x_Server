package pct;

import java.util.List;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.MibaoProtos.MibaoInfo;
import qxmobile.protobuf.MibaoProtos.MibaoInfoResp;
import qxmobile.protobuf.MibaoProtos.MibaoStarUpReq;

public class TestJiangHunInfo extends TestBase{
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl){
		Object obj = cl.session.removeAttribute("STARUP");
		if(obj == null ){
			System.out.println("没有升星标记，开始升级");
			cl.readMB(builder);
		}else{
			System.out.println("有升星标记，开始升星");
			MibaoInfoResp.Builder resp = (MibaoInfoResp.Builder) builder;
			List<MibaoInfo.Builder> jiangHunList = resp.getMiBaoListBuilderList();
			MibaoStarUpReq.Builder request = MibaoStarUpReq.newBuilder();
			System.out.println("收到将魂信息返回，准备升星");
			for( MibaoInfo.Builder jiangHunInfo : jiangHunList ){
				if(jiangHunInfo.getSuiPianNum() >= jiangHunInfo.getNeedSuipianNum() ){
					request.setMibaoId(jiangHunInfo.getMiBaoId());
					cl.session.write(request.build());
					return;
				}
			}
			cl.session.write(PD.C_TaskReq);
		}
	}
}
