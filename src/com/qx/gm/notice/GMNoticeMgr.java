package com.qx.gm.notice;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.core.servlet.GMServlet;
import com.qx.gm.message.BaseResp;
import com.qx.gm.message.DoDelGMNoticeReq;
import com.qx.gm.message.QueryRoleListReq;
import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;
import com.qx.persistent.HibernateUtil;
import com.qx.world.BroadcastEntry;

/**
 * @ClassName: NoticeMgr
 * @Description: GM管理公告
 * @author 何金成
 * @date 2015年7月4日 下午6:12:52
 * 
 */
public class GMNoticeMgr {
	public static GMNoticeMgr inst;
	public Logger logger = LoggerFactory.getLogger(GMNoticeMgr.class);

	public GMNoticeMgr() {
		inst = this;
		initData();
	}

	public void initData() {

	}

	/**
	 * @throws UnsupportedEncodingException
	 * @Title: queryRoleList
	 * @Description: 系统发送公告信息
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doSendGMNotice(QueryRoleListReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}
		long start = request.getStart_time();
		long end = request.getEnd_time();
		BroadcastEntry be = HibernateUtil.find(BroadcastEntry.class,
				request.getNoticeid());
		if (be == null) {
			be = new BroadcastEntry();
			be.id = request.getNoticeid();
		}
		be.startTime = new Date(start * 1000);
		be.endTime = new Date(end * 1000);
		be.intervalMinutes = request.getInterval_time();
		be.open = true;
		try {
			be.content = new String(request.getContent().getBytes(), "UTF-8");
			// be.content = request.getContent();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (be.startTime == null || be.endTime == null || be.content == null) {
			logger.info("添加/修改系统公告参数错误");
			response.setCode(CodeUtil.PARAM_ERROR);
		} else {
			Throwable t = HibernateUtil.save(be);
			if (t == null) {
				logger.info("添加/修改系统公告成功，公告内容{}", be.content);
				response.setCode(CodeUtil.SUCCESS);
			} else {
				logger.info("添加/修改系统公告失败，公告内容{}", be.content);
				response.setCode(CodeUtil.PARAM_ERROR);
			}
		}
		GMServlet.write(response, writer);
	}

	/**
	 * @Title: doDelGMNotice
	 * @Description: 删除系统公告
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doDelGMNotice(DoDelGMNoticeReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			return;
		}

		int id = request.getNoticeid();
		if (id != 0) {
			BroadcastEntry be = HibernateUtil.find(BroadcastEntry.class,
					"where id=" + id);
			if (be != null) {
				Throwable t = HibernateUtil.delete(be);
				if (t == null) {
					response.setCode(CodeUtil.SUCCESS);
					logger.info("删除系统公告成功，公告内容{}", be.content);
				} else {
					response.setCode(CodeUtil.PARAM_ERROR);
					logger.info("删除系统公告成功，公告内容{}", be.content);
				}
			} else {
				response.setCode(CodeUtil.PARAM_ERROR);
			}
		} else {
			response.setCode(CodeUtil.PARAM_ERROR);
		}
		GMServlet.write(response, writer);
	}
}
