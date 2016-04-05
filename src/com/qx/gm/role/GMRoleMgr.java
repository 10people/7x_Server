package com.qx.gm.role;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.core.servlet.GMServlet;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.BaseItem;
import com.qx.account.AccountManager;
import com.qx.alliance.AllianceMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.gm.message.BaseResp;
import com.qx.gm.message.ConsumeRecords;
import com.qx.gm.message.DoBanUserDownReq;
import com.qx.gm.message.DoBanUserReq;
import com.qx.gm.message.DoBanUserSpeakReq;
import com.qx.gm.message.DoLiftBanUserReq;
import com.qx.gm.message.DoLiftBanUserSpeakReq;
import com.qx.gm.message.OperateConsumeReq;
import com.qx.gm.message.OperateConsumeResp;
import com.qx.gm.message.OperateTopupReq;
import com.qx.gm.message.OperateTopupResp;
import com.qx.gm.message.QueryRoleInfoReq;
import com.qx.gm.message.QueryRoleInfoResp;
import com.qx.gm.message.QueryRoleStatusReq;
import com.qx.gm.message.QueryRoleStatusResp;
import com.qx.gm.message.RoleBackpack;
import com.qx.gm.message.RoleEquip;
import com.qx.gm.message.RolePet;
import com.qx.gm.message.TopupRecords;
import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.HibernateUtil;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoInfo;

/**
 * @ClassName: RoleMgr
 * @Description: GM管理玩家信息
 * @author 何金成
 * @date 2015年7月4日 下午6:13:11
 * 
 */
public class GMRoleMgr {
	public static GMRoleMgr inst;
	private Logger logger = LoggerFactory.getLogger(GMRoleMgr.class);
	public static String CACHE_ROLE_BAN_USER = "GMRoleBanUser:id:";// 角色封停信息
	public static String CACHE_ROLE_BAN_USER_SPEAK = "GMRoleBanUserSpeak:id:";// 角色禁言信息

	public GMRoleMgr() {
		inst = this;
		initData();
	}

	public void initData() {

	}

