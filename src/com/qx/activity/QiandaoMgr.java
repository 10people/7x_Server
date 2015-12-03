package com.qx.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Qiandao.GetQiandaoResp;
import qxmobile.protobuf.Qiandao.QiandaoAward;
import qxmobile.protobuf.Qiandao.QiandaoResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.QianDao;
import com.manu.dynasty.template.QianDaoDesc;
import com.manu.dynasty.template.QianDaoMonth;
import com.manu.dynasty.util.StringUtils;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.award.AwardMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;

/**
 * @author hejincheng
 * 
 */
public class QiandaoMgr {
	public Logger logger = LoggerFactory.getLogger(QiandaoMgr.class);
	public static QiandaoMgr instance;
	public static final short SUCCESS = 0;// 签到成功并领取一份奖励
	public static final short ERROR_EXIST = 101;// 今日已签过
	public static final short ERROR_NULL = 102;// 奖励不存在
	// public static final short STATE_Y = 1;// 已签过
	// public static final short STATE_N = 0;// 没签过
	public static Map<Integer, List<QianDao>> awardMap = new HashMap<Integer, List<QianDao>>();
	public Map<Integer, QianDaoDesc> qianDaoDescMap = new HashMap<Integer, QianDaoDesc>();
	public Map<Integer, QianDaoMonth> qianDaoMonthMap = new HashMap<Integer, QianDaoMonth>();
	public static Date debugDate = new Date();// 测试用日期,可调整
	public static boolean DATE_DEBUG = false;// 日期测试开关
	public static int RESET_TIME = 4;// 凌晨4点刷新

	public QiandaoMgr() {
		instance = this;
		initData();
	}

	public void initData() {
		// 加载每月累计登录奖励配置文件
		List<QianDao> awardList = TempletService.listAll(QianDao.class
				.getSimpleName());
		Map<Integer, List<QianDao>> awardMap = new HashMap<Integer, List<QianDao>>();
		for (QianDao tmp : awardList) {
			int month = tmp.getMonth();// 月份
			List<QianDao> tmpList = null;
			if (awardMap.containsKey(month)) {// map中已存在这个月数据
				tmpList = awardMap.get(month);
				tmpList.add(tmp);
			} else {// map中不存在这个月数据
				tmpList = new ArrayList<QianDao>();
				tmpList.add(tmp);
			}
			awardMap.put(month, tmpList);
		}
		QiandaoMgr.awardMap = awardMap;
		
		List<QianDaoDesc> descList = TempletService.listAll(QianDaoDesc.class.getSimpleName());
		Map<Integer, QianDaoDesc> qianDaoDescMap = new HashMap<Integer, QianDaoDesc>();
		for(QianDaoDesc desc : descList) {
			qianDaoDescMap.put(desc.getMonth(), desc);
		}
		this.qianDaoDescMap = qianDaoDescMap;
		
		List<QianDaoMonth> qianDaoMonthList = TempletService.listAll(QianDaoMonth.class
				.getSimpleName());
		Map<Integer, QianDaoMonth> qianDaoMonthMap = new HashMap<Integer, QianDaoMonth>();
		for (QianDaoMonth qianDaoMonth : qianDaoMonthList) {
			qianDaoMonthMap.put(qianDaoMonth.getMonth(), qianDaoMonth);
		}
		this.qianDaoMonthMap = qianDaoMonthMap;
	}

