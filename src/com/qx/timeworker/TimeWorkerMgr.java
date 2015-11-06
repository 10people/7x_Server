package com.qx.timeworker;

import java.util.Date;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.TimeWorkerProtos.TimeWorkerRequest;
import qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.CanShu;
import com.manu.network.SessionAttKey;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;

/**
 * 定时请求任务管理
 * 
 * @author lizhaowen
 * 
 */
public class TimeWorkerMgr extends EventProc {

	public static TimeWorkerMgr instance;
	public static boolean showLog = false;

	private static Logger logger = LoggerFactory.getLogger(TimeWorkerMgr.class);

	/** 请求添加体力 **/
	public static int TYPE_ADD_TILI = 1;

	/** 请求添加洗练次数 **/
	public static int TYPE_ADD_XILIAN = 2;

	/** 操作成功 **/
	public static int OPR_SUCCEED = 1;

	/** 操作失败 **/
	public static int OPR_FAIL = 2;

	public static boolean open = true;

	public TimeWorkerMgr() {
		instance = this;
	}

	public void exec(int id, IoSession session, Builder builder) {
		if (!open) {
			logger.info("定时请求任务未开启");
			return;
		}
		TimeWorkerRequest.Builder request = (qxmobile.protobuf.TimeWorkerProtos.TimeWorkerRequest.Builder) builder;
		// 1 添加体力
		int type = request.getType();
		switch (type) {
			case 1:
				addTiLi(session);
				break;
			default:
				logger.error("unkown operation code {}, type:{}", id, type);
				sendError(session, id, "unkonw operation code " + id + ", type:" + type);
				break;
		}
		EventMgr.addEvent(ED.REFRESH_TIME_WORK, session);
	}

