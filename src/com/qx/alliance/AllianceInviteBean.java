package com.qx.alliance;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AllianceInviteBean {
	@Id
	public long id;
	public long junzhuId;
	public Date date;
	public int allianceId;
}
