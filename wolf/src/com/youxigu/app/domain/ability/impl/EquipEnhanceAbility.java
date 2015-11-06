package com.youxigu.app.domain.ability.impl;

import com.youxigu.app.domain.ability.IAbility;

/**
 * 装备强化：
 * 1 一般只是保存一个道具Id，此道具效果中配置了强化效果，比如所有属性提高x点，或者 提高 x%点。效果有等级，名字和描述
 * 
 * 2 星级和强化一样，只是显示不同；
 * 
 * 3 宝石镶嵌和强化也是一样的。
 * 
 * 这里主张，存放实体id，而不是普通而言的效果，只是为了便于显示
 * 
 * @author wuliangzhu
 *
 */
public class EquipEnhanceAbility implements IAbility {

}
