package log;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.json.JSONArray;
public class gen {
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
//<!--//////////////////////////////////////////////
/////////君主改名日志///////////////////////////////
///////////////////////////////////////////////////-->
	public void KingChange(long RoleId, String RoleName, String vopenid,String oldName,String newName){
//</struct>
		KingChange.info("{},{},{},{},{},{},{}"
		//KingChange.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"		size="25"							desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="int"								desc="(必填) 角色id" />
,oldName//4<entry  name="oldName"			type="string"		size="64"						desc="(必填) 旧名称" />
,newName//5<entry  name="newName"			type="string"		size="64"						desc="(必填)新名称" />
,fmt.format(Calendar.getInstance().getTime())//6<entry  name="dtEventTime"			type="string"		size="64"						desc="(必填)升级时间 格式 YYYY-MM-DD HH:MM:SS" />
,ZoneID//7<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////君主升级日志///////////////////////////////
///////////////////////////////////////////////////-->
	public void KingLvup(long RoleId, String RoleName, String vopenid,String Level,String Exp){
//</struct>
		KingLvup.info("{},{},{},{},{},{},{},{}"
		//KingLvup.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"		size="25"							desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="int"								desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,Level//5<entry  name="Level"			type="string"		size="64"						desc="(必填)角色升级后等级" />
,Exp//6<entry  name="Exp"			type="string"		size="64"						desc="(必填)升级后剩余经验值" />
,fmt.format(Calendar.getInstance().getTime())//7<entry  name="dtEventTime"			type="string"		size="64"						desc="(必填)升级时间 格式 YYYY-MM-DD HH:MM:SS" />
,ZoneID//8<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////经验获得日志///////////////////////////////
///////////////////////////////////////////////////-->
	public void GetExp(long RoleId, String RoleName, String vopenid,int Reason,String Num,int CurNum){
//</struct>
		GetExp.info("{},{},{},{},{},{},{},{},{}"
		//GetExp.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"		size="25"							desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"							desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Reason//6<entry  name="Reason"			type="int"						defaultvalue="0"	desc="(必填)经验获得方式"/>
,Num//7<entry  name="Num"			type="string"		size="25"	desc="(必填)获得数量" />
,CurNum//8<entry  name="CurNum"		type="int"						defaultvalue="0"		desc="(必填)当前经验值"/>
,fmt.format(Calendar.getInstance().getTime())//9<entry  name="dtEventTime"		type="datetime"					desc="(必填) 经验获得时间 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////君主天赋日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="VigourType">
//<macro name="svigour"       value="1" desc="武艺精气"/>
//<macro name="bvigour"      value="2" desc="体魄精气"/>
//</macrosgroup>
//
	public void KingTalent(long RoleId, String RoleName, String vopenid,int Talentid,String Talents,int Level,int VigourType,int Num){
//</struct>
		KingTalent.info("{},{},{},{},{},{},{},{},{},{},{}"
		//KingTalent.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"		size="25"							desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"							desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Talentid//6<entry  name="Talentid"			type="int"						defaultvalue="0"	desc="(必填)天赋id"/>
,Talents//7<entry  name="Talents"			type="string"	size="64"					defaultvalue="0"	desc="(必填)天赋名称"/>
,Level//8<entry  name="Level"			type="int"			desc="(必填)升级后等级" />
,VigourType//9<entry  name="VigourType"		type="int"						defaultvalue="0"		desc="(必填)升级消耗 1：武艺精气 2：体魄精气"/>
,Num//10<entry  name="Num"		type="int"						defaultvalue="0"		desc="(必填)消耗数量"/>
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"		type="datetime"					desc="(必填) 经验获得时间 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//
//<!--//////////////////////////////////////////////
/////////装备进阶日志///////////////////////////////
///////////////////////////////////////////////////-->
//
	public void EquipLvup(long RoleId, String RoleName, String vopenid,int EquipId,String Equips,int oldEquipId,String oldEquips,String iGoods,int iGoodsnum){
//</struct>
		EquipLvup.info("{},{},{},{},{},{},{},{},{},{},{},{}"
		//EquipLvup.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,EquipId//6<entry  name="EquipId"			type="int"								desc="(必填) 新装备id" />
,Equips//7<entry  name="Equips"			type="string"		size="64"						desc="(必填) 新装备名称" />
,oldEquipId//8<entry  name="oldEquipId"			type="int"								desc="(必填) 原装备id" />
,oldEquips//9<entry  name="oldEquips"			type="string"		size="64"						desc="(必填) 原装备名称" />
,iGoods//10<entry  name="iGoods"			type="string"		size="64"							desc="(必填) 消耗道具名称" />
,iGoodsnum//11<entry  name="iGoodsnum"			type="int"								desc="(必填) 消耗道具数量" />
,fmt.format(Calendar.getInstance().getTime())//12<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////装备强化///////////////////////////////
///////////////////////////////////////////////////-->
	public void EquipStrength(long RoleId, String RoleName, String vopenid,int EquipId,String Equips,int BeforeLevel,int EquipLevel,JSONArray Consumes){
//</struct>
		EquipStrength.info("{},{},{},{},{},{},{},{},{},{},{}"
		//EquipStrength.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,EquipId//6<entry  name="EquipId"			type="int"								desc="(必填) 装备id" />
,Equips//7<entry  name="Equips"			type="string"		size="64"						desc="(必填) 装备名称" />
,BeforeLevel//8<entry  name="BeforeLevel"			type="int"								desc="(必填) 强化前等级" />
,EquipLevel//9<entry  name="EquipLevel"			type="int"								desc="(必填) 强化后等级" />
,Consumes//10<entry  name="Consumes"			type="text"					desc='(必填) 消耗道具名称和数量 以json方式存储[{"name":"**","num":"2"},{"name":"*","num":"2"}...] ' />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//
//<!--//////////////////////////////////////////////
/////////装备洗练///////////////////////////////
///////////////////////////////////////////////////-->
	public void EquipRefine(long RoleId, String RoleName, String vopenid,int EquipId,String Equips,String BeforeAttr,String Attr,int Money){
//</struct>
		EquipRefine.info("{},{},{},{},{},{},{},{},{},{},{}"
		//EquipRefine.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"				desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,EquipId//6<entry  name="EquipId"			type="int"								desc="(必填) 装备id" />
,Equips//7<entry  name="Equips"			type="string"		size="64"						desc="(必填) 装备名称" />
,BeforeAttr//8<entry  name="BeforeAttr"			type="string"	size="64"								desc="(必填) 洗练前属性" />
,Attr//9<entry  name="Attr"			type="string"		size="64"							desc="(必填) 洗练后属性" />
,Money//10<entry  name="Money"			type="int"								desc="(必填) 消耗元宝数量" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////商铺购买日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="LogType">
//<macro name="vcoin"       value="1" desc="元宝购买"/>
//<macro name="contribute"      value="2" desc="贡献购买"/>
//</macrosgroup>
	public void ShopBuy(long RoleId, String RoleName, String vopenid,int LogType,int iGoodsid,String iGoods,int BuyNum,int Money,int RemainMoney){
//</struct>
		ShopBuy.info("{},{},{},{},{},{},{},{},{},{},{},{}"
		//ShopBuy.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"        size="25"							desc="(必填)登录的游戏服务器编号" />
,LogType//2<entry  name="LogType"			type="int"								desc="(必填)log类型 1：元宝够买  2：贡献购买" />
,vopenid//3<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//4<entry  name="RoleId"			type="string"		size="64"						desc="(必填) 角色id" />
,RoleName//5<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//6<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,iGoodsid//7<entry  name="iGoodsid"			type="int"								desc="(必填) 购买物品id" />
,iGoods//8<entry  name="iGoods"			type="string"		size="64"						desc="(必填) 购买物品名称" />
,BuyNum//9<entry  name="BuyNum"			type="int"								desc="(必填) 购买数量" />
,Money//10<entry  name="Money"			type="int"								desc="(必填) 消耗元宝/贡献 数量" />
,RemainMoney//11<entry  name="RemainMoney"			type="int"								desc="(必填) 剩余元宝/贡献 数量" />
,fmt.format(Calendar.getInstance().getTime())//12<entry  name="dtEventTime"       type="datetime"											desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////典当物品日志///////////////////////////////
///////////////////////////////////////////////////-->
	public void Pawn(long RoleId, String RoleName, String vopenid,int iGoodsid,String iGoods,int Num,int Renum,int Money){
//</struct>
		Pawn.info("{},{},{},{},{},{},{},{},{},{},{}"
		//Pawn.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"         type="string"        size="25"							desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"						desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,iGoodsid//6<entry  name="iGoodsid"			type="int"								desc="(必填) 典当物品id" />
,iGoods//7<entry  name="iGoods"			type="string"		size="64"						desc="(必填) 典当物品名称" />
,Num//8<entry  name="Num"			type="int"								desc="(必填) 典当数量" />
,Renum//9<entry  name="Renum"			type="int"								desc="(必填)典当后剩余物品数量" />
,Money//10<entry  name="Money"			type="int"								desc="(必填) 获得元宝数量" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"       type="datetime"											desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////体力日志///////////////////////////////
///////////////////////////////////////////////////-->
	public void PhysicalPower(long RoleId, String RoleName, String vopenid,int OldPower,int Num,int Power,String Reason){
//</struct>
		PhysicalPower.info("{},{},{},{},{},{},{},{},{},{}"
		//PhysicalPower.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"				desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,OldPower//6<entry  name="OldPower"			type="int"								desc="(必填) 原体力" />
,Num//7<entry  name="Num"			type="int"								desc="(必填) 增加数量" />
,Power//8<entry  name="Power"			type="int"								desc="(必填) 增加后体力数量" />
,Reason//9<entry  name="Reason"			type="string"		size="64"						desc="(必填) 增加体力方式" />
,fmt.format(Calendar.getInstance().getTime())//10<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////邮件日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="LogType">
//<macro name="system"       value="1" desc="系统邮件"/>
//<macro name="player"      value="2" desc="玩家邮件"/>
//</macrosgroup>
	public void EmailLog(long RoleId, String RoleName, String vopenid,int LogType,String Recopenid,String Recrid,String Recrname,String Title,JSONArray Content,JSONArray Attach){
//</struct>
		EmailLog.info("{},{},{},{},{},{},{},{},{},{},{},{},{}"
		//EmailLog.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,LogType//2<entry  name="LogType"			type="int"								desc="(必填)log类型 1:系统邮件  2:玩家邮件" />
,vopenid//3<entry  name="vopenid"			type="string"		size="64"						desc="(必填)发件人 OPENID号" />
,RoleId//4<entry  name="RoleId"		type="string"		size="64"			desc="(必填) 发件人 角色id" />
,RoleName//5<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 发件人 角色名称" />
,ZoneID//6<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Recopenid//7<entry  name="Recopenid"			type="string"		size="64"						desc="(必填)收件人 OPENID号" />
,Recrid//8<entry  name="Recrid"		type="string"		size="64"			desc="(必填) 收件人 角色id" />
,Recrname//9<entry  name="Recrname"			type="string"		size="64"						desc="(必填) 收件人 角色名称" />
,Title//10<entry  name="Title"			type="string"		size="128"							desc="(必填) 邮件标题" />
,Content//11<entry  name="Content"			type="text"								desc="(必填) 内容" />
,Attach//12<entry  name="Attach"			type="text"							desc='(必填) 附件 以json方式存储[{"name":"**","num":"2"},{"name":"*","num":"2"}...] ' />
,fmt.format(Calendar.getInstance().getTime())//13<entry  name="dtEventTime"        type="datetime"							desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////过关斩将日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="Win">
//<macro name="victory"       value="1" desc="胜利"/>
//<macro name="lose"       value="2" desc="失败"/>
//</macrosgroup>
	public void HeroBattle(long RoleId, String RoleName, String vopenid,int GateId,String Gates,int Win,int Num){
//</struct>
		HeroBattle.info("{},{},{},{},{},{},{},{},{},{}"
		//HeroBattle.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,GateId//6<entry  name="GateId"			type="int"								desc="(必填) 挑战关卡id" />
,Gates//7<entry  name="Gates"			type="string"		size="64"							desc="(必填) 挑战关卡名称" />
,Win//8<entry  name="Win"			type="int"								desc="(必填) 挑战结果 1：胜利  2：失败" />
,Num//9<entry  name="Num"			type="int"								desc="(必填) 挑战次数" />
,fmt.format(Calendar.getInstance().getTime())//10<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////百战千军挑战日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="Win">
//<macro name="victory"       value="1" desc="胜利"/>
//<macro name="lose"       value="2" desc="失败"/>
//</macrosgroup>
	public void Challenge(long RoleId, String RoleName, String vopenid,String OpposName,int OpposId,int Win,int OldRank,int Rank){
//</struct>
		Challenge.info("{},{},{},{},{},{},{},{},{},{},{}"
		//Challenge.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"				desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,OpposName//6<entry  name="OpposName"			type="string"		size="64"						desc="(必填) 挑战君主名称" />
,OpposId//7<entry  name="OpposId"			type="int"								desc="(必填) 挑战君主ID" />
,Win//8<entry  name="Win"			type="int"								desc="(必填) 挑战结果 1：胜利  2：失败" />
,OldRank//9<entry  name="OldRank"			type="int"								desc="(必填) 挑战前排名" />
,Rank//10<entry  name="Rank"			type="int"								desc="(必填) 挑战后排名" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////百战千军领奖日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="Awardtype">
//<macro name="copper"       value="1" desc="铜币"/>
//<macro name="prestige"       value="2" desc="威望"/>
//</macrosgroup>
	public void ChallengeAward(long RoleId, String RoleName, String vopenid,int AwardType,int Num){
//</struct>
		ChallengeAward.info("{},{},{},{},{},{},{},{}"
		//ChallengeAward.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,AwardType//6<entry  name="AwardType"			type="int"								desc="(必填) 奖励类型 1：铜币 2：威望" />
,Num//7<entry  name="Num"			type="int"								desc="(必填) 奖励数量" />
,fmt.format(Calendar.getInstance().getTime())//8<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////百战千军兑换日志///////////////////////////////
///////////////////////////////////////////////////-->
	public void ChallengeExchange(long RoleId, String RoleName, String vopenid,int iGoodsid,String iGoods,int Num,int OldPrestige,int Prestige){
//</struct>
		ChallengeExchange.info("{},{},{},{},{},{},{},{},{},{},{}"
		//ChallengeExchange.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,iGoodsid//6<entry  name="iGoodsid"			type="int"								desc="(必填) 兑换物品id" />
,iGoods//7<entry  name="iGoods"			type="string"		size="64"				desc="(必填) 兑换物品名称" />
,Num//8<entry  name="Num"			type="int"								desc="(必填) 兑换数量" />
,OldPrestige//9<entry  name="OldPrestige"			type="int"								desc="(必填) 兑换前威望" />
,Prestige//10<entry  name="Prestige"			type="int"								desc="(必填) 兑换后威望" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////联盟创建加入离开///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="LogType">
//<macro name="create"       value="CREATE" desc="创建"/>
//<macro name="join "       value="JOIN" desc="加入"/>
//<macro name="out "       value="OUT" desc="离开"/>
//</macrosgroup>
	public void Guild(long RoleId, String RoleName, String vopenid,String LogType,int Guildid,String GuildName,int GuildLv,String Reason){
//</struct>
		Guild.info("{},{},{},{},{},{},{},{},{},{},{}"
		//Guild.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,LogType//2<entry  name="LogType"			type="string"  size="16"								desc="(必填)日志类型 CREATE：创建; JOIN:加入 OUT:离开" />
,vopenid//3<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//4<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//5<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//6<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Guildid//7<entry  name="Guildid"			type="int"								desc="(必填) 联盟id" />
,GuildName//8<entry  name="GuildName"			type="string"		size="64"				desc="(必填) 联盟名称" />
,GuildLv//9<entry  name="GuildLv"			type="int"								desc="(必填) 联盟等级" />
,Reason//10<entry  name="Reason"			type="string"		size="64"							desc="(必填) 离开原因" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////踢出联盟///////////////////////////////
///////////////////////////////////////////////////-->
//
	public void GuildOut(long RoleId, String RoleName, String vopenid,int GuildId,String GuildName,String KickOpenid,String KickrId,String KickrName){
//</struct>
		GuildOut.info("{},{},{},{},{},{},{},{},{},{},{}"
		//GuildOut.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,GuildId//6<entry  name="GuildId"			type="int"								desc="(必填) 联盟id" />
,GuildName//7<entry  name="GuildName"			type="string"		size="64"				desc="(必填) 联盟名称" />
,KickOpenid//8<entry  name="KickOpenid"			type="string"		size="64"						desc="(必填)踢出玩家openid" />
,KickrId//9<entry  name="KickrId"			type="string"		size="64"					desc="(必填) 踢出玩家角色id" />
,KickrName//10<entry  name="KickrName"			type="string"		size="64"						desc="(必填) 踢出玩家角色名称" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////解散联盟///////////////////////////////
///////////////////////////////////////////////////-->
//
	public void GuildBreak(long RoleId, String RoleName, String vopenid,int GuildId,String GuildName,int GuildLv,int GuildExp,int GuildBuild){
//</struct>
		GuildBreak.info("{},{},{},{},{},{},{},{},{},{},{}"
		//GuildBreak.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)用户OPENID号" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,GuildId//6<entry  name="GuildId"			type="int"								desc="(必填) 联盟id" />
,GuildName//7<entry  name="GuildName"			type="string"		size="64"				desc="(必填) 联盟名称" />
,GuildLv//8<entry  name="GuildLv"			type="int"						desc="(必填)联盟等级" />
,GuildExp//9<entry  name="GuildExp"			type="int"						desc="(必填) 联盟经验" />
,GuildBuild//10<entry  name="GuildBuild"			type="int"							desc="(必填)联盟建设" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////转让联盟///////////////////////////////
///////////////////////////////////////////////////-->
//
	public void GuildTransfer(long RoleId, String RoleName, String vopenid,int GuildId,String GuildName,String oldOpenid,String oldRid,String oldRname){
//</struct>
		GuildTransfer.info("{},{},{},{},{},{},{},{},{},{},{}"
		//GuildTransfer.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)盟主 OPENID" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 盟主角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填) 盟主角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,GuildId//6<entry  name="GuildId"			type="int"								desc="(必填) 联盟id" />
,GuildName//7<entry  name="GuildName"			type="string"		size="64"				desc="(必填) 联盟名称" />
,oldOpenid//8<entry  name="oldOpenid"			type="string"		size="64"						desc="(必填)原盟主 OPENID" />
,oldRid//9<entry  name="oldRid"			type="string"		size="64"					desc="(必填) 原盟主角色id" />
,oldRname//10<entry  name="oldRname"			type="string"		size="64"						desc="(必填) 原盟主角色名称" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////联盟捐献///////////////////////////////
///////////////////////////////////////////////////-->
//
	public void GuildDonate(long RoleId, String RoleName, String vopenid,int GuildId,String GuildName,int Tigernum,int Contribute){
//</struct>
		GuildDonate.info("{},{},{},{},{},{},{},{},{},{}"
		//GuildDonate.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)OPENID" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填) 角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填)角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,GuildId//6<entry  name="GuildId"			type="int"								desc="(必填) 联盟id" />
,GuildName//7<entry  name="GuildName"			type="string"		size="64"				desc="(必填) 联盟名称" />
,Tigernum//8<entry  name="Tigernum"			type="int"							desc="(必填)捐献虎符数量" />
,Contribute//9<entry  name="Contribute"			type="int"						desc="(必填) 获得贡献" />
,fmt.format(Calendar.getInstance().getTime())//10<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////膜拜日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="wsType">
//<macro name="normal"       value="1" desc="普通"/>
//<macro name="pious"       value="2" desc="虔诚"/>
//<macro name="bow "       value="3" desc="顶礼"/>
//</macrosgroup>
//
	public void Worship(long RoleId, String RoleName, String vopenid,String wsType,JSONArray Consumes){
//</struct>
		Worship.info("{},{},{},{},{},{},{},{}"
		//Worship.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,wsType//2<entry  name="wsType"          type="string"		  size="25"					desc="(必填)膜拜方式" />
,vopenid//3<entry  name="vopenid"			type="string"		size="64"						desc="(必填) OPENID" />
,RoleId//4<entry  name="RoleId"			type="string"		size="64"					desc="(必填)角色id" />
,RoleName//5<entry  name="RoleName"			type="string"		size="64"						desc="(必填)角色名称" />
,ZoneID//6<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Consumes//7<entry  name="Consumes"			type="text"					desc='(必填) 消耗道具名称和数量 以json方式存储[{"name":"**","num":"2"},{"name":"*","num":"2"}...] ' />
,fmt.format(Calendar.getInstance().getTime())//8<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////每日任务日志///////////////////////////////
///////////////////////////////////////////////////-->
	public void DailyTask(long RoleId, String RoleName, String vopenid,String Task,int Taskid){
//</struct>
		DailyTask.info("{},{},{},{},{},{},{},{}"
		//DailyTask.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填) OPENID" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填)角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填)角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Task//6<entry  name="Task"			type="string"		size="64"							desc="(必填) 完成任务名称" />
,Taskid//7<entry  name="Taskid"			type="int"								desc="(必填) 完成任务id" />
,fmt.format(Calendar.getInstance().getTime())//8<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////洗劫权贵日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="Win">
//<macro name="victory"       value="1" desc="胜利"/>
//<macro name="lose"       value="2" desc="失败"/>
//</macrosgroup>
	public void LootRich(long RoleId, String RoleName, String vopenid,String Gates,int GateId,int Win,int Count,int Copper){
//</struct>
		LootRich.info("{},{},{},{},{},{},{},{},{},{},{}"
		//LootRich.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填) OPENID" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填)角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填)角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Gates//6<entry  name="Gates"			type="string"		size="64"							desc="(必填) 挑战关卡名称" />
,GateId//7<entry  name="GateId"			type="int"								desc="(必填) 挑战关卡id" />
,Win//8<entry  name="Win"			type="int"								desc="(必填) 挑战结果 1：胜利  2：失败" />
,Count//9<entry  name="Count"			type="int"								desc="(必填) 挑战次数" />
,Copper//10<entry  name="Copper"			type="int"								desc="(必填) 收益铜币数量" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////讨伐山贼日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="Win">
//<macro name="victory"       value="1" desc="胜利"/>
//<macro name="lose"       value="2" desc="失败"/>
//</macrosgroup>
//<macrosgroup name="AwardType">
//<macro name="crys"       value="1" desc="陨铁仙晶"/>
//<macro name="spirit"       value="2" desc="陨铁真元"/>
//</macrosgroup>
	public void KillRobber(long RoleId, String RoleName, String vopenid,String Gates,int Gateid,int Win,int Count,int Awardtype,int Num){
//</struct>
		KillRobber.info("{},{},{},{},{},{},{},{},{},{},{},{}"
		//KillRobber.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填) OPENID" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填)角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填)角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Gates//6<entry  name="Gates"			type="string"		size="64"							desc="(必填) 挑战关卡名称" />
,Gateid//7<entry  name="Gateid"			type="int"								desc="(必填) 挑战关卡id" />
,Win//8<entry  name="Win"			type="int"								desc="(必填) 挑战结果 1：胜利  2：失败" />
,Count//9<entry  name="Count"			type="int"								desc="(必填) 挑战次数" />
,Awardtype//10<entry  name="Awardtype"			type="int"								desc="(必填) 收益类型 1：陨铁仙晶 2：陨铁真元" />
,Num//11<entry  name="Num"			type="int"								desc="(必填) 收益数量" />
,fmt.format(Calendar.getInstance().getTime())//12<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////剿灭叛军日志///////////////////////////////
///////////////////////////////////////////////////-->
//<macrosgroup name="Win">
//<macro name="victory"       value="1" desc="胜利"/>
//<macro name="lose"       value="2" desc="失败"/>
//</macrosgroup>
//<macrosgroup name="Awardtype">
//<macro name="svigour"       value="1" desc="武艺精气"/>
//<macro name="bvigour"      value="2" desc="体魄精气"/>
//</macrosgroup>
	public void KillRebelArmy(long RoleId, String RoleName, String vopenid,String Gates,int GateId,int Win,int Count,int AwardType,int Num){
//</struct>
		KillRebelArmy.info("{},{},{},{},{},{},{},{},{},{},{},{}"
		//KillRebelArmy.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填) OPENID" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填)角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填)角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,Gates//6<entry  name="Gates"			type="string"		size="64"							desc="(必填) 挑战关卡名称" />
,GateId//7<entry  name="GateId"			type="int"								desc="(必填) 挑战关卡id" />
,Win//8<entry  name="Win"			type="int"								desc="(必填) 挑战结果 1：胜利  2：失败" />
,Count//9<entry  name="Count"			type="int"								desc="(必填) 挑战次数" />
,AwardType//10<entry  name="AwardType"			type="int"								desc="(必填) 收益类型 1:武艺精气 2：体魄精气" />
,Num//11<entry  name="Num"			type="int"								desc="(必填) 收益数量" />
,fmt.format(Calendar.getInstance().getTime())//12<entry  name="dtEventTime"        type="datetime"										desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////运镖日志//////////////////////////////
///////////////////////////////////////////////////-->
	public void ConveyDart(long RoleId, String RoleName, String vopenid,int DartType,int Copper,int Count){
//</struct>
		ConveyDart.info("{},{},{},{},{},{},{},{},{}"
		//ConveyDart.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填) OPENID" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填)角色id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填)角色名称" />
,ZoneID//5<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
,DartType//6<entry  name="DartType"			type="int"							desc="(必填) 运镖品质" />
,Copper//7<entry  name="Copper"			type="int"								desc="(必填) 收益铜币数量" />
,Count//8<entry  name="Count"			type="int"								desc="(必填) 运镖次数" />
,fmt.format(Calendar.getInstance().getTime())//9<entry  name="dtEventTime"        type="datetime"				desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
);//</struct>
	}
//
//
//<!--//////////////////////////////////////////////
/////////劫镖日志//////////////////////////////
///////////////////////////////////////////////////-->
	public void LootDart(long RoleId, String RoleName, String vopenid,String RobedOpenid,String RobedRid,String Robedrname,int Copper,int Count){
//</struct>
		LootDart.info("{},{},{},{},{},{},{},{},{},{},{}"
		//LootDart.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,vopenid//2<entry  name="vopenid"			type="string"		size="64"						desc="(必填)劫镖君主 OPENID" />
,RoleId//3<entry  name="RoleId"			type="string"		size="64"					desc="(必填)劫镖君主id" />
,RoleName//4<entry  name="RoleName"			type="string"		size="64"						desc="(必填)劫镖君主名称" />
,RobedOpenid//5<entry  name="RobedOpenid"			type="string"		size="64"						desc="(必填)被劫镖君主 OPENID" />
,RobedRid//6<entry  name="RobedRid"			type="string"		size="64"					desc="(必填)被劫镖君主id" />
,Robedrname//7<entry  name="Robedrname"			type="string"		size="64"						desc="(必填)被劫镖君主名称" />
,Copper//8<entry  name="Copper"			type="int"								desc="(必填) 收益 铜币数量" />
,Count//9<entry  name="Count"			type="int"								desc="(必填) 劫镖次数" />
,fmt.format(Calendar.getInstance().getTime())//10<entry  name="dtEventTime"        type="datetime"				desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,ZoneID//11<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
);//</struct>
	}
//
//<!--//////////////////////////////////////////////
/////////探宝日志//////////////////////////////
///////////////////////////////////////////////////-->
	public void FineGem(long RoleId, String RoleName, String vopenid,int SeekType,int iGoodsid,String iGoods,int Num,String DeliGoods,int DelNum){
//</struct>
		FineGem.info("{},{},{},{},{},{},{},{},{},{},{},{}"
		//FineGem.info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30
,GameSvrId//1<entry  name="GameSvrId"          type="string"		  size="25"					desc="(必填)登录的游戏服务器编号" />
,SeekType//2<entry  name="SeekType"			type="int"							desc="(必填) 探宝方式" />
,vopenid//3<entry  name="vopenid"			type="string"		size="64"						desc="(必填)君主 OPENID" />
,RoleId//4<entry  name="RoleId"			type="string"		size="64"					desc="(必填)君主id" />
,RoleName//5<entry  name="RoleName"			type="string"		size="64"						desc="(必填)君主名称" />
,iGoodsid//6<entry  name="iGoodsid"			type="int"								desc="(必填) 获得 物品id" />
,iGoods//7<entry  name="iGoods"			type="string"		size="64"				desc="(必填) 获得 物品名称" />
,Num//8<entry  name="Num"			type="int"								desc="(必填) 获得 物品数量" />
,DeliGoods//9<entry  name="DeliGoods"			type="string"		size="64"				desc="(必填) 消耗 物品名称" />
,DelNum//10<entry  name="DelNum"			type="int"								desc="(必填) 消耗 物品数量" />
,fmt.format(Calendar.getInstance().getTime())//11<entry  name="dtEventTime"        type="datetime"				desc="(必填)游戏事件的时间, 格式 YYYY-MM-DD HH:MM:SS" />
,ZoneID//12<entry  name="ZoneID"			type="int"						defaultvalue="0"	desc="(必填)针对分区分服的游戏填写分区id，用来唯一标示一个区；非分区分服游戏请填写0"/>
);//</struct>
	}
//
//</metalib>
	public static Logger KingChange = LoggerFactory.getLogger("KingChange");
	public static Logger KingLvup = LoggerFactory.getLogger("KingLvup");
	public static Logger GetExp = LoggerFactory.getLogger("GetExp");
	public static Logger KingTalent = LoggerFactory.getLogger("KingTalent");
	public static Logger EquipLvup = LoggerFactory.getLogger("EquipLvup");
	public static Logger EquipStrength = LoggerFactory.getLogger("EquipStrength");
	public static Logger EquipRefine = LoggerFactory.getLogger("EquipRefine");
	public static Logger ShopBuy = LoggerFactory.getLogger("ShopBuy");
	public static Logger Pawn = LoggerFactory.getLogger("Pawn");
	public static Logger PhysicalPower = LoggerFactory.getLogger("PhysicalPower");
	public static Logger EmailLog = LoggerFactory.getLogger("EmailLog");
	public static Logger HeroBattle = LoggerFactory.getLogger("HeroBattle");
	public static Logger Challenge = LoggerFactory.getLogger("Challenge");
	public static Logger ChallengeAward = LoggerFactory.getLogger("ChallengeAward");
	public static Logger ChallengeExchange = LoggerFactory.getLogger("ChallengeExchange");
	public static Logger Guild = LoggerFactory.getLogger("Guild");
	public static Logger GuildOut = LoggerFactory.getLogger("GuildOut");
	public static Logger GuildBreak = LoggerFactory.getLogger("GuildBreak");
	public static Logger GuildTransfer = LoggerFactory.getLogger("GuildTransfer");
	public static Logger GuildDonate = LoggerFactory.getLogger("GuildDonate");
	public static Logger Worship = LoggerFactory.getLogger("Worship");
	public static Logger DailyTask = LoggerFactory.getLogger("DailyTask");
	public static Logger LootRich = LoggerFactory.getLogger("LootRich");
	public static Logger KillRobber = LoggerFactory.getLogger("KillRobber");
	public static Logger KillRebelArmy = LoggerFactory.getLogger("KillRebelArmy");
	public static Logger ConveyDart = LoggerFactory.getLogger("ConveyDart");
	public static Logger LootDart = LoggerFactory.getLogger("LootDart");
	public static Logger FineGem = LoggerFactory.getLogger("FineGem");

public int GameSvrId = 1; 
public int ZoneID = 1; 
public SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
/*
  <!-- ===KingChange===== -->
	<appender name="KingChange" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/KingChange.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/KingChange.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="KingChange" level="INFO" additivity="false">
  	<appender-ref ref="KingChange"></appender-ref>
  </logger>
  <!-- ===KingLvup===== -->
	<appender name="KingLvup" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/KingLvup.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/KingLvup.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="KingLvup" level="INFO" additivity="false">
  	<appender-ref ref="KingLvup"></appender-ref>
  </logger>
  <!-- ===GetExp===== -->
	<appender name="GetExp" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/GetExp.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/GetExp.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="GetExp" level="INFO" additivity="false">
  	<appender-ref ref="GetExp"></appender-ref>
  </logger>
  <!-- ===KingTalent===== -->
	<appender name="KingTalent" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/KingTalent.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/KingTalent.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="KingTalent" level="INFO" additivity="false">
  	<appender-ref ref="KingTalent"></appender-ref>
  </logger>
  <!-- ===EquipLvup===== -->
	<appender name="EquipLvup" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/EquipLvup.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/EquipLvup.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="EquipLvup" level="INFO" additivity="false">
  	<appender-ref ref="EquipLvup"></appender-ref>
  </logger>
  <!-- ===EquipStrength===== -->
	<appender name="EquipStrength" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/EquipStrength.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/EquipStrength.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="EquipStrength" level="INFO" additivity="false">
  	<appender-ref ref="EquipStrength"></appender-ref>
  </logger>
  <!-- ===EquipRefine===== -->
	<appender name="EquipRefine" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/EquipRefine.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/EquipRefine.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="EquipRefine" level="INFO" additivity="false">
  	<appender-ref ref="EquipRefine"></appender-ref>
  </logger>
  <!-- ===ShopBuy===== -->
	<appender name="ShopBuy" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/ShopBuy.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/ShopBuy.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="ShopBuy" level="INFO" additivity="false">
  	<appender-ref ref="ShopBuy"></appender-ref>
  </logger>
  <!-- ===Pawn===== -->
	<appender name="Pawn" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/Pawn.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/Pawn.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="Pawn" level="INFO" additivity="false">
  	<appender-ref ref="Pawn"></appender-ref>
  </logger>
  <!-- ===PhysicalPower===== -->
	<appender name="PhysicalPower" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/PhysicalPower.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/PhysicalPower.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="PhysicalPower" level="INFO" additivity="false">
  	<appender-ref ref="PhysicalPower"></appender-ref>
  </logger>
  <!-- ===EmailLog===== -->
	<appender name="EmailLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/EmailLog.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/EmailLog.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="EmailLog" level="INFO" additivity="false">
  	<appender-ref ref="EmailLog"></appender-ref>
  </logger>
  <!-- ===HeroBattle===== -->
	<appender name="HeroBattle" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/HeroBattle.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/HeroBattle.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="HeroBattle" level="INFO" additivity="false">
  	<appender-ref ref="HeroBattle"></appender-ref>
  </logger>
  <!-- ===Challenge===== -->
	<appender name="Challenge" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/Challenge.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/Challenge.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="Challenge" level="INFO" additivity="false">
  	<appender-ref ref="Challenge"></appender-ref>
  </logger>
  <!-- ===ChallengeAward===== -->
	<appender name="ChallengeAward" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/ChallengeAward.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/ChallengeAward.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="ChallengeAward" level="INFO" additivity="false">
  	<appender-ref ref="ChallengeAward"></appender-ref>
  </logger>
  <!-- ===ChallengeExchange===== -->
	<appender name="ChallengeExchange" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/ChallengeExchange.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/ChallengeExchange.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="ChallengeExchange" level="INFO" additivity="false">
  	<appender-ref ref="ChallengeExchange"></appender-ref>
  </logger>
  <!-- ===Guild===== -->
	<appender name="Guild" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/Guild.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/Guild.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="Guild" level="INFO" additivity="false">
  	<appender-ref ref="Guild"></appender-ref>
  </logger>
  <!-- ===GuildOut===== -->
	<appender name="GuildOut" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/GuildOut.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/GuildOut.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="GuildOut" level="INFO" additivity="false">
  	<appender-ref ref="GuildOut"></appender-ref>
  </logger>
  <!-- ===GuildBreak===== -->
	<appender name="GuildBreak" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/GuildBreak.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/GuildBreak.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="GuildBreak" level="INFO" additivity="false">
  	<appender-ref ref="GuildBreak"></appender-ref>
  </logger>
  <!-- ===GuildTransfer===== -->
	<appender name="GuildTransfer" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/GuildTransfer.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/GuildTransfer.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="GuildTransfer" level="INFO" additivity="false">
  	<appender-ref ref="GuildTransfer"></appender-ref>
  </logger>
  <!-- ===GuildDonate===== -->
	<appender name="GuildDonate" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/GuildDonate.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/GuildDonate.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="GuildDonate" level="INFO" additivity="false">
  	<appender-ref ref="GuildDonate"></appender-ref>
  </logger>
  <!-- ===Worship===== -->
	<appender name="Worship" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/Worship.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/Worship.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="Worship" level="INFO" additivity="false">
  	<appender-ref ref="Worship"></appender-ref>
  </logger>
  <!-- ===DailyTask===== -->
	<appender name="DailyTask" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/DailyTask.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/DailyTask.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="DailyTask" level="INFO" additivity="false">
  	<appender-ref ref="DailyTask"></appender-ref>
  </logger>
  <!-- ===LootRich===== -->
	<appender name="LootRich" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/LootRich.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/LootRich.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="LootRich" level="INFO" additivity="false">
  	<appender-ref ref="LootRich"></appender-ref>
  </logger>
  <!-- ===KillRobber===== -->
	<appender name="KillRobber" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/KillRobber.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/KillRobber.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="KillRobber" level="INFO" additivity="false">
  	<appender-ref ref="KillRobber"></appender-ref>
  </logger>
  <!-- ===KillRebelArmy===== -->
	<appender name="KillRebelArmy" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/KillRebelArmy.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/KillRebelArmy.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="KillRebelArmy" level="INFO" additivity="false">
  	<appender-ref ref="KillRebelArmy"></appender-ref>
  </logger>
  <!-- ===ConveyDart===== -->
	<appender name="ConveyDart" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/ConveyDart.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/ConveyDart.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="ConveyDart" level="INFO" additivity="false">
  	<appender-ref ref="ConveyDart"></appender-ref>
  </logger>
  <!-- ===LootDart===== -->
	<appender name="LootDart" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/LootDart.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/LootDart.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="LootDart" level="INFO" additivity="false">
  	<appender-ref ref="LootDart"></appender-ref>
  </logger>
  <!-- ===FineGem===== -->
	<appender name="FineGem" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${GAME_LOG_PATH}/FineGem.log</file>
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${GAME_LOG_PATH}/FineGem.%d{yyyy-MM-dd_HH}.log</fileNamePattern>
    </rollingPolicy>
  </appender>
  <logger name="FineGem" level="INFO" additivity="false">
  	<appender-ref ref="FineGem"></appender-ref>
  </logger>
*/
