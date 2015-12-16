package com.manu.dynasty.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.util.ProtobufUtils;

public class Redis {
	private static Redis instance;
	public static Logger log = LoggerFactory.getLogger(Redis.class);

	private RedisPubSubListener redisListener;

	public static Redis getInstance() {
		if (instance == null) {
			instance = new Redis();
			instance.init();
		}

		return instance;
	}

	private JedisPool pool;

	public String host;

	public int port;

	/**
	 * 测试用,别外调
	 * 
	 * @return
	 */
	public Jedis getRedis() {
		return this.pool.getResource();
	}

	private void init() {
		String redisServer = GameServer.cfg.get("redisServer");
		if (redisServer == null) {
			redisServer = "localhost:6379";
		}
		redisServer = redisServer.trim();

		String[] tmp = redisServer.split(":");
		host = tmp[0];
		port = 6379;
		if (tmp.length == 2) {
			port = Integer.parseInt(tmp[1].trim());
		}
		log.info("Redis at {}:{}", host, port);
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(500);
		config.setTestWhileIdle(true);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		config.setTimeBetweenEvictionRunsMillis(30000);
		config.setNumTestsPerEvictionRun(1);
		config.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
		//表示当borrow(引入)一个jedis实例时，最大的等待时间
		//如果超过等待时间，则直接抛出JedisConnectionException；
		config.setMaxWait(5000);
		pool = new JedisPool(config,host, port);

		this.redisListener = new RedisPubSubListener();
	}

	public void test() {
		Jedis j = pool.getResource();
		pool.returnResource(j);
	}

	/**
	 * Map 的存放和获取
	 */
	public void add(String group, Map<String, String> values) {
		if (values == null || values.size() == 0) {
			return;
		}

		Jedis redis = this.pool.getResource();
		redis.hmset(group, values);
		this.pool.returnResource(redis);
	}

	public void add(String group, String key, String value) {
		if (value == null || key == null) {
			return;
		}

		Jedis redis = this.pool.getResource();
		redis.hset(group, key, value);
		this.pool.returnResource(redis);
	}

	public void set(String key, String value) {
		if (value == null || key == null) {
			return;
		}

		Jedis redis = this.pool.getResource();
		redis.set(key, value);
		this.pool.returnResource(redis);
	}

	public String get(String group, String key) {
		if (key == null) {
			return null;
		}

		Jedis redis = this.pool.getResource();
		String ret = redis.hget(group, key);

		this.pool.returnResource(redis);

		return ret;
	}

	public void hset(String key, String field, String value) {
		if (value == null || key == null || field == null) {
			return;
		}
		Jedis redis = this.pool.getResource();
		redis.hset(key, field, value);
		this.pool.returnResource(redis);
	}
	
	public String hget(String key, String field) {
		if (key == null || field == null) {
			return "";
		}
		Jedis redis = this.pool.getResource();
		String value = redis.hget(key, field);
		this.pool.returnResource(redis);
		return value;
	}
	
	public void hdel(String key, String field) {
		if (key == null || field == null) {
			return;
		}
		Jedis redis = this.pool.getResource();
		redis.hdel(key, field);
		this.pool.returnResource(redis);
	}
	
	
	/**
	 * Map 的存放和获取
	 */
	public void add(String group, String key, MessageLite.Builder builder) {
		if (builder == null) {
			return;
		}

		Jedis redis = this.pool.getResource();
		byte[] data = ProtobufUtils.toByteArray(builder.build());
		redis.hset(group.getBytes(), key.getBytes(), data);

		this.pool.returnResource(redis);
	}

	public MessageLite.Builder getByBytes(String group, String key) {
		Jedis redis = this.pool.getResource();

		byte[] ret = redis.hget(group.getBytes(), key.getBytes());

		this.pool.returnResource(redis);

		if (ret == null) {
			return null;
		}

		return ProtobufUtils.parseFrom(ret.length, ret);
	}

