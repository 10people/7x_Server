import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;


public class TestM {
	public static void main(String[] args) throws Exception{
//		int id = 100000;
		int id = 101152;
		while(true){
			go(id);
			id++;
		}
	}
	public static void go(int id) throws Exception{
		String url = "http://p.3.cn/prices/get?skuid=J_"+id;
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(url);
		int r = client.executeMethod(get);
		String ret = get.getResponseBodyAsString();
		if(ret.contains("-1")==false)
			System.out.println(ret.trim());
	}
}
