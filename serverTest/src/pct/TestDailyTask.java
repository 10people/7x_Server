package pct;

import java.util.List;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.main.GameClient;

import qxmobile.protobuf.DailyTaskProtos.DailyTaskFinishInform;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskInfo;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskListResponse;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskRewardRequest;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskRewardResponse;
import qxmobile.protobuf.GameTask.TaskInfo;

public class TestDailyTask extends TestBase{
	@Override
	public void req(GameClient cl) {
		super.req(cl);
		sendReq(cl, PD.C_DAILY_TASK_GET_REWARD_REQ);
	}
	public void sendReq(GameClient cl, short gId) {
		//请求关卡信息
		cl.session.write(gId);
		System.out.println("请求每日任务列表");
	}
	
	public void reqGetHuoYueAward(GameClient c1){
		c1.session.write(PD.dailyTask_get_huoYue_award_req);
	}
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		switch (id) {
		case PD.S_DAILY_TASK_LIST_RESP:
			getDailyTaskList(session, builder);
			break;
		case PD.S_DAILY_TASK_GET_REWARD_RESP:
			getAwardResult(session,builder);
			break;
		case PD.S_DAILY_TASK_FINISH_INFORM:
			refreshReqDailyTask(session,builder);
			break;
		default:
			break;
		}
	}
	public void  getDailyTaskList(IoSession session,Builder builder){
		DailyTaskListResponse.Builder response = (DailyTaskListResponse.Builder) builder;
		List<DailyTaskInfo> list = response.getTaskInfoList();
		for (DailyTaskInfo a : list) {
			System.out.println("每日任务Id:"+a.getTaskId());
			if (a.getIsFinish() && !a.getIsGet()) {
				System.out.println(a.getTaskId()+"未领奖");
				DailyTaskRewardRequest.Builder request = DailyTaskRewardRequest.newBuilder();
				request.setTaskId(a.getTaskId());
				ProtobufMsg msg = new ProtobufMsg();
				msg.builder = request;
				msg.id = PD.C_DAILY_TASK_GET_REWARD_REQ;
				session.write(msg);
			} else {
				continue;
			}
		}
	}

	public void getAwardResult(IoSession session,Builder builder){
		DailyTaskRewardResponse.Builder response = (qxmobile.protobuf.DailyTaskProtos.DailyTaskRewardResponse.Builder) builder;
	    if(response.getStatus()){
	    	System.out.println("任务Id:"+response.getTaskId()+"领奖成功");
	    }else{
	    	System.out.println("任务Id:"+response.getTaskId()+"领奖失败"+"原因:"+response.getMsg());
	    }
	}
	/**
	 * 检测到服务器推送任务 完成未领奖 则领奖
	 * @param session
	 * @param builder
	 */
	public void refreshReqDailyTask(IoSession session,Builder builder){
		DailyTaskFinishInform.Builder response = (DailyTaskFinishInform.Builder) builder;
		DailyTaskInfo taskInfo = response.getTaskInfo();
		if(null == taskInfo)return;
		System.out.println("推送每日任务进度信息"+"任务id"+taskInfo.getTaskId());
		if(response.getTaskInfo().getIsFinish() && !response.getTaskInfo().getIsGet()){
			DailyTaskRewardRequest.Builder request = DailyTaskRewardRequest.newBuilder();
			request.setTaskId(taskInfo.getTaskId());
			ProtobufMsg msg = new ProtobufMsg();
			msg.builder = request;
			msg.id = PD.C_DAILY_TASK_GET_REWARD_REQ;
			session.write(msg);
		}
		
	}
}