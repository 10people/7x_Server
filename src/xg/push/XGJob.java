package xg.push;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.qx.task.DailyTaskConstants;

public class XGJob  implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Date date = new Date();
		int hour = date.getHours();
		switch(hour){
		case 11:
			XG.inst.pushGetTili(10);
			break;
		case 17:
			XG.inst.pushGetTili(20);
			break;
		case 20:
			//<PushInfo ID="90" str="周天子：20分钟后，依【竞技】排名封赏百官！" />
			XG.inst.pushGetTili(90);
			break;
		}		
	}

}
