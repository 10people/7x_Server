package com.qx.gm.log;

import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.core.servlet.GMServlet;
import com.qx.gm.message.OperateLogReq;
import com.qx.gm.message.OperateLogResp;
import com.qx.gm.role.GMRoleMgr;
import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;
import com.qx.junzhu.JunZhu;

/**
 * @ClassName: LogMgr
 * @Description: GM管理Log
 * @author 何金成
 * @date 2015年7月4日 下午6:12:27
 * 
 */
public class GMLogMgr {
	public static GMLogMgr inst;
	private Logger logger = LoggerFactory.getLogger(GMLogMgr.class);

	public GMLogMgr() {
		inst = this;
		initData();
	}

	public void initData() {

	}

	/**
	 * @Title: operateLog
	 * @Description: 日志查询
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void operateLog(OperateLogReq request, PrintWriter writer) {
		OperateLogResp response = new OperateLogResp();

		// MD5验证
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(request.getType()).append(request.getFirm())
				.append(request.getZone()).append(request.getUin())
				.append(request.getRoleid()).append(request.getRolename())
				.append(request.getSystem()).append(request.getAction())
				.append(request.getStart()).append(request.getEnd())
				.append(CodeUtil.MD5_KEY);
		if (!MD5Util.checkMD5(sBuffer.toString(), request.getMd5())) {// MD5验证
			return;
		}

		JunZhu junZhu = GMRoleMgr.inst.getJunzhu(request.getZone(),
				request.getUin(), request.getRolename(), request.getRoleid());
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		String uin = null;
		if (request.getUin().length() > 0) {// 根据账号id查找
			uin = request.getUin();
		}
		if (request.getRolename().length() != 0 && uin != null) {// 按角色名查找，查询账号名
			uin = String.valueOf(GMRoleMgr.getAccountIdByJunZhuId(junZhu.id));
		}
		if (request.getRoleid().length() != 0 && uin != null) {// 按角色id查找
			uin = String.valueOf(GMRoleMgr.getAccountIdByJunZhuId(Long
					.valueOf(request.getRoleid())));
		}

		response.setUin(uin);
		response.setRolename(junZhu.name);
		// TODO Records为操作行为数据数组，具体包括内容由负责的产品和策划以及相关服务器人员人员制定。
		// response.setDttm(dttm);
		// response.setRecords(records);
		GMServlet.write(response, writer);
	}

}
