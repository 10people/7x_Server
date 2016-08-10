package com.qx.prompt;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;

/**
 * 押镖军情
 * @author 
 *
 */
@Entity
@Table(name = "YaBiaoJunQing")
public class YaBiaoJunQing implements DBHash{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;
	public int lmId;//联盟Id
	public long ybjzId;//押镖君主id
	public long jbjzId;//劫镖君主id
	public int ybjzUid;//押镖君主Uid
	public int jbjzUid;//劫镖君主Uid
	public Date happenTime;
	@Override
	public long hash() {
		return ybjzId;
	}
}