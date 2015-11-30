package com.qx.explore;

import com.manu.dynasty.template.CanShu;

public class TanBaoData {
	
	
	public static final byte tongBi_type = 1;
	public static final byte yuanBao_type = 2;


	/**铜币当日免费次数**/
	public static int tongBi_all_free_times = CanShu.TONGBI_TANBAO_FREETIMES;
	public static byte yuanBao_all_free_times = 1;
	
	/**铜币免费单抽CD**/
	public static int tongBi_CD = CanShu.TONGBI_TANBAO_REFRESHTIME; 
	public static int yuanBao_CD = CanShu.YUANBAO_TANBAO_REFRESHTIME;

	//几种探宝奖励id，都表示 AwardTemp.xlsx 中的 awardId 字段
	public static int tongBi_first_free_awardId = 9100;
	public static int tongBi_free_normal_awardId = 9101;
	public static int tongBi_first_pay_awardId = 9104;
	public static int tongBi_pay_normal_awardId = 9102;
	public static int tongBi_pay_good_awardId = 9103;
	
	public static int yuanBao_first_free_awardId = 9000;
	public static int yuanBao_free_normal_awardId = 9001;
	public static int yuanBao_first_pay_awardId = 9004;
	public static int yuanBao_pay_normal_awardId = 9002;
	public static int yuanBao_pay_good_awardId = 9003;



	// 探宝的价钱  , 表示purchase.xlsx的id
	public static int tongBi_pay_sigle = 40011;
	public static int tongBi_pay_ten = 40012;

	public static int yuanBao_pay_sigle = 40001;
	public static int yuanBao_pay_ten = 40002;

}
