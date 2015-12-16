package com.qx.ranking;

import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Ranking.GongJinInfo;
import qxmobile.protobuf.Ranking.RankingResp;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.LueduoLianmengRank;
import com.manu.dynasty.template.LueduoPersonRank;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;

public class RankingGongJinMgr {
	
	public static RankingGongJinMgr inst;
	public static Logger log = LoggerFactory.getLogger(RankingGongJinMgr.class);
	
	public static String gongJinPersonalRank = "gongJinPersonalRank";
	public static String gongJinAllianceRank =  "gongJinAlliance";
	public static int PAGE_SIZE = RankingMgr.PAGE_SIZE;
	public static Redis DB = RankingMgr.DB;
	
//	public static Map<Integer, LueduoPersonRank> lRankMap =
//			new HashMap<Integer, LueduoPersonRank>();
	
	public static List<LueduoPersonRank> persRankList;
	public static List<LueduoLianmengRank> lianMengRankList;

	public static int initGongJin = CanShu.LUEDUO_GONGJIN_INIT ; 

	public RankingGongJinMgr(){
		inst = this;
		
		List<LueduoPersonRank> list = TempletService
				.listAll(LueduoPersonRank.class.getSimpleName());
		persRankList = list;
		
		List<LueduoLianmengRank> list2 = TempletService
				.listAll(LueduoLianmengRank.class.getSimpleName());
		lianMengRankList = list2;
	}

