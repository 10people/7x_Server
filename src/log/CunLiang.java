package log;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CunLiang {
public String gameappid;// char(64) DEFAULT NULL,
public String openid;// char(64) DEFAULT NULL,
public int zoneid;// int(16) DEFAULT NULL,
public Date regtime;// datetime DEFAULT NULL,
public int level;// int(16) DEFAULT NULL,
public int iFriends;// int(16) DEFAULT NULL,
public int moneyios;// int(16) DEFAULT NULL,
public int moneyandroid;// int(16) DEFAULT NULL,
public int diamondios;// int(16) DEFAULT NULL,
public int diamondandroid;// int(16) DEFAULT NULL,
public int Fight;// int(16) DEFAULT NULL,
public int VipPoint;// int(16) DEFAULT NULL,
public int CareerId;// int(16) DEFAULT NULL,
@Id
public long RoleId;// int(16) DEFAULT NULL,
public int islogin;// tinyint(1) DEFAULT NULL,
public int totaltime;// int(10) DEFAULT NULL,
public int todayonlinetime;// int(10) NOT NULL,
public int ispay;// tinyint(1) NOT NULL,
public int todaypay;// int(11) NOT NULL,
public int LoginChannel;// int(11) NOT NULL,
public String RoleName;// varchar(64) NOT NULL
public int PlatId = 1  ;// 平台  ios 0 /android 1
}
