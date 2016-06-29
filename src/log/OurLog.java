package log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.boot.GameServer;
import com.manu.network.SessionManager;
import com.qx.account.AccountManager;
import com.qx.http.LoginServ;

/**
 * 公司内部的log，用于数据分析。经分日志。设计文件是 rawsql.xml
 * http://192.168.3.80/
 * @author 康建虎
 *
 */
public class OurLog {
	public static LoginServ chCode = new LoginServ(null, "");
	public String vGameAppid = "vGameAppid";
	public String GameSvrId = String.valueOf(GameServer.serverId);
	public int PlatID = 1;//(必填)ios 0 /android 1
	public int ZoneID = 0;//(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0
	public static OurLog log = new OurLog();
	public SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static Logger GameSvrState = LoggerFactory.getLogger("GameSvrState");
	public static Logger PlayerOnline = LoggerFactory.getLogger("PlayerOnline");
	
	public static Logger PlayerLogout = LoggerFactory.getLogger("PlayerLogout");
	public static Logger MoneyFlow = LoggerFactory.getLogger("MoneyFlow");
	public static Logger ItemFlow = LoggerFactory.getLogger("ItemFlow");
	public static Logger PlayerExpFlow = LoggerFactory.getLogger("PlayerExpFlow");
	public static Logger SnsFlow = LoggerFactory.getLogger("SnsFlow");
	public static Logger RoundFlow = LoggerFactory.getLogger("RoundFlow");
	/*
  <struct  name="GameSvrState"  version="1" desc="(必填)服务器状态流水，每分钟一条日志">
    <entry  name="dtEventTime"		type="datetime"					desc="(必填) 格式 YYYY-MM-DD HH:MM:SS" />
    <entry  name="vGameIP"			type="string"		size="32"						desc="(必填)服务器IP" />
  </struct> 
	 */
	public void GameSvrState(){
		//FIXME 不用每次都取
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String ip = addr.getHostAddress().toString();//获得本机IP
		GameSvrState.info("{},{}",fmt.format(Calendar.getInstance().getTime()),ip);
	}
	public void PlayerOnline(){
		Thread t = new Thread(new Log2DBRegAndLogin(),"Log2DBRegAndLogin");
		t.setDaemon(true);
		t.start();
	}
	public void PlayerOnline(long RoleId, String RoleName, String vopenid,int num,int reg,int login,int PlatID,int LoginChannel){
		//</struct>
				PlayerOnline.info("{},{},{},{},{},{},{}"
				//PlayerOnline.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
		,num//1<entry  name="num"		type="int"						defaultvalue="0"	desc="(必填)在线人数"/>
		,reg//2<entry  name="reg"		type="int"						defaultvalue="0"	desc="(必填)5分钟注册数"/>
		,login//3<entry  name="login"		type="int"						defaultvalue="0"	desc="(必填)5分钟之内登陆数"/>
		,fmt.format(Calendar.getInstance().getTime())//4<entry  name="dtEventTime"		type="datetime"					desc="(必填) 格式 YYYY-MM-DD HH:MM:SS" />
		,PlatID//5<entry  name="PlatID"			type="int"						defaultvalue="0"	desc="(必填)ios 0 /android 1"/>
		,GameSvrId//6<entry  name="GameSvrId"			type="string"		size="25"	desc="(必填)登录的游戏服务器编号" />
		,LoginChannel//7<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
		);//</struct>
			}
	
	
	public void PlayerLogout(long OnlineTime,int level,int PlayerFriendsNum,String RoleId){
					//     {1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
		PlayerLogout.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
					//<struct  name="PlayerLogout" version="1" desc="(必填)玩家登出">
				GameServer.cfg.get("serverId"),//	    <entry  name="GameSvrId"          type="string"      size="25"							desc="(必填)登录的游戏服务器编号" />
				fmt.format(Calendar.getInstance().getTime()),//	    <entry  name="dtEventTime"		 type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
				vGameAppid,//	    <entry  name="vGameAppid"          type="string"		size="32"							desc="(必填)游戏APPID" />
				PlatID,//	    <entry  name="PlatID"			 type="int"						defaultvalue="0"		desc="(必填)ios 0/android 1"/>
				ZoneID,//	    <entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
				"0",//	    <entry  name="vopenid"             type="string"		size="64"							desc="(必填)用户OPENID号" />
				OnlineTime,	//	    <entry  name="OnlineTime"		 type="int"												desc="(必填)本次登录在线时间(秒)" />
				level,	//	    <entry  name="Level"				 type="int"												desc="(必填)等级" />
				PlayerFriendsNum,	//	    <entry  name="PlayerFriendsNum"   type="int"												desc="(必填)玩家好友数量"/>
				"ClientVersion",//	    <entry  name="ClientVersion"		 type="string"		size="64"	defaultvalue="NULL"		desc="(必填)客户端版本"/>
				"SystemSoftware",//	    <entry  name="SystemSoftware"	 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)移动终端操作系统版本"/>
				"SystemHardware",//	    <entry  name="SystemHardware"	 type="string"		size="64"	defaultvalue="NULL"		desc="(必填)移动终端机型"/>
				"TelecomOper",//	    <entry  name="TelecomOper"		 type="string"		size="64"	defaultvalue="NULL"		desc="(必填)运营商"/>
				"Network",//	    <entry  name="Network"			 type="string"		size="64"	defaultvalue="NULL"		desc="(必填)3G/WIFI/2G"/>
				"0",//	    <entry  name="ScreenWidth"		 type="int"						defaultvalue="0"		desc="(可选)显示屏宽度"/>
				"0",//	    <entry  name="ScreenHight"		 type="int"						defaultvalue="0"		desc="(可选)显示高度"/>
				"0",//	    <entry  name="Density"			 type="float"					defaultvalue="0"		desc="(可选)像素密度"/>
				chCode.getChCodeByRoleId(RoleId),//	    <entry  name="LoginChannel"		 type="int"						defaultvalue="0"		desc="(可选)登录渠道"/>
				"CpuHardware",//	    <entry  name="CpuHardware"		 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)cpu类型;频率;核数"/>
				0,//	    <entry  name="Memory"			 type="int"						defaultvalue="0"		desc="(可选)内存信息单位M"/>
				"GLRender",//	    <entry  name="GLRender"			 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl render信息"/>
				"GLVersion",//	    <entry  name="GLVersion"			 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl版本信息"/>
				"DeviceId",//	    <entry  name="DeviceId"			 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)设备ID"/>
				RoleId//<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
					//	  </struct>
				);
	}
	/////////////////////////
	public void MoneyFlow(int level, int AfterMoney,int iMoney, int Reason,int add0sub1, int MONEYTYPE_pay1_free0, String RoleId,int rmb){
		//				{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
		MoneyFlow.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
		// <struct  name="MoneyFlow" version="1" desc="(必填)货币流水">
		GameServer.cfg.get("serverId"),//1<entry  name="GameSvrId"        type="string"      size="25"							desc="(必填)登录的游戏服务器编号" />
		fmt.format(Calendar.getInstance().getTime()),//2<entry  name="dtEventTime"      type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
		vGameAppid,//3<entry  name="vGameAppid"        type="string"      size="32"							desc="(必填)游戏APPID" />
		PlatID,//4<entry  name="PlatID"		   type="int"						defaultvalue="0"	desc="(必填)ios 0/android 1"/>
		ZoneID,//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
		"0",//6<entry  name="vopenid"           type="string"      size="64"							desc="(必填)用户OPENID号" />
		"0",//7<entry  name="Sequence"		   type="int"											desc="(可选)用于关联一次动作产生多条不同类型的货币流动日志" />
		level,//8<entry  name="Level"            type="int"											desc="(必填)玩家等级" />
		AfterMoney,//9<entry  name="AfterMoney"       type="int"       									desc="(可选)动作后的金钱数" />
		add0sub1 == 0 ? iMoney : -iMoney,//10<entry  name="iMoney"            type="int"       									desc="(必填)动作涉及的金钱数" />
		Reason,//11<entry  name="Reason"           type="int"       									desc="(必填)货币流动一级原因" />
		0,//12<entry  name="SubReason"        type="int"       									desc="(可选)货币流动二级原因" />
		add0sub1,//13<entry  name="AddOrReduce"      type="int"											desc="(必填)增加 0/减少 1" />
		MONEYTYPE_pay1_free0,//14<entry  name="iMoneyType"        type="int"											desc="(必填)钱的类型MONEYTYPE" />
		chCode.getChCodeByRoleId(RoleId),//15<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
		RoleId,//16<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
	    rmb//17<entry  name="Rmb"			type="int"						desc="(可选)充值的RMB数量" />

		//  </struct>
				);
	}
	public void ItemFlow(int level,int iGoodsType,int iGoodsId,int Count,int AfterCount,int Reason,int SubReason,int iMoney,int MONEYTYPE_pay1_free0,int add0sub1, String RoleId){
		ItemFlow.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
		//  <struct  name="ItemFlow" version="1" desc="(必填)道具流水表">
		GameServer.cfg.get("serverId"),//<entry  name="GameSvrId"      type="string"       size="25"							desc="(必填)登录的游戏服务器编号" />
		fmt.format(Calendar.getInstance().getTime()),//<entry  name="dtEventTime"    type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
		vGameAppid,//<entry  name="vGameAppid"      type="string"       size="32"							desc="(必填)游戏APPID" />
		PlatID,//<entry  name="PlatID"         type="int"							defaultvalue="0"    desc="(必填)ios 0/android 1"/>
		ZoneID,//<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
		"0",//<entry  name="vopenid"         type="string"       size="64"							desc="(必填)玩家" />
		level,//<entry  name="Level"          type="int"												desc="(必填)玩家等级" />
		0,//<entry  name="Sequence"			 type="int"												desc="(必填)用于关联一次购买产生多条不同类型的货币日志" />
		iGoodsType,//<entry  name="iGoodsType"       type="int"												desc="(必填)道具类型" />
		iGoodsId,//<entry  name="iGoodsId"         type="int"												desc="(必填)道具ID" />
		Count,//<entry  name="Count"          type="int"												desc="(必填)数量" />
		AfterCount,//<entry  name="AfterCount"			type="int"											desc="(必填)动作后的物品存量" />
		Reason,//<entry  name="Reason"				type="int"       									desc="(必填)道具流动一级原因" />
		SubReason,//<entry  name="SubReason"				type="int"       									desc="(必填)道具流动二级原因" />
		iMoney,//<entry  name="iMoney"          type="int"												desc="(必填)花费代币或金币购买道具情况下输出消耗的钱数量，否则填0" />
		MONEYTYPE_pay1_free0,//<entry  name="iMoneyType"      type="int"												desc="(必填)钱的类型MONEYTYPE,其它货币类型参考FAQ文档" />
		add0sub1,//<entry  name="AddOrReduce"           type="int"											desc="(必填)增加 0/减少 1" />
		chCode.getChCodeByRoleId(RoleId),//<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
		RoleId//<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
		//  </struct>
				);
	}
	
	public void PlayerExpFlow(int ExpChange,int BeforeLevel,int AfterLevel,int Time, int Reason,int SubReason,String RoleId){
		PlayerExpFlow.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
		//  <struct  name="PlayerExpFlow" version="1" desc="(可选)人物等级流水表">
		GameServer.cfg.get("serverId"),//<entry  name="GameSvrId"          type="string"        size="25"						desc="(必填)登录的游戏服务器编号" />
		fmt.format(Calendar.getInstance().getTime()),//<entry  name="dtEventTime"        type="datetime"									desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
		vGameAppid,//<entry  name="vGameAppid"          type="string"		  size="32"						desc="(必填)游戏APPID" />
		PlatID,//<entry  name="PlatID"			       type="int"						defaultvalue="0"	desc="(必填)ios 0/android 1"/>
		ZoneID,//<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
		"0",//<entry  name="vopenid"             type="string"        size="64"						desc="(必填)玩家" />
		ExpChange,//<entry  name="ExpChange"          type="int"											desc="(必填)经验变化" />
		BeforeLevel,//<entry  name="BeforeLevel"        type="int"											desc="(可选)动作前等级" />
		AfterLevel,//<entry  name="AfterLevel"         type="int"											desc="(必填)动作后等级" />
		Time,//<entry  name="Time"				       type="int"											desc="(必填)升级所用时间(秒)" />
		Reason,//<entry  name="Reason"             type="int"       									desc="(必填)经验流动一级原因" />
		SubReason,//<entry  name="SubReason"          type="int"       									desc="(必填)经验流动二级原因" />
		chCode.getChCodeByRoleId(RoleId),//<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
		RoleId//<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
		//  </struct>
				);
	}
	//====================================
