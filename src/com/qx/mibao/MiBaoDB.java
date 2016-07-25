package com.qx.mibao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.manu.dynasty.template.MiBao;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.DBHash;

@Entity
@Table(name="MiBao",indexes={@Index(name="ownerId",columnList="ownerId")})
public class MiBaoDB implements DBHash {
	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	public long dbId;//2015年4月17日16:57:30int改为long
	public long ownerId;//所有者id即君主id
	public int tempId;	//秘宝类型
	public int miBaoId;//配置文件mibao中id字段
	
	@Column(columnDefinition = "INT default 1")
	public int star;
	
	//  秘宝等级，根据它来判断秘宝是否激活，等级大于等于1，表示激活
	@Column(columnDefinition = "INT default 0")
	public int level;
	
	@Column(columnDefinition = "INT default 0")
	public int suiPianNum;//拥有的碎片数量

	/*   以下的秘宝属性都可以通过公式计算出来，所以不需要存入数据库       */
	@Transient
	public int gongJi;
	
	@Transient
	public int fangYu;
	
	@Transient
	public int shengMing;
	
	public boolean hasShengXing = false;

	@Override
	public long hash() {
		return ownerId;
	}
	
	/*
	 * 20151203  1.1 版本 秘宝没有锁定状态
	 */

//	/**秘宝是否解锁 true: 解锁状态，false： 未解锁状态
//	 * 只有当是true的时候，数据可信，否则需要进一步验证*/
//	@Column(columnDefinition = "boolean default false")
//	public boolean isClear;
	
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
}
