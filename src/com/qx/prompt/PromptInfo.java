package com.qx.prompt;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="prompt_info")
public class PromptInfo {
	@Id 
	public long jId;
	public int ldComfortCount; //掠夺安慰别人次数
	public int ybComfortCount; // 押镖安慰别人次数
}
