package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;

/**
 * @Description 首日、七日活动以及成就大活动数据对用的数据库实体
 *
 */
@Entity
@Table(name = "XianShiBean")
public class XianShiBean implements MCSupport,DBHash{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	public static final long serialVersionUID = 123122L;
	@Id
	public long id;//jzId*100+bigId;
	
	public long junZhuId;
	/*活动分类id*/
	public int bigId;
	/*参与活动开始时间*/
	public Date startDate;
	/*参与活动完成时间*/
	public Date finishDate;
	/*活动进度id*/
	public int huoDongId;
	@Override
	public long getIdentifier() {
		return id;
	}
	@Override
	public long hash() {
		return id;
	}
}