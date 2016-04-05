package com.qx.explore.treasure;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 拾取宝箱记录
 * @author 康
 *
 */
@Entity
public class BXRecord {
	@Id
	public long jzId;
	public int bxCnt;
	public int yuanBao;
	public Date resetTime;
}
