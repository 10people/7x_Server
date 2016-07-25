package com.qx.gm.message;

public class DoServrStateResp extends BaseResp {
	public int status;// 服务器状态值2关闭1正常）

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
