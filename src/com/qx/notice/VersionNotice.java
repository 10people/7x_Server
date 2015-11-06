package com.qx.notice;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName: VersionNotice
 * @Description: 版本公告
 * @author 何金成
 * @date 2015年7月7日 上午10:25:10
 * 
 */
@Entity
@Table
public class VersionNotice {
	@Id
	private long id;// id
	private int serverId;// serverId
	private String title;// 公告标题
	private String tag;// 公告标签
	private String content;// 公告内容
	private int notice_order;// 公告顺序1-99，数值越小越靠前

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
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

	public int getNotice_order() {
		return notice_order;
	}

	public void setNotice_order(int notice_order) {
		this.notice_order = notice_order;
	}

}