	public Map<String, MessageLite.Builder> getBytesMap(String group) {
		Jedis redis = this.pool.getResource();
		Map<byte[], byte[]> ret = redis.hgetAll(group.getBytes());
		this.pool.returnResource(redis);
		Map<String, MessageLite.Builder> builderMap = new HashMap<String, MessageLite.Builder>();
		Iterator<byte[]> itr = ret.keySet().iterator();
		while (itr.hasNext()) {
			String keyStr = new String(itr.next());
			byte[] data = ret.get(itr.next());
			MessageLite.Builder builder = ProtobufUtils.parseFrom(data.length,
					data);
			builderMap.put(keyStr, builder);
		}
		return builderMap;
	}

	public Long hDelString(String group, String... keys) {
		Jedis redis = this.pool.getResource();
		Long cnt = redis.hdel(group, keys);
		this.pool.returnResource(redis);
		return cnt;
	}

	public Long hDelBuilder(String group, String... keys) {
		Jedis redis = this.pool.getResource();
		byte[][] fields = new byte[keys.length][];
		for (int i = 0; i < keys.length; i++) {
			fields[i] = keys[i].getBytes();
		}
		Long cnt = redis.hdel(group.getBytes(), fields);
		this.pool.returnResource(redis);
		return cnt;
	}

	public Map<String, String> getMap(String group) {
		Jedis redis = this.pool.getResource();
		Map<String, String> ret = redis.hgetAll(group);
		this.pool.returnResource(redis);

		return ret;
	}

	public String get(String key) {
		Jedis redis = this.pool.getResource();
		String ret = redis.get(key);
		this.pool.returnResource(redis);
		return ret;
	}

	/**
	 * 添加元素到集合中
	 * 
	 * @param key
	 * @param element
	 */
	public boolean sadd(String key, String... element) {
		if (element == null || element.length == 0) {
			return false;
		}

		Jedis redis = this.pool.getResource();

		boolean success = redis.sadd(key, element) == 1;

		this.pool.returnResource(redis);

		return success;
	}
	
	public boolean smove(String oldKey, String newKey, String element) {
		if (element == null) {
			return false;
		}
		Jedis redis = this.pool.getResource();
		boolean success = (redis.smove(oldKey, newKey, element) == 1);
		this.pool.returnResource(redis);
		return success;
	}

	/**
	 * 删除指定set内的元素
	 * */
	public boolean sremove(String key, String... element) {
		if (element == null) {
			return false;
		}
		Jedis redis = this.pool.getResource();
		boolean success = (redis.srem(key, element) == 1);
		this.pool.returnResource(redis);
		return success;
	}

	public Set<String> sget(String key) {
		Jedis redis = this.pool.getResource();

		Set<String> m = redis.smembers(key);

		this.pool.returnResource(redis);

		return m;
	}

	/**
	 * 返回set的的元素个数
	 * 
	 * @Title: zcard_
	 * @Description:
	 * @param key
	 * @return
	 */
	public long scard_(String key) {
		Jedis redis = this.pool.getResource();
		long size = redis.scard(key);
		this.pool.returnResource(redis);
		return size;
	}
	
	public void laddList(String key, String... elements) {
		if (elements == null || elements.length == 0) {
			return;
		}
		Jedis redis = this.pool.getResource();

		redis.lpush(key, elements);

		this.pool.returnResource(redis);
	}

	/**
	 * add by wangZhuan
	 * 
	 * @Title: lpush_
	 * @Description:
	 * @param key
	 * @param id
	 */
	public void lpush_(String key, String id) {
		Jedis redis = this.pool.getResource();
		redis.lpush(key, id);
		this.pool.returnResource(redis);
	}
	
	public long llen(String key) {
		Jedis redis = this.pool.getResource();
		long len = redis.llen(key);
		this.pool.returnResource(redis);
		return len;
	}

	public void rpush_(String key, String id) {
		Jedis redis = this.pool.getResource();
		redis.rpush(key, id);
		this.pool.returnResource(redis);
	}
	
