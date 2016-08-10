package xg.push;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.store.Redis;
import com.manu.network.SessionAttKey;
import com.qx.account.SettingsBean;
import com.qx.persistent.HibernateUtil;
import com.tencent.xinge.TagTokenPair;
import com.tencent.xinge.XingeApp;

/**
 * 添加tag失败，增加日志。
 * @author kjh
 *
 */
public class XGFixB extends XG{
	public static Logger log = LoggerFactory.getLogger(XGFixB.class);
	public static XG prInst;
	public static XGFixB inst;
	public XGFixB(){
		inst =this;
		XGFixBEvt.inst = new XGFixBEvt();
	}
	public void clientReportToken(int id, IoSession session, Builder builder){
		ErrorMessage.Builder msg = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		String token = msg.getErrorDesc();
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			log.info("sid {} null junZhuId when report token {}",session.getId(),token);
			session.setAttribute("XGTOKEN0982398", token);//乱写的数字是为了防止巧合
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
//		SettingsBean confBean = HibernateUtil.find(SettingsBean.class, junZhuId);
		String settingsBean = Redis.instance.hget("SettingsBean", String.valueOf(junZhuId));
		if(settingsBean == null || settingsBean.isEmpty()){
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
}
