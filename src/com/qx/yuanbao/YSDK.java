package com.qx.yuanbao;

import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qq.open.OpenApiV3;
import com.qq.open.OpensnsException;

import net.sf.json.JSONObject;


/**
 * 应用宝
 * 
[手Q] AppId：1105332189
Appkey：XDLyqqD3yYHNz9r4

[微信] AppId：wx2494776da74da632
Appkey：203eba963304272bb9ffa8dcb9dda268

米大师  http://midas.qq.com/jr  3286074930  youxiguwushuang QQ卡
http://wiki.open.qq.com/wiki/%E8%85%BE%E8%AE%AF%E7%A7%BB%E5%8A%A8%E6%94%AF%E4%BB%98%E6%8F%92%E4%BB%B6Midas%E7%99%BD%E7%9A%AE%E4%B9%A6

QQ卡卡号:113625146   QQ卡密码:733640186479 QQ卡面值:100 Q币

QQ卡卡号:113625368QQ卡密码:802790052754QQ卡面值:100 Q币

QQ卡卡号:113625387QQ卡密码:549703531102QQ卡面值:100 Q币

QQ卡卡号:113625401QQ卡密码:883820147870QQ卡面值:100 Q币
 * @author 康建虎
 *
 */
public class YSDK {
	// 应用基本信息
	public static String appid_qq = "1105332189";
	public static String appkey_qq = "XDLyqqD3yYHNz9r4";
	public static String appkey_pay = "kISosZBMXjentmMNfZGYKp1332zwQzK4";

//	public static String appid_wx =  "1104965145";
//	public static String appkey_wx = "r2krevcrdBtBsyga";
	public static String appid_wx =  "wx2494776da74da632";
	public static String appkey_wx = "203eba963304272bb9ffa8dcb9dda268";
	
	// 用户的OpenID/OpenKey
//	public static String openid = "0EF80D52AE52324D51958FE6EDC3DBF3";
//	public static String openkey = "5FCBE90E15DF85CC093E3267481962E2";
	
	// OpenAPI的服务器IP 
	// 最新的API服务器地址请参考wiki文档: http://wiki.open.qq.com/wiki/API3.0%E6%96%87%E6%A1%A3
	//正式环境下使用域名：openapi.tencentyun.com。测试环境下使用IP：119.147.19.43。
	public static String serverName = "ysdktest.qq.com";
//	public static String serverName = "ysdk.qq.com";
//	public static String serverName = "119.147.19.43";

	public static Logger log = LoggerFactory.getLogger(YSDK.class.getSimpleName());
	
	public static void main(String[] args) throws Exception{
		JSONObject ret = new JSONObject();
//		ret.put("type", 2);//1wx, 2qq
//		ret.put("openid", "CA347694E09DD543B4F3E3A5CD2A7176");
//		ret.put("openkey", "034590AB5CA08D9C28476359774B0751");
		ret.put("type", 2);//1wx, 2qq
		ret.put("openid", "A82D933835329D3615F26D33B598B784");
		ret.put("openkey", "19E536B92A539AA9B23E188DD2FEA1D6");
//		new YSDK().checkLogin(ret);
		
		//{"openkey":"19E536B92A539AA9B23E188DD2FEA1D6",
		//"pfkey":"fbc548661a9f2d37c0af01f2792690a5","pay_token":"D7A282052BCC2910331F431891AEE828",
		//"openid":"A82D933835329D3615F26D33B598B784","pf":"myapp_m_qq-00000000-android-00000000-ysdk","type":"2"}
		ret.put("pay_token", "31E4FDB6700DB8DAE5A17FDD75FDC516");
//		ret.put("pf", "desktop_m_qq-10000144-android-2002-");
		ret.put("pf", "myapp_m_qq-00000000-android-00000000-ysdk");
		ret.put("pfkey", "dbbe953701b2f5ec13d1d0fe4c63e5e9");
		ret.put("zoneid", "1");
		new YSDK().get_balance_m(ret);
		
//		ret.put("amt", "1");
//		new YSDK().pay_m(ret);
		
//		ret.put("amt", "1");
//		ret.put("billno", "644625604");
//		new YSDK().cancel_pay_m(ret);

//		ret.put("discountid", "UM160517181451967");
//		ret.put("giftid", "565093963PID201605171814519872");
//		ret.put("presenttimes", "5000");
//		new YSDK().present_m(ret);
//		HttpClient hc = new HttpClient();
//		String uri = "http://ysdktest.qq.com/auth/qq_check_token?openkey=034590AB5CA08D9C28476359774B0751&sig=4593a42b8b6c240886a8dfae690aac20&openid=CA347694E09DD543B4F3E3A5CD2A7176&appid=1105332189&timestamp=1463542462";
////		String uri = "http://baidu.com";
//		GetMethod m = new GetMethod(uri);
//		hc.executeMethod(m);
//		String str = m.getResponseBodyAsString();
//		System.out.println(str);
	}
	
