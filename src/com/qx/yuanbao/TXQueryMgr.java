package com.qx.yuanbao;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;

import net.sf.json.JSONObject;

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
				task.optime -= 500;
				if(task.optime>0){
					q.put(task);
					if(q.size()==1){
						Thread.sleep(500);
					}
				}else if(task.diff > 0){
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
			b.type = "获得奖励";
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
			b.type = "获得奖励";
		}
		HibernateUtil.insert(b);
	}
	public void doQuery(TXQuery task) {
		if(task.params == null){
			log.error("参数缺失 {}", task);
			return;
		}
		JSONObject ret = JSONObject.fromObject(task.params);
		new YSDK().get_balance_m(ret);
		String ysdkRet = ret.optString("ysdkRet");
		if(ysdkRet == null){
			return;
		}
		JSONObject yo = JSONObject.fromObject(ysdkRet);
		int code = yo.optInt("ret");
		if(code != 0){
			return;
		}
		//{"ret":0,"balance":5065,"gen_balance":5005,"first_save":0,"save_amt":60,"gen_expire":0,"tss_list":[]}
		int save_amt = yo.optInt("save_amt");//累计充值金额的游戏币数量
		int balance = yo.optInt("balance");//balance：游戏币个数（包含了赠送游戏币）
		BillHist b = new BillHist();
		b.jzId = task.jzId;
		b.balance = balance;
		b.gen_balance = yo.optInt("gen_balance");
		b.save_amt = yo.optInt("save_amt");
		b.first_save = yo.optInt("first_save");
		b.dt = task.dt;
		if(save_amt == task.pre_save_amt){
			log.info("无变化 jzId {}",task.jzId);
			b.type = "查询返回：正常";
		}else if(save_amt<task.pre_save_amt){//不应该
			log.warn("不应该 {} < {} jzId {},pre time {}",
					save_amt, task.pre_save_amt, task.jzId, task.dt);
			b.type = "查询返回：我方较小";
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
			log.info("有到账 {} to {} of {}",task.pre_save_amt, 
					task.new_save_amt, task.jzId);
			YuanBaoMgr.inst.diff(jz, diff, 0, 0, YBType.YB_YSDK, "YSDK充值");
			HibernateUtil.update(jz);
		}
	}
}
