package com.qx.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Ranking.AlliancePlayerReq;
import qxmobile.protobuf.Ranking.AlliancePlayerResp;
import qxmobile.protobuf.Ranking.BaiZhanInfo;
import qxmobile.protobuf.Ranking.GetRankReq;
import qxmobile.protobuf.Ranking.GetRankResp;
import qxmobile.protobuf.Ranking.GuoGuanInfo;
import qxmobile.protobuf.Ranking.JunZhuInfo;
import qxmobile.protobuf.Ranking.LianMengInfo;
import qxmobile.protobuf.Ranking.RankingReq;
import qxmobile.protobuf.Ranking.RankingResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.BaiZhanNpc;
import com.manu.dynasty.template.CanShu;
import com.manu.network.SessionManager;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.guojia.GuoJiaBean;
import com.qx.guojia.GuoJiaMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.ChengHaoBean;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pve.PveRecord;
import com.qx.pvp.PvpBean;
import com.qx.pvp.PvpMgr;

/**
 * 排行榜管理类 This class is used for ...
 * 
 * @author wangZhuan
 * @version 9.0, 2015年2月4日 下午3:12:07
 */
public class RankingMgr extends EventProc{
	public static Logger log = LoggerFactory.getLogger(RankingMgr.class);
	public static final Redis DB = Redis.getInstance();
	public static String JUNZHU_RANK = "junzhu_" + GameServer.serverId;// Redis君主榜
	public static String LIANMENG_RANK = "lianmeng_" + GameServer.serverId;// Redis联盟榜
	public static String BAIZHAN_RANK = "baizhan_" + GameServer.serverId;// Redis百战榜
	public static String GUOGUAN_RANK = "guoguan_" + GameServer.serverId;// Redis过关榜
	public static String LIANMENG_SW_DAY_RANK = "lianmeng_sw_day_" + GameServer.serverId;// 联盟声望榜（日榜）
	public static String LIANMENG_SW_WEEK_RANK = "lianmeng_sw_week_" + GameServer.serverId;// 联盟声望榜（周榜）
	public static String LIANMENG_SW_LAST_DAY_RANK = "lianmeng_sw_last_day_" + GameServer.serverId;// 联盟声望榜（昨日榜）
	public static String LIANMENG_SW_LAST_WEEK_RANK = "lianmeng_sw_last_week_" + GameServer.serverId;// 联盟声望榜（上周榜）
	public static String GUOJIA_DAY_RANK = "guojia_day_" + GameServer.serverId;// Redis国家榜（日榜）
	public static String GUOJIA_WEEK_RANK = "guojia_week_" + GameServer.serverId;// Redis国家榜（周榜）
	public static String GUOJIA_DAY_LAST_RANK = "guojia_day_last_" + GameServer.serverId;// Redis国家榜（昨日日榜）
	public static String GUOJIA_WEEK_LAST_RANK = "guojia_week_last_" + GameServer.serverId;// Redis国家榜（上周周榜）
	public static String zhanliRank = "zhanli_" + GameServer.serverId;
	public static String lianMengLevel = "mengLevel_" + GameServer.serverId;
	public static RankingMgr inst;
	public static int number = 100;
	public static int lianmengNumber = 20;
	public static int PAGE_SIZE = 20;// 每页数目
	public static int RANK_MAXNUM = 200;// 君主筛选范围
	public static int RANK_MINLEVEL = 1;// 君主筛选最低等级

	public RankingMgr() {
		initData();
		inst = this;
	}
	
	public void initData(){
		if(!DB.exist_(GUOJIA_DAY_RANK)){
			// 国家日榜插入空数据
			newGuojiaRank(GUOJIA_DAY_RANK);
		}
		if(!DB.exist_(GUOJIA_WEEK_RANK)){
			// 国家周榜插入空数据
			newGuojiaRank(GUOJIA_WEEK_RANK);
		}
		if(!DB.exist_(GUOJIA_DAY_LAST_RANK)){
			// 国家昨日日榜插入空数据
			newGuojiaRank(GUOJIA_DAY_LAST_RANK);
		}
		if(!DB.exist_(GUOJIA_WEEK_LAST_RANK)){
			// 国家上周周榜插入空数据
			newGuojiaRank(GUOJIA_WEEK_LAST_RANK);
		}
		// 参数配置
		RANK_MAXNUM = CanShu.RANK_MAXNUM;
		RANK_MINLEVEL = CanShu.RANK_MINLEVEL;
	}

	public void resetZhanliRankRedis(JunZhu jz, int zhanli) {
		if (jz == null) {
			log.error("resetZhanliRankRedis 参数为null");
			return;
		}
		DB.zadd(zhanliRank, zhanli, jz.id + "_" + jz.name);
		log.info("君主：{}战力：{}添加到redisd,key={}", jz.name, zhanli, zhanliRank);
		int size = (int) DB.zcard_(zhanliRank);
		log.info("战力排行榜的size是： {}", size);
	}

	public long resetLianMengLevelRedis(int lianmengId, int level){
		long ret  = DB.zadd(lianMengLevel, level, lianmengId+"");
		log.info("联盟:{},等级是:{}添加到key={}的redis中, 排名是:{}",
				lianmengId, level, lianMengLevel, ret);
		return ret;
	}
	
	/** 
	 * @Title: resetJunzhuRankRedis 
	 * @Description: 刷新君主榜
	 * @param jz
	 * @param zhanli
	 * @return void
	 * @throws 
	 */
	public void resetJunzhuRankRedis(JunZhu jz) {
		if (jz == null) {
			log.error("resetJunzhuRankRedis 参数为null");
			return;
		}
		if(jz.level<RANK_MINLEVEL){
			log.info("君主榜刷新错误，君主等级不满足最低限制:{}级",RANK_MINLEVEL);
			return;
		}
		long zhanli = PvpMgr.inst.getZhanli(jz);
		long level = jz.level;
		PvpBean bean = HibernateUtil.find(PvpBean.class,jz.id);
		long junxianLevel = 1;
		if(bean!=null){
			junxianLevel = bean.junXianLevel;
		}
		// Redis存储score计算为：战力*100000+等级*100+军衔
		long score = zhanli*100000+level*100+junxianLevel;
		int guojiaId = jz.guoJiaId;
		DB.zadd(JUNZHU_RANK+"_0", score, jz.id + "");// 添加到全部排名
		DB.zadd(JUNZHU_RANK+"_"+guojiaId, score, jz.id + "");// 添加在相应国家排名
		log.info("君主榜==>君主：{}战力：{}等级：{}军衔等级：{}添加到redisd,key={}", jz.name, zhanli,level,junxianLevel, JUNZHU_RANK+"_0");
		log.info("君主榜==>君主：{}战力：{}等级：{}军衔等级：{}添加到redisd,key={}", jz.name, zhanli,level,junxianLevel, JUNZHU_RANK+"_"+guojiaId);
		int allSize = (int) DB.zcard_(JUNZHU_RANK+"_0");
		log.info("总君主排行榜的size是： {}", allSize);
		int gjSize = (int) DB.zcard_(JUNZHU_RANK+"_"+guojiaId);
		log.info("国家{}君主排行榜的size是： {}", guojiaId,gjSize);
	}

