package com.manu.dynasty.template;

import com.qx.huangye.shop.BaseDuiHuan;

public class DangpuCommon extends BaseDuiHuan{
	
	public int max;
	public int getMax(){
		System.out.print("是的，调用了：max =  " + max);
		return max;
	}
//implements Comparable<DangpuCommon> {

//
//	@Override
//	public int compareTo(DangpuCommon o) {
//		if (this.site > o.site) {
//			return 1;
//		} else if (this.site < o.site) {
//			return -1;
//		}
//		return 0;
//	}
}
