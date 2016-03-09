package com.qx.persistent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

import net.sf.json.JSONObject;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.util.BaseException;
import com.qx.account.Account;
import com.qx.alliance.AllianceBean;
import com.qx.alliancefight.AllianceFightMatch;
import com.qx.http.LoginServ;
import com.qx.robot.RobotSession;
import com.qx.util.TableIDCreator;

public class HibernateUtil {
	public static boolean showMCHitLog = false;
	public static Logger log = LoggerFactory.getLogger(HibernateUtil.class);
	public static Map<Class<?>, String> beanKeyMap = new HashMap<Class<?>, String>();
	public static final SessionFactory sessionFactory = buildSessionFactory();

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    public static Throwable insert(Object o){
    	Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	try {
   			session.save(o);
        	session.getTransaction().commit();
		} catch (Throwable e) {
			log.error("0要insert的数据{}", o == null ? "null" : JSONObject.fromObject(o).toString());
			log.error("0保存出错", e);
			session.getTransaction().rollback();
			return e;
		}
    	return null;
    }
    /**
     *FIXME 不要这样返回异常，没人会关系返回的异常。
     * @param o
     * @return
     */
    public static Throwable save(Object o){
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	boolean mcOk = false;
    	try {
    		if(o instanceof MCSupport){
    			MCSupport s = (MCSupport) o;//需要对控制了的对象在第一次存库时调用MC.add
    			MC.update(o, s.getIdentifier());//MC中控制了哪些类存缓存。
    			mcOk = true;
    			session.update(o);
    		}else{
    			session.saveOrUpdate(o);
    		}
        	t.commit();
		} catch (Throwable e) {
			log.error("1要save的数据{},{}",o, o == null ? "null" : JSONObject.fromObject(o).toString());
			if(mcOk){
				log.error("MC保存成功后报错，可能是数据库条目丢失。");
			}
			log.error("1保存出错", e);
			t.rollback();
			return e;
		}
    	return null;
    }
    public static Throwable update(Object o){
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	try {
    		if(o instanceof MCSupport){
    			MCSupport s = (MCSupport) o;//需要对控制了的对象在第一次存库时调用MC.add
    			MC.update(o, s.getIdentifier());//MC中控制了哪些类存缓存。
    			session.update(o);
    		}else{
    			session.update(o);
    		}
        	t.commit();
		} catch (Throwable e) {
			log.error("1要update的数据{},{}",o, o == null ? "null" : JSONObject.fromObject(o).toString());
			log.error("1保存出错", e);
			t.rollback();
			return e;
		}
    	return null;
    }
    
    public static String getKeyField(Class<?> c){
    	synchronized (beanKeyMap) {
	    	String key = beanKeyMap.get(c);
	    	if(key != null){
	    		return key;
	    	}
	    	Field[] fs = c.getDeclaredFields();
	    	for(Field f : fs){
	    		if(f.isAnnotationPresent(javax.persistence.Id.class)){
	    			key = f.getName();
	    			beanKeyMap.put(c, key);
	    			break;
	    		}
	    	}
	    	return key;
    	}
    }
    public static <T> T find(Class<T> t,long id){
    	String keyField = getKeyField(t);
    	if(keyField == null){
    		throw new RuntimeException("类型"+t+"没有标注主键");
    	}
    	if(!MC.cachedClass.contains(t)){
    		return find(t, "where "+keyField+"="+id,false);
    	}
    	T ret = MC.get(t, id);
    	if(ret == null){
    		if(showMCHitLog)log.info("MC未命中{}#{}",t.getSimpleName(),id);
    		ret = find(t, "where "+keyField+"="+id,false);
    		if(ret != null){
    			if(showMCHitLog)log.info("DB命中{}#{}",t.getSimpleName(),id);
    			MC.add(ret,id);
    		} else {
    			if(showMCHitLog)log.info("DB未命中{}#{}",t.getSimpleName(),id);
    		}
    	}else{
    		if(showMCHitLog)log.info("MC命中{}#{}",t.getSimpleName(),id);
    	}
    	return ret;
    }
    public static <T> T find(Class<T> t, String where){
    	return find(t, where, true);
    }
    public static <T> T find(Class<T> t, String where, boolean checkMCControl){
    	if(checkMCControl && MC.cachedClass.contains(t)){
    		//请使用static <T> T find(Class<T> t,long id)
    		throw new BaseException("由MC控制的类不能直接查询DB:"+t);
    	}
    	Session session = sessionFactory.getCurrentSession();
    	Transaction tr = session.beginTransaction();
    	T ret = null;
    	try{
    		//FIXME 使用 session的get方法代替。
	    	String hql = "from "+t.getSimpleName()+" "+ where;
	    	Query query = session.createQuery(hql);
	    	
	    	ret = (T) query.uniqueResult();
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("list fail for {} {}", t, where);
    		log.error("list fail", e);
    	}
    	return ret;
    }
    
