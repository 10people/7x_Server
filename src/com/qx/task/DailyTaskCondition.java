package com.qx.task;

public class DailyTaskCondition {
	public long junzhuId;
	public int renWuId;
	/**增加的进度数*/
	public int jinduAdd;

	public DailyTaskCondition(long junzhuId, int renWuId, int jinduAdd) {
		this.junzhuId = junzhuId;
		this.renWuId = renWuId;
		this.jinduAdd = jinduAdd;
	}
}
