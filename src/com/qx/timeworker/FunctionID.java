package com.qx.timeworker;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;

public class FunctionID {
	public static Logger log = LoggerFactory.getLogger(FunctionID.class);
	//国家-上缴
	public static int ShangJiao4Gongjin=500020;
	// 签到
	public static int Qiandao=140;
	// 首冲
	public static int Shouchong=142;
	//回家领经验
	public static int HuiJia4JingYan=500040;//2015年9月29日11:47:37 之前是 7;
	//铁匠-洗练
	public static int XiLian=1210;
	// 游侠
	public static int youXia = 300;
	// 游侠-可攻打
	public static int youXiaStatus = 305;
	// 百战
	public static int baizhan = 300100;
	// 铜币膜拜
	public static int tongBiMoBai = 400010;
	// 进阶
	public static int jinJie = 1211;
	// 秘宝升级
	public static int miBaoShengJi = 600;
	// lianmeng
	public static int alliance = 410000;
	// 联盟事件
	public static final int ALLIANCE_EVENT = 1000001;
	// 联盟新申请
	public static final int ALLIANCE_NEW_APPLYER = 1000002;

	public static int tanBao = 11;  // 探宝
	public static int tanBao_free = 1101;		// 免费单抽
	public static int tanBao_sigle_free = 1102; // 付费单抽的免费
	
	public static int lveDuo = 211;		//掠夺

	public static int youxiang_system = 41;	// 邮箱-系统
	public static int youxiang_person = 10; // 邮箱 -私信

	public static int yabiao = 310; //押镖
	public static int yabiao_history = 313;		//押镖-历史记录
	public static int yabiao_enemy = 315;//押镖-仇人
	
	
	//推送某功能红点可以出现 和前段约定code为负数时红点消失
	public static void pushCanShangjiao(long jzId,IoSession session,int Code){
		log.info("向君主{}推送--{}可以出现提示红点",jzId,Code);
		ErrorMessage.Builder resp=ErrorMessage.newBuilder();
		resp.setErrorCode(Code);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.RED_NOTICE;
		pm.builder = resp;
		session.write(pm);
	}
}