	public void rpop(String key) {
		Jedis redis = this.pool.getResource();
		redis.rpop(key);
		this.pool.returnResource(redis);
	}
	/**
	 * 存储押镖仇人
	 */
	public Long rpush4YaBiao(String key, String id) {
		Jedis redis = this.pool.getResource();
		Long rest = redis.rpush(key, id);
		this.pool.returnResource(redis);
		return rest;
	}
	/**
	 * 存储劫镖历史
	 * @param byte[] key reids键名
	 * @param byte[] value 键值
	 */
	public Long rpush4JieBiao(byte[] key, byte[] value) {
		Jedis redis = this.pool.getResource();
		Long rest = redis.rpush(key,value);
		this.pool.returnResource(redis);
		return rest;
	}

	
	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public List<String> lrange4String(String key, int start, int end) {
		Jedis redis = this.pool.getResource();
		List<String> list = redis.lrange(key, start, end);
		this.pool.returnResource(redis);
		return list;
	}

	/**
	 * 获取 MessageLite List
	 * 
	 * @param key
	 * @return
	 */
	public List<MessageLite.Builder> lgetBuilderList(String key) {
		Jedis redis = this.pool.getResource();

		long len = redis.llen(key.getBytes());
		List<byte[]> ret = redis.lrange(key.getBytes(), 0, (int) (len - 1));
		List<MessageLite.Builder> bList = new ArrayList<MessageLite.Builder>();
		this.pool.returnResource(redis);
		for (int i = 0; i < ret.size(); i++) {
			byte[] data = ret.get(i);
			MessageLite.Builder builder = ProtobufUtils.parseFrom(data.length,
					data);
			bList.add(builder);
		}
		return bList;
	}

	/**
	 * 添加 MessageLite List
	 * 
	 * @param key
	 * @param elements
	 */
	public void laddBuilderList(String key, MessageLite.Builder... elements) {
		if (elements == null || elements.length == 0) {
			return;
		}
		Jedis redis = this.pool.getResource();

		byte[][] datas = new byte[elements.length][];

		for (int i = 0; i < elements.length; i++) {
			MessageLite.Builder element = elements[i];
			byte[] data = ProtobufUtils.toByteArray(element.build());
			datas[i] = data;
		}

		redis.lpush(key.getBytes(), datas);

		this.pool.returnResource(redis);
	}

	public List<String> lgetList(String key) {
		Jedis redis = this.pool.getResource();

		long len = redis.llen(key);
		List<String> ret = redis.lrange(key, 0, len - 1);

		this.pool.returnResource(redis);

		return ret;
	}

	/**
	 * 列表list中是否包含value
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean lexist(String key, String value) {
		List<String> list = lgetList(key);
		return list.contains(value);
	}

	public List<String> lgetList(String key, long len) {
		Jedis redis = this.pool.getResource();

		long max = redis.llen(key);

		long l = max > len ? len : max;

		List<String> ret = redis.lrange(key, 0, l - 1);

		this.pool.returnResource(redis);

		return ret;
	}

	public Long del(String key) {
		Jedis redis = this.pool.getResource();

		Long cnt = redis.del(key);

		this.pool.returnResource(redis);
		return cnt;
	}

	/**
	 * 模糊删除
	 * 
	 * @param key
	 * @return
	 */
	public Long delKeyLikes(String key) {
		Jedis redis = this.pool.getResource();

		Set<String> keys = redis.keys(key);

		Long cnt = redis.del(keys.toArray(new String[keys.size()]));

		this.pool.returnResource(redis);

		return cnt;
	}

	/**
	 * 测试元素是否存在
	 * 
	 * @param key
	 * @param element
	 * @return
	 */
	public boolean sexist(String key, String element) {
		Jedis redis = this.pool.getResource();

		boolean ret = redis.sismember(key, element);

		this.pool.returnResource(redis);

		return ret;
	}

