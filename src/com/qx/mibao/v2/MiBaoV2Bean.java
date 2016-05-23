package com.qx.mibao.v2;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes={@Index(name="ownerId",columnList="ownerId")})
public class MiBaoV2Bean {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long dbId;
	public long ownerId;
	public int  miBaoId;
	public int suiPianNum;//拥有的碎片数量
	public boolean active;
	public boolean main;
}
