package com.qx.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.DailyTaskProtos.DailyTaskFinishInform;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskInfo;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskListResponse;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskRewardRequest;
import qxmobile.protobuf.DailyTaskProtos.DailyTaskRewardResponse;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.HuoYueTemp;
import com.manu.dynasty.template.RenWu;
import com.manu.dynasty.util.DateUtils;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.vip.VipMgr;

/**
 * 每日任务管理
 * @author lizhaowen
 * change by wangzhuan
 *
 */
public class DailyTaskMgr extends EventProc {
	public static DailyTaskMgr INSTANCE;
	public Logger logger = LoggerFactory.getLogger(DailyTaskMgr.class);
	/** <类型，列表> **/
	public static Map<Integer, RenWu> renWuMap = new HashMap<Integer, RenWu>();
	public static Map<Integer, RenWu> shangjiaoRenwuMap = new HashMap<Integer, RenWu>();
	public static final int space = 100;
	public static int maxTaskId = -1;
	public static List<Integer> taskIdArr;
	public static int dailyTaskOpenRowId = 106;
	public final  static int allGongJinNum = 8;
	
	public static Map<Integer, HuoYueTemp> HuoYueTempMap = new HashMap<Integer, HuoYueTemp>();
	public static int weekHuoYue = 1;
	public static int dailyHuoYue = 0;
	
	public DailyTaskMgr() {
		INSTANCE = this;
		initData();
	}

	public void initData() {
		@SuppressWarnings("unchecked")
		List<RenWu> list = TempletService.listAll(RenWu.class.getSimpleName());
		if(list == null) {
			logger.error("每日任务配置文件加载错误");
			return;
		}
		taskIdArr = new ArrayList<Integer>();
		for(RenWu renWu : list) {
			int rId = renWu.id;
			if(rId <= DailyTaskConstants.give_gongJin){
				renWuMap.put(rId, renWu);
				taskIdArr.add(rId);
				maxTaskId = Math.max(rId,  maxTaskId);
			}
			if(rId >= DailyTaskConstants.give_gongJin){
				shangjiaoRenwuMap.put(rId, renWu);
			}
		}
		
		List<HuoYueTemp> hl = TempletService.listAll(HuoYueTemp.class.getSimpleName());
		for(HuoYueTemp t: hl){
			HuoYueTempMap.put(t.id, t);
		}
	}
	
	/**
	 * 请求每日任务列表
	 * @param cmd
	 * @param session
	 */
	public void taskListRequest(int cmd, IoSession session) {
		Long jId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(jId == null){
			logger.error("请求每日任务列表失败君主不在线， cmd:{}", cmd);
			return;
		}
//		boolean isOpen =FunctionOpenMgr.inst.isFunctionOpen(dailyTaskOpenRowId, jId, -1);
//		if(!isOpen){
//			logger.error("请求每日任务列表失败：君主：{}，主线任务尚未完成到可以开启此功能", jId);
//			return;
//		}
		List<DailyTaskBean> tasks  = getDailyTasks(jId);
		if(tasks == null || tasks.size() == 0){
			// 减小压力不存数据库
			tasks = initTaskList(jId);
		}else{
			List<DailyTaskBean> removList = new ArrayList<DailyTaskBean>();
			for(DailyTaskBean b: tasks){
				int id = (int)(b.dbId % 100L);
				if(renWuMap.get(id) == null){
					removList.add(b);
					HibernateUtil.delete(b);
				}
			}
			tasks.removeAll(removList);
			
			// 重置 task
			resetOrAddTaskList(jId, tasks);
		}
		List<DailyTaskInfo> taskInfoList = fillTaskInfo(tasks, jId);
		DailyTaskListResponse.Builder response = DailyTaskListResponse.newBuilder();
		for(DailyTaskInfo taskInfo : taskInfoList) {
			response.addTaskInfo(taskInfo);
		}
		// 判断是不是有联盟
		boolean has = isLianMeng(jId);
		response.setHasGuild(has);
		// 增加任务活跃度相关情况
		DailyTaskActivity acti = HibernateUtil.find(DailyTaskActivity.class, jId);
		if(acti == null){
			acti = new DailyTaskActivity();
			acti.jid = jId;
		}else{
			resetDailyTaskActivity(acti);
		}
		int timeDistance = DateUtils.timeDistanceBySecond();
		response.setRemainTime(timeDistance);
		int todayHuo = acti.todyHuoYue;
		int weekHuo = acti.weekHuoYue;
		response.setTodaylHuoYue(todayHuo);
		response.setWeekHuoYue(weekHuo);
		response.addAwardStatus(acti.isGet1? 1: 0);
		response.addAwardStatus(acti.isGet2? 1: 0);
		response.addAwardStatus(acti.isGet3? 1: 0);
		response.addAwardStatus(acti.isGet4? 1: 0);
		response.addAwardStatus(acti.isGet5? 1: 0);
		response.addAwardStatus(acti.isGet6? 1: 0);
		response.addAwardStatus(acti.isGet7? 1: 0);
		session.write(response.build());
	}
	
