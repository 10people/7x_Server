package com.qx.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.task.GameTaskMgr;
import com.qx.util.DelayedSQLMgr;
import com.qx.util.TPE;

/**
 * 服务器内部事件处理器；用于断开模块之间耦合。
 * 比如登录后要给好友发上线通知，则登录完成后触发一个登录事件，
 * 关心这个事件的模块，去处理自己的业务；
 * 避免在登录完成后直接去调用其他模块的接口。
 * @author 康建虎
 *
 */
public class EventMgr{
	public static ThreadPoolExecutor[] es;
	public static Set<Integer> taskCareIds = new HashSet<>();
	public static Logger log = LoggerFactory.getLogger(EventMgr.class.getSimpleName());
	public static Map<Integer,List<EventProc>> procs = new HashMap<Integer, List<EventProc>>();;
	public static EventMgr inst;
	public EventMgr(){
		inst = this;
		int len = 50;
		es = new ThreadPoolExecutor[len];
		for(int i=0;i<len;i++){
			es[i] = new TPE(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
		}
	}
	public static void shutdown(){
		for(ThreadPoolExecutor s : es){
			s.shutdown();
		}
	}
	public static void submit(long jzId, Runnable r){
		long hash = jzId/1000;
		hash = hash % es.length;
		int grid = (int) hash;
		es[grid].submit(r);
	}
	public static void addEvent(long jzId, int id, Object param){
		Event evt = new Event();
		evt.id = id;
		evt.param = param;
		if(taskCareIds.contains(id)){
			GameTaskMgr.inst.fireEvent(jzId,evt);
		}
		//有些事件不止是任务关心，所以任然要添加。
		submit(jzId,()->inst.handle(evt));
	}
	public void handle(Event evt) {
		List<EventProc> list = procs.get(evt.id);
		if(list == null){
			log.info("该事件id：{},没有事件注册过", evt.id);
			return;
		}
		int cnt = list.size();
		long start = System.currentTimeMillis();
		for(int i=0; i<cnt; i++){
			EventProc proc = list.get(i);
			if(proc.disable){
				continue;
			}
			try{
				proc.proc(evt);
			}catch(Throwable t){
				log.error("处理事件异常 {} {} proc {}",evt.id, evt.param, proc.getClass().getSimpleName());
				log.error("异常内容", t);
			}
		}
		long end = System.currentTimeMillis();
		long diff = end-start;
		if(diff>200){
			DelayedSQLMgr.inst.slowAct(evt.id, -diff);
		}
	}
	public static void regist(int id, EventProc proc) {
		List<EventProc> list = procs.get(id);
		if(list == null){
			list = new ArrayList<EventProc>(1);
			procs.put(id, list);
		}
		if(proc instanceof GameTaskMgr){
			taskCareIds.add(id);
		}else{
			list.add(proc);
		}
	}
	public static void sendEventFinishMessage(){
		
	}
}
