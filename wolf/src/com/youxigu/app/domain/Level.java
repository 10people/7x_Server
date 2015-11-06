package com.youxigu.app.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 这里指关卡，不是等级。
 * 
 * 关卡是树形结构，树根是玩法名字，树根的子树就是当前关卡的操作对象，如果操作对象了绑定了AI，绑定了Action
 * 他就可以可以交互了
 * 
 * @author wuliangzhu
 *
 */
public class Level {
	private List<Level> children = new ArrayList<Level>();
	
	private int levelId;
	private String leveName;
	private String levelDesc;
	private int scriptId; // 关卡的AI脚本，比如PVP活动，需要一个逻辑计算程序
	private int enterActionId; // 进入的行为，这个行为包括 条件检查， 逻辑执行等。 怪可以攻击，商店可以购买物品
	
}