	/**
	 * 判断某一个key值得存储结构是否存在
	 * 
	 * @Title: exist_
	 * @Description:
	 * @param key
	 * @return
	 */
	public boolean exist_(String key) {
		Jedis redis = this.pool.getResource();
		boolean yes = redis.exists(key);
		this.pool.returnResource(redis);
		return yes;
	}
	
	
	// TODO ,需要测试返回结果。
	public String rename(String oldkey, String newkey) {
		Jedis redis = this.pool.getResource();
		String ok = "error";
		try {
			ok = redis.rename(oldkey, newkey);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			this.pool.returnResource(redis);
		}
		return ok;
	}

	/**********************************************************************
	 * 排行用到的SortedSet 2015-7-17 改score类型int为double
	 **********************************************************************/
	public long zadd(String key, double score, String member) {
		Jedis redis = this.pool.getResource();
		long ret = 0;
		try {
			ret = redis.zadd(key, score, member);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			this.pool.returnResource(redis);
		}
		return ret;
	}

	/**
	 * 添加 分数，并返回修改后的值
	 * 
	 * @param key
	 * @param update
	 * @param member
	 * @return
	 */
	public double zincrby(String key, int update, String member) {
		Jedis redis = this.pool.getResource();

		double ret = redis.zincrby(key, update, member);

		this.pool.returnResource(redis);

		return ret;
	}

	/**
	 * 返回有序集 key 中，成员 member 的 score 值,存在返回score，不存在返回-1
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public double zscore(String key, String member) {
		Jedis redis = this.pool.getResource();

		Double ret = redis.zscore(key, member);

		this.pool.returnResource(redis);

		if (ret == null) {
			return -1;
		}

		return ret;
	}

	/**
	 * 按照score的值从小到大排序，返回member的排名 排序是从0开始
	 * 
	 * @Title: zrevrank
	 * @Description:
	 * @param key
	 * @param member
	 * @return 
	 * 设置为名次从1开始。返回为-1，表示member无记录
	 */
	public long zrevrank(String key, String member) {
		Jedis redis = this.pool.getResource();
		long ret = -1;
		Long vv = redis.zrevrank(key, member);
		if(vv != null){
			ret = vv.longValue();
		}
		this.pool.returnResource(redis);
		if(ret != -1){
			ret += 1; 
		}
		return ret;
	}

	/**
	 * 按照score的值从小到大排序，返回member的排名 排序是从0开始
	 * 
	 * @Title: zrank
	 * @Description:
	 * @param key
	 * @param member
	 * @return 
	 * 设置为名次从1开始。返回为-1，表示member无记录
	 */
	public long zrank(String key, String member) {
		Jedis redis = this.pool.getResource();
		long ret = -1;
		Long vv = redis.zrank(key, member);
		if(vv != null){
			ret = vv.longValue();
		}
		this.pool.returnResource(redis);
		if(ret != -1){
			ret += 1; 
		}
		return ret;
	}

	/**
	 * 返回的是score的值
	 * @Title: zscore_ 
	 * @Description:
	 * @param key
	 * @param member
	 * @return
	 * 返回有序集 key 中，成员 member 的 score 值
	 * 如果 member 元素不是有序集 key 的成员，或 key 不存在，返回 null 。
	 */
	public int zscore_(String key, String member) {
		Jedis redis = this.pool.getResource();
		int ret = -1;
		Double vv = redis.zscore(key, member);
		if(vv != null){
			ret = (int)vv.doubleValue();
		}
		this.pool.returnResource(redis);
		if(ret != -1){
			ret += 1; 
		}
		return ret;
	}

