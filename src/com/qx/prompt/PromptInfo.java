package com.qx.prompt;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="prompt_info")
public class PromptInfo {
	@Id 
	public long jId;
	@Column(columnDefinition = "INT default 0")
	public int ldComfortCount; //掠夺安慰别人次数
	@Column(columnDefinition = "INT default 0")
	public int ybComfortCount; // 押镖安慰别人次数
	public Date lastTime; // 上次安慰时间
}
