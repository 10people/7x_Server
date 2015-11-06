package com.qx.huangye;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.persistent.MCSupport;
import com.qx.util.TableIDCreator;

/**
 * 荒野藏宝点
 * @author lizhaowen
 *
 */
@Entity
public class HYTreasure implements MCSupport{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public long id;//2015年4月17日16:57:30int改为long
	
	public int lianMengId;
	
	public int idOfFile; // 配置文件id
	
//	@Column(columnDefinition = "INT default 0" )
	public long battleJunzhuId;//当前正在挑战的君主id, 0表示没有人在挑战
	public Date battleBeginTime; // battleJunZhuid 开始挑战的时间,null表示挑战结束

	// 1.0版本 确定藏宝点为永久开启
	public int maxTime = -1;//最大开启持续时间, -1 默认永久开启不关闭
	
	public Date openTime = null;//藏宝点本次开启时间， null表示关闭状态
	
	public int progress;//剩余血量比
	
	// 1.0版本不计累计挑战时间
//	@Column(columnDefinition = "INT default 0" )
//	public int costTime;//本次开启时间内，累计挑战的时间

	public int passTimes = 0; //通关次数

	public HYTreasure() {
		super();
	}

	public HYTreasure(int lianMengId, int idOfFile) {
		super();
		//改自增主键为指定
		//2015年4月17日16:57:30int改为long
	    this.id=(TableIDCreator.getTableID(HYTreasure.class, 1L)); // ok
		this.lianMengId = lianMengId;
		this.idOfFile = idOfFile;
		this.openTime = new Date();
		this.progress = 100;
	}
	
//	public int getRemainOpenTime() {
		/*
		 * 新荒野功能暂时不会用这些代码（这个方法） 20150902
		 */
//		if(openTime == null) {
//			return -1;
//		}
//		long differTime = System.currentTimeMillis() - openTime.getTime();
//		differTime = maxTime * 1000 - differTime;
//		if(differTime <= 0 ) {
//			return 0;
//		} else {
//			return (int) differTime/1000;
//		}
//		return -1;
//	}
	
	public boolean isOpen() {
		if(openTime == null) {
			return false;
		}
		return true;
	}

@Override
public long getIdentifier() {
	return id;
}
	
}
