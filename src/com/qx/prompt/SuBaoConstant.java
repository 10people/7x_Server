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
	//交互相关
	//打招呼 
	public static final int greet = 401;
	/**玩家打完第一次百战千军**/
	public static final int askgh4baizhan = 402;
	
	/**第一次打完百战被第一个玩家恭贺*/
	public static final int firstgh2baizhan = 403;
	/**第一次打完百战3分钟后结算*/
	public static final int settle4baizhan = 404;
	/**玩家开启联盟功能，向盟主&副盟主请求恭贺某人**/
	public static final int askgh4lm2leader = 301;
	/**玩家开启联盟功能，向盟主&副盟主之外的人请求恭贺某人*/
	public static final int askgh4lm2other = 302; 
	//邀请入盟
	public static final int invite = 303;
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
	//场景互动 TODO 配置！！
	public static final int gonghe = 8;//恭贺
	public static final int joinLM = 9;//同意加入联盟
	public static final int refuseLM = 10;//拒绝加入联盟
	public static final int hello = 11;//你好
	public static final int gun = 12;//走开
	public static final int invite2Lm = 13;//

//	/**第一次打完百战3分钟后结算*/
//	public static final int settle4lm = 404;
	//目前没有配置
	//8小时过时的通知参数
	public static int clearShortDistance = 8;
	//48小时过时的通知参数
	public static int clearLongDistance = 48;
	//3分钟过时的通知参数
	public static int clearSecondsDistance = 3*60;

}