	/** 
	 * @Title: resetLianMengLevelRedis 
	 * @Description: 刷新联盟榜
	 * @param lianmengId
	 * @return
	 * @return long
	 * @throws 
	 */
	public void resetLianMengRankRedis(int lianmengId){
		AllianceBean lianmeng = HibernateUtil.find(AllianceBean.class, lianmengId);
		if(lianmeng==null){
			log.error("resetLianMengRankRedis 参数为null");
			return;
		}
		long level = lianmeng.level;
		long reputation = lianmeng.reputation;
		long members = lianmeng.members;
		// Redis存储score计算为：等级*1000000000000+声望*1000+成员数量
		long score = level*1000000000000L+reputation*1000+members;
		int guojiaId = lianmeng.country;
		long allRet = DB.zadd(LIANMENG_RANK+"_0", score, lianmengId+"");// 联盟全服榜
		long gjRet = DB.zadd(LIANMENG_RANK+"_"+guojiaId, score, lianmengId+"");// 联盟国家榜
		log.info("联盟榜==>联盟:{},等级是:{}添加到key={}的redis中, 排名是:{}",
				lianmengId, level, LIANMENG_RANK+"_0", allRet);
		log.info("联盟榜==>联盟:{},等级是:{}添加到key={}的redis中, 排名是:{}",
				lianmengId, level, LIANMENG_RANK+"_"+guojiaId, gjRet);
	}
	
	/** 
	 * @Title: resetBaizhanRankRedis 
	 * @Description: 刷新百战榜
	 * @param jz
	 * @return void
	 * @throws 
	 */
	public void resetBaizhanRankRedis(JunZhu jz){
		if (jz == null) {
			log.error("resetBaizhanRankRedis 参数为null");
			return;
		}
		if(jz.level<RANK_MINLEVEL){
			log.info("百战榜刷新错误，君主等级不满足最低限制:{}级",RANK_MINLEVEL);
			return;
		}
		PvpBean bean = HibernateUtil.find(PvpBean.class,jz.id);
		long junxianLevel = 1;
//		int winCount = 0;
//		int reputation = 0;
		if(bean!=null){
			junxianLevel = bean.junXianLevel;
//			winCount = bean.allWin;
//			reputation = bean.showWeiWang;
		}
		// Redis存储score计算为：军衔等级*100000+军衔排名
		//long score=junxianLevel;
		long junxianRank = PvpMgr.inst.getPvpRankById(jz.id);
		junxianRank = junxianRank==-1?99999:junxianRank;// 无百战排名的排到最后
		long score=(10-junxianLevel)*100000+junxianRank;		
		int guojiaId = jz.guoJiaId;
		DB.zadd(BAIZHAN_RANK+"_0", score, jz.id+"");// 全服百战榜
		DB.zadd(BAIZHAN_RANK+"_"+guojiaId, score, jz.id + "");// 全国百战榜
		log.info("百战榜==>君主：{}军衔：{}添加到redisd,key={}", jz.name, junxianLevel, BAIZHAN_RANK+"_0");
		log.info("百战榜==>君主：{}军衔：{}添加到redisd,key={}", jz.name, junxianLevel, BAIZHAN_RANK+"_"+guojiaId);
		int allSize = (int) DB.zcard_(BAIZHAN_RANK+"_0");
		log.info("总百战排行榜的size是： {}", allSize);
		int gjSize = (int) DB.zcard_(BAIZHAN_RANK+"_"+guojiaId);
		log.info("国家{}百战排行榜的size是： {}", guojiaId,gjSize);
	}
	
	/** 
	 * @Title: resetGuoguanRankRedis 
	 * @Description: 刷新过关榜
	 * @param jz
	 * @return void
	 * @throws 
	 */
	public void resetGuoguanRankRedis(JunZhu jz){
		if (jz == null) {
			log.error("resetGuoguanRankRedis 参数为null");
			return;
		}
		if(jz.level<RANK_MINLEVEL){
			log.info("过关榜刷新错误，君主等级不满足最低限制:{}级",RANK_MINLEVEL);
			return;
		}
		List<PveRecord> pveList = HibernateUtil.list(PveRecord.class,"where uid="+jz.id+" order by guanQiaId DESC");
		long starCount = 0;
		long ptMax=0;
		long cqMax=0;
		for(PveRecord pve:pveList){// 计算总星数和最高传奇关卡最高普通关卡
			starCount=starCount+pve.star+pve.cqStar;
			ptMax = Math.max(ptMax, pve.guanQiaId);
			if(pve.chuanQiPass){
				cqMax = Math.max(cqMax, pve.guanQiaId);
			}
		}
		ptMax = ptMax%10000;
		cqMax = cqMax%10000;
		// Redis存储score计算为：普通*100000000+传奇*10000+评星
		long score=ptMax*100000000+cqMax*10000+starCount;
		int guojiaId = jz.guoJiaId;
		DB.zadd(GUOGUAN_RANK+"_0", score, jz.id + "");// 全服百战榜
		DB.zadd(GUOGUAN_RANK+"_"+guojiaId, score, jz.id + "");// 全国百战榜
		log.info("过关榜==>君主：{}普通关：{}传奇关：{}总星数：{}添加到redisd,key={}", jz.name, ptMax,cqMax,starCount, GUOGUAN_RANK+"_0");
		log.info("过关榜==>君主：{}普通关：{}传奇关：{}总星数：{}添加到redisd,key={}", jz.name, ptMax,cqMax,starCount, GUOGUAN_RANK+"_"+guojiaId);
		int allSize = (int) DB.zcard_(GUOGUAN_RANK+"_0");
		log.info("过关榜排行榜的size是： {}", allSize);
		int gjSize = (int) DB.zcard_(GUOGUAN_RANK+"_"+guojiaId);
		log.info("国家{}过关排行榜的size是： {}", guojiaId,gjSize);
	}
	
	
	/** 
	 * @Title: resetLianmengSWDayRankRedis 
	 * @Description: 刷新联盟榜日榜（按声望排）
	 * @param lianmengId
	 * @param changeSW
	 * @return void
	 * @throws 
	 */
	public void resetLianmengSWDayRankRedis(AllianceBean lianmeng,int changeSW){
		if(lianmeng==null){
			log.error("resetLianmengSWDayRankRedis 参数为null");
			return;
		}
		double reputation = DB.zscore(LIANMENG_SW_DAY_RANK+"_0", lianmeng.id+"");
		reputation = (reputation==-1)?0:reputation;
		// Redis存储score计算为：声望
		double score = reputation+changeSW;
		int guojiaId = lianmeng.country;
		DB.zadd(LIANMENG_SW_DAY_RANK+"_0", score, lianmeng.id+"");// 联盟全服榜
		DB.zadd(LIANMENG_SW_DAY_RANK+"_"+guojiaId, score, lianmeng.id+"");// 联盟国家榜
		log.info("联盟日榜（按声望）==>联盟:{},添加到key={}的redis中",lianmeng.id,  LIANMENG_SW_DAY_RANK+"_0");
		log.info("联盟日榜（按声望）==>联盟:{},添加到key={}的redis中",lianmeng.id,  LIANMENG_SW_DAY_RANK+"_"+guojiaId);
	}
	
	/** 
	 * @Title: resetLianmengSWWeekRankRedis 
	 * @Description: 刷新联盟榜周榜（按声望排）
	 * @param lianmengId
	 * @return
	 * @return long
	 * @throws 
	 */
	public void resetLianmengSWWeekRankRedis(AllianceBean lianmeng,int changeSW){
		if(lianmeng==null){
			log.error("resetLianmengSWWeekRankRedis 参数为null");
			return;
		}
		double reputation = DB.zscore(LIANMENG_SW_WEEK_RANK+"_0", lianmeng.id+"");
		reputation = (reputation==-1)?0:reputation;
		// Redis存储score计算为：声望
		double score = reputation+changeSW;
		int guojiaId = lianmeng.country;
		DB.zadd(LIANMENG_SW_WEEK_RANK+"_0", score, lianmeng.id+"");// 联盟全服榜
		DB.zadd(LIANMENG_SW_WEEK_RANK+"_"+guojiaId, score, lianmeng.id+"");// 联盟国家榜
		log.info("联盟周榜（按声望）==>联盟:{},添加到key={}的redis中",lianmeng.id,  LIANMENG_SW_WEEK_RANK+"_0");
		log.info("联盟周榜（按声望）==>联盟:{},添加到key={}的redis中",lianmeng.id,  LIANMENG_SW_WEEK_RANK+"_"+guojiaId);
	}
	