	/**
	 * @Title: operateTopup
	 * @Description: 充值查询
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void operateTopup(OperateTopupReq request, PrintWriter writer) {
		OperateTopupResp response = new OperateTopupResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			return;
		}

		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), request.getRoleid());
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		String uin = null;
		if (request.getUin().length() > 0) {// 根据账号id查找
			uin = request.getUin();
		}
		if (request.getRolename().length() != 0 && uin == null) {// 按角色名查找，查询账号名
			uin = String.valueOf(getAccountIdByJunZhuId(junZhu.id));
		}
		if (request.getRoleid().length() != 0 && uin == null) {// 按角色id查找
			uin = String.valueOf(getAccountIdByJunZhuId(Long.valueOf(request
					.getRoleid())));
		}

		response.setUin(uin);
		response.setRolename(junZhu.name);

		StringBuilder sBuilder = new StringBuilder(
				"where 1=1 and costMoney>0 and type=" + YBType.YB_VIP_CHONGZHI
						+ " and ownerid=" + junZhu.id + " ");
		String startStr = request.getStart();
		String endStr = request.getEnd();
		long start = 0;
		long end = 0;
		if (!startStr.equals("false")) {
			start = Long.parseLong(startStr);
		}
		if (!endStr.equals("false")) {
			end = Long.parseLong(endStr);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (start == 0 && end == 0) {// 开始时间结束时间都没选
		} else if (start == 0 && end != 0) {// 只选结束时间
			sBuilder.append(" and timestamp<='"
					+ sdf.format(new Date(end * 1000)) + "'");
		} else if (start != 0 && end == 0) {// 只选开始时间
			sBuilder.append(" and timestamp>='"
					+ sdf.format(new Date(start * 1000)) + "'");
		} else {// 同时选了开始时间和结束时间
			sBuilder.append(" and timestamp>='"
					+ sdf.format(new Date(start * 1000)) + "' and timestamp<='"
					+ sdf.format(new Date(end * 1000)) + "'");
		}
		List<YuanBaoInfo> yuanbaoList = HibernateUtil.list(YuanBaoInfo.class,
				sBuilder.toString());
		List<TopupRecords> recordList = new ArrayList<TopupRecords>();
		for (YuanBaoInfo yuanbao : yuanbaoList) {
			TopupRecords records = new TopupRecords();
			records.setMoney(yuanbao.getCostMoney());
			if (yuanbao.getCostMoney() == 0 && yuanbao.getYuanbaoChange() > 0) {
				// 消费了金钱没有元宝变化则充值失败
				records.setStatus(yuanbao.getReason());
			} else {
				records.setStatus("充值成功");
			}
			records.setTop_time(yuanbao.getTimestamp().toLocaleString());
			records.setVcoin(yuanbao.getYuanbaoChange());
			recordList.add(records);
		}
		response.setRecords(recordList);
		GMServlet.write(response, writer);
		return;
	}

	/**
	 * @Title: operateConsume
	 * @Description: 元宝消费查询
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void operateConsume(OperateConsumeReq request, PrintWriter writer) {
		OperateConsumeResp response = new OperateConsumeResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			return;
		}

		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), null);
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		String uin = null;
		if (request.getUin().length() > 0) {// 根据账号id查找
			uin = request.getUin();
		}
		if (request.getRolename().length() != 0 && uin == null) {// 按角色名查找，查询账号名
			uin = String.valueOf(getAccountIdByJunZhuId(junZhu.id));
		}

		response.setUin(uin);
		response.setRolename(junZhu.name);

		StringBuilder sBuilder = new StringBuilder(
				"where 1=1 and yuanbaoChange<0 and ownerid=" + junZhu.id + " ");
		String startStr = request.getStart();
		String endStr = request.getEnd();
		long start = 0;
		long end = 0;
		if (!startStr.equals("false")) {
			start = Long.parseLong(startStr);
		}
		if (!endStr.equals("false")) {
			end = Long.parseLong(endStr);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (start == 0 && end == 0) {// 开始时间结束时间都没选
		} else if (start == 0 && end != 0) {// 只选结束时间
			sBuilder.append(" and timestamp<='"
					+ sdf.format(new Date(end * 1000)) + "'");
		} else if (start != 0 && end == 0) {// 只选开始时间
			sBuilder.append(" and timestamp>='"
					+ sdf.format(new Date(start * 1000)) + "'");
		} else {// 同时选了开始时间和结束时间
			sBuilder.append(" and timestamp>='"
					+ sdf.format(new Date(start * 1000)) + "' and timestamp<='"
					+ sdf.format(new Date(end * 1000)) + "'");
		}
		List<YuanBaoInfo> yuanbaoList = HibernateUtil.list(YuanBaoInfo.class,
				sBuilder.toString());
		List<ConsumeRecords> recordList = new ArrayList<ConsumeRecords>();
		for (YuanBaoInfo yuanbao : yuanbaoList) {
			ConsumeRecords records = new ConsumeRecords();
			records.setDttm(yuanbao.getTimestamp().toLocaleString());
			records.setFuncname(yuanbao.getReason());
			records.setMoney(String.valueOf(Math.abs(yuanbao.getYuanbaoChange())));
			records.setPrice(yuanbao.getPrice());
			recordList.add(records);
		}
		response.setRecords(recordList);
		GMServlet.write(response, writer);
		return;
	}

	/**
	 * @Title: queryRoleinfo
	 * @Description: 查询角色信息
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void queryRoleinfo(QueryRoleInfoReq request, PrintWriter writer) {
		QueryRoleInfoResp response = new QueryRoleInfoResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		// 查询君主信息
		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), null);
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		String uin = String.valueOf(getAccountIdByJunZhuId(junZhu.id));

		response.setUin(uin);
		response.setRid((int) junZhu.id);
		response.setName(junZhu.name);
		// 按照模型id判断性别
		if (junZhu.roleId == 1 || junZhu.roleId == 2) {// 豪杰模型，男性
			response.setGender("男");
		} else if (junZhu.roleId == 3 || junZhu.roleId == 4) {// 郡主模型，女性
			response.setGender("女");
		}
		// TODO 查询职业（暂无职业）
		response.setJob(1);
		// 查询联盟
		response.setGang(AllianceMgr.inst.getAlliance(junZhu));
		response.setMoney(junZhu.yuanBao);
		response.setCopper(junZhu.tongBi);
		response.setTopup(getTopup(junZhu));
		response.setLevel(junZhu.level);
		response.setViplevel(junZhu.vipLevel);
		response.setExp((int) junZhu.exp);
		// 注册时间和上次登录时间记录
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, junZhu.id);
		if (null == playerTime) {
			playerTime = new PlayerTime(junZhu.id);
			HibernateUtil.insert(playerTime);
		}
		response.setRegistertime(null == playerTime.getCreateRoleTime() ? new Date()
				.toLocaleString() : playerTime.getCreateRoleTime()
				.toLocaleString());
		response.setLastlogintime(null == playerTime.getLoginTime() ? new Date()
				.toLocaleString() : playerTime.getLoginTime().toLocaleString());
		// 查找背包
		response.setBackpack(getBackpackList(junZhu));
		// 查找装备
		response.setEquip(getEquipList(junZhu));
		// TODO 没有宠物信息
		response.setPet(new ArrayList<RolePet>());
		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
	}

	/**
	 * @Title: queryRoleStatuse
	 * @Description: 查询角色状态
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void queryRoleStatus(QueryRoleStatusReq request, PrintWriter writer) {
		QueryRoleStatusResp response = new QueryRoleStatusResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		// 查询君主信息
		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), null);
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		String uin = String.valueOf(getAccountIdByJunZhuId(junZhu.id));

		response.setUin(uin);
		response.setName(junZhu.name);
		response.setLevel(junZhu.level);
		response.setAreaid(request.getZone());
		response.setTopup(getTopup(junZhu));

		String ftState = Redis.getInstance().get(
				CACHE_ROLE_BAN_USER + junZhu.id);
		String jyState = Redis.getInstance().get(
				CACHE_ROLE_BAN_USER_SPEAK + junZhu.id);
		if (null == ftState) {
			ftState = "0";
		}
		if (null == jyState) {
			jyState = "0";
		}
		if (ftState.equals("0") && jyState.equals("0")) {// 正常
			response.setStatus(1);
		} else if (!ftState.equals("0") && jyState.equals("0")) {// 封停
			response.setStatus(2);
		} else if (ftState.equals("0") && !jyState.equals("0")) {// 禁言
			response.setStatus(3);
		} else {// 封停又禁言
			response.setStatus(2);
		}

		int isOnline = isJunzhuOnline(junZhu);
		response.setOnline(isOnline);

		long minutes = getTotalOnlineTime(junZhu);

		response.setOnlinetime(minutes);
		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
	}

	/**
	 * @Title: doBanUserSpeak
	 * @Description: 禁言处理
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doBanUserSpeak(DoBanUserSpeakReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), null);
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		String status = Redis.getInstance().get(
				CACHE_ROLE_BAN_USER_SPEAK + junZhu.id);
		if (status != null && !status.equals("0")) {// 已经处于禁言状态
			response.setCode(CodeUtil.ALREADY_JINYAN);
			GMServlet.write(response, writer);
			return;
		}

		int times = request.getTimes();
		// 设置禁言时间
		Redis.getInstance().set(CACHE_ROLE_BAN_USER_SPEAK + junZhu.id,
				(new Date().getTime() + times * 1000) + "");

		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
		return;
	}

	/**
	 * @Title: doBanUser
	 * @Description: 封停账号
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doBanUser(DoBanUserReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), null);
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		kickRole(junZhu);// 封停完了踢下线

		String status = Redis.getInstance()
				.get(CACHE_ROLE_BAN_USER + junZhu.id);
		if (status != null && !status.equals("0")) {// 已经处于封停状态
			response.setCode(CodeUtil.ALREADY_FENGTING);
			GMServlet.write(response, writer);
			return;
		}

		int times = request.getTimes();
		// 设置封停时间
		Redis.getInstance().set(CACHE_ROLE_BAN_USER + junZhu.id,
				(new Date().getTime() + times * 1000) + "");

		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
		return;
	}

	/**
	 * @Title: doLiftBanUser
	 * @Description: 解除封停账号
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doLiftBanUser(DoLiftBanUserReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), null);
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		// 封停时间清空
		Redis.getInstance().set(CACHE_ROLE_BAN_USER + junZhu.id, "0");

		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
		return;
	}

	/**
	 * @Title: doLiftBanUserSpeak
	 * @Description: 解除禁言
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doLiftBanUserSpeak(DoLiftBanUserSpeakReq request,
			PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), null);
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}

		// 封停时间清空
		Redis.getInstance().set(CACHE_ROLE_BAN_USER_SPEAK + junZhu.id, "0");

		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
		return;
	}

	/**
	 * @Title: doBanUserDown
	 * @Description: 踢下线
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doBanUserDown(DoBanUserDownReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5()) {// MD5验证
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		JunZhu junZhu = getJunzhu(request.getZone(), request.getUin(),
				request.getRolename(), null);
		if (null == junZhu) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		}
		boolean flag = kickRole(junZhu);
		if (flag) {
			response.setCode(CodeUtil.SUCCESS);
			GMServlet.write(response, writer);
			return;
		}
		response.setCode(CodeUtil.JUNZHU_OFFLINE);
		GMServlet.write(response, writer);
		return;
	}

	/**
	 * @Title: kickRole
	 * @Description: 踢角色下线
	 * @param junZhu
	 * @return
	 * @return boolean
	 * @throws
	 */
	public boolean kickRole(JunZhu junZhu) {
		IoSession session = AccountManager.getIoSession(junZhu.id);
		if (session != null) {
			// 关闭session，移出sessionMap
			session.close(false);
			AccountManager.sessionMap.remove(junZhu.id);
			return true;
		}
		return false;
	}

