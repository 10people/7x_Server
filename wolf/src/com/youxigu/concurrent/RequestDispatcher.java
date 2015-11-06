package com.youxigu.concurrent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这里按操作对象进行数据分组，对于同一组的数据会让指定线程来执行
 * 所有的请求都有一个destinationId,对于对于进入线程池的所有请求都有一个标示，进入这个标示++，如果执行完毕--
 * 如果这个标示为0，才能让这个请求进入执行队列，否则不能进入执行队列
 * 使用分2种情况：
 * 1 进程内使用，直接调用dispatch就可以了，然后得到返回结果；
 * 2 跨进程使用，那就需要配置RequestDispatcherService
 * @author wuliangzhu
 *
 */
public class RequestDispatcher {
	private static Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);
	
	public static final int DEFAULT_WORKER_SIZE = 32;
	public static final int MAX_BUF_SIZE = 1000;
	
	private ExecutorService worker; // 执行线程
	private ConcurrentMap<Integer, AtomicInteger> requestFilter = new ConcurrentHashMap<Integer, AtomicInteger>();
	private Map<Integer, Class<? extends Handler>> cmdHandlerMap = new HashMap<Integer, Class<? extends Handler>>();
	private BlockingQueue<Request> reqQueue = new LinkedBlockingQueue<Request>();
	
	private Thread deamon;
	private AtomicInteger requestHandling = new AtomicInteger(); // 统计正在处理的请求数量
	@SuppressWarnings("unchecked")
	private Callable empty = new NullCallable();
	
	private static RequestDispatcher instance;
	
	private RequestDispatcher(){}
	
	public static RequestDispatcher getInstance() {
		if (instance == null) {
			instance = new RequestDispatcher();
			instance.init();
		}
		
		return instance;
	}
	
	public void init() {
		this.worker = Executors.newFixedThreadPool(DEFAULT_WORKER_SIZE);
		
		this.deamon = new Thread(){
			
			/**
			 * 利用双buf机制来完成数据交替：
			 * 1 获取reqQueue数据，添加到buf中；
			 * 2 处理buf中的request，如果前面有队列，则添加到buf2中；
			 * 3 当前的buf修改为buf2， buf2 改为吧uf
			 * 4 回到1中处理
			 */
			public void run(){
				Request request = null;
				LinkedList<Request> buf = new LinkedList<Request>();
				LinkedList<Request> buf2 = new LinkedList<Request>();
				LinkedList<Request> tmp = null;
				try {
					while(true) {						
						if (buf.size() == 0){
							getReceivedRequestByTake(buf, MAX_BUF_SIZE);
						}
						else {
							getReceivedRequestByPoll(buf, MAX_BUF_SIZE);
						}
						
						
						while((request = buf.poll()) != null) {	// buf中获取数据进行处理						
							// 判断是否有标示了
							if (request.counter == null) {
								request.counter = RequestDispatcher.this.getRequetFilter(request.getDestinationId());
							}
							
							if (request.counter.get() == 0) {
								logger.info("submit request destinationId:{} -> cmd:{} workingNum:{}", request.getDestinationId(), request.getCommandType(), requestHandling.get());
								RequestDispatcher.this.sumbit(request);
							}else {
								logger.info("readd request destinationId:{} -> cmd:{}", request.getDestinationId(), request.getCommandType());
								buf2.add(request); // 如果不为0，需要重新加入队列
							}
						}
						
						// 数据处理完要进行buf交换,让没有处理的数据还是在数据队列前面
						tmp = buf;
						buf = buf2;
						buf2 = tmp;
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		this.deamon.setName("requestDispatcher");
		this.deamon.setDaemon(true);
		this.deamon.start();
	}
	
	/**
	 * 从 接收队列中获取数据到buf中，设置最大设置长度，避免死循环在里面
	 * 如果buf不为空，则如果外界没有数据，就要继续处理buf中数据
	 * 
	 * @return
	 */
	private LinkedList<Request> getReceivedRequestByPoll(LinkedList<Request> buf, int maxCount) {
		Request req =  null;
		try {
			int counter = 0;
			while((req = this.reqQueue.poll(16L, TimeUnit.MILLISECONDS)) != null) {
				buf.add(req);
				
				counter++;
				if (counter >= maxCount) {
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return buf;
	}
	
	/**
	 * 如果buf中为空的，则数据源只能从外界获取，所以等到有数据再处理
	 * 
	 * @param buf
	 * @param maxCount
	 * @return
	 */
	private LinkedList<Request> getReceivedRequestByTake(LinkedList<Request> buf, int maxCount) {
		Request req =  null;
		try {
			int counter = 0;
			req = this.reqQueue.take();
			buf.add(req);
			
			counter++;
			if (counter >= maxCount) {
				return buf;
			}
			
			while((req = this.reqQueue.poll(16L, TimeUnit.MILLISECONDS)) != null) {
				buf.add(req);
				
				counter++;
				if (counter >= maxCount) {
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return buf;
	}
	
	/**
	 * 请求处理结束
	 * 
	 * @param req
	 */
	void requestFinish(Request req){
		this.requestHandling.decrementAndGet();
		logger.info("request finished destinationId:{} -> cmd:{}, workingNum:{}", req.getDestinationId(), req.getCommandType(), requestHandling.get());
	}
	
	@SuppressWarnings("unchecked")
	public Future<Object> dispatch(Request req) {
		logger.info("dispatch request destinationId:{} -> cmd:{}", req.getDestinationId(), req.getCommandType());
		req.result = new FutureRequest<Object>(empty);
		
		boolean f = this.reqQueue.offer(req);
		if (!f) {
			req.result.setException(new RuntimeException("add req Queue failed!"));
		}
		
		return req.result;
	}
	
	public void stop() {
		this.worker.shutdown();
	}
	
	/**
	 * 这个方法会被单线程调用, 当counter为0的时候调用
	 * 
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	private void sumbit(Request request) {
		AtomicInteger flag = request.counter;
		flag.incrementAndGet(); // 标示添加的唯一地方
		Handler handler = this.getHandler(request.getCommandType());
		handler.command = request;
		request.counter = flag;
		
		this.worker.submit(handler);
		this.requestHandling.incrementAndGet();
	}

	private AtomicInteger getRequetFilter(int destinationId) {
		AtomicInteger flag = this.requestFilter.get(destinationId);
		
		if (flag == null) {
			AtomicInteger tmp = new AtomicInteger();
			flag = this.requestFilter.putIfAbsent(destinationId, tmp);
			if (flag == null) { // 标示先前没有值，那直接用这个值
				flag = tmp;
			}
		}
		
		return flag;
	}
	
	public void registerHandler(int commandType, Class<? extends Handler> handlerClazz) {
		this.cmdHandlerMap.put(commandType, handlerClazz);
	}
	
	/**
	 * 获取命令对应的处理器
	 * 
	 * @param commandType
	 * @return
	 */
	private Handler getHandler(int commandType){
		Class<? extends Handler> handlerClazz = this.cmdHandlerMap.get(commandType);
		if (handlerClazz == null) {
			return null;
		}
		
		try {
			Handler handler = handlerClazz.newInstance();
			
			return handler;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	class NullCallable implements Callable{

		@Override
		public Object call() throws Exception {
			return null;
		}
		
	}
}
