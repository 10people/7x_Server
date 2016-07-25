package com.qx.quartz.job;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.guojia.GuoJiaBean;
import com.qx.persistent.HibernateUtil;

public class GuojiaChouhenJieSuanJob implements Job {
	public Logger log = LoggerFactory.getLogger(GuojiaChouhenJieSuanJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("国家仇恨刷新开始");
		for (int i = 1; i < 8; i++) {
			GuoJiaBean bean = HibernateUtil.find(GuoJiaBean.class, i);
			if(bean!=null){
				synchronized (bean) {
					setttleGuojiaHate(bean);
				}
			}
		}
		log.info("国家仇恨刷新完成");
	}
	public void setttleGuojiaHate(GuoJiaBean bean) {
		if(bean == null){
			return;
		}
		log.info("国家{}前四个期的仇恨刷新开始----THE-START",bean.guoJiaId);
		settleHate4Month2(bean);
		log.info("国家{}前四个期的仇恨刷新结束----THE-END",bean.guoJiaId);
		refreshGuoJiaHate(bean);
	}
	/**
	 * @Description 刷新上2个期仇恨数据
	 * @param settleHate
	 * @param bean
	 * @return
	 */
	public void settleHate4Month2(GuoJiaBean bean) {
		GuoJiaBean month2bean = HibernateUtil.find(GuoJiaBean.class, bean.guoJiaId+10);
		if(month2bean!=null){
			settleHate4Month3(month2bean);
			log.info("国家{}对其他国家上2个期的仇恨---有数据，刷新开始",bean.guoJiaId+10);
			//交换上1个期和上2个期仇恨值
			month2bean=changeGuojiaHate(bean, month2bean);
			HibernateUtil.save(month2bean);
			log.info("国家{}对其他国家上2个期的仇恨---有数据，刷新保存",bean.guoJiaId+10);
		}else{
			log.info("国家{}对其他国家上2个期的仇恨---无数据，初始化开始",bean.guoJiaId+10);
			month2bean=new GuoJiaBean();
			month2bean.guoJiaId=bean.guoJiaId+10;
			//交换上1个期和上2个期仇恨值
			month2bean=changeGuojiaHate(bean, month2bean);
			HibernateUtil.save(month2bean);
		}
	}
	/**
	 * @Description 刷新上3个期仇恨数据
	 * @param settleHate
	 * @param month2bean
	 * @return
	 */
	public void settleHate4Month3(GuoJiaBean month2bean) {
		GuoJiaBean month3bean = HibernateUtil.find(GuoJiaBean.class, month2bean.guoJiaId+10);
		if(month3bean!=null){ 
			settleHate4Month4(month3bean);
			log.info("国家{}对其他国家上3个期的仇恨---有数据，刷新开始",month3bean.guoJiaId);
			month3bean=changeGuojiaHate(month2bean, month3bean);
			HibernateUtil.save(month3bean);
			log.info("国家{}对其他国家上3个期的仇恨---有数据，刷新保存",month3bean.guoJiaId);
		}else{
			log.info("国家{}对其他国家上3个期的仇恨---无数据，初始化开始", month2bean.guoJiaId+10);
			month3bean=new GuoJiaBean();
			month3bean.guoJiaId=month2bean.guoJiaId+10;
			month3bean=changeGuojiaHate(month2bean, month3bean);
			HibernateUtil.save(month3bean);
		}
	}
	/**
	 * @Description 刷新上4个期仇恨数据
	 * @param settleHate
	 * @param month3bean
	 * @return
	 */
	public void settleHate4Month4(GuoJiaBean month3bean) {
		GuoJiaBean month4bean = HibernateUtil.find(GuoJiaBean.class, month3bean.guoJiaId+10);
		if(month4bean!=null){
			log.info("国家{}对其他国家上4个期的仇恨---有数据，刷新开始",month4bean.guoJiaId+10);
			month4bean=changeGuojiaHate(month3bean, month4bean);
			HibernateUtil.save(month4bean);
			log.info("国家{}对其他国家上4个期的仇恨---有数据，刷新保存",month4bean.guoJiaId+10);
		}else{
			log.info("国家{}对其他国家上4个期的仇恨---无数据，初始化开始", month3bean.guoJiaId+10);
			month4bean=new GuoJiaBean();
			month4bean.guoJiaId=month3bean.guoJiaId+10;
			month4bean=changeGuojiaHate(month3bean, month4bean);
			HibernateUtil.save(month4bean);
		}
	}
	
	/**
	 * @Description 刷新国家仇恨数据
	 * @param jiesuanhate
	 * @param nowBean
	 */
	public void refreshGuoJiaHate(GuoJiaBean nowBean) {
		nowBean.hate_1=0;
		nowBean.hate_2=0;
		nowBean.hate_3=0;
		nowBean.hate_4=0;
		nowBean.hate_5=0;
		nowBean.hate_6=0;
		nowBean.hate_7=0;
		nowBean.shengWang=0;

		HibernateUtil.save(nowBean);
	}
	
	
	/**
	 * @Description 更新上个期国家仇恨数据
	 * @param thisbean 某个期
	 * @param lastbean 某个期的前一个期
	 * @param lastBeanId 某个期的前一个期国家Id =当前期国家id+10
	 * @return
	 */
	public GuoJiaBean changeGuojiaHate(GuoJiaBean thisbean,GuoJiaBean lastbean) {
		log.info("国家{}-{}仇恨值交换更新更新",thisbean.guoJiaId,lastbean.guoJiaId);
		lastbean.hate_1=thisbean.hate_1;
		lastbean.hate_2=thisbean.hate_2;
		lastbean.hate_3=thisbean.hate_3;
		lastbean.hate_4=thisbean.hate_4;
		lastbean.hate_5=thisbean.hate_5;
		lastbean.hate_6=thisbean.hate_6;
		lastbean.hate_7=thisbean.hate_7;
		lastbean.diDuiGuo_1=thisbean.diDuiGuo_1;
		lastbean.diDuiGuo_2=thisbean.diDuiGuo_2;
		lastbean.shengWang=thisbean.shengWang;
		return lastbean;
	}
	
}
   