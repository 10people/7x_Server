package com.qx.friends;

import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.FriendsProtos.FriendIds;
import qxmobile.protobuf.FriendsProtos.FriendJunzhuInfo;
import qxmobile.protobuf.FriendsProtos.AddFriendReq;
import qxmobile.protobuf.FriendsProtos.FriendResp;
import qxmobile.protobuf.FriendsProtos.GetFriendListReq;
import qxmobile.protobuf.FriendsProtos.GetFriendListResp;
import qxmobile.protobuf.FriendsProtos.RemoveFriendReq;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.BaiZhan;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pvp.PvpBean;
import com.qx.pvp.PvpMgr;

public class FriendMgr {
	public static FriendMgr inst;
	private Logger logger = LoggerFactory.getLogger(FriendMgr.class);
	public static String CACHE_FRIEDNLIST_OF_JUNZHU = "FriendsList:id:";
	public static int MAX_FRIEND_NUM = 100;// 最大好友数量
	public static final int SUCCESS = 0;// 关注的好友成功
	public static final int ERROR_EXIST = 101;// 关注的好友已存在
	public static final int ERROR_SELF = 102;// 不能关注自己
	public static final int ERROR_MAXNUM = 103;// 好友数达到上限
	public static final int ERROR_JUNZHU_NULL = 104;// 君主不存在
	public static final int ERROR_NOT_FRIEND = 105;// 未关注的君主

