package com.qx.prompt;

/**
 * @Description 速报类型常量
 *
 */
public class SuBaoConstant {
	/**玩家的镖马被攻打，系统自动发消息(给自己)         **/
	public static final int mcbd_toSelf=101 ;
	
	/**玩家的镖马被攻打，系统自动发消息 (给盟友)       **/
	public static final int mcbd_toOther=102 ;
	
	/**马车出发，系统自动发消息(给自己)           **/
	public static  final int mccf_toSelf=103 ;//此id废弃 2015年12月24日策划拆成2个事件 2015年12月24日15:30:08恢复马车出发发给自己
	
	/**玩家的镖马出发，系统自动发消息(给未加入协助的盟友)           **/
	public static  final int mccf_toOther=104 ;
	
	/**玩家主动请求协助                         **/
	public static  final int zdqz=105 ;
	/**盟友祝福玩家的镖车（在消息中点击祝福）   **/
	public static  final int myzf=106 ;
	/**运镖失败求安慰（在消息中点击安慰）   **/
	public static final int qiuaw4yb=107 ;
	/**运镖失败，被盟友安慰                    **/
	public static final int sbaw4yb=108;
	/**运镖失败，获得运镖奖励                   **/
	public static  final int ybsb=109 ;
	/**运镖成功，获得运镖奖励                   **/
	public static final int ybcg=110 ;
	/**协助成功，获得协助运镖奖励               **/       	
	public static final int xzcg=111 ;
	/**协助失败，告知协助玩家                   **/       	
	public static final int xzsb=112;
	/**击杀仇人                                 **/       	
	public static final int jscr=113;         
	/**击杀仇人马车                                 **/       	
	public static final int djcr_toSelf=114;         
	/**击杀非仇人 马车                                **/       	
	public static final int djfcr_toSelf=115;         
	/**加入协助成功给自己                               **/       	
	public static final int jionxz_toSelf=116;         
	/**领取押镖福利次数                             **/       	
	public static final int fuli_toSelf=117;         
	
	public static final int lveDuo_fail_event = 201;
	public static final int been_lveDuo_event = 202;
	public static final int lveDuo_comfort_event = 203;
	
//	0：无按钮
//	1：忽略
//	2：押镖前往
//	3：祝福
//	4：安慰
//	5：领取
//	6：知道了
//	7：掠夺前往
	public static final int ignore = 1;
	public static final int go = 2;
	public static final int bless = 3;
	public static final int comfort = 4;
	public static final int getAward= 5;
	public static final int iKnow= 6;
	public static final int lveDuo_go = 7;
}
