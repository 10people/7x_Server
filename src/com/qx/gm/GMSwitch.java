package com.qx.gm;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.manu.dynasty.core.servlet.GMServlet;
import com.qx.gm.activity.GMActivityMgr;
import com.qx.gm.email.GMEmailMgr;
import com.qx.gm.func.GMFuncMgr;
import com.qx.gm.log.GMLogMgr;
import com.qx.gm.message.ActivityCompensationReq;
import com.qx.gm.message.ActivityConsumeReq;
import com.qx.gm.message.ActivityConsumeRankReq;
import com.qx.gm.message.ActivityDelReq;
import com.qx.gm.message.ActivityInnerReq;
import com.qx.gm.message.ActivityTimeLoginReq;
import com.qx.gm.message.ActivityTopupReq;
import com.qx.gm.message.ActivityTopupRankReq;
import com.qx.gm.message.DoBanUserDownReq;
import com.qx.gm.message.DoBanUserReq;
import com.qx.gm.message.DoBanUserSpeakReq;
import com.qx.gm.message.DoDelGMNoticeReq;
import com.qx.gm.message.DoFuncSwitchReq;
import com.qx.gm.message.DoLiftBanUserReq;
import com.qx.gm.message.DoLiftBanUserSpeakReq;
import com.qx.gm.message.DoSendBareMailReq;
import com.qx.gm.message.DoSendMailReq;
import com.qx.gm.message.DoSendTestCodeReq;
import com.qx.gm.message.DoSendWelfareReq;
import com.qx.gm.message.DoServrStateReq;
import com.qx.gm.message.MailProp;
import com.qx.gm.message.QueryRoleInfoReq;
import com.qx.gm.message.QueryRoleListReq;
import com.qx.gm.message.QueryRoleStatusReq;
import com.qx.gm.message.Reward;
import com.qx.gm.notice.GMNoticeMgr;
import com.qx.gm.role.GMRoleMgr;
import com.qx.gm.util.CodeUtil;
import com.qx.gm.welfare.GMWelFareMgr;
import com.qx.util.JsonUtils;

/**
 * @ClassName: GMSwitch
 * @Description: GM请求的分发
 * @author 何金成
 * @date 2015年7月4日 下午6:13:57
 * 
 */
public class GMSwitch {
	public static GMSwitch inst;
	public GMRoleMgr roleMgr;
	public GMNoticeMgr noticeMgr;
	public GMEmailMgr emailMgr;
	public GMLogMgr logMgr;
	public GMActivityMgr activityMgr;
	public GMWelFareMgr welFareMgr;
	public GMFuncMgr funcMgr;

	public static GMSwitch getInstance() {
		if (null == inst) {
			inst = new GMSwitch();
		}
		return inst;
	}

	public GMSwitch() {
		initGM();
	}

	public void initGM() {
		roleMgr = new GMRoleMgr();
		noticeMgr = new GMNoticeMgr();
		emailMgr = new GMEmailMgr();
		logMgr = new GMLogMgr();
		activityMgr = new GMActivityMgr();
		funcMgr = new GMFuncMgr();
		welFareMgr = new GMWelFareMgr();
	}

