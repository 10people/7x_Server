package com.qx.yabiao;

import com.qx.world.Scene;

public class ProduceCartInfo {
	public Scene ybsc;
	public int ybScId;
	public int pathId;
	public int produceNo;
	
	public ProduceCartInfo() {
		super();
	}

	public ProduceCartInfo(Scene ybsc, int ybScId, int pathId, int produceNo) {
		super();
		this.ybsc = ybsc;
		this.ybScId = ybScId;
		this.pathId = pathId;
		this.produceNo = produceNo;
	}

	@Override
	public String toString() {
		return "ProduceCartInfo [ybsc=" + ybsc + ", ybScId=" + ybScId
				+ ", pathId=" + pathId + ", produceNo=" + produceNo + "]";
	}
	
	
}
