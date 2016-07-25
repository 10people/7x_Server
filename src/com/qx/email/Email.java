package com.qx.email;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes={@Index(name="receiverId",columnList="receiverId")})
public class Email {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	@Column(columnDefinition="int default 0")
	public long senderJzId;
	public int type;
	public String senderName;
	public String title;
	public String taitou;
	public String content;
	public Date sendTime;
	public Date expireTime;
	// 附件 组成形式  type:itemId:count#type:itemId:count(类型，id,数量#类型，id,数量)
	public String goods;
	/** 是否删除，1没有删除，2已经删除 **/
	public int isDelete;
	public int isReaded;
	public int isGetReward;
	public long receiverId;
	public String param = "";//参数
	
	//2015年4月17日16:57:30int改为long
	public void setContent(String content) {
		if(content!=null&&content.length()>250){
			EmailMgr.log.warn("###呵呵这么长的邮件内容--{}",content);
			content=content.substring(0, 250);
		}
		this.content = content;
	}
	
	
}