	/**
	 * min 和max 都是score的值
	 * 
	 * @Title: zrangebyscore_
	 * @Description:
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	// add 20141216
	public Set<String> zrangebyscore_(String key, long min, long max) {
		Jedis redis = this.pool.getResource();

		Set<String> ss = redis.zrangeByScore(key, min, max);
		this.pool.returnResource(redis);
		return ss;
	}
	
	public Set<String> zrange(String key){
		Jedis redis = this.pool.getResource();

		Set<String> ss = Collections.EMPTY_SET;
		try{
			ss = redis.zrange(key, 0, -1);//取出所有元素
		}finally{
			this.pool.returnResource(redis);
		}
		return ss;
	}

	/**
	 * min 和max 都是score的值 获得一个包含了score的元组集合. 元组（Tuple）
	 * 笛卡尔积中每一个元素（d1，d2，…，dn）叫作一个n元组（n-tuple）或简称元组
	 * 
	 * @Title: zrangebyscorewithscores_
	 * @Description:
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<Tuple> zrangebyscorewithscores_(String key, long min, long max) {
		Jedis redis = this.pool.getResource();
		if (redis == null) {
			return null;
		}
		Set<Tuple> result = null;
		try {
			result = redis.zrangeByScoreWithScores(key, min, max);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			this.pool.returnResource(redis);
		}
		return result;
	}

	/**
	 * zrevrangeWithScores ： 从大到小排序 zrangeWithScores ： 从小到大排序
	 * 
	 * @Title: zrangeWithScores
	 * @Description:
	 * @param key
	 * @param start
	 *            ： （排名）0表示第一个元素，-x：表示倒数第x个元素
	 * @param end
	 *            ： （排名）-1表示最后一个元素（最大值）
	 * @return 返回 排名在start 、end之间带score元素
	 */
	public Map<String, Double> zrevrangeWithScores(String key, long start,
			long end) {
		Jedis redis = this.pool.getResource();
		if (redis == null) {
			return null;
		}
		Set<Tuple> result = null;
		try {
			result = redis.zrevrangeWithScores(key, start, end);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			this.pool.returnResource(redis);
		}
		return tupleToMap(result);
	}

	/**
	 * @Title: tupleToMap
	 * @Description:
	 * @param tupleSet
	 * @return Map<String, Double> ： 返回的是 有序<element, score>
	 */
	public Map<String, Double> tupleToMap(Set<Tuple> tupleSet) {
		if (tupleSet == null)
			return null;
		Map<String, Double> map = new LinkedHashMap<String, Double>();
		for (Tuple tup : tupleSet) {
			map.put(tup.getElement(), tup.getScore());
		}
		return map;
	}

	/**
	 * 删除key中的member
	 * 
	 * @Title: zrem
	 * @Description:
	 * @param key
	 * @param mem
	 * @return
	 */
	public long zrem(String key, String member) {
		Jedis redis = this.pool.getResource();
		if (redis == null) {
			return -1;
		}
		long result = -1;
		try {
			result = redis.zrem(key, member);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			this.pool.returnResource(redis);
		}
		return result;
	}

	/**
	 * 从高到低排名，返回前 num 个score和member
	 * 
	 * @param key
	 * @param num
	 * @return
	 */
	public Set<Tuple> ztopWithScore(String key, int num) {
		if (num <= 0) {
			return null;
		}

		Jedis redis = this.pool.getResource();

		Set<Tuple> ret = redis.zrevrangeWithScores(key, 0, num - 1);

		this.pool.returnResource(redis);

		return ret;
	}

	/**
	 * 返回score区间的member
	 * 
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 */
	public Set<String> zrankByScore(String key, int max, int min) {
		Jedis redis = this.pool.getResource();
		Set<String> ret = redis.zrevrangeByScore(key, max, min);
		this.pool.returnResource(redis);
		return ret;
	}

	/**
	 * 从高到低排名，返回前 num 个
	 * 
	 * @param key
	 * @param num
	 * @return
	 */
	public Set<String> ztop(String key, int num) {
		if (num <= 0) {
			return null;
		}

		Jedis redis = this.pool.getResource();

		Set<String> ret = redis.zrevrange(key, 0, num - 1);

		this.pool.returnResource(redis);

		return ret;
	}

