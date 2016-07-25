package com.qx.gm.message;

import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;

public class DoDelGMNoticeReq {
	public int type;
	public int firm;
	public int zone;
	public int noticeid;
	public String md5;

	public boolean checkMd5() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(getType()).append(getFirm()).append(getZone())
				.append(getNoticeid()).append(CodeUtil.MD5_KEY);
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

	public int getNoticeid() {
		return noticeid;
	}

	public void setNoticeid(int noticeid) {
		this.noticeid = noticeid;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
