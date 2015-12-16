package com.qx.mibao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.manu.dynasty.template.MiBao;
import com.qx.junzhu.JunZhu;

@Entity
@Table(name="MiBao",indexes={@Index(name="ownerId",columnList="ownerId")})
public class MiBaoDB {
	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	private long dbId;//2015年4月17日16:57:30int改为long
	private long ownerId;//所有者id即君主id
	private int tempId;	//秘宝类型
	private int miBaoId;//配置文件mibao中id字段
	
	@Column(columnDefinition = "INT default 1")
	private int star;
	
	//  秘宝等级，根据它来判断秘宝是否激活，等级大于等于1，表示激活
	@Column(columnDefinition = "INT default 0")
	private int level;
	
	@Column(columnDefinition = "INT default 0")
	private int suiPianNum;//拥有的碎片数量

	/*   以下的秘宝属性都可以通过公式计算出来，所以不需要存入数据库       */
	@Transient
	private int gongJi;
	
	@Transient
	private int fangYu;
	
	@Transient
	private int shengMing;
	
	public boolean hasShengXing = false;
	
	/*
	 * 20151203  1.1 版本 秘宝没有锁定状态
	 */

//	/**秘宝是否解锁 true: 解锁状态，false： 未解锁状态
//	 * 只有当是true的时候，数据可信，否则需要进一步验证*/
//	@Column(columnDefinition = "boolean default false")
//	private boolean isClear;
	
//	public boolean isClear() {
//		return this.isClear;
//	}
//	public void setClear(MiBao miBaoCfg, JunZhu jz) {
//		if(miBaoCfg == null || jz == null) {
//			this.isClear = false;
//			return;
//		}
//		boolean lock = MibaoMgr.inst.isLock(miBaoCfg.unlockType, miBaoCfg.unlockValue, jz);
//		this.isClear = !lock;
//	}
//
//	public void setClear(boolean isClear){
//		this.isClear = isClear;
//	}
	//2015年4月17日16:57:30int改为long
	public long getDbId() {
		return dbId;
	}
	public void setDbId(long dbId) {
		this.dbId = dbId;
	}
	public int getMiBaoId() {
		return miBaoId;
	}
	public void setMiBaoId(int miBaoId) {
		this.miBaoId = miBaoId;
	}
	public long getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}
	public int getStar() {
		return star;
	}
	public void setStar(int star) {
		this.star = star;
	}
	public int getTempId() {
		return tempId;
	}
	public void setTempId(int tempId) {
		this.tempId = tempId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getSuiPianNum() {
		return suiPianNum;
	}
	public void setSuiPianNum(int suiPianNum) {
		this.suiPianNum = suiPianNum;
	}
	public int getGongJi() {
		return gongJi;
	}
	public void setGongJi(int gongJi) {
		this.gongJi = gongJi;
	}
	public int getFangYu() {
		return fangYu;
	}
	public void setFangYu(int fangYu) {
		this.fangYu = fangYu;
	}
	public int getShengMing() {
		return shengMing;
	}
	public void setShengMing(int shengMing) {
		this.shengMing = shengMing;
	}
}
