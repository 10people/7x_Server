package log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;

import com.qx.persistent.HibernateUtil;


/**
 * grant all on sourcedata_tlog_newdb.* to qxws@'%';
 * @author 康建虎
 *
 */
public class Log2DB {
	
	public static Logger log = LoggerFactory.getLogger("Log2DB");
	public List<String> tables = new ArrayList<String>();

	public String logDir = "null";
	public SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH");
	
	public Log2DB(){
		parseLogDir();
		initLogs();
	}
	
	public void parseLogDir() {
		ch.qos.logback.classic.Logger lg = (ch.qos.logback.classic.Logger) OurLog.ItemFlow;
		Appender<ILoggingEvent> ap = lg.getAppender("ItemFlow");
		RollingFileAppender<ILoggingEvent> rfa = (RollingFileAppender<ILoggingEvent>) ap;
		String fpath = rfa.getFile();
		log.info("path is {}", fpath);
		File file = new File(fpath);
		log.info("path exists {}", file.exists());
		logDir = fpath.substring(0,fpath.lastIndexOf("/"));
	}

	public void initLogs(){
		tables.clear();
		tables.add("GameSvrState");
		tables.add("PlayerOnline");
//		tables.add("PlayerLogin");
		tables.add("PlayerLogout");
		tables.add("MoneyFlow");//ok
		tables.add("ItemFlow");//ok
		tables.add("PlayerExpFlow");//ok
		tables.add("SnsFlow");
		tables.add("RoundFlow");//ok
		///下面是ActLog
		tables.add("KingChange");
		tables.add("KingLvup");
		tables.add("GetExp");
		tables.add("KingTalent");
		tables.add("EquipLvup");
		tables.add("EquipStrength");
		tables.add("EquipRefine");
		tables.add("ShopBuy");
		tables.add("Pawn");
		tables.add("PhysicalPower");
		tables.add("EmailLog");
		tables.add("HeroBattle");
		tables.add("Challenge");
		tables.add("ChallengeAward");
		tables.add("ChallengeExchange");
		tables.add("Guild");
		tables.add("GuildOut");
		tables.add("GuildBreak");
		tables.add("GuildTransfer");
		tables.add("GuildDonate");
		tables.add("Worship");
		tables.add("DailyTask");
		tables.add("LootRich");
		tables.add("KillRobber");
		tables.add("KillRebelArmy");
		tables.add("ConveyDart");
		tables.add("LootDart");
		tables.add("FineGem");
	}
	public void saveAll2db(){
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR_OF_DAY, -1);
		Date time = c.getTime();
		String timeTag = fmt.format(time);
		saveAll2db(timeTag);
	}
	public void saveAll2db(String timeTag){
		for(String s:tables){
			try{
				save2db(s+"."+timeTag+".log");
			}catch(Exception e){
				log.error("当前参数 {}, {}",s,timeTag);
				log.error("处理出错",e);
			}
		}
	}
	public void save2db(final String which){
		String file = logDir +"/"+ which;
		File f = new File(file);
		Log2DBBean bean = new Log2DBBean();
		bean.fileName = file;
		bean.importTime = new Date();
		if(f.exists() == false){
			bean.result = "文件不存在";
			log.error("文件不存在 {}", file);
			HibernateUtil.save(bean);
			return;
		}
		String table = which.substring(0,which.indexOf("."));
        DBHelper db = null;
        String sql = "load data local infile '"+file+"' "
        		+ "into table `"+table+"` "
        		+ "fields terminated by ','";// enclosed by '\\'' lines terminated by '\\r\\n'";
        log.info("准备执行 {}",sql);
        try {
        	db = new DBHelper(sql);
        	if(db.conn == null || db.pst == null){
        		bean.result = "数据库连接异常";
        	}else{
	        	db.pst.execute();
	        	int cnt = db.pst.getUpdateCount();
	 
	            log.info("{} into {} 执行结果完毕 cnt {}", file, table, cnt);
	            //
	            bean.result = "成功";
	            bean.rowCnt = cnt;
        	}
        }catch(Exception e){
        	log.error("执行出错.",e);
        	bean.result="异常"+e.toString();
        } finally {
            if(db != null )db.close();
            HibernateUtil.save(bean);
        }
	}
}
/*
IP 10.221.104.120:5029
qxws
5xAsus9bWxdX6AaA
sourcedata_tlog_qxws
mysql -h112.90.5.111 -P90 -Dsourcedata_tlog_qxws -uqxws -p5xAsus9bWxdX6AaA
mysql -h183.232.16.14 -P90 -Dsourcedata_tlog_qxws -uqxws -p5xAsus9bWxdX6AaA   router上用这个
*/