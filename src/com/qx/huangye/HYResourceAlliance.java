package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.qx.util.TableIDCreator;

/**
 * 资源点联盟匹配表
 * @author lizhaowen
 *
 */
@Entity
public class HYResourceAlliance {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	
	public int idOfFile;
	
	public long resourceId;//资源点表主键 2015年4月17日16:52:06改为long
	
	public int lianMengId;
	
	public HYResourceAlliance() {
		super();
	}
	//2015年4月17日16:57:30int resourceId改为long resourceId
	public HYResourceAlliance(long resourceId, int lianMengId, int idOfFile) {
		super();
		//改自增主键为指定
	    this.id=(TableIDCreator.getTableID(HYResourceAlliance.class, 1L));
		this.resourceId = resourceId;
		this.lianMengId = lianMengId;
		this.idOfFile = idOfFile;
	}
	
	

}
