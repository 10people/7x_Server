package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.util.TableIDCreator;

/**
 * 荒野藏宝点
 * @author lizhaowen
 *
 */
@Entity
public class HYResourceNpc {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	
	public int lianmengId;
	
	public long resourceId;//2015年4月17日16:57:30改为long
	
	public int bossId;
	
	public int battleSuccess;//是否挑战成功，0-没有成功，1-成功
	
	public int bing1;
	public int bing2;
	public int bing3;
	public int bing4;
	public long battleJZId;//正在挑战的君主id。<=0表示没有人在挑战
	
	
	public HYResourceNpc() {
		super();
	}

	//2015年4月17日16:57:30int改为long  resourceId
	public HYResourceNpc(int lianmengId, long resourceId, int bossId,
			int battleSuccess, int bing1, int bing2, int bing3, int bing4) {
		super();
		//改自增主键为指定
		this.id=(TableIDCreator.getTableID(HYResourceNpc.class, 1L));

		this.lianmengId = lianmengId;
		this.resourceId = resourceId;
		this.bossId = bossId;
		this.battleSuccess = battleSuccess;
		this.bing1 = bing1;
		this.bing2 = bing2;
		this.bing3 = bing3;
		this.bing4 = bing4;
	}
	
	

}
