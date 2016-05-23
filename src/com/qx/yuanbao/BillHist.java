package com.qx.yuanbao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class BillHist {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;
	public long jzId;
	public Date dt;
	//我方数据，客户端充值后，通知服务器去查余额。
	public int curYB;
	public int wantYB;//
	public int buyItemId;//
	//共有数据
	public String type;//发起充值，请查余额，查到余额 （为了提高可读性，用字符串保存）
	//下面是腾讯接口数据
	public int balance;//：游戏币个数（包含了赠送游戏币）
	public int gen_balance;//: 赠送游戏币个数
	public int first_save;//: 是否满足首次充值，1：满足，0：不满足。
	public int save_amt;//: 累计充值金额的游戏币数量
}
