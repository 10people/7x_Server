package xg.push;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import log.CunLiang;

import org.apache.mina.core.session.IoSession;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.PushInfo;
import com.manu.network.SessionAttKey;
import com.qx.account.AccountManager;
import com.qx.account.SettingsBean;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.http.LoginServ;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.tencent.xinge.TagTokenPair;
import com.tencent.xinge.XingeApp;

/**
 * Android
 * 3286074930
youxiguwushuang
 * ==================================
 * IOS
 * 新申请了个QQ 3270162390
密码1q2w3e4r
http://xg.qq.com/xg/apps/ctr_app
 * @author 康建虎
 * -------------------------------------------------2016年2月22日16:23:59 应用宝------------------------------
 张玉谷(张玉谷) 11:06:04
安卓信鸽也接一下吧
张玉谷(张玉谷) 11:06:13
http://xg.qq.com/xg/apps/ctr_app/get_app_info?app_id=2100172721
张玉谷(张玉谷) 11:06:13
信鸽：
3286074930
youxiguwushuang
张玉谷(张玉谷) 11:06:37
ACCESS ID
2100172721

ACCESS KEY
A24E38E6ANZP

应用包名
com.tencent.tmgp.qixiongwushuang

SECRET KEY
9ddc56da653412ec346f0aaa8b8aea52
 *
 */
public class XG extends EventProc{
	public static boolean pushOpen = false;//默认不开启，不然每个开着的服务器都会发推送。
	public static boolean isAndroidServer = true;
	public static String tag1TiLiGive="tagTiLiGive";//体力赠送提示
	public static String tag2TiLiFull="tagTiLiFull";//2.	体力回满提示
	public static String tag3BiaoJuYunSong="tagBiaoJuYunSong";//3.	镖局运送提醒
	public static String tag4DangPuShuaXin="tagDangPuShuaXin";//4.	商铺刷新提醒
	private Logger log = LoggerFactory.getLogger(XG.class);
	public static String title = "七雄无双";
	public static XG inst = new XG();
	
	//示例：
	//XingeApp.pushAllIos(000, "myKey", "大家好!", XingeApp.IOSENV_PROD);
	//返回值:
	//返回 push_id，即推送的任务 ID
//	{"ret_code":0, "result":{"push_id":"11121"}} //成功
//	{"ret_code":-1, "err_msg":"error description"} //失败
//	注：ret_code 为 0 表示成功，其他为失败，具体请查看附录。
	public org.json.JSONObject pushAllIos(String content){
		Iterator<XGParam> it = XGParam.channels.values().iterator();
		JSONObject chs = new JSONObject();
		while(it.hasNext()){
			XGParam param = it.next();
			org.json.JSONObject ret = pushAllIos(content, param.accessId, param.secretKey,param.env);
			chs.put(param.channel, ret);
		}
		return chs;
	}
	public org.json.JSONObject pushAllIos(String content, long accessId, String secretKey, int env){
		log.info("pushAllIos请求发送 {}",content);
		org.json.JSONObject ret = null;
		if(isAndroidServer ){
			ret = XingeApp.pushAllAndroid(accessId, secretKey, title, content);
		}else{
			ret = XingeApp.pushAllIos(accessId, secretKey, content, env);
		}
		log.info("发送结果{}",ret);
		return ret;
	}
	
