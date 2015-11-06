package log;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 用于生成消费原因和id
 * @author 康建虎
 *
 */
@Entity
public class ReasonBean {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	public String reason;
}