    /**
     * 通过指定key值来查询对应的对象
     * @param t
     * @param name
     * @param where
     * @return
     */
    public static <T> T findByName(Class<? extends MCSupport> t, String name, String where){
    	Class<? extends MCSupport> targetClz = t;//.getClass();
		String key = targetClz.getSimpleName() + ":" + name;
    	Object id = MC.getValue(key);
    	T ret = null;
    	if(id != null){
    		log.info("id find in cache");
    		ret = (T) find(targetClz, Long.parseLong((String)id));
    		return ret;
    	} else {
			ret = (T) find(targetClz, where, false);
		}
    	if(ret == null){
    		log.info("no record {}, {}",key, where);
    	}else{
    		MCSupport mc = (MCSupport) ret;
    		long mcId = mc.getIdentifier();
			log.info("found id from DB {}#{}",targetClz.getSimpleName(),mcId);
			MC.add(key, mcId);
    		ret = (T) find(targetClz, mcId);
    	}
    	return ret;
    
    }
    /**
     * @param t
     * @param where 例子： where uid>100
     * @return
     */
    public static <T>  List<T> list(Class<T> t, String where){
    	Session session = sessionFactory.getCurrentSession();
    	Transaction tr = session.beginTransaction();
    	List<T> list = Collections.EMPTY_LIST;
    	try{
	    	String hql = "from "+t.getSimpleName()+" "+ where;
	    	Query query = session.createQuery(hql);
	    	
	    	list = query.list();
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("list fail for {} {}", t, where);
    		log.error("list fail", e);
    	}
    	return list;
    }
    
