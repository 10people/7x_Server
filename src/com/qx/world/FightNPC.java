package com.qx.world;

import com.manu.dynasty.template.JCZNpcTemp;
import com.qx.junzhu.JunZhu;

public class FightNPC extends Player{
	public JCZNpcTemp temp;
	public Player target;
	public long preSkillTime;
	public JunZhu fakeJz;
	public int state = 0;//2归位;1追击;0空闲
}
