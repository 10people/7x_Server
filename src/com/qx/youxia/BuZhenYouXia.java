package com.qx.youxia;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BuZhenYouXia")
public class BuZhenYouXia {
	@Id
	public long junzhuId;

	@Column(columnDefinition = "INT default -1")
	public int jinBiZuheId;
	
	@Column(columnDefinition = "INT default -1")
	public int caiLiaoZuheId;
	
	@Column(columnDefinition = "INT default -1")
	public int jingQiZuheId;
}
