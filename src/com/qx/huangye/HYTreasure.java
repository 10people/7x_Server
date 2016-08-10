package com.qx.huangye;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;
import com.qx.util.TableIDCreator;

/**
 * 荒野藏宝点
 * @author lizhaowen
 *
 */
@Entity
public class HYTreasure implements DBHash {

	public static final long serialVersionUID = 1L;

	@Id
	public long id;
	
	public int lianMengId;
	
	public int guanQiaId; // 配置文件id
	
//	@Column(columnDefinition = "INT default 0" )
	public long battleJunzhuId;		// 当前正在挑战的君主id, 0表示没有人在挑战
	public Date battleBeginTime; 	// battleJunZhuid 开始挑战的时间,null表示挑战结束

	public Date openTime = null;	// 藏宝点本次开启时间， null表示关闭状态
	
	public int progress;			// 剩余血量比

	public int passTimes = 0; 		// 通关次数

	public HYTreasure() {
		super();
	}

	public HYTreasure(int lianMengId, int guanQiaId) {
		super();
	    this.id=(TableIDCreator.getTableID(HYTreasure.class, 1L)); // ok
		this.lianMengId = lianMengId;
		this.guanQiaId = guanQiaId;
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
	public long hash() {
		return lianMengId * 1000;
	}

	
}
