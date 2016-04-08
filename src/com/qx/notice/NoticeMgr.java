package com.qx.notice;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.NoticeProtos.GetVersionNoticeResp;
import qxmobile.protobuf.NoticeProtos.VersionNoticeInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.boot.GameServer;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.persistent.HibernateUtil;

/**
 * @ClassName: NoticeMgr
 * @Description: 公告管理
 * @author 何金成
 * @date 2015年7月8日 上午11:24:27
 * 
 */
public class NoticeMgr {
	public static NoticeMgr instance;
	public Logger logger = LoggerFactory.getLogger(NoticeMgr.class);

	public NoticeMgr() {
		instance = this;
		initData();
	}

	public void initData() {

	}

	/**
	 * @Title: getVersionNotice
	 * @Description: 获取版本公告
	 * @param cmd
	 * @param session
	 * @param builder
	 * @return void
	 * @throws
	 */
	public void getVersionNotice(int cmd, IoSession session, Builder builder) {
		GetVersionNoticeResp.Builder response = GetVersionNoticeResp
				.newBuilder();
		List<VersionNotice> noticeList = HibernateUtil.list(
				VersionNotice.class, "where serverId=" + GameServer.serverId
						+ " order by notice_order ASC");// 按order升序排列
		for (VersionNotice notice : noticeList) {
			VersionNoticeInfo.Builder versionNotice = VersionNoticeInfo
					.newBuilder();
			versionNotice.setTitle(notice.getTitle());
			if (null != notice.getTag()) {
				versionNotice.setTag(notice.getTag());
			}
			versionNotice.setContent(notice.getContent());
			versionNotice.setOrder(notice.getNotice_order());
			response.addNotice(versionNotice);
		}
		writeByProtoMsg(session, PD.S_GET_VERSION_NOTICE_RESP, response);
	}

	/**
	 * 发送指定协议号的消息
	 * 
	 * @param session
	 * @param prototype
	 * @param response
	 * @return
	 */
	public void writeByProtoMsg(IoSession session, int prototype,
			Builder response) {
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = prototype;
		msg.builder = response;
		logger.info("发送协议号为：{}", prototype);
		session.write(msg);
	}

	/**
	 * 发送错误消息
	 * 
	 * @param session
	 * @param cmd
	 * @param msg
	 */
	public void sendError(IoSession session, int cmd, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}
}
