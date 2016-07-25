package log.parser;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.qx.persistent.HibernateUtil;
import com.qx.util.DelayedSQLMgr;

import log.ReasonBean;

public class ReasonMgr {
	public static ReasonMgr inst;
	public ConcurrentHashMap<String, ReasonBean> map = new ConcurrentHashMap<String, ReasonBean>();
	public ReasonMgr(){
		inst = this;
		loadFromDB();
	}
	public void loadFromDB() {
		List<ReasonBean> list = HibernateUtil.list(ReasonBean.class, "");
		for(ReasonBean r : list){
			map.put(r.reason, r);
		}
	}
	public synchronized int getId(String reason){
		if(reason == null){
			return -1;
		}
		ReasonBean bean = map.get(reason);
		if(bean != null){
			return bean.id;
		}
		bean = new ReasonBean();
		bean.reason = reason;
		ReasonBean ref = bean;
		DelayedSQLMgr.es.submit(()->
			HibernateUtil.insert(ref)
		);
		map.put(reason, bean);
		return bean.id;
	}
}
