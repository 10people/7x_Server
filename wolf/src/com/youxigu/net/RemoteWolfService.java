package com.youxigu.net;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理有返回值的请求，相当于RPC
 *  1 利用ServiceLocator 取得 service指定的 实例；
 *  2 利用反射找到methodName的调用
 * @author wuliangzhu
 *
 */
public class RemoteWolfService implements IWolfService, ISessionListener, IInitListener {
	private static Logger logger = LoggerFactory.getLogger(RemoteWolfService.class);
	private static Logger perf = LoggerFactory.getLogger("perf");
	private Map<String, Method> cache = new ConcurrentHashMap<String, Method>();
	private ResultMgr resultMgr;
	
	public void setResultMgr(ResultMgr resultMgr) {
		this.resultMgr = resultMgr;
	}

	@SuppressWarnings("unchecked")
	public boolean handleMessage(Response response, Object message) {
		if (message instanceof SyncWolfTask) {
			SyncWolfTask task = SyncWolfTask.class.cast(message);
			if (task == null) {
				return true;
			}
			
			// 检测task状态，如果是请求，继续往下走；如果是响应，设置响应状态
			if (task.getState() == SyncWolfTask.RESPONSE) {
				resultMgr.requestCompleted(task.getRequestId(), task.getResult());
				
				return true;
			}
			
			task.setState(SyncWolfTask.RESPONSE);
			
			String serviceName = task.getServiceName();
			String methodName = task.getMethodName();
			Object[] params = task.getParams();
			int paramsLength = 0;
			if(params != null){
				paramsLength = params.length;
			}
			Object instance = com.youxigu.boot.ServiceLocator.get(serviceName, Object.class);
			String callKey = serviceName + "_" + methodName;
			Class<?>[] paramz = null;
			if(paramsLength > 0){
				paramz = new Class[paramsLength];
				callKey += "_";
				for (int i = 0; i < paramsLength; i++) {
					paramz[i] = params[i].getClass();
					callKey += params[i].getClass().getName() + "_";
				}
			}
			Method method = this.cache.get(callKey);
			if (method == null) {
				try {
					if(paramz == null || paramz.length == 0){
						method = instance.getClass().getDeclaredMethod(methodName);
					}else{
						List possibleParamList = searchAllMatch(paramz);
						for (Object param : possibleParamList) {
							try{
								method = instance.getClass().getDeclaredMethod(methodName, (Class[]) param);
							}catch (NoSuchMethodException e) {
								continue;
							}
							break;
						}
					}
				} catch (Exception e) {
					logger.error(e.toString(), e);
				}
				this.cache.put(callKey, method);
			}
			
			try {
				if (method == null) {
					throw new NoSuchMethodException(methodName);
				}
				Object ret = method.invoke(instance, params);
				task.setResult(ret);
			} catch (Exception e) {
				logger.error(e.toString(), e);
				task.setResult(new RuntimeException(e.toString()));
			}
		
			response.write(task);
			
			return true;
		}
		
		return false;
	}

	/**
	 * 找出所有可能的匹配
	 * 比如 ArrayList 则返回 ArryList List
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List searchAllMatch(Class[] clazz) {
		List ret = new LinkedList();
		if (clazz == null || clazz.length == 0) {
			return ret;
		}
		
		int len = clazz.length;
		ClassList[] paramList = new ClassList[clazz.length];
		for (int i = 0; i < len; i++){
			ClassList tmp = paramList[i] = new ClassList();
			Class tmpClass = clazz[i];
			
			tmp.add(tmpClass);
			
			if (tmpClass == Integer.class) {
				tmp.add(int.class);
			}else if (tmpClass == Boolean.class) {
				tmp.add(boolean.class);
			}else if (tmpClass == Long.class) {
				tmp.add(long.class);
			}else if (tmpClass == Float.class){
				tmp.add(float.class);
			}
			
			// 获得所有的父类
			Class superClass = tmpClass.getSuperclass();
			while (superClass != null) {
				tmp.add(superClass);
				
				superClass = superClass.getSuperclass();
			}
			
			// 会的所有的接口
			Class[] allFace = tmpClass.getInterfaces();
			if (allFace != null && allFace.length > 0) {
				for (Class face : allFace) {
					tmp.add(face);
				}
			}
		}
		
		// 得到一个二维矩阵，下面就要把这个矩阵所有可能的情况都要列举出来
		// 先得到一个数组，然后把得到的这个和下一个合起来得到一个新的数组
		for (int i = 0; i < len; i++) {
			if (ret.size() == 0) { // 把第一组结果放入
				int tmplen = paramList[i].length();
				for (int j = 0; j < tmplen; j++) {
					ret.add(new Class[]{paramList[i].get(j)});
				}
			}else { // 如果已经有结果了就要把上次的结果和这次的一组合起来
				ClassList tmpd = paramList[i]; // element is classObj
				List lastResult = ret; // element is array
				ret = new LinkedList();
				for (int m = 0; m < lastResult.size(); m++) {
					Class[] arr = (Class[]) lastResult.get(m);
					for (int n = 0; n < tmpd.length(); n++) {
						Class[] tmparr = new Class[arr.length + 1];
						System.arraycopy(arr, 0, tmparr, 0, arr.length);
						tmparr[tmparr.length - 1] = tmpd.get(n);
						ret.add(tmparr);
					}
				}
			}
		}

		return ret;
	}
	
	private static class ClassList {
		@SuppressWarnings("unchecked")
		private Class[] data = new Class[8];
		private int usedNum = 0;
		@SuppressWarnings("unchecked")
		public void add (Class c) {
			if (usedNum >= data.length) {
				Class[] tmpRet = new Class[data.length + 8];
				System.arraycopy(data, 0, tmpRet, 0, data.length);
				data = null;
				data = tmpRet;
			}
			
			data[usedNum++] = c;
		}
		
		@SuppressWarnings("unchecked")
		public Class get(int i) {
			return data[i];
		}
		
		public int length() {
			return usedNum;
		}
		
		@SuppressWarnings("unchecked")
		public Class[] getData() {
			Class[] ret = new Class[usedNum];
			System.arraycopy(this.data, 0, ret, 0, usedNum);
			
			return ret;
		}
		
		public void reset() {
			this.usedNum = 0;
			this.data = null;
			this.data = new Class[8];
		}
	}

	public void close(Response response) {
		Exception e = new Exception("连接已经关闭");
		// 释放ResultMgr关联的请求
		if (this.resultMgr != null)
			this.resultMgr.notifyAllRequest(e);
	}

	public void open(Response response) {
		// 新建一个ResultMgr		
	}

	public void init(SocketContext context) {
		resultMgr = context.get("resultMgr", ResultMgr.class);
	}
	
	public void shutdown(){
		
	}
}
