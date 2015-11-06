package com.youxigu.app.cache.protocol;

import java.util.List;

/**
 * 进行操作的命令
 * 1 op : add or  update or create
 * 2 scope: the target of op; 如果 op 为 create ,则 scope 为 bucket类型
 * 3 [field value]+ 修改的属性集合
 * 
 * @author wuliangzhu
 *
 */
public class Command {
	public static final int OP_ADD = 1;
	public static final int OP_SET = 2;
	public static final int OP_CREATE = 3;
	
	public static final int OP_BGET = 4;
	public static final int OP_SGET = 5;
	public static final int OP_FGET = 6;
	
	public int opType;
	public String uid; // 要操作的对象
	public String scope;
	public List<Operation> opList;
}
