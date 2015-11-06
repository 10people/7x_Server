package log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLog {
	public static Logger log = LoggerFactory.getLogger(TestLog.class);
	
	public static void main(String[] args) {
//	    OurLog.log.GameSvrState();
//	    OurLog.log.PlayerOnline();
	    
		log.info("over {},{2}",11,12);
	}
}
