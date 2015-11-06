package com.qx.alliance;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes={@Index(name="lmId",columnList="lmId")})
public class BigHouse {
	public static final int ForUse = 20;
	public static final int ForFree = 10;
	public static final int ForClose = 404;
	@Id
	public long jzId;//jun zhu id
	public int lmId;//联盟id
	public int gongXian;//贡献
	public int hworth;//高级房屋价值
	public int location;
	public int state;//10 无主；20 自住；获得房屋是默认为自住。 404关闭；
	public boolean open;
	public long previousId;//前任主人id
	public int previousWorth;//当前主人竞拍高级房屋所花费的贡献
	public String firstOwner;
	public Date firstHoldTime;//第一次入住时间
	public Date previousHoldTime;//上次竞拍入住时间
	public Date preGainExpTime;//上次领取经验时间。
}
