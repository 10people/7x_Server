package com.qx.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.SessionManager;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.bag.Bag;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.chonglou.ChongLouMgr;
import com.qx.chonglou.ChongLouRecord;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.guojia.GuoJiaBean;
import com.qx.guojia.GuoJiaMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.ChengHaoBean;
import com.qx.junzhu.JunZhu;
import com.qx.mibao.v2.MiBaoV2Mgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pve.PveRecord;
import com.qx.pvp.PvpBean;
import com.qx.pvp.PvpMgr;

import qxmobile.protobuf.Ranking.AlliancePlayerReq;
import qxmobile.protobuf.Ranking.AlliancePlayerResp;
import qxmobile.protobuf.Ranking.BaiZhanInfo;
import qxmobile.protobuf.Ranking.ChongLouInfo;
import qxmobile.protobuf.Ranking.GetRankReq;
import qxmobile.protobuf.Ranking.GetRankResp;
import qxmobile.protobuf.Ranking.GuoGuanInfo;
import qxmobile.protobuf.Ranking.JunZhuInfo;
import qxmobile.protobuf.Ranking.LianMengInfo;
import qxmobile.protobuf.Ranking.RankingReq;
import qxmobile.protobuf.Ranking.RankingResp;
import redis.clients.jedis.Tuple;

/**
 * 排行榜管理类 This class is used for ...
 * 
 * @author wangZhuan
 * @version 9.0, 2015年2月4日 下午3:12:07
 */
public class RankingMgr extends EventProc{
	public static Logger log = LoggerFactory.getLogger(RankingMgr.class.getSimpleName());
	public static final Redis DB = Redis.getInstance();
	public static String JUNZHU_RANK = "junzhu_" + GameServer.serverId;// Redis君主榜
	public static String CHONGLOU_RANK = "chonglou_" + GameServer.serverId;// Redis重楼榜
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
	public static String JUNZHU_LEVEL_RANK = "junzhu_level_" + GameServer.serverId;// Redis君主等级榜
	public static String zhanliRank = "zhanli_" + GameServer.serverId;
	public static String lianMengLevel = "mengLevel_" + GameServer.serverId;
	public static RankingMgr inst;
	public static int number = 100;
	public static int lianmengNumber = 20;
	public static int PAGE_SIZE = 20;// 每页数目
	public static int RANK_MAXNUM = 200;// 君主筛选范围
	public static int RANK_MINLEVEL = 20;// 君主筛选最低等级
	public static int CHONGLOU_JUNZHU_MIN_LEVEL = 20;	// 重楼榜君主等级最低要求
	public static int GUOGUAN_JUNZHU_MIN_LEVEL = 20;	// 过关榜君主等级最低要求
	public static int BAIZHAN_JUNZHU_MIN_LEVEL = 20;	// 百战榜君主等级最低要求

	public RankingMgr() {
		initData();
		inst = this;
		new RankingGongJinMgr();
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
		CHONGLOU_JUNZHU_MIN_LEVEL = CanShu.CHONGLOU_RANK_MINLEVEL;
		GUOGUAN_JUNZHU_MIN_LEVEL = CanShu.GUOGUAN_RANK_MINLEVEL;
		BAIZHAN_JUNZHU_MIN_LEVEL = CanShu.BAIZHAN_RANK_MINLEVEL;
	}
	
	public void resetLevelRankRedis(JunZhu jz) {
		if (jz == null) {
			log.error("resetLevelRankRedis 参数为null");
			return;
		}
		DB.zadd(JUNZHU_LEVEL_RANK, jz.level, jz.id + "");
		log.info("君主：{}登录：{}添加到redisd,key={}", jz.name, jz.level, JUNZHU_LEVEL_RANK);
		int size = (int) DB.zcard_(zhanliRank);
		log.info("君主等级排行榜的size是： {}", size);
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
				lianmengId, level, lianMengLevel, level);
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
		long junxianLevel = PvpMgr.getJunxianLevel(jz.id);
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
	
