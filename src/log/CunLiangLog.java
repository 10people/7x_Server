package log;

import java.text.SimpleDateFormat;
import java.util.Date;

import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet.Builder;

import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;

/**
 * 每次有注册就加入一条，相关数据有变动就更新下。
 * 每天零点导出一份，再重置那些会变化的数据
 * @author 康建虎
 *
 */
public class CunLiangLog {
	public static CunLiangLog inst = new CunLiangLog();
	public static String gameappid="gameappid";
	public static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static int zoneid = 1;
	public void add(long jzId, int channel, String name, int CareerId){
		String jzIdStr = String.valueOf(jzId);
		CunLiang cl = new CunLiang();
		cl.gameappid=gameappid;
		cl.openid=jzIdStr;
		cl.zoneid= zoneid;
		cl.regtime= new Date();
		cl.level= 1;
		cl.iFriends= 0;
		cl.moneyios= 0;
		cl.moneyandroid= 0;
		cl.diamondios= 0;
		cl.diamondandroid= 0;
		cl.Fight= 0;
		cl.VipPoint= 0;
		cl.CareerId= CareerId;
		cl.RoleId= jzId;
		cl.islogin= 0;
		cl.totaltime=0 ;
		cl.todayonlinetime= 0;
		cl.ispay= 0;
		cl.todaypay= 0;
		cl.LoginChannel= channel;
		cl.RoleName= name;
		HibernateUtil.insert(cl);
	}
	public void levelChange(long jzId, int lv){
		CunLiang cl = HibernateUtil.find(CunLiang.class, jzId);
		if(cl == null){
			return;
		}
		cl.level = lv;
		HibernateUtil.save(cl);
	}
	public void yuanBaoChange(long jzId,int yuanBao){
		CunLiang cl = HibernateUtil.find(CunLiang.class, jzId);
		if(cl == null){
			return;
		}
		cl.diamondios = yuanBao;
		HibernateUtil.save(cl);
	}
	public void zhangLiChange(long jzId, long zhanLi){};
	public void logout(long jzId, long onlinetime, int tongBi){
		CunLiang cl = HibernateUtil.find(CunLiang.class, jzId);
		if(cl == null){
			return;
		}
		cl.islogin = 1;
		cl.totaltime += onlinetime;
		cl.todayonlinetime+=onlinetime;
		cl.moneyios = tongBi;
		Builder b = JunZhuMgr.inst.jzInfoCache.get(jzId);
		if(b != null){
			cl.Fight = b.getZhanLi();
		}
		HibernateUtil.save(cl);
	}
}
