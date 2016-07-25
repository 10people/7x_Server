package com.qx.persistent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DBSaver {
	public static ThreadPoolExecutor[] es;
	public static DBSaver inst = new DBSaver();
	
	public DBSaver(){
		es = new ThreadPoolExecutor[20];
		int len = es.length;
		for(int i=0;i<len; i++){
			es[i] = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
		}
	}
	
	public void shutdown(){
		int len = es.length;
		for(int i=0;i<len; i++){
			es[i].shutdown();
		}
	}
	
	public void update(DBHash b){
		int v = hash(b);
		es[v].submit(()->
			HibernateUtil.updateMCDB(b)
		);
	}

	public int hash(DBHash b) {
		long hash = b.hash();
		hash/=1000;
		long grid = hash % es.length;
		int v = (int) grid;
		return v;
	}

	public void save(DBHash b) {
		int v = hash(b);
		es[v].submit(()->
			HibernateUtil.saveMCDB(b)
		);
	}

	public void insert(DBHash b) {
		int v = hash(b);
		es[v].submit(()->
			HibernateUtil.insertDB(b)
		);		
	}

	public void delete(DBHash b) {
		int v = hash(b);
		es[v].submit(()->
			HibernateUtil.deleteMCDB(b)
		);		
	}
}