	public void resetChongLouRank(JunZhu jz, ChongLouRecord record, int oldGuoJiaId) {
		if (jz == null) {
			log.error("resetJunzhuRankRedis 参数为null");
			return;
		}
		if(jz.level < CHONGLOU_JUNZHU_MIN_LEVEL) {
			return;
		}
		int guojiaId = jz.guoJiaId;
		if(guojiaId != oldGuoJiaId) {
			// 移除旧国家排名
			removeChongLouPreRank(jz, oldGuoJiaId);
		}
		removeChongLouPreRank(jz, 0);
		removeChongLouPreRank(jz, guojiaId);
		
		long finishTime = record.highestLevelFirstTime == null ? System.currentTimeMillis() : record.highestLevelFirstTime.getTime();
		finishTime = Long.MAX_VALUE - finishTime;
		DB.zadd(CHONGLOU_RANK+"_0", record.highestLevel, finishTime + "_" +jz.id);// 添加到全部排名
		DB.zadd(CHONGLOU_RANK+"_"+guojiaId, record.highestLevel, finishTime + "_" +jz.id);// 添加在相应国家排名
		log.info("重楼榜==>君主：{} 重楼挑战层数：{} 添加到周国排名中,key={}", jz.name, record.highestLevel, CHONGLOU_RANK+"_0");
		log.info("重楼榜==>君主：{} 重楼挑战层数：{} 添加到国家{}排名中,key={}", jz.name, record.highestLevel, guojiaId,CHONGLOU_RANK+"_"+guojiaId);
		int allSize = (int) DB.zcard_(CHONGLOU_RANK+"_0");
		log.info("周国重楼排行榜的size是： {}", allSize);
		int gjSize = (int) DB.zcard_(CHONGLOU_RANK+"_"+guojiaId);
		log.info("国家{}重楼排行榜的size是： {}", guojiaId,gjSize);
	}

	public void removeChongLouPreRank(JunZhu jz, int guojiaId) {
		Set<String> memberList = DB.zrange(CHONGLOU_RANK + "_" + guojiaId);;
		if(memberList == null) {
			return;
		}
		for(String member : memberList) {
			String[] memberArray = member.split("_");
			if(member.length() < 2) {
				continue;
			}
			long jzId = Long.parseLong(memberArray[1]);
			if(jzId == jz.id) {
				DB.zrem(CHONGLOU_RANK + "_" + guojiaId, member);// 移除旧排名
			}
		}
	}
	

