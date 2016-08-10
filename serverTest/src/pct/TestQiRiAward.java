package pct;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.activity.XianShiConstont;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.XianShi.GainAward;
import qxmobile.protobuf.XianShi.HuoDongInfo;
import qxmobile.protobuf.XianShi.XinShouXianShiInfo;

public class TestQiRiAward extends TestBase{
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		XinShouXianShiInfo.Builder resp= (XinShouXianShiInfo.Builder)builder;
		System.out.println("检查七日奖励是否领取！");
		boolean isAward = false;
		for(HuoDongInfo huoDongInfo:resp.getHuodongList()){
			int state = huoDongInfo.getState();
			if(state == 10){ //可以领取
				//尝试领取奖励
				isAward = true;
				System.out.println("尝试领取七日奖励");
				GainAward.Builder req = GainAward.newBuilder();
				req.setTypeId(XianShiConstont.QIRIQIANDAO_TYPE);
				req.setHuodongId(huoDongInfo.getHuodongId());
				ProtobufMsg msg = new ProtobufMsg();
				msg.id = PD.C_XINSHOU_XIANSHI_AWARD_REQ;
				msg.builder = req;
				session.write(msg);
			}
		}
		if(!isAward){
			System.out.println("没有七日奖励可以领取！");
		}
	}
}
