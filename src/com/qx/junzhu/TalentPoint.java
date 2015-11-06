package com.qx.junzhu;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name="talent_point",indexes={@Index(name="junZhuId",columnList="junZhuId")})
public class TalentPoint {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id; 
	public long junZhuId;
	public int point;
	public int level;
}