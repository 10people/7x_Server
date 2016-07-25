package com.qx.gm.func;

import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.core.servlet.GMServlet;
import com.qx.gm.message.BaseResp;
import com.qx.gm.message.DoFuncSwitchReq;
import com.qx.gm.message.DoServrStateReq;
import com.qx.gm.message.DoServrStateResp;
import com.qx.gm.util.CodeUtil;

public class GMFuncMgr {
	public static GMFuncMgr inst;
	public Logger logger = LoggerFactory.getLogger(GMFuncMgr.class);

	public GMFuncMgr() {
		inst = this;
		initData();
	}

	public void initData() {

	}
	
	public void doServrState(DoServrStateReq request,PrintWriter writer){
		DoServrStateResp response = new DoServrStateResp();

		if (!request.checkMd5()) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		String funcid = request.getFuncid();

	}
	
	public void doFuncSwitch(DoFuncSwitchReq request,PrintWriter writer){
		BaseResp response = new BaseResp();

		if (!request.checkMd5()) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		String funcid = request.getFuncid();
		int status = request.getStatus();
		
		
	}
}
