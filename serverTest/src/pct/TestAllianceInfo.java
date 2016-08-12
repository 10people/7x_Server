package pct;

import java.util.List;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.AllianceProtos.AllianceHaveResp;
import qxmobile.protobuf.AllianceProtos.AllianceNonResp;
import qxmobile.protobuf.AllianceProtos.ApplyAlliance;
import qxmobile.protobuf.AllianceProtos.NonAllianceInfo;
import qxmobile.protobuf.AllianceProtos.immediatelyJoin;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;

public class TestAllianceInfo extends TestBase{
	@Override
	public void req(GameClient cl) {
		super.req(cl);
		sendReq(cl, PD.ALLIANCE_INFO_REQ);
	}
	public void sendReq(GameClient cl, short gId) {
		//请求关卡信息
		cl.session.write(gId);
		System.out.println("请求联盟信息");
	}
	
	@Override
	public void handle(int cmd, IoSession session, Builder builder, GameClient cl) {
		switch (cmd) {
		case PD.ALLIANCE_NON_RESP:
			procNonAllianceInfoResp(builder, cl);
			break;
		case PD.ALLIANCE_HAVE_RESP:
			procAllianceInfoResp(builder);
			break;
		
		default:
			break;
		}
	}

	public void procNonAllianceInfoResp(Builder builder, GameClient cl) {
		System.out.println("收到联盟返回数据，无联盟，用时：" +(System.currentTimeMillis()-preMS)+"毫秒");
		AllianceNonResp.Builder response = (qxmobile.protobuf.AllianceProtos.AllianceNonResp.Builder) builder;
		System.out.println("联盟数量：" + response.getAlincInfoCount());
		List<NonAllianceInfo> infoList = response.getAlincInfoList();
		JunZhuInfoRet.Builder jzInfo = (qxmobile.protobuf.JunZhuProto.JunZhuInfoRet.Builder) cl.session.getAttachment();
		if(jzInfo == null){
//			req(cl);
			System.out.println("君主信息为空");
			return;
		}
		int state = 0;// 0-没有能加入的联盟，1-立刻加入了一个联盟，2-申请了一个联盟
		for(NonAllianceInfo info : infoList) {
			if(info.getApplyLevel() > jzInfo.getLevel() || info.getJunXian() > jzInfo.getJunXian()) {
				continue;
			}
			ProtobufMsg msg = new ProtobufMsg();
			if(info.getIsShenPi() == 1) {
				immediatelyJoin.Builder request = immediatelyJoin.newBuilder();
				request.setLianMengId(info.getId());
				msg.id = PD.IMMEDIATELY_JOIN;
				msg.builder = request;
				cl.session.write(msg);
				state = 1;
			} else {
				ApplyAlliance.Builder request = ApplyAlliance.newBuilder();
				request.setId(10014);
				msg.id = PD.APPLY_ALLIANCE;
				msg.builder = request;
				state = 2;
			}
			cl.session.write(msg);
			break;
		}
		switch (state) {
		case 0:	System.out.println("没有能够加入的联盟");		break;	
		case 1:	System.out.println("立刻加入了一个联盟");		break;	
		case 2:	System.out.println("申请了一个的联盟");		break;	
		default:
			break;
		}
	}
	
	public void procAllianceInfoResp(Builder builder) {
		boolean haveAlliance = false;
		if(builder instanceof AllianceHaveResp.Builder) {
			haveAlliance = true;
		}
		System.out.println("收到联盟返回数据，" + (haveAlliance ? "有" : "无")+"联盟，用时：" +(System.currentTimeMillis()-preMS)+"毫秒");
	}
	
	public void reqMemberList(GameClient cl) {
		cl.session.write(PD.LOOK_MEMBERS);
		System.out.println("请求联盟所有成员信息");
	}
}
