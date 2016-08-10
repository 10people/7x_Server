package pct;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.Qiandao.GetQiandaoResp;
import qxmobile.protobuf.Qiandao.GetVipDoubleReq;
import qxmobile.protobuf.Qiandao.QiandaoAward;

public class TestQiandao extends TestBase{
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		GetQiandaoResp.Builder resp = (GetQiandaoResp.Builder) builder;
		System.out.println("检查是否签到！");
		int leiji = resp.getCnt();
		boolean isQiandao = true;
		for (QiandaoAward qiandaoAward : resp.getAwardList()) {
			int state = qiandaoAward.getState();
			if(state == 1){
				//签到
				isQiandao = false;
				System.out.println("今天没有签到，立即签到！");
				session.write(PD.C_QIANDAO_REQ);
			}else if(state == 0 && qiandaoAward.getIsDouble() == 1 && leiji > 0){
				//领取双倍
				isQiandao = false;
				GetVipDoubleReq.Builder req = GetVipDoubleReq.newBuilder();
				ProtobufMsg msg = new ProtobufMsg();
				req.setId(qiandaoAward.getDay() - 1);
				msg.id = PD.C_GET_QIANDAO_DOUBLE_REQ;
				System.out.println("补领第" + qiandaoAward.getDay() + "天奖励");
				msg.builder = req;
				session.write(msg);
			}
			if(leiji <= 0){
				break;
			}
			leiji--;
		}
		if(isQiandao){
			System.out.println("没有签到奖励可领！");
		}
		
	}
}
