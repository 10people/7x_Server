package com.qx.quartz.job;

import java.util.Arrays;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.guojia.GuoJiaBean;
import com.qx.guojia.GuoJiaMgr;
import com.qx.persistent.HibernateUtil;

public class GuojiaSetDiDuiGuoJob implements Job {
	private Logger log = LoggerFactory.getLogger(GuojiaSetDiDuiGuoJob.class);
	public double qz4Month1=GuoJiaMgr.inst.chouHenJiSuanMap.get(0); 
	public double qz4Month2=GuoJiaMgr.inst.chouHenJiSuanMap.get(1);
	public double qz4Month3=GuoJiaMgr.inst.chouHenJiSuanMap.get(2);
	public double qz4Month4=GuoJiaMgr.inst.chouHenJiSuanMap.get(3);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("国家仇恨结算开始");
		for (int i = 1; i < 8; i++) {
			GuoJiaBean bean = HibernateUtil.find(GuoJiaBean.class, i);
			if(bean!=null){
				synchronized (bean) {
					setttleGuojiaHate(bean);
				}
			}
		}
		log.info("国家仇恨结算完成");
	}
	public void setttleGuojiaHate(GuoJiaBean month1bean) {
		if(month1bean == null){
			return;
		}
		GuoJiaBean month2bean = HibernateUtil.find(GuoJiaBean.class, month1bean.guoJiaId+10);
		GuoJiaBean month3bean = HibernateUtil.find(GuoJiaBean.class, month1bean.guoJiaId+20);
		GuoJiaBean month4bean = HibernateUtil.find(GuoJiaBean.class, month1bean.guoJiaId+30);
		if(month2bean == null){
			month2bean = new GuoJiaBean();
		}
		if(month3bean == null){
			month3bean = new GuoJiaBean();
		}
		if(month4bean == null){
			month4bean = new GuoJiaBean();
		}
		
		int hate1 = getHate(month1bean.hate_1, month2bean.hate_1, month3bean.hate_1, month4bean.hate_1);
		int hate2 = getHate(month1bean.hate_2, month2bean.hate_2, month3bean.hate_2, month4bean.hate_2);
		int hate3 = getHate(month1bean.hate_3, month2bean.hate_3, month3bean.hate_3, month4bean.hate_3);
		int hate4 = getHate(month1bean.hate_4, month2bean.hate_4, month3bean.hate_4, month4bean.hate_4);
		int hate5 = getHate(month1bean.hate_5, month2bean.hate_5, month3bean.hate_5, month4bean.hate_5);
		int hate6 = getHate(month1bean.hate_6, month2bean.hate_6, month3bean.hate_6, month4bean.hate_6);
		int hate7 = getHate(month1bean.hate_7, month2bean.hate_7, month3bean.hate_7, month4bean.hate_7);
		int[] jiesuanhate = new int[]{hate1, hate2, hate3, hate4, hate5, hate6, hate7};
		refreshGuoJiaHate(jiesuanhate, month1bean);
	}

	public int getHate(int monthhate1,int monthhate2,int monthhate3, int monthhate4){
		double value = monthhate1 * qz4Month1 + monthhate2 * qz4Month2
				+ monthhate3 * qz4Month3 + monthhate4 * qz4Month4; 
		return (int)Math.floor(value);
	}
	
	/**
	 * @Description 刷新国家仇恨数据
	 * @param jiesuanhate
	 * @param nowBean
	 */
	public void refreshGuoJiaHate(int[] jiesuanhate ,GuoJiaBean nowBean) {
		log.info("结算国家{}对其他国家的仇恨---{}",nowBean.guoJiaId,Arrays.toString(jiesuanhate));
		Integer[] didui=GuoJiaMgr.inst.getDiDuiGuo(jiesuanhate,nowBean.guoJiaId);
		nowBean.diDuiGuo_1=didui[0];
		nowBean.diDuiGuo_2=didui[1];
		HibernateUtil.save(nowBean);
	}
	
}
   