	public void checkLogin(JSONObject ret) throws Exception{
//		serverName = "openapi.tencentyun.com";// real server.
//		serverName = "119.147.19.43";
//		serverName = "msdktest.qq.com";
//		serverName = "msdk.qq.com";
//		serverName = "opensdktest.tencent.com";
		
		String openid = ret.optString("openid");// "0EF80D52AE52324D51958FE6EDC3DBF3";
		String openkey = ret.optString("openkey");//"5FCBE90E15DF85CC093E3267481962E2";
		
		int type = ret.optInt("type");
		String appid = null;
		String appkey = null;
		switch(type){
		case 1:
			appid = appid_wx;
			appkey = appkey_wx;
			break;
		case 2:
			appid = appid_qq;
			appkey = appkey_qq;
			break;
		default:
			log.error("unknow type {}, for {}", type, ret);
			ret.put("msg","unknow type "+type);
			return;
		}
		
		log.info("check {}", ret);
        
        OpenApiV3 sdk = new OpenApiV3(appid, appkey);
        sdk.setServerName(serverName);
        
        // MSDK接口使用的URL查询串
        HashMap<String, String> qs = buildBaseParam(openid, openkey, appid, appkey);
        
        //log.info("===========verify_login()===========");
        String txRet = "{}";
        if(type==1){//WX
        	txRet = wx_check_token(sdk, qs);
        }else{//QQ
        	txRet = qq_check_token(sdk, qs, appid, appkey, openid, openkey);
        }
        //qqprofile(sdk, qs, appid, openid, openkey);
        JSONObject txJo = JSONObject.fromObject(txRet);
        ret.put("txRet", txJo);
        ///
        int state = txJo.optInt("ret", -1);
		if(state == 0){
			ret.put("code", 1);
			ret.put("msg", "登录成功");
			ret.put("chAcc", "TX:"+openid);
		}else{
			ret.put("code", -2);//
			ret.put("msg", "登录失败");
		}
		log.info("final ret {}",ret);
	}

