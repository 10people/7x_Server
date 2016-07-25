package com.qx.explore;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;


@Entity
@Table(name="tan_bao")
public class ExploreMine implements DBHash{

	@Id
	// id = junzhuId * 100 + type
	// 探宝类型：type == 1-铜币抽奖， 2-元宝抽奖
	public long id;
	// 上次免费抽奖时间
	public Date lastFreeGetTime;
	// 当日免费抽奖次数
	public int usedFreeNumber;
	// 付费抽奖累计概率, <= 1000;
	public int totalProbability;


	/*
	 * 元宝抽奖：(historyFree ==1 && historyPay == 1)以后，
	 * historyBaoDi从1开始累加, 而historyFree以及historyPay不再update
	 * 
	 * 铜币抽奖：无论付费或者免费单抽，或者十抽，historyFree从1开始累加计算。另两个字段无关。
	 * 
	 */
	public int historyFree;
	// 元宝抽：付费次数
	public int historyPay;
	// 元宝抽：保底次数
	public int historyBaoDi;
	
	public int danChouClickNumber; //单抽点击次数
	public int tenChouClickNumber; //十连抽点击次数
	@Override
	public long hash() {
		return id;
	}
//	// 免费单抽或者联盟单抽是否已经领取过好的奖励了: 
//	// add 包括10连抽是否已经领过好的奖励
//	@Column(columnDefinition = "boolean default false")
//	public boolean hasGoodAwar;

//	public ExploreMine() {
//		super();
//	}

//	/**
//	 * 新建初始化
//	 * @Title: ExploreMine 
//	 * @Description:
//	 * @param junZhuId
//	 * @param type
//	 */
//	public ExploreMine(long id, int type){
//		this.id =  id;
//		this.type  = type;
//		this.times = 0;
//		// 免费单抽 和付费单抽的首次抽奖不一样
//		if (this.type == ExploreConstant.SIGLE ||
//				this.type == ExploreConstant.FREE)
//		{
//			allTimes = -1;
//		}
//		else this.allTimes = 0;
//		this.lastGetTime = null;	
//	}
//
//	/**
//	 *  当日共拥有的免费次数
//	 * @Title: setExactTimes 
//	 * @Description:
//	 */
//	public int getTotalTimes(){
//		int total = 0;
//		switch(this.type){
//			case ExploreConstant.FREE:
//				total = ExploreConstant.FREE_DRAW_NUMBER;
//				break;
//		}
//		return total;
//	}
//
//	public int getAllTimes() {
//		return allTimes;
//	}
//
//	public void setAllTimes(int allTimes) {
//		this.allTimes = allTimes;
//	}
//
//	/**
//	 * 获取已经免费领取的次数
//	 * @Title: getExactTimes 
//	 * @Description:
//	 * @param curTime
//	 * @return
//	 */
//	public int getExactTimes(Date curTime){
//		if (this.lastGetTime == null){
//			this.times = 0;
//			return 0;
//		}
//		switch(this.type){
//			case ExploreConstant.FREE:
//				if (!DateUtils.isSameSideOfFour(this.lastGetTime, curTime)){
//					this.times = 0;
//				}
//				break;
//		}
//		return this.times;
//	}
//	
//	/**
//	 * 获取免费剩余领取次数
//	 * @Title: remainingTimes 
//	 * @Description:
//	 * @return
//	 */
//	public int getRemainingTimes(){
//		int remain = this.getTotalTimes() - this.getExactTimes(new Date());
//		return remain < 0 ? 0: remain;
//	}
//
//	/**
//	 * 获取矿开发的打折数
//	 * @Title: getDiscount 
//	 * @Description:
//	 * @return
//	 */
//	public int getDiscount(){
//		int discount = 10;
//		switch(this.type){
//			case ExploreConstant.PAY:
//				return ExploreConstant.PAY_DISCOUNT;
//			case ExploreConstant.GUILD_2:
//				return ExploreConstant.GUILD_2_DISCOUNT;
//		}
//		return discount;
//	}
//
//	/**
//	 * 获取距离下一次免费领取还有多少时间
//	 * @Title: getReminingTime 
//	 * @Description:
//	 * @returnn 
//	 */
//	public int getReminingTime(){
//		if (this.getLastGetTime() == null)
//			return 0;
//		int timeInterval = 0;
//		switch(this.type){
//			case ExploreConstant.FREE:
//				timeInterval = ExploreConstant.FREE_TIME_INTERVAL;
//				break;
//			case ExploreConstant.SIGLE:
//				timeInterval = ExploreConstant.SIGLE_TIME_INTERVAL;
//				break;
//		}
//		long lastTime = this.getLastGetTime().getTime() / 1000;
//		long time = lastTime + timeInterval - System.currentTimeMillis() / 1000;
//		return time < 0? 0: (int)time;
//	}
//
//	/**
//	 * @Title: haveFreeChance 
//	 * @Description:
//	 * @return 127 表示还有免费的探宝次数
//	 */
//	public byte haveFreeChance(){
//		if (this.type == ExploreConstant.PAY ||
//				this.type == ExploreConstant.GUILD_1 ||
//				this.type == ExploreConstant.GUILD_2)
//		{
//			return ExploreConstant.HAVE_NOT_FREE_NUMBER;
//		}
//		if(this.type == ExploreConstant.FREE){
//			if (this.getTotalTimes() - this.getExactTimes(new Date()) <= 0){
//				return ExploreConstant.HAVE_NOT_FREE_NUMBER;
//			}
//		}
//		if (this.lastGetTime == null) return 127;
//		int timeInterval = 0;
//		switch(this.type){
//			case ExploreConstant.FREE:
//				timeInterval = ExploreConstant.FREE_TIME_INTERVAL;
//				break;
//			case ExploreConstant.SIGLE:
//				timeInterval = ExploreConstant.SIGLE_TIME_INTERVAL;
//				break;
//		}
//		if (this.lastGetTime.getTime() + timeInterval * 1000> System.currentTimeMillis()){
//			return ExploreConstant.TIME_IS_NOT_COMING;
//		}
//		return 127;
//	}
}