	/**
	 * 错误处理。（完成）
	 * 
	 * @param session
	 * @param code
	 * @param msg
	 */
	private void sendError(IoSession session, int code, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(code);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	/**
	 * 添加体力
	 * 
	 * @param session
	 */
	public void addTiLi(IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("not find the JunZhu by accound id {}",
					(Long) session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		session.write(addTiLi(junZhu, session).build());
	}

	public TimeWorkerResponse.Builder addTiLi(JunZhu junZhu, IoSession session) {
		int needTime = CanShu.ADD_TILI_INTERVAL_TIME;// 毫秒
		if (junZhu == null) {
			logger.error("not find the JunZhu");
			return fillTimeWorkerResponse(TYPE_ADD_TILI, -1, OPR_FAIL,
					"not find the JunZhu", needTime / 1000);
		}
		if (junZhu.tiLi >= junZhu.tiLiMax) {
			if (showLog)
				logger.info(junZhu.name + "的当前体力已达到最大值，不需要再添加，当前等级{}",
						junZhu.level);
			return fillTimeWorkerResponse(TYPE_ADD_TILI, junZhu.tiLi, OPR_FAIL,
					junZhu.name + "的当前体力已达到最大值，不需要再添加，当前等级" + junZhu.level,
					needTime / 1000);
		}
		TimeWorker tiLiWorker = HibernateUtil.find(TimeWorker.class, junZhu.id);
		// 第一次请求定时添加体力操作
		if (tiLiWorker == null) {
			tiLiWorker = initTimeWorker(junZhu.id);
		}
		
		long nowTime = System.currentTimeMillis();
		long lastTime = tiLiWorker.getLastAddTiliTime().getTime();

		if ((nowTime - lastTime) < needTime) {
			logger.info("距离（" + junZhu.name + "）上次增加体力时间间隔不到{}毫秒", needTime);
			int countDown = (int) ((needTime - (nowTime - lastTime)) / 1000);
			return fillTimeWorkerResponse(TYPE_ADD_TILI, junZhu.tiLi, OPR_FAIL,
					"距离君主（" + junZhu.name + "）上次增加体力时间间隔不到" + needTime + "毫秒",
					countDown);
		}

		int addTili = CanShu.ADD_TILI_INTERVAL_VALUE;
		if (junZhu.tiLi < junZhu.tiLiMax) {
			int tempTili = junZhu.tiLi + addTili;
			addTili = tempTili > junZhu.tiLiMax ? (junZhu.tiLiMax - junZhu.tiLi)
					: addTili;
			JunZhuMgr.inst.updateTiLi(junZhu, addTili, "按时送");
		}
		Date date = new Date();
		tiLiWorker.setLastAddTiliTime(date);
		HibernateUtil.save(junZhu);
		HibernateUtil.save(tiLiWorker);
		JunZhuMgr.inst.sendMainInfo(session);
		logger.info("君主（" + junZhu.name + "）自动增加体力值:{}, 时间:{}", addTili, date);
		return fillTimeWorkerResponse(TYPE_ADD_TILI, junZhu.tiLi, OPR_SUCCEED,
				"增加体力成功", needTime / 1000);
	}

	/**
	 * 设置数据返回信息 TimeWorkerResponse.Builder 
	 * @param response 返回消息对象
	 * @param type 请求类型 :1 添加体力，2 装备免费洗练次数
	 * @param value 类型值
	 * @param status 操作是否成功:1成功，2失败
	 * @param msg 携带消息
	 * @param time 剩余时间，单位：秒
	 */
	private TimeWorkerResponse.Builder fillTimeWorkerResponse(int type,
			int value, int status, String msg, int time) {
		TimeWorkerResponse.Builder response = TimeWorkerResponse.newBuilder();
		response.setType(type);
		response.setStatus(status);
		response.setValue(value);
		response.setMsg(msg);
		response.setTime(time);
		return response;
	}

	/**
	 * 请求增加装备洗练次数
	 * 
	 * @param junZhu
	 * @param response
	 */
	public TimeWorkerResponse.Builder addEquipFreeXilianTimes(JunZhu junZhu) {
		int needTime = CanShu.ADD_XILIAN_INTERVAL_TIME;// 毫秒
		int freeTimesMax = CanShu.FREE_XILIAN_TIMES_MAX;
		if (junZhu == null) {
			logger.error("not find the JunZhu");
			return fillTimeWorkerResponse(TYPE_ADD_XILIAN, -1, OPR_FAIL,
					"not find the JunZhu", needTime / 1000);
		}
		TimeWorker xilianWorker = HibernateUtil.find(TimeWorker.class,
				junZhu.id);
		Date date = new Date();
		// 第一次请求定时添加免费洗练次数操作
		if (xilianWorker == null) {
			xilianWorker = initTimeWorker(junZhu.id);
			logger.info("（" + junZhu.name + "）初始化免费洗练次数");
			return fillTimeWorkerResponse(TYPE_ADD_XILIAN, freeTimesMax,
					OPR_FAIL, "免费洗练次数已达上限", needTime / 1000);
		} else {
			if (xilianWorker.getXilianTimes() >= freeTimesMax) {
				logger.info("（" + junZhu.name + "）免费洗练次数已达上限");
				return fillTimeWorkerResponse(TYPE_ADD_XILIAN,
						xilianWorker.getXilianTimes(), OPR_FAIL, "免费洗练次数已达上限",
						needTime / 1000);
			}
			// 间隔时间是否达到 1 小时
			long nowTime = System.currentTimeMillis();
			long lastTime = xilianWorker.getLastAddXilianTime().getTime();
			if ((nowTime - lastTime) < needTime) {
				int countDown = (int) ((needTime - (nowTime - lastTime)) / 1000);
				logger.info("（" + junZhu.name + "）增加免费洗练次数时间间隔不到{}毫秒,剩余{}秒",
						needTime, countDown);
				return fillTimeWorkerResponse(TYPE_ADD_XILIAN,
						xilianWorker.getXilianTimes(), OPR_FAIL,
						"增加免费洗练次数时间间隔不到1小时", countDown);
			}
			int times = xilianWorker.getXilianTimes() + CanShu.ADD_XILIAN_VALUE;
			times = times > freeTimesMax ? freeTimesMax : times;
			xilianWorker.setXilianTimes(times);
			xilianWorker.setLastAddXilianTime(date);
			logger.info("（" + junZhu.name + "）增加免费洗练次数值:{}, 时间:{}",
					CanShu.ADD_TILI_INTERVAL_VALUE, date);
			HibernateUtil.save(xilianWorker);
			return fillTimeWorkerResponse(TYPE_ADD_XILIAN,
					xilianWorker.getXilianTimes(), OPR_SUCCEED, "增加免费洗练成功",
					needTime);
		}
	}

	/**
	 * 扣除免费洗练次数
	 * 
	 * @param junZhuId
	 * @param value
	 * @return 剩余的免费洗练次数
	 */
	public int subFreeXilianTimes(long junZhuId, int value) {
		TimeWorker xilianWorker = HibernateUtil
				.find(TimeWorker.class, junZhuId);
		if (xilianWorker == null) {
			xilianWorker = initTimeWorker(junZhuId);
		}
		int times = xilianWorker.getXilianTimes();
		if(times == CanShu.FREE_XILIAN_TIMES_MAX) {
			xilianWorker.setLastAddXilianTime(new Date());
		}
		times = times - value;
		times = times < 0 ? 0 : times;
		xilianWorker.setXilianTimes(times);
		HibernateUtil.save(xilianWorker);
		return times;
	}

	/**
	 * 取得君主免费洗练次数
	 * 
	 * @param junZhu
	 * @return
	 */
	public int getXilianTimes(JunZhu junZhu) {
		if (junZhu == null) {
			logger.error("not find the JunZhu");
			return -1;
		}
//		TimeWorker xilianWorker = HibernateUtil.find(TimeWorker.class,
//				junZhu.id);
//		if (xilianWorker == null) {
//			xilianWorker = initTimeWorker(junZhu.id);
//		}
		TimeWorker	xilianWorker=calcOfflineXilian(junZhu.id);
		return xilianWorker.getXilianTimes();
	}

	/**
	 * 计算玩家离线状态下应该增加的体力值
	 * 
	 * @param account
	 */
	private void calcOfflineTili(long junZhuId) {
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, junZhuId);
		if (junZhu == null) {
			logger.error("not find junzhu by junZhuId:" + junZhuId);
			return;
		}
		if (junZhu.tiLi >= junZhu.tiLiMax) {
			return;
		}
		TimeWorker tiliWorker = HibernateUtil.find(TimeWorker.class, junZhuId);
		if (tiliWorker == null) {
			tiliWorker = initTimeWorker(junZhuId);
		}
		long nowTime = System.currentTimeMillis();
		long lastTime = tiliWorker.getLastAddTiliTime().getTime();
		int needTime = CanShu.ADD_TILI_INTERVAL_TIME;// 毫秒
		int addTili = (int) ((nowTime - lastTime) / needTime);
		lastTime = lastTime + addTili * needTime;
		Date date = new Date(lastTime);
		if (junZhu.tiLi < junZhu.tiLiMax) {
			int tempTili = junZhu.tiLi + addTili;
			addTili = tempTili > junZhu.tiLiMax ? (junZhu.tiLiMax - junZhu.tiLi)
					: addTili;
			JunZhuMgr.inst.updateTiLi(junZhu, addTili, "离线得");
			logger.info("[{}]离线期间获得体力{}, 添加时间{}", junZhu.name, addTili, date);
		}
		tiliWorker.setLastAddTiliTime(date);
		HibernateUtil.save(junZhu);
		HibernateUtil.save(tiliWorker);
	}

	/**
	 * 获取免费洗练定时请求倒计时，单位：秒
	 * 
	 * @param junZhuId
	 * @return
	 */
	public int getXilianCountDown(long junZhuId) {
		TimeWorker xilianWorker = HibernateUtil
				.find(TimeWorker.class, junZhuId);
		if (xilianWorker == null) {
			xilianWorker = initTimeWorker(junZhuId);
		}
		long nowTime = System.currentTimeMillis();
		long lastTime = xilianWorker.getLastAddXilianTime().getTime();
		int needTime = CanShu.ADD_XILIAN_INTERVAL_TIME;// 毫秒
		int countDown = (int) ((needTime - (nowTime - lastTime)) / 1000);
		logger.info("距离junzhuID:{}下一次增加洗练次数还剩{}秒", junZhuId, countDown);
		return countDown;
	}

	/**
	 * 计算玩家免费洗练次数
	 * 
	 * @param junZhuId
	 */
	public TimeWorker calcOfflineXilian(long junZhuId) { 
		TimeWorker xilianWorker = HibernateUtil.find(TimeWorker.class, junZhuId);
		if (xilianWorker == null) {
			xilianWorker = initTimeWorker(junZhuId);
			return xilianWorker;
		}
		long nowTime = System.currentTimeMillis();
		long lastTime = xilianWorker.getLastAddXilianTime().getTime();
		int needTime = CanShu.ADD_XILIAN_INTERVAL_TIME;// 毫秒
		int freeTimesMax = CanShu.FREE_XILIAN_TIMES_MAX;
		int addTimes = (int) ((nowTime - lastTime) / needTime);
		int times = xilianWorker.getXilianTimes() + addTimes;
		times = times > freeTimesMax ? freeTimesMax : times;
		lastTime = lastTime + addTimes * needTime;
		xilianWorker.setXilianTimes(times);
		xilianWorker.setLastAddXilianTime(new Date(lastTime));
		logger.info("离线增加洗练次数 :{}, junzhuId:{}", addTimes, junZhuId);
		HibernateUtil.save(xilianWorker);
		return xilianWorker;
	}

	/**
	 * 初始化一个玩家 TimeWorker
	 * 
	 * @param junzhuId
	 * @return
	 */
	private TimeWorker initTimeWorker(long junzhuId) {
		TimeWorker timeWorker = new TimeWorker();
		timeWorker.setJunzhuId(junzhuId);
		Date date = new Date(System.currentTimeMillis());
		timeWorker.setLastAddTiliTime(date);
		timeWorker.setLastAddXilianTime(date);
		timeWorker.setXilianTimes(CanShu.FREE_XILIAN_TIMES_MAX);
		// 添加缓存
		MC.add(timeWorker, junzhuId);
		HibernateUtil.insert(timeWorker);
		return timeWorker;
	}

	@Override
	public void proc(Event param) {
		switch (param.id) {
			case ED.ACC_LOGIN:
				calcOfflineTili((Long) param.param);
				break;
			default:
				logger.error("触发了没有注册过的事件类型,id:{}", param.id);
				break;
		}
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);
	}

}
