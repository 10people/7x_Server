package com.qx.quartz.job;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.friends.GongHeBean;
import com.qx.friends.GreetMgr;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.prompt.PromptMsgMgr;
import com.qx.prompt.SuBaoConstant;
import com.qx.world.BroadcastMgr;

public class RefreshGongHeInfo  implements Job {
	private Logger log = LoggerFactory.getLogger(RefreshGongHeInfo.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("开始刷新恭贺信息");
		refresh4FirsBZ();
		refresh4FirsLMOpen();
		log.info("结束刷新恭贺信息");
	}
	
	public void refresh4FirsBZ() {
		log.info("开始刷新第一次百战恭贺信息");
		String where = " where start4firstBZ >0 " ;
		List<GongHeBean> list = HibernateUtil.list(GongHeBean.class, where);
		Date now=new Date();
		for(GongHeBean info: list){
			long jzId=info.jzId;
			JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				log.info("结算君主--{}联盟恭贺广播失败，JunZhu未找到",jzId);
				continue;
			}
			int distance2t=(int) (now.getTime()-info.start4firstBZ);
			if(distance2t>=GreetMgr.SETTLE_GONGHE_TIME){
				info.start4firstBZ=-info.start4firstBZ;
				HibernateUtil.save(info);
				log.info("结算君主--{}第一次恭贺奖励",jzId);
				int count=info.times4firstBZ;
				if(count>0){
					String award="0:900002:"+info.award4firstBZ;
					int eventId=SuBaoConstant.settle4baizhan;
					PromptMsgMgr.inst.savePromptMSG4GongHe(jzId, jzId, eventId, new String[]{count+"",null,award});
					String content=GreetMgr.inst.getBroadString(239);
					if(content!=null){
						content=content.replace("*N*",count+"");
						log.info("向--{} 发送系统广播--{}", jzId,content);
						BroadcastMgr.inst.send2JunZhu(content, jzId);
					}
				}
			}
		}
	} 
	public void refresh4FirsLMOpen() {
		log.info("开始刷新联盟开启恭贺信息");
		String where = " where start4LM >0 " ;
		List<GongHeBean> list = HibernateUtil.list(GongHeBean.class, where);
		Date now=new Date();
		for(GongHeBean info: list){
			long jzId=info.jzId;
			JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				log.info("结算君主--{}联盟恭贺广播失败，JunZhu未找到",jzId);
				continue;
			}
			int distance2t=(int) (now.getTime()-info.start4LM);
			if(distance2t>=GreetMgr.SETTLE_GONGHE_TIME){
				info.start4LM=-info.start4LM;
				HibernateUtil.save(info);
				log.info("结算君主--{}第一次恭贺奖励",jzId);
				int count=info.times4LM;
				if(count>0){
					String content=GreetMgr.inst.getBroadString(238);
					if(content!=null){
						content=content.replace("*N*", count+"");
						log.info("向--{} 发送系统广播--{}", jzId,content);
						BroadcastMgr.inst.send2JunZhu(content, jzId);
					}
				}
			}
		}
	} 
}

