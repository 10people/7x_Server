package com.qx.cdkey;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.CDKey.GetCDKeyAwardReq;
import qxmobile.protobuf.CDKey.GetCDKeyAwardResp;
import qxmobile.protobuf.ShouChong.AwardInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.store.Redis;
import com.qx.award.AwardMgr;
import com.qx.bag.BagMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.util.RandomUtil;
import com.qx.util.TableIDCreator;

public class CDKeyMgr {
	public static CDKeyMgr inst;
	public Logger logger = LoggerFactory.getLogger(CDKeyMgr.class);
	public static int KEY_LENGTH = 12;// 礼包码长度
	public Redis db = Redis.getInstance();

	public CDKeyMgr() {
		inst = this;
		initData();
	}

	public void initData() {

	}

	public synchronized void getCDKeyAward(int cmd, IoSession session, Builder builder) {
		GetCDKeyAwardReq.Builder req = (GetCDKeyAwardReq.Builder) builder;
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("君主不存在");
			return;
		}
		GetCDKeyAwardResp.Builder resp = GetCDKeyAwardResp.newBuilder();
		String key = req.getCdkey();
		key = key.replace(" ", "");
		key = key.replace("'", "");
		CDKeyInfo keyInfo = HibernateUtil.find(CDKeyInfo.class, "where cdkey='"
				+ key + "'");
		if (null == keyInfo) {
			resp.setResult(1);
			resp.setErrorMsg("礼包码错误");
			logger.error("君主{}兑换的礼包码  {} 错误", junZhu.id, key);
			session.write(resp.build());
			return;
		}
		
		if (keyInfo.getJzId() != 0
				|| keyInfo.getDeadDate().getTime() < new Date().getTime()) {
			resp.setResult(1);
			resp.setErrorMsg("礼包码已失效");
			logger.error("君主{}兑换的礼包码 {} 已失效", junZhu.id, key);
			session.write(resp.build());
			return;
		}
		if(keyInfo.getChanId()>0){
			List<CDKeyInfo> size = HibernateUtil.list(CDKeyInfo.class, 
					"where jzId="+junZhu.id+"and chanId="+keyInfo.getChanId());
			if(size.size()>0){
				resp.setResult(2);
				resp.setErrorMsg("您已领取过此类型礼包。");
				logger.error("君主{}兑换的礼包码 {} 类型重复 , {}", junZhu.id, key, size.size());
				session.write(resp.build());
				return;
			}
		}
		keyInfo.setJzId(junZhu.id);
		HibernateUtil.save(keyInfo);
		logger.info("{} 领取礼包 {},{},{}",
				junZhu.id, keyInfo.getKeyId(),keyInfo.getCdkey(),keyInfo.getAwards());
		resp.setResult(0);
		AwardMgr.inst.giveReward(session, keyInfo.getAwards(), junZhu);
		
