package log;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.json.JSONArray;
public class gen2 {
//
//<!--
//////////////////////////////////////////////////////////
//文档说明：
//1.该XML只针对第一类数据，用于LOG开发。
//2.表与字段说明。 表与字段desc列里面说明可选或必填。
//3.注意事项。
//A>增删字段。LOG XML版本确定并接入TDW后，不能删除字段，增加字段在表结尾增加。
//B>数据类型。同种数据类型可变更。int类型可以转换string,但string 不能转int.
//4.表名与字段名尽可能与本XML模板保持一致，方便维护。
///////////////////////////////////////////////////////////
//-->
//
//<!--*****************************************************************
/////////////////////////////////////////////////////////////////////
////////////////////第一类流水数据/////////////////////////////
//////////////////////////////////////////////////////////////////////
//********************************************************************-->
//
//<!--//////////////////////////////////////////////
/////////服务器状态日志///////////////////////////////
///////////////////////////////////////////////////-->
	public void GameSvrState(long RoleId, String RoleName, String vopenid,String vGameIP){
//</struct>
		GameSvrState.info("{},{}"
		//GameSvrState.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,fmt.format(Calendar.getInstance().getTime())//1<entry  name="dtEventTime"		type="datetime"					desc="(必填) 格式 YYYY-MM-DD HH:MM:SS" />
,vGameIP//2<entry  name="vGameIP"			type="string"		size="32"						desc="(必填)服务器IP" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////用户在线表///////////////////////////////
///////////////////////////////////////////////////-->
	public void PlayerOnline(long RoleId, String RoleName, String vopenid,int num,int reg,int login,int PlatID,int LoginChannel){
//</struct>
		PlayerOnline.info("{},{},{},{},{},{},{}"
		//PlayerOnline.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,num//1<entry  name="num"		type="int"						defaultvalue="0"	desc="(必填)在线人数"/>
,reg//2<entry  name="reg"		type="int"						defaultvalue="0"	desc="(必填)5分钟注册数"/>
,login//3<entry  name="login"		type="int"						defaultvalue="0"	desc="(必填)5分钟之内登陆数"/>
,fmt.format(Calendar.getInstance().getTime())//4<entry  name="dtEventTime"		type="datetime"					desc="(必填) 格式 YYYY-MM-DD HH:MM:30   MM为5的整倍数  SS为30" />
,PlatID//5<entry  name="PlatID"			type="int"						defaultvalue="0"	desc="(必填)ios 0 /android 1"/>
,GameSvrId//6<entry  name="GameSvrId"			type="string"		size="25"	desc="(必填)登录的游戏服务器编号" />
,LoginChannel//7<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
);//</struct>
	}
//<!--//////////////////////////////////////////////
/////////玩家注册表///////////////////////////////
///////////////////////////////////////////////////-->
	public void PlayerRegister(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,String ClientVersion,String SystemSoftware,String SystemHardware,String TelecomOper,String Network,int ScreenWidth,int ScreenHight,float Density,int RegChannel,String CpuHardware,int Memory,String GLRender,String GLVersion,String DeviceId,String Ip){
//</struct>
		PlayerRegister.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//PlayerRegister.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"			type="string"		size="25"	desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"		type="datetime"					desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"			type="string"		size="32"						desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"			type="int"						defaultvalue="0"	desc="(必填)ios 0 /android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//6<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,ClientVersion//7<entry  name="ClientVersion"		type="string"		size="64"	defaultvalue="NULL" desc="(可选)客户端版本"/>
,SystemSoftware//8<entry  name="SystemSoftware"	type="string"		size="64"	defaultvalue="NULL" desc="(必填)移动终端操作系统版本"/>
,SystemHardware//9<entry  name="SystemHardware"	type="string"		size="64"	defaultvalue="NULL" desc="(必填)移动终端机型"/>
,TelecomOper//10<entry  name="TelecomOper"		type="string"		size="64"	defaultvalue="NULL" desc="(必填)运营商"/>
,Network//11<entry  name="Network"			type="string"		size="64"	defaultvalue="NULL" desc="(可选)3G/WIFI/2G"/>
,ScreenWidth//12<entry  name="ScreenWidth"		type="int"						defaultvalue="0"	desc="(必填)显示屏宽度"/>
,ScreenHight//13<entry  name="ScreenHight"		type="int"						defaultvalue="0"	desc="(必填)显示屏高度"/>
,Density//14<entry  name="Density"			type="float"					defaultvalue="0"	desc="(必填)像素密度"/>
,RegChannel//15<entry  name="RegChannel"		type="int"						defaultvalue="0"	desc="(必填)注册渠道"/>
,CpuHardware//16<entry  name="CpuHardware"		type="string"		size="64"	defaultvalue="NULL" desc="(可选)cpu类型|频率|核数"/>
,Memory//17<entry  name="Memory"			type="int"						defaultvalue="0"	desc="(可选)内存信息单位M"/>
,GLRender//18<entry  name="GLRender"			type="string"		size="64"	defaultvalue="NULL" desc="(可选)opengl render信息"/>
,GLVersion//19<entry  name="GLVersion"			type="string"		size="64"	defaultvalue="NULL" desc="(可选)opengl版本信息"/>
,DeviceId//20<entry  name="DeviceId"			type="string"		size="64"	defaultvalue="NULL"	desc="(可选)设备ID"/>
,RoleId//21<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
,Ip//22<entry  name="Ip"			type="string"		size="64"						desc="(必填)玩家登录IP" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////玩家登录表///////////////////////////////
///////////////////////////////////////////////////-->
	public void PlayerLogin(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,int Level,int PlayerFriendsNum,String ClientVersion,String SystemSoftware,String SystemHardware,String TelecomOper,String Network,int ScreenWidth,int ScreenHight,float Density,int LoginChannel,String CpuHardware,int Memory,String GLRender,String GLVersion,String DeviceId,String Ip){
//</struct>
		PlayerLogin.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//PlayerLogin.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"		size="25"							desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"		type="datetime"											desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"         type="string"		size="32"							desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"			type="int"						defaultvalue="0"		desc="(必填)ios 0/android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//6<entry  name="vopenid"            type="string"		size="64"							desc="(必填)用户OPENID号" />
,Level//7<entry  name="Level"             type="int"												desc="(必填)等级" />
,PlayerFriendsNum//8<entry  name="PlayerFriendsNum"  type="int"												desc="(必填)玩家好友数量"/>
,ClientVersion//9<entry  name="ClientVersion"		type="string"		size="64"	defaultvalue="NULL"		desc="(必填)客户端版本"/>
,SystemSoftware//10<entry  name="SystemSoftware"	type="string"		size="64"	defaultvalue="NULL"		desc="(必填)移动终端操作系统版本"/>
,SystemHardware//11<entry  name="SystemHardware"	type="string"		size="64"	defaultvalue="NULL"		desc="(必填)移动终端机型"/>
,TelecomOper//12<entry  name="TelecomOper"		type="string"		size="64"	defaultvalue="NULL"		desc="(必填)运营商"/>
,Network//13<entry  name="Network"			type="string"		size="64"	defaultvalue="NULL"		desc="(必填)3G/WIFI/2G"/>
,ScreenWidth//14<entry  name="ScreenWidth"		type="int"						defaultvalue="0"		desc="(必填)显示屏宽度"/>
,ScreenHight//15<entry  name="ScreenHight"		type="int"						defaultvalue="0"		desc="(必填)显示屏高度"/>
,Density//16<entry  name="Density"			type="float"					defaultvalue="0"		desc="(必填)像素密度"/>
,LoginChannel//17<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
,CpuHardware//18<entry  name="CpuHardware"		type="string"		size="64"	defaultvalue="NULL"		desc="(可选)cpu类型|频率|核数"/>
,Memory//19<entry  name="Memory"			type="int"						defaultvalue="0"		desc="(可选)内存信息单位M"/>
,GLRender//20<entry  name="GLRender"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl render信息"/>
,GLVersion//21<entry  name="GLVersion"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl版本信息"/>
,DeviceId//22<entry  name="DeviceId"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)设备ID"/>
,RoleId//23<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
,Ip//24<entry  name="Ip"			type="string"		size="64"						desc="(必填)玩家登录IP" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////玩家登出表///////////////////////////////
///////////////////////////////////////////////////-->
	public void PlayerLogout(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,int OnlineTime,int Level,int PlayerFriendsNum,String ClientVersion,String SystemSoftware,String SystemHardware,String TelecomOper,String Network,int ScreenWidth,int ScreenHight,float Density,int LoginChannel,String CpuHardware,int Memory,String GLRender,String GLVersion,String DeviceId){
//</struct>
		PlayerLogout.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//PlayerLogout.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"      size="25"							desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"		 type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"          type="string"		size="32"							desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"			 type="int"						defaultvalue="0"		desc="(必填)ios 0/android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//6<entry  name="vopenid"             type="string"		size="64"							desc="(必填)用户OPENID号" />
,OnlineTime//7<entry  name="OnlineTime"		 type="int"												desc="(必填)本次登录在线时间(秒)" />
,Level//8<entry  name="Level"				 type="int"												desc="(必填)等级" />
,PlayerFriendsNum//9<entry  name="PlayerFriendsNum"   type="int"												desc="(必填)玩家好友数量"/>
,ClientVersion//10<entry  name="ClientVersion"		 type="string"		size="64"	defaultvalue="NULL"		desc="(必填)客户端版本"/>
,SystemSoftware//11<entry  name="SystemSoftware"	 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)移动终端操作系统版本"/>
,SystemHardware//12<entry  name="SystemHardware"	 type="string"		size="64"	defaultvalue="NULL"		desc="(必填)移动终端机型"/>
,TelecomOper//13<entry  name="TelecomOper"		 type="string"		size="64"	defaultvalue="NULL"		desc="(必填)运营商"/>
,Network//14<entry  name="Network"			 type="string"		size="64"	defaultvalue="NULL"		desc="(必填)3G/WIFI/2G"/>
,ScreenWidth//15<entry  name="ScreenWidth"		 type="int"						defaultvalue="0"		desc="(可选)显示屏宽度"/>
,ScreenHight//16<entry  name="ScreenHight"		 type="int"						defaultvalue="0"		desc="(可选)显示高度"/>
,Density//17<entry  name="Density"			 type="float"					defaultvalue="0"		desc="(可选)像素密度"/>
,LoginChannel//18<entry  name="LoginChannel"		 type="int"						defaultvalue="0"		desc="(可选)登录渠道"/>
,CpuHardware//19<entry  name="CpuHardware"		 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)cpu类型;频率;核数"/>
,Memory//20<entry  name="Memory"			 type="int"						defaultvalue="0"		desc="(可选)内存信息单位M"/>
,GLRender//21<entry  name="GLRender"			 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl render信息"/>
,GLVersion//22<entry  name="GLVersion"			 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl版本信息"/>
,DeviceId//23<entry  name="DeviceId"			 type="string"		size="64"	defaultvalue="NULL"		desc="(可选)设备ID"/>
,RoleId//24<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
);//</struct>
	}
//
//
//<!--//////////////////////////////////////////////
/////////游戏货币流水表///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="ADDORREDUCE">
//<macro name="ADD"       value="0" desc="加"/>
//<macro name="REDUCE"    value="1" desc="减"/>
//</macrosgroup>
//
//<macrosgroup name="iMoneyType">
//<macro name="MT_MONEY"       value="0" desc="游戏币"/>
//<macro name="MT_DIAMOND"     value="1" desc="钻石"/>
//</macrosgroup>
//
	public void MoneyFlow(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,int Sequence,int Level,int AfterMoney,int iMoney,int Reason,int SubReason,int AddOrReduce,int iMoneyType,int LoginChannel,int Rmb){
//</struct>
		MoneyFlow.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//MoneyFlow.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"        type="string"      size="25"							desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"      type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"        type="string"      size="32"							desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"		   type="int"						defaultvalue="0"	desc="(必填)ios 0/android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//6<entry  name="vopenid"           type="string"      size="64"							desc="(必填)用户OPENID号" />
,Sequence//7<entry  name="Sequence"		   type="int"											desc="(可选)用于关联一次动作产生多条不同类型的货币流动日志" />
,Level//8<entry  name="Level"            type="int"											desc="(必填)玩家等级" />
,AfterMoney//9<entry  name="AfterMoney"       type="int"       									desc="(可选)动作后的金钱数" />
,iMoney//10<entry  name="iMoney"            type="int"       									desc="(必填)动作涉及的金钱数" />
,Reason//11<entry  name="Reason"           type="int"       									desc="(必填)货币流动一级原因" />
,SubReason//12<entry  name="SubReason"        type="int"       									desc="(可选)货币流动二级原因" />
,AddOrReduce//13<entry  name="AddOrReduce"      type="int"											desc="(必填)增加 0/减少 1" />
,iMoneyType//14<entry  name="iMoneyType"        type="int"											desc="(必填)钱的类型MONEYTYPE" />
,LoginChannel//15<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
,RoleId//16<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
,Rmb//17<entry  name="Rmb"			type="int"						desc="(可选)充值的RMB数量" />
);//</struct>
	}
//
//
//
//
//<!--//////////////////////////////////////////////
/////////道具流水表///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="ADDORREDUCE">
//<macro name="ADD"       value="0" desc="加"/>
//<macro name="REDUCE"    value="1" desc="减"/>
//</macrosgroup>
//
//
//<macrosgroup name="iMoneyType">
//<macro name="MT_MONEY"       value="0" desc="游戏币"/>
//<macro name="MT_DIAMOND"     value="1" desc="钻石"/>
//</macrosgroup>
//
	public void ItemFlow(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,int Level,int Sequence,int iGoodsType,int iGoodsId,int Count,int AfterCount,int Reason,int SubReason,int iMoney,int iMoneyType,int AddOrReduce,int LoginChannel){
//</struct>
		ItemFlow.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//ItemFlow.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"      type="string"       size="25"							desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"    type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"      type="string"       size="32"							desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"         type="int"							defaultvalue="0"    desc="(必填)ios 0/android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//6<entry  name="vopenid"         type="string"       size="64"							desc="(必填)玩家" />
,Level//7<entry  name="Level"          type="int"												desc="(必填)玩家等级" />
,Sequence//8<entry  name="Sequence"			 type="int"												desc="(必填)用于关联一次购买产生多条不同类型的货币日志" />
,iGoodsType//9<entry  name="iGoodsType"       type="int"												desc="(必填)道具类型" />
,iGoodsId//10<entry  name="iGoodsId"         type="int"												desc="(必填)道具ID" />
,Count//11<entry  name="Count"          type="int"												desc="(必填)数量" />
,AfterCount//12<entry  name="AfterCount"			type="int"											desc="(必填)动作后的物品存量" />
,Reason//13<entry  name="Reason"				type="int"       									desc="(必填)道具流动一级原因" />
,SubReason//14<entry  name="SubReason"				type="int"       									desc="(必填)道具流动二级原因" />
,iMoney//15<entry  name="iMoney"          type="int"												desc="(必填)花费代币或金币购买道具情况下输出消耗的钱数量，否则填0" />
,iMoneyType//16<entry  name="iMoneyType"      type="int"												desc="(必填)钱的类型MONEYTYPE,其它货币类型参考FAQ文档" />
,AddOrReduce//17<entry  name="AddOrReduce"           type="int"											desc="(必填)增加 0/减少 1" />
,LoginChannel//18<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
,RoleId//19<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
);//</struct>
	}
//
//
//<!--//////////////////////////////////////////////
/////////人物等级流水表///////////////////////////////
///////////////////////////////////////////////////-->
//
	public void PlayerExpFlow(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,int ExpChange,int BeforeLevel,int AfterLevel,int Time,int Reason,int SubReason,int LoginChannel){
//</struct>
		PlayerExpFlow.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//PlayerExpFlow.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"        size="25"						desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"        type="datetime"									desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"          type="string"		  size="32"						desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"			       type="int"						defaultvalue="0"	desc="(必填)ios 0/android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//6<entry  name="vopenid"             type="string"        size="64"						desc="(必填)玩家" />
,ExpChange//7<entry  name="ExpChange"          type="int"											desc="(必填)经验变化" />
,BeforeLevel//8<entry  name="BeforeLevel"        type="int"											desc="(可选)动作前等级" />
,AfterLevel//9<entry  name="AfterLevel"         type="int"											desc="(必填)动作后等级" />
,Time//10<entry  name="Time"				       type="int"											desc="(必填)升级所用时间(秒)" />
,Reason//11<entry  name="Reason"             type="int"       									desc="(必填)经验流动一级原因" />
,SubReason//12<entry  name="SubReason"          type="int"       									desc="(必填)经验流动二级原因" />
,LoginChannel//13<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
,RoleId//14<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
);//</struct>
	}
//
//
//<!--//////////////////////////////////////////////
/////////社交系统流水表///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="SNSTYPE">
//<macro name="SNSTYPE_SHOWOFF"                 value="0" desc="炫耀"/>
//<macro name="SNSTYPE_INVITE"                  value="1" desc="邀请"/>
//<macro name="SNSTYPE_SENDHEART"               value="2" desc="送心"/>
//<macro name="SNSTYPE_RECEIVEHEART"            value="3" desc="收取心"/>
//<macro name="SNSTYPE_SENDEMAIL"               value="4" desc="发邮件"/>
//<macro name="SNSTYPE_RECEIVEEMAIL"            value="5" desc="收邮件"/>
//<macro name="SNSTYPE_SHARE"                    value="6" desc="分享"/>
//<macro name="SNSTYPE_OTHER"                   value="7" desc="其他原因"/>
//</macrosgroup>
//
//
	public void SnsFlow(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,String ActorOpenID,int RecNum,int Count,int SNSType,int SNSSubType,int LoginChannel){
//</struct>
		SnsFlow.info("{},{},{},{},{},{},{},{},{},{},{},{}"
		//SnsFlow.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"        size="25"							desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"       type="datetime"											desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"         type="string"        size="32"							desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"            type="int"							defaultvalue="0"	desc="(必填)ios 0/android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,ActorOpenID//6<entry  name="ActorOpenID"       type="string"        size="64"					desc="(必填)动作发起玩家" />
,RecNum//7<entry  name="RecNum"             type="int"												desc="(可选)接收玩家个数"/>
,Count//8<entry  name="Count"			  type="int"												desc="(必填)发送的数量"/>
,SNSType//9<entry  name="SNSType"           type="int"										desc="(必填)交互一级类型,其它说明参考FAQ文档" />
,SNSSubType//10<entry  name="SNSSubType"        type="int"										desc="(可选)交互二级类型" />
,LoginChannel//11<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
,RoleId//12<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
);//</struct>
	}
//
//
//
//<!--//////////////////////////////////////////////
/////////单局流水表///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="BATTLETYPE">
//<macro name="BATTLE_PVE"       value="0" desc="单人游戏"/>
//<macro name="BATTLE_PVP"       value="1" desc="对战游戏"/>
//<macro name="BATTLE_OTHER"     value="2" desc="其他对局"/>
//</macrosgroup>
//
	public void RoundFlow(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,int BattleID,int BattleType,int RoundScore,int RoundTime,int Result,int Rank,int Gold,int LoginChannel){
//</struct>
		RoundFlow.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//RoundFlow.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"          type="string"		  size="32"				desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"			 type="int"							defaultvalue="0"	desc="(必填)ios 0/android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//6<entry  name="vopenid"             type="string"        size="64"			desc="(必填)玩家" />
,BattleID//7<entry  name="BattleID"           type="int"												desc="(必填)本局id" />
,BattleType//8<entry  name="BattleType"         type="int"						desc="(必填)战斗类型 对应BATTLETYPE" />
,RoundScore//9<entry  name="RoundScore"         type="int"												desc="(必填)本局分数" />
,RoundTime//10<entry  name="RoundTime"         type="int"												desc="(必填)对局时长(秒)" />
,Result//11<entry  name="Result"             type="int"												desc="(必填)单局结果" />
,Rank//12<entry  name="Rank"               type="int"												desc="(必填)排名" />
,Gold//13<entry  name="Gold"               type="int"												desc="(必填)金钱" />
,LoginChannel//14<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
,RoleId//15<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
);//</struct>
	}
