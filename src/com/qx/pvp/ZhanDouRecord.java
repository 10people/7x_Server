package com.qx.pvp;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 对于战斗记录，记录的是全服战斗，以zhandouId为主键，每一条记录，并不区分junzhu  和  enemy的区别，
 * 只不过junzhuId指的是本次战斗发起挑战的一方。
 *
 * This class is used for ...   
 * @author wangZhuan
 * @version   
 *       9.0, 2015年1月19日 下午7:37:16
 */
@Entity
@Table(name = "ZhanDouRecord")
public class ZhanDouRecord{
	@Id
	public int zhandouId;
	public long junzhuId; // 挑战者（攻击方）
	public long enemyId; //被挑战者（防守方）
	/*战斗发生的时间(结束时间)*/
	public Date time;
	// 1 攻击胜利 2攻击失败
	public int result1;
	/*君主名次变化值*/
	public int junRankChangeV;
	/*对手名次变化值*/
	public int enemyRankChangeV;
	public int getWeiWang;
	// 只有挑战者失败的时候，才会损失联盟建设值，但不会得到(大于0的值)
	public int lostBuild;

	public ZhanDouRecord(){}
	public ZhanDouRecord(int zhandouId, long junzhuId, long enemyId, Date time,
			int result1, int junRankChangeV, int enemyRankChangeV, int getWeiWang, int lostBuild) {
		
		super();
		this.zhandouId = zhandouId;
		this.junzhuId = junzhuId;
		this.enemyId = enemyId;
		this.time = time;
		this.result1 = result1;
		this.junRankChangeV = junRankChangeV;
		this.enemyRankChangeV = enemyRankChangeV;
		this.getWeiWang = getWeiWang;
		this.lostBuild = lostBuild;
	}

}