	public void route(String data, PrintWriter writer) {
		JSONObject jo = JSONObject.fromObject(data);
		int type = 0;
		if (jo.get("type") instanceof Integer) {
			type = (Integer) jo.get("type");
		} else if (jo.get("type") instanceof String) {
			type = Integer.parseInt((String) jo.get("type"));
		}
		switch (type) {
		case GMPD.IDIP_QUERY_ROLEINFO_REQ:// 玩家信息查询
			roleMgr.queryRoleinfo(
					JsonUtils.strToJavaBean(data, QueryRoleInfoReq.class),
					writer);
			break;
		case GMPD.IDIP_QUERY_ROLELSTATUSE_REQ:// 角色状态查询
			roleMgr.queryRoleStatus(
					JsonUtils.strToJavaBean(data, QueryRoleStatusReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_BAN_USR_SPEAK_REQ:// 禁言处理请求
			roleMgr.doBanUserSpeak(
					JsonUtils.strToJavaBean(data, DoBanUserSpeakReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_BAN_USR_REQ:// 封号处理请求
			roleMgr.doBanUser(
					JsonUtils.strToJavaBean(data, DoBanUserReq.class), writer);
			break;
		case GMPD.IDIP_DO_BAN_USR_DOWN_REQ:// 踢玩家下线
			roleMgr.doBanUserDown(
					JsonUtils.strToJavaBean(data, DoBanUserDownReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_LIFTBAN_USR_SPEAK_REQ:// 解除禁言请求
			roleMgr.doLiftBanUserSpeak(
					JsonUtils.strToJavaBean(data, DoLiftBanUserSpeakReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_LIFTBAN_USR_REQ:// 解除封号请求
			roleMgr.doLiftBanUser(
					JsonUtils.strToJavaBean(data, DoLiftBanUserReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_SEND_MAIL_REQ:// 发送带附件邮件请求（邮件发道具）
			// 邮件需要对prop数组处理，去掉两头的双引号，并去掉里面引号转义，使json能识别
			data = data.replace("}]\"", "}]").replace("\"[{", "[{")
					.replace("\\\"", "\"");
			// json要转换的javabean含 复杂类型(List)，特殊处理
			Map<String, Class<MailProp>> classMap = new HashMap<String, Class<MailProp>>();
			classMap.put("prop", MailProp.class);
			DoSendMailReq request = (DoSendMailReq) JSONObject.toBean(
					JSONObject.fromObject(data), DoSendMailReq.class, classMap);
			emailMgr.doSendMail(
					request,
					jo.getString("prop").replace("}]\"", "}]")
							.replace("\"[{", "[{").replace("\\\"", "\""),
					writer);// prop传入做md5验证
			break;
		case GMPD.IDIP_DO_SEND_BARE_MAIL_REQ:// 发送不带附件邮件请求
			emailMgr.doSendBareMail(
					JsonUtils.strToJavaBean(data, DoSendBareMailReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_SEND_TEST_CODE_REQ:// 申请福利之验证信息
			welFareMgr.doSendTestCode(
					JsonUtils.strToJavaBean(data, DoSendTestCodeReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_SEND_WELFARE_REQ:// 申请福利请求
			welFareMgr.doSendWelfare(
					JsonUtils.strToJavaBean(data, DoSendWelfareReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_SERVR_STATE_REQ:// 功能开关之单独功能服务器状态
			funcMgr.doServrState(
					JsonUtils.strToJavaBean(data, DoServrStateReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_FUNC_SWITCH_REQ:// 功能开关设置
			funcMgr.doFuncSwitch(
					JsonUtils.strToJavaBean(data, DoFuncSwitchReq.class),
					writer);
			break;
		case GMPD.IDIP_ACTIVITY_COMPENSATION:// 全服补偿配置
			// 活动需要对reward数组处理，去掉两头的双引号，并去掉里面引号转义，使json能识别
			data = data.replace("}]\"", "}]").replace("\"[{", "[{")
					.replace("\\\"", "\"");
			// json要转换的javabean含 复杂类型(List)，特殊处理
			Map<String, Class<Reward>> compensationMap = new HashMap<String, Class<Reward>>();
			compensationMap.put("reward", Reward.class);
			ActivityCompensationReq activityCompensation = (ActivityCompensationReq) JSONObject
					.toBean(JSONObject.fromObject(data),
							ActivityCompensationReq.class, compensationMap);
			activityMgr.activityCompensation(
					activityCompensation,
					writer,
					jo.getString("reward").replace("}]\"", "}]")
							.replace("\"[{", "[{").replace("\\\"", "\""));
			break;
		case GMPD.IDIP_ACTIVITY_TIMEDLOGIN:// 限时登录配置
			// 活动需要对reward数组处理，去掉两头的双引号，并去掉里面引号转义，使json能识别
			data = data.replace("}]\"", "}]").replace("\"[{", "[{")
					.replace("\\\"", "\"");
			// json要转换的javabean含 复杂类型(List)，特殊处理
			Map<String, Class<Reward>> timeLoginMap = new HashMap<String, Class<Reward>>();
			timeLoginMap.put("reward", Reward.class);
			ActivityTimeLoginReq timeLogin = (ActivityTimeLoginReq) JSONObject
					.toBean(JSONObject.fromObject(data),
							ActivityCompensationReq.class, timeLoginMap);
			activityMgr.activityTimeLogin(
					timeLogin,
					writer,
					jo.getString("reward").replace("}]\"", "}]")
							.replace("\"[{", "[{").replace("\\\"", "\""));
			break;
		case GMPD.IDIP_ACTIVITY_TOPUP:// 累计充值配置
			// 活动需要对reward数组处理，去掉两头的双引号，并去掉里面引号转义，使json能识别
			data = data.replace("}]\"", "}]").replace("\"[{", "[{")
					.replace("\\\"", "\"");
			// json要转换的javabean含 复杂类型(List)，特殊处理
			Map<String, Class<Reward>> topupMap = new HashMap<String, Class<Reward>>();
			topupMap.put("reward", Reward.class);
			ActivityTopupReq topup = (ActivityTopupReq) JSONObject.toBean(
					JSONObject.fromObject(data), ActivityTopupReq.class, topupMap);
			activityMgr.activityTopup(
					topup,
					writer,
					jo.getString("reward").replace("}]\"", "}]")
							.replace("\"[{", "[{").replace("\\\"", "\""));
			break;
		case GMPD.IDIP_ACTIVITY_CONSUME:// 累计消费配置
			// 活动需要对reward数组处理，去掉两头的双引号，并去掉里面引号转义，使json能识别
			data = data.replace("}]\"", "}]").replace("\"[{", "[{")
					.replace("\\\"", "\"");
			// json要转换的javabean含 复杂类型(List)，特殊处理
			Map<String, Class<Reward>> consumeMap = new HashMap<String, Class<Reward>>();
			consumeMap.put("reward", Reward.class);
			ActivityConsumeReq consume = (ActivityConsumeReq) JSONObject.toBean(
					JSONObject.fromObject(data), ActivityConsumeReq.class,
					consumeMap);
			activityMgr.activityConsume(
					consume,
					writer,
					jo.getString("reward").replace("}]\"", "}]")
							.replace("\"[{", "[{").replace("\\\"", "\""));
			break;
		case GMPD.IDIP_ACTIVITY_TOPUP_RANK:// 充值排行配置
			// 活动需要对reward数组处理，去掉两头的双引号，并去掉里面引号转义，使json能识别
			data = data.replace("}]\"", "}]").replace("\"[{", "[{")
					.replace("\\\"", "\"");
			// json要转换的javabean含 复杂类型(List)，特殊处理
			Map<String, Class<Reward>> topupRankMap = new HashMap<String, Class<Reward>>();
			topupRankMap.put("reward", Reward.class);
			ActivityTopupRankReq topupRank = (ActivityTopupRankReq) JSONObject.toBean(
					JSONObject.fromObject(data), ActivityTopupRankReq.class, topupRankMap);
			activityMgr.activityTopupRank(
					topupRank,
					writer,
					jo.getString("reward").replace("}]\"", "}]")
							.replace("\"[{", "[{").replace("\\\"", "\""));
			break;
		case GMPD.IDIP_ACTIVITY_CONSUME_RANK:// 消费排行配置
			// 活动需要对reward数组处理，去掉两头的双引号，并去掉里面引号转义，使json能识别
			data = data.replace("}]\"", "}]").replace("\"[{", "[{")
					.replace("\\\"", "\"");
			// json要转换的javabean含 复杂类型(List)，特殊处理
			Map<String, Class<Reward>> consumeRankMap = new HashMap<String, Class<Reward>>();
			consumeRankMap.put("reward", Reward.class);
			ActivityConsumeRankReq consumeRank = (ActivityConsumeRankReq) JSONObject
					.toBean(JSONObject.fromObject(data),
							ActivityConsumeRankReq.class, consumeRankMap);
			activityMgr.activityConsumeRank(
					consumeRank,
					writer,
					jo.getString("reward").replace("}]\"", "}]")
							.replace("\"[{", "[{").replace("\\\"", "\""));
			break;
		case GMPD.IDIP_ACTIVITY_INNER:// 内置活动配置
			activityMgr.activityInner(
					JsonUtils.strToJavaBean(data, ActivityInnerReq.class), writer);
			break;
		case GMPD.IDIP_ACTIVITY_DEL:// 活动删除请求
			activityMgr.activityDel(
					JsonUtils.strToJavaBean(data, ActivityDelReq.class), writer);
			break;
		case GMPD.IDIP_DO_SEND_GM_NOTICE_REQ:// 发送系统公告信息请求
			noticeMgr.doSendGMNotice(
					JsonUtils.strToJavaBean(data, QueryRoleListReq.class),
					writer);
			break;
		case GMPD.IDIP_DO_DEL_GM_NOTICE_REQ:
			noticeMgr.doDelGMNotice(
					JsonUtils.strToJavaBean(data, DoDelGMNoticeReq.class),
					writer);
			break;
		/*
		 * case GMPD.IDIP_QUERY_VERSION_REQ:// 发送版本公告信息请求
		 * noticeMgr.queryVersion( JsonUtils.strToJavaBean(data,
		 * QueryVersionReq.class), writer); break; case
		 * GMPD.IDIP_OPREATE_TOPUP_REQ:// 玩家充值查询 roleMgr.operateTopup(
		 * JsonUtils.strToJavaBean(data, OperateTopupReq.class), writer); break;
		 * case GMPD.IDIP_OPREATE_CONSUME_REQ:// 元宝消费查询 roleMgr.operateConsume(
		 * JsonUtils.strToJavaBean(data, OperateConsumeReq.class), writer);
		 * break; case GMPD.IDIP_OPREATE_LOG_REQ:// 日志查询 logMgr.operateLog(
		 * JsonUtils.strToJavaBean(data, OperateLogReq.class), writer); break;
		 */
		default:
			GMServlet.write("{\"code\":" + CodeUtil.NOT_DEFINE_CODE + "}",
					writer);
			break;
		}
	}
}
