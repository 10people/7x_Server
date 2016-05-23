package com.manu.dynasty.template;

public class YunbiaoTemp {
	public static float saveArea_recoveryPro;             //安全区血量回复速率          
	public static int saveArea_recovery_interval;         //安全区血量回复时间间隔秒        
	public static int saveArea_people_max;              //单个安全区人数上限          
	public static int bloodVial_recoveryPro;            //血瓶血量回复速率            
	public static int bloodVialCD;                      //血瓶CD（秒）                
	public static int bloodVial_freeTimes;              //每日免费血瓶数量            
	public static int resurgenceTimes;                  //每日满血复活次数            
	public static int incomeAddPro;                     //时间段内运镖收益倍数       
	public static String incomeAdd_startTime1;          //运镖收益加成起始时间1       2016年1月27日去掉  2016年2月1日加上
	public static String incomeAdd_endTime1;            //运镖收益加成结束时间1    
	public static int	time1_switch;	//时段1开关
	public static String incomeAdd_startTime2;          //运镖收益加成起始时间2       
	public static String incomeAdd_endTime2;            //运镖收益加成结束时间2 
	public static int	time2_switch;
	public static String incomeAdd_startTime3;          //运镖收益加成起始时间3       
	public static String incomeAdd_endTime3;            //运镖收益加成结束时间3    
	public static int	time3_switch;
	public static int income_lossless_price;            //100%收益价格                
	public static int protectDuration;                  //镖马保护罩持续时间（秒）    
	public static int protectionCD;                     //镖马保护罩冷却时间（秒）    
	public static int speedUpDuration;                  //马鞭加速时间（秒）          
	public static int speedUpEffect;                    //马鞭加速效果（%）           
	public static int speedUpCD;                        //马鞭加速冷却时间（秒）      
	public static int speedUpPrice;                     //马鞭价格                    
	public static int cart_attribute_pro;               //镖马属性系数                
	public static int foeCart_incomeAdd_pro;            //劫获仇人镖马收益加成(%)     
	public static int rewarding_killFoe_max;            //（有奖励）杀死仇人上限      
	public static String cartAI_refresh_interval;          //机器人镖马刷新时间间隔（min)
	public static int cartAImax;                        //场景内AI镖马数量上限        
	public static int cartAILvlMin;                     //AI镖马最低等级              
	public static int cartAILvlMax;                     //AI镖马最高等级              
	public static int autoResurgenceTime;				//死亡后自动安全复活时间(秒）
	public static String	yunbiao_start_broadcast;//运镖开始广播
	public static String	yunbiao_end_broadcast;//运镖结束广播
	public static double yunbiao_comforted_award_k;//运镖被安慰奖励系数k
	public static int yunbiao_comforted_award_b;//运镖被安慰奖励参数b
	public static int cartAI_appear_interval;//机器人镖马出现时间间隔（s）
	public static double killFoeAward_k;//杀死单个仇人奖励系数k
	public static double killFoeAward_b;//杀死单个仇人奖励系数b
	public static int cartTime;//马车正常到达时间（秒）
	public static int yunbiaoScene_modelNum_max;//运镖场景模型数量上限
	public static int yunbiao_start_broadcast_CD;//"运镖开始广播循环CD（秒）
	public static int robincome_LvMax;//劫镖收益无损等级差上限
//	public static int cartTimesAdd1;// desc="时段1可领运镖次数" value="3" />2016年1月27日 去掉
	public static int cartTimesAdd2;// desc="时段2可领运镖次数" value="3" />
	public static float cartLifebarNum;//君主马车生命倍数
	public static float damage_amend_X;
	public static float damage_amend_Y;
	public static int assistant_application_CD;
	
	/*押镖相关参数--结束*/

	public String key;
	public String value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