//	  <macrosgroup name="SNSTYPE">
//	    <macro name="SNSTYPE_SHOWOFF"                 value="0" desc="炫耀"/>
//	    <macro name="SNSTYPE_INVITE"                  value="1" desc="邀请"/>
//	    <macro name="SNSTYPE_SENDHEART"               value="2" desc="送心"/>
//	    <macro name="SNSTYPE_RECEIVEHEART"            value="3" desc="收取心"/>
//	    <macro name="SNSTYPE_SENDEMAIL"               value="4" desc="发邮件"/>
//	    <macro name="SNSTYPE_RECEIVEEMAIL"            value="5" desc="收邮件"/>
//	    <macro name="SNSTYPE_SHARE"                    value="6" desc="分享"/>
//	    <macro name="SNSTYPE_OTHER"                   value="7" desc="其他原因"/>
//	  </macrosgroup>
	
	public void SnsFlow(String ActorOpenID,int Count,int SNSType,int SNSSubType, String RoleId){
		SnsFlow.info("{},{},{},{},{},{},{},{},{},{},{},{}",
		//  <struct     name="SnsFlow" version="1" desc="(必填)SNS流水">
		GameServer.cfg.get("serverId"),//<entry  name="GameSvrId"         type="string"        size="25"							desc="(必填)登录的游戏服务器编号" />
		fmt.format(Calendar.getInstance().getTime()),//<entry  name="dtEventTime"       type="datetime"											desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
		vGameAppid,//<entry  name="vGameAppid"         type="string"        size="32"							desc="(必填)游戏APPID" />
		PlatID,//<entry  name="PlatID"            type="int"							defaultvalue="0"	desc="(必填)ios 0/android 1"/>
		ZoneID,//<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
		ActorOpenID,//<entry  name="ActorOpenID"       type="string"        size="64"					desc="(必填)动作发起玩家" />
		0,//<entry  name="RecNum"             type="int"												desc="(可选)接收玩家个数"/>
		Count,//<entry  name="Count"			  type="int"												desc="(必填)发送的数量"/>
		SNSType,//<entry  name="SNSType"           type="int"										desc="(必填)交互一级类型,其它说明参考FAQ文档" />
		SNSSubType,//<entry  name="SNSSubType"        type="int"										desc="(可选)交互二级类型" />
		chCode.getChCodeByRoleId(RoleId),//<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
		RoleId//<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
		//   </struct>
				);
	}
	/*
	 * RoundFlow表        BATTLETYPE   区分是任务还是关卡 
	 * BATTLETYPE=1任务  
	 * BATTLETYPE=2关卡
   Result   任务 Result=1接收 Result=2完成
   Result   关卡 Result=2首次通关 Result=3不是首次通关

	 */
	public void RoundFlow(String vopenid,int BattleID, int BattleType,int RoundScore, int RoundTime,int Result,String RoleId){
		RoundFlow.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}",
		//  <struct name="RoundFlow" version="1" desc="(必填)单局结束数据流水">
		GameServer.cfg.get("serverId"),//<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
		fmt.format(Calendar.getInstance().getTime()),//<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
		vGameAppid,//<entry  name="vGameAppid"          type="string"		  size="32"				desc="(必填)游戏APPID" />
		PlatID,//<entry  name="PlatID"			 type="int"							defaultvalue="0"	desc="(必填)ios 0/android 1"/>
		ZoneID,//<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
		vopenid,//<entry  name="vopenid"             type="string"        size="64"			desc="(必填)玩家" />
		BattleID,//<entry  name="BattleID"           type="int"												desc="(必填)本局id" />
		BattleType,//<entry  name="BattleType"         type="int"						desc="(必填)战斗类型 对应BATTLETYPE" />
		RoundScore,//<entry  name="RoundScore"         type="int"												desc="(必填)本局分数" />
		RoundTime,//<entry  name="RoundTime"         type="int"												desc="(必填)对局时长(秒)" />
		Result,//<entry  name="Result"             type="int"												desc="(必填)单局结果" />
		0,//<entry  name="Rank"               type="int"												desc="(必填)排名" />
		0,//<entry  name="Gold"               type="int"												desc="(必填)金钱" />
		chCode.getChCodeByRoleId(RoleId),//<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
		RoleId//<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
		//  </struct>
				);
	}
}
//////////
/*
 2015年7月7日17:00:08
 尚兴跃-游戏谷<wssxy@hotmail.com>  15:03:46
大家注意了~ 更新一下LOG的结构，在每个表的最后，追加一个字段：roleid

用于记录游戏角色ID
 
 ---------------------------
 
太公望 13:36:20 
按照每个表，存成一个csv之类的文本文件。 用每字段位置用,号分割
太公望 13:36:37 
每行一条记录。用Linux的/n换行符换行

每小时一次。 但cunliang表是每天一次

----
$ip = 'xx.xx.xx.xx';
  $comd = "/usr/bin/mysql   -h {$ip} -P 5029 --local-infile  -uroot -p'xxxxxxxxxx' -S /tmp/mysql-ib.sock -D {$db_name} --skip-column-names -e \"LOAD DATA LOCAL INFILE '/data/www/app/ib_log/{$表名}_{$Ymdh}.csv'  into table {$表名} FIELDS TERMINATED BY ',' \"";

http://www.cnblogs.com/obullxl/archive/2012/06/11/jdbc-mysql-load-data-infile.html 
批量导入Java实现

*/
//{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30

/*
use sourcedata_tlog_newdb;
LOAD DATA LOCAL INFILE '/var/log/qxrouter/PlayerRegister.log'  into table PlayerRegister FIELDS TERMINATED BY ',';
*/