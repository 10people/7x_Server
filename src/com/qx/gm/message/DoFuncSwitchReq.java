package com.qx.gm.message;

import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;

public class DoFuncSwitchReq {
	public int type;// 协议编号
	public int firm;// 表示厂商ID
	public int zone;// 大区号
	public String funcid;// 功能开关id
	public int status;// 开关状态1正常2关闭
	public String md5;// 加密

	public boolean checkMd5() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(getType()).append(getFirm()).append(getZone())
				.append(getFuncid()).append(getStatus())
				.append(CodeUtil.MD5_KEY);
		if (!MD5Util.checkMD5(sBuffer.toString(), getMd5())) {// MD5验证
			return false;
		}
		return true;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFirm() {
		return firm;
	}

	public void setFirm(int firm) {
		this.firm = firm;
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public String getFuncid() {
		return funcid;
	}

	public void setFuncid(String funcid) {
		this.funcid = funcid;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