	public void resetDailyTaskActivity(DailyTaskActivity acti){
		if(acti == null){
			return;
		}
		Date now = new Date();
		boolean isSave = false;
		if(acti.lastResetWeek != null){
			boolean isSameWeek = DateUtils.isSameWeek_CN(now, acti.lastResetWeek);
			if(!isSameWeek){
				acti.weekHuoYue = 0;
				acti.lastResetWeek = now;
				acti.isGet6 = false;
				acti.isGet7 = false;
				isSave = true;
			}
		}
		if(acti.lastResetDaily != null && DateUtils.isTimeToReset(acti.lastResetDaily,
				CanShu.REFRESHTIME_PURCHASE)) {
			acti.todyHuoYue = 0;
			acti.lastResetDaily = now;
			acti.isGet1 = false;
			acti.isGet2 = false;
			acti.isGet3 = false;
			acti.isGet4 = false;
			acti.isGet5 = false;
			isSave = true;
		}
		if(isSave){
			HibernateUtil.save(acti);
		}
	}
	public List<DailyTaskInfo> fillTaskInfo(List<DailyTaskBean> tasks, long jid){
		List<DailyTaskInfo> taskInfoList = new ArrayList<DailyTaskInfo>();
		if(tasks == null) return taskInfoList;
		DailyTaskInfo.Builder taskInfo = null;
		int taskId = 0;
		for(DailyTaskBean task: tasks){
			if(task.isFinish && task.isGetReward){
				continue;
			}
			/*
			 * 判断是不是要显示领取体力
			 */
			taskId = getRenWuIdBydbId(task.dbId);
			if(taskId == DailyTaskConstants.get_tili_id_11 && 
					!isShowTiliTask(DailyTaskConstants.get_tili_id_11)){
					continue;
			}
			if(taskId == DailyTaskConstants.get_tili_id_12 && 
					!isShowTiliTask(DailyTaskConstants.get_tili_id_12)){
					continue;
			}
			if(taskId == DailyTaskConstants.get_tili_id_13 && 
					!isShowTiliTask(DailyTaskConstants.get_tili_id_13)){
					continue;
			}
	
			taskInfo = DailyTaskInfo.newBuilder();
			taskInfo.setTaskId(taskId);
			taskInfo.setJindu(task.jundu);
			taskInfo.setIsFinish(task.isFinish);
			/*
			 * 判断月卡任务完成状态
			 */
			if(taskId == DailyTaskConstants.get_yueka_money_id 
					&& isYuekaTaskFinish(jid)){
				taskInfo.setIsFinish(true);
			}
			taskInfo.setIsGet(task.isGetReward);
			/*
			 * 缴纳贡金总数
			 */
			if(taskId == DailyTaskConstants.give_gongJin){
				int taskCon = task.gongJinCondition;
				taskInfo.setTaskId(taskId + taskCon -1);
			}
			// 添加
			taskInfoList.add(taskInfo.build());
		}
		return taskInfoList;
	}
	