	public FriendMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		// TODO Auto-generated method stub
	}

	/**
	 * 单向关注好友
	 * 
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void addFriend(int cmd, IoSession session, Builder builder) {
		AddFriendReq.Builder request = (qxmobile.protobuf.FriendsProtos.AddFriendReq.Builder) builder;
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			sendError(session, cmd, "未发现君主");
			logger.error("cmd:{},未发现君主", cmd);
			return;
		}
		long friendJunzhuId = request.getJunzhuId();
		FriendResp.Builder response = FriendResp.newBuilder();
		JunZhu friendJunZhu = HibernateUtil.find(JunZhu.class, friendJunzhuId);
		if (friendJunZhu == null) {
			response.setJunzhuId(friendJunzhuId);
			response.setResult(ERROR_JUNZHU_NULL);
			logger.info("要关注的君主id {} 不存在", friendJunzhuId);
			writeByProtoMsg(session, PD.S_FRIEND_ADD_RESP, response);
			return;
		}
		if (junZhu.id == friendJunzhuId) {
			logger.info("不能关注自己");
			response.setJunzhuId(friendJunzhuId);
			response.setResult(ERROR_SELF);
			writeByProtoMsg(session, PD.S_FRIEND_ADD_RESP, response);
			return;
		}
		double tmpScore = Redis.getInstance().zscore(
				CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id, "" + friendJunzhuId);
		Boolean exits = tmpScore == -1 ? false : true;
		if (exits) {// 好友已存在
			logger.info("好友已关注，请不要重复关注");
			response.setJunzhuId(friendJunzhuId);
			response.setResult(ERROR_EXIST);
			writeByProtoMsg(session, PD.S_FRIEND_ADD_RESP, response);
			return;
		}
		long friendNum = Redis.getInstance().zcard_(
				CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id);
		if (friendNum >= MAX_FRIEND_NUM) {// 好友达到人数上限
			logger.info("好友数达到上限{}人", MAX_FRIEND_NUM);
			response.setJunzhuId(friendJunzhuId);
			response.setResult(ERROR_MAXNUM);
			writeByProtoMsg(session, PD.S_FRIEND_ADD_RESP, response);
			return;
		}
		double addScore = 0;
		if (Redis.getInstance().zcard_(CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id) > 0) {
			addScore = Redis.getInstance().zscore(
					CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id,
					Redis.getInstance()
							.ztop(CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id, 1)
							.iterator().next());// 获取目前最高的score
		} else {// 好友数为0
			addScore = 1;
		}
		Redis.getInstance().zadd(CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id,
				(int) (addScore + 1), "" + friendJunzhuId);
		response.setResult(SUCCESS);
		response.setJunzhuId(friendJunzhuId);
		logger.info("{}将{}添加进好友", junZhu.name, friendJunZhu.name);
		// if (Redis.getInstance().sexist(CACHE_FRIEDNLIST_OF_JUNZHU +
		// junZhu.id,
		// "" + friendJunzhuId)) {// 如果好友存在屏蔽玩家列表里
		// logger.info("{}存在{}的屏蔽玩家列表里", friendJunZhu.name, junZhu.name);
		// Redis.getInstance().sremove(
		// ChatMgr.CACHE_BLACKLIST_OF_JUNZHU + junZhu.id,
		// "" + friendJunzhuId);
		// logger.info("{}将{}添加从黑名单中移除", junZhu.name, friendJunZhu.name);
		// }
		writeByProtoMsg(session, PD.S_FRIEND_ADD_RESP, response);
	}

	/**
	 * 取消关注好友
	 * 
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void removeFriend(int cmd, IoSession session, Builder builder) {
		RemoveFriendReq.Builder request = (qxmobile.protobuf.FriendsProtos.RemoveFriendReq.Builder) builder;
		long removeJunzhuId = request.getJunzhuId();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			sendError(session, cmd, "未发现君主");
			logger.error("cmd:{},未发现君主", cmd);
			return;
		}
		FriendResp.Builder response = FriendResp.newBuilder();
		JunZhu removeJunzhu = HibernateUtil.find(JunZhu.class, removeJunzhuId);
		if (removeJunzhu == null) {
			logger.error("找不到君主,id:{}", removeJunzhu);
			response.setJunzhuId(removeJunzhuId);
			response.setResult(ERROR_JUNZHU_NULL);
			session.write(response.build());
			return;
		}
		double tmpScore = Redis.getInstance().zscore(
				CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id, "" + removeJunzhuId);
		Boolean exits = tmpScore == -1 ? false : true;
		if (!exits) {
			logger.error("君主:{}不在君主:{}的好友里", removeJunzhu.name, junZhu.name);
			response.setJunzhuId(removeJunzhuId);
			response.setResult(ERROR_NOT_FRIEND);
			session.write(response.build());
			return;
		}
		Redis.getInstance().zrem(CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id,
				"" + removeJunzhuId);
		response.setJunzhuId(removeJunzhuId);
		response.setResult(SUCCESS);
		logger.info("{}将{}添加从好友中移除", junZhu.name, removeJunzhu.name);
		writeByProtoMsg(session, PD.S_FRIEND_REMOVE_RESP, response);
	}

	/**
	 * 获取好友列表
	 * 
	 * @param cmd
	 * @param session
	 */
	public void getFriendList(int cmd, IoSession session, Builder builder) {
		GetFriendListReq.Builder request = (qxmobile.protobuf.FriendsProtos.GetFriendListReq.Builder) builder;
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			sendError(session, cmd, "未发现君主");
			logger.error("cmd:{},未发现君主", cmd);
			return;
		}
		int pageNo = request.getPageNo();
		int pageSize = request.getPageSize();
		GetFriendListResp.Builder response = GetFriendListResp.newBuilder();
		if (Redis.getInstance().exist_(CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id)) {// 有好友
			Set<String> ids = null;
			if(pageNo==0&&pageSize==0){// 查询全部好友
				ids = Redis.getInstance().ztop(
						CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id, 0, MAX_FRIEND_NUM);
			} else{
				int start = pageSize * (pageNo - 1);
				int end = start + pageSize;
				ids = Redis.getInstance().ztop(
						CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id, start, end);
			}
			if (ids != null && ids.size() > 0) {
				for (String id : ids) {
					JunZhu friend = HibernateUtil.find(JunZhu.class,
							Long.parseLong(id));
					FriendJunzhuInfo.Builder fjz = FriendJunzhuInfo
							.newBuilder();
					fjz.setGuojia(friend.guoJiaId);
					fjz.setIconId(friend.roleId);
					fjz.setJunXian("1");
					{
						PvpBean bean = HibernateUtil.find(PvpBean.class,
								friend.id);
						if (bean != null) {
							// BaiZhan bz = PvpMgr.inst.baiZhanMap
							// .get(bean.junXianLevel);
							// String jxStr = bz == null ? "???" : HeroService
							// .getNameById(bz.name);
							// fjz.setJunXian(jxStr);
							fjz.setJunXian(String.valueOf(bean.junXianLevel));
						}
					}
					fjz.setName(friend.name);
					fjz.setLevel(friend.level);
					fjz.setOwnerid(friend.id);
					fjz.setVipLv(friend.vipLevel);
					fjz.setZhanLi(PvpMgr.inst.getZhanli(friend));
					AlliancePlayer member = HibernateUtil.find(
							AlliancePlayer.class, friend.id);
					if (member == null || member.lianMengId <= 0) {
						fjz.setLianMengName("");
					} else {
						AllianceBean alnc = HibernateUtil.find(
								AllianceBean.class, member.lianMengId);
						fjz.setLianMengName(alnc == null ? "" : alnc.name);
					}
					logger.info("获取到好友的君主id {}，name {}", friend.id, friend.name);
					response.addFriends(fjz.build());
				}
			}
			response.setFriendCount((int) Redis.getInstance().zcard_(
					CACHE_FRIEDNLIST_OF_JUNZHU + junZhu.id));
		} else {
			response.setFriendCount(0);
		}
		response.setFriendMax(MAX_FRIEND_NUM);
		writeByProtoMsg(session, PD.S_FRIEND_RESP, response);
	}

	/**
	 * 发送指定协议号的消息
	 * 
	 * @param session
	 * @param prototype
	 * @param response
	 * @return
	 */
	private void writeByProtoMsg(IoSession session, int prototype,
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
	private void sendError(IoSession session, int cmd, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	public void getFriendIds(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		Set<String> ids = Redis.getInstance().zrange(CACHE_FRIEDNLIST_OF_JUNZHU + junZhuId);
		FriendIds.Builder ret = FriendIds.newBuilder();
		ret.addAllIds(ids);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_GET_FRIEND_IDS;
		msg.builder = ret;
		session.write(msg);
	}
}
