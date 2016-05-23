package com.qx.yuanbao;

import java.util.Date;

import com.manu.dynasty.util.DateUtils;

public class TXQuery {
	public Date dt=new Date();
	public String params;
	public long optime;
	public long jzId;
	public int pre_save_amt;
	public int new_save_amt;
	public int balance;
	
	//扣款用/赠送，其他情况请勿使用
	public int diff;
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("dt:");sb.append(DateUtils.datetime2Text(dt));
		sb.append(",params:");sb.append(params);
		sb.append(",jzId:");sb.append(jzId);
		sb.append(",pre_save_amt:");sb.append(pre_save_amt);
		sb.append(",new_save_amt:");sb.append(new_save_amt);
		sb.append(",balance:");sb.append(balance);
		return sb.toString();
	}
}
