package com.qx.alliance;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.LianmengJuanxian;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.SessionManager;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.AllianceProtos.FengShanInfo;
import qxmobile.protobuf.AllianceProtos.FengShanInfoResp;
import qxmobile.protobuf.AllianceProtos.FengShanReq;

public class AllianceJuanXianMgr extends EventProc {
	public Logger log = LoggerFactory.getLogger(AllianceJuanXianMgr.class);
	public static AllianceJuanXianMgr inst;
	public Map<Integer, LianmengJuanxian> juanXianConf = new HashMap<Integer, LianmengJuanxian>(); //联盟捐献配置
	public Map<Integer, Purchase> jiansheConf = new HashMap<Integer, Purchase>();//基础建设配置
	public Map<Integer, Purchase> hufuConf = new HashMap<Integer, Purchase>();//军政建设配置
	
	public static int  jianSheEventId = 32; //对应LianmengEvent.xml中的事件id，如果策划改了，这里要修改
	public static int  hufuEventId = 33; //对应LianmengEvent.xml中的事件id，如果策划改了，这里要修改

	public AllianceJuanXianMgr(){
		inst = this;
		inst.init();
	}
	
	//初始化功能相关配置
	public void init(){
		List<LianmengJuanxian> juanXianList = TempletService.getInstance().listAll(LianmengJuanxian.class.getSimpleName());
		List<Purchase> jiansheList = TempletService.getInstance().listAll(Purchase.class.getSimpleName());
		for(LianmengJuanxian jx : juanXianList){
			juanXianConf.put(jx.id, jx);
		}
		
		for(Purchase p : jiansheList ){
			if(p.type == juanXianConf.get(1).type){
				jiansheConf.put(p.time, p);
			}
			if(p.type == juanXianConf.get(2).type){
				hufuConf.put(p.time, p);
			}
		}
	}
	/**响应客户端获取捐献信息请求*/
	public void getJuanXianInfo(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if( junZhu == null ){
			log.error("捐献信息查询失败，无法找到君主信息");
			return ;
		}
		AlliancePlayer ap = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if(ap == null | ap.lianMengId <= 0){
			log.error("捐献信息查询失败，君主不在联盟中");
		}
		//根据联盟ID获取联盟虔诚度信息
		LmTuTeng tt = HibernateUtil.find(LmTuTeng.class, ap.lianMengId);
		//获取捐献信息
		JuanXianBean bean = getJuanXianBean(junZhu.id) ;
		
		//根据VIP的功能获取两种建设的总次数
		int jsTotalTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel 
				, VipData.buy_jianShezhi_times);  
		int hfTotalTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel 
				, VipData.buy_Hufu_times);  
		
		//构建返回协议
		FengShanInfoResp.Builder resp  = FengShanInfoResp.newBuilder();
		FengShanInfo.Builder jianshe = FengShanInfo.newBuilder();
		FengShanInfo.Builder hufu = FengShanInfo.newBuilder();
		
		//基础建设信息
		jianshe.setConfId(1);
		jianshe.setUsedTimes(bean.jianSheTimes);
		jianshe.setTotalTimes(jsTotalTimes);
		jianshe.setNeedYuanBao(jiansheConf.get(bean.jianSheTimes+1).yuanbao);
	
		//军政建设信息
		hufu.setConfId(2);
		hufu.setUsedTimes(bean.huFuTimes);
		hufu.setTotalTimes(hfTotalTimes);
		hufu.setNeedYuanBao(hufuConf.get(bean.huFuTimes+1).yuanbao);
		
		//添加联盟虔诚度信息，之前的huoyue字段不再需要，所以用于发送虔诚度信息
		resp.addFsInfo(jianshe);
		resp.addFsInfo(hufu);
		resp.setHuoyuedu( tt == null? 0 : tt.times);
		session.write(resp.build());
	}
	
	/**响应客户端的捐献请求*/
	public void doJuanXian(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if( junZhu == null ){
			log.error("捐献失败，无法找到君主信息");
			return ;
		}
		AlliancePlayer ap = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if(ap == null | ap.lianMengId <= 0){
			log.error("捐献失败，君主不在联盟中");
		}
		//获取请求捐献类型
		FengShanReq.Builder req = (FengShanReq.Builder)builder;
		int confId = req.getConfId();
		
		//因为需要switch分别填写返回信息，所以提前创建返回协议
		FengShanInfoResp.Builder resp  = FengShanInfoResp.newBuilder();
		FengShanInfo.Builder info = FengShanInfo.newBuilder();
		
		//获取玩家的捐献信息
		JuanXianBean bean = getJuanXianBean(junZhu.id) ;
		
		//根据请求类型分别处理
		switch (confId) {
		case 1:{
			//检查捐献次数是否足够
			int jsTotalTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel 
					, VipData.buy_jianShezhi_times);  
			if(bean.jianSheTimes >= jsTotalTimes ){
				log.error("基础建设捐献次数超过最大次数");
				return ;
			}
			//配置获取需要元宝
			Purchase yuanBaoConf = jiansheConf.get(bean.jianSheTimes+1);
			if(yuanBaoConf == null ){
				log.error("无法获取基础建设次数：{}的消耗元宝配置" ,bean.jianSheTimes+1);
				return ;
			}
			int needYb = yuanBaoConf.yuanbao;
			//判断元宝足够，扣除，不够，返回
			if( junZhu.yuanBao >= needYb ){
				YuanBaoMgr.inst.diff(junZhu, -1*needYb , 0 , 0 , YBType.LIAN_MENG_JUAN_XIAN, "基础建设");
			}else{
				log.error("君主{}请求基础建设失败：元宝不足",junZhu.id);
				return ;
			}
			//成功扣除元宝，发奖
			String award = juanXianConf.get(1).award;
			AwardMgr.inst.giveReward(session, award, junZhu);
			//联盟事件公告需要展示玩家增加的建设值，所以从奖励字段中获取，存入awardNum
			int awardNum = Integer.parseInt(award.split("#")[0].split(":")[2]);
			
			//使用次数+1
			bean.jianSheTimes += 1 ;
			HibernateUtil.save(bean);
			//编辑返回信息
			info.setConfId(1);
			info.setUsedTimes(bean.jianSheTimes);
			info.setTotalTimes(jsTotalTimes);
			Purchase nextConf = jiansheConf.get(bean.jianSheTimes+1);
			info.setNeedYuanBao(nextConf == null ? 0 : nextConf.yuanbao);
			log.info("君主{}进行联盟基础建设捐献",junZhu.id);
			//保存联盟事件信息
			saveAllianceEvent(ap.lianMengId, jianSheEventId, junZhu.name, awardNum);
		}
		break;
		case 2:{			
			//检查捐献次数是否足够
			int hfTotalTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel 
					, VipData.buy_Hufu_times);  
			if(bean.huFuTimes >= hfTotalTimes ){
				log.error("军政建设捐献次数超过最大次数");
				return ;
			}
			//配置获取需要元宝
			Purchase yuanBaoConf = hufuConf.get(bean.huFuTimes+1);
			if(yuanBaoConf == null ){
				log.error("无法获取军政建设次数：{}的消耗元宝配置" ,bean.jianSheTimes+1);
				return ;
			}
			int needYb = yuanBaoConf.yuanbao;
			//判断元宝足够，扣除，不够，返回
			if( junZhu.yuanBao >= needYb ){
				YuanBaoMgr.inst.diff(junZhu, -1*needYb , 0 , 0 , YBType.LIAN_MENG_JUAN_XIAN, "军政建设");
			}else{
				log.error("君主{}请求军政建设失败：元宝不足",junZhu.id);
				return ;
			}
			//成功扣除元宝，发奖
			String award = juanXianConf.get(2).award;
			AwardMgr.inst.giveReward(session, award, junZhu);
			//联盟事件公告需要展示玩家增加的虎符数量，所以从奖励字段中获取，存入awardNum
			int awardNum = Integer.parseInt(award.split("#")[0].split(":")[2]);
			//使用次数+1
			bean.huFuTimes += 1 ;
			HibernateUtil.save(bean);
			//编辑返回信息
			info.setConfId(2);
			info.setUsedTimes(bean.huFuTimes);
			info.setTotalTimes(hfTotalTimes);
			Purchase nextConf = hufuConf.get(bean.huFuTimes+1);
			info.setNeedYuanBao(nextConf == null ? 0 : nextConf.yuanbao);
			log.info("君主{}进行联盟军政建设捐献",junZhu.id);
			//保存联盟事件信息
			saveAllianceEvent(ap.lianMengId, hufuEventId, junZhu.name, awardNum);
		}
		break;
		default:
			log.error("联盟捐献操作失败：错误的confId：{}" , confId);
		return;
		}
		
		//捐献操作完成，保存数据
		HibernateUtil.save(bean);
		//修改联盟虔诚值
		int qianCheng = MoBaiMgr.inst.updateMobaiLevel(ap.lianMengId, 1, new Date());
		resp.setHuoyuedu(qianCheng);
		resp.addFsInfo(info);
		session.write(resp.build());
	}
	
	/**获取玩家的捐献信息，如果没有，就初始化一个*/
	public JuanXianBean getJuanXianBean(long junZhuId){
		JuanXianBean res = HibernateUtil.find(JuanXianBean.class, junZhuId);
		if(res == null ){
			res = new JuanXianBean();
			res.jzId = junZhuId;
			res.jianSheTimes = 0 ;
			res.huFuTimes = 0 ;
			res.lastResetTime = new Date();
		}else{
			refreshJuanXianBean(res);
		}
		return res ;
	}
	
	/**判断是否跨天，如果是，刷新玩家的捐献信息*/
	public void refreshJuanXianBean(JuanXianBean bean){
		Date now = new Date();
		if(bean.lastResetTime != null && DateUtils.isTimeToReset(bean.lastResetTime,CanShu.REFRESHTIME)) {
			log.info("reset君主--{}封禅信息",bean.jzId);
			bean.lastResetTime = now;
			bean.jianSheTimes = 0;
			bean.huFuTimes = 0;
			HibernateUtil.save(bean);
		}
	}
	
	/**联盟捐献的红点推送*/
	public void pushRedPoint( IoSession session ){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		//获取捐献信息
		JuanXianBean bean = getJuanXianBean(junZhu.id) ;
		
		//根据VIP的功能获取两种建设的总次数
		int jsTotalTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel 
				, VipData.buy_jianShezhi_times);  
		int hfTotalTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel 
				, VipData.buy_Hufu_times);  
		//基础建设红点：有次数，元宝够
		if(bean.jianSheTimes < jsTotalTimes ){
			Purchase yuanBaoConf = jiansheConf.get(bean.jianSheTimes+1);
			if(yuanBaoConf != null){
				if(yuanBaoConf.yuanbao <= junZhu.yuanBao){
					log.info("君主{}基础建设满足红点推送，发送推送消息",junZhu.id);
					FunctionID.pushCanShowRed(junZhu.id , session, FunctionID.FengShanDaDian);
				}
			}
		}
		//军政建设红点：有次数，元宝够
		if(bean.huFuTimes < hfTotalTimes ){
			Purchase yuanBaoConf = hufuConf.get(bean.huFuTimes+1);
			if(yuanBaoConf != null ){
				if(yuanBaoConf.yuanbao <= junZhu.yuanBao){
					log.info("君主{}军政建设满足红点推送，发送推送消息",junZhu.id);
					FunctionID.pushCanShowRed(junZhu.id, session, FunctionID.FengShanShengDian);
				}
			}
		}
		
		
	}
	
	public void saveAllianceEvent( int lianMengId , int id , String jzName, int awardNum) {
		String eventStr = AllianceMgr.inst.lianmengEventMap.get(id).str.replace("%a", jzName)
				.replace("%b", awardNum+"");
		AllianceMgr.inst.addAllianceEvent(lianMengId, eventStr);
	}

	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.JUNZHU_LOGIN:{
			IoSession session = SessionManager.inst.getIoSession((long)event.param);
			pushRedPoint(session);
		}
		break;
		case ED.REFRESH_TIME_WORK:{
			pushRedPoint((IoSession)event.param);
		}
		break;
		default:
			log.error("注册了错误的事件ID{}",event.id);
		break;
		}
	}

	@Override
	public void doReg() {
//		// TODO Auto-generated method stub
//		EventMgr.regist(ED.ACC_LOGIN, this);
//		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}
}
