package com.qx.youxia;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;

@Entity
@Table(name = "BuZhenYouXia")
public class BuZhenYouXia implements DBHash {
	@Id
	public long junzhuId;

	/** 洗劫权贵 */
	@Column(columnDefinition = "INT default -1")
	public int jinBiZuheId;
	
	/** 讨伐山贼 */
	@Column(columnDefinition = "INT default -1")
	public int caiLiaoZuheId;
	
	/** 剿灭叛军 */
	@Column(columnDefinition = "INT default -1")
	public int jingQiZuheId;
	
	/** 完璧归赵 */
	@Column(columnDefinition = "INT default -1")
	public int type4;		//完璧归赵
	
	/** 横扫六合 */
	@Column(columnDefinition = "INT default -1")
	public int type5;		//横扫六合

	@Override
	public long hash() {
		return junzhuId;
	}
}
