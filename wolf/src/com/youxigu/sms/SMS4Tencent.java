package com.youxigu.sms;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsplatform.CSmsPlatformApi;

import com.youxigu.boot.Config;

public class SMS4Tencent {
	private static  String strDnsMasterIP="172.16.61.73";//主dns服务器
	private static  String strDnsSlaverIP="172.23.20.48";//备dns服务器
	private static  String strDefaultIp="172.27.27.21";//服务器默认ip
	private static  int iDefaultPort=4444;//服务器默认端口
	private static  String strDomain="FreeServer_test";//服务域名
	private static  String strType="CGI";//企业端，全部使用cgi
	private static  String byteCode = "GB2312";//默认编码
	private static  int time = 5000;//超时时间,单位:毫秒
//	private static  String  reg = "1[0-9]{10}";//手机号是11位，且第一位开头是1
	
	private static String AppClass = "TENCENT";
	private static String AppID = "NORMAL";
	private static String AppSubID = "-QXZBDX";
	
	private static Logger logger = LoggerFactory.getLogger("smsplatform");
	
	/**
	 * 开关  0,关;1,可以init;2,可以connect;3,可以发短信
	 */
	public static int sms_switch = 0;
	
	public static CSmsPlatformApi instance = new CSmsPlatformApi();
	
	public SMS4Tencent(){
		try {
			strDnsMasterIP = Config.get("dnsMasterIP");
			strDnsSlaverIP = Config.get("dnsSlaverIP");
			strDefaultIp = Config.get("defaultIp");
			iDefaultPort = Integer.valueOf(Config.get("defaultPort"));
			strDomain = Config.get("strDomain");

			AppSubID = Config.get("appSubID");
		
			logger.info("SMS4Tencent:init初始化开始");
			int ret=instance.init(strType);
			if(ret!=0)
			{
				String str = "";
				try {
					str = new String(instance.getError(strType).getBytes(byteCode));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("SMS4Tencent:init iRet="+ret+" "+str);
				instance.close(strType);//init失败，进行关闭
				logger.info("SMS4Tencent:init初始化失败");
				
			} else {
				logger.info("SMS4Tencent:init初始化成功");
			}
			
			logger.info("SMS4Tencent:connect连接开始");
			ret=instance.connect(strType,strDnsMasterIP,strDnsSlaverIP,strDefaultIp,iDefaultPort,strDomain,time);
			if(ret!=0) {
				
				String str = "";
				try {
					str = new String(instance.getError(strType).getBytes(byteCode));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("SMS4Tencent:connect iRet="+ret+" "+str);
				instance.close(strType);//connect失败，进行关闭
				logger.info("SMS4Tencent:connect连接失败");
				
			} else {
				logger.info("SMS4Tencent:connect连接成功");
			}
			
			sms_switch = 3;//设置成可以发短信
			
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run () {
					// close mobile message
					logger.info("SMS4Tencent:close开始关闭");
					instance.close(strType);
					sms_switch = 0;//开关状态设置成0
					logger.info("SMS4Tencent:close关闭成功");
				}
			}));
		
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 发送短信
	 * @param moblieId 手机号,必须是11位的
	 * @param message 短信内容
	 * @throws UnsupportedEncodingException 
	 */
	public static int sendsms(String mobileId, String message) throws UnsupportedEncodingException {
		
//		if (sms_switch!=3) {//不能发短信，没有成功init和connect
//			return 3000;
//		}
		
//		if (!mobileId.matches(reg)) {
//			throw new ManuAppException(Messages.getString("手机号码只能是11位数字且第一位是1"));
//		}
		smsplatform.tagSmsMsg sms=new smsplatform.tagSmsMsg();
		
		sms.sSrcNo = new String("1065902181822").toCharArray();
		
		sms.sMobile =new String(mobileId).toCharArray();
		
		String strC = new String(message);
		byte bySms[] = strC.getBytes(byteCode);
		for (int j = 0; j < bySms.length; j++)
		{
			char c = (char)bySms[j];
			sms.sMessage[j] = c;
		}
		
//		logger.info("SMS4Tencent:sms content len:"+ sms.sMessage.length + "  手机号:"+mobileId);
		sms.sAppClass = new String(AppClass).toCharArray();
		sms.sAppID = new String(AppID).toCharArray();
		sms.sAppSubID = new String(AppSubID).toCharArray();
		
//		logger.info("SMS4Tencent:sendsms 手机号:"+mobileId);
		int ret = instance.sendsms(strType, sms);
		if(ret!=0)
		{
			String str=new String(instance.getError(strType).getBytes(byteCode));
			logger.info("SMS4Tencent:sendsms 发送短信出现异常 iRet="+ret+" "+str + "  手机号:"+mobileId);
		}
		
		return ret;
	}
}
