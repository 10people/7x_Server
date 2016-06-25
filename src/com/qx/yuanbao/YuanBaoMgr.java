package com.qx.yuanbao;

import java.util.Date;

import log.CunLiangLog;
import log.OurLog;
import log.parser.ReasonMgr;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.qx.account.AccountManager;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.util.TableIDCreator;

/**
 * @author 
 * 
 */
public class YuanBaoMgr extends EventProc{
	public static YuanBaoMgr inst;
	public Logger logger = LoggerFactory.getLogger(YuanBaoMgr.class);

	public YuanBaoMgr() {
		inst = this;
		initData();
	}

	public void initData() {

	}

	/**
	 * 修改元宝数量
	 * 
	 * @param junzhu
	 *            君主信息
	 * @param change
	 *            增量
	 * @param money
	 *            消费人民币金额
	 * @param price
	 *            单价
	 * @param type
	 *            消费类型，对应YBType类
	 * @param reason
	 *            变更原因
	 * @param type
	 *            使用类型
	 */
	public synchronized void diff(JunZhu junzhu, int change, int money, int price, int type,
			String reason) {
		int yuanbaoBefore = junzhu.yuanBao;
		junzhu.yuanBao += change;
		int yuanbaoAfter = junzhu.yuanBao;
		logger.info(
				"元宝计算：君主id{},元宝计算：账号{},元宝计算：变动前{},元宝计算：增量{},元宝计算：变动后{},元宝计算：原因{}",
				junzhu.id, junzhu.name, yuanbaoBefore, change, yuanbaoAfter,
				reason);
		//
		if(type != YBType.YB_YSDK){
			TXQuery t = new TXQuery();
			t.jzId = junzhu.id;
			t.pre_save_amt = yuanbaoBefore;
			t.new_save_amt = yuanbaoAfter;
			t.diff = change;
			IoSession session = AccountManager.sessionMap.get(junzhu.id);
			if(session != null){
				t.params = (String) session.getAttribute("TXClientInfo");
				TXQueryMgr.inst.q.add(t);
			}
		}
		// 存到数据库
		YuanBaoInfo info = new YuanBaoInfo();

		// 改自增主键为指定
		// 2015年4月17日16:57:30int改为long
		long dbId = (TableIDCreator.getTableID(YuanBaoInfo.class, 1L));
		info.setDbId(dbId);

		info.setYuanbaoBefore(yuanbaoBefore);
		info.setYuanbaoAfter(yuanbaoAfter);
		info.setYuanbaoChange(change);
		info.setOwnerid(junzhu.id);
		info.setTimestamp(new Date());
		info.setReason(reason);
		info.setCostMoney(money);
		info.setPrice(price);
		info.setType(type);
		HibernateUtil.save(info);
		//
		int Reason = ReasonMgr.inst.getId(reason);
		OurLog.log.MoneyFlow(junzhu.level, junzhu.yuanBao, change, Reason, change>0?0:1, 1, String.valueOf(junzhu.id));
		CunLiangLog.inst.yuanBaoChange(junzhu.id, junzhu.yuanBao);
	}

	public synchronized void checkCharge(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		ErrorMessage.Builder req = (ErrorMessage.Builder)builder;
		int amt = req.getErrorCode();
		int type = req.getCmd();
		BillHist b = new BillHist();
		b.jzId = jz.id;
		b.curYB = jz.yuanBao;
		b.wantYB = amt;
		b.buyItemId = Integer.parseInt(req.getErrorDesc());
		b.dt = new Date();
		if(type == 40){//sdk充值结束后，如果成功了，发40
			b.type = "客户端充值成功";
			addTask(session, jz, b,8);//在2分钟之内间隔15秒多次调用
		}else if(type == 10){
			b.type = "例行查询";
			addTask(session, jz, b, 1);
		}else if(type == 20){//请查余额,弹sdk界面前，发20
			b.type = "发起充值";
		}else if(type == 50){//充值取消发50，失败发60
			b.type = "取消";
		}else if(type == 60){//充值取消发50，失败发60
			b.type = "失败";
		}else{
			b.type = "未知:"+req.getErrorCode();
			logger.error("未知类型 {} amt{} of jz {}",type, amt,jz.id);
		}
		HibernateUtil.insert(b);
		logger.info("{} 汇报 {} buy {}", jz.id, b.type, b.buyItemId);
	}

	public void addTask(IoSession session, JunZhu jz, BillHist b, int times) {
		try{
			TXQuery tq = new TXQuery();
			tq.pre_save_amt = HibernateUtil.getColumnValueMaxOnWhere(BillHist.class,
					"save_amt", "where jzId="+jz.id);
			tq.jzId = jz.id;
			tq.buyItemId = b.buyItemId;
			tq.params = (String) session.getAttribute("TXClientInfo");
			tq.retry = times;
			TXQueryMgr.inst.q.add(tq);
		}catch(Exception e){
			logger.error(jz.id+"创建查询请求失败 {}", e);
		}
	}

	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.ACC_LOGIN:{
			long junZhuId = (long)event.param;
			addQuery(junZhuId);
			break;
		}
		}
	}

	public void addQuery(long junZhuId) {
		IoSession ss = AccountManager.sessionMap.get(junZhuId);
		if(ss == null){
			return;
		}
		ErrorMessage.Builder req = ErrorMessage.newBuilder();
		req.setCmd(10);
		req.setErrorCode(0);
		req.setErrorDesc("-20160525");
		checkCharge(0,ss,req);
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);		
	}
}
