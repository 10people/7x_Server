package pct;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;

public class TestBase {
	public void handle(int id,IoSession session, Builder builder){}
}
