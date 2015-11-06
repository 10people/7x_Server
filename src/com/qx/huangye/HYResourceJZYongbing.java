package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.util.TableIDCreator;

/**
 * 荒野君主雇佣兵
 * @author lizhaowen
 *
 */
@Entity
public class HYResourceJZYongbing {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	
	public int lianmengId;
	
	public long resourceId;//2015年4月17日16:57:30改为long
	
	public long junzhuId;
	
	public int bing1;
	public int bing2;
	public int bing3;
	public int bing4;
	
	public HYResourceJZYongbing() {
		super();
	}
	//2015年4月17日16:57:30int改为long resourceId
	public HYResourceJZYongbing(int lianmengId, long resourceId, long junzhuId,
			int bing1, int bing2, int bing3, int bing4) {
		super();
		//改自增主键为指定
	    this.id=( TableIDCreator.getTableID(HYResourceJZYongbing.class, 1L));
		this.lianmengId = lianmengId;
		this.resourceId = resourceId;
		this.junzhuId = junzhuId;
		this.bing1 = bing1;
		this.bing2 = bing2;
		this.bing3 = bing3;
		this.bing4 = bing4;
	}
	
}
