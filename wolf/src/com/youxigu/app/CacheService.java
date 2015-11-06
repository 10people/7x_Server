package com.youxigu.app;

import java.util.ArrayList;

import com.youxigu.app.cache.DataManager;
import com.youxigu.app.cache.protocol.Command;
import com.youxigu.app.cache.protocol.FieldCommand;
import com.youxigu.app.cache.protocol.Operation;
import com.youxigu.net.IInitListener;
import com.youxigu.net.IWolfService;
import com.youxigu.net.Response;
import com.youxigu.net.SocketContext;

/**
 * 1 bget 获取uid 指定的 bucket数据 scopeName k v k v | scopeName k v k v
 * 2 sget 获取uid 指定的 bucket.scope 数据 k v k  v
 * 3 fget 获取uid 指定的 bucket.scope.field数据 v 
 * 
 * 1 使用数据bucket之前，需要 先create,否则不能使用 create bucketClass,初始化数据建议在默认值完成
 * 2 数据的更新操作有 add 和 set 2种， 不建议使用set，set 最后只出现在字符串的修改中 
 * 	a 单个：cmd uid bucket scope field value
 *  b 多个: cmd uid bucket scope field value field value
 * 
 * @author wuliangzhu
 *
 */
public class CacheService implements IWolfService, IInitListener {
	private DataManager dataMgr;
	@Override
	public boolean handleMessage(Response response, Object message) {
		if (message instanceof String) {
			Command cmd = this.parseCommand(message.toString());
			String ret = dataMgr.execute(cmd);
			
			response.write(ret);
			
			return true;
		}
		
		return false;
	}
	@Override
	public void init(SocketContext context) {
		dataMgr = new DataManager();	
		dataMgr.init();
	}

	public void shutdown(){
		
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	private Command parseCommand(String message){
		String[] args = message.split(" ");
		int len = args.length;
		if (len < 2) { // 参数格式不对，至少2个 bget uid
			return null;
		}
		
		String cmd = args[0];
		if ("bget".equals(cmd)) {
			return this.parseBget(args);
		}else if ("sget".equals(cmd)) {
			return this.parseSget(args);
		}else if ("fget".equals(cmd)) {
			return this.parseFget(args);
		}else if ("add".equals(cmd) || "set".equals(cmd)) {
			if (len < 5 || (len - 5) % 2 != 0) {
				return null;
			}
			Command ret = new Command();
			ret.opType = "add".equals(cmd) ? Command.OP_ADD : Command.OP_SET;
			ret.uid = args[1];
			ret.scope = args[2];
			ret.opList = new ArrayList<Operation>();
			Operation op = null;
			for (int i = 3; i < len;) {
				op = new Operation(args[i++], args[i++]);
				
				ret.opList.add(op);
			}
			
			return ret;
		}else {
			return null;
		}
	}
	
	/**
	 * 生成bget命令
	 * @param args
	 * @return
	 */
	private Command parseBget(String[] args){
		if (args.length != 2) {
			return null;
		}
		
		Command ret = new Command();
		ret.opType = Command.OP_BGET;
		ret.uid = args[1];
		
		return ret;
	}
	
	private Command parseSget(String[] args){
		if (args.length != 3) {
			return null;
		}
		
		Command ret = new Command();
		ret.opType = Command.OP_BGET;
		ret.uid = args[1];
		ret.scope = args[2];
		
		return ret;
	}
	
	private Command parseFget(String[] args){
		if (args.length != 4) {
			return null;
		}
		
		FieldCommand ret = new FieldCommand();
		ret.opType = Command.OP_BGET;
		ret.uid = args[1];
		ret.scope = args[2];
		ret.field = args[3];
		
		return ret;
	}
}
