package com.manu.dynasty.template;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="RoleName")
public class NameKu {
	@Id
	public int id;
	public String name;
	public int sex;
	public byte isUse;
	
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
}
