package com.qx.alliance;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 记录每个联盟的总体膜拜次数
 * @author 康建虎
 *
 */
@Entity
public class LmTuTeng {
	@Id
	public int lmId;
	public int times;
	public Date dTime;//今日首次膜拜时间，用于判断是否需要清零?
}
