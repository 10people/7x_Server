package com.youxigu.app.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.app.cache.data.IBucket;
import com.youxigu.app.cache.data.IFieldValue;
import com.youxigu.app.cache.data.IScope;
import com.youxigu.app.cache.data.UserBucket;
import com.youxigu.app.cache.protocol.Command;
import com.youxigu.app.cache.protocol.FieldCommand;
import com.youxigu.app.cache.protocol.Operation;
import com.youxigu.app.cache.protocol.Reply;


/**
 * 根据修改命令进行数据修改
 * 
 * 1 无条件修改，则只需要进行基础的数据有效性校验
 * 
 * TODO: scope 和 bucket 以后修改为 xml配置的，使用者提供xml配置
 * @author wuliangzhu
 *
 */
public class DataManager {
	private static Logger logger = LoggerFactory.getLogger("cache");
	
	// 用户数据
	private Map<String, IBucket> data = new ConcurrentHashMap<String, IBucket>();
	private Map<String, Class<? extends IBucket>> bucketClass = new HashMap<String, Class<? extends IBucket>>();
	
	public static final String SUCCESS = new Reply(Reply.SUCCESS).toString();
	public static final String FAILED = new Reply(Reply.FAILED).toString();
	public static final String NOTEXIST = new Reply(Reply.NOTEXIST).toString();
	public static final String INVALID_CMD = new Reply(Reply.INVALID_CMD).toString();
	
	/**
	 * 初始化数据类型
	 * 
	 */
	public void init() {
		this.bucketClass.put("user", UserBucket.class);
	}
	
	/**
	 * 对数据进行更新操作，不是设置操作
	 * 
	 * @param command
	 * @return
	 */
	public String execute(Command command){
		switch(command.opType) {
			case Command.OP_CREATE: { // 创建新数据
					logger.debug("create bucket {} -> {}", command.scope, command.uid);
					
					IBucket bucket = this.createBucket(command.scope);
					this.data.put(command.uid, bucket);
					
					if (bucket != null) {
						logger.debug("create bucket success {} -> {}", command.scope, command.uid);
						return SUCCESS;
					}else {
						logger.debug("create bucket failed {} -> {}", command.scope, command.uid);
						return NOTEXIST;
					}
				}
			case Command.OP_BGET:{
					return this.handleBGet(command);
				}
			case Command.OP_SGET:{
					return this.handleFGet(command);
				}
		}
		
		IBucket bucket = this.getBucket(command.uid);
		if (bucket == null) {
			logger.debug("bucket not exist {}", command.uid);
			return NOTEXIST;
		}
		
		IScope scope = bucket.getScope(command.scope);
		if (scope == null) {
			logger.debug("scope not exist {}:{}", command.uid, command.scope);
			return NOTEXIST;
		}
		
		List<Operation> opList = command.opList;
		for (Operation op : opList) {
			IFieldValue value = scope.get(op.key);
			logger.debug("op {} data {} {}", command.opType + "", op.key, op.value);
			
			switch (command.opType) {
				case Command.OP_ADD: {
					value.add(op.value);
					break;
				}
				
				case Command.OP_SET: {
					value.set(op.value);
					break;
				}
				default:{
					return INVALID_CMD;
				}
			}
		}
		
		
		return SUCCESS;
	}
	private String handleBGet(Command command){
		if (command.opType != Command.OP_BGET) {
			return null;
		}
		
		IBucket bucket = this.getBucket(command.uid);
		if (bucket == null) {
			return null;
		}
		
		return bucket.toString();
	}
	
	private String handleSGet(Command command){
		if (command.opType != Command.OP_SGET) {
			return null;
		}
		
		IBucket bucket = this.getBucket(command.uid);
		if (bucket == null) {
			return null;
		}
		IScope scope = bucket.getScope(command.scope);
		if (scope == null) {
			return null;
		}
		
		return scope.toString();
	}
	
	private String handleFGet(Command cmd){
		if (cmd.opType != Command.OP_FGET) {
			return null;
		}
		
		FieldCommand command = (FieldCommand)cmd;
		IBucket bucket = this.getBucket(command.uid);
		if (bucket == null) {
			return null;
		}
		IScope scope = bucket.getScope(command.scope);
		if (scope == null) {
			return null;
		}
		
		
		IFieldValue fv = scope.get(command.field);
		if (fv == null) {
			return null;
		}
		
		return fv.toString();
	}
	
	public String get(String key){
		return null;
	}
	
	/**
	 * 获取数据，然后根据命令进行操作
	 * 
	 * @param key
	 * @return
	 */
	private IBucket getBucket(String key) {
		return this.data.get(key);
	}
	
	private IBucket createBucket(String key) {
		Class<? extends IBucket> clazz = this.bucketClass.get(key);
		if (clazz == null) {
			return null;
		}
		
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			return null;
		}
	} 
}
