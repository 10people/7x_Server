package log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.bag.HashBag;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.boot.GameServer;
import com.manu.network.SessionAttKey;
import com.qx.account.AccountManager;
import com.qx.http.LoginServ;
import com.qx.http.MyClient;

/**
 * 向日志服务器发送注册和登录的统计
 * @author 康建虎
 *
 */
public class Log2DBRegAndLogin implements Runnable{
	public static Logger log = LoggerFactory.getLogger("Log2DB");
	public static void main(String[] args) {
		GameServer.init();
		IoSession ss = new DummySession();
		ss.setAttribute(SessionAttKey.ACC_CHANNEL, null);
		AccountManager.sessionMap.put(1L, ss);
		/////////上面是测试条据
		new Log2DBRegAndLogin().run();
	}
	@Override
	public void run() {
		JSONObject o = sendRequest();
		if(o == null){
			log.error("router未返回数据,null");
			return;
		}
		Map<String, Integer> regMap = toMap(o.getJSONObject("reg"));
		Map<String, Integer> loginMap = toMap(o.getJSONObject("login"));
		HashBag onlineMap = calcOnlineMap();
		Iterator<Entry<String, Integer>> it = LoginServ.chName2id.entrySet().iterator();
		Date time = Calendar.getInstance().getTime();
		time.setSeconds(30);
		String dtEventTime = OurLog.log.fmt.format(time);
		while(it.hasNext()){
			Entry<String, Integer> e = it.next();
			makeChannelReport(regMap, loginMap, onlineMap, e, dtEventTime);
		}
	}
	protected void makeChannelReport(Map<String, Integer> regMap,
			Map<String, Integer> loginMap, HashBag onlineMap,
			Entry<String, Integer> e, String dtEventTime) {
		String sql = "insert into PlayerOnline values(num,reg,login,'dtEventTime',PlatID,'GameSvrId',LoginChannel)";
		sql = sql.replace("num", String.valueOf(onlineMap.getCount(e.getKey())));
		sql = sql.replace("reg", String.valueOf(getInt(regMap, e.getKey())));
		sql = sql.replace("login", String.valueOf(getInt(loginMap, e.getKey())));
		sql = sql.replace("dtEventTime", dtEventTime);
		sql = sql.replace("PlatID", String.valueOf(OurLog.log.PlatID));
		sql = sql.replace("GameSvrId", OurLog.log.GameSvrId);
		sql = sql.replace("LoginChannel", String.valueOf(e.getValue()));
		log.info("channel {} sql {}", e.getKey(), sql);
		DBHelper db = null;
		String result = "OK";
		try {
        	db = new DBHelper(sql);
        	if(db.conn == null || db.pst == null){
        		result = "数据库连接异常";
        	}else{
	        	boolean sqlRet = db.pst.execute();
	            //
	            result = "成功:"+sqlRet;
        	}
        }catch(Exception ex){
        	log.error("执行出错.",ex);
        	result="异常"+e.toString();
        } finally {
            if(db != null )db.close();
        }
		log.info("执行结果 {}", result);
	}
	protected int getInt(Map<String, Integer> onlineMap, String key) {
		Integer v = onlineMap.get(key);
		if(v == null)return 0;
		return v;
	}
	protected HashBag calcOnlineMap() {
		HashBag bag = new HashBag();
		synchronized(AccountManager.sessionMap){
			Iterator<Entry<Long, IoSession>> it = AccountManager.sessionMap.entrySet().iterator();
			while(it.hasNext()){
				Entry<Long, IoSession> e = it.next();
				String ch = (String)e.getValue().getAttribute(SessionAttKey.ACC_CHANNEL);
				if(ch == null || ch.equals("null")){
					ch = "GuangFang";
				}
				bag.add(ch);
			}
		}
		return bag;
	}
	protected Map<String, Integer> toMap(JSONObject jsonObject) {
		JSONArray data = jsonObject.getJSONArray("data");
		Map<String, Integer> map = new HashMap<String, Integer>();
		int cnt = data.size();
		for(int i=0; i<cnt; i++){
			JSONObject jo = data.getJSONObject(i);
			String ch = jo.getString("channel");
			if(ch == null || ch.equals("null")){
				map.put("GuangFang", jo.optInt("cnt", 0));
			}else{
				map.put(ch, jo.optInt("cnt", 0));
			}
		}
		return map;
	}
	public JSONObject sendRequest() {
		String page = "/qxrouter/yunying/regAndLoginStat.jsp";
		String host =  GameServer.cfg.get("loginServer");
		int port = GameServer.cfg.get("loginPort", 8090);
		{
			// 本地测试用
				//host = "203.195.230.100";
				//port = 9091;
		}
		MyClient hc = new MyClient(host, port);
		String respMesg = hc.startServerSendRequest(page, "");
		JSONObject o = null;
		try{
			o = JSONObject.fromObject(respMesg);
		}catch(Exception e){
			log.error("登录返回的字符串转换为json时出错", e);
		}
		return o;
	}
}
