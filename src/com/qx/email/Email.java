package com.qx.email;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Email {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;//2015年4月17日16:57:30int改为long
	@Column(columnDefinition="int default 0")
	public long senderJzId;
	public int type;
	private String senderName;
	private String title;
	public String taitou;
	private String content;
	public Date sendTime;
	public Date expireTime;
	// 附件 组成形式  type:itemId:count#type:itemId:count(类型，id,数量#类型，id,数量)
	private String goods;
	/** 是否删除，1没有删除，2已经删除 **/
	private int isDelete;
	public int isReaded;
	public int isGetReward;
	private long receiverId;
	public String param = "";//参数
	
	//2015年4月17日16:57:30int改为long
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
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
	public int getIsDelete() {
		return isDelete;
	}
	public void setIsDelete(int isDelete) {
		this.isDelete = isDelete;
	}
	public String getGoods() {
		return goods;
	}
	public void setGoods(String goods) {
		this.goods = goods;
	}
	public long getReceiverId() {
		return receiverId;
	}
	public void setReceiverId(long receiverId) {
		this.receiverId = receiverId;
	}
	public int getIsReaded() {
		return isReaded;
	}
	public void setIsReaded(int isReaded) {
		this.isReaded = isReaded;
	}
	
}