	/** 
	 * @Title: resetLianMengLevelRedis 
	 * @Description: 刷新联盟榜
	 * @param lianmengId
	 * @return
	 * @return long
	 * @throws 
	 */
	public void resetLianMengRankRedis(Object obj){
		Integer lianmengId = 0;
		if(obj instanceof Integer) {
			lianmengId = (Integer) obj;
		} else if(obj instanceof Object[]) {
			Object[] params = (Object[]) obj;
			lianmengId = (Integer) params[0];
		}
		
		AllianceBean lianmeng = HibernateUtil.find(AllianceBean.class, lianmengId);
		if(lianmeng==null){
			log.error("resetLianMengRankRedis 参数为null");
			return;
		}
		if(obj instanceof Object[]) {
			Object[] params = (Object[]) obj;
			Integer oldGuoJiaId = (Integer) params[1];
			if(lianmeng.country != oldGuoJiaId) {
				DB.zrem(LIANMENG_RANK+"_" + oldGuoJiaId, lianmengId+"");// 移除旧国家的排名
			}
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
	public void resetBaizhanRankRedis(Object obj){
		Object[] params = (Object[]) obj;
		JunZhu jz = (JunZhu) params[0];
		Integer oldGuoJiaId = (Integer) params[1];
		if (jz == null) {
			log.error("resetBaizhanRankRedis 参数为null");
			return;
		}
		if(jz.level<BAIZHAN_JUNZHU_MIN_LEVEL){
			log.info("百战榜刷新错误，君主等级不满足最低限制:{}级",BAIZHAN_JUNZHU_MIN_LEVEL);
			return;
		}
		if(jz.guoJiaId != oldGuoJiaId) {
			DB.zrem(BAIZHAN_RANK+"_"+oldGuoJiaId, jz.id + "");// 移除旧国家排名
		}
		long junxianLevel = PvpMgr.getJunxianLevel(jz.id);
		long junxianRank = PvpMgr.inst.getPvpRankById(jz.id);
		if(junxianRank <= 0) {
			return;
		}
		int guojiaId = jz.guoJiaId;
		DB.zadd(BAIZHAN_RANK+"_0", junxianRank, jz.id+"");// 全服百战榜
		DB.zadd(BAIZHAN_RANK+"_"+guojiaId, junxianRank, jz.id + "");// 全国百战榜
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
	public void resetGuoguanRankRedis(Object obj){
		Object[] params = (Object[]) obj;
		JunZhu jz = (JunZhu) params[0];
		Integer oldGuoJiaId = (Integer) params[1];
		if (jz == null) {
			log.error("resetGuoguanRankRedis 参数为null");
			return;
		}
		if(jz.level < GUOGUAN_JUNZHU_MIN_LEVEL){
			log.info("过关榜刷新错误，君主等级不满足最低限制:{}级",GUOGUAN_JUNZHU_MIN_LEVEL);
			return;
		}
		if(jz.guoJiaId != oldGuoJiaId) {
			DB.zrem(GUOGUAN_RANK+"_"+oldGuoJiaId, jz.id + "");// 全国百战榜
		}
		List<PveRecord> pveList = HibernateUtil.list(PveRecord.class,"where uid="+jz.id+" order by guanQiaId DESC");
		long starCount = BigSwitch.pveGuanQiaMgr.getAllGuanQiaStartSum(pveList);
		long ptMax=0;
		long cqMax=0;
		for(PveRecord pve:pveList){// 计算总星数和最高传奇关卡最高普通关卡
//			starCount=starCount+pve.star+pve.cqStar;
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
	


	public void printPaiHang(Map<String, Double> map) {
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			String idN = entry.getKey();
			Double value = entry.getValue();
			log.info("君主id: {}, 姓名: {}, value: {}", idN.split("_")[0],
					idN.split("_")[1], value);
		}
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
	public long getRankByGjIdAndId(String key,int gjId, long junzhuId){
		long rank = 0;
		if(key.startsWith(BAIZHAN_RANK)){
			rank = DB.zrank(key+"_"+gjId, junzhuId+"");
		} else if(key.startsWith(CHONGLOU_RANK)) {
			rank = getRankInChongLouByGuojia(gjId, junzhuId, rank);
		} else{
			rank = DB.zrevrank(key+"_"+gjId, junzhuId+"");
		}
		return rank;
	}
	
	/**
	 * 获取某个国家君主所在重楼排行榜的排名
	 * @param gjId
	 * @param junzhuId
	 * @param rank
	 * @return
	 */
	public long getRankInChongLouByGuojia(int gjId, long junzhuId, long rank) {
		Set<String> memberList = DB.zrange(CHONGLOU_RANK + "_" + gjId);;
		if(memberList == null) {
			return rank;
		}
		for(String member : memberList) {
			String[] memberArray = member.split("_");
			if(member.length() < 2) {
				continue;
			}
			long jzId = Long.parseLong(memberArray[1]);
			if(jzId == junzhuId) {
				rank = DB.zrevrank(CHONGLOU_RANK + "_" + gjId, member);// 移除旧排名
				return rank;
			}
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

		AlliancePlayerResp.Builder response = AlliancePlayerResp.newBuilder();
		List<AlliancePlayer> playerList = AllianceMgr.inst.getAllianceMembers(mengId);
		if(playerList==null||playerList.size()==0){
			session.write(response.build());
			return;
		}
		List<RankAlliancePlayer> rankPlayerList = new ArrayList<RankAlliancePlayer>();
		for(AlliancePlayer player:playerList){
			JunZhu jz = HibernateUtil.find(JunZhu.class, player.junzhuId);
			JunZhuInfo.Builder jzBuilder = JunZhuInfo.newBuilder();
			jzBuilder.setJunZhuId(jz.id);
			jzBuilder.setName(jz.name);
			jzBuilder.setJob(player.title);
			jzBuilder.setLevel(jz.level);
			jzBuilder.setGongxian(player.gongXian);
			jzBuilder.setChongLouLayer(ChongLouMgr.inst.getChongLouHighestLayer(jz));
			jzBuilder.setZhanli(getZhanliInRedis(jz));
			jzBuilder.setGongjin(RankingGongJinMgr.inst.getJunZhuGongJin(jz.id));
			int junxianLevel = PvpMgr.getJunxianLevel(jz.id);
			jzBuilder.setJunxianLevel(junxianLevel==-1?1:junxianLevel);
			jzBuilder.setJunxianRank(PvpMgr.inst.getPvpRankById(jz.id));
			jzBuilder.setJunxian(PvpMgr.inst.getJunxian(jz));
//			if(GuoJiaMgr.inst.isCanShangjiao(jz.id)){
//				GuoJiaMgr.inst.pushCanShangjiao(jz.id);
//			}
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
		case 7:// 重楼榜
			rank = (int)getRankByGjIdAndId(CHONGLOU_RANK, guojiaId, queryId);
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
		int type = request.getRankType();
		response.setRankType(type);
		String name = request.getName();
		name = (null==name||name.length()==0)?null:name;
		if(name!=null){
			log.info("查询排行字符串处理前为---{}",name);
			name=name.replace("'", "").replace("\"", "");
			log.info("查询排行字符串处理后结果为---{}",name);
			if(type == 2  || type == 201) {//联盟榜
				AllianceBean alliance = HibernateUtil.find(AllianceBean.class, " where name='" + name+"'", false);
				if(alliance == null) {
					sendGetRankByNameFailMsg(session, response, 1);
					return;
				}
			} else {
				JunZhu findJz = HibernateUtil.find(JunZhu.class, " where name='" + name +"'", false);
				if(findJz == null) {
					sendGetRankByNameFailMsg(session, response, 1);
					return;
				}
			}
		}
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
			} else {
				if(name!=null && !"".equals(name)) {
					sendGetRankByNameFailMsg(session, response, 2);
					return;
				}
			}
			break;
		case 2:// 联盟榜
		case 201:
			List<LianMengInfo.Builder> mengList = getLianMengRank(pageNo, guojiaId, name, type);
			if(mengList!=null&&mengList.size()!=0){
				for(LianMengInfo.Builder meng:mengList){
					meng.setShengWang(AllianceMgr.inst.getCaptureCityCount(meng.getMengId()));
					response.addMengList(meng);
				}
				if(name!=null){// 如果是按名字搜索，计算当前页码
					int rank = mengList.get(0).getRank();
					response.setPageNo(rank%PAGE_SIZE==0?rank/PAGE_SIZE:rank/PAGE_SIZE+1);
				}
			}else {
				if(name!=null && !"".equals(name)) {
					sendGetRankByNameFailMsg(session, response, 2);
					return;
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
			}else {
				if(name!=null && !"".equals(name)) {
					sendGetRankByNameFailMsg(session, response, 2);
					return;
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
			}else {
				if(name!=null && !"".equals(name)) {
					sendGetRankByNameFailMsg(session, response, 2);
					return;
				}
			}
			rankNum = (int)DB.zcard_(GUOGUAN_RANK+"_"+guojiaId);
			break;
		case 5: //贡金个人排行榜
			RankingGongJinMgr.inst.sendPersonalGongJinRank(pageNo, response, session);
			rankNum = (int)DB.zcard_(RankingGongJinMgr.gongJinPersonalRank);
			break;
		case 6://贡金联盟排行榜
			RankingGongJinMgr.inst.sendAllianceGongJinRank(pageNo, response, session);
			rankNum = (int)DB.zcard_(RankingGongJinMgr.gongJinAllianceRank);
			break;
		case 7://重楼排行榜
			List<ChongLouInfo.Builder> chonglouList = getChongLouRank(pageNo, guojiaId, name, type);
			if(chonglouList!=null && chonglouList.size()!=0){
				for(ChongLouInfo.Builder guoguan:chonglouList){
					response.addChongLouList(guoguan);
				}
				if(name!=null){// 如果是按名字搜索，计算当前页码
					int rank = chonglouList.get(0).getRank();
					response.setPageNo(rank%PAGE_SIZE==0?rank/PAGE_SIZE:rank/PAGE_SIZE+1);
				}
			}else {
				if(name!=null && !"".equals(name)) {
					sendGetRankByNameFailMsg(session, response, 2);
					return;
				}
			}
			rankNum = (int)DB.zcard_(CHONGLOU_RANK+"_"+guojiaId);
			break;
		default:
			break;
		}
		if(pageNo!=0&&name==null){// 如果不是按名字搜索，请求的页码就是当前页码
			response.setPageNo(pageNo);
		}
		int pageCount = rankNum%PAGE_SIZE==0?rankNum/PAGE_SIZE:rankNum/PAGE_SIZE+1;
		response.setResult(0);
		response.setPageCount(pageCount);
		session.write(response.build());
	}

	public void sendGetRankByNameFailMsg(IoSession session, RankingResp.Builder response, int result) {
		response.setResult(result);
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
			long rank = getRankByGjIdAndId(JUNZHU_RANK, guojiaId, jz.id);
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
			int rank =0;
			// 去掉等级限制，否则会出排行榜数据显示排行和redis中数据不符合的bug
			if(rank<=RANK_MAXNUM){// 过滤筛选范围
				rank = ++start;
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
	
	public List<ChongLouInfo.Builder> getChongLouRank(int pageNo,int guojiaId,String jzName,int type){
		int start = PAGE_SIZE * (pageNo - 1);
		if(jzName!=null){
			guojiaId=0;// 按全服榜搜索
		}
		if(jzName!=null){
			JunZhu jz = HibernateUtil.findByName(JunZhu.class, jzName, " where name='"+jzName+"'");
			if(jz==null){
				return null;
			}
			long rank = getRankByGjIdAndId(CHONGLOU_RANK, guojiaId, jz.id);
			if(rank==-1){
				log.info("君主不存在君主榜中");
				return null;
			}
			start = (int)(rank%PAGE_SIZE==0?((rank-1)-(rank-1)%PAGE_SIZE):rank-rank%PAGE_SIZE);
		}
		int end = start + PAGE_SIZE;
		Set<String> junSet = DB.ztop(CHONGLOU_RANK+"_"+guojiaId, start, end);
		if(null==junSet||junSet.size()==0){
			loadChongLouRank();
			junSet = DB.ztop(CHONGLOU_RANK+"_"+guojiaId, start, end);
		}
		List<ChongLouInfo.Builder> chonglouList = new ArrayList<ChongLouInfo.Builder>();
		for(String member : junSet){
			String[] memberArray = member.split("_");
			long jzId = Long.parseLong(memberArray[1]);
			ChongLouInfo.Builder chonglouBuilder = ChongLouInfo.newBuilder();
			JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				continue;
			}
			ChongLouRecord chongLouRecord = ChongLouMgr.inst.getChongLouRecord(jz); 
			int rank =0;
			// 去掉等级限制，否则会出排行榜数据显示排行和redis中数据不符合的bug
			if(rank<=RANK_MAXNUM){// 过滤筛选范围
				rank = ++start;
				chonglouBuilder.setJunZhuId(jzId);
				chonglouBuilder.setRank(rank);
				chonglouBuilder.setGuojiaId(jz.guoJiaId);
				chonglouBuilder.setName(jz.name);
				chonglouBuilder.setLevel(jz.level);
				chonglouBuilder.setLianMeng(AllianceMgr.inst.getAlliance(jz));
				chonglouBuilder.setLayer(chongLouRecord.highestLevel);
				chonglouBuilder.setTime(DateUtils.date2Text(chongLouRecord.highestLevelFirstTime, "yyyy-MM-dd"));
				chonglouList.add(chonglouBuilder);
			}
		}
		return chonglouList;
	}

	public void loadChongLouRank() {
		List<ChongLouRecord> recordList = HibernateUtil.list(ChongLouRecord.class, " where highestLevel>0");
		for(ChongLouRecord record : recordList) {
			if(record.junzhuId % 1000 != GameServer.serverId) {
				continue;
			}
			JunZhu jz = HibernateUtil.find(JunZhu.class, record.junzhuId);
			resetChongLouRank(jz, record, jz.guoJiaId);
		}
	}
	
	public void loadBaizhanRank() {
		List<PvpBean> recordList = HibernateUtil.list(PvpBean.class,"");
		for(PvpBean pvpBean : recordList) {
			if(pvpBean.junZhuId % 1000 != GameServer.serverId) {
				continue;
			}
			JunZhu jz = HibernateUtil.find(JunZhu.class, pvpBean.junZhuId);
			resetBaizhanRankRedis(new Object[]{jz, jz.guoJiaId});
		}
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
				mengBuilder.setShengWang(alliance.reputation);
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
			int rank = 0;
			if(rank<=RANK_MAXNUM){// 过滤筛选范围
				rank = ++start;
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
			long rank = getRankByGjIdAndId(GUOGUAN_RANK, guojiaId, jz.id);
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
			int rank = 0;
			if(rank<=RANK_MAXNUM){// 过滤筛选范围
				rank =++start;
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
				starCount=BigSwitch.inst.pveGuanQiaMgr.getAllGuanQiaStartSum(pveList);
				for(PveRecord pve:pveList){// 计算总星数和最高传奇关卡最高普通关卡
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
	 * @Title: getTopJunzhuAvgLevel 
	 * @Description: 获取等级排行前num位君主平均等级
	 * @param num
	 * @return
	 * @return double
	 * @throws 
	 */
	public double getTopJunzhuAvgLevel(int num){
		Set<Tuple> tops = DB.ztopWithScore(JUNZHU_LEVEL_RANK, num);
		if(tops == null || tops.size()==0){
			return 1;
		}
		double sumLv = 0;
		for(Tuple t : tops){
			sumLv += t.getScore();
		}
		return sumLv / tops.size();
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
		String lianmename = AllianceMgr.inst.getAlliance(jz);
		junBuilder.setLianMeng(lianmename);
		junBuilder.setName(jz.name);
		junBuilder.setGongji(jz.gongJi);
		junBuilder.setZhanli(PvpMgr.inst.getZhanli(jz));
		junBuilder.setLevel(jz.level);
		junBuilder.setFangyu(jz.fangYu);
		junBuilder.setRemainHp(jz.shengMingMax);
		junBuilder.setGuojiaId(jz.guoJiaId);
		junBuilder.setJunxian(PvpMgr.inst.getJunxian(jz));
		int junxianLevel = PvpMgr.getJunxianLevel(jz.id);
		junBuilder.setJunxianLevel(junxianLevel==-1?1:junxianLevel);
		junBuilder.setGongjin(RankingGongJinMgr.inst.getJunZhuGongJin(jz.id));
//		if(GuoJiaMgr.inst.isCanShangjiao(jz.id)){
//			GuoJiaMgr.inst.pushCanShangjiao(jz.id);
//		}
		ChengHaoBean cur = HibernateUtil.find(ChengHaoBean.class, "where jzId="+jz.id+" and state='U'");
		if(cur != null){
			junBuilder.setChenghao(cur.tid);
		}		
		junBuilder.setRoleId(jz.roleId);
		Bag<EquipGrid> bag = EquipMgr.inst.loadEquips(jz.id);
		junBuilder.setEquip(BagMgr.inst.getEquipInfo(bag));
		junBuilder.setMibaoInfoResp(MiBaoV2Mgr.inst.getMibaoInfoResp(jz, new AtomicBoolean()));
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
			String mengName = alli == null?"":alli.name;
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
	public void jzChangeGuojia(long jzId,int oldGjId,int newGjId, int jLevel){
		// 君主榜转到相应国家
		// 等级符合条件下，君主榜、百战榜、过关榜转到相应国家,
		
		if(jLevel >= GUOGUAN_JUNZHU_MIN_LEVEL){
			// 过关榜转到相应国家
			double ggScore = DB.zscore(GUOGUAN_RANK+"_"+oldGjId, jzId+"");
			DB.zrem(GUOGUAN_RANK+"_"+oldGjId, jzId+"");
			DB.zadd(GUOGUAN_RANK+"_"+newGjId, ggScore, jzId+"");
		}
		if(jLevel >= RANK_MINLEVEL){
			double junScore = DB.zscore(JUNZHU_RANK+"_"+oldGjId, jzId+"");
			DB.zrem(JUNZHU_RANK+"_"+oldGjId, jzId+"");
			DB.zadd(JUNZHU_RANK+"_"+newGjId, junScore, jzId+"");
		}
		if(jLevel >= BAIZHAN_JUNZHU_MIN_LEVEL) {
			double bzScore = DB.zscore(BAIZHAN_RANK+"_"+oldGjId, jzId+"");
			DB.zrem(BAIZHAN_RANK+"_"+oldGjId, jzId+"");
			DB.zadd(BAIZHAN_RANK+"_"+newGjId, bzScore, jzId+"");
			// 重楼榜
		}
		if(jLevel >= CHONGLOU_JUNZHU_MIN_LEVEL) {
			double chonglouScore = DB.zscore(CHONGLOU_RANK+"_"+oldGjId, jzId+"");
			DB.zrem(CHONGLOU_RANK+"_"+oldGjId, jzId+"");
			DB.zadd(CHONGLOU_RANK+"_"+newGjId, chonglouScore, jzId+"");
		}
	}
	
//	public boolean addLianMengInfo(RankingResp.Builder resp, int mengRank,
//			int mengId, AllianceBean alncBean){
//		if(alncBean == null){
//			alncBean = HibernateUtil.find(AllianceBean.class, mengId);
//		}
//		LianMengInfo.Builder info = LianMengInfo.newBuilder();
//		if(alncBean != null){
//			info.setMengId(mengId);
//			info.setMengName(alncBean.name);
//			info.setMember(alncBean.members);
//			int memberMax = AllianceMgr.inst.getAllianceMemberMax(alncBean.level);
//			info.setAllMember(memberMax);
//			info.setIcon(alncBean.icon);
//			info.setLevel(alncBean.level);
//			info.setRank(mengRank);
//			info.setGuoJiaId(alncBean.country);
//			resp.addMengList(info);
//			return true;
//		}else{
//			log.info("联盟排行榜出错，没有找到联盟id:{}的联盟", mengId);
//			removeLianmeng(mengId);
//			EventMgr.addEvent(ED.REM_LIANMENG_RANK_REFRESH, mengId);
//			return false;
//		}
//	}
	
//	public void removeLianmeng(int mengId){
//		long resu =  DB.zrem(lianMengLevel, mengId+"");
//		if(resu == -1){
//			log.error("redis中key={},删除联盟:{}数据失败", lianMengLevel, mengId);
//		}else{
//			log.info("redis中key={},删除联盟:{}数据成功，删除的数据的排名是:{}", 
//					lianMengLevel, mengId, resu);
//		}
//		// 联盟榜刷新
//		remLianmeng(mengId);
//	}
	
	public void remLianmeng(int mengId, int guojiaId){
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
		// 20151211
		DB.zrem(RankingGongJinMgr.gongJinAllianceRank, mengId+"");
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
			resetLianMengRankRedis(event.param);
			break;
		case ED.BAIZHAN_RANK_REFRESH:
			resetBaizhanRankRedis(event.param);
			break;
		case ED.GUOGUAN_RANK_REFRESH:
			resetGuoguanRankRedis(event.param);
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
			int jlevel = 0;
			if(event.param !=null && event.param instanceof Object[]){
				Object[] obj = (Object[])event.param;
				jzId = (Long)obj[0];
				oldGjId = (Integer)obj[1];
				newGjId = (Integer)obj[2];
				jlevel = (Integer)obj[3];
			}
			jzChangeGuojia(jzId, oldGjId, newGjId, jlevel);
			break;
		case ED.JUNZHU_LEVEL_RANK_REFRESH:
			resetLevelRankRedis(jz);
			break;
		case ED.CHONGLOU_RANK_REFRESH:
			if(event.param != null && event.param instanceof Object[]){
				Object[] obj = (Object[])event.param;
				JunZhu junZhu = (JunZhu)obj[0];
				int oldGuoJiaId = (Integer)obj[1];
				ChongLouRecord record = HibernateUtil.find(ChongLouRecord.class, junZhu.id);
				if(record == null || record.highestLevel <= 0) {
					return;
				}
				resetChongLouRank(junZhu, record, oldGuoJiaId);
			}
			break;
		}
	}

	@Override
	protected void doReg() {// 注册榜刷新事件
		EventMgr.regist(ED.JUN_RANK_REFRESH, this);
		EventMgr.regist(ED.LIANMENG_RANK_REFRESH, this);
		EventMgr.regist(ED.BAIZHAN_RANK_REFRESH, this);
		EventMgr.regist(ED.GUOGUAN_RANK_REFRESH, this);
		EventMgr.regist(ED.GUOJIA_DAY_RANK_REFRESH, this);
		EventMgr.regist(ED.GUOJIA_WEEK_RANK_REFRESH, this);
		EventMgr.regist(ED.GUOJIA_DAY_RANK_RESET, this);
		EventMgr.regist(ED.GUOJIA_WEEK_RANK_RESET, this);
		EventMgr.regist(ED.CHANGE_GJ_RANK_REFRESH, this);
		EventMgr.regist(ED.LIANMENG_DAY_RANK_RESET, this);
		EventMgr.regist(ED.LIANMENG_DAY_RANK_REFRESH, this);
		EventMgr.regist(ED.LIANMENG_WEEK_RANK_RESET, this);
		EventMgr.regist(ED.LIANMENG_WEEK_RANK_REFRESH, this);
		EventMgr.regist(ED.JUNZHU_LEVEL_RANK_REFRESH, this);
		EventMgr.regist(ED.CHONGLOU_RANK_REFRESH, this);
	}
}
