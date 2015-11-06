package com.qx.ranking;

import qxmobile.protobuf.Ranking.JunZhuInfo;

/** 
 * @ClassName: RankAlliancePlayer 
 * @Description: 对联盟成员按照职位-》等级来排序
 * @author 何金成 
 * @date 2015年8月4日 下午6:04:24 
 *  
 */
public class RankAlliancePlayer implements Comparable<RankAlliancePlayer>  {
	public int job;
	public int level;
	public JunZhuInfo.Builder jzBuilder;
	
	public RankAlliancePlayer(JunZhuInfo.Builder jzBuilder) {
		this.jzBuilder = jzBuilder;
		this.job = jzBuilder.getJob();
		this.level = jzBuilder.getLevel();
	}

	/* (非 Javadoc) 
	 * <p>Title: compareTo</p> 
	 * <p>Description: 联盟成员排序</p> 
	 * @param player
	 * @return 
	 * @see java.lang.Comparable#compareTo(java.lang.Object) 
	 */
	@Override
	public int compareTo(RankAlliancePlayer player) {
		if(this.job>player.job){
			return 1;
		} else if(this.job==player.job){
			if(this.level>=player.level){
				return 1;
			} else{
				return -1;
			}
		} else{
			return -1;
		}
	}
}
