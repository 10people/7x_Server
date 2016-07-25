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
	public long id;// id
	public int serverId;// serverId
	public String title;// 公告标题
	public String tag;// 公告标签
	public String content;// 公告内容
	public int notice_order;// 公告顺序1-99，数值越小越靠前
}
