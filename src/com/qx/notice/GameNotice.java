package com.qx.notice;

import java.util.Date;

/**
 * @ClassName: GameNotice
 * @Description: 游戏内公告
 * @author 何金成
 * @date 2015年7月7日 上午10:26:14
 * 
 */
public class GameNotice {
	public int id;
	public int serverId;
	public Date starttime;
	public Date endtime;
	public int timeInterval;
	public String content;

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getStarttime() {
		return starttime;
	}

	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}

	public Date getEndtime() {
		return endtime;
	}

	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}

	public int getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(int timeInterval) {
		this.timeInterval = timeInterval;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