	/**
	 * @Title: isJunzhuOnline
	 * @Description: 君主是否在线
	 * @param junZhu
	 * @return
	 * @return int 0-不在线 1-在线
	 * @throws
	 */
	public int isJunzhuOnline(JunZhu junZhu) {
		IoSession session = AccountManager.getIoSession(junZhu.id);
		if (null == session) {
			return 0;
		}
		return 1;
	}

	/**
	 * @Title: getBackpackList
	 * @Description: 获取背包列表
	 * @param junZhu
	 * @return
	 * @return List<RoleBackpack>
	 * @throws
	 */
	public List<RoleBackpack> getBackpackList(JunZhu junZhu) {
		List<RoleBackpack> bagPackList = new ArrayList<RoleBackpack>();
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		List<BagGrid> bagList = bag.grids;
		for (BagGrid bagGrid : bagList) {
			if (bagGrid != null) {
				BaseItem o = TempletService.itemMap.get(bagGrid.itemId);
				RoleBackpack roleBackpack = new RoleBackpack();
				if (o == null) {
					logger.error("背包中的物品ID错误 {}", bagGrid.itemId);
					roleBackpack.setId(bagGrid.itemId);
				} else {
					roleBackpack.setId(bagGrid.itemId);
					roleBackpack.setNum(bagGrid.cnt);
					roleBackpack.setName(HeroService.getNameById(o.getName()));
				}
				if (roleBackpack.getId() != 0) {
					bagPackList.add(roleBackpack);
				}
			}
		}
		return bagPackList;
	}