	public void sendPersonalGongJinRank(int pageNo, RankingResp.Builder response){
		int start = PAGE_SIZE * (pageNo - 1);
		int end = start + PAGE_SIZE;
		Map<String, Double> gongJinPermap = getPaiHangOfType(start, end, gongJinPersonalRank);
		int needId = 1; 
		int needRank = 1;
		if(gongJinPermap != null && gongJinPermap.size() != 0){
			for(Map.Entry<String, Double> entry: gongJinPermap.entrySet()){
				String id = entry.getKey();
				double value = entry.getValue();
				needId = Integer.parseInt(id == null? "-1" : id);
				JunZhu junzhu = HibernateUtil.find(JunZhu.class, needId);
				if(junzhu != null){
					GongJinInfo.Builder f = GongJinInfo.newBuilder();
					f.setId(needId);
					f.setName(junzhu.name);
					f.setGongJin((int)value);
					f.setRank(needRank ++);
					response.addGongInfoList(f);
				}
			}
		}else{
			log.error("获取start:{}, end:{} 贡金个人排行榜出错", start, end);
		}

	}
	public void sendAllianceGongJinRank(int pageNo, RankingResp.Builder response){
		int start = PAGE_SIZE * (pageNo - 1);
		int end = start + PAGE_SIZE;
		Map<String, Double> gongJinAllamap = getPaiHangOfType(start, end, gongJinAllianceRank);
		int needId = 1; 
		int needRank = 1;
		if(gongJinAllamap != null && gongJinAllamap.size() != 0){
			for(Map.Entry<String, Double> entry: gongJinAllamap.entrySet()){
				String id = entry.getKey();
				double value = entry.getValue();
				needId = Integer.parseInt(id == null? "-1" : id);
				AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, needId);
				if(alncBean != null){
					GongJinInfo.Builder f = GongJinInfo.newBuilder();
					f.setId(needId);
					f.setName(alncBean.name);
					f.setGongJin((int)value);
					f.setRank(needRank ++);
					response.addGongInfoList(f);
				}
			}
		}else{
			log.error("获取start:{}, end:{} 贡金联盟排行榜出错", start, end);
		}
	}

	/**
	 * 获取start ~ end名次之间的玩家 （大到小,包含start 和end）
	 * 
	 * @Title: getZhanliRank
	 * @Description:
	 * @param start
	 * @param end
	 * @return 返回一个 从大到小的有序map
	 */
	public Map<String, Double> getPaiHangOfType(long start, long end, String paiHangType) {
		log.info("start:{} 和 end :{} ", start, end);
		Map<String, Double> map = DB.zrevrangeWithScores(paiHangType, start - 1,
				end - 1);
		return map;
	}

	public int getJunZhuGongJin(long junzhuId){
		int gongj = DB.zscore_(gongJinPersonalRank, junzhuId + "");
		if(gongj != -1){
			return gongj;
		}
		log.error("贡金数据不正确，君主id:{}", junzhuId);
		return -1;
	}

	public int getAllianceGongJin(int allianceId){
		int gongj = DB.zscore_(gongJinAllianceRank, allianceId + "");
		if(gongj != -1){
			return gongj;
		}
		log.error("联盟整体贡金数据不正确，联盟id:{}", allianceId);
		return -1;
	}

	public void addGongJin(long junZhuId, int addValue){
		// 查看是否有联盟，否则不添加
		AlliancePlayer a = HibernateUtil.find(AlliancePlayer.class, junZhuId);
		if(a == null || a.lianMengId <= 0){
			return;
		}
		int g = getJunZhuGongJin(junZhuId);

		g += addValue; 
		g = g <= 0? 0: g; 
		DB.zadd(gongJinPersonalRank, g, junZhuId+"");
		log.info("君主：{}的贡金增加增加值是：{}，目前是：{}", junZhuId, addValue, g);

		int allG = getAllianceGongJin(a.lianMengId);
		allG = allG < 0? 0: allG;
		DB.zadd(gongJinAllianceRank, allG + addValue , a.lianMengId+"");
		log.info("联盟：{}的贡金增加增加值是：{}，目前是：{}", a.lianMengId, addValue, allG + addValue);
	}

	public void setGongJinTo0(long junZhuId, int lianMengId){
		int g = getJunZhuGongJin(junZhuId);
		if(g == -1){
			return;
		}
		DB.zadd(gongJinPersonalRank, 0, junZhuId+"");

		if(lianMengId != -1){
			int all = getAllianceGongJin(lianMengId);
			if(all != -1){
				DB.zadd(gongJinAllianceRank, (all - g), lianMengId+"");
			}
		}
	}

	/**
	 * 每天上午8点名次不变，重新设置排行中玩家贡金数据，和联盟中贡金数据
	 */
	public void resetGongJinAt8_clock(){
		String oldRank = "gongJinOld";
		DB.rename(gongJinPersonalRank, oldRank);
		/*
		 * 判断是否重命名成功
		 */
		if(DB.exist_(oldRank)){
			log.info("oldRank已经产生");
		}
		if(DB.exist_(gongJinPersonalRank)){
			log.error("gongJinPersonalRank已经重命名，但是排行仍然存在");
		}else{
			log.info("gongJinPersonalRank排行榜已经重命名成功");
		}
		
		// 获取上一日贡金排行数据
		int size = (int)DB.zcard_(oldRank);
		for(LueduoPersonRank lpr: persRankList){
			// 排行数据有限，则return
			if(lpr.min > size){
				break;
			}
			int min = lpr.min;
			int max = lpr.max;
			max = Math.min(max, size);

			Map<String, Double> map = 
					getPaiHangOfType(min, max, oldRank);
			if(map == null || map.size() == 0){
				continue;
			}
			for(Map.Entry<String, Double> entry: map.entrySet()){
				String junzId = entry.getKey();
				double score = entry.getValue();
				if(junzId == null ){
					continue;
				}
				if(score == 0){
					continue;
				}
				int newData = 0;
				AlliancePlayer a = HibernateUtil.find(AlliancePlayer.class,
						Long.parseLong(junzId));
				if(a == null || a.lianMengId <= 0){
					DB.zadd(gongJinPersonalRank, 0, junzId);
					log.error("君主id：{}无联盟，但是在贡金排行榜中， 且贡金不为0, 那现在设置为0");
				}else{
					// 重新把数据加到贡金排行榜中
					newData = lpr.updateNum;
					DB.zadd(gongJinPersonalRank, newData, junzId);
				}

				int all = getAllianceGongJin(a.lianMengId);
				if(all != -1){
					DB.zadd(gongJinAllianceRank, (all - score + newData), a.lianMengId+"");
				}
			}
		}
		// 把已有的删掉
		DB.del(oldRank);
	}

	/*
	 *初次加入联盟，初始化贡金
	 */
	public void firstSetGongJin(long junzhuId, int allianceId){
		int g = getJunZhuGongJin(junzhuId);
		// 排行中没有数据，所以应该是首次初始化，首次初始化，会有一个初始值
		if(g < 0){
			DB.zadd(gongJinPersonalRank, initGongJin, junzhuId+"");
			int allG = getAllianceGongJin(allianceId);
			allG = allG < 0? 0: allG;
			DB.zadd(gongJinAllianceRank, allG + initGongJin, allianceId+"");
		}
	}

	/*
	 * 先保留
	 */
