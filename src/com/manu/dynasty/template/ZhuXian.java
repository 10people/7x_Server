package com.manu.dynasty.template;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.mapping.Array;

public class ZhuXian {
	private int id;
	private String title;		//string 	任务主UI，展示该条目时，做条目名
	private String brief;		//string 	供主城某固定区域展示用。未完成时："找到汉献帝"，完成后显示："找到汉献帝(完成)"
	private String desc;		//string	任务主UI，展示该条目时，作为对任务的详细描述
	private byte triggerType;	//byte		范围：0-255。类型0：需要前置任务A+B+C；类型1：需要君主等级N；类型2：攻打到制定章节-关卡
	private String triggerCond;	//string	Type为0时，将前置任务ID用+号连接，如1或1+2或1+2+3；Type为1时，则填写需要等级，如18；Type为2时，填写章节号+关卡，如3+2表示第3章第2
	private short doneType;		//byte		范围0-255。类型0：君主达到等级N；类型1：通过制定章节-关卡；类型2：收集到N个指定物品；类型3：消灭N个制定敌人。
	private String doneCond;	//string	Type为0事，填写需要等级，如18；Type为1，填写章节号+关卡，如3+2；Type为2，物品类型.物品id=物品数量，如1.3124=10+2.9767=5；Type为3，同2。
	private String yindaoId;		//byte		每个ID对应一个引导逻辑，由程序根据备注单独制作
	private String award;		//string	奖励内容，格式为  道具类型(0道具;2装备;7武将)：道具ID：数目#道具类型(0道具;2装备;7武将)：道具ID：数目（#代表分隔符）
	private String awardDesc;	//string	奖励描述
	private String icon;		//string	图标	

//	public int nextId;//服务器做主线任务用的，配置文件没用。
	public int orderIdx;//服务器用来检查主线任务的顺序，策划不需要配置这个值
	
	public int type; //主线 还是支线
	public int rank; //支线的排序
	public String nextGroup; //关联的下一组任务，格式举例：“10001:100002”
	public int finishType;

	public List<Integer> getBranchRenWuIds(){
		List<Integer> list = new ArrayList<Integer>();
		if(this.nextGroup == null || "".equals(this.nextGroup)){
			return null;
		}
		String[] ids = this.nextGroup.split(":");
		int len = ids.length;
		int id;
		for(int i = 0; i < len; i++){
			if(ids[i] == null){
				continue;
			}
			id = Integer.parseInt(ids[i]);
			list.add(id);
		}
		return list;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBrief() {
		return brief;
	}
	public void setBrief(String brief) {
		this.brief = brief;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public byte getTriggerType() {
		return triggerType;
	}
	public void setTriggerType(byte triggerType) {
		this.triggerType = triggerType;
	}
	public String getTriggerCond() {
		return triggerCond;
	}
	public void setTriggerCond(String triggerCond) {
		this.triggerCond = triggerCond;
	}
	public short getDoneType() {
		return doneType;
	}
	public void setDoneType(short doneType) {
		this.doneType = doneType;
	}
	public String getDoneCond() {
		return doneCond;
	}
	public void setDoneCond(String doneCond) {
		this.doneCond = doneCond;
	}
	public String getYindaoId() {
		return yindaoId;
	}
	public void setYindaoId(String yindaoId) {
		this.yindaoId = yindaoId;
	}
	public String getAward() {
		return award;
	}
	public void setAward(String award) {
		this.award = award;
	}
	public String getAwardDesc() {
		return awardDesc;
	}
	public void setAwardDesc(String awardDesc) {
		this.awardDesc = awardDesc;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
}
