package com.manu.dynasty.template;

public class MiBao {
	public int id;			//秘宝ID；秘宝品质不同，秘宝ID不同
	public int tempId;		//秘宝类型ID；秘宝品质不同，秘宝类型ID相同
	public int icon;
	public int initialStar;//秘宝获得时的初始星级
	public float initialGrow;//秘宝获得时的初始成长
	public int pinzhi;		//秘宝的品质
	public int zuheId;		//秘宝所属的组合套ID
	public int dengji;		//所需君主等级
	public int gongji;		
	public int fangyu;
	public int shengming;	//基础属性初值
	public double gongjiRate;	//基础属性系数
	public double fangyuRate;
	public double shengmingRate;
	public int maxLv;	//进阶等级
	public int expId;		//升级消耗ID，从ExpTemp表中获取升级消耗
	public int suipianId;	//秘宝对应的碎片
	public int nameId;
	public int unlockType;
	public int unlockValue;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTempId() {
		return tempId;
	}
	public void setTempId(int tempId) {
		this.tempId = tempId;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public int getMaxLv() {
		return maxLv;
	}
	public void setMaxLv(int maxLv) {
		this.maxLv = maxLv;
	}
	public int getInitialStar() {
		return initialStar;
	}
	public void setInitialStar(int initialStar) {
		this.initialStar = initialStar;
	}
	public float getInitialGrow() {
		return initialGrow;
	}
	public void setInitialGrow(float initialGrow) {
		this.initialGrow = initialGrow;
	}
	public int getPinzhi() {
		return pinzhi;
	}
	public void setPinzhi(int pinzhi) {
		this.pinzhi = pinzhi;
	}
	public int getZuheId() {
		return zuheId;
	}
	public void setZuheId(int zuheId) {
		this.zuheId = zuheId;
	}
	public int getDengji() {
		return dengji;
	}
	public void setDengji(int dengji) {
		this.dengji = dengji;
	}
	public double getGongji() {
		return gongji;
	}
	public void setGongji(int gongji) {
		this.gongji = gongji;
	}
	public double getFangyu() {
		return fangyu;
	}
	public void setFangyu(int fangyu) {
		this.fangyu = fangyu;
	}
	public double getShengming() {
		return shengming;
	}
	public void setShengming(int shengming) {
		this.shengming = shengming;
	}
	public double getGongjiRate() {
		return gongjiRate;
	}
	public void setGongjiRate(double gongjiRate) {
		this.gongjiRate = gongjiRate;
	}
	public double getFangyuRate() {
		return fangyuRate;
	}
	public void setFangyuRate(double fangyuRate) {
		this.fangyuRate = fangyuRate;
	}
	public double getShengmingRate() {
		return shengmingRate;
	}
	public void setShengmingRate(double shengmingRate) {
		this.shengmingRate = shengmingRate;
	}
	public void setShengmingRate(int shengmingRate) {
		this.shengmingRate = shengmingRate;
	}
	public int getJinjieLv() {
		return maxLv;
	}
	public void setJinjieLv(int jinjieLv) {
		this.maxLv = jinjieLv;
	}
	public int getExpId() {
		return expId;
	}
	public void setExpId(int expId) {
		this.expId = expId;
	}
	public int getSuipianId() {
		return suipianId;
	}
	public void setSuipianId(int suipianId) {
		this.suipianId = suipianId;
	}

}
