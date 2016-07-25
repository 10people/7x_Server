package com.qx.gm.message;

import java.util.List;

import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;

public class ActivityCompensationReq {
	public int type;// 协议编号
	public int firm;// 表示厂商ID
	public int zone;// 大区号
	public int ctype;// 操作类型（1：添加； 2：编辑；）
	public int actid;// 活动记录ID
	public String title;// 活动标题
	public String content;// 活动描述
	public int minlv;// 最低等级
	public int maxlv;// 最好等级
	public List<Reward> reward;// 奖励内容（json串）
	public String md5;// 加密

	/**
	 * 注解：reward 字段内容: [{"item":"10001","num":1},{"item":"10002","num":2}……]
	 * item:道具id ； num: 道具数量
	 **/
	
	public boolean checkMd5(String reward4Md5) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(getType()).append(getFirm()).append(getZone())
				.append(getCtype()).append(getActid()).append(getTitle())
				.append(getContent()).append(getMinlv()).append(getMaxlv())
				.append(reward4Md5).append(CodeUtil.MD5_KEY);
		if (!MD5Util.checkMD5(sBuffer.toString(), getMd5())) {// MD5验证
			return false;
		}
		return true;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFirm() {
		return firm;
	}

	public void setFirm(int firm) {
		this.firm = firm;
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public int getCtype() {
		return ctype;
	}

	public void setCtype(int ctype) {
		this.ctype = ctype;
	}

	public int getActid() {
		return actid;
	}

	public void setActid(int actid) {
		this.actid = actid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getMinlv() {
		return minlv;
	}

	public void setMinlv(int minlv) {
		this.minlv = minlv;
	}

	public int getMaxlv() {
		return maxlv;
	}

	public void setMaxlv(int maxlv) {
		this.maxlv = maxlv;
	}

	public List<Reward> getReward() {
		return reward;
	}

	public void setReward(List<Reward> reward) {
		this.reward = reward;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
