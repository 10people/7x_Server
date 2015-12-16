package com.manu.dynasty.template;

public class YunbiaoTemp {
	public static int saveArea_recoveryPro;             //安全区血量回复速率          
	public static int saveArea_people_max;              //单个安全区人数上限          
	public static int bloodVial_recoveryPro;            //血瓶血量回复速率            
	public static int bloodVialCD;                      //血瓶CD（秒）                
	public static int bloodVial_freeTimes;              //每日免费血瓶数量            
	public static int resurgenceTimes;                  //每日满血复活次数            
	public static int incomeAddPro;                     //时间段内运镖收益倍数       
	public static String incomeAdd_startTime1;          //运镖收益加成起始时间1       
	public static String incomeAdd_endTime1;            //运镖收益加成结束时间1       
	public static String incomeAdd_startTime2;          //运镖收益加成起始时间2       
	public static String incomeAdd_endTime2;            //运镖收益加成结束时间2       
	public static int income_lossless_price;            //100%收益价格                
	public static int protectDuration;                  //镖马保护罩持续时间（秒）    
	public static int protectionCD;                     //镖马保护罩冷却时间（秒）    
	public static int speedUpDuration;                  //马鞭加速时间（秒）          
	public static int speedUpEffect;                    //马鞭加速效果（%）           
	public static int speedUpCD;                        //马鞭加速冷却时间（秒）      
	public static int speedUpPrice;                     //马鞭价格                    
	public static int cart_attribute_pro;               //镖马属性系数                
	public static int foeCart_incomeAdd_pro;            //劫获仇人镖马收益加成(%)     
	public static String killFoeAward;                  //杀死单个仇人奖励            
	public static int rewarding_killFoe_max;            //（有奖励）杀死仇人上限      
	public static int cartAI_refresh_interval;          //机器人镖马刷新时间间隔（min)
	public static int cartAImax;                        //场景内AI镖马数量上限        
	public static int cartAILvlMin;                     //AI镖马最低等级              
	public static int cartAILvlMax;                     //AI镖马最高等级              
	public static int autoResurgenceTime;				//死亡后自动安全复活时间(秒）
	public static int resurgence_onSite_price;			//原地复活价格(元宝）
	public static String	yunbiao_start_broadcast;//运镖开始广播
	public static String	yunbiao_end_broadcast;//运镖结束广播
	
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
