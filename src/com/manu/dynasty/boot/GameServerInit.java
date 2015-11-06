package com.manu.dynasty.boot;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AccountProtos.Account;
import qxmobile.protobuf.BattleItemInfoProtos.BattleItemInfo;
import qxmobile.protobuf.BattleItemInfoProtos.BattleItemInfoList;
import qxmobile.protobuf.BuildingProtos.Buildings;
import qxmobile.protobuf.BuildingProtos.Farm;
import qxmobile.protobuf.FriendProtos.Friend;
import qxmobile.protobuf.FriendProtos.Friends;
import qxmobile.protobuf.GuildProtos.GuildList;
import qxmobile.protobuf.HeroMessage.UserHeroList;
import qxmobile.protobuf.HeroRankProto.HeroRankList;
import qxmobile.protobuf.HeroUnitMessage.HeroCombatUnitList;
import qxmobile.protobuf.IrrigateRecordProtos.IrrigateRecords;
import qxmobile.protobuf.ItemProtos.Bag;
import qxmobile.protobuf.LotteryMessage.UserLottery;
import qxmobile.protobuf.MailProtos.Mails;
import qxmobile.protobuf.PkRecordMessage.PkRecordList;
import qxmobile.protobuf.PveMessage.UserPveList;
import qxmobile.protobuf.TimerProtos.Timers;
import qxmobile.protobuf.UserProtos.User;
import qxmobile.protobuf.UserYeZhanMessage.UserYeZhan;

import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.util.DataLoader;
import com.manu.dynasty.util.ProtobufUtils;
import com.manu.network.BigSwitch;
import com.manu.network.SessionManager;
import com.manu.network.TXSocketMgr;
import com.qx.http.StartServ;
import com.qx.util.ThreadViewer;

public class GameServerInit {
	public static Logger log = LoggerFactory.getLogger(GameServerInit.class);
	public static void init(){
		System.setProperty("serverStartTime", new Date().toLocaleString());
		new SessionManager();
//		new ChatMgr();
		ThreadViewer.start();
		Redis.getInstance();
		init1000();
		init2000();
		init3000();
		
		log.info("开始载入配置xml。");
		initData();
		log.info("开始初始化BigSwitch和各个系统模块。");
		new BigSwitch();
		log.info("开始启动网络端口。");
		try {
			TXSocketMgr.getInst().start();
			log.info("启动成功，通知登陆服务器");
			// 向登录服务器发送启动服务器成功通知
			new StartServ().start();
		} catch (IOException e) {
			log.error("启动端口失败", e);
			System.err.println("GameServerInit.init() 启动socket失败");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	private static void init1000(){
		ProtobufUtils.register(Buildings.getDefaultInstance(), 1001);
		ProtobufUtils.register(Account.getDefaultInstance(), 1002);
		ProtobufUtils.register(User.getDefaultInstance(), 1003);
		ProtobufUtils.register(Bag.getDefaultInstance(), 1004);
		ProtobufUtils.register(Timers.getDefaultInstance(), 1005);
		ProtobufUtils.register(UserHeroList.getDefaultInstance(), 1006);
		ProtobufUtils.register(HeroRankList.getDefaultInstance(), 1007);
		ProtobufUtils.register(UserPveList.getDefaultInstance(), 1008);
		ProtobufUtils.register(BattleItemInfo.getDefaultInstance(), 1009);
		ProtobufUtils.register(BattleItemInfoList.getDefaultInstance(), 1010);
		ProtobufUtils.register(Farm.getDefaultInstance(), 1011);
		ProtobufUtils.register(GuildList.getDefaultInstance(), 1012);
		ProtobufUtils.register(Friends.getDefaultInstance(), 1013);
		ProtobufUtils.register(Friend.getDefaultInstance(), 1014);
		ProtobufUtils.register(HeroCombatUnitList.getDefaultInstance(), 1015);
		ProtobufUtils.register(IrrigateRecords.getDefaultInstance(), 1016);
		ProtobufUtils.register(UserYeZhan.getDefaultInstance(), 1017);
		ProtobufUtils.register(UserLottery.getDefaultInstance(), 1018);
		ProtobufUtils.register(PkRecordList.getDefaultInstance(), 1019);
		ProtobufUtils.register(Mails.getDefaultInstance(), 1020);
	}
	
	private static void init2000(){
		
	}

	private static void init3000(){
		
	}
	
	private static void initData(){
		DataLoader dl = new DataLoader("com.manu.dynasty.template.", "/dataConfig.xml");
		dl.load();
	}
	
	public static void reloadData() {
		initData();
		BigSwitch.inst.loadModuleData();
	}
}