	public void clientReportToken(int id, IoSession session, Builder builder){
		ErrorMessage.Builder msg = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		if(msg == null){
			log.error("消息是null");
			return;
		}
		String token = msg.getErrorDesc();
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			log.info("sid {} null junZhuId when report token {}",session.getId(),token);
			session.setAttribute(SessionAttKey.XG_TOKEN, token);//乱写的数字是为了防止巧合
			return;
		}
		saveToken(session, junZhuId, token);
	}
	public void saveToken(IoSession session,long junZhuId, String token){
		log.info("client {} report xg token {}", junZhuId, token);
		XGTokenBean bean = new XGTokenBean();
		bean.jzId = junZhuId;
		bean.token = token;
		HibernateUtil.save(bean);
		session.setAttribute(SessionAttKey.XG_TOKEN, token);
		String channel = (String)session.getAttribute(SessionAttKey.ACC_CHANNEL, null);
		log.info("jzId {} channel is {}, token {}",junZhuId,channel,token);
		if(channel == null){
			return;
		}
		XGParam xgParam = XGParam.channels.get(channel);
		if(xgParam == null){
			log.info("渠道参数没有找到{}", channel);
			return;
		}
		log.info("XG param {}, {},token {}",xgParam.accessId,xgParam.secretKey, token);
		//客户端先登录，服务器会从router取到channel，根据该取到参数，去设置tag
		SettingsBean confBean = HibernateUtil.find(SettingsBean.class, junZhuId);
		if(confBean == null || confBean.str == null || confBean.str.isEmpty()){
			XingeApp push = new XingeApp(xgParam.accessId, xgParam.secretKey);
			//客户端没有保存过设置，全部开启.
			List<TagTokenPair> pairs = new ArrayList<TagTokenPair>();
			pairs.add(new TagTokenPair(tag1TiLiGive,token));
			pairs.add(new TagTokenPair(tag2TiLiFull,token));
			pairs.add(new TagTokenPair(tag3BiaoJuYunSong,token));
			pairs.add(new TagTokenPair(tag4DangPuShuaXin,token));
			JSONObject ret = push.BatchSetTag(pairs);
			log.info("{} token {} 添加4个tag结果{}", junZhuId, token, ret);
		}else{
			//如果保存过，则在保存时处理
			log.info("do nothing {}",token);
		}
	}
	public void procTag(IoSession session, String conf){
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		String token = (String) session.getAttribute(SessionAttKey.XG_TOKEN);
		if(token == null){
				XGTokenBean bean = HibernateUtil.find(XGTokenBean.class, junZhuId);
				if(bean != null){
					token = bean.token;
					session.setAttribute(SessionAttKey.XG_TOKEN, token);
				}
		}
		String channel = (String) session.getAttribute(SessionAttKey.ACC_CHANNEL);
		if(token == null || channel == null){
			log.info("{} 参数不全，token {} channel {}",junZhuId, token, channel);
			return;
		}
		XGParam xgParam = XGParam.channels.get(channel);
		if(xgParam == null){
			log.info("procTag,渠道参数没有找到{}", channel);
			return;
		}
		XingeApp push = new XingeApp(xgParam.accessId, xgParam.secretKey);
		//{"MUSIC":"1", "AUDIO_EFFECT":"1", "POWER_GET":"0", "POWER_FULL":"0", "PAWNSHOP_FRESH":"0", "BIAOJU":"0"}
		JSONObject json = new JSONObject(conf);
		checkConf(json, "POWER_GET",push, token,tag1TiLiGive);
		/* 这三个没做。
		checkConf(json, "POWER_FULL",push, token,tag2TiLiFull);
		checkConf(json, "PAWNSHOP_FRESH",push, token,tag4DangPuShuaXin);
		checkConf(json, "BIAOJU",push, token,tag3BiaoJuYunSong);
		*/
	}
	/*
	2.1 Android 平台推送消息给单个设备
	public static JSONObject pushTokenAndroid(long accessId,String secretKey,String title,String
	content,String token)
	示例：
	XingeApp.pushTokenAndroid(000, "myKey", "标题", "大家好!", "3dc4gcd98sdc");
	返回值:
	{"ret_code":0} //成功
	{"ret_code":-1, "err_msg":"error description"} //失败
	注：ret_code 为 0 表示成功，其他为失败，具体请查看附录。
	*/
	public void checkConf(JSONObject json, String confKey, XingeApp push, String token, String tag) {
		int on = json.optInt(confKey,2);
		if(on == 2){
			addTag(confKey,push, token, tag);
		}else{
			delTag(confKey,push, token,tag);
		}
	}
	public void delTag(String confKey, XingeApp push, String token, String tag) {
		List<TagTokenPair> pairs = new ArrayList<TagTokenPair>();
		pairs.add(new TagTokenPair(tag1TiLiGive,token));
		JSONObject ret = push.BatchDelTag(pairs);
		log.info("token {} 移除tag {} 结果{}", token, tag, ret);
	}
	public void addTag(String string, XingeApp push, String token, String tag) {
		List<TagTokenPair> pairs = new ArrayList<TagTokenPair>();
		pairs.add(new TagTokenPair(tag1TiLiGive,token));
		JSONObject ret = push.BatchSetTag(pairs);
		log.info("token {} 添加tag {} 结果{}", token, tag, ret);	
	}
	/**
	 * 提醒领取体力
	 * 	<PushInfo ID="10" str="小七：午饭时间到！肚子饿了吧？主人快来领取体力吧！" />
		<PushInfo ID="20" str="小七：晚饭时间到！肚子饿了吧？主人快来领取体力吧！" />
	 * @param i
	 */
	public void pushGetTili(int i) {
		if(pushOpen == false){
			log.info("本服务器未开启push");
			return;
		}
		String tag = tag1TiLiGive;
		String info = null;
		List<PushInfo> list = TempletService.listAll(PushInfo.class.getSimpleName());
		if(list == null){
			log.error("没有配置数据");
			return;
		}
		for(PushInfo conf :list){
			if(conf.ID == i){
				info = conf.str;
				break;
			}
		}
		if(info == null){
			log.error("配置没有找到 {}", i);
			return;
		}
		Iterator<XGParam> it = XGParam.channels.values().iterator();
		while(it.hasNext()){
			XGParam param = it.next();
			JSONObject ret = null;
			if(isAndroidServer){
				ret = XingeApp.pushTagAndroid(param.accessId, param.secretKey, title, info, tag);
			}else {
				ret = XingeApp.pushTagIos(param.accessId, param.secretKey, info, tag, param.env);
			}
			log.info("{} 推送领取体力 结果:{}",param.channel, ret);
		}
	}
	@Override
	public void proc(Event param) {
		switch(param.id){
		case ED.ACC_LOGIN:
			login(param);
			break;
		case ED.BAI_ZHAN_A_WIN_B:{
			//ED.BAI_ZHAN_A_WIN_B,new Object[]{jz,otherJun}
			Object[] arr = (Object[]) param.param;
			JunZhu win = (JunZhu) arr[0];
			JunZhu lose = (JunZhu) arr[1];
			sendBaiZhanFail(win,lose);
		}
			break;
		}
	}
	public void login(Event param){
		Long junZhuId = (Long)param.param;
		if(junZhuId == null){
			log.info("null jzId");
			return;
		}
		IoSession session = AccountManager.sessionMap.get(junZhuId);
		if(session == null){
			log.info("null session");
			return;
		}
		String token = (String) session.getAttribute(SessionAttKey.XG_TOKEN);
		if(token == null){
			log.info("null token sid {}",session.getId());
			return;
		}
		log.info("do fix sid {}",session.getId());
		saveToken(session, junZhuId, token);
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);
		EventMgr.regist(ED.BAI_ZHAN_A_WIN_B, this);
	}
	
	public void sendBaiZhanFail(JunZhu win, JunZhu lose){
		if( win == null || lose == null){
			return;
		}
		final String winName = win.name;
		long loseId = lose.id;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				List<PushInfo> list = TempletService.listAll(PushInfo.class.getSimpleName());
				if(list == null){
					log.error("没有配置数据");
					return;
				}
				XGTokenBean bean = HibernateUtil.find(XGTokenBean.class, loseId);
				if(bean == null){
					log.info("null token bean for {}", loseId);
					return;
				}
				CunLiang cl = HibernateUtil.find(CunLiang.class, loseId);
				if(cl == null){
					log.info("null cunLiang for {}", loseId);
					return;
				}
				String chName = null;
				Iterator<Entry<String, Integer>> it = LoginServ.chName2id.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<java.lang.String, java.lang.Integer> entry = (Map.Entry<java.lang.String, java.lang.Integer>) it
							.next();
					if(entry.getValue().intValue()==cl.LoginChannel){
						chName = entry.getKey();
						break;
					}
				}
				if(chName == null){
					log.info("渠道名称是null，渠道code{}", cl.LoginChannel);
					return;
				}
				XGParam pp = XGParam.channels.get(chName);
				if(pp == null){
					log.info("渠道配置是null, {},{}",chName,cl.LoginChannel);
					return;
				}
				String cc = null;
				for(PushInfo conf :list){
					if(conf.ID == 80){
						cc = conf.str;
						break;
					}
				}
				if(cc == null){
					log.info("null conf for {}",loseId);
					return;
				}
				cc = cc.replace("YYY", win.name);
				JSONObject ret = null;
				if(isAndroidServer){
					ret = XingeApp.pushTokenAndroid(pp.accessId, pp.secretKey, title, cc, bean.token);
				}else{
					ret = XingeApp.pushTokenIos(pp.accessId, pp.secretKey, cc, bean.token, pp.env);
				}
				log.info("push {} to {} ret {}",cc,loseId,ret);
			}
		},"sendBaiZhanFail").start();
	}
}