//
//
//<!--//////////////////////////////////////////////
/////////玩家Crash 表///////////////////////////////
///////////////////////////////////////////////////-->
	public void PlayerCrash(long RoleId, String RoleName, String vopenid,String vGameAppid,int PlatID,int Level,int PlayerFriendsNum,String ClientVersion,String SystemSoftware,String SystemHardware,String TelecomOper,String Network,int ScreenWidth,int ScreenHight,float Density,int LoginChannel,String CpuHardware,int Memory,String GLRender,String GLVersion,String DeviceId){
//</struct>
		PlayerCrash.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//PlayerCrash.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"		size="25"							desc="(必填)登录的游戏服务器编号" />
,fmt.format(Calendar.getInstance().getTime())//2<entry  name="dtEventTime"		type="datetime"											desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//3<entry  name="vGameAppid"         type="string"		size="32"							desc="(必填)游戏APPID" />
,PlatID//4<entry  name="PlatID"			type="int"						defaultvalue="0"		desc="(必填)ios 0/android 1"/>
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//6<entry  name="vopenid"            type="string"		size="64"							desc="(必填)用户OPENID号" />
,Level//7<entry  name="Level"             type="int"												desc="(必填)等级" />
,PlayerFriendsNum//8<entry  name="PlayerFriendsNum"  type="int"												desc="(必填)玩家好友数量"/>
,ClientVersion//9<entry  name="ClientVersion"		type="string"		size="64"	defaultvalue="NULL"		desc="(必填)客户端版本"/>
,SystemSoftware//10<entry  name="SystemSoftware"	type="string"		size="64"	defaultvalue="NULL"		desc="(必填)移动终端操作系统版本"/>
,SystemHardware//11<entry  name="SystemHardware"	type="string"		size="64"	defaultvalue="NULL"		desc="(必填)移动终端机型"/>
,TelecomOper//12<entry  name="TelecomOper"		type="string"		size="64"	defaultvalue="NULL"		desc="(必填)运营商"/>
,Network//13<entry  name="Network"			type="string"		size="64"	defaultvalue="NULL"		desc="(必填)3G/WIFI/2G"/>
,ScreenWidth//14<entry  name="ScreenWidth"		type="int"						defaultvalue="0"		desc="(必填)显示屏宽度"/>
,ScreenHight//15<entry  name="ScreenHight"		type="int"						defaultvalue="0"		desc="(必填)显示屏高度"/>
,Density//16<entry  name="Density"			type="float"					defaultvalue="0"		desc="(必填)像素密度"/>
,LoginChannel//17<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
,CpuHardware//18<entry  name="CpuHardware"		type="string"		size="64"	defaultvalue="NULL"		desc="(可选)cpu类型|频率|核数"/>
,Memory//19<entry  name="Memory"			type="int"						defaultvalue="0"		desc="(可选)内存信息单位M"/>
,GLRender//20<entry  name="GLRender"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl render信息"/>
,GLVersion//21<entry  name="GLVersion"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl版本信息"/>
,DeviceId//22<entry  name="DeviceId"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)设备ID"/>
,RoleId//23<entry  name="RoleId"			type="string"		size="64"						desc="(必填)角色ID" />
);//</struct>
	}
