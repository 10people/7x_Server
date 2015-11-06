package log;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBHelper {
	public static String url = "jdbc:mysql://192.168.3.80/sourcedata_tlog_newdb";
	public static String Drivername = "com.mysql.jdbc.Driver";
	public static String user = "devuser";
	public static String password = "devuser";
	public static Logger log = Log2DB.log;

	public Connection conn = null;
	public PreparedStatement pst = null;

	public DBHelper(){
		readDBConf();
	}
	public DBHelper(String sql) {
		readDBConf();
		try {
			Class.forName(Drivername);//指定连接类型
			conn = DriverManager.getConnection(url, user, password);//获取连接
			pst = conn.prepareStatement(sql);//准备执行语句
		} catch (Exception e) {
			log.error("创建数据库连接失败",e);
		}
	}

	public void readDBConf() {
		Properties prop = new Properties();
		InputStream is = DBHelper.class.getResourceAsStream("/log2db.properties");
		if(is != null){
			try {
				prop.load(is);
				url = prop.getProperty("url", url);
				user = prop.getProperty("user", user);
				password = prop.getProperty("password", password);
			} catch (IOException e) {
				log.error("载入配置出错",e);
			}finally{
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void close() {
		try {
			if(conn!=null)this.conn.close();
			if(pst!=null)this.pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
