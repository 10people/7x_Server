package com.qx.yabiao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "YunBiaoHistory123")
public class YunBiaoHistory{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 1L;
	@Id
	public long junZhuId;


	/*押镖成功次数*/
	public int successYB;
	/*劫镖胜利次数*/
	public int successJB;
	/*押镖历史次数*/
	public int historyYB;
	/*劫镖历史次数*/
	public int historyJB;
	

}