	public HashMap<String, String> buildBaseParam(String openid, String openkey, String appid, String appkey) {
		HashMap<String,String> qs = new HashMap<String, String>();
        String ts = Long.toString(System.currentTimeMillis() / 1000);
        qs.put("appid", appid);
        qs.put("openid", openid);
        qs.put("openkey", openkey);
        qs.put("timestamp", ts);
        qs.put("sig", DigestUtils.md5Hex(appkey + ts));
		return qs;
	}
	//////
	public static String wx_check_token(OpenApiV3 sdk, HashMap<String,String> qs){
		// 指定OpenApi Cgi名字 
        String scriptName = "/auth/wx_check_token";

        // 指定HTTP请求协议类型
        String protocol = "http";

        // 填充URL请求参数
        HashMap<String,String> params = new HashMap<String, String>();
        /*
        params.put("appid", appid);
        params.put("openid", openid);
        params.put("accessToken", openkey);
        */
          
        try
        {
        	sdk.get=true;
            String resp = sdk.api_msdk(scriptName, qs, params, protocol);
            log.info(resp);
            return resp;
        }
        catch (OpensnsException e)
        {
            log.error("Request Failed. code:{}, msg:{}\n", e.getErrorCode(), e);
            e.printStackTrace();
        }
        return "{}";
	}
	//////
	/**
     * 调用verify_login()接口
     *
     */
    public static String qq_check_token(OpenApiV3 sdk, HashMap<String,String> qs, String appid, String appkey, String openid, String openkey)
    {
        // 指定OpenApi Cgi名字 
        String scriptName = "/auth/qq_check_token";

        // 指定HTTP请求协议类型
        String protocol = "http";

        // 填充URL请求参数
        HashMap<String,String> params = new HashMap<String, String>();
//        params.put("appid", appid);
//        params.put("openid", openid);
//        params.put("openkey", openkey);
//        params.put("userip", "");
          
        try
        {
        	sdk.get=true;
            String resp = sdk.api_msdk(scriptName, qs, params, protocol);
            log.info(resp);
            return resp;
        }
        catch (OpensnsException e)
        {
            log.error("Request Failed. code:{}, msg:{}\n", e.getErrorCode(), e);
            e.printStackTrace();
        }
        return "{}";
    }
    
    public void present_m (JSONObject ret){
    	HashMap<String, String> ck = buildCkByType(ret);
    	if(ret.optString("appid") == null){
    		return;
    	}
    	OpenApiV3 sdk = new OpenApiV3(ret.optString("appid"), appkey_pay);
    	HashMap<String,String> params = new HashMap<String, String>();
    	buildPayParam(ret, sdk, params);
        params.put("discountid", ret.optString("discountid"));
        params.put("giftid", ret.optString("giftid"));
        params.put("presenttimes", ret.optString("presenttimes"));
//        ts = "1463563288";
//        ts = "1463563346436";
    	try
    	{
    		String scriptName = "/v3/r/mpay/present_m";
			String resp = present_m(scriptName, sdk, ck, params);
			log.info("{} ret {}",scriptName,resp);
            ret.put("ysdkRet", resp);
    		return ;
    	}
    	catch (Exception e)
    	{
    		log.error("Request Failed. msg:{}\n", e);
    		e.printStackTrace();
    	}
    	return ;
    }
    public void get_balance_m (JSONObject ret){
    	HashMap<String, String> ck = buildCkByType(ret);
    	if(ret.optString("appid") == null){
    		return;
    	}
    	OpenApiV3 sdk = new OpenApiV3(ret.optString("appid"), appkey_pay);
    	HashMap<String,String> params = new HashMap<String, String>();
    	buildPayParam(ret, sdk, params);
//        ts = "1463563288";
//        ts = "1463563346436";
        try
        {
        	sdk.get = true;
        	String scriptName = "/v3/r/mpay/get_balance_m";
            String resp = pay_m(scriptName,sdk, ck, params);
            // sdk.api_pay("/mpay/get_balance_m", ck, qs,"https");
            log.info(resp);
            ret.put("ysdkRet", resp);
            return ;
        }
        catch (Exception e)
        {
            log.error("Request Failed. msg:{}\n", e);
            e.printStackTrace();
        }
        return ;
    }
        
