package com.qx.gm.email;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gmemailsendrecord")
public class GMEmailSendRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int id;
	public long jzId;
	public long mailId;
}