//
//
//
//<!--//////////////////////////////////////////////
/////////玩家Demo 表///////////////////////////////
///////////////////////////////////////////////////-->
//
//<macrosgroup name="Reason">
//<macro       value="0" desc="启动游戏      stEventTime必填 ltEventTime cteEventTime itEventTime不必填"/>
//<macro       value="1" desc="资源加载完成  stEventTime ltEventTime 必填 cteEventTime itEventTime不必填"/>
//<macro       value="2" desc="创建角色      stEventTime ltEventTime cteEventTime 必填 itEventTime不必填"/>
//<macro       value="3" desc="进入游戏      stEventTime ltEventTime cteEventTime itEventTime 必填"/>
//
//</macrosgroup>
//
	public void PlayerDemo(long RoleId, String RoleName, String vopenid,String stEventTime,String ltEventTime,String ctEventTime,String itEventTime,String vGameAppid,int PlatID,int Reason,String ClientVersion,String SystemSoftware,String SystemHardware,String TelecomOper,String Network,int ScreenWidth,int ScreenHight,float Density,int LoginChannel,String CpuHardware,int Memory,String GLRender,String GLVersion,String DeviceId){
//</struct>
		PlayerDemo.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//PlayerDemo.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"		size="25"							desc="(必填)登录的游戏服务器编号" />
,stEventTime//2<entry  name="stEventTime"		type="datetime"											desc="(必填)启动游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,ltEventTime//3<entry  name="ltEventTime"		type="datetime"											desc="(可选)加载事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,ctEventTime//4<entry  name="ctEventTime"		type="datetime"											desc="(可选)创建事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,itEventTime//5<entry  name="itEventTime"		type="datetime"											desc="(可选)进入游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,vGameAppid//6<entry  name="vGameAppid"         type="string"		size="32"							desc="(必填)游戏APPID" />
,PlatID//7<entry  name="PlatID"			type="int"						defaultvalue="0"		desc="(必填)ios 0/android 1"/>
,ZoneID//8<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,vopenid//9<entry  name="vopenid"            type="string"		size="64"							desc="(可选)用户OPENID号" />
,Reason//10<entry  name="Reason"           type="int"       									desc="(必填)操作原因" />
,ClientVersion//11<entry  name="ClientVersion"		type="string"		size="64"	defaultvalue="NULL"		desc="(必填)客户端版本"/>
,SystemSoftware//12<entry  name="SystemSoftware"	type="string"		size="64"	defaultvalue="NULL"		desc="(必填)移动终端操作系统版本"/>
,SystemHardware//13<entry  name="SystemHardware"	type="string"		size="64"	defaultvalue="NULL"		desc="(必填)移动终端机型"/>
,TelecomOper//14<entry  name="TelecomOper"		type="string"		size="64"	defaultvalue="NULL"		desc="(必填)运营商"/>
,Network//15<entry  name="Network"			type="string"		size="64"	defaultvalue="NULL"		desc="(必填)3G/WIFI/2G"/>
,ScreenWidth//16<entry  name="ScreenWidth"		type="int"						defaultvalue="0"		desc="(必填)显示屏宽度"/>
,ScreenHight//17<entry  name="ScreenHight"		type="int"						defaultvalue="0"		desc="(必填)显示屏高度"/>
,Density//18<entry  name="Density"			type="float"					defaultvalue="0"		desc="(必填)像素密度"/>
,LoginChannel//19<entry  name="LoginChannel"		type="int"						defaultvalue="0"		desc="(必填)登录渠道"/>
,CpuHardware//20<entry  name="CpuHardware"		type="string"		size="64"	defaultvalue="NULL"		desc="(可选)cpu类型|频率|核数"/>
,Memory//21<entry  name="Memory"			type="int"						defaultvalue="0"		desc="(可选)内存信息单位M"/>
,GLRender//22<entry  name="GLRender"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl render信息"/>
,GLVersion//23<entry  name="GLVersion"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)opengl版本信息"/>
,DeviceId//24<entry  name="DeviceId"			type="string"		size="64"	defaultvalue="NULL"		desc="(可选)设备ID"/>
);//</struct>
	}
