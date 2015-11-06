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
	private int id;
	private long jzId;
	private long mailId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getJzId() {
		return jzId;
	}

	public void setJzId(long jzId) {
		this.jzId = jzId;
	}

	public long getMailId() {
		return mailId;
	}

	public void setMailId(long mailId) {
		this.mailId = mailId;
	}

}