	/**
	 * 获取签到信息
	 * 
	 * @param session
	 * @param builder
	 * @param cmd
	 */
	public void getQiandao(int cmd, IoSession session, Builder builder) {
		Date date = null;
		if (DATE_DEBUG) {
			date = debugDate;
		} else {
			date = new Date();
		}
		Date tmpDate = new Date(date.getTime());
		if(tmpDate.getHours()<RESET_TIME){
			tmpDate.setDate(tmpDate.getDate()-1);
		}
		List<QianDao> awardList = awardMap.get(getMonth(tmpDate));
		GetQiandaoResp.Builder response = GetQiandaoResp.newBuilder();
		if (null == awardList) {
			response.setCnt(0);
			response.setCurDate(getDate(date));
			writeByProtoMsg(session, PD.S_GET_QIANDAO_RESP, response);
			return;
		} else {
			JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
			if (junZhu == null) {
				sendError(session, cmd, "未发现君主");
				logger.error("cmd:{},未发现君主", cmd);
				return;
			}
			QiandaoInfo qiandaoInfo = HibernateUtil.find(QiandaoInfo.class,
					"where junzhuId=" + junZhu.id + "");
			int leijiQiandao = 0;
			if (null != qiandaoInfo) {
				// 进入到了第二个月
				if (isNextMonth(qiandaoInfo.getPreQiandao(), date)) {
					qiandaoInfo.setLeijiQiandao(0);// 累计天数归0
					qiandaoInfo.setQiandaoDate(""); // 签到日期清空
					qiandaoInfo.setGetDoubleDate("");// 领取双倍奖励日期清空
					HibernateUtil.save(qiandaoInfo);
				}
				leijiQiandao = qiandaoInfo.getLeijiQiandao();
			}
			response.setCnt(leijiQiandao);
			// 获取所有的奖励
			for (QianDao qianDao : awardList) {
				QiandaoAward.Builder award = QiandaoAward.newBuilder();
				award.setAwardId(qianDao.getAwardId());
				award.setAwardNum(qianDao.getAwardNum());
				award.setAwardType(qianDao.getAwardType());
				award.setDay(qianDao.getDay());
				award.setId(qianDao.getId());
				award.setMonth(qianDao.getMonth());
				award.setVipDouble(qianDao.getVipDouble());
				if (hasAlreadyQiandao(qiandaoInfo)) {// 今天已签到
					if (qianDao.getDay() == leijiQiandao && getBuqianState(qiandaoInfo, qianDao, junZhu) == 1) {
						award.setState(1);
					} else {
						award.setState(0);
					}
				} else {// 今天未签到
					if (qianDao.getDay() == leijiQiandao + 1) {
						award.setState(1);
					} else {
						award.setState(0);
					}
				}
				response.addAward(award);
			}
			response.setIcon(this.qianDaoMonthMap.get(getMonth(tmpDate)).getIcon());
			response.setDesc(this.qianDaoMonthMap.get(getMonth(tmpDate)).getDesc());
			response.setCurDate(getDate(date));
			writeByProtoMsg(session, PD.S_GET_QIANDAO_RESP, response);
		}
	}

