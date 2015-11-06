package com.manu.dynasty.template;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="RoleName")
public class NameKu {
	@Id
	private int id;
	private String name;
	private int sex;
	private byte isUse;
	
	public NameKu() {
		super();
	}
	public NameKu(int id, String name, int sex, byte isUse) {
		super();
		this.id = id;
		this.name = name;
		this.sex = sex;
		this.isUse = isUse;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public byte getIsUse() {
		return isUse;
	}
	public void setIsUse(byte isUse) {
		this.isUse = isUse;
	}
}