	/** 
	 * @Title: resetGuojiaDayRankRedis 
	 * @Description: 刷新国家每日榜
	 * @param guojiaId
	 * @return void
	 * @throws 
	 */
	public void resetGuojiaDayRankRedis(GuoJiaBean guojia,int changeSW){
		if (guojia == null) {
			log.error("resetGuojiaDayRankRedis 参数为null");
			return;
		}
		double shengwang = DB.zscore(GUOJIA_DAY_RANK, guojia.guoJiaId+"");
		shengwang = (shengwang==-1)?0:shengwang;
		// Redis存储score计算为：声望
		double score=shengwang+changeSW;
		DB.zadd(GUOJIA_DAY_RANK, score,guojia.guoJiaId + "");
		log.info("国家每日榜==>国家：{}声望：{}添加到redisd,key={}", guojia.guoJiaId, score,GUOJIA_DAY_RANK);
	}
	
	/** 
	 * @Title: resetGuojiaWeekRankRedis 
	 * @Description: 刷新国家每周榜
	 * @param guojiaId
	 * @return void
	 * @throws 
	 */
	public void resetGuojiaWeekRankRedis(GuoJiaBean guojia,int changeSW){
		if (guojia == null) {
			log.error("resetGuojiaWeekRankRedis 参数为null");
			return;
		}
		double shengwang = DB.zscore(GUOJIA_WEEK_RANK, guojia.guoJiaId+"");
		shengwang = (shengwang==-1)?0:shengwang;
		// Redis存储score计算为：声望
		double score=shengwang+changeSW;
		DB.zadd(GUOJIA_WEEK_RANK, score,guojia.guoJiaId + "");
		log.info("国家每日榜==>国家：{}声望：{}添加到redisd,key={}", guojia.guoJiaId, score,GUOJIA_WEEK_RANK);
	}
	
	/** 
	 * @Title: newLianmengSWRank 
	 * @Description: 联盟声望榜清零
	 * @param key
	 * @return void
	 * @throws 
	 */
	public void newLianmengSWRank(String key){
		for(int i=0;i<=7;i++){
			Set<String> mengIds = DB.zrange(key+"_"+i);
			for(String mengId:mengIds){
				DB.zadd(key+"_"+i, 0, mengId);// 声望清零
			}
		}
	}
	
	/** 
	 * @Title: newGuojiaRank 
	 * @Description: 国家榜声望清零
	 * @param key
	 * @return void
	 * @throws 
	 */
	public void newGuojiaRank(String key){
		for(int i=1;i<=7;i++){
			DB.zadd(key, 0, i+"");
		}
	}
	
	/** 
	 * @Title: guojiaDayRankReset 
	 * @Description: 国家每日榜重置
	 * @return void
	 * @throws 
	 */
	public void guojiaDayRankReset(){
		DB.del(GUOJIA_DAY_LAST_RANK);
		Set<String> oldIds = DB.zrange(GUOJIA_DAY_RANK);
		for(String oldId:oldIds){// 备份国家日榜
			double oldScore = DB.zscore(GUOJIA_DAY_RANK, oldId);
			DB.zadd(GUOJIA_DAY_LAST_RANK, oldScore, oldId);
		}
		newGuojiaRank(GUOJIA_DAY_RANK);// 清空国家日榜
	}
	
	/** 
	 * @Title: guojiaWeekRankReset 
	 * @Description: 国家每周榜重置
	 * @return void
	 * @throws 
	 */
	public void guojiaWeekRankReset(){
		DB.del(GUOJIA_WEEK_LAST_RANK);
		Set<String> oldIds = DB.zrange(GUOJIA_WEEK_RANK);
		for(String oldId:oldIds){// 备份国家周榜
			double oldScore = DB.zscore(GUOJIA_WEEK_RANK, oldId);
			DB.zadd(GUOJIA_WEEK_LAST_RANK, oldScore, oldId);
		}
		newGuojiaRank(GUOJIA_WEEK_RANK);// 清空国家周榜
	}
	
	/** 
	 * @Title: lianMengBySWDayRankReset 
	 * @Description: 联盟日榜重置（按声望排）
	 * @throws 
	 */
	public void lianMengBySWDayRankReset(){
		for(int gjId=0;gjId<=7;gjId++){
			DB.del(LIANMENG_SW_LAST_DAY_RANK+"_"+gjId);// 清空联盟声望昨日榜
			Set<String> mengIds = DB.zrange(LIANMENG_SW_DAY_RANK+"_"+gjId);
			if(mengIds==null||mengIds.size()==0){
				continue;
			}
			for(String mengId:mengIds){// 备份联盟声望榜
				double score = DB.zscore(LIANMENG_SW_DAY_RANK+"_"+gjId, mengId);
				DB.zadd(LIANMENG_SW_LAST_DAY_RANK+"_"+gjId, score, mengId);
			}
		}
		newLianmengSWRank(LIANMENG_SW_DAY_RANK);
	}
	
	/** 
	 * @Title: lianMengBySWWeekRankReset 
	 * @Description: 联盟周榜重置（按声望排）
	 * @throws 
	 */
	public void lianMengBySWWeekRankReset(){
		for(int gjId=0;gjId<=7;gjId++){
			DB.del(LIANMENG_SW_LAST_WEEK_RANK+"_"+gjId);// 清空联盟声望昨日榜
			Set<String> mengIds = DB.zrange(LIANMENG_SW_WEEK_RANK+"_"+gjId);
			if(mengIds==null||mengIds.size()==0){
				continue;
			}
			for(String mengId:mengIds){// 备份联盟声望榜
				double score = DB.zscore(LIANMENG_SW_WEEK_RANK+"_"+gjId, mengId);
				DB.zadd(LIANMENG_SW_LAST_WEEK_RANK+"_"+gjId, score, mengId);
			}
		}
		newLianmengSWRank(LIANMENG_SW_WEEK_RANK);
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

	public void printPaiHang(Map<String, Double> map) {
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			String idN = entry.getKey();
			Double value = entry.getValue();
			log.info("君主id: {}, 姓名: {}, value: {}", idN.split("_")[0],
					idN.split("_")[1], value);
		}
	}

	public void getPaiHangBang(int cmd, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null){
			log.error("请求排行榜，君主不存在");
			return;
		}
		long jId = jz.id;
		boolean isInclude = false;
		RankingResp.Builder resp = RankingResp.newBuilder();
		/*
		 * 百战个人排行榜
		 */
		Set<String> elems = DB.zrangebyscore_(PvpMgr.KEY, 0, number-1);
		JunZhuInfo.Builder junInfo = null;
		if(elems != null && elems.size() != 0)
		{
			int rank = 1000;
			String name = "";
			for (String s : elems) {
				String[] sss = s.split("_");
				long playerId = Long.parseLong(sss[1]);
				if ("npc".equals(sss[0])) {
					// NPC
					BaiZhanNpc npc = PvpMgr.inst.npcs.get((int) playerId);
					String nameInt = npc.name;
					name = HeroService.heroNameMap.get(nameInt).Name;
					junInfo = JunZhuInfo.newBuilder();
					junInfo.setJunZhuId(-playerId);
					junInfo.setName(name);
					junInfo.setLevel(npc.level);
					junInfo.setLianMeng("无联盟");
//					junInfo.setWinCount(20);
					junInfo.setZhanli(npc.power);
					junInfo.setGuojiaId(npc.getGuoJiaId((int) playerId));
					junInfo.setRoleId(npc.getRoleId((int) playerId));
					rank = PvpMgr.inst.getPvpRankById(-playerId);
					junInfo.setRank(rank);
					resp.addJunList(junInfo);
				} else {
					JunZhu junzhu = HibernateUtil.find(JunZhu.class, playerId);
					if(playerId == jId){
						isInclude = true;
					}
					rank = PvpMgr.inst.getPvpRankById(playerId);
					addJunZhuRankInfo(resp, junzhu, rank);
				}
			}
		}else{
			log.error("获取百战个人排行榜前:{}名数据出错", number);
		}
		/*
		 * 玩家不在100以内，发送玩家信息
		 */
		if(!isInclude){
			int rank =  PvpMgr.inst.getPvpRankById(jz.id);
			addJunZhuRankInfo(resp, jz, rank);
		}
		/*
		 * 联盟排行榜 
		 */
		isInclude = false;
		int jzMengid = 0;
		AlliancePlayer guild = HibernateUtil.find(AlliancePlayer.class, jId);
		if(guild != null){
			jzMengid = guild.lianMengId;
		}else{
			isInclude = true;
		}