	/**
	 * 签到
	 * 
	 * @param session
	 * @param builder
	 * @param cmd
	 */
	public void qiandao(int cmd, IoSession session, Builder builder) {
		Date date = null;
		if (DATE_DEBUG) {
			date = debugDate;
		} else {
			date = new Date();
		}
		Date tmpDate = new Date(date.getTime());
		if(tmpDate.getHours()<RESET_TIME){
			tmpDate.setDate(tmpDate.getDate()-1);
		}
		List<QianDao> awardList = awardMap.get(getMonth(tmpDate));
		QiandaoResp.Builder response = QiandaoResp.newBuilder();
		response.setVipCount(0);
		if (null == awardList) {
			response.setResult(ERROR_NULL);
			writeByProtoMsg(session, PD.S_QIANDAO_RESP, response);
			return;
		} else {
			JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
			if (junZhu == null) {
				sendError(session, cmd, "未发现君主");
				logger.error("cmd:{},未发现君主", cmd);
				return;
			}
			boolean isBuqian = false;
			/* 记录信息到DB */
			QiandaoInfo qiandaoInfo = HibernateUtil.find(QiandaoInfo.class,
					"where junzhuId=" + junZhu.id + "");
			if (qiandaoInfo == null) {// DB没有签到信息
				qiandaoInfo = new QiandaoInfo();
				qiandaoInfo.setJunzhuId((int) junZhu.id);
				qiandaoInfo.setLeijiQiandao(1);
				qiandaoInfo.setPreQiandao(date);
				qiandaoInfo
						.setQiandaoDate(getMonth(date) + ":" + getDate(date));
				HibernateUtil.insert(qiandaoInfo);
			} else {// DB已有有签到信息
				if (isSameDate(date, qiandaoInfo.getPreQiandao())) {
					// 今日已经签过
					if (!canBuQian(junZhu.id,qiandaoInfo)) {
						// 不能补签
						response.setResult(ERROR_EXIST);
						logger.info("{}今日签到失败，今日已签过", junZhu.id);
						writeByProtoMsg(session, PD.S_QIANDAO_RESP, response);
						return;
					} else {
						// 可以补签，DB签到信息不变
						isBuqian = true;
					}
				} else {
					// 今日未签到
					qiandaoInfo
							.setLeijiQiandao(qiandaoInfo.getLeijiQiandao() + 1);
					if (null == qiandaoInfo.getQiandaoDate()
							|| "".equals(qiandaoInfo.getQiandaoDate())) {
						qiandaoInfo.setQiandaoDate(getMonth(tmpDate) + ":"
								+ getDate(tmpDate));
					} else {
						qiandaoInfo.setQiandaoDate(qiandaoInfo.getQiandaoDate()
								+ "#" + getMonth(tmpDate) + ":" + getDate(tmpDate));
					}
					qiandaoInfo.setPreQiandao(date);
				}
				HibernateUtil.save(qiandaoInfo);
			}
			/* 计算奖励是否双倍 */
			int leijiQiandao = qiandaoInfo.getLeijiQiandao();
			QianDao qianDao = awardList.get(leijiQiandao - 1);// 获取签到的那一天的奖励
			int awardCount = 1;// 奖励份数

			if (qianDao.getVipDouble() != 0
					&& junZhu.vipLevel >= qianDao.getVipDouble()) {
				// 达成如下条件，可领取双倍数量奖励
				// 1、双倍等级不为0，为0代表没有双倍
				// 2、君主等级大于等于双倍领取等级
				if (!isBuqian) {
					awardCount = 2;// 除了补签情况外，领取双倍奖励
				}
				if (null == qiandaoInfo.getGetDoubleDate()
						|| "".equals(qiandaoInfo.getGetDoubleDate())) {
					qiandaoInfo.setGetDoubleDate(qianDao.getMonth() + ":"
							+ qianDao.getDay());
				} else {
					qiandaoInfo
							.setGetDoubleDate(qiandaoInfo.getGetDoubleDate()
									+ "#" + qianDao.getMonth() + ":"
									+ qianDao.getDay());
				}
				HibernateUtil.save(qiandaoInfo);
			}
			response.setVipCount(awardCount);
			/* 添加奖励到君主 */
			QiandaoAward.Builder award = QiandaoAward.newBuilder();
			award.setAwardId(qianDao.getAwardId());
			award.setAwardNum(qianDao.getAwardNum());
			award.setAwardType(qianDao.getAwardType());
			award.setDay(qianDao.getDay());
			award.setId(qianDao.getId());
			award.setMonth(qianDao.getMonth());
			award.setVipDouble(qianDao.getVipDouble());
			// award.setState(isQiandaoByDate(qiandaoInfo, junZhu.id,
			// qianDao.getMonth(), qianDao.getDay()));
			// 添加奖励到账户
			AwardTemp tmp = new AwardTemp();
			tmp.setItemType(award.getAwardType());
			tmp.setItemId(award.getAwardId());
			tmp.setItemNum(award.getAwardNum());
			for (int i = 1; i <= awardCount; i++) {
				response.addAward(award);// 添加到消息体
				AwardMgr.inst.giveReward(session, tmp, junZhu);// 添加到账户
				logger.info("{}领取到奖励 type {}, itemId {}, itemNum {}",
						junZhu.id, award.getAwardType(), award.getAwardId(),
						award.getAwardNum());
			}
			logger.info("{}今日签到成功，获取到{}份{}奖励", junZhu.id, awardCount,
					award.getAwardId());
			response.setResult(SUCCESS);
			writeByProtoMsg(session, PD.S_QIANDAO_RESP, response);
		}
	}