	/**
	 * 从高到低排名，返回start到end的前 num 个
	 * 
	 * @param key
	 * @param num
	 * @return
	 */
	public Set<String> ztop(String key, int start, int end) {
		if (end <= start) {
			return null;
		}

		Jedis redis = this.pool.getResource();

		Set<String> ret = redis.zrevrange(key, start, end - 1);

		this.pool.returnResource(redis);

		return ret;
	}
	
	/** 
	 * @Title: zrange 
	 * @Description: 从低到高排名，返回start到end的前 num 个
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @return Set<String>
	 * @throws 
	 */
	public Set<String> zrange(String key,int start,int end){
		if (end <= start) {
			return null;
		}

		Jedis redis = this.pool.getResource();

		Set<String> ret = redis.zrange(key, start, end - 1);

		this.pool.returnResource(redis);

		return ret;
	}

	/**
	 * 返回zset的的元素个数
	 * 
	 * @Title: zcard_
	 * @Description:
	 * @param key
	 * @return
	 */
	public long zcard_(String key) {
		Jedis redis = this.pool.getResource();
		long size = redis.zcard(key);
		this.pool.returnResource(redis);
		return size;
	}
	/************************************************************************************
	 * 消息订阅和发布接口
	 ***********************************************************************************/
	public void subcribe(IMessageListener listener, final String... channel) {
		if (channel == null || listener == null) {
			return;
		}

		final Jedis redis = this.pool.getResource();

		for (String c : channel) {
			this.redisListener.registerListener(c, listener);
		}

		Thread thread = new Thread() {

			public void run() {
				redis.subscribe(redisListener, channel);

				pool.returnResource(redis);
			}
		};
		thread.setName("channellistener");
		thread.setDaemon(true);
		thread.start();
	}

	public void unsubcribe(String channel) {
		this.redisListener.unsubscribe(channel);
	}

	public void unsubscribe() {
		this.redisListener.unsubscribe();
	}

	public static void destroy() {
		getInstance().pool.destroy();
	}

	public void publish(String channel, String message) {
		if (channel == null || message == null) {
			return;
		}

		Jedis redis = this.pool.getResource();

		redis.publish(channel, message);

		this.pool.returnResource(redis);
	}

	public Long rpush(String key, MessageLite msg) {
		if (msg == null) {
			return -1L;
		}
		Jedis redis = this.pool.getResource();

		Long ret = redis.rpush(key.getBytes(), msg.toByteArray());

		this.pool.returnResource(redis);
		return ret;
	}

	public String lpop(String key) {
		Jedis redis = this.pool.getResource();
		String value = redis.lpop(key);
		this.pool.returnResource(redis);
		return value;
	}

	public List<?> lrange(String key, MessageLite lite, int start, int end) {
		Jedis redis = this.pool.getResource();

		List<byte[]> list = redis.lrange(key.getBytes(), start, end);
		if (lite != null) {
			int size = list.size();
			List<MessageLite.Builder> ret = new ArrayList<MessageLite.Builder>(
					size);
			for (int i = 0; i < size; i++) {
				Builder bd = lite.newBuilderForType();
				try {
					bd.mergeFrom(list.get(i));
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
				ret.add(bd);
			}

			this.pool.returnResource(redis);
			return ret;
		} else {
			this.pool.returnResource(redis);
			return list;
		}
	}
	public List<byte[]>lrange(String key,int start, int end) {
		Jedis redis = this.pool.getResource();
		List<byte[]> list = redis.lrange(key.getBytes(), start, end);
		this.pool.returnResource(redis);
		return list;
	}
	public Builder lindex(String key, MessageLite lite, int start) {
		Jedis redis = this.pool.getResource();

		byte[] list = redis.lindex(key.getBytes(), start);
		if (list == null) {
			this.pool.returnResource(redis);
			return null;
		}
		Builder bd = lite.newBuilderForType();
		try {
			bd.mergeFrom(list);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}finally{
			this.pool.returnResource(redis);
		}
		return bd;
	}

	public void lrem(String key, int count, String value) {
		Jedis redis = this.pool.getResource();
		redis.lrem(key, count, value);
		this.pool.returnResource(redis);
	}
}