    public static String present_m(String scriptName,OpenApiV3 sdk, HashMap<String,String> cookie, HashMap<String, String> params)
    {
        // 指定OpenApi Cgi名字 
        //String scriptName = "/v3/r/mpay/present_m";
        
        // 指定HTTP请求协议类型
        String protocol = "https";
        
        // 添加cookie参数
        cookie.remove("org_loc");
        cookie.put("org_loc", URLEncoder.encode(scriptName));
        
        try
        {
            String resp = sdk.api_pay(scriptName, cookie, params, protocol);
            return resp;
        }
        catch (OpensnsException e)
        {
            log.error("Request Failed. code:%d, msg:%s\n", e.getErrorCode(), e.getMessage());
            log.error("{}",e);;
        }
        return "{}";
    }
    public void cancel_pay_m (JSONObject ret){
    	HashMap<String, String> ck = buildCkByType(ret);
    	if(ret.optString("appid") == null){
    		return;
    	}
    	OpenApiV3 sdk = new OpenApiV3(ret.optString("appid"), appkey_pay);
    	HashMap<String,String> params = new HashMap<String, String>();
    	buildPayParam(ret, sdk, params);
    	params.put("amt", ret.optString("amt"));
    	params.put("billno", ret.optString("billno"));
    	
    	// ySDK接口使用的URL查询串
    	try
    	{
    		String scriptName = "/v3/r/mpay/cancel_pay_m";
    		pay_m(scriptName,sdk, ck, params);
    		// sdk.api_pay("/mpay/get_balance_m", ck, qs,"https");
    		//log.info(resp);
    		return ;
    	}
    	catch (Exception e)
    	{
    		log.error("Request Failed. msg:{}\n", e);
    		e.printStackTrace();
    	}
    	return ;
    }
    public void pay_m (JSONObject ret){
    	HashMap<String, String> ck = buildCkByType(ret);
    	if(ret.optString("appid") == null){
    		return;
    	}
    	OpenApiV3 sdk = new OpenApiV3(ret.optString("appid"), appkey_pay);
    	HashMap<String,String> params = new HashMap<String, String>();
    	buildPayParam(ret, sdk, params);
    	params.put("amt", ret.optString("amt"));
    	
    	// ySDK接口使用的URL查询串
    	try
    	{
	        String scriptName = "/v3/r/mpay/pay_m";
			String resp = pay_m(scriptName,sdk, ck, params);
			log.info(resp);
            ret.put("ysdkRet", resp);
    		return ;
    	}
    	catch (Exception e)
    	{
    		log.error("Request Failed. msg:{}\n", e);
    		e.printStackTrace();
    	}
    	return ;
    }

	public HashMap<String, String> buildCkByType(JSONObject ret) {
		HashMap<String, String> ck = new HashMap<>();
    	
    	int type = ret.optInt("type");
    	switch(type){
    	case 1:
    		ret.put("appid",appid_qq);//支付要用QQ的app id
    		//appkey = appkey_wx;
    		ck.put("session_id", "hy_gameid");
    		ck.put("session_type", "wc_actoken");
    		break;
    	case 2:
    		ret.put("appid",appid_qq);
//    		appkey = appkey_qq;
    		ck.put("session_id", "openid");
    		ck.put("session_type", "kp_actoken");
    		break;
    	default:
    		log.error("unknow type {}, for {}", type, ret);
    		ret.put("msg","unknow type "+type);
    		break;
    	}
		return ck;
	}

	public void buildPayParam(JSONObject ret, OpenApiV3 sdk, HashMap<String, String> params) {
		sdk.setServerName(serverName);
    	sdk.get = true;
    	// 填充URL请求参数
    	params.put("appid", ret.optString("appid"));
    	params.put("openid", ret.optString("openid"));
    	params.put("openkey", ret.optString("openkey"));
    	params.put("pay_token", ret.optString("pay_token"));
    	params.put("ts", ""+System.currentTimeMillis()/1000);
    	params.put("pf", ret.optString("pf"));
    	params.put("pfkey", ret.optString("pfkey"));
    	params.put("zoneid", ret.optString("zoneid"));
	}
    public static String pay_m(String scriptName,OpenApiV3 sdk, HashMap<String,String> cookie, HashMap<String, String> params)
    {
        // 指定OpenApi Cgi名字 
       // String scriptName = "/v3/r/mpay/pay_m";
        
        // 指定HTTP请求协议类型
        String protocol = "https";
        
        // 添加cookie参数
        cookie.remove("org_loc");
        cookie.put("org_loc", URLEncoder.encode(scriptName));
        
        try
        {
            String resp = sdk.api_pay(scriptName, cookie, params, protocol);
            return resp;
        }
        catch (OpensnsException e)
        {
            log.error("Request Failed. code:%d, msg:%s\n", e.getErrorCode(), e.getMessage());
            e.printStackTrace();
        }
        return "{}";
    }
}