	public boolean isShowTiliTask(int type){
		int hour = DateUtils.getHourOfDay(new Date());
		switch(type){
			case DailyTaskConstants.get_tili_id_11:
			{
				if(hour >= DailyTaskConstants.show_tili_clock_12 && 
						hour < DailyTaskConstants.show_tili_clock_14){
					return true;
				}
				return false;
			}
			case DailyTaskConstants.get_tili_id_12:
			{
				if(hour >= DailyTaskConstants.show_tili_clock_18 && 
						hour < DailyTaskConstants.show_tili_clock_20){
					return true;
				}
				return false;
			}
			case DailyTaskConstants.get_tili_id_13:
			{
				if(hour >= DailyTaskConstants.show_tili_clock_21 && 
						hour < DailyTaskConstants.show_tili_clock_24){
					return true;
				}
				return false;
			}
		}
		return false;
	}
	/**
	 * 检查是否需要重置每日任务
	 * @param taskBean
	 */
	public boolean isTimeToReset(Date lastDate) {
		if(lastDate == null){
			return false;
		}
		Date curDate = new Date();
		boolean yes = DateUtils.isSameSideOfFour(lastDate, curDate);
		if(yes){
			return false;
		}
		return true;
	}
	/**
	 * 每日任务处理
	 * @param taskCondition
	 */
	public void taskProcess(DailyTaskCondition taskCondition) {
		long jzId = taskCondition.junzhuId;
		int rwId  = taskCondition.renWuId;
//		boolean isOpen =FunctionOpenMgr.isFunctionOpen(dailyTaskOpenRowId, jzId, -1);
//		if(!isOpen){
//			logger.error("君主:{}主线任务没有开启", jzId);
//			return;
//		}
		DailyTaskBean taskBean = getTaskByTaskId(jzId, rwId);
		if(taskBean == null){
			logger.error("玩家：{}，taskProcess执行，taskBean==null没有renwuId:{}",
					jzId, rwId);
			return ;
		}
		
		// 重置
		resetOneTask(jzId, taskBean, rwId);
		RenWu renWu = renWuMap.get(rwId);
		if(renWu == null) {
			logger.error("taskProcess执行，找不到对应任务的配置信息，renwuId:{}", rwId);
			return;
		}
		taskBean.jundu += taskCondition.jinduAdd;
		int condition = renWu.condition;
		/*
		 * 缴纳贡金的完成条件
		 */
		if(rwId == DailyTaskConstants.give_gongJin){
			condition = taskBean.gongJinCondition;
			if(condition <= 0){
				condition = 1;
				taskBean.gongJinCondition = 1;
			}
		
			rwId = rwId + condition -1;
		}
		// 判断是否完成每日任务,完成则次数设为 需要完成次数
		if(taskBean.jundu >= condition) {
			taskBean.isFinish = true;
		}
		HibernateUtil.save(taskBean);
		logger.info("{}完成任务{}",jzId,rwId);
		ActLog.log.DailyTask(jzId, "", ActLog.vopenid, "", rwId);
		SessionUser user = SessionManager.inst.findByJunZhuId(jzId);
		if(user == null){
			logger.error("找不到相对应的 userjunzhuId:{},可能是从jsp页面上访问该方法", jzId);
			return;
		}
		IoSession session = user.session;
		if(session != null){
			DailyTaskFinishInform.Builder response = DailyTaskFinishInform.newBuilder();
			DailyTaskInfo.Builder taskInfo = DailyTaskInfo.newBuilder();
			taskInfo.setTaskId(rwId);
			taskInfo.setJindu(taskBean.jundu);
			taskInfo.setIsFinish(taskBean.isFinish);
			taskInfo.setIsGet(taskBean.isGetReward);
			response.setTaskInfo(taskInfo.build());
			session.write(response.build());
		} else {
			logger.error("找不到相对应的 IoSession，junzhuId:{},可能是从jsp页面上访问该方法", jzId);
		}
	}

	
	/**
	 * 领取每日任务奖励
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void getTaskReward(int cmd, IoSession session, Builder builder) {
		DailyTaskRewardRequest.Builder request = 
				(DailyTaskRewardRequest.Builder) builder;
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null){
			logger.error("玩家领取每日任务奖励失败：君主不在线");
			return;
		}
		long jId = junzhu.id;
		// taskId 就是 rewuId
		int taskId = request.getTaskId();
		RenWu renWu  = renWuMap.get(taskId);
		int dbtaskId = taskId;
	
		/*
		 * 判断是否是缴纳贡金任务
		 */
		boolean isGongJinTask = false;
		if(taskId >= DailyTaskConstants.give_gongJin){
			isGongJinTask = true;
			renWu = shangjiaoRenwuMap.get(taskId);
			dbtaskId = DailyTaskConstants.give_gongJin;
		}
		
		if(renWu == null) {
			logger.error("玩家：{}，领取每日任务奖励失败：找不到任务配置信息，renwuId:{}",
					jId, taskId);
			return;
		}
		DailyTaskRewardResponse.Builder response = DailyTaskRewardResponse.newBuilder();
		DailyTaskBean taskBean = getTaskByTaskId(jId, dbtaskId);
		if(taskBean == null){
			logger.error("玩家：{}，领取每日任务奖励失败：taskBean不存在，renwuId:{}",
					jId, dbtaskId);
			return;
		}
		