		String[] awards = keyInfo.getAwards().split("#");
		for (String award : awards) {
			int awardType = Integer.parseInt(award.split(":")[0]);
			int awardId = Integer.parseInt(award.split(":")[1]);
			int awardNum = Integer.parseInt(award.split(":")[2]);
			AwardInfo.Builder awardInfo = AwardInfo.newBuilder();
			awardInfo.setAwardId(awardId);
			awardInfo.setAwardType(awardType);
			awardInfo.setAwardNum(awardNum);
			resp.addAwards(awardInfo);
		}
		session.write(resp.build());
	}

	/**
	 * @Title: generateCDKey
	 * @Description: 生成CDKey礼包
	 * @param channelId
	 *            渠道id
	 * @param deadDate
	 * 
	 * @param num
	 * @param awards
	 * @return
	 * @return String
	 * @throws
	 */
	public List<CDKeyInfo> generateCDKey(int chanId, Date deadDate, int num,
			String awards, String prefix) {
		// 1） 避免大小写和数字的痛苦切换过程，统一为N位小写字母，位数由后台确定；
		// 2） 礼包码在生成时可根据需求，设定对应的有效期截止日期，过期的礼包码输入后提示礼包码已失效。
		// 3） 不同渠道的礼包码需要进行标记，以确保研发可追踪到异常情况下的各种礼包码的来源。
		// 数值来源请峻宇/峰青配合
		Date now = new Date();
		final List<CDKeyInfo> keyList = new ArrayList<CDKeyInfo>();
		for (int i = 1; i <= num; i++) {
			String tmp = null;
			do {
				tmp = getRandomString(prefix);
			} while (HibernateUtil.find(CDKeyInfo.class, "where cdkey='" + tmp
					+ "'") != null);
			CDKeyInfo keyInfo = new CDKeyInfo();
			keyInfo.setKeyId(TableIDCreator.getTableID(CDKeyInfo.class, 1));
			keyInfo.setCdkey(tmp);
			keyInfo.setChanId(chanId);
			keyInfo.setDeadDate(deadDate);
			keyInfo.setCreateDate(now);
			keyInfo.setAwards(awards);
			keyList.add(keyInfo);
			HibernateUtil.insert(keyInfo);
		}
		return keyList;
	}

	public static String getRandomString(String prefix) { // length表示生成字符串的长度
		String base = "abcdefghijklmnopqrstuvwxyz";
		StringBuffer sb = new StringBuffer(prefix);
		int len = KEY_LENGTH - prefix.length();
		for (int i = 0; i < len; i++) {
			int number = RandomUtil.getRandomNum(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * @Title: getCDKeys
	 * @Description: 获取CDKey（不需要的条件传入null即可）
	 * @param jzId
	 *            根据使用的君主id查询
	 * @param startDate
	 *            创建日期大于的日期
	 * @param endDate
	 *            创建日期小于的日期
	 * @param chanId
	 *            渠道Id
	 * @param state
	 *            状态 ，0-可兑换,1-已兑换，2-已过期
	 * @return List<CDKeyInfo>
	 * @throws
	 */
	public List<CDKeyInfo> getCDKeys(Long jzId, Date startDate, Date endDate,
			String key, Integer chanId, Integer state) {
		List<CDKeyInfo> keyList = null;
		StringBuffer where = new StringBuffer("where 1=1 ");
		if (jzId != null) {
			where.append("and jzId=" + jzId + " ");
		}
		if (startDate != null) {
			where.append("and deadDate>=" + startDate + " ");
		}
		if (key != null) {
			where.append("and cdkey='" + key + "' ");
		}
		if (endDate != null) {
			where.append("and deadDate<=" + endDate + " ");
		}
		if (chanId != null) {
			where.append("and chanId=" + chanId + " ");
		}
		if (state != null) {
			switch (state) {
			case 0:// 未使用
				where.append("and jzId = 0 and deadDate>='"
						+ new Date().toLocaleString() + "' ");
				break;
			case 1:// 使用过
				where.append("and jzId != 0");
				break;
			case 2:// 过期的
				where.append("and jzId = 0 and deadDate<'"
						+ new Date().toLocaleString() + "' ");
				break;
			default:
				break;
			}
		}
		keyList = HibernateUtil.list(CDKeyInfo.class, where.toString());
		if (keyList == null || keyList.size() == 0) {
			return null;
		}
		return keyList;
	}

	/**
	 * @Title: deleteCDKey
	 * @Description: 根据条件删除CDKey（不需要的条件传入null即可）
	 * @param jzId
	 *            使用者君主id
	 * @param key
	 *            cdkey
	 * @param chanId
	 *            渠道id
	 * @param state
	 *            状态 ，0-可兑换,1-已兑换，2-已过期
	 * @return void
	 * @throws
	 */
	public List<CDKeyInfo> deleteCDKey(Long jzId, String key, Integer chanId,
			Integer state) {
		StringBuffer where = new StringBuffer("where 1=1 ");
		if (jzId != null) {
			where.append("and jzId=" + jzId + " ");
		}
		if (key != null) {
			where.append("and cdkey='" + key + "' ");
		}
		if (chanId != null) {
			where.append("and chanId=" + chanId + " ");
		}
		if (state != null) {
			switch (state) {
			case 0:// 未使用
				where.append("and jzId = 0 and deadDate>='"
						+ new Date().toLocaleString() + "' ");
				break;
			case 1:// 使用过
				where.append("and jzId != 0");
				break;
			case 2:// 过期的
				where.append("and jzId = 0 and deadDate<'"
						+ new Date().toLocaleString() + "' ");
				break;
			default:
				break;
			}
		}
		final List<CDKeyInfo> keyList = HibernateUtil.list(CDKeyInfo.class,
				where.toString());
		if (keyList == null || keyList.size() == 0) {
			return null;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				for (CDKeyInfo cdKeyInfo : keyList) {
					HibernateUtil.delete(cdKeyInfo);
				}
			}
		}).start();
		return keyList;
	}
}
