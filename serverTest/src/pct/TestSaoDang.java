package pct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.main.GameClient;
import com.qx.test.main.WuJiangTest;
import com.qx.util.RandomUtil;

import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;
import qxmobile.protobuf.PveLevel.Level;
import qxmobile.protobuf.PveLevel.PvePageReq;
import qxmobile.protobuf.PveLevel.PveSaoDangAward;
import qxmobile.protobuf.PveLevel.PveSaoDangReq;
import qxmobile.protobuf.PveLevel.PveSaoDangRet;
import qxmobile.protobuf.PveLevel.Section;

public class TestSaoDang extends TestBase{

public ProtobufMsg reqsaoDang(int guanQiaId,int saoDangTimes){
	PveSaoDangReq.Builder req = PveSaoDangReq.newBuilder();
	req.setGuanQiaId(guanQiaId);
	req.setTimes(saoDangTimes);
	ProtobufMsg msg = new ProtobufMsg();
	msg.builder = req;
	msg.id = PD.C_PVE_SAO_DANG;
	return msg;
	
}
    @Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		switch (id) {
        case PD.S_PVE_SAO_DANG:
        	System.out.println("服务器返回扫荡结果");
            getSaoDangResult(session, builder);
			break;
        case PD.PVE_PAGE_RET:
        	System.out.println("服务器返回章节关卡信息");
        	getChptGuanQiaList(session, builder);
			break;
        case PD.JunZhuInfoRet:
        	getJzInfo(session,builder);
        	break;
		default:
			break;
		}
	}
    
    public void getSaoDangResult(IoSession session, Builder builder){
    	PveSaoDangRet.Builder ret = (qxmobile.protobuf.PveLevel.PveSaoDangRet.Builder) builder;
    	int result = ret.getResult();
    	String msg = "";
//    	结果：0-成功，1-扫荡次数已用完,2-传奇关的挑战次数已用完,3-游侠关还未通关，4-游侠关处于cd时间
    	switch (result) {
		case 0:
			msg = "成功";
			break;
		case 1:
			msg = "失败，扫荡次数已用完";
			break;
		case 2:
			msg = "失败，传奇关卡的挑战次数已用完";
			break;
		case 3:
			msg = "失败，游侠关还未通关";
			break;
		case 4:
			msg = "失败，游侠关处于cd时间";
			break;
		default:
			break;
		}
    	List<PveSaoDangAward> list = ret.getAwardsList();
    	String awardMsg = "";
    	for(PveSaoDangAward a:list){
    		awardMsg +="经验:"+a.getExp()+"金钱:"+a.getMoney()+"奖品数:"+a.getAwardItemsCount();
    	}
    	System.out.println("扫荡关卡Id："+ret.getGuanQiaId()+"扫荡结果:"+msg+"收获:"+awardMsg);
    }
    
    public void autoSaoDang(IoSession session,int guanQiaId,int saoDangTimes) {
		ProtobufMsg msg = reqsaoDang(guanQiaId, saoDangTimes);
		System.out.println("发送扫荡请求");
		session.write(msg);
	}
    
    public void reqGuanQiaList(IoSession session){
    	PvePageReq.Builder req = PvePageReq.newBuilder();
    	req.setSSection(RandomUtil.getRandomNum(6));
    	ProtobufMsg msg = new ProtobufMsg();
    	msg.builder = req;
    	msg.id = PD.PVE_PAGE_REQ;
    	System.out.println("请求随机章节关卡信息");
    	session.write(msg);
    }
    
    public void getChptGuanQiaList(IoSession session, Builder builder) {
    	boolean flag = false;
    	Section.Builder b = (qxmobile.protobuf.PveLevel.Section.Builder) builder;
		List<Level> list = b.getSAllLevelList();
		List<Integer> list1 = new ArrayList<Integer>();
		for (int i =0;i<list.size();i++){
			list1.add(i);
		}
	    Collections.shuffle(list1);
		for(int j =0;j<list1.size();j++){
			Level a = list.get(list1.get(j));
			System.out.println("winLevel:"+a.getWinLevel()+"type"+a.getType());
			if(a.getWinLevel() >= 111 && a.getType() == 1){
				System.out.println("就是它了 扫荡它"+a.getGuanQiaId());
				session.write(PD.JunZhuInfoReq);
				autoSaoDang(session, a.getGuanQiaId(), 10);
				flag = true;
				break;
			}
		}
		if(!flag){
			reqGuanQiaList(session);
		}
	}
  
    public void getJzInfo(IoSession session, Builder builder){
    	JunZhuInfoRet.Builder jzInfo = WuJiangTest.readJunZhuInfo(session, builder);
		if(jzInfo.getTili() < 999){
			session.write(PD.C_BUY_TiLi);
		}
    }
}
