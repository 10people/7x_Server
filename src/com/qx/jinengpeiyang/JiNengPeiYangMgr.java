package com.qx.jinengpeiyang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.JiNengPeiYang.GetJiNengPeiYangQuality;
import qxmobile.protobuf.JiNengPeiYang.JiNengQuality;
import qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengReq;
import qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.JiNengPeiYang;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;

public class JiNengPeiYangMgr {
	public static JiNengPeiYangMgr inst;
	public Logger logger = LoggerFactory.getLogger(JiNengPeiYangMgr.class);
	public Map<Integer, Map<Integer, Map<Integer, JiNengPeiYang>>> wuqiMap = new HashMap<Integer, Map<Integer, Map<Integer, JiNengPeiYang>>>();
	public Map<Integer, JiNengPeiYang> jiNengPeiYangMap = new HashMap<Integer, JiNengPeiYang>();
	public static final String CACHE_JINENGPEIYANG = "jinengpeiyang:";
	public Redis redis = Redis.getInstance();

	public JiNengPeiYangMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		List<JiNengPeiYang> jiNengPeiYangList = TempletService
				.listAll(JiNengPeiYang.class.getSimpleName());
		Map<Integer, JiNengPeiYang> jiNengPeiYangMap = new HashMap<Integer, JiNengPeiYang>();
		for (JiNengPeiYang jiNengPeiYang : jiNengPeiYangList) {
			jiNengPeiYangMap.put(jiNengPeiYang.getId(), jiNengPeiYang);

			Map<Integer, Map<Integer, JiNengPeiYang>> jinengMap = wuqiMap
					.get(jiNengPeiYang.getWuqiType());
			if (jinengMap == null) {
				jinengMap = new HashMap<Integer, Map<Integer, JiNengPeiYang>>();
			}
			Map<Integer, JiNengPeiYang> peiYangMap = jinengMap
					.get(jiNengPeiYang.getJinengType());
			if (peiYangMap == null) {
				peiYangMap = new HashMap<Integer, JiNengPeiYang>();
			}
			peiYangMap.put(jiNengPeiYang.getQuality(), jiNengPeiYang);
			jinengMap.put(jiNengPeiYang.getJinengType(), peiYangMap);
			wuqiMap.put(jiNengPeiYang.getWuqiType(), jinengMap);
		}
		this.jiNengPeiYangMap = jiNengPeiYangMap;
	}

	public void upgradeJiNeng(int cmd, IoSession session, Builder builder) {
		UpgradeJiNengReq.Builder request = (UpgradeJiNengReq.Builder) builder;
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("君主不存在");
			return;
		}
		UpgradeJiNengResp.Builder resp = UpgradeJiNengResp.newBuilder();
		int wuqiType = request.getWuqiType();
		int jinengType = request.getJinengType();
		String field = wuqiType + "" + jinengType;
		String qualityStr = redis.hget(CACHE_JINENGPEIYANG + junZhu.id, field);
		int quality = (null == qualityStr) ? 0 : Integer.parseInt(qualityStr);
		if (quality == 2) {// 小于最高等级
			logger.info("君主 {} 的技能突破已达到最高等级", junZhu.id);
			resp.setResult(1);
			resp.setErrorMsg("已突破到最高等级");
			session.write(resp.build());
			return;
		}
		JiNengPeiYang jiNengPeiYang = wuqiMap.get(wuqiType).get(jinengType)
				.get(quality + 1);
		if (junZhu.level < jiNengPeiYang.getNeedLv()) {
			logger.info("君主 {} 等级未达到技能培养 {} 突破要求的等级 {}", junZhu.id,
					jiNengPeiYang.getId(), jiNengPeiYang.getNeedLv());
			resp.setResult(1);
			resp.setErrorMsg("技能突破未解锁");
			session.write(resp.build());
			return;
		}
		if (junZhu.tongBi < jiNengPeiYang.getNeedNum()) {
			logger.info("君主 {} 铜币 {} 不足技能 {} 培养需要的 {} 铜币" + junZhu.id,
					jiNengPeiYang.getId(), jiNengPeiYang.getNeedNum());
			resp.setResult(1);
			resp.setErrorMsg("铜币不足");
			session.write(resp.build());
			return;
		}
		// 扣除铜币
		junZhu.tongBi = junZhu.tongBi - jiNengPeiYang.getNeedNum();
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);
		// 技能突破
		redis.hset(CACHE_JINENGPEIYANG + junZhu.id, field,
				String.valueOf(quality + 1));
		resp.setResult(0);
		session.write(resp.build());
	}

	public void getJiNengPeiYangQuality(int cmd, IoSession session,
			Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("君主不存在");
			return;
		}
		GetJiNengPeiYangQuality.Builder resp = GetJiNengPeiYangQuality
				.newBuilder();
		for (int wuqiType : wuqiMap.keySet()) {
			for (int jinengType : wuqiMap.get(wuqiType).keySet()) {
				String field = wuqiType + "" + jinengType;
				String qualityStr = redis.hget(CACHE_JINENGPEIYANG + junZhu.id,
						field);
				int quality = (null == qualityStr) ? 0 : Integer
						.parseInt(qualityStr);
				JiNengQuality.Builder jn = JiNengQuality.newBuilder();
				jn.setJinengType(jinengType);
				jn.setWuqiType(wuqiType);
				jn.setQuality(quality);
				resp.addJn(jn);
			}
		}
		session.write(resp.build());
	}

}
