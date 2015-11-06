package xg.push;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class XGTokenBean {
	public String token;
	@Id
	public long jzId;
}