    public static SessionFactory buildSessionFactory() {
    	log.info("开始构建hibernate");
        try {
            // Create the SessionFactory from hibernate.cfg.xml
        	Configuration configuration = new Configuration().configure();
        	ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        	SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        	log.info("结束构建hibernate");
        	return sessionFactory;
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            log.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

	public static Throwable delete(Object o) {
		if(o == null){
			return null;
		}
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	try {
    		if(o instanceof MCSupport){
    			MCSupport s = (MCSupport) o;//需要对控制了的对象在第一次存库时调用MC.add
    			MC.delete(o.getClass(), s.getIdentifier());//MC中控制了哪些类存缓存。
    		}
        	session.delete(o);
        	session.getTransaction().commit();
		} catch (Throwable e) {
			log.error("要删除的数据{}", o);
			log.error("出错", e);
			session.getTransaction().rollback();
			return e;
		}
    	return null;		
	}

	public static void setAllAllianceReputation(){
		Session session = sessionFactory.getCurrentSession();
    	Transaction tr = session.beginTransaction();
    	String hql = "update  AllianceBean set reputation = 0";
    	try{
	    	Query query = session.createQuery(hql);
	    	query.executeUpdate();
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("setAllAllianceReputation faild ", AllianceBean.class, hql);
    		log.error("setAllAllianceReputation fail", e);
    	}
	}
	
	/**
	 * 注意这个方法会返回大于等于1的值。数据库无记录也会返回1，而不是null
	 * @param t
	 * @return 
	 */
	public static <T> Long getTableIDMax(Class<T> t) {
		Long id = null;
		Session session = sessionFactory.getCurrentSession();
    	Transaction tr = session.beginTransaction();
    	String hql = "select max(id) from "+ t.getSimpleName();
    	try{
	    	Query query = session.createQuery(hql);
	    	Object uniqueResult = query.uniqueResult();
	    	if(uniqueResult == null){
	    		id = 1L;
	    	}else{
	    		id = Long.parseLong(uniqueResult +"");
	    		id = Math.max(1L, id);
	    	}
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("query max id fail for {} {}", t, hql);
    		log.error("query max id fail", e);
    	}
    	return id;
	}
	
	public static <T> int getColumnValueMax(Class<T> t, String column) {
		Integer id = null;
		Session session = sessionFactory.getCurrentSession();
    	Transaction tr = session.beginTransaction();
    	String hql = "select max("+column+") from "+ t.getSimpleName();
    	try{
	    	Query query = session.createQuery(hql);
	    	Object uniqueResult = query.uniqueResult();
	    	if(uniqueResult == null){
	    		id = 1;
	    	}else{
	    		id = Integer.parseInt(uniqueResult +"");
	    		id = Math.max(1, id);
	    	}
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("query column value max fail for {} {}", t, hql);
    		log.error("query column value max fail", e);
    	}
    	return id;
	}
	
	
	public static <T> int getColumnValueMaxOnWhere(Class<T> t, String column, String where) {
		Integer id = null;
		Session session = sessionFactory.getCurrentSession();
    	Transaction tr = session.beginTransaction();
    	String hql = "select max("+column+") from "+ t.getSimpleName() + where;
    	try{
	    	Query query = session.createQuery(hql);
	    	Object uniqueResult = query.uniqueResult();
	    	if(uniqueResult == null){
	    		id = 1;
	    	}else{
	    		id = Integer.parseInt(uniqueResult +"");
	    		id = Math.max(1, id);
	    	}
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("query column value max fail for {} {}", t, hql);
    		log.error("query column value max fail", e);
    	}
    	return id;
	}
	public static List<Map<String, Object>> querySql(String hql){
		Session session = sessionFactory.getCurrentSession();
    	Transaction tr = session.beginTransaction();
    	List list = Collections.emptyList();
    	try{
	    	SQLQuery query = session.createSQLQuery(hql);
	    	query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
	    	list = query.list();
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("query  failed {}", hql);
    		log.error("query count(type) fail", e);
    	}
    	return list;
	}
	public static int getCount(String hql){
		Session session = sessionFactory.getCurrentSession();
    	Transaction tr = session.beginTransaction();
    	int count = -1;
    	try{
	    	Query query = session.createQuery(hql);
	    	Object uniqueResult = query.uniqueResult();
	    	if(uniqueResult != null){
	    		count = Integer.parseInt(uniqueResult +"");
	    	}
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("query  faild", hql);
    		log.error("query count(type) fail", e);
    	}
    	return count;
	}
	
	/**
	 * 除了jsp界面，请勿使用此方法！
	 * @param name
	 * @return
	 */
	public static Account getAccount(String name){
		Account ret = null;
		ret = find(Account.class, "where accountName='"+name+"'", false);
		if(ret == null){
			LoginServ req = new LoginServ(new RobotSession(),name);
			JSONObject o = req.sendRequest();
			if(o!=null){
				int accId = o.optInt("accId", -1);
				if(accId != -1){
					ret = new Account();
					ret.setAccountName(name);
					ret.setAccountId(accId);
					ret.setAccountPwd(o.optString("pwd",""));
					HibernateUtil.insert(ret);
				}
			}
		}
		return ret;
	}
	
	public static <T> boolean clearTable(Class<T> clazz) {
		String className = clazz.getSimpleName();
		String hql = "delete from " + className;
		Session session = sessionFactory.getCurrentSession();
		Transaction tr = session.beginTransaction();
		try{
			Query query = session.createQuery(hql);
			query.executeUpdate();
	    	tr.commit();
    	}catch(Exception e){
    		tr.rollback();
    		log.error("delete table failed hql:{} e:{}", hql, e);
    		return false;
    	} 
		return true; 
	}
	
}