		// 重置任务
		resetOneTask(jId, taskBean, taskId);
		int jindu = taskBean.jundu;
		int condi = renWu.condition;
		String jiangLi = renWu.award;
		int val = renWu.LmGongxian;
		/*
		 * 缴纳贡金
		 */
		if(isGongJinTask){
			condi = taskBean.gongJinCondition;
			if(condi <= 0){
				condi = 1;
			}
		}
		if(jindu < condi || !taskBean.isFinish) {
			logger.error("玩家：{}每日任务还未完成，不能领取奖励，renwuId:{}", jId, taskId);
			response.setTaskId(taskId);
			response.setStatus(false);
			response.setMsg("notFinishRenWu");
			session.write(response.build());
			return;
		}
		if(taskBean.isGetReward){
			logger.error("玩家:{}已经领取了奖励", jId);
			response.setTaskId(taskId);
			response.setStatus(false);
			response.setMsg("hasGet");
			session.write(response.build());
			return;
		}
		// 对于领取体力进行时间判定
		if(taskId == DailyTaskConstants.get_tili_id_11 
				|| taskId == DailyTaskConstants.get_tili_id_12
				|| taskId == DailyTaskConstants.get_tili_id_13)
		{
			boolean yes = isShowTiliTask(taskId);
			if(!yes){
				logger.error("玩家:{}领取体力奖励失败，时间不正确", jId);
				response.setTaskId(taskId);
				response.setStatus(false);
				response.setMsg("timeIsWrong");
				session.write(response.build());
				return;
			}
		}
		// 对于领取月卡返利是否时间依然有效进行判定
		if(taskId == DailyTaskConstants.get_yueka_money_id){
			boolean yes = isYuekaTaskFinish(jId);
			if(!yes){
				logger.error("玩家:{}领取月卡返利失败，没有月卡奖励", jId);
				response.setTaskId(taskId);
				response.setStatus(false);
				response.setMsg("noYueKa");
				session.write(response.build());
				return;
			}
		}
		String[] jiangliArray = jiangLi.split("#");
		for(String jiangli : jiangliArray) {
			String[] infos = jiangli.split(":");
			int type = Integer.parseInt(infos[0]);
			int itemId = Integer.parseInt(infos[1]);
			int count = Integer.parseInt(infos[2]);
			AwardTemp a = new AwardTemp();
			a.setItemType(type);
			a.setItemId(itemId);
			a.setItemNum(count);
			AwardMgr.inst.giveReward(session, a, junzhu);
			logger.info("给予{}奖励 type {} id {} cnt{}", junzhu.id,type,itemId,count);
		}
		// 如果有联盟奖励 并且 玩家有联盟的则领取联盟奖励
		if(val > 0){
			AllianceBean a = AllianceMgr.inst.getAllianceByJunZid(jId);
			if(a != null){
				// 增加联盟经验
				AllianceMgr.inst.addAllianceExp(val, a);
				// 增加联盟建设值
				AllianceMgr.inst.changeAlianceBuild(a, val);
			}
		}
		
		// 每日任务之活跃度记录
		DailyTaskActivity acti = HibernateUtil.find(DailyTaskActivity.class, junzhu.id);
		if(acti == null){
			acti = new DailyTaskActivity();
			acti.jid = junzhu.id;
		}else{
			resetDailyTaskActivity(acti);
		}
		acti.todyHuoYue += renWu.huoyue;
		acti.weekHuoYue += renWu.huoyue;
		if(acti.lastResetDaily == null) acti.lastResetDaily = new Date();
		if(acti.lastResetWeek == null) acti.lastResetWeek = new Date();
		HibernateUtil.save(acti);
		