//	public void getPaiHangBang(int cmd, IoSession session, Builder builder) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		if (jz == null){
//			log.error("请求排行榜，君主不存在");
//			return;
//		}
//		long jId = jz.id;
//		boolean isInclude = false;
//		RankingResp.Builder resp = RankingResp.newBuilder();
//		/*
//		 * 百战个人排行榜
//		 */
//		Set<String> elems = DB.zrangebyscore_(PvpMgr.KEY, 0, number-1);
//		JunZhuInfo.Builder junInfo = null;
//		if(elems != null && elems.size() != 0)
//		{
//			int rank = 1000;
//			String name = "";
//			for (String s : elems) {
//				String[] sss = s.split("_");
//				long playerId = Long.parseLong(sss[1]);
//				if ("npc".equals(sss[0])) {
//					// NPC
//					BaiZhanNpc npc = PvpMgr.inst.npcs.get((int) playerId);
//					String nameInt = npc.name;
//					name = HeroService.heroNameMap.get(nameInt).Name;
//					junInfo = JunZhuInfo.newBuilder();
//					junInfo.setJunZhuId(-playerId);
//					junInfo.setName(name);
//					junInfo.setLevel(npc.level);
//					junInfo.setLianMeng("无联盟");
////					junInfo.setWinCount(20);
//					junInfo.setZhanli(npc.power);
//					junInfo.setGuojiaId(npc.getGuoJiaId((int) playerId));
//					junInfo.setRoleId(npc.getRoleId((int) playerId));
//					rank = PvpMgr.inst.getPvpRankById(-playerId);
//					junInfo.setRank(rank);
//					resp.addJunList(junInfo);
//				} else {
//					JunZhu junzhu = HibernateUtil.find(JunZhu.class, playerId);
//					if(playerId == jId){
//						isInclude = true;
//					}
//					rank = PvpMgr.inst.getPvpRankById(playerId);
//					addJunZhuRankInfo(resp, junzhu, rank);
//				}
//			}
//		}else{
//			log.error("获取百战个人排行榜前:{}名数据出错", number);
//		}
//		/*
//		 * 玩家不在100以内，发送玩家信息
//		 */
//		if(!isInclude){
//			int rank =  PvpMgr.inst.getPvpRankById(jz.id);
//			addJunZhuRankInfo(resp, jz, rank);
//		}
//		/*
//		 * 联盟排行榜 
//		 */
//		isInclude = false;
//		int jzMengid = 0;
//		AlliancePlayer guild = HibernateUtil.find(AlliancePlayer.class, jId);
//		if(guild != null){
//			jzMengid = guild.lianMengId;
//		}else{
//			isInclude = true;
//		}
//
//		Map<String, Double> map = getPaiHangOfType(1, lianmengNumber, lianMengLevel);
//		int mengId = 1; 
//		int mengRank = 1;
//		if(map != null && map.size() != 0){
//			for(Map.Entry<String, Double> entry: map.entrySet()){
//				String id = entry.getKey();
//				mengId = Integer.parseInt(id == null? "-1" : id);
//				boolean ok = addLianMengInfo(resp, mengRank, mengId, null);
//				if(ok){
//					mengRank++;
//				}
//				if(mengId == jzMengid){
//					isInclude = true;
//				}
//			}
//		}else{
//			log.error("获取前:{}名联盟等级排行出错", lianmengNumber);
//		}
//		// 判断玩家所在联盟的名次是否在100名以内，否则读取玩家的数据发送
//		if(isInclude){
//			log.error("君主:{}可能没有联盟也或者包括在前：{}名次之内，所以不再单独发送", jId, lianmengNumber);
//		}else {
//			int lianmengId = guild.lianMengId;
//			AllianceBean alncBean = null;
//			try{
//				// 从大到小的排名中占名次
//				mengRank = (int)DB.zrevrank(lianMengLevel, lianmengId+"") + 1;
//			}catch(Exception e){
//				log.error("有可能这个联盟是很早就申请的，所以需要加入到redis中");
//				alncBean = HibernateUtil.find(AllianceBean.class, lianmengId);
//				if(alncBean != null){
//					mengRank = (int)resetLianMengLevelRedis(lianmengId, alncBean.level);
//				}
//			}
//			addLianMengInfo(resp, mengRank, lianmengId, alncBean);
//		}
//		session.write(resp.build());
//	}

}
