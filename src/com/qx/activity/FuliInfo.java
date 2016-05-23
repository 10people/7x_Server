package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *  @Description 福利的数据库实体类
 *
 */
@Entity
@Table(name = "FuliInfo")

public class FuliInfo  {
	@Id
	public long jzId;//用作君主id
	public Date getTiLiTime1;// 上次获取体力时间1
	public Date getTiLiTime2;// 上次获取体力时间2
	public Date getTiLiTime3;// 上次获取体力时间3
	public Date getFengCeHongBaoTime1;// 上次获取封测红包时间1
	public Date getFengCeHongBaoTime2;// 上次获取封测红包时间2
	public Date getYuKaFuLiTime;// 上次获取月卡时间
	public Date getZhongShenKaTime;// 上次获取月卡时间
}
