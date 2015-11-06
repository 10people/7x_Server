package log;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Log2DBHistory")
public class Log2DBBean {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	public String fileName;
	public Date importTime;
	public String result;
	public int rowCnt;
}