		// TODO @ you 添加改变活跃度事件
		EventMgr.addEvent(ED.HUOYUE_CHANGE, new Object[] { jId,acti.todyHuoYue, junzhu.level});
		/*
		 * 是否是缴纳贡金任务
		 */
		boolean reSend = false;
		if(isGongJinTask && condi < allGongJinNum){
			condi += 1;
			taskBean.gongJinCondition  = condi;
			if(taskBean.jundu >= condi){
				taskBean.isFinish = true;
			}else{
				taskBean.isFinish = false;
			}
			taskBean.isGetReward = false;
			reSend = true;
		}else{
			taskBean.isFinish = true;
			taskBean.isGetReward = true;
		}
		HibernateUtil.save(taskBean);
		response.setTaskId(taskId);
		response.setStatus(true);
		response.setMsg("领取成功");
		response.setTodaylHuoYue(acti.todyHuoYue);
		response.setWeekHuoYue(acti.weekHuoYue);
		session.write(response.build());
		if(reSend){
			taskListRequest(1, session);
		}
	}

	public int getTodayHuoYueDu(long jid){
		DailyTaskActivity acti = HibernateUtil.find(DailyTaskActivity.class, jid);
		if(acti == null){
			return 0;
		}
		resetDailyTaskActivity(acti);
		return acti.todyHuoYue;
	}
	
	@Override
	public void proc(Event event) {
		if(event.param == null || !(event.param instanceof DailyTaskCondition)){
			logger.error("event param type is not Class--DailyTaskCondition");
			return;
		}
		DailyTaskCondition taskCondition = (DailyTaskCondition) event.param;
		switch (event.id) {
			case ED.DAILY_TASK_PROCESS:
				taskProcess(taskCondition);
				break;
			default:
				break;
		}
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.DAILY_TASK_PROCESS, this);
	}
	
	public List<DailyTaskBean> getDailyTasks(long jzId){
		long start = jzId * space;
		long end = start + maxTaskId;
		String where = "where id >= " + start + "and id <= " + end;
		List<DailyTaskBean> tasks = HibernateUtil.list(DailyTaskBean.class, where);
		return tasks;
	}

	/**
	 * 初始化task列表
	 * @Title: initTaskList 
	 * @Description:
	 * @param jId
	 * @return
	 */
	public List<DailyTaskBean> initTaskList(long jId){
		List<DailyTaskBean> tasks = new ArrayList<DailyTaskBean>();
		DailyTaskBean task = null;
		for (Integer id: taskIdArr){
			task = initTask(jId, id);
			if(task == null){
				continue;
			}
			tasks.add(task);
		}
		return tasks;
	}

	public DailyTaskBean initTask(long jId, int renWuId){
		RenWu rw = renWuMap.get(renWuId);
		if(rw == null){
			logger.error("初始化任务失败，任务ID为：{}" ,renWuId);
			return null ;
		}
		long dbId = jId * space + renWuId;
		DailyTaskBean task = new DailyTaskBean(); 
		task.dbId = dbId;
		task.jundu = 0;
		task.isFinish = false;
		/*
		 * 获取体力默认为完成状态
		 */
		if(renWuId == DailyTaskConstants.get_tili_id_11 
				|| renWuId == DailyTaskConstants.get_tili_id_12
				|| renWuId == DailyTaskConstants.get_tili_id_13){
			task.isFinish = true;
		}
		/*
		 * 判断月卡的状态
		 */
		if(renWuId == DailyTaskConstants.get_yueka_money_id 
				&& isYuekaTaskFinish(jId)){
			task.isFinish = true;
		}
		task.isGetReward = false;
		task.type = rw.type;
		task.time = new Date();
		return task;
	}
	public DailyTaskBean getTaskByTaskId(long jzId, int renWuId){
		long dbId = jzId * space + renWuId;
		DailyTaskBean task = HibernateUtil.find(DailyTaskBean.class, dbId);
		if(task == null){
			task = initTask(jzId, renWuId);
			if(task == null){
				return null ;
			}
			// 向缓存中添加
			MC.add(task, dbId);
			HibernateUtil.insert(task);
		}
		return task;
	}
	public int getRenWuIdBydbId(long dbId){
		return (int)(dbId % 100L);
	}

	public void resetOrAddTaskList(long jzId, List<DailyTaskBean> tasks){
		Map<Integer, DailyTaskBean> map = new HashMap<Integer, DailyTaskBean>();
		for(DailyTaskBean task: tasks){
			map.put((int)(task.dbId % 100L), task);
		}
		for(Integer taskId: taskIdArr){
			DailyTaskBean task = map.get(taskId);
			if(task == null){
				task = initTask(jzId, taskId);
				if(task == null){
					continue;
				}
				tasks.add(task);
			}else if(isTimeToReset(task.time)){
				tasks.remove(task);
				task = initTask(jzId, taskId);
				if(task == null){
					continue;
				}
				tasks.add(task);
				HibernateUtil.save(task);
			}
		}
	}
	
	public void resetOneTask(long jzId, DailyTaskBean task, int renWuId){
		if(task == null) return;
		if(task.time != null && isTimeToReset(task.time)){
			task.jundu = 0;
			task.isFinish = false;
			task.isGetReward = false;
			task.type = renWuMap.get(renWuId).type;
			task.time = new Date();
			HibernateUtil.save(task);
		}
	}
	public int[] getTwoGuoJia(long jId){
		JunZhu jz = HibernateUtil.find(JunZhu.class, jId);
		if(jz == null){
			return null;
		}
		int[] datas = new int[2];
		for(int i=0; i<2; i++){
			do{
				datas[i] = MathUtils.getRandomInMax(1, 7);
			}while(datas[i] == jz.guoJiaId);
		}
		if(datas[0] == datas[1]){
			do{
				datas[1] = MathUtils.getRandomInMax(1, 7);
			}while(datas[1] == jz.guoJiaId || datas[1] == datas[0]);
		}
		return datas;
	}

	public boolean isLianMeng(long id){
		AllianceBean b = AllianceMgr.inst.getAllianceByJunZid(id);
		if(b == null){
			return false;
		}
		return true;
	}
	public boolean isYuekaTaskFinish(long jid){
		return VipMgr.INSTANCE.hasYueKaAward(jid);
	}
	
	public void getHuoYueDuAward(int id, IoSession session, Builder builder){
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("找不到君主");
			return;
		}
		ErrorMessage.Builder req = (ErrorMessage.Builder)builder;
		int huodongid = req.getErrorCode();
		DailyTaskActivity acti = HibernateUtil.find(DailyTaskActivity.class, junzhu.id);
		if(acti == null){
			return;
		}else{
			resetDailyTaskActivity(acti);
		}
		// 是否已经领奖
		if(isGet(acti, huodongid)){
			logger.info("玩家：{}每日任务活跃度数：{}奖励已经领取过", junzhu.id, huodongid);
			sendError(session, huodongid, PD.dailyTask_get_huoYue_award_resp, 1);
			return;
		}
		HuoYueTemp t =  HuoYueTempMap.get(huodongid);
		if(t == null){
			logger.error("HuoYueTempMap 获取huodongid：{}的配置失败", huodongid);
			sendError(session,huodongid, PD.dailyTask_get_huoYue_award_resp, 2);
			return;
		}
		// 是否达到领取条件
		if(t.type == weekHuoYue){
			if(acti.weekHuoYue < t.needNum){
				logger.info("玩家：{}的每日任务周活跃度数：{}太小，不够领奖", junzhu.id, acti.weekHuoYue);
				sendError(session, huodongid, PD.dailyTask_get_huoYue_award_resp, 3);
				return;
			}
		}else if(t.type == dailyHuoYue){
			if(acti.todyHuoYue < t.needNum){
				logger.info("玩家：{}的每日任务日活跃度数：{}太小，不够领奖", junzhu.id, acti.todyHuoYue);
				sendError(session, huodongid, PD.dailyTask_get_huoYue_award_resp, 4);
				return;
			}
		}
		// 领奖
		AwardMgr.inst.giveReward(session, t.award, junzhu);
		// 记录已领奖
		setGet(acti, huodongid, true);
		HibernateUtil.save(acti);
		sendError(session,huodongid, PD.dailyTask_get_huoYue_award_resp, 0);
	}
	public void sendError(IoSession session,int xiangZiid, short PDid, int errorCode) {
		if (session == null) {
			logger.warn("session is null: {}", errorCode);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(errorCode);
		test.setErrorDesc(xiangZiid+"");
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PDid;
		pm.builder = test;
		session.write(pm);
	}
	public void setGet(DailyTaskActivity acti, int huodongid, boolean status){
		if(acti == null) return;
		switch(huodongid){
		case 1: acti.isGet1 = status;break;
		case 2: acti.isGet2 = status;break;
		case 3: acti.isGet3 = status;break;
		case 4: acti.isGet4 = status;break;
		case 5: acti.isGet5 = status;break;
		case 6: acti.isGet6 = status;break;
		case 7: acti.isGet7 = status;break;
		}
	}
	public boolean isGet(DailyTaskActivity acti, int huodongid){
		if(acti == null) return false;
		switch(huodongid){
		case 1: return acti.isGet1;
		case 2: return acti.isGet2;
		case 3: return acti.isGet3;
		case 4: return acti.isGet4;
		case 5: return acti.isGet5;
		case 6: return acti.isGet6;
		case 7: return acti.isGet7;
		}
		logger.error("没有活跃度id：{}的数据表DailyTaskActivity数据", huodongid);
		return false;
	}
}
