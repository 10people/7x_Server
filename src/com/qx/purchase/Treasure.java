package com.qx.purchase;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.util.DateUtils;
import com.qx.util.TableIDCreator;

@Entity
@Table(name="shop_treasure")
public class Treasure {
	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;//2015年4月17日16:57:30int改为long
	private long junZhuId;
	// 宝箱类型：1-小袋宝箱，2-中袋宝箱，3-大袋宝箱
	private int type;
	// 已经领取次数
	private int times;
	// 上次领取时间
	private Date lastGetTime;
	public Treasure() {
		super();
	}
	public Treasure(long junZhuId, int type) {
		super();
		//改自增主键为指定
		//2015年4月17日16:57:30int改为long
		this.id=( TableIDCreator.getTableID(Treasure.class, 1L));
		this.junZhuId = junZhuId;
		this.type = type;
		this.times = 0;
		Calendar calendar = Calendar.getInstance();
		calendar.set(1991, 1, 1);
		this.lastGetTime = calendar.getTime();
	}
	//2015年4月17日16:57:30int改为long
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getJunZhuId() {
		return junZhuId;
	}
	public void setJunZhuId(long junZhuId) {
		this.junZhuId = junZhuId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * 取得是否可以领取
	 * @return
	 */
	public boolean isGet() {
		Date curTime = new Date();
		refreshTimes(curTime);
		// 判断次数，现在只有小袋每日有次数限制
		if(type == PurchaseMgr.TREASURE_CODE_SMALL && times >= PurchaseConstants.SMALL_TIMES_MAX){
			return false;
		}
		// 判断时间
		if((curTime.getTime() - getLastGetTime().getTime()) >= getInterval() * 1000){
			return true;
		}
		return false;
	}
	public int getTimes() {
		Date curTime = new Date();
		refreshTimes(curTime);
		return times;
	}
	public void setTimes(int times) {
		this.times = times;
	}
	public int getCountDown() {
		int chazhi = (int) (getInterval() - (System.currentTimeMillis() - lastGetTime.getTime()) / 1000);
		if(chazhi > 0){
			return chazhi;
		}
		return 0;
	}
	public Date getLastGetTime() {
		return lastGetTime;
	}
	public void setLastGetTime(Date lastGetTime) {
		this.lastGetTime = lastGetTime;
	}
	/**
	 * 获取不同宝箱两次领取的间隔时间，单位-秒
	 * @return
	 */
	private int getInterval(){
		int time = 0;
		switch (type) {
			case PurchaseMgr.TREASURE_CODE_SMALL:
				time = PurchaseConstants.SMALL_WAIT_TIME;
				break;
			case PurchaseMgr.TREASURE_CODE_MIDDLE:
				time = PurchaseConstants.MIDDLE_WAIT_TIME;
				break;
			default:
				break;
		}
		return time;
	}
	
	/**
	 * 进行每日次数刷新
	 */
	private void refreshTimes(Date curTime) {
		// change 20150901
		if(DateUtils.isTimeToReset(getLastGetTime(), CanShu.REFRESHTIME_PURCHASE)){
//		if(!DateUtils.isSameDay(curTime, getLastGetTime()) 
//				//&& DateUtils.getHourInCurrentDay(curTime) >= PurchaseConstants.TREASURE_REFRESH_TIME
//				){
			// 只有是小袋宝箱时才是每日刷新次数
			if(type == PurchaseMgr.TREASURE_CODE_SMALL){
				times = 0;
			}
		}
	}
}
