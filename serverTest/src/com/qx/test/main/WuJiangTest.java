package com.qx.test.main;

import java.util.List;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;
import qxmobile.protobuf.PveLevel.EnemyInfo;
import qxmobile.protobuf.PveLevel.GuanQiaInfo;
import qxmobile.protobuf.PveLevel.RewardItem;
import qxmobile.protobuf.WuJiangProtos.HeroDate;
import qxmobile.protobuf.WuJiangProtos.HeroInfoResp;
import qxmobile.protobuf.WuJiangProtos.WuJiangTech;
import qxmobile.protobuf.WuJiangProtos.WuJiangTechnologyDate;

import com.google.protobuf.MessageLite.Builder;

public class WuJiangTest {

	public static void readList(IoSession session, Builder builder) {
		System.out.println("获得武将列表 ");
		HeroInfoResp.Builder ret = (qxmobile.protobuf.WuJiangProtos.HeroInfoResp.Builder) builder;
		List<HeroDate> list = ret.getHerosList();
		int cnt = list.size();
		System.out.println("武将个数:"+cnt);
		for(int i=0; i<cnt ;i++){
			HeroDate h = list.get(i);
			System.out.println("getHeroGrowId:"+h.getHeroGrowId());
			System.out.println("战力:"+h.getZhanLi());
		}
	}

	public static void readGuanQiaInfo(IoSession session, Builder builder) {
		GuanQiaInfo.Builder ret = (GuanQiaInfo.Builder)builder;
		int cnt = ret.getItemsCount();
		System.out.println("奖励个数:"+cnt);
		for(int i=0; i<cnt ; i++){
			RewardItem item = ret.getItems(i);
			System.out.println(item.getName());
		}
		cnt = ret.getEnemiesCount();
		System.out.println("敌人个数:"+cnt);
		for(int i=0; i<cnt; i++){
			EnemyInfo enemy = ret.getEnemies(i);
			System.out.println(enemy.getName());
		}
		System.out.println(ret.getDesc());
	}

	public static void readKeJi(IoSession session, Builder builder) {
		WuJiangTech.Builder b = (WuJiangTech.Builder)builder;
		int cnt = b.getTechnologyListCount();
		System.out.println("武将科技个数:"+cnt);
		for(int i=0; i<cnt; i++){
			WuJiangTechnologyDate t = b.getTechnologyList(i);
			System.out.println(t.getTechType());
		}
	}

	public static JunZhuInfoRet.Builder readJunZhuInfo(IoSession session, Builder builder) {
		JunZhuInfoRet.Builder ret = (qxmobile.protobuf.JunZhuProto.JunZhuInfoRet.Builder) builder;
//		System.out.println("收到君主将信息:铜币"+ret.getJinBi()+" 元宝:"+ret.getYuanBao());
		return ret;
	}

}