//
//</metalib>
	public static Logger GameSvrState = LoggerFactory.getLogger("GameSvrState");
	public static Logger PlayerOnline = LoggerFactory.getLogger("PlayerOnline");
	public static Logger PlayerRegister = LoggerFactory.getLogger("PlayerRegister");
	public static Logger PlayerLogin = LoggerFactory.getLogger("PlayerLogin");
	public static Logger PlayerLogout = LoggerFactory.getLogger("PlayerLogout");
	public static Logger MoneyFlow = LoggerFactory.getLogger("MoneyFlow");
	public static Logger ItemFlow = LoggerFactory.getLogger("ItemFlow");
	public static Logger PlayerExpFlow = LoggerFactory.getLogger("PlayerExpFlow");
	public static Logger SnsFlow = LoggerFactory.getLogger("SnsFlow");
	public static Logger RoundFlow = LoggerFactory.getLogger("RoundFlow");
	public static Logger PlayerCrash = LoggerFactory.getLogger("PlayerCrash");
	public static Logger PlayerDemo = LoggerFactory.getLogger("PlayerDemo");

public int GameSvrId = 1; 
public int ZoneID = 1; 
public SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
/*
  <!-- ===GameSvrState===== -->
	<appender name="GameSvrState" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/GameSvrState.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/GameSvrState.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="GameSvrState" level="INFO" additivity="false">
  	<appender-ref ref="GameSvrState"></appender-ref>
  </logger>
  <!-- ===PlayerOnline===== -->
	<appender name="PlayerOnline" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/PlayerOnline.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/PlayerOnline.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="PlayerOnline" level="INFO" additivity="false">
  	<appender-ref ref="PlayerOnline"></appender-ref>
  </logger>
  <!-- ===PlayerRegister===== -->
	<appender name="PlayerRegister" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/PlayerRegister.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/PlayerRegister.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="PlayerRegister" level="INFO" additivity="false">
  	<appender-ref ref="PlayerRegister"></appender-ref>
  </logger>
  <!-- ===PlayerLogin===== -->
	<appender name="PlayerLogin" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/PlayerLogin.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/PlayerLogin.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="PlayerLogin" level="INFO" additivity="false">
  	<appender-ref ref="PlayerLogin"></appender-ref>
  </logger>
  <!-- ===PlayerLogout===== -->
	<appender name="PlayerLogout" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/PlayerLogout.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/PlayerLogout.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="PlayerLogout" level="INFO" additivity="false">
  	<appender-ref ref="PlayerLogout"></appender-ref>
  </logger>
  <!-- ===MoneyFlow===== -->
	<appender name="MoneyFlow" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/MoneyFlow.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/MoneyFlow.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="MoneyFlow" level="INFO" additivity="false">
  	<appender-ref ref="MoneyFlow"></appender-ref>
  </logger>
  <!-- ===ItemFlow===== -->
	<appender name="ItemFlow" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/ItemFlow.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/ItemFlow.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="ItemFlow" level="INFO" additivity="false">
  	<appender-ref ref="ItemFlow"></appender-ref>
  </logger>
  <!-- ===PlayerExpFlow===== -->
	<appender name="PlayerExpFlow" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/PlayerExpFlow.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/PlayerExpFlow.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="PlayerExpFlow" level="INFO" additivity="false">
  	<appender-ref ref="PlayerExpFlow"></appender-ref>
  </logger>
  <!-- ===SnsFlow===== -->
	<appender name="SnsFlow" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/SnsFlow.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/SnsFlow.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="SnsFlow" level="INFO" additivity="false">
  	<appender-ref ref="SnsFlow"></appender-ref>
  </logger>
  <!-- ===RoundFlow===== -->
	<appender name="RoundFlow" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/RoundFlow.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/RoundFlow.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="RoundFlow" level="INFO" additivity="false">
  	<appender-ref ref="RoundFlow"></appender-ref>
  </logger>
  <!-- ===PlayerCrash===== -->
	<appender name="PlayerCrash" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/PlayerCrash.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/PlayerCrash.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="PlayerCrash" level="INFO" additivity="false">
  	<appender-ref ref="PlayerCrash"></appender-ref>
  </logger>
  <!-- ===PlayerDemo===== -->
	<appender name="PlayerDemo" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/PlayerDemo.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/PlayerDemo.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="PlayerDemo" level="INFO" additivity="false">
  	<appender-ref ref="PlayerDemo"></appender-ref>
  </logger>
*/
