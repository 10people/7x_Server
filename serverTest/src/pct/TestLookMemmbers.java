package pct;

import java.util.List;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.AllianceProtos.LookMembersResp;
import qxmobile.protobuf.AllianceProtos.MemberInfo;

public class TestLookMemmbers extends TestBase{
	@Override
	public void req(GameClient cl) {
		super.req(cl);
		cl.session.write(PD.LOOK_MEMBERS);
	}
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		switch (id) {
		case PD.LOOK_MEMBERS_RESP:
			System.out.println("服务器返回成员信息");
			getMemmbers(session, builder);
			break;
		case PD.IMMEDIATELY_JOIN_RESP:
			System.out.println("成功加入联盟 现请求获取成员信息");
			req(cl);
			break;
		default:
			break;
		}
	}
	public void getMemmbers(IoSession session, Builder builder){
		LookMembersResp.Builder response = (LookMembersResp.Builder) builder;
		List<MemberInfo> list = response.getMemberInfoList();
		for(MemberInfo a:list){
			System.out.println("君主Id:"+a.getJunzhuId()+"君主名称:"+a.getName()+"等级"+a.getLevel()+"战力:"+a.getZhanLi());
		}
	}
	
	
}
