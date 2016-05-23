package com.qx.task;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="WorkTask")
public class WorkTaskBean {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)	
	/** 自增主键，用于删改任务 */
	public long dbId;
	
	/** 持有任务的君主ID */
	public long jzid;
	
	/** 任务ID, 主线表中的id */
	public int tid;//
	/**
	 * 0: 添加此任务，未完成。
	 * -1：任务已经完成。
	 * -2： 任务已经完成，且已经领奖。
	 * 已领奖的任务客户端看不到。
	 */
	public int progress;
	
	
	/*之前复制表用代码，无视之
	public static void copy(){
		List<WorkTaskBean> list =  HibernateUtil.list(WorkTaskBean.class, "") ;
		for(WorkTaskBean b : list){
			WorkTaskBean2 b2 = new WorkTaskBean2();
			b2.jzid = b.dbId/100;
			b2.tid = b.tid;
			b2.progress = b .progress ;
			HibernateUtil.save(b2);
		}
	}
*/	 
}
