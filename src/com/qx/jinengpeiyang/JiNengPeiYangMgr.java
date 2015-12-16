package com.qx.jinengpeiyang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.JiNengPeiYang.GetJiNengPeiYangQuality;
import qxmobile.protobuf.JiNengPeiYang.HeroData;
import qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengReq;
import qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.JiNengPeiYang;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;

public class JiNengPeiYangMgr {
	public static JiNengPeiYangMgr inst;
	public Logger log = LoggerFactory.getLogger(JiNengPeiYangMgr.class);
	public Map<Integer, JiNengPeiYang> jiNengPeiYangMap = new HashMap<Integer, JiNengPeiYang>();

	public JiNengPeiYangMgr() {
		inst = this;
		initData();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		List<JiNengPeiYang> jiNengPeiYangList = TempletService
				.listAll(JiNengPeiYang.class.getSimpleName());
		Map<Integer, JiNengPeiYang> jiNengPeiYangMap = new HashMap<Integer, JiNengPeiYang>();
		for (JiNengPeiYang jiNengPeiYang : jiNengPeiYangList) {
			jiNengPeiYangMap.put(jiNengPeiYang.getId(), jiNengPeiYang);
		}
		this.jiNengPeiYangMap = jiNengPeiYangMap;
	}

	public void upgradeJiNeng(int cmd, IoSession session, Builder builder) {
		UpgradeJiNengReq.Builder request = (UpgradeJiNengReq.Builder) builder;
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.info("君主不存在");
			return;
		}
		UpgradeJiNengResp.Builder resp = UpgradeJiNengResp.newBuilder();
		int jnId = request.getSkillID();
		JiNengPeiYang p = jiNengPeiYangMap.get(jnId);
		if(p == null){
			resp.setErrorMsg("skill not found, id:"+jnId);
			resp.setResult(404);
			session.write(resp.build());
			return;
		}
		int quality = p.quality;
		if (quality == 2) {// 小于最高等级
			log.info("君主 {} 的技能突破已达到最高等级", junZhu.id);
			resp.setResult(100);
			resp.setErrorMsg("已突破到最高等级");
			session.write(resp.build());
			return;
		}
		JiNengPeiYang next = jiNengPeiYangMap.get(p.nextId);
		if(next == null){
			log.info("未找到下个技能 {}", p.id);
			resp.setResult(405);
			resp.setErrorMsg("暂时不能突破");
			session.write(resp.build());
			return;
		}
		
		if (junZhu.level < next.getNeedLv()) {
			log.info("君主 {} 等级未达到技能培养 {} 突破要求的等级 {}", junZhu.id,
					next.getId(), next.getNeedLv());
			resp.setResult(1);
			resp.setErrorMsg("技能突破未解锁");
			session.write(resp.build());
			return;
		}
		if (junZhu.tongBi < next.getNeedNum()) {
			log.info("君主 {} 铜币 {} 不足技能 {} 培养需要的 {} 铜币" + junZhu.id,
					next.getId(), next.getNeedNum());
			resp.setResult(101);
			resp.setErrorMsg("铜币不足");
			session.write(resp.build());
			return;
		}
		// 扣除铜币
		junZhu.tongBi = junZhu.tongBi - next.getNeedNum();
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);
		// 技能突破
		JNBean bean = HibernateUtil.find(JNBean.class, junZhu.id);
		boolean insert = false;
		if(bean == null){
			bean = getDefaultBean();
			bean.jzId = junZhu.id;
			insert = true;
		}
		setIdToBean(bean,next);
		if(insert){
			HibernateUtil.insert(bean);
		}else{
			HibernateUtil.update(bean);
		}
		log.info("{} get skill {}, pay tongBi {}", junZhu.id, next.id, next.getNeedNum());
		resp.setResult(0);
		session.write(resp.build());
		// 进阶角色技能
		EventMgr.addEvent(ED.jinJie_jueSe_jiNeng, new Object[]{junZhu.id});
	}
	public void setIdToBean(JNBean bean, JiNengPeiYang next) {
		switch(next.wuqiType){
		case 0:{
			switch(next.jinengType){
			case 0:bean.wq1_1 = next.id;break;
			case 1:bean.wq1_2 = next.id;break;
			case 2:bean.wq1_3 = next.id;break;
			default:log.error("miss skill id 1A {}",next.id);break;
			}
			break;
		}
		case 1:{
			switch(next.jinengType){
			case 0:bean.wq2_1 = next.id;break;
			case 1:bean.wq2_2 = next.id;break;
			case 2:bean.wq2_3 = next.id;break;
			default:log.error("miss skill id 2A {}",next.id);break;
			}
			break;
		}
		case 2:{
			switch(next.jinengType){
			case 0:bean.wq3_1 = next.id;break;
			case 1:bean.wq3_2 = next.id;break;
			case 2:bean.wq3_3 = next.id;break;
			default:log.error("miss skill id 3A {}",next.id);break;
			}
			break;
		}
		default:
			log.error("miss skill wuqi type BB {}", next.id);
			break;
		}
	}

	public JNBean getDefaultBean(){
		//不要共享
		JNBean bean = new JNBean();
		bean.wq1_1 = 1100;
		bean.wq1_2 = 1200;
		bean.wq1_3 = 1300;
		//
		bean.wq2_1 = 2100;
		bean.wq2_2 = 2200;
		bean.wq2_3 = 2300;
		//
		bean.wq3_1 = 3100;
		bean.wq3_2 = 3200;
		bean.wq3_3 = 3300;
		return bean;
	}
	public void getJiNengPeiYangQuality(int cmd, IoSession session,
			Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.info("君主不存在");
			return;
		}
		JNBean bean = HibernateUtil.find(JNBean.class, junZhu.id);
		if(bean == null){
			bean = getDefaultBean();
		}
		GetJiNengPeiYangQuality.Builder resp = GetJiNengPeiYangQuality
				.newBuilder();
		@SuppressWarnings("unchecked")
		List<JiNengPeiYang> jiNengPeiYangList = TempletService
				.listAll(JiNengPeiYang.class.getSimpleName());
		for (JiNengPeiYang p : jiNengPeiYangList) {
			int curUseId = 0;
			switch(p.wuqiType){
			case 0:curUseId=bean.wq1_1;break;
			case 1:curUseId=bean.wq2_1;break;
			case 2:curUseId=bean.wq3_1;break;
			default:break;			
			}
			HeroData.Builder hb = HeroData.newBuilder();
			hb.setSkillId(p.id);
			hb.setIsUp(p.id<curUseId);
			resp.addListHeroData(hb);
		}
		session.write(resp.build());
	}

}
