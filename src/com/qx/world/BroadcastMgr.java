package com.qx.world;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder;
import qxmobile.protobuf.SMessageProtos.SMessage;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.AnnounceTemp;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.Chenghao;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MibaoStar;
import com.manu.network.PD;
import com.manu.network.TXSocketMgr;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;

public class BroadcastMgr extends EventProc{
	/*
type	完成条件
1	探宝得到N星秘宝
2	合成得到N星秘宝
3	玩家获得N个秘宝
4	玩家获得某个物品
5	玩家等级达到N级
//test below
6	玩家获得某个称号
7	玩家缴纳N次贡金
8	玩家完成顶礼膜拜（每次完成都发广播）
9	玩家完成成功运送X品质的马车（1~5表示白~橙品质，每次运成功都发广播）
10	玩家成功通关某难度的游侠活动（填写游侠关卡ID）
11	玩家百战前10名名次发生变动
12 当玩家所有秘宝的总星数达到N的时候播放广播


	 * 
	 */
	public static Logger log = LoggerFactory.getLogger(BroadcastMgr.class.getSimpleName());
	public static BroadcastMgr inst = new BroadcastMgr();
	public void send(String text){
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_Broadcast;
		Builder em = ErrorMessage.newBuilder();
		em.setCmd(0);
		em.setErrorCode(0);
		em.setErrorDesc(text);
		msg.builder = em;
		Set<WriteFuture> receivers = TXSocketMgr.inst.acceptor.broadcast(msg);
		log.info("发送广播 ：{}，目标数量{}", text, receivers.size());
	}
	public void check() {
		List<BroadcastEntry> list = HibernateUtil.list(BroadcastEntry.class, "");
		Date now = new Date();
		log.info("开始检查定时广播 ，数量 {}",list.size());
		for(BroadcastEntry be : list){
			if(be.open ==false)continue;//没有开启
			if(be.endTime.before(now))continue;//已结束
			if(be.startTime.after(now))continue;//还没开始
			if(be.lastSendTime!=null){//检查发送间隔
				long last = be.lastSendTime.getTime();
				long cur = now.getTime();
				long diff = cur - last;
				long minuteDiff = diff / 1000 / 60;
				if(minuteDiff<be.intervalMinutes){
					log.info("{}时间间隔不足", be.id);
					continue;
				}
			}
			log.info("发送定时广播 id{}，内容 {}",be.id,be.content);
			be.lastSendTime = now;
			HibernateUtil.save(be);
			send(be.content);
		}
		log.info("检查结束");
	}
	@Override
	public void proc(Event param) {
		switch(param.id){
		case ED.MIBAO_HECHENG_BROADCAST:
			checkMiBaoActive(param);
			break;
		case ED.TAN_BAO_JIANG_LI:
			checkTanBao(param);
			break;
		case ED.MIBAO_UP_STAR:
			checkMiBaoUpStart(param);
			break;
		case ED.GAIN_MIBAO:
			checkMiBaoCnt(param);
			break;
		case ED.GAIN_ITEM:
			checkGainItem(param);
			break;
		case ED.junzhu_level_up:
			checkLvUp(param);
			break;
		case ED.GAIN_CHENG_HAO:
			checkChengHao(param);
			break;
		case ED.JUAN_XIAN_GONG_JIN:
			checkGongJinTimes(param);
			break;
		case ED.YU_MO_BAI:
			checkYuMoBai(param);
			break;
		case ED.YA_BIAO_SUCCESS:
			checkYaBiao(param);
			break;
		case ED.YOU_XIA_SUCCESS:
			checkYouXia(param);
			break;
		case ED.BAI_ZHAN_RANK_UP:
			checkBaiZhan(param);
			break;
		}
	}
	protected void checkMiBaoActive(Event param) {
		//junZhu,session,miBaoCfg
		Object[] arr = (Object[]) param.param;
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		MiBao miBaoCfg = (MiBao) arr[2];
		AnnounceTemp targetConf = null;
		String strCon = String.valueOf(miBaoCfg.initialStar);
		for(AnnounceTemp conf : confList){
			if(conf.type != 13){//13	玩家合成3星秘宝
				continue;
			}
			if(strCon.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		String template = targetConf.announcement;
		//[ffffff]恭喜[-][dbba8f]*玩家名字七个字*[-][e5e205]合成[-][0dbce8]3星[-][ffffff]秘宝[-][f5aa29]#秘宝名字#[-][ffffff]！[-]
		JunZhu jz = (JunZhu) arr[0];
		template = template.replace("*玩家名字七个字*", jz.name);
		template = template.replace("#秘宝名字#", HeroService.getNameById(String.valueOf(miBaoCfg.nameId)));
		send(template);
	}
	protected void checkBaiZhan(Event param) {
		//new Object[]{jz.name, otherJun.name, bean.highestRank}
		Object[] arr = (Object[]) param.param;
		String winName =  (String) arr[0];
		String failName = (String) arr[1];
		Integer rank = (Integer)arr[2];
		if(rank>10)return;
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		AnnounceTemp targetConf = null;
//		String strCon = String.valueOf(rank);
		for(AnnounceTemp conf : confList){
			if(conf.type != 11){//11	玩家百战前10名名次发生变动
				continue;
			}
				targetConf = conf;
				break;
		}
		if(targetConf == null){
			return;
		}
		//*玩家名字七个字*于百战千军中击败了#玩家名字七个字#，上升为第$N$名
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", winName);
		template = template.replace("#玩家名字七个字#", failName);
		template = template.replace("$N$", rank.toString());
		send(template);
	}
	protected void checkYouXia(Event param) {
		//new Object[]{junZhu, zhangJieId}
		Object[] arr = (Object[]) param.param;
		JunZhu jz = (JunZhu) arr[0];
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		Integer horseType = (Integer) arr[1];
		AnnounceTemp targetConf = null;
		String strCon = String.valueOf(horseType);
		for(AnnounceTemp conf : confList){
			if(conf.type != 10){//10	玩家成功通关某难度的游侠活动（填写游侠关卡ID）
				continue;
			}
			if(strCon.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//祝贺*玩家名字七个字*通关了折磨难度的剿灭叛军
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		send(template);
	}
	protected void checkYaBiao(Event param) {
		//new Object[]{jz,ybbean.horseType}
		Object[] arr = (Object[]) param.param;
		JunZhu jz = (JunZhu) arr[0];
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		Integer horseType = (Integer) arr[1];
		AnnounceTemp targetConf = null;
		String strCon = String.valueOf(horseType);
		for(AnnounceTemp conf : confList){
			if(conf.type != 9){//9	玩家完成成功运送X品质的马车（1~5表示白~橙品质，每次运成功都发广播）
				continue;
			}
			if(strCon.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//恭喜*玩家名字七个字*成功将橙马镖队运达目的地，有惊无险
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		send(template);
	}
	protected void checkYuMoBai(Event param) {
		Object[] arr = (Object[]) param.param;
		JunZhu jz = (JunZhu) arr[0];
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		AnnounceTemp targetConf = null;
		for(AnnounceTemp conf : confList){
			if(conf.type != 8){//8	玩家完成顶礼膜拜（每次完成都发广播）
				continue;
			}
				targetConf = conf;
				break;
		}
		if(targetConf == null){
			return;
		}
		//*玩家名字七个字*于#联盟名字七个字#盟主雕像处完成顶礼膜拜，获得大量奖励
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if(ap == null){
			return;
		}
		AllianceBean ab = HibernateUtil.find(AllianceBean.class, ap.lianMengId);
		if(ab == null){
			return;
		}
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		template = template.replace("#联盟名字七个字#", ab.name);
		send(template);
	}
	protected void checkGongJinTimes(Event param) {
		//new Object[]{jz, session, gongjinBean.todayJXTimes,AllianceBean}
		Object[] arr = (Object[]) param.param;
		JunZhu jz = (JunZhu) arr[0];
		Integer times = (Integer) arr[2];
		AllianceBean lm = (AllianceBean) arr[3];
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		AnnounceTemp targetConf = null;
		String lvStr = String.valueOf(times);
		for(AnnounceTemp conf : confList){
			if(conf.type != 7){//7	玩家缴纳N次贡金
				continue;
			}
			if(lvStr.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//恭喜*玩家名字七个字*在主簿处上缴贡金8次，联盟声望提升了
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		template = template.replace("#联盟名字七个字#", lm.name);
		send(template);
	}
	protected void checkChengHao(Event param) {
		//new Object[]{pid, want}
		Object[] arr = (Object[]) param.param;
		Long jzId = (Long) arr[0];
		Chenghao confCh = (Chenghao) arr[1];
		IoSession session = AccountManager.sessionMap.get(jzId);
		if(session == null){
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		AnnounceTemp targetConf = null;
		String lvStr = String.valueOf(confCh.id);
		for(AnnounceTemp conf : confList){
			if(conf.type != 6){//6	玩家获得某个称号
				continue;
			}
			if(lvStr.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//恭喜*玩家名字七个字*获得称号初出茅庐
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		send(template);
	}
	protected void checkLvUp(Event param) {
		//new Object[] { jz.id,		jz.level, jz })
		Object[] arr = (Object[]) param.param;
		Integer lv = (Integer) arr[1];
		JunZhu jz = (JunZhu) arr[2];
		if(lv<20){
			return;
		}
		if( (lv%10) != 0){
			return;
		}
		Long jzId = jz.id;
		IoSession session = AccountManager.sessionMap.get(jzId);
		if(session == null){
			return;
		}
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		AnnounceTemp targetConf = null;
		String lvStr = String.valueOf(lv);
		for(AnnounceTemp conf : confList){
			if(conf.type != 5){//5	玩家等级达到N级
				continue;
			}
			if(lvStr.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//恭喜*玩家名字七个字*等级达到20级
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		send(template);
	}
	protected void checkGainItem(Event param) {
		Object[] arr = (Object[]) param.param;
		Long jzId = (Long) arr[0];
		IoSession session = AccountManager.sessionMap.get(jzId);
		if(session == null){
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		Integer itemId = (Integer) arr[1];
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		String hql = "select count(1) from MiBaoDB where ownerId="+jz.id+" and level>=1";
		int cnt = HibernateUtil.getCount(hql);
		if(cnt<=0){
			return;
		}
		AnnounceTemp targetConf = null;
		String itemIdStr = String.valueOf(itemId);
		for(AnnounceTemp conf : confList){
			if(conf.type != 4){//4	玩家获得某个物品
				continue;
			}
			if(itemIdStr.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//恭喜*玩家名字七个字*获得6级坚韧符文
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		send(template);
	}
	protected void checkMiBaoCnt(Event param) {
		//new Object[]{jz,session});
		Object[] arr = (Object[]) param.param;
		JunZhu jz =  (JunZhu) arr[0];
		checkMiBaoStarCnt(jz);
		IoSession session = (IoSession) arr[1];
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		String hql = "select count(1) from MiBaoDB where ownerId="+jz.id+" and level>=1";
		int cnt = HibernateUtil.getCount(hql);
		if(cnt<=0){
			return;
		}
		String confStr = String.valueOf(cnt);
		AnnounceTemp targetConf = null;
		for(AnnounceTemp conf : confList){
			if(conf.type != 3){//3	玩家获得N个秘宝
				continue;
			}
			if(confStr.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//恭喜*玩家名字七个字*收集到了#N#个秘宝
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		//template = template.replace("#N#", String.valueOf(cnt));
		send(template);
	}
	protected void checkMiBaoUpStart(Event param) {
		//new Object[]{junZhu,session,miBaoCfg}
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		Object[] arr = (Object[]) param.param;
		JunZhu jz =  (JunZhu) arr[0];
		checkMiBaoStarCnt(jz);
		IoSession session = (IoSession) arr[1];
		MiBao mibao = (MiBao) arr[2];
		MibaoStar curStarCfg = (MibaoStar) arr[3];
		String starStr = String.valueOf(curStarCfg.getStar()+1);
		AnnounceTemp targetConf = null;
		for(AnnounceTemp conf : confList){
			if(conf.type != 2){//2	合成得到N星秘宝
				continue;
			}
			if(starStr.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//恭喜*玩家名字七个字*合成了3星秘宝#秘宝名字#
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		template = template.replace("#秘宝名字#", HeroService.getNameById(String.valueOf(mibao.nameId)));
		send(template);
	}
	protected void checkTanBao(Event param) {
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		Object[] arr = (Object[]) param.param;
		JunZhu jz =  (JunZhu) arr[0];
		IoSession session = (IoSession) arr[1];
		List<AwardTemp> awards = (List<AwardTemp>) arr[2];
		{//test code
//			AwardTemp at = new AwardTemp();
//			at.setItemType(4); //301013
//			at.setItemId(301013);
//			at.setItemNum(0);
//			awards.add(at);
		}
		for (AwardTemp a : awards) {
			// 4 表示 秘宝的itemType是4,
			if (a.getItemType() != 4) {
				continue;
			}
			MiBao mibao = MibaoMgr.mibaoMap.get(a.getItemId());
			if(mibao == null){
				//不是秘宝
				continue;
			}
			String starStr = String.valueOf(mibao.initialStar);
			AnnounceTemp targetConf = null;
			for(AnnounceTemp conf : confList){
				if(conf.type != 1){//1	探宝得到N星秘宝
					continue;
				}
				if(starStr.equals(conf.condition)){
					targetConf = conf;
					break;
				}
			}
			if(targetConf == null){
				continue;
			}
			//恭喜*玩家名字七个字*探宝获得3星秘宝#秘宝名字#
			String template = targetConf.announcement;
			template = template.replace("*玩家名字七个字*", jz.name);
			template = template.replace("#秘宝名字#", HeroService.getNameById(String.valueOf(mibao.nameId)));
			send(template);
		}
	}
	protected void checkMiBaoStarCnt(JunZhu jz) {
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList == null){
			return;
		}
		String hql = "select sum(star) from MiBaoDB where ownerId="+jz.id+" and level>=1";
		int cnt = HibernateUtil.getCount(hql);
		if(cnt<=0){
			return;
		}
		String confStr = String.valueOf(cnt);
		AnnounceTemp targetConf = null;
		for(AnnounceTemp conf : confList){
			if(conf.type != 12){//12 当玩家所有秘宝的总星数达到N的时候播放广播
				continue;
			}
			if(confStr.equals(conf.condition)){
				targetConf = conf;
				break;
			}
		}
		if(targetConf == null){
			return;
		}
		//恭喜*玩家名字七个字*秘宝总星数达到
		String template = targetConf.announcement;
		template = template.replace("*玩家名字七个字*", jz.name);
		//template = template.replace("#N#", String.valueOf(cnt));
		send(template);
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.TAN_BAO_JIANG_LI, this);
		EventMgr.regist(ED.MIBAO_UP_STAR, this);
		EventMgr.regist(ED.GAIN_MIBAO, this);
		EventMgr.regist(ED.GAIN_ITEM, this);
		EventMgr.regist(ED.junzhu_level_up, this);
		EventMgr.regist(ED.GAIN_CHENG_HAO, this);
		EventMgr.regist(ED.JUAN_XIAN_GONG_JIN, this);
		EventMgr.regist(ED.YU_MO_BAI, this);
		EventMgr.regist(ED.YA_BIAO_SUCCESS, this);
		EventMgr.regist(ED.YOU_XIA_SUCCESS, this);
		EventMgr.regist(ED.BAI_ZHAN_RANK_UP, this);
		EventMgr.regist(ED.MIBAO_HECHENG_BROADCAST, this);
	}
}
