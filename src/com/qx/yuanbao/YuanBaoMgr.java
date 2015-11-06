package com.qx.yuanbao;

import java.util.Date;

import log.CunLiangLog;
import log.OurLog;
import log.parser.ReasonMgr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.util.TableIDCreator;

/**
 * @author hejincheng
 * 
 */
public class YuanBaoMgr {
	public static YuanBaoMgr inst;
	private Logger logger = LoggerFactory.getLogger(YuanBaoMgr.class);

	public YuanBaoMgr() {
		inst = this;
		initData();
	}

	private void initData() {

	}

	/**
	 * 修改元宝数量
	 * 
	 * @param junzhu
	 *            君主信息
	 * @param change
	 *            增量
	 * @param money
	 *            消费人民币金额
	 * @param price
	 *            单价
	 * @param type
	 *            消费类型，对应YBType类
	 * @param reason
	 *            变更原因
	 * @param type
	 *            使用类型
	 */
	public synchronized void diff(JunZhu junzhu, int change, int money, int price, int type,
			String reason) {
		int yuanbaoBefore = junzhu.yuanBao;
		junzhu.yuanBao += change;
		int yuanbaoAfter = junzhu.yuanBao;
		logger.info(
				"元宝计算：君主id{},元宝计算：账号{},元宝计算：变动前{},元宝计算：增量{},元宝计算：变动后{},元宝计算：原因{}",
				junzhu.id, junzhu.name, yuanbaoBefore, change, yuanbaoAfter,
				reason);
		// 存到数据库
		YuanBaoInfo info = new YuanBaoInfo();

		// 改自增主键为指定
		// 2015年4月17日16:57:30int改为long
		long dbId = (TableIDCreator.getTableID(YuanBaoInfo.class, 1L));
		info.setDbId(dbId);

		info.setYuanbaoBefore(yuanbaoBefore);
		info.setYuanbaoAfter(yuanbaoAfter);
		info.setYuanbaoChange(change);
		info.setOwnerid(junzhu.id);
		info.setTimestamp(new Date());
		info.setReason(reason);
		info.setCostMoney(money);
		info.setPrice(price);
		info.setType(type);
		HibernateUtil.save(info);
		//
		int Reason = ReasonMgr.inst.getId(reason);
		OurLog.log.MoneyFlow(junzhu.level, junzhu.yuanBao, change, Reason, change>0?0:1, 1, String.valueOf(junzhu.id));
		CunLiangLog.inst.yuanBaoChange(junzhu.id, junzhu.yuanBao);
	}
}