	/**
	 * 今天是否可以补签
	 * 
	 * @param junZhuId
	 * @return false 不可以 true 可以
	 */
	public boolean canBuQian(long junZhuId,QiandaoInfo qiandaoInfo) {
		Date date = null;
		if (DATE_DEBUG) {
			date = debugDate;
		} else {
			date = new Date();
		}
		Date tmpDate = new Date(date.getTime());
		if(tmpDate.getHours()<RESET_TIME){
			tmpDate.setDate(tmpDate.getDate()-1);
		}
		List<QianDao> awardList = awardMap.get(getMonth(tmpDate));
//		QiandaoInfo qiandaoInfo = HibernateUtil.find(QiandaoInfo.class,
//				"where junzhuId=" + junZhuId + "");
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, junZhuId);
		QianDao qianDao = awardList.get(qiandaoInfo.getLeijiQiandao() - 1);
		if (getBuqianState(qiandaoInfo, qianDao, junZhu) == 1) {
			// 满足条件当天签到能够补签
			return true;
		}
		return false;
	}

	/**
	 * 获取这一天是否能够补签
	 * 
	 * @param qiandaoInfo
	 * @param qianDao
	 * @param junZhu
	 * @return 0-不能补签；1-可以补签
	 */
	protected int getBuqianState(QiandaoInfo qiandaoInfo, QianDao qianDao,
			JunZhu junZhu) {
		if (null == qiandaoInfo) {
			return 0;
		}
		int state = 0;
		if (junZhu.vipLevel >= qianDao.getVipDouble()
				&& !isGetDoubleByDate(qiandaoInfo, qianDao.getMonth(),
						qianDao.getDay())&&qianDao.getVipDouble()!=0) {
			// 满足条件
			// 1、vip等级满足双倍条件
			// 2、当天的没有领过双倍奖励
			// 3、当天奖励有双倍
			state = 1;
		}
		return state;
	}

	/**
	 * 判断这一天是否领过双倍奖励
	 * 
	 * @param qiandaoInfo
	 * @param month
	 * @param day
	 * @return
	 */
	protected boolean isGetDoubleByDate(QiandaoInfo qiandaoInfo, int month,
			int day) {
		if (null == qiandaoInfo || qiandaoInfo.getQiandaoDate().equals("")) {
			return false;
		}
		if (null == qiandaoInfo.getGetDoubleDate()
				|| qiandaoInfo.getGetDoubleDate().equals("")) {
			return false;
		}
		String[] dates = StringUtils.split(qiandaoInfo.getGetDoubleDate(), "#");// 获取领过双倍奖励的日期
		for (String str : dates) {
			if (null != str && str.length() > 0) {
				int m = Integer.parseInt(StringUtils.split(str, ":")[0]);
				int d = Integer.parseInt(StringUtils.split(str, ":")[1]);
				if (m == month && d == day) { // 如果这一天领过双倍奖励
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断今天是否签到过
	 * 
	 * @return
	 */
	public boolean hasAlreadyQiandao(QiandaoInfo qiandaoInfo) {
		Date date = null;
		if (DATE_DEBUG) {
			date = debugDate;
		} else {
			date = new Date();
		}
//		QiandaoInfo qiandaoInfo = HibernateUtil.find(QiandaoInfo.class,
//				"where junzhuId=" + JunzhuId + "");
		if (null == qiandaoInfo) {// 签到信息为空，没有签到过
			return false;
		} else if (isSameDate(qiandaoInfo.getPreQiandao(), date)) {// 如果当前日期和上次签到日期是同一天
			return true;
		}
		return false;
	}

	/**
	 * 判断是否进入了第二个月
	 * 
	 * @param pre
	 * @param after
	 * @return
	 */
	protected boolean isNextMonth(Date pre, Date after) {
		long preTmp = pre.getTime();
		long afterTmp = after.getTime();
		
		Date flagDate = new Date(after.getTime());
		flagDate.setDate(1);
		flagDate.setHours(RESET_TIME);
		flagDate.setMinutes(0);
		flagDate.setSeconds(0);
		
		long flag = flagDate.getTime();
		
		if(preTmp<flag&&afterTmp>=flag){
			return true;
		}
		return false;
	}

	/**
	 * 判断是够是同一天
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected boolean isSameDate(Date a, Date b) {
		if (a == null || b == null) {
			return false;
		}
		if(a.getMonth()!=b.getMonth()&&(b.getHours()<RESET_TIME||a.getHours()<RESET_TIME)){
			return true;
		}
//		return a.getYear() == b.getYear() && a.getMonth() == b.getMonth()
//				&& a.getDate() == b.getDate();
		Date aTmp = new Date(a.getTime());
		Date bTmp = new Date(b.getTime());
		long start = 0;
		long end = 0;
		if(aTmp.getHours()<RESET_TIME){
			aTmp.setDate(aTmp.getDate()-1);
		}
		aTmp.setHours(RESET_TIME);
		aTmp.setMinutes(0);
		aTmp.setSeconds(0);
		start = aTmp.getTime();
		end = start + 24*60*60*1000;
		if(bTmp.getTime() >= start&&bTmp.getTime() < end){
			return true;
		}
		return false;
	}

	/**
	 * 发送指定协议号的消息
	 * 
	 * @param session
	 * @param prototype
	 * @param response
	 * @return
	 */
	protected void writeByProtoMsg(IoSession session, int prototype,
			Builder response) {
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = prototype;
		msg.builder = response;
		logger.info("发送协议号为：{}", prototype);
		session.write(msg);
	}

	/**
	 * 发送错误消息
	 * 
	 * @param session
	 * @param cmd
	 * @param msg
	 */
	protected void sendError(IoSession session, int cmd, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	// /**
	// * 根据日期和id判断是否签到
	// *
	// * @param junzhuId
	// * @param day
	// * @return 0-没有签过，1-签过
	// */
	// public int isQiandaoaByDate(QiandaoInfo qiandaoInfo, long junzhuId,
	// int month, int day) {
	// if (null == qiandaoInfo || qiandaoInfo.getQiandaoDate().equals("")) {
	// return STATE_N;
	// }
	// String[] dates = StringUtils.split(qiandaoInfo.getQiandaoDate(), "#");//
	// 获取签到过的日期
	// for (String str : dates) {
	// if (null != str && str.length() > 0) {
	// int m = Integer.parseInt(StringUtils.split(str, ":")[0]);
	// int d = Integer.parseInt(StringUtils.split(str, ":")[1]);
	// if (m == month && d == day) { // 如果这一天签过
	// return STATE_Y;
	// }
	// }
	// }
	// return STATE_N;
	// }

	/**
	 * 返回年份
	 * 
	 * @return
	 */
	protected int getYear(Date date) {
		return date.getYear() + 1900;
	}

	/**
	 * 返回月份
	 * 
	 * @return
	 */
	protected int getMonth(Date date) {
		return date.getMonth() + 1;
	}

	/**
	 * 返回日期
	 * 
	 * @return
	 */
	protected int getDate(Date date) {
		return date.getDate();
	}

	/**
	 * 奖励排序比较器
	 */
	protected Comparator<QianDao> comparator = new Comparator<QianDao>() {
		public int compare(QianDao q1, QianDao q2) {
			// 先排月份
			if (q1.getMonth() != q2.getMonth()) {
				return q1.getMonth() - q2.getMonth();
			} else if (q1.getDay() != q2.getDay()) {
				// 月份相同则按日期排序
				return q1.getDay() - q2.getDay();
			} else {
				// 日期也相同则按id排序
				return q1.getId() - q2.getId();
			}
		}
	};
}