	/**
	 * @Title: getEquipList
	 * @Description: 获取装备列表
	 * @param junZhu
	 * @return
	 * @return List<RoleEquip>
	 * @throws
	 */
	public List<RoleEquip> getEquipList(JunZhu junZhu) {
		List<RoleEquip> equipList = new ArrayList<RoleEquip>();
		Bag<EquipGrid> equip = EquipMgr.inst.loadEquips(junZhu.id);
		List<EquipGrid> equipGridList = equip.grids;
		for (EquipGrid equipGrid : equipGridList) {
			if (equipGrid != null) {
				BaseItem o = TempletService.itemMap.get(equipGrid.itemId);
				RoleEquip roleEquip = new RoleEquip();
				if (o == null) {
					logger.error("背包中的物品ID错误 {}", equipGrid.itemId);
					roleEquip.setId(equipGrid.itemId);
				} else {
					roleEquip.setId(equipGrid.itemId);
					roleEquip.setLevel(o.getPinZhi());
					roleEquip.setName(HeroService.getNameById(o.getName()));
				}
				equipList.add(roleEquip);
			}
		}
		return equipList;
	}

	/**
	 * @Title: getTotalOnlineTime
	 * @Description: 获取累计在线时间（分钟）
	 * @param junZhu
	 * @return
	 * @return long
	 * @throws
	 */
	private long getTotalOnlineTime(JunZhu junZhu) {
		long minutes = 0;
		int isOnline = isJunzhuOnline(junZhu);
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, junZhu.id);
		if (null == playerTime) {
			playerTime = new PlayerTime(junZhu.id);
			HibernateUtil.insert(playerTime);
		}
		long loginTime = 0;
		String loginTimeStr = Redis.getInstance().get(
				AccountManager.CACHE_ONLINETIME + junZhu.id);
		loginTime = (null == loginTimeStr) ? new Date().getTime() : Long
				.valueOf(loginTimeStr);
		long nowTime = new Date().getTime();
		if (0 == playerTime.getTotalOnlineTime()) {// 尚未统计在线时间，直接计算
			minutes = ((nowTime - loginTime) % (1000 * 60 * 60)) / (1000 * 60);
		} else {
			if (isOnline == 1) {// 当前在线，获取db记录时间加上本次上线时间
				minutes = ((playerTime.getTotalOnlineTime() + (nowTime - loginTime)) % (1000 * 60 * 60))
						/ (1000 * 60);
			} else {// 当前不在线，直接获取db记录时间
				minutes = (playerTime.getTotalOnlineTime() % (1000 * 60 * 60))
						/ (1000 * 60);
			}
		}
		return minutes;
	}

	/**
	 * @Title: getTopup
	 * @Description: 获取累计充值
	 * @return
	 * @return int
	 * @throws
	 */
	public int getTopup(JunZhu junZhu) {
		// 计算累计充值
		List<YuanBaoInfo> yuanbaoList = HibernateUtil.list(YuanBaoInfo.class,
				"where ownerid=" + junZhu.id + " and type="
						+ YBType.YB_VIP_CHONGZHI + " and yuanbaoChange>0");
		int topup = 0;
		for (YuanBaoInfo yuanbao : yuanbaoList) {
			topup += yuanbao.getYuanbaoChange();
		}
		return topup;
	}

	/**
	 * @Title: getJunzhu
	 * @Description: 查询君主信息(按账号名或角色名查找)
	 * @param zone
	 * @param uin
	 * @param rolename
	 * @param roleid
	 * @param writer
	 * @return
	 * @return JunZhu
	 * @throws
	 */
	public JunZhu getJunzhu(int zone, String uin, String rolename,
			String roleid) {
		JunZhu junZhu = null;
		if (rolename.length() == 0 && uin.length() == 0) {// 没有查询信息
			if (null == roleid) {
				return null;
			} else if (roleid.length() == 0) {
				return null;
			}
		}
		if (uin.length() > 0) {// 根据账号id查找
			junZhu = HibernateUtil.find(JunZhu.class,
					getJunZhuIdByAccountId(Long.valueOf(uin)));
		}
		if (rolename.length() > 0 && junZhu == null) {// 按角色名查找，如果账号已经找到则不用查找
			junZhu = HibernateUtil.find(JunZhu.class, "where name='" + rolename
					+ "'", false);
		}
		if (roleid != null && roleid.length() > 0 && junZhu == null) {// 按角色id查找，如果账号已找到则不用查找
			junZhu = HibernateUtil.find(JunZhu.class, Long.valueOf(roleid));
		}
		if (null == junZhu) {// 君主不存在
			return null;
		}
		if (!String.valueOf(junZhu.id).endsWith(String.valueOf(zone))) {// 角色不属于这个大区
			return null;
		}
		return junZhu;
	}

	/**
	 * @Title: getJunZhuIdByAccountId
	 * @Description: 根据账号id获取君主id
	 * @param accountId
	 * @return
	 * @return long
	 * @throws
	 */
	public static long getJunZhuIdByAccountId(long accountId) {
		return accountId * 1000 + GameServer.serverId;
	}

	/**
	 * @Title: getAccountIdByJunZhuId
	 * @Description: 根据君主id获得账号id
	 * @param junZhuId
	 * @return
	 * @return long
	 * @throws
	 */
	public static long getAccountIdByJunZhuId(long junZhuId) {
		return (junZhuId - GameServer.serverId) / 1000;
	}

	/**
	 * @Title: checkGMFengting
	 * @Description: 检查君主是否处于封停状态
	 * @param junZhuId
	 * @return
	 * @return boolean
	 * @throws
	 */
	public static boolean checkGMFengting(long junZhuId) {
		String status = Redis.getInstance().get(CACHE_ROLE_BAN_USER + junZhuId);
		if (null == status) {
			return false;
		}
		if (!status.equals("0")) {// 处于封停状态
			if (new Date().getTime() > Long.valueOf(status)) {// 如果封停期限已过
				Redis.getInstance().set(CACHE_ROLE_BAN_USER + junZhuId, "0");
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @Title: checkGMJinyan
	 * @Description: 检查君主是否处于禁言状态
	 * @param junZhuId
	 * @return
	 * @return boolean
	 * @throws
	 */
	public static boolean checkGMJinyan(long junZhuId) {
		String status = Redis.getInstance().get(
				CACHE_ROLE_BAN_USER_SPEAK + junZhuId);
		if (null == status) {
			return false;
		}
		if (!status.equals("0")) {// 处于禁言状态
			if (new Date().getTime() > Long.valueOf(status)) {// 如果禁言期限已过
				Redis.getInstance().set(CACHE_ROLE_BAN_USER_SPEAK + junZhuId,
						"0");
				return false;
			}
			return true;
		}
		return false;
	}
}
