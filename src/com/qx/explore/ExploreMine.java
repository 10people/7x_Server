package com.qx.explore;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;



import com.manu.dynasty.util.DateUtils;
import com.qx.persistent.MCSupport;


@Entity
@Table(name="explore_mine")
public class ExploreMine implements MCSupport{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 1L;
	@Id
	// id = junzhuId * 100 + type
	private long id;
	// 探宝类型：0-免费，1-单抽，10-10连抽
	// 矿区类型：0-矿洞，1-矿井，10-矿脉,11-联盟单抽， 12-联盟10抽
	private int type;
	// 当日已经免费领取次数
	private int times;
	// 上次免费领取时间
	private Date lastGetTime;
	// 历史总共抽取次数
	private int allTimes;
	// 免费单抽或者联盟单抽是否已经领取过好的奖励了: 
	// add 包括10连抽是否已经领过好的奖励
	@Column(columnDefinition = "boolean default false")
	public boolean hasGoodAwar;

	public ExploreMine() {
		super();
	}

	/**
	 * 新建初始化
	 * @Title: ExploreMine 
	 * @Description:
	 * @param junZhuId
	 * @param type
	 */
	public ExploreMine(long id, int type){
		this.id =  id;
		this.type  = type;
		this.times = 0;
		// 免费单抽 和付费单抽的首次抽奖不一样
		if (this.type == ExploreConstant.SIGLE ||
				this.type == ExploreConstant.FREE)
		{
			allTimes = -1;
		}
		else this.allTimes = 0;
		this.lastGetTime = null;	
	}

	/**
	 *  当日共拥有的免费次数
	 * @Title: setExactTimes 
	 * @Description:
	 */
	public int getTotalTimes(){
		int total = 0;
		switch(this.type){
			case ExploreConstant.FREE:
				total = ExploreConstant.FREE_DRAW_NUMBER;
				break;
		}
		return total;
	}

	public int getAllTimes() {
		return allTimes;
	}

	public void setAllTimes(int allTimes) {
		this.allTimes = allTimes;
	}

	/**
	 * 获取已经免费领取的次数
	 * @Title: getExactTimes 
	 * @Description:
	 * @param curTime
	 * @return
	 */
	public int getExactTimes(Date curTime){
		if (this.lastGetTime == null){
			this.times = 0;
			return 0;
		}
		switch(this.type){
			case ExploreConstant.FREE:
				if (!DateUtils.isSameSideOfFour(this.lastGetTime, curTime)){
					this.times = 0;
				}
				break;
		}
		return this.times;
	}
	
	/**
	 * 获取免费剩余领取次数
	 * @Title: remainingTimes 
	 * @Description:
	 * @return
	 */
	public int getRemainingTimes(){
		int remain = this.getTotalTimes() - this.getExactTimes(new Date());
		return remain < 0 ? 0: remain;
	}

	/**
	 * 获取矿开发的打折数
	 * @Title: getDiscount 
	 * @Description:
	 * @return
	 */
	public int getDiscount(){
		int discount = 10;
		switch(this.type){
			case ExploreConstant.PAY:
				return ExploreConstant.PAY_DISCOUNT;
			case ExploreConstant.GUILD_2:
				return ExploreConstant.GUILD_2_DISCOUNT;
		}
		return discount;
	}

	/**
	 * 获取距离下一次免费领取还有多少时间
	 * @Title: getReminingTime 
	 * @Description:
	 * @returnn 
	 */
	public int getReminingTime(){
		if (this.getLastGetTime() == null)
			return 0;
		int timeInterval = 0;
		switch(this.type){
			case ExploreConstant.FREE:
				timeInterval = ExploreConstant.FREE_TIME_INTERVAL;
				break;
			case ExploreConstant.SIGLE:
				timeInterval = ExploreConstant.SIGLE_TIME_INTERVAL;
				break;
		}
		long lastTime = this.getLastGetTime().getTime() / 1000;
		long time = lastTime + timeInterval - System.currentTimeMillis() / 1000;
		return time < 0? 0: (int)time;
	}
	public int getTimes() {
		return times;
	}
	public void setTimes(int times) {
		this.times = times;
	}
	public Date getLastGetTime() {
		return lastGetTime;
	}
	public void setLastGetTime(Date lastGetTime) {
		this.lastGetTime = lastGetTime;
	}
	public long getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @Title: haveFreeChance 
	 * @Description:
	 * @return 127 表示还有免费的探宝次数
	 */
	public byte haveFreeChance(){
		if (this.type == ExploreConstant.PAY ||
				this.type == ExploreConstant.GUILD_1 ||
				this.type == ExploreConstant.GUILD_2)
		{
			return ExploreConstant.HAVE_NOT_FREE_NUMBER;
		}
		if(this.type == ExploreConstant.FREE){
			if (this.getTotalTimes() - this.getExactTimes(new Date()) <= 0){
				return ExploreConstant.HAVE_NOT_FREE_NUMBER;
			}
		}
		if (this.lastGetTime == null) return 127;
		int timeInterval = 0;
		switch(this.type){
			case ExploreConstant.FREE:
				timeInterval = ExploreConstant.FREE_TIME_INTERVAL;
				break;
			case ExploreConstant.SIGLE:
				timeInterval = ExploreConstant.SIGLE_TIME_INTERVAL;
				break;
		}
		if (this.lastGetTime.getTime() + timeInterval * 1000> System.currentTimeMillis()){
			return ExploreConstant.TIME_IS_NOT_COMING;
		}
		return 127;
	}

	@Override
	public long getIdentifier() {
		return id;
	}
}
