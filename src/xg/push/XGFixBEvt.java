package xg.push;

import org.apache.mina.core.session.IoSession;

import com.manu.network.BigSwitch;
import com.qx.account.AccountManager;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;

public class XGFixBEvt extends EventProc {
	public static XGFixBEvt inst;
	@Override
	public void proc(Event param) {
		Long junZhuId = (Long)param.param;
		if(junZhuId == null){
			XGFixB.log.info("null jzId");
			return;
		}
		IoSession session = AccountManager.sessionMap.get(junZhuId);
		if(session == null){
			XGFixB.log.info("null session");
			return;
		}
		String token = (String) session.getAttribute("XGTOKEN0982398");
		if(token == null){
			XGFixB.log.info("null token sid {}",session.getId());
			return;
		}
		XGFixB.log.info("do fix sid {}",session.getId());
		XGFixB.inst.saveToken(session, junZhuId, token);
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);
	}

}
