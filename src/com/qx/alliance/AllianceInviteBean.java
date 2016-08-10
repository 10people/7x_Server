package com.qx.alliance;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.persistent.DBHash;

@Entity
public class AllianceInviteBean implements DBHash {
	@Id
	public long id;
	public long junzhuId;
	public Date date;
	public int allianceId;

	@Override
	public long hash() {
		return junzhuId;
	}
}
