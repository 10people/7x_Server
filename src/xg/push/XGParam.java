package xg.push;

import java.util.HashMap;
import java.util.Map;

import com.tencent.xinge.XingeApp;

/**
 * 不同渠道，有不同的 accessId 和 secretKey
 * @author 康建虎
 *
 */
public class XGParam {
	public static Map<String, XGParam> channels = new HashMap<String, XGParam>();
	static{
//		channels.put("PP", 		new XGParam(2200136542L, "7e41bbe028ada1c9f27edc221eba7109", "PP"));
//		channels.put("XY", 		new XGParam(2200136543L, "b10dee6ce3a965070eef8813a3e6056a", "XY"));
//		//channels.put("TongBu",new XGParam(2200136544L, "16291de59e31041281a01984df0c84e2", "TongBu"));
//		channels.put("TongBu", 	new XGParam(2200142040L, "03fa6c4bee7fa2c2ae44407e1ead5437", "TongBu"));// 2015年8月18日13:25:41
//		channels.put("AiSi", 	new XGParam(2200136545L, "bcd13334503c9b95f0c7cce325c87578", "AiSi"));
//		channels.put("KuaiYong",new XGParam(2200136546L, "c85625deb9c4e5105995eb09c52f1718", "KuaiYong"));
//		channels.put("HaiMa",	new XGParam(2200152420L, "6d2b41225cfb75e0d9daab9096b137d2", "HaiMa"));
//		channels.put("IApple",	new XGParam(2200152563L, "4ce0f696dde3209031b88cb03237a432", "IApple"));
//		channels.put("iTools",	new XGParam(2200152649L, "0bf74ffb49795e73f537c2aef49bfbd7", "ITools"));
//		//
//		channels.get("PP").env = XingeApp.IOSENV_DEV;//见bug http://bugfree.youxigu.net:81/index.php/bug/27926
		
		channels.put("TX", 		new XGParam(2100172721L, "9ddc56da653412ec346f0aaa8b8aea52", "TX"));
	}
	public String channel;
	public long accessId;// = 2200135874L;
	public String secretKey;// = "c3195489c342ab1523f0e65454b0be68";
	public int env = XingeApp.IOSENV_PROD;
	public XGParam(long id, String key, String ch){
		accessId = id;
		secretKey = key;
		channel = ch;
	}
}
