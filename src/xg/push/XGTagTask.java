package xg.push;

import org.apache.mina.core.session.IoSession;

public class XGTagTask implements Runnable{

	public IoSession session;
	String conf;

	public XGTagTask(IoSession ss, String str) {
		session = ss;
		conf = str;
	}

	@Override
	public void run() {
		XG.inst.procTag(session,conf);
	}

}
