package com.qx.util;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.ColumnDefault;

@Entity
public class SlowAct {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long dbId;//
	public int reqId;
	public int costMS;
	public Date dt;
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int serverId;
}
