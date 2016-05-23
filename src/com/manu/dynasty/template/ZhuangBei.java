package com.manu.dynasty.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZhuangBei extends BaseItem implements SelfBuilder{
	private Logger logger = LoggerFactory.getLogger(ZhuangBei.class);

	public int id;
	public String name;
	public String funDesc;
	public int buWei;
	public int pinZhi;
	public int color;
	
	public int gongji;
	public int fangyu;
	public int shengming;
	public int dengji;
	
	public int tongli;
	public int wuli;
	public int mouli;
	
	public int qianghuaMaxLv;
	public int xilianMaxLv;
	
	public int exp;
	public int expId;
	public int qianghuaId;
	public String xishu;
	public float[] xishuArray;
	public int icon;
	public int gongjiType;
	public int jinjieLv;
	public int jinjieItem;
	public int jinjieNum;
	public int jiejieId;
	
	public int wqSH;//	武器伤害加深
	public int wqJM;//	武器伤害减免
	public int wqBJ;//	武器暴击加深
	public int wqRX;//	武器暴击减免
	public int jnSH;//	技能伤害加深
	public int jnJM;//	技能伤害减免
	public int jnBJ;//	技能暴击加深
	public int jnRX;//	技能暴击减免
	public int wqBJL;//	武器暴击率
	public int jnBJL;//	技能暴击率
	public int wqMBL;//	武器免暴率
	public int jnMBL;//	技能免暴率
	public int sxJiaCheng;//	技能冷却缩减
	public String skill;
	public int modelId;
	
	public int holeNum;//装备孔数量
	public int inlayColor; //装备孔颜色
	
	public int lvlupExp ;//装备进阶所需经验
	
	public String getXishu() {
		return xishu;
	}
	public void setXishu(String xishu) {
		this.xishu = xishu;
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
	public String getFunDesc() {
		return funDesc;
	}
	public void setFunDesc(String funDesc) {
		this.funDesc = funDesc;
	}
	public int getBuWei() {
		return buWei;
	}
	public void setBuWei(int buWei) {
		this.buWei = buWei;
	}
	public int getPinZhi() {
		return pinZhi;
	}
	public void setPinZhi(int pinZhi) {
		this.pinZhi = pinZhi;
	}
	public int getGongji() {
		return gongji;
	}
	public void setGongji(int gongji) {
		this.gongji = gongji;
	}
	public int getFangyu() {
		return fangyu;
	}
	public void setFangyu(int fangyu) {
		this.fangyu = fangyu;
	}
	public int getShengming() {
		return shengming;
	}
	public void setShengming(int shengming) {
		this.shengming = shengming;
	}
	public int getTongli() {
		return tongli;
	}
	public void setTongli(int tongli) {
		this.tongli = tongli;
	}
	public int getWuli() {
		return wuli;
	}
	public void setWuli(int wuli) {
		this.wuli = wuli;
	}
	public int getMouli() {
		return mouli;
	}
	public void setMouli(int mouli) {
		this.mouli = mouli;
	}
	public int getQianghuaMaxLv() {
		return qianghuaMaxLv;
	}
	public void setQianghuaMaxLv(int qianghuaMaxLv) {
		this.qianghuaMaxLv = qianghuaMaxLv;
	}
	public int getXilianMaxLv() {
		return xilianMaxLv;
	}
	public void setXilianMaxLv(int xilianMaxLv) {
		this.xilianMaxLv = xilianMaxLv;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public int getExpId() {
		return expId;
	}
	public void setExpId(int expId) {
		this.expId = expId;
	}
	public int getQianghuaId() {
		return qianghuaId;
	}
	public void setQianghuaId(int qianghuaId) {
		this.qianghuaId = qianghuaId;
	}
	@Override
	public int getType() {
		return TYPE_EQUIP;
	}
	public int getDengji() {
		return dengji;
	}
	public void setDengji(int dengji) {
		this.dengji = dengji;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public int getGongjiType() {
		return gongjiType;
	}
	public void setGongjiType(int gongjiType) {
		this.gongjiType = gongjiType;
	}
	
	public int getJinjieLv() {
		return jinjieLv;
	}
	public void setJinjieLv(int jinjieLv) {
		this.jinjieLv = jinjieLv;
	}
	public int getJinjieItem() {
		return jinjieItem;
	}
	public void setJinjieItem(int jinjieItem) {
		this.jinjieItem = jinjieItem;
	}
	public int getJinjieNum() {
		return jinjieNum;
	}
	public void setJinjieNum(int jinjieNum) {
		this.jinjieNum = jinjieNum;
	}
	public int getJiejieId() {
		return jiejieId;
	}
	public void setJiejieId(int jiejieId) {
		this.jiejieId = jiejieId;
	}
	@Override
	public int getIconId() {
		return icon;
	}
	public int getWqSH() {
		return wqSH;
	}
	public void setWqSH(int wqSH) {
		this.wqSH = wqSH;
	}
	public int getWqJM() {
		return wqJM;
	}
	public void setWqJM(int wqJM) {
		this.wqJM = wqJM;
	}
	public int getWqBJ() {
		return wqBJ;
	}
	public void setWqBJ(int wqBJ) {
		this.wqBJ = wqBJ;
	}
	public int getWqRX() {
		return wqRX;
	}
	public void setWqRX(int wqRX) {
		this.wqRX = wqRX;
	}
	public int getJnSH() {
		return jnSH;
	}
	public void setJnSH(int jnSH) {
		this.jnSH = jnSH;
	}
	public int getJnJM() {
		return jnJM;
	}
	public void setJnJM(int jnJM) {
		this.jnJM = jnJM;
	}
	public int getJnBJ() {
		return jnBJ;
	}
	public void setJnBJ(int jnBJ) {
		this.jnBJ = jnBJ;
	}
	public int getJnRX() {
		return jnRX;
	}
	public void setJnRX(int jnRX) {
		this.jnRX = jnRX;
	}
	// 背包物品堆叠上限999
	public int getRepeatNum(){
		return 1;
	}
	
	public float[] getXishuArray() {
		return xishuArray;
	}
	
	public int getWqBJL() {
		return wqBJL;
	}
	public void setWqBJL(int wqBJL) {
		this.wqBJL = wqBJL;
	}
	public int getJnBJL() {
		return jnBJL;
	}
	public void setJnBJL(int jnBJL) {
		this.jnBJL = jnBJL;
	}
	public int getWqMBL() {
		return wqMBL;
	}
	public void setWqMBL(int wqMBL) {
		this.wqMBL = wqMBL;
	}
	public int getJnMBL() {
		return jnMBL;
	}
	public void setJnMBL(int jnMBL) {
		this.jnMBL = jnMBL;
	}
	
	public int getSxJiaCheng() {
		return sxJiaCheng;
	}
	public void setSxJiaCheng(int sxJiaCheng) {
		this.sxJiaCheng = sxJiaCheng;
	}
	private void setXishuArray() {
		if(xishu == null || xishu.equals("")) {
			logger.error("装备xishu不能为空");
			return;
		}
		String[] strs = xishu.split(",");
		int length = strs.length;
		float[] xishuArray = new float[length];
		for(int i = 0; i < length; i++) {
			xishuArray[i] = Float.parseFloat(strs[0]);
		}
		this.xishuArray = xishuArray;
	}
	
	@Override
	public void build() {
		setXishuArray();
	}
}
