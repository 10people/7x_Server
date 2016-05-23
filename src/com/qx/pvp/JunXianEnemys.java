package com.qx.pvp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
/**根据jzId+junXianId可以定位一条数据，获取该玩家在该军衔已锁定的四个排名记录*/
public class JunXianEnemys {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)		
	long dbId ;
	long jzId ;
	long junxianId ;
	//对应排名从前往后的四个排名记录
	int rank1;
	int rank2;
	int rank3;
	int rank4;
	
}
