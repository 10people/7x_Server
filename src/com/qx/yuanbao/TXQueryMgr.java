package com.qx.yuanbao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ChongZhi;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.account.AccountManager;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.task.DailyTaskMgr;
import com.qx.timeworker.FunctionID;
import com.qx.timeworker.FunctionID4Open;
import com.qx.vip.PlayerVipInfo;
import com.qx.vip.VipMgr;
import com.qx.vip.VipRechargeRecord;

import net.sf.json.JSONObject;
import qxmobile.protobuf.VIP.RechargeReq;
import qxmobile.protobuf.VIP.RechargeResp;

public class TXQueryMgr {
	public static TXQueryMgr inst;
	public static Logger log = LoggerFactory.getLogger(TXQueryMgr.class.getSimpleName());
	public LinkedBlockingQueue<TXQuery> q = new LinkedBlockingQueue<>();
	public static TXQuery stop = new TXQuery();
	public TXQueryMgr(){
		inst = this;
	}
	public void start(){
		new Thread(()->check(), "TXQueryMgr").start();
	}
	public void check() {
		while(true){
			try{
				TXQuery task = q.take();
				if(task == stop){
					break;
				}
				if(task.diff > 0){
					doPresent(task);
				}else if(task.diff<0){
					doPay(task);
				}else{
					doQuery(task);
				}
			}catch(Throwable t){
				log.error("exception {}",t);
			}
		}
		log.info("stop");
	}
	public void doPay(TXQuery task) {
		if(task.params == null){
			log.error("参数缺失 {}", task);
			return;
		}
		JSONObject ret = JSONObject.fromObject(task.params);
		ret.put("amt", -task.diff);
		log.info("{}发送支付消息,amt {}",task.jzId,task.diff);
		new YSDK().pay_m(ret);
		String ysdkRet = ret.optString("ysdkRet");
		if(ysdkRet == null){
			return;
		}
		JSONObject yo = JSONObject.fromObject(ysdkRet);
		int code = yo.optInt("ret");
		BillHist b = new BillHist();
		b.jzId = task.jzId;
		b.curYB = task.new_save_amt;//jz.yuanBao;
		b.wantYB = task.diff;//amt;
		b.dt = task.dt;
		if(code != 0){
			b.type = "支付出错";
			log.error("pay fail of {}, {}",task.jzId,ysdkRet);
		}else{
			b.balance = yo.optInt("balance");
			b.type = "支付";
		}
		HibernateUtil.insert(b);
	}
	public void doPresent(TXQuery task) {
		if(task.params == null){
			log.error("参数缺失 {}", task);
			return;
		}
		JSONObject ret = JSONObject.fromObject(task.params);
		ret.put("discountid", ("discountid"));
		ret.put("giftid", ("giftid"));
		ret.put("presenttimes", task.diff);
		new YSDK().present_m(ret);
		String ysdkRet = ret.optString("ysdkRet");
		if(ysdkRet == null){
			return;
		}
		JSONObject yo = JSONObject.fromObject(ysdkRet);
		int code = yo.optInt("ret");
		BillHist b = new BillHist();
		b.jzId = task.jzId;
		b.curYB = task.new_save_amt;//jz.yuanBao;
		b.wantYB = task.diff;//amt;
		b.dt = task.dt;
		if(code != 0){
			b.type = "汇报奖励出错";
			log.error("present fail of {}, {}",task.jzId,ysdkRet);
		}else{
			b.balance = yo.optInt("balance");
			b.type = "Present成功";
		}
		HibernateUtil.insert(b);
	}
	public void doQuery(TXQuery task) {
		if(task.params == null){
			log.error("参数缺失 {}", task);
			return;
		}
		log.info("开始查询{}, times {}",task.jzId, task.retry);
		JSONObject ret = JSONObject.fromObject(task.params);
		new YSDK().get_balance_m(ret);
		String ysdkRet = ret.optString("ysdkRet");
		BillHist b = new BillHist();
		b.jzId = task.jzId;
		b.dt = task.dt;
		if(ysdkRet == null){
			b.type = "查询无返回";
			HibernateUtil.insert(b);
			return;
		}
		JSONObject yo = JSONObject.fromObject(ysdkRet);
		int code = yo.optInt("ret");
		if(code != 0){
			b.type = "查询返回错误:"+ysdkRet;
			HibernateUtil.insert(b);
			return;
		}
		//{"ret":0,"balance":5065,"gen_balance":5005,"first_save":0,"save_amt":60,"gen_expire":0,"tss_list":[]}
		int save_amt = yo.optInt("save_amt");//累计充值金额的游戏币数量
		int balance = yo.optInt("balance");//balance：游戏币个数（包含了赠送游戏币）
		
		b.gen_balance = yo.optInt("gen_balance");
		b.balance = balance;
		b.save_amt = yo.optInt("save_amt");
		b.first_save = yo.optInt("first_save");
		if(save_amt == task.pre_save_amt){
			log.info("无变化 jzId {}",task.jzId);
			b.type = "查询返回：无变化";
			task.retry -= 1;
			if(task.retry>0){
				log.info("需要重试 {},times {}",task.jzId, task.retry);
				new Thread(()->delayAddTask(task)).start();
			}
		}else if(save_amt<task.pre_save_amt){//不应该
			log.warn("不应该 {} < {} jzId {},pre time {}",
					save_amt, task.pre_save_amt, task.jzId, task.dt);
			b.type = "查询返回：我方较大";
		}else{//大于，说明有到账
			log.info("新到账 {}, 之前 {}, jzId {} , pre time {}",
					save_amt, task.pre_save_amt, task.jzId, task.dt);
			task.new_save_amt = save_amt;
			task.balance = balance;
			//
			b.type = "充值到账";
			//
			updateYB(task);
		}
		HibernateUtil.insert(b);
	}
	public void delayAddTask(TXQuery task) {
		try {
			Thread.sleep(1000*15);
		} catch (Exception e) {
			//e.printStackTrace();
		}	
		log.info("重试，放入队列 {},times {}",task.jzId, task.retry);
		q.add(task);
	}
	public void updateYB(TXQuery task) {
		JunZhu jz = HibernateUtil.find(JunZhu.class, task.jzId);
		if(jz == null){
			return;
		}
		int diff = task.new_save_amt - task.pre_save_amt;
		int ourNewB = jz.yuanBao + diff;
		if(ourNewB == task.balance){
			log.info("前后一致 {}", task.jzId);
		}else if(ourNewB > task.balance){
			//可能有赠送（比如领奖里）还未发给腾讯
			log.warn("我方大{}>{} of {}",jz.yuanBao,task.balance, task.jzId);
		}else{//我方的小
			//可能有扣款（消费）未发给腾讯
			log.warn("我方小{}<{} of {}",jz.yuanBao,task.balance, task.jzId);
		}
		if(diff>0){
			log.info("有到账 from  {} to {} of {}, diff {}",task.pre_save_amt, 
					task.new_save_amt, task.jzId, diff);
			YuanBaoMgr.inst.diff(jz, diff, 0, 0, YBType.YB_YSDK, "YSDK充值");
			HibernateUtil.update(jz);
			//========================================
			log.info("处理 {} 购买 {}",jz.id, task.buyItemId);
			//

			IoSession session = AccountManager.sessionMap.get(jz.id);
			if(session == null){
				session = new DummySession(){
					public WriteFuture write(Object message) {
						log.info("{}收到返回{}",jz.id,message);
						return null;
					};
				};
				session.setAttribute(SessionAttKey.junZhuId, jz.id);
			}
			//
			if(task.buyItemId == -20160525){//登录后例行查询
				//这种情况有到账，说明是以前的到账
				checkPreBill(session,task,diff, jz);
				return;
			}
			ChongZhi conf = VipMgr.chongZhiTemp.get(task.buyItemId);
			if(conf == null){
				log.error("{}购买{}没有找到配置",jz.id,task.buyItemId);
				return;
			}
			deal(session, conf, jz,task);
		}
	}
	public void deal(IoSession session, ChongZhi conf, JunZhu jz, TXQuery task) {
		//如果充值时点的是月卡、周卡、终身卡，则走以前的购买（充值）接口，会给富利卡，并扣元宝。
		//否则就是充值元宝，到账了就结束。 
		if (conf.id == VipMgr.yuekaid) {
		}else if(conf.id == VipMgr.zhongShenKa){
		}else if(conf.id == VipMgr.yuekaid){
		}else{
			boolean first = false;
			if(conf.extraFirst>0){
				log.info("检查首充赠送{}, item {}",jz.id,conf.id);
				int buyCnt = VipMgr.INSTANCE.getBuyCount(conf.id, jz.id);
				if(buyCnt == 0){
					first = true;
					log.info("{}首充赠送{},购买的是{}:{}",jz.id,conf.extraFirst,conf.id,conf.name);
					YuanBaoMgr.inst.diff(jz, conf.extraFirst, 0, 0, YBType.YB_YSDK, "道具首充赠送"+conf.name);
					HibernateUtil.update(jz);
				}
			}
			if(conf.extraYuanbao>0 && first == false){
				//赠送
				log.info("{}充值赠送 {}", jz.id, conf.extraYuanbao);
				YuanBaoMgr.inst.diff(jz, conf.extraYuanbao, 0, 0, YBType.YB_YSDK, "YSDK充值赠送");
				HibernateUtil.update(jz);
			}
		}
		log.info("给客户端{}发送到账消息",jz.id);
		/*
		RechargeReq.Builder req = RechargeReq.newBuilder();
		req.setAmount(0);
		req.setType(conf.id);
		//走扣元宝购买各种卡的过程，和VIP计算
		VipMgr.INSTANCE.recharge(0, session, req);
		*/
		//走元宝/各种卡的过程，和VIP计算 元宝已经在上面加过了。
		VipMgr.INSTANCE.recharge(session,jz,conf,false);
		session.write(PD.CHARGE_OK);
	}
	public void checkPreBill(IoSession session, TXQuery task, int diff, JunZhu jz) {
		List<BillHist> list = HibernateUtil.list(BillHist.class, 
				"where jzId = "+task.jzId+" and type='发起充值' "
						+ "order by id desc", 0, 50);
		if(list.size()==0){
			log.info("{}之前发起充值为0次，不补单",task.jzId);
			return;
		}
		ChongZhi preOne = null;
		for(BillHist b : list){
			int buyId = b.buyItemId;
			ChongZhi conf = VipMgr.chongZhiTemp.get(buyId);
			if(conf == null){
				log.info("{}补单失败，商品ID {}不存在",task.jzId,buyId);
				return;
			}
			if(conf.addNum == diff){
				//价格相等，则就是购买这个
				preOne = conf;
				break;
			}
		}
		if(preOne != null){
			log.info("找到{}的前一单{},{}，补单",
					task.jzId,preOne.id,preOne.name);
			deal(session,preOne,jz,task);
			log.info("补单{}完毕",task.jzId);
			return;
		}
		//没有找到一单价格相等，看看能不能凑单
		int sum = 0;
		List<BillHist> bs = new ArrayList<>(5);
		for(BillHist b : list){
			int buyId = b.buyItemId;
			ChongZhi conf = VipMgr.chongZhiTemp.get(buyId);
			if(conf == null){
				continue;
			}
			sum+=conf.addNum;
			bs.add(b);
			if(sum >= diff){
				break;
			}
		}
		if(sum!=diff){
			log.info("{} 计算多个订单结果不匹配，不补单",task.jzId);
			return;
		}
		log.info("{} 多个订单补单",task.jzId);
		for(BillHist b : bs){
			log.info("{} 多个订单补单 dbId:{}, itemId:{}",
					task.jzId, b.id, b.buyItemId);
			deal(session,preOne,jz,task);
		}
		log.info("{} 多个订单补单,完毕",task.jzId);
	}
}
