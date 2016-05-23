package com.qx.activity;

import com.manu.dynasty.template.Fengcefuli;


/**
 * @Description 月卡福利、体力福利、封测红包福利用到的常量
 *
 */
public class FuliConstant{
	public static final int yuekafuli=1392;//月卡福利
	public static String yuekafuliAward="120";//"0:900002:120";//月卡福利
	public static final int tilifuli=1391;//体力福利
	public static  String tilifuliAward1="50";//"0:900003:50";//体力福利
	public static  String tilifuliAward2="50";//"0:900003:50";//体力福利
	public static  String tilifuliAward3="50";//"0:900003:50";//体力福利
	public static final int fengcehongbao=1390;//封测红包福利
	public static  String fengcehongbaoAward1="5000";//"0:900002:5000";//封测红包福利
	public static  String fengcehongbaoAward2="5000";//"0:900002:5000";//封测红包福利
	public static  int show_tili_clock_12 = 12;
	public static  int show_tili_clock_14 = 14;
	public static  int show_tili_clock_18 = 18;
	public static  int show_tili_clock_20 = 20;
	public static  int show_tili_clock_21 = 21;
	public static  int show_tili_clock_24 = 24;
	//坐等被坑 待改可能100000000000000000000%
	public static  int show_hongbao_clock_1 = 9;
	public static  int show_hongbao_clock_2_hour = 17;
	public static  int show_hongbao_clock_2_min = 30;
	static{
		yuekafuliAward=	Fengcefuli.Yuekaaward.split(":")[2];  
		
		show_tili_clock_12=Integer.valueOf(Fengcefuli.TiliStartTime1.split(":")[0]);	  
		show_tili_clock_14=Integer.valueOf(Fengcefuli.TiliEndTime1.split(":")[0]);    
		show_tili_clock_18=Integer.valueOf(Fengcefuli.TiliStartTime2.split(":")[0]);	  
		show_tili_clock_20=Integer.valueOf(Fengcefuli.TiliEndTime2.split(":")[0]);    
		show_tili_clock_21=Integer.valueOf(Fengcefuli.TiliStartTime3.split(":")[0]);	  
		show_tili_clock_24=Integer.valueOf(Fengcefuli.TiliEndTime3.split(":")[0]);    
		tilifuliAward1=Fengcefuli.Tiliaward1.split(":")[2];       
		tilifuliAward2=Fengcefuli.Tiliaward2.split(":")[2];       
		tilifuliAward3=Fengcefuli.Tiliaward3.split(":")[2];   
		
		fengcehongbaoAward1=Fengcefuli.YBaward1.split(":")[2];       
		fengcehongbaoAward2=Fengcefuli.YBaward2.split(":")[2];       
		show_hongbao_clock_1=Integer.valueOf(Fengcefuli.YBStartTime1.split(":")[0]);	  
		show_hongbao_clock_2_hour=Integer.valueOf(Fengcefuli.YBStartTime2.split(":")[0]);	  
		show_hongbao_clock_2_hour=Integer.valueOf(Fengcefuli.YBStartTime2.split(":")[1]);  

	}
}