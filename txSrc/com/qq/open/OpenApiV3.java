package com.qq.open;

import java.util.*;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.*;

import com.qq.open.SnsNetwork;
import com.qq.open.SnsSigCheck;
import com.qq.open.SnsStat;
import com.qx.yuanbao.TXQueryMgr;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.json.JSONException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.methods.multipart.FilePart;


 /**
 * 提供访问腾讯开放平台 OpenApiV3 的接口
 *
 * @version 3.0.2
 * @since jdk1.5
 * @author open.qq.com
 * @copyright © 2012, Tencent Corporation. All rights reserved.
 * @History:
 *				 3.0.3 | coolinchen| 2012-11-07 11:20:12 | support POST request in  "multipart/form-data" format
 *               3.0.2 | coolinchen| 2012-10-08 11:20:12 | support printing request string and result
 *				 3.0.1 | nemozhang | 2012-08-28 16:40:20 | support cpay callback sig verifictaion
 *               3.0.0 | nemozhang | 2012-03-21 12:01:05 | initialization
 *
 */
 
public class OpenApiV3
{
	public static Logger log = TXQueryMgr.log;

    /**
     * 构造函数
     *
     * @param appid 应用的ID
     * @param appkey 应用的密钥
     */
    public OpenApiV3(String appid, String appkey)
    {
        this.appid = appid;
        this.appkey = appkey;

    }
    
    /**
     * 设置OpenApi服务器的地址
     *
     * @param serverName OpenApi服务器的地址
     */
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }


    /**
     * 执行API调用
     * 
     * @param scriptName OpenApi CGI名字 ,如/v3/user/get_info
     * @param params OpenApi的参数列表
     * @param protocol HTTP请求协议 "http" / "https"
     * @return 返回服务器响应内容
     */
    public String api_pay(String scriptName, HashMap<String, String> cookies, HashMap<String, String> params, String protocol) throws OpensnsException
    {
        // 检查openid openkey等参数
        if (params.get("openid") == null)
        {
            throw new OpensnsException(ErrorCode.PARAMETER_EMPTY, "openid is empty");
        }

        // 无需传sig,会自动生成
        params.remove("sig");

        // 添加固定参数
        params.put("appid", this.appid);

        // 请求方法
        String method = "get";
        
        // 签名密钥
        String secret = this.appkey + "&";
        
        // 计算签名
        String sig = SnsSigCheck.makeSig(method, scriptName, params, secret);
        
        params.put("sig", sig);

        StringBuilder sb = new StringBuilder(64);
        sb.append(protocol).append("://").append(this.serverName).append(scriptName);
        String url = sb.toString(); 
        
        String qs = null;
        try 
        {
			qs= mkQueryString(params);
		} 
        catch (UnsupportedEncodingException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        url += "?";
        url += qs;
        
        // cookie
        // cookies = null;

        long startTime = System.currentTimeMillis();
		
		//通过调用以下方法，可以打印出最终发送到openapi服务器的请求参数以及url，默认注释
        log.info("==========Request Info==========\n");
        log.info("method:  " + method);
		log.info("url:  " + url);
		printRequest(url,method,params);
		printRequest(url,method,cookies);
		
        // 发送请求
        String resp = SnsNetwork.getRequest(url, cookies, protocol);

        // 解码JSON
        JSONObject jo = null;
        try 
        {
            jo = new JSONObject(resp);
        } 
        catch (JSONException e) 
        {
            throw new OpensnsException(ErrorCode.RESPONSE_DATA_INVALID, e); 
        } 

        // 检测ret值
        int rc = jo.optInt("ret", 0);

		//通过调用以下方法，可以打印出调用openapi请求的返回码以及错误信息，默认注释
		printRespond(resp);
		
        return resp;
    }
    public boolean get=false;
    // cookie
    public HashMap<String, String> cookies = null;
    public String api_msdk(String scriptName, HashMap<String, String> qs, HashMap<String, String> params, String protocol) throws OpensnsException
    {
        // 检查openid openkey等参数
        if (params.get("openid") == null)
        {
//            throw new OpensnsException(ErrorCode.PARAMETER_EMPTY, "openid is empty");
        }

        // 添加固定参数
        // params.put("appid", this.appid);
        // qs.put("appid", this.appid);
        
        // 请求方法
        String method = "post";
        if(get){
        	method = "get";
        }
       
                
        String params_json = new JSONObject(params).toString();
        
        StringBuilder sb = new StringBuilder(64);
        sb.append(protocol).append("://").append(this.serverName).append(scriptName);
        String url = sb.toString(); 
        
        String _qs = null;
        try {
        	_qs = OpenApiV3.mkQueryString(qs);
        	url += "?";
        	url += _qs;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         

        long startTime = System.currentTimeMillis();
		
		//通过调用以下方法，可以打印出最终发送到openapi服务器的请求参数以及url，默认注释
        log.info("==========Request Info==========\n");
        log.info("method:  " + method);
        log.info("url:  " + url);
        printRequest(url,method,params);
        // 发送请求
        String resp = get ? SnsNetwork.getRequest(url, cookies, protocol)
        		: SnsNetwork.postRequest(url, params_json, cookies, protocol);

        // 解码JSON
        JSONObject jo = null;
        try 
        {
            jo = new JSONObject(resp);
        } 
        catch (JSONException e) 
        {
            throw new OpensnsException(ErrorCode.RESPONSE_DATA_INVALID, e); 
        } 

        // 检测ret值
        int rc = jo.optInt("ret", 0);
		
		//通过调用以下方法，可以打印出调用openapi请求的返回码以及错误信息，默认注释
		printRespond(resp);
		
        return resp;
    }
	
	/**
     * 辅助函数，打印出完整的请求串内容
     * 
     * @param url 请求cgi的url
     * @param method 请求的方式 get/post
     * @param params OpenApi的参数列表
     */
	private void printRequest(String url,String method,HashMap<String, String> params) throws OpensnsException
	{
		log.info("{}",params);
	}
	
	
	public static String mkQueryString(HashMap<String, String> params) throws UnsupportedEncodingException 
	{
		StringBuilder buffer = new StringBuilder(128);
		Iterator iter = params.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next(); 
			buffer.append(URLEncoder.encode((String)entry.getKey(), "UTF-8").replace("+", "%20").replace("*", "%2A")).append("=").append(URLEncoder.encode((String)entry.getValue(), "UTF-8").replace("+", "%20").replace("*", "%2A")).append("&");
		}
		String tmp = buffer.toString();
		tmp = tmp.substring(0,tmp.length()-1);
		return tmp;
	}
	
	
	
	
	/**
     * 辅助函数，打印出完整的执行的返回信息
     * 
     * @return 返回服务器响应内容
     */
	private void printRespond(String resp)
	{
		log.info("===========Respond Info============");
		log.info(resp);
	}
	
    /**
     * 验证openid是否合法
     */
    private boolean isOpenid(String openid)
    {
        return (openid.length()==32) && openid.matches("^[0-9A-Fa-f]+$");
    }
    
    private String appid;;
    private String appkey;
    private String serverName;
}
