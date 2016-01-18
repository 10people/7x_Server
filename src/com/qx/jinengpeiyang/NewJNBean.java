package com.qx.jinengpeiyang;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 新技能要给发给客户端，以便做展示。
 * @author 康建虎
 *
 */
@Entity
public class NewJNBean {
	@Id
	public long jzId;
	
	public String ids;
}
