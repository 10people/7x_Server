package com.qx.fuwen;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class FuWenBean {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int id;
	public long junzhuId;
	public int lanWeiId;			// 对应fuwenopen表的id
	public int tab;
	public int itemId;
	public int exp;
}
