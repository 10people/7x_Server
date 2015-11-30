package com.qx.gm.activity;

import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.core.servlet.GMServlet;
import com.qx.gm.message.ActivityCompensationReq;
import com.qx.gm.message.ActivityConsumeReq;
import com.qx.gm.message.ActivityConsumeRankReq;
import com.qx.gm.message.ActivityDelReq;
import com.qx.gm.message.ActivityInnerReq;
import com.qx.gm.message.ActivityTimeLoginReq;
import com.qx.gm.message.ActivityTopupReq;
import com.qx.gm.message.ActivityTopupRankReq;
import com.qx.gm.message.BaseResp;
import com.qx.gm.message.DoSendTestCodeReq;
import com.qx.gm.message.Reward;
import com.qx.gm.util.CodeUtil;

public class GMActivityMgr {
	public static GMActivityMgr inst;
	private Logger logger = LoggerFactory.getLogger(GMActivityMgr.class);

	public GMActivityMgr() {
		inst = this;
		initData();
	}

	public void initData() {

	}
	
	public void activityCompensation(ActivityCompensationReq request, PrintWriter writer,String reward4Md5) {
		BaseResp response = new BaseResp();

		if (!request.checkMd5(reward4Md5)) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		int ctype = request.getCtype();
		int actid = request.getActid();
		String title = request.getTitle();
		String content = request.getContent();
		int minlv = request.getMinlv();
		int maxlv = request.getMaxlv();
		List<Reward> rewards = request.getReward();

	}
	
	public void activityDel(ActivityDelReq request,PrintWriter writer){
		BaseResp response = new BaseResp();

		if (!request.checkMd5()) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		int actid = request.getActid();
		int acttype = request.getActtype();
		
		
	}
	
	public void activityTimeLogin(ActivityTimeLoginReq request,PrintWriter writer,String reward4Md5){
		BaseResp response = new BaseResp();

		if (!request.checkMd5(reward4Md5)) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		int ctype = request.getCtype();
		int actid = request.getActid();
		String title = request.getTitle();
		String content = request.getContent();
		int minlv = request.getMinlv();
		int maxlv = request.getMaxlv();
		int start = request.getStart();
		int end = request.getEnd();
		List<Reward> rewards = request.getReward();
		
		
	}
	
	public void activityTopup(ActivityTopupReq request,PrintWriter writer,String reward4Md5){
		BaseResp response = new BaseResp();

		if (!request.checkMd5(reward4Md5)) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		int ctype = request.getCtype();
		int actid = request.getActid();
		String title = request.getTitle();
		String content = request.getContent();
		int start = request.getStart();
		int end = request.getEnd();
		List<Reward> rewards = request.getReward();
	}
	
	public void activityConsume(ActivityConsumeReq request,PrintWriter writer,String reward4Md5){
		BaseResp response = new BaseResp();

		if (!request.checkMd5(reward4Md5)) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		int ctype = request.getCtype();
		int actid = request.getActid();
		String title = request.getTitle();
		String content = request.getContent();
		int start = request.getStart();
		int end = request.getEnd();
		List<Reward> rewards = request.getReward();
	}
	
	public void activityTopupRank(ActivityTopupRankReq request,PrintWriter writer,String reward4Md5){
		BaseResp response = new BaseResp();

		if (!request.checkMd5(reward4Md5)) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		int ctype = request.getCtype();
		int actid = request.getActid();
		String title = request.getTitle();
		String content = request.getContent();
		int start = request.getStart();
		int end = request.getEnd();
		List<Reward> rewards = request.getReward();
		
		
	}
	
	public void activityConsumeRank(ActivityConsumeRankReq request,PrintWriter writer,String reward4Md5){
		BaseResp response = new BaseResp();

		if (!request.checkMd5(reward4Md5)) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		int ctype = request.getCtype();
		int actid = request.getActid();
		String title = request.getTitle();
		String content = request.getContent();
		int start = request.getStart();
		int end = request.getEnd();
		List<Reward> rewards = request.getReward();
	} 
	
	public void activityInner(ActivityInnerReq request,PrintWriter writer){
		BaseResp response = new BaseResp();

		if (!request.checkMd5()) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		int ctype = request.getCtype();
		int actid = request.getActid();
		int activityid = request.getActid();
		String title = request.getTitle();
		String content = request.getContent();
		int start = request.getStart();
		int end = request.getEnd();
		
		
	}
}