		Map<String, Double> map = getPaiHangOfType(1, lianmengNumber, lianMengLevel);
		int mengId = 1; 
		int mengRank = 1;
		if(map != null && map.size() != 0){
			for(Map.Entry<String, Double> entry: map.entrySet()){
				String id = entry.getKey();
				mengId = Integer.parseInt(id == null? "-1" : id);
				boolean ok = addLianMengInfo(resp, mengRank, mengId, null);
				if(ok){
					mengRank++;
				}
				if(mengId == jzMengid){
					isInclude = true;
				}
			}
		}else{
			log.error("获取前:{}名联盟等级排行出错", lianmengNumber);
		}
		// 判断玩家所在联盟的名次是否在100名以内，否则读取玩家的数据发送
		if(isInclude){
			log.error("君主:{}可能没有联盟也或者包括在前：{}名次之内，所以不再单独发送", jId, lianmengNumber);
		}else {
			int lianmengId = guild.lianMengId;
			AllianceBean alncBean = null;
			try{
				// 从大到小的排名中占名次
				mengRank = (int)DB.zrevrank(lianMengLevel, lianmengId+"") + 1;
			}catch(Exception e){
				log.error("有可能这个联盟是很早就申请的，所以需要加入到redis中");
				alncBean = HibernateUtil.find(AllianceBean.class, lianmengId);
				if(alncBean != null){
					mengRank = (int)resetLianMengLevelRedis(lianmengId, alncBean.level);
				}
			}
			addLianMengInfo(resp, mengRank, lianmengId, alncBean);
		}
		session.write(resp.build());
	}

	/** 
	 * @Title: getRankByGjIdAndId 
	 * @Description: 通过国家id和id获取在排行榜名次
	 * @param key 排行榜的key
	 * @param gjId 国家id
	 * @param id 
	 * @return
	 * @return long
	 * @throws 
	 */
	public long getRankByGjIdAndId(String key,int gjId,int id){
		long rank = 0;
		if(key.startsWith(BAIZHAN_RANK)){
			rank = DB.zrank(key+"_"+gjId, id+"");
		} else{
			rank = DB.zrevrank(key+"_"+gjId, id+"");
		}
		return rank;
	}
	
	/** 
	 * @Title: getRankById 
	 * @Description: 通过id查询在排行榜的名次
	 * @param key 排行榜的key
	 * @param id
	 * @return
	 * @return long
	 * @throws 
	 */
	public long getRankById(String key,int id){
		long rank = DB.zrevrank(key, id+"");
		return rank;
	}
	
	/** 
	 * @Title: getRankAlliancePlayer 
	 * @Description: 获取联盟的成员列表
	 * @param id
	 * @param session
	 * @param builder
	 * @return void
	 * @throws 
	 */
	public void getRankAlliancePlayer(int id, IoSession session, Builder builder) {
		AlliancePlayerReq.Builder request = (AlliancePlayerReq.Builder)builder;
		int mengId = request.getMengId();
		List<AlliancePlayer> playerList = AllianceMgr.inst.getAllianceMembers(mengId);
		if(playerList==null||playerList.size()==0){
			return;
		}
		AlliancePlayerResp.Builder response = AlliancePlayerResp.newBuilder();
		List<RankAlliancePlayer> rankPlayerList = new ArrayList<RankAlliancePlayer>();
		for(AlliancePlayer player:playerList){
			JunZhu jz = HibernateUtil.find(JunZhu.class, player.junzhuId);
			JunZhuInfo.Builder jzBuilder = JunZhuInfo.newBuilder();
			jzBuilder.setJunZhuId(jz.id);
			jzBuilder.setName(jz.name);
			jzBuilder.setJob(player.title);
			jzBuilder.setLevel(jz.level);
			jzBuilder.setGongxian(player.gongXian);
			jzBuilder.setZhanli(PvpMgr.inst.getZhanli(jz));
			jzBuilder.setGongjin(GuoJiaMgr.inst.getGongJin(jz.id, PvpMgr.getJunxianLevel(jz.id)));
			if(GuoJiaMgr.inst.isCanShangjiao(jz.id)){
				GuoJiaMgr.inst.pushCanShangjiao(jz.id);
			}
			RankAlliancePlayer rankPlayer = new RankAlliancePlayer(jzBuilder);
			rankPlayerList.add(rankPlayer);
		}
		Collections.sort(rankPlayerList);// 对联盟成员进行排序
		Collections.reverse(rankPlayerList);// 倒序排列
		for(RankAlliancePlayer player:rankPlayerList){
			response.addPlayer(player.jzBuilder);
		}
		session.write(response.build());
	}
	
	/** 
	 * @Title: getRankById 
	 * @Description: 获取排名
	 * @param id
	 * @param session
	 * @param builder
	 * @return void
	 * @throws 
	 */
	public void getRankById(int id, IoSession session, Builder builder) {
		GetRankReq.Builder request = (GetRankReq.Builder)builder;
		GetRankResp.Builder response = GetRankResp.newBuilder();
		int type = request.getRankType();
		int queryId = request.getId();
		int guojiaId = request.getGuojiaId();
		int rank = 0;
		switch (type) {
		case 1:// 君主榜
			rank = (int)getRankByGjIdAndId(JUNZHU_RANK, guojiaId, queryId);
			break;
		case 2:// 联盟榜
			rank = (int)getRankByGjIdAndId(LIANMENG_RANK, guojiaId, queryId);
			break;
		case 3:// 百战榜
			rank = (int)getRankByGjIdAndId(BAIZHAN_RANK, guojiaId, queryId);
			break;
		case 4:// 过关榜
			rank = (int)getRankByGjIdAndId(GUOGUAN_RANK, guojiaId, queryId);
			break;
		}
		response.setRank(rank);
		session.write(response.build());
	}
	
	/** 
	 * @Title: getRank 
	 * @Description: 获取排行榜
	 * @param cmd
	 * @param session
	 * @param builder
	 * @return void
	 * @throws 
	 */
	public void getRank(int cmd, IoSession session, Builder builder){
		RankingReq.Builder request = (RankingReq.Builder)builder;
		RankingResp.Builder response = RankingResp.newBuilder();
		int pageNo = request.getPageNo();
		int guojiaId = request.getGuojiaId();
		String name = request.getName();
		name = (null==name||name.length()==0)?null:name;
		int type = request.getRankType();
		response.setRankType(type);
		int rankNum = 0;
		switch (type) {
		case 1:// 君主榜
			List<JunZhuInfo.Builder> junList = getJunzhuRank(pageNo, guojiaId, name, type);
			if(junList!=null&&junList.size()!=0){
				for(JunZhuInfo.Builder junzhu:junList){
					response.addJunList(junzhu);
				}
				if(name!=null){// 如果是按名字搜索，计算当前页码
					int rank = junList.get(0).getRank();
					response.setPageNo(rank%PAGE_SIZE==0?rank/PAGE_SIZE:rank/PAGE_SIZE+1);
				}
				rankNum = (int)DB.zcard_(JUNZHU_RANK+"_"+guojiaId);
			}
			break;
		case 2:// 联盟榜
		case 201:
			List<LianMengInfo.Builder> mengList = getLianMengRank(pageNo, guojiaId, name, type);
			if(mengList!=null&&mengList.size()!=0){
				for(LianMengInfo.Builder meng:mengList){
					response.addMengList(meng);
				}
				if(name!=null){// 如果是按名字搜索，计算当前页码
					int rank = mengList.get(0).getRank();
					response.setPageNo(rank%PAGE_SIZE==0?rank/PAGE_SIZE:rank/PAGE_SIZE+1);
				}
			}
			rankNum = (type==2)?(int)DB.zcard_(LIANMENG_RANK+"_"+guojiaId):(int)DB.zcard_(LIANMENG_RANK+"_SW_"+guojiaId);
			break;
		case 3:// 百战榜
			List<BaiZhanInfo.Builder> baizhanList = getBaiZhanRank(pageNo, guojiaId, name, type);
			if(baizhanList!=null&&baizhanList.size()!=0){
				for(BaiZhanInfo.Builder baizhan:baizhanList){
					response.addBaizhanList(baizhan);
				}
				if(name!=null){// 如果是按名字搜索，计算当前页码
					int rank = baizhanList.get(0).getRank();
					response.setPageNo(rank%PAGE_SIZE==0?rank/PAGE_SIZE:rank/PAGE_SIZE+1);
				}
			}
			rankNum = (int)DB.zcard_(BAIZHAN_RANK+"_"+guojiaId);
			break;
		case 4:// 过关榜
			List<GuoGuanInfo.Builder> guoguanList = getGuoGuanRank(pageNo, guojiaId, name, type);
			if(guoguanList!=null&&guoguanList.size()!=0){
				for(GuoGuanInfo.Builder guoguan:guoguanList){
					response.addGuoguanList(guoguan);
				}
				if(name!=null){// 如果是按名字搜索，计算当前页码
					int rank = guoguanList.get(0).getRank();
					response.setPageNo(rank%PAGE_SIZE==0?rank/PAGE_SIZE:rank/PAGE_SIZE+1);
				}
			}
			rankNum = (int)DB.zcard_(GUOGUAN_RANK+"_"+guojiaId);
			break;
		default:
			break;
		}
		if(pageNo!=0&&name==null){// 如果不是按名字搜索，请求的页码就是当前页码
			response.setPageNo(pageNo);
		}
		int pageCount = rankNum%PAGE_SIZE==0?rankNum/PAGE_SIZE:rankNum/PAGE_SIZE+1;
		response.setPageCount(pageCount);
		session.write(response.build());
	}
	
	/** 
	 * @Title: getJunzhuRank 
	 * @Description: 获取君主榜
	 * @param pageNo 当前页码
	 * @param guojiaId 国家id
	 * @param jzName 按君主名查询
	 * @param type 排序类型（1为默认排序）
	 * @return
	 * @return List<JunZhuInfo>
	 * @throws 
	 */
	public List<JunZhuInfo.Builder> getJunzhuRank(int pageNo,int guojiaId,String jzName,int type){
		//		a)	显示玩家战力、等级、军衔三项信息
		//		b)	默认按战力排名，战力相同按等级排名，等级相同按军衔排名
		//		c)	君主榜玩家筛选范围取等级>=20级的玩家，读表
		//		d)	君主榜筛选范围：N，读表
		int start = PAGE_SIZE * (pageNo - 1);
		if(jzName!=null){
			guojiaId=0;// 按全服榜搜索
		}
		if(DB.zcard_(JUNZHU_RANK+"_"+guojiaId)==0){
			return null;
		}
		if(jzName!=null){
			JunZhu jz = HibernateUtil.findByName(JunZhu.class, jzName, " where name='"+jzName+"'");
			if(jz==null){
				return null;
			}
			long rank = getRankByGjIdAndId(JUNZHU_RANK, guojiaId, (int)jz.id);
			if(rank==-1){
				log.info("君主不存在君主榜中");
				return null;
			}
			start = (int)(rank%PAGE_SIZE==0?((rank-1)-(rank-1)%PAGE_SIZE):rank-rank%PAGE_SIZE);
		}
		int end = start + PAGE_SIZE;
		Set<String> junSet = DB.ztop(JUNZHU_RANK+"_"+guojiaId, start, end);
		if(null==junSet||junSet.size()==0){
			return null;
		}
		List<JunZhuInfo.Builder> junList = new ArrayList<JunZhuInfo.Builder>();
		for(String jzIdStr:junSet){
			long jzId = Long.parseLong(jzIdStr);
			JunZhuInfo.Builder jzBuilder = JunZhuInfo.newBuilder();
			JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				continue;
			}
			int rank = ++start;
			if(jz.level>=RANK_MINLEVEL&&rank<=RANK_MAXNUM){// 过滤筛选范围
				jzBuilder.setJunZhuId(jzId);
				jzBuilder.setRank(rank);
				jzBuilder.setGuojiaId(jz.guoJiaId);
				jzBuilder.setName(jz.name);
				jzBuilder.setLianMeng(AllianceMgr.inst.getAlliance(jz));
				// jzBuilder.setZhanli(PvpMgr.inst.getZhanli(jz));
				int zhanli = getZhanliInRedis(jz);
				jzBuilder.setZhanli(zhanli);
				jzBuilder.setLevel(jz.level);
				jzBuilder.setJunxian(PvpMgr.inst.getJunxian(jz));
				int junxianLevel = PvpMgr.getJunxianLevel(jz.id);
				jzBuilder.setJunxianLevel(junxianLevel==-1?1:junxianLevel);
				jzBuilder.setJunxianRank(PvpMgr.inst.getPvpRankById(jzId));
				junList.add(jzBuilder);
			}
		}
		return junList;
	}
	
	public int getZhanliInRedis(JunZhu jz){
		double score = DB.zscore(JUNZHU_RANK+"_"+jz.guoJiaId,String.valueOf(jz.id));
		int zhanli = (int)(score/100000d);
		return zhanli;
	}
	
	/** 
	 * @Title: getLianMengRank 
	 * @Description: 获取联盟榜
	 * @param pageNo 当前页码
	 * @param guojiaId 国家id
	 * @param otherJzName 按联盟名查询
	 * @param type 排序类型（2为默认排序,201按声望排序）
	 * @return
	 * @return List<LianMengInfo>
	 * @throws 
	 */
	public List<LianMengInfo.Builder> getLianMengRank(int pageNo,int guojiaId,String lianmeng,int type){
		//		a)	显示联盟等级、声望值、人数三项信息
		//		b)	默认按等级排名，等级相同按声望值排名，声望相同按人数排名
		int start = PAGE_SIZE * (pageNo - 1);
		if(lianmeng!=null){
			guojiaId=0;// 按全服榜搜索
		}
		if(DB.zcard_(LIANMENG_RANK+"_"+guojiaId)==0){
			return null;
		}
		if(lianmeng!=null){
			AllianceBean alliance = HibernateUtil.find(AllianceBean.class, 
					" where name='"+lianmeng+"'");
			if(alliance==null){
				return null;
			}
			long rank = getRankByGjIdAndId(LIANMENG_RANK, guojiaId, alliance.id);
			if(rank==-1){
				log.info("联盟不存在联盟榜中");
				return null;
			}
			start = (int)(rank%PAGE_SIZE==0?((rank-1)-(rank-1)%PAGE_SIZE):rank-rank%PAGE_SIZE);
		}
		int end = start + PAGE_SIZE;
		Set<String> mengSet = null;
		switch (type) {
		case 2:
			mengSet = DB.ztop(LIANMENG_RANK+"_"+guojiaId, start, end);
			break;
		case 201:// 按声望排序
			mengSet = DB.ztop(LIANMENG_SW_WEEK_RANK+"_"+guojiaId, start, end);
			break;
		default:
			log.info("没有对应的排序类型");
			break;
		}
		if(mengSet==null||mengSet.size()==0){
			return null;
		}
		List<LianMengInfo.Builder> mengList = new ArrayList<LianMengInfo.Builder>();
		for(String lianmengIdStr:mengSet){
			long lianmengId = Long.parseLong(lianmengIdStr);
			LianMengInfo.Builder mengBuilder = LianMengInfo.newBuilder();
			AllianceBean alliance = HibernateUtil.find(AllianceBean.class, lianmengId);
			if(alliance==null){
				continue;
			}
			int rank = ++start;
			if(true){// 过滤筛选范围(联盟暂无筛选条件)
				mengBuilder.setMengId(alliance.id);
				mengBuilder.setRank(rank);
				mengBuilder.setGuoJiaId(alliance.country);
				mengBuilder.setMengName(alliance.name);
				mengBuilder.setLevel(alliance.level);
				long shengwang = getShengwangInRedis(alliance);
				mengBuilder.setShengWang((int)shengwang);
				mengBuilder.setMember(alliance.members);
				mengBuilder.setAllMember(AllianceMgr.inst.getAllianceMemberMax(alliance.level));
				mengList.add(mengBuilder);
			}
		}
		return mengList;
	}
	
	public long getShengwangInRedis(AllianceBean alliance){
		double tmp = DB.zscore(LIANMENG_RANK+"_" + alliance.country,String.valueOf(alliance.id));
		// 等级*1000000000000+声望*1000+成员数量
		long shengwang = (long)(tmp - alliance.level * 1000000000000L - alliance.members)/1000; 
		return shengwang;
	}
	
	/** 
	 * @Title: getBaiZhanRank 
	 * @Description: 获取百战榜
	 * @param pageNo 当前页码
	 * @param guojiaId 国家id
	 * @param jzName 按君主名查询
	 * @param type 排序类型（3为默认排序）
	 * @return
	 * @return List<BaiZhanInfo>
	 * @throws 
	 */
	public List<BaiZhanInfo.Builder> getBaiZhanRank(int pageNo,int guojiaId,String jzName,int type){
		//		a)	显示普通、传奇、评星三项
		//		b)	默认按普通、传奇、评星的顺序排序
		//		c)	君主榜玩家筛选范围取等级>=20级的玩家，读表
		//		d)	君主榜筛选范围：N，读表
		int start = PAGE_SIZE * (pageNo - 1);
		if(jzName!=null){
			guojiaId=0;// 按全服榜搜索
		}
		if(DB.zcard_(BAIZHAN_RANK+"_"+guojiaId)==0){
			return null;
		}
		if(jzName!=null){
			JunZhu jz = HibernateUtil.findByName(JunZhu.class, jzName, " where name='"+jzName+"'");
			if(jz==null){
				return null;
			}
			long rank = DB.zrank(BAIZHAN_RANK+"_"+guojiaId,jz.id+"");
			if(rank == -1){
				log.info("君主不存在百战榜中");
				return null;
			}
			start = (int)(rank%PAGE_SIZE==0?((rank-1)-(rank-1)%PAGE_SIZE):rank-rank%PAGE_SIZE);
		}
		int end = start + PAGE_SIZE;
		Set<String> baiZhanSet = DB.zrange(BAIZHAN_RANK+"_"+guojiaId, start, end);
		if(baiZhanSet==null||baiZhanSet.size()==0){
			return null;
		}
		List<BaiZhanInfo.Builder> baiZhanList = new ArrayList<BaiZhanInfo.Builder>();
		for(String jzIdStr:baiZhanSet){
			long jzId = Long.parseLong(jzIdStr);
			BaiZhanInfo.Builder bzBuilder = BaiZhanInfo.newBuilder();
			JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				continue;
			}
			int rank = ++start;
			if(jz.level>=RANK_MINLEVEL&&rank<=RANK_MAXNUM){// 过滤筛选范围
				bzBuilder.setRank(rank);
				bzBuilder.setGuojiaId(jz.guoJiaId);
				bzBuilder.setJunZhuId(jzId);
				bzBuilder.setName(jz.name);
				bzBuilder.setLianmeng(AllianceMgr.inst.getAlliance(jz));
				bzBuilder.setJunxian(PvpMgr.inst.getJunxian(jz));
				int junxianLevel = PvpMgr.getJunxianLevel(jzId);
				bzBuilder.setJunxianLevel(junxianLevel==-1?1:junxianLevel);
				bzBuilder.setJunxianRank(PvpMgr.inst.getPvpRankById(jzId));
				PvpBean bean = HibernateUtil.find(PvpBean.class, jzId);
				if(bean!=null){
					bzBuilder.setWinCount(bean.allWin);
					bzBuilder.setWeiwang(bean.showWeiWang);
				}
				int wei = ShopMgr.inst.getMoney(ShopMgr.Money.weiWang, jz.id, null);
				bzBuilder.setWeiwang(wei);
				baiZhanList.add(bzBuilder);
			}
		}
		return baiZhanList;
	}
	
	/** 
	 * @Title: getGuoGuanRank 
	 * @Description: 获取过关榜
	 * @param pageNo 当前页码
	 * @param guojiaId 国家id
	 * @param jzName 按君主名查询
	 * @param type 排序类型（4为默认排序）
	 * @return
	 * @return List<GuoGuanInfo>
	 * @throws 
	 */
	public List<GuoGuanInfo.Builder> getGuoGuanRank(int pageNo,int guojiaId,String jzName,int type){
		//		a)	显示军衔、胜场、威望
		//		b)	按军衔排序
		//		c)	君主榜玩家筛选范围取等级>=20级的玩家，读表
		//		d)	君主榜筛选范围：N，读表
		int start = PAGE_SIZE * (pageNo - 1);
		if(jzName!=null){
			guojiaId=0;// 按全服榜搜索
		}
		if(DB.zcard_(GUOGUAN_RANK+"_"+guojiaId)==0){
			return null;
		}
		if(jzName!=null){
			JunZhu jz = HibernateUtil.findByName(JunZhu.class, jzName, " where name='"+jzName+"'");
			if(jz==null){
				return null;
			}
			long rank = getRankByGjIdAndId(GUOGUAN_RANK, guojiaId, (int)jz.id);
			if(rank==-1){
				log.info("君主不存在过关榜中");
				return null;
			}
			start = (int)(rank%PAGE_SIZE==0?((rank-1)-(rank-1)%PAGE_SIZE):rank-rank%PAGE_SIZE);
		}
		int end = start + PAGE_SIZE;
		Set<String> guoGuanSet = DB.ztop(GUOGUAN_RANK+"_"+guojiaId, start, end);
		if(guoGuanSet==null||guoGuanSet.size()==0){
			return null;
		}
		List<GuoGuanInfo.Builder> guoGuanList = new ArrayList<GuoGuanInfo.Builder>();
		for(String jzIdStr:guoGuanSet){
			long jzId = Long.parseLong(jzIdStr);
			GuoGuanInfo.Builder ggBuilder = GuoGuanInfo.newBuilder();
			JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				continue;
			}
			int rank = ++start;
			if(jz.level>=RANK_MINLEVEL&&rank<=RANK_MAXNUM){// 过滤筛选范围
				ggBuilder.setJunZhuId(jzId);
				ggBuilder.setRank(rank);
				ggBuilder.setName(jz.name);
				ggBuilder.setLianmeng(AllianceMgr.inst.getAlliance(jz));
				ggBuilder.setGuojiaId(jz.guoJiaId);
				// pve列表
				List<PveRecord> pveList = HibernateUtil.list(PveRecord.class,"where uid="+jz.id+" order by guanQiaId DESC");
				// 总星数
				int starCount = 0;
				long ptMax=0;
				long cqMax=0;
				for(PveRecord pve:pveList){// 计算总星数和最高传奇关卡最高普通关卡
					starCount=starCount+pve.star+pve.cqStar;
					ptMax = Math.max(ptMax, pve.guanQiaId);
					// 普通关卡
					if(pve.chuanQiPass){
						// 传奇关卡
						cqMax = Math.max(cqMax, pve.guanQiaId);
					}
				}
				// set 普通关卡
				if(ptMax!=0){
					String ptStr = String.valueOf(ptMax);
					ptStr = ptStr.substring(ptStr.length()-4);
					ggBuilder.setPutong(ptStr.substring(0,2)+"-"+ptStr.substring(3,4));
				}
				// set传奇关卡
				if(cqMax!=0){
					String cqStr = String.valueOf(cqMax);
					cqStr = cqStr.substring(cqStr.length()-4);
					ggBuilder.setChuanqi(cqStr.substring(0,2)+"-"+cqStr.substring(3,4));
				}
				// set starCount
				ggBuilder.setStarCount(starCount);
				guoGuanList.add(ggBuilder);
			}
		}
		return guoGuanList;
	}
	
	/** 
	 * @Title: getGuojiaRank 
	 * @Description: 获取国家排行
	 * @param key 排行榜的key
	 * @return
	 * @return List<GuoJiaBean>
	 * @throws 
	 */
	public List<GuoJiaBean> getGuojiaRank(String key){
		Pattern p = Pattern.compile("\\d+");
		List<GuoJiaBean> guojiaList = new ArrayList<GuoJiaBean>();
		Set<String> gjIds = DB.ztop(key, 7); 
		if(gjIds==null){
			return null;
		}
		for(String gjId:gjIds){
			if(p.matcher(gjId).matches()==false){
				log.warn("错误的Redis数据 {} for {}",gjId,key);
				continue;
			}
			GuoJiaBean bean = HibernateUtil.find(GuoJiaBean.class, Integer.parseInt(gjId));
			if(bean!=null){
				bean.shengWang = (int)DB.zscore(key, gjId);
				guojiaList.add(bean);
			}
		}
		return guojiaList;
	}

	/** 
	 * @Title: getLianmengSWRank
	 * @Description: 获取联盟声望排行
	 * @param key 排行榜的key
	 * @param gjId
	 * @return
	 * @return List<AllianceBean>
	 * @throws 
	 */
	public List<AllianceBean> getLianmengSWRank(String key,int gjId){
		List<AllianceBean> allianceList = new ArrayList<AllianceBean>();
		Set<String> mengIds = DB.ztop(key+"_"+gjId, 1000);
		if(mengIds==null||mengIds.size()==0){
			return null;
		}
		for(String mengId:mengIds){
			AllianceBean alliance = HibernateUtil.find(AllianceBean.class, Integer.parseInt(mengId));
			if(alliance!=null){
				alliance.reputation = (int)DB.zscore(key+"_"+gjId, mengId);
				allianceList.add(alliance);
			}
		}
		return allianceList;
	}
	
	/** 
	 * @Title: getJunZhuDetail 
	 * @Description: 获取君主详细信息
	 * @param junBuilder
	 * @param jz
	 * @return
	 * @return JunZhuInfo.Builder
	 * @throws 
	 */
	public JunZhuInfo.Builder getJunZhuDetail(JunZhuInfo.Builder junBuilder,JunZhu jz){
		junBuilder.setJunZhuId(jz.id);
		junBuilder.setRoleId(jz.roleId);
		junBuilder.setLianMeng(AllianceMgr.inst.getAlliance(jz));
		junBuilder.setName(jz.name);
		junBuilder.setGongji(jz.gongJi);
		junBuilder.setZhanli(PvpMgr.inst.getZhanli(jz));
		junBuilder.setLevel(jz.level);
		junBuilder.setFangyu(jz.fangYu);
		junBuilder.setRemainHp(jz.shengMingMax);
		junBuilder.setJunxian(PvpMgr.inst.getJunxian(jz));
		int junxianLevel = PvpMgr.getJunxianLevel(jz.id);
		junBuilder.setJunxianLevel(junxianLevel==-1?1:junxianLevel);
		junBuilder.setGongjin(GuoJiaMgr.inst.getGongJin(jz.id, PvpMgr.getJunxianLevel(jz.id)));
		if(GuoJiaMgr.inst.isCanShangjiao(jz.id)){
			GuoJiaMgr.inst.pushCanShangjiao(jz.id);
		}
		ChengHaoBean cur = HibernateUtil.find(ChengHaoBean.class, "where jzId="+jz.id+" and state='U'");
		if(cur != null){
			junBuilder.setChenghao(cur.tid);
		}		
		junBuilder.setRoleId(jz.roleId);
		junBuilder.setEquip(BagMgr.inst.getEquipInfo(jz.id));
		return junBuilder;
	}
	
	public void addJunZhuRankInfo(RankingResp.Builder resp, JunZhu jz, int rank){
		JunZhuInfo.Builder junInfo = JunZhuInfo.newBuilder();
		long jId = jz.id;
		PvpBean bean = HibernateUtil.find(PvpBean.class, jId);
		if(bean == null){
			log.error("排行榜中：{}的百战数据为空",jz.name);
		}else{
			int zhanli = PvpMgr.inst.getZhanli(jz);
			AllianceBean alli = AllianceMgr.inst.getAllianceByJunZid(jId);
			String mengName = alli == null?"无联盟":alli.name;
			junInfo.setJunZhuId(jId);
			junInfo.setName(jz.name);
			junInfo.setLevel(jz.level); 
			junInfo.setLianMeng(mengName);
//			junInfo.setWinCount(bean.allWin);
			junInfo.setZhanli(zhanli);
			junInfo.setRank(rank);
			junInfo.setGuojiaId(jz.guoJiaId);
			junInfo.setRoleId(jz.roleId);
			resp.addJunList(junInfo);
		}
	} 
	
	/** 
	 * @Title: jzChangeGuojia 
	 * @Description: 君主转国，相应国家榜刷新
	 * @param jzId
	 * @param oldGjId
	 * @param newGjId
	 * @return void
	 * @throws 
	 */
	public void jzChangeGuojia(long jzId,int oldGjId,int newGjId){
		// 君主榜转到相应国家
		int junScore = DB.zscore_(JUNZHU_RANK+"_"+oldGjId, jzId+"");
		DB.zrem(JUNZHU_RANK+"_"+oldGjId, jzId+"");
		DB.zadd(JUNZHU_RANK+"_"+newGjId, junScore, jzId+"");
		// 百战榜转到相应国家
		int bzScore = DB.zscore_(BAIZHAN_RANK+"_"+oldGjId, jzId+"");
		DB.zrem(BAIZHAN_RANK+"_"+oldGjId, jzId+"");
		DB.zadd(BAIZHAN_RANK+"_"+newGjId, bzScore, jzId+"");
		// 过关榜转到相应国家
		int ggScore = DB.zscore_(GUOGUAN_RANK+"_"+oldGjId, jzId+"");
		DB.zrem(GUOGUAN_RANK+"_"+oldGjId, jzId+"");
		DB.zadd(GUOGUAN_RANK+"_"+newGjId, ggScore, jzId+"");
	}
	
	public boolean addLianMengInfo(RankingResp.Builder resp, int mengRank,
			int mengId, AllianceBean alncBean){
		if(alncBean == null){
			alncBean = HibernateUtil.find(AllianceBean.class, mengId);
		}
		LianMengInfo.Builder info = LianMengInfo.newBuilder();
		if(alncBean != null){
			info.setMengId(mengId);
			info.setMengName(alncBean.name);
			info.setMember(alncBean.members);
			int memberMax = AllianceMgr.inst.getAllianceMemberMax(alncBean.level);
			info.setAllMember(memberMax);
			info.setIcon(alncBean.icon);
			info.setLevel(alncBean.level);
			info.setRank(mengRank);
			info.setGuoJiaId(alncBean.country);
			resp.addMengList(info);
			return true;
		}else{
			log.info("联盟排行榜出错，没有找到联盟id:{}的联盟", mengId);
			removeLianmeng(mengId);
			EventMgr.addEvent(ED.REM_LIANMENG_RANK_REFRESH, mengId);
			return false;
		}
	}
	
	public void removeLianmeng(int mengId){
		long resu =  DB.zrem(lianMengLevel, mengId+"");
		if(resu == -1){
			log.error("redis中key={},删除联盟:{}数据失败", lianMengLevel, mengId);
		}else{
			log.info("redis中key={},删除联盟:{}数据成功，删除的数据的排名是:{}", 
					lianMengLevel, mengId, resu);
		}
		// 联盟榜刷新
		remLianmeng(mengId);
	}
	
	public void remLianmeng(int mengId){
		AllianceBean lianmeng = HibernateUtil.find(AllianceBean.class, mengId);
		if(lianmeng==null){
			log.error("redis中key={},删除联盟:{}数据失败,联盟为null", LIANMENG_RANK+"_0", mengId);
			return;
		}
		int guojiaId = lianmeng.country;
		DB.zrem(LIANMENG_RANK+"_0", mengId+"");
		DB.zrem(LIANMENG_RANK+"_"+guojiaId, mengId+"");
		DB.zrem(LIANMENG_SW_DAY_RANK+"_0", mengId+"");
		DB.zrem(LIANMENG_SW_DAY_RANK+"_"+guojiaId, mengId+"");
		DB.zrem(LIANMENG_SW_LAST_DAY_RANK+"_0", mengId+"");
		DB.zrem(LIANMENG_SW_LAST_DAY_RANK+"_"+guojiaId, mengId+"");
		DB.zrem(LIANMENG_SW_WEEK_RANK+"_0", mengId+"");
		DB.zrem(LIANMENG_SW_WEEK_RANK+"_"+guojiaId, mengId+"");
		DB.zrem(LIANMENG_SW_LAST_WEEK_RANK+"_0", mengId+"");
		DB.zrem(LIANMENG_SW_LAST_WEEK_RANK+"_"+guojiaId, mengId+"");
		log.info("已经从redis所有联盟相关榜清除id={}的联盟信息",mengId+"");
	}
	
	@Override
	public void proc(Event event) {
		int id=0;
		JunZhu jz = null;
		long jzId = 0;
		int oldGjId = 0;
		int newGjId = 0;
		GuoJiaBean guoJia = null;
		AllianceBean alliance = null;
		int changeSW = 0;
		if (event.param != null && event.param instanceof Integer) {
			id = (Integer) event.param;
		} else if (event.param != null && event.param instanceof JunZhu) {
			jz = (JunZhu) event.param;
		} else if(event.param==null){
			log.info("排行榜刷新事件无参数");
		}
		switch (event.id) {
		case ED.JUN_RANK_REFRESH:
			resetJunzhuRankRedis(jz);
			break;
		case ED.LIANMENG_RANK_REFRESH:
			resetLianMengRankRedis(id);
			break;
		case ED.REM_LIANMENG_RANK_REFRESH:
			remLianmeng(id);
			break;
		case ED.BAIZHAN_RANK_REFRESH:
			resetBaizhanRankRedis(jz);
			break;
		case ED.GUOGUAN_RANK_REFRESH:
			resetGuoguanRankRedis(jz);
			break;
		case ED.GUOJIA_DAY_RANK_REFRESH:
			if(event.param !=null && event.param instanceof Object[]){
				Object[] obj = (Object[])event.param;
				guoJia = (GuoJiaBean)obj[0];
				changeSW= (Integer)obj[1];
			}
			resetGuojiaDayRankRedis(guoJia, changeSW);;
			break;
		case ED.GUOJIA_WEEK_RANK_REFRESH:
			if(event.param !=null && event.param instanceof Object[]){
				Object[] obj = (Object[])event.param;
				guoJia = (GuoJiaBean)obj[0];
				changeSW= (Integer)obj[1];
			}
			resetGuojiaWeekRankRedis(guoJia, changeSW);
			break;
		case ED.GUOJIA_DAY_RANK_RESET:
			guojiaDayRankReset();
			break;
		case ED.GUOJIA_WEEK_RANK_RESET:
			guojiaWeekRankReset();
			/*
			 * 设置了上周排行之后进行发放周排行奖励
			 */
			List<Long> list = SessionManager.inst.getAllOnlineJunZhuId();
			if(list != null){
				for (Long junid: list){
					if(junid == null){ continue; }
					jz = HibernateUtil.find(JunZhu.class, id);
					GuoJiaMgr.inst.sendWeekRankWard(jz);
				}
			}
			break;
		case ED.LIANMENG_DAY_RANK_REFRESH:
			if(event.param !=null && event.param instanceof Object[]){
				Object[] obj = (Object[])event.param;
				alliance = (AllianceBean)obj[0];
				changeSW= (Integer)obj[1];
			}
			resetLianmengSWDayRankRedis(alliance, changeSW);
			break;
		case ED.LIANMENG_WEEK_RANK_REFRESH:
			if(event.param !=null && event.param instanceof Object[]){
				Object[] obj = (Object[])event.param;
				alliance = (AllianceBean)obj[0];
				changeSW= (Integer)obj[1];
			}
			resetLianmengSWWeekRankRedis(alliance, changeSW);
			break;
		case ED.LIANMENG_DAY_RANK_RESET:
			lianMengBySWDayRankReset();
			break;
		case ED.LIANMENG_WEEK_RANK_RESET:
			lianMengBySWWeekRankReset();
			break;
		case ED.CHANGE_GJ_RANK_REFRESH:
			if(event.param !=null && event.param instanceof Object[]){
				Object[] obj = (Object[])event.param;
				jzId = (Long)obj[0];
				oldGjId = (Integer)obj[1];
				newGjId = (Integer)obj[2];
			}
			jzChangeGuojia(jzId, oldGjId, newGjId);
			break;
		}
	}

	@Override
	protected void doReg() {// 注册榜刷新事件
		EventMgr.regist(ED.JUN_RANK_REFRESH, this);
		EventMgr.regist(ED.LIANMENG_RANK_REFRESH, this);
		EventMgr.regist(ED.BAIZHAN_RANK_REFRESH, this);
		EventMgr.regist(ED.GUOGUAN_RANK_REFRESH, this);
		EventMgr.regist(ED.REM_LIANMENG_RANK_REFRESH, this);
		EventMgr.regist(ED.GUOJIA_DAY_RANK_REFRESH, this);
		EventMgr.regist(ED.GUOJIA_WEEK_RANK_REFRESH, this);
		EventMgr.regist(ED.GUOJIA_DAY_RANK_RESET, this);
		EventMgr.regist(ED.GUOJIA_WEEK_RANK_RESET, this);
		EventMgr.regist(ED.CHANGE_GJ_RANK_REFRESH, this);
		EventMgr.regist(ED.LIANMENG_DAY_RANK_RESET, this);
		EventMgr.regist(ED.LIANMENG_DAY_RANK_REFRESH, this);
		EventMgr.regist(ED.LIANMENG_WEEK_RANK_RESET, this);
		EventMgr.regist(ED.LIANMENG_WEEK_RANK_REFRESH, this);
	}
}
