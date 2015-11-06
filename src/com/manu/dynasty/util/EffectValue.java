package com.manu.dynasty.util;

public class EffectValue {
	private int absValue;
	private int perValue;

	public EffectValue(){
		
	}
	
	public void add(EffectValue ev){
		if (ev != null) {
			this.absValue += ev.absValue;
			this.perValue += ev.perValue;
		}
	}
	
	public EffectValue(int abs,int percent){
		this.absValue=abs;
		this.perValue=percent;
	}
	public int getAbsValue() {
		return absValue;
	}

	public void setAbsValue(int absValue) {
		this.absValue = absValue;
	}

	public int getPerValue() {
		return perValue;
	}

	public void setPerValue(int perValue) {
		this.perValue = perValue;
	}

	public double getDoublePerValue(){
		return 1.0d * perValue / 100d; 
	}
	
	public boolean isValid(){
		return absValue!=0 || perValue!=0;
	}
}
