package com.qx.guojia;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * 
 * This class is used for ...   
 * 对应的数据库表只有7行记录
 * @author wangZhuan
 * @version   
 *       9.0, 2015年7月15日 下午3:49:46
 */
@Entity
@Table(name="guo_jia")
public class GuoJiaBean {
	/**1~7 已经确定*/
	@Id
	public int guoJiaId;
	/**注意：：别国对本国的厌恶程度， 
	 * hate_1 就表示  国家1  对 国家guojiaId 的厌恶程度*/
	public int hate_1;
	public int hate_2;
	public int hate_3;
	public int hate_4;
	public int hate_5;
	public int hate_6;
	public int hate_7;
	public int diDuiGuo_1;
	public int diDuiGuo_2;
	public int shengWang;
	
}
