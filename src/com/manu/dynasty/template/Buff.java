package com.manu.dynasty.template;

public class Buff {
	 public int BuffId;
	 public int IsDebuff;
	 public int Attr_1;
	 public int Attr_1_P1;
	 public int Attr_1_P2;
	 public int Attr_2;
	 public int Attr_2_P1;
	 public int Attr_2_P2;
	 public int Attr_3;
	 public int Attr_3_P1;
	 public int Attr_3_P2;
	 public int BuffDisplay;
	 public int BuffDuration; 
	 public int SkillId;
	 /**
	 * 获得该buff后延迟多久开始生效。
	 */
	public int EffectTime;
	 /**
	 * 周期；每多少时间执行一次。
	 */
	public int EffectCycle;
	 public int Caster;
	 public int SkillTarget;

}
