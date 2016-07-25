package com.qx.gm.message;

public class ConsumeRecords {
	public String funcname;// 功能名称
	public int price;// 功能单价
	public String money;// 消费元宝
	public String dttm;// 消费时间

	public String getFuncname() {
		return funcname;
	}

	public void setFuncname(String funcname) {
		this.funcname = funcname;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getDttm() {
		return dttm;
	}

	public void setDttm(String dttm) {
		this.dttm = dttm;
	}

}
