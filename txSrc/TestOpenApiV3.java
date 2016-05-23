import java.net.URLEncoder;
import java.util.*;

import com.qq.open.OpenApiV3;
import com.qq.open.OpensnsException;

import org.apache.commons.codec.digest.DigestUtils; 




/**
 * OpenAPI V3 SDK 示例代码
 *
 * @version 3.0.0
 * @since jdk1.5
 * @author open.qq.com
 * @copyright © 2012, Tencent Corporation. All rights reserved.
 * @History:
 * 				 1.0.0 | applebian | 2015-04-19 19:58:03 | updated for mobile game msdk
 *               3.0.0 | nemozhang | 2012-03-21 12:01:05 | initialization
 *
 */
 
public class TestOpenApiV3
{
    public static void main(String args[])
    {
        // 应用基本信息
        String appid = "100703379";
        String appkey = "4578e54fb3a1bd18e0681bc1c734514e";

        // 用户的OpenID/OpenKey
        String openid = "0EF80D52AE52324D51958FE6EDC3DBF3";
        String openkey = "5FCBE90E15DF85CC093E3267481962E2";

        // OpenAPI的服务器IP 
        // 最新的API服务器地址请参考wiki文档: http://wiki.open.qq.com/wiki/API3.0%E6%96%87%E6%A1%A3 
        String serverName = "msdktest.qq.com";
        
        OpenApiV3 sdk = new OpenApiV3(appid, appkey);
        sdk.setServerName(serverName);
        
        // MSDK接口使用的URL查询串
        HashMap<String,String> qs = new HashMap<String, String>();
        String ts = Long.toString(System.currentTimeMillis() / 1000);
        qs.put("appid", appid);
        qs.put("timestamp", ts);
        qs.put("sig", DigestUtils.md5Hex(appkey + ts));
        qs.put("encode", "1");
        qs.put("openid", openid);
        
        //System.out.println("===========verify_login()===========");
        //verify_login(sdk, qs, appid, appkey, openid, openkey);
        //qqprofile(sdk, qs, appid, openid, openkey);
        
        
        
        // 支付接口使用的cookie参数
        HashMap<String,String> cookie = new HashMap<String, String>();
        cookie.put("session_id", "hy_gameid");
        cookie.put("session_type", "st_dummy");
        cookie.put("org_loc", "");
        
        
        String pay_token = "D0E24D58E0901FDEC084BB208B12479C";
    
        String pf = "wechat_qq-73213123-android-73213123";
        String pfkey = "36fd747db1e14f773c74b5aa29c1d30f";
        String zoneid = "1";
        //get_balance_m(sdk, cookie, appid, appkey, openid, openkey, pay_token, ts, pf, pfkey, zoneid);
        
        
        String amt = "100";
        //pay_m(sdk, cookie, appid, appkey, openid, openkey, pay_token, ts, pf, pfkey, zoneid, amt);
        
        
        String discountid = "";
        String giftid = "";
        String presenttimes = "50";
        //present_m(sdk, cookie, appid, appkey, openid, openkey, pay_token, ts, pf, pfkey, zoneid, discountid, giftid, presenttimes);
    }

    /**
     * 调用verify_login()接口
     *
     */
    public static void verify_login(OpenApiV3 sdk, HashMap<String,String> qs, String appid, String appkey, String openid, String openkey)
    {
        // 指定OpenApi Cgi名字 
        String scriptName = "/auth/verify_login";

        // 指定HTTP请求协议类型
        String protocol = "http";

        // 填充URL请求参数
        HashMap<String,String> params = new HashMap<String, String>();
        params.put("appid", appid);
        params.put("openid", openid);
        params.put("openkey", openkey);
        params.put("userip", "");
          
        try
        {
            String resp = sdk.api_msdk(scriptName, qs, params, protocol);
            System.out.println(resp);
        }
        catch (OpensnsException e)
        {
            System.out.printf("Request Failed. code:%d, msg:%s\n", e.getErrorCode(), e.getMessage());
            e.printStackTrace();
        }
    }
    

