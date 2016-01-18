import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;


public class TestM {
	public static void main(String[] args) throws Exception{
		MC.cachedClass.remove(JunZhu.class);
		
		HibernateUtil.find(JunZhu.class,1001);
		System.out.println("-----");
		HibernateUtil.find(JunZhu.class,1001);
	}
}