    /**
     * 调用qqprofile()接口
     *
     */
    public static void qqprofile(OpenApiV3 sdk, HashMap<String,String> qs, String appid, String openid, String openkey)
    {
        // 指定OpenApi Cgi名字 
        String scriptName = "/relation/qqprofile";

        // 指定HTTP请求协议类型
        String protocol = "http";

        // 填充URL请求参数
        HashMap<String,String> params = new HashMap<String, String>();
        params.put("appid", appid);
        params.put("openid", openid);
        params.put("accessToken", openkey);
          
        try
        {
            String resp = sdk.api_msdk(scriptName, qs, params, protocol);
            System.out.println(resp);
        }
        catch (OpensnsException e)
        {
            System.out.printf("Request Failed. code:%d, msg:%s\n", e.getErrorCode(), e.getMessage());
            e.printStackTrace();
        }
    }
    
    

    /**
     * 调用get_balance_m()接口
     *
     */
    public static void get_balance_m(OpenApiV3 sdk, HashMap<String,String> cookie, 
    		String appid, String appkey, String openid, String openkey, String pay_token, String ts, String pf, String pfkey, String zoneid)
    {
        // 指定OpenApi Cgi名字 
        String scriptName = "/mpay/get_balance_m";
        
        // 指定HTTP请求协议类型
        String protocol = "http";
        
        // 添加cookie参数
        cookie.remove("org_loc");
        cookie.put("org_loc", URLEncoder.encode(scriptName));
        
        // 填充URL请求参数
        HashMap<String,String> params = new HashMap<String, String>();
        params.put("appid", appid);
        params.put("openid", openid);
        params.put("openkey", openkey);
        params.put("pay_token", pay_token);
        params.put("ts", ts);
        params.put("pf", pf);
        params.put("pfkey", pfkey);
        params.put("zoneid", zoneid);
        
        try
        {
            String resp = sdk.api_pay(scriptName, cookie, params, protocol);
            System.out.println(resp);
        }
        catch (OpensnsException e)
        {
            System.out.printf("Request Failed. code:%d, msg:%s\n", e.getErrorCode(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 调用pay_m()接口
     *
     */
    public static void pay_m(OpenApiV3 sdk, HashMap<String,String> cookie, 
    		String appid, String appkey, String openid, String openkey, String pay_token, String ts, String pf, String pfkey, String zoneid, String amt)
    {
        // 指定OpenApi Cgi名字 
        String scriptName = "/mpay/pay_m";
        
        // 指定HTTP请求协议类型
        String protocol = "http";
        
        // 添加cookie参数
        cookie.remove("org_loc");
        cookie.put("org_loc", URLEncoder.encode(scriptName));
        
        // 填充URL请求参数
        HashMap<String,String> params = new HashMap<String, String>();
        params.put("appid", appid);
        params.put("openid", openid);
        params.put("openkey", openkey);
        params.put("pay_token", pay_token);
        params.put("ts", ts);
        params.put("pf", pf);
        params.put("pfkey", pfkey);
        params.put("zoneid", zoneid);
        params.put("amt", amt);
        
        
        try
        {
            String resp = sdk.api_pay(scriptName, cookie, params, protocol);
            System.out.println(resp);
        }
        catch (OpensnsException e)
        {
            System.out.printf("Request Failed. code:%d, msg:%s\n", e.getErrorCode(), e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 调用present_m()接口
     *
     */
    public static void present_m(OpenApiV3 sdk, HashMap<String,String> cookie, 
    		String appid, String appkey, String openid, String openkey, String pay_token, String ts, String pf, String pfkey, String zoneid, String discountid, String giftid, String presenttimes)
    {
        // 指定OpenApi Cgi名字 
        String scriptName = "/mpay/present_m";
        
        // 指定HTTP请求协议类型
        String protocol = "http";
        
        // 添加cookie参数
        cookie.remove("org_loc");
        cookie.put("org_loc", URLEncoder.encode(scriptName));
        
        // 填充URL请求参数
        HashMap<String,String> params = new HashMap<String, String>();
        params.put("appid", appid);
        params.put("openid", openid);
        params.put("openkey", openkey);
        params.put("pay_token", pay_token);
        params.put("ts", ts);
        params.put("pf", pf);
        params.put("pfkey", pfkey);
        params.put("zoneid", zoneid);
        params.put("discountid", discountid);
        params.put("giftid", giftid);
        params.put("presenttimes", presenttimes);
        
        try
        {
            String resp = sdk.api_pay(scriptName, cookie, params, protocol);
            System.out.println(resp);
        }
        catch (OpensnsException e)
        {
            System.out.printf("Request Failed. code:%d, msg:%s\n", e.getErrorCode(), e.getMessage());
            e.printStackTrace();
        }
    }
    
}
