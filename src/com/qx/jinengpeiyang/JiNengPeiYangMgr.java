package com.qx.jinengpeiyang;

import java.util.Arrays;
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
import com.manu.network.SessionManager;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;

public class JiNengPeiYangMgr extends EventProc{
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
//		int quality = p.quality;
//		if (p.nextId<=0) {// 小于最高等级
//			log.info("君主 {} 的技能突破已达到最高等级", junZhu.id);
//			resp.setResult(100);
//			resp.setErrorMsg("已突破到最高等级");
//			session.write(resp.build());
//			return;
//		}
		
		if (junZhu.level < p.getNeedLv()) {
			log.info("君主 {} 等级未达到技能培养 {} 突破要求的等级 {}", junZhu.id,
					p.getId(), p.getNeedLv());
			resp.setResult(1);
			resp.setErrorMsg("技能突破未解锁");
			session.write(resp.build());
			return;
		}
		if (junZhu.tongBi < p.getNeedNum()) {
			log.info("君主 {} 铜币 {} 不足技能 {} 培养需要的 {} 铜币" , junZhu.id,
					junZhu.tongBi, p.getId(), p.getNeedNum());
			resp.setResult(101);
			resp.setErrorMsg("铜币不足");
			session.write(resp.build());
			return;
		}
		// 扣除铜币
		junZhu.tongBi = junZhu.tongBi - p.getNeedNum();
		HibernateUtil.save(junZhu);
//		JunZhuMgr.inst.sendMainInfo(session);
		// 技能突破
		JNBean bean = HibernateUtil.find(JNBean.class, junZhu.id);
		boolean insert = false;
		if(bean == null){
			bean = getDefaultBean();
			bean.jzId = junZhu.id;
			insert = true;
		}
		setIdToBean(bean,p);
		if(insert){
			HibernateUtil.insert(bean);
		}else{
			HibernateUtil.update(bean);
		}
		log.info("{} get skill {}, pay tongBi {}", junZhu.id, p.id, p.getNeedNum());
		resp.setResult(0);
		session.write(resp.build());
		// 进阶角色技能
		EventMgr.addEvent(ED.jinJie_jueSe_jiNeng, new Object[]{junZhu.id});
		//
		addNewJn(junZhu, p);
		//
		JunZhuMgr.inst.sendMainInfo(session);
		
	}

	/**
	 * 新获得的技能保存起来，用于进战斗后的提示。
	 * @param junZhu
	 * @param p
	 */
	public void addNewJn(JunZhu junZhu, JiNengPeiYang p) {
		NewJNBean nb = HibernateUtil.find(NewJNBean.class, junZhu.id);
		if(nb == null){
			nb = new NewJNBean();
			nb.ids="";
			nb.jzId = junZhu.id;
			HibernateUtil.insert(nb);
		}
		if(nb.ids==null || nb.ids.isEmpty()){
			nb.ids=""+p.id;
		}else{
			nb.ids += "#"+p.id;
		}
		HibernateUtil.update(nb);
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
	public void fixOpenByLevel(JNBean bean, long jzId){
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		if(jz == null){
			return;
		}
		fixOpenByLevel(bean, jz, bean.wq1_1);
		fixOpenByLevel(bean, jz, bean.wq1_2);
		fixOpenByLevel(bean, jz, bean.wq1_3);
		fixOpenByLevel(bean, jz, bean.wq2_1);
		fixOpenByLevel(bean, jz, bean.wq2_2);
		fixOpenByLevel(bean, jz, bean.wq2_3);
		fixOpenByLevel(bean, jz, bean.wq3_1);
		fixOpenByLevel(bean, jz, bean.wq3_2);
		fixOpenByLevel(bean, jz, bean.wq3_3);
	}
	public void fixOpenByLevel(JNBean bean, JunZhu jz, int jnId) {
		JiNengPeiYang conf = jiNengPeiYangMap.get(jnId);
		if(conf == null){
			return;
		}
		if(jz.level>=conf.needLv){
			return;
		}
		JiNengPeiYang t = new JiNengPeiYang();
		t.wuqiType = conf.wuqiType;
		t.jinengType = conf.jinengType;
		t.id = 0;
		//把等级不够的技能id设置为0
		if(jz.level<conf.needLv){
			setIdToBean(bean, t);
		}
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
			int curUseId = getCurId(bean, p);
			HeroData.Builder hb = HeroData.newBuilder();
			hb.setSkillId(p.id);
			boolean open = p.id<=curUseId;
			if(p.quality==0){//第一列技能，等级到了就开启（已突破状态）
				open = p.needLv<=junZhu.level;
			}
			hb.setIsUp(open);
			resp.addListHeroData(hb);
		}
		session.write(resp.build());
	}

	public int getCurId(JNBean bean, JiNengPeiYang p) {
		int curUseId = 0;
		switch(p.wuqiType){
		case 0:
			switch(p.jinengType){
			case 0:curUseId=bean.wq1_1;break;
			case 1:curUseId=bean.wq1_2;break;
			case 2:curUseId=bean.wq1_3;break;
			}
			break;
		case 1:
			switch(p.jinengType){
			case 0:curUseId=bean.wq2_1;break;
			case 1:curUseId=bean.wq2_2;break;
			case 2:curUseId=bean.wq2_3;break;
			}
			break;
		case 2:
			switch(p.jinengType){
			case 0:curUseId=bean.wq3_1;break;
			case 1:curUseId=bean.wq3_2;break;
			case 2:curUseId=bean.wq3_3;break;
			}
			break;
		default:break;			
		}
		return curUseId;
	}
	
	public int[] getNewJNIds(long jzId){
		NewJNBean bean = HibernateUtil.find(NewJNBean.class, jzId);
		if(bean == null || bean.ids == null || bean.ids.isEmpty()){
			return new int[0];
		}
		String arr[] = bean.ids.split("#");
		int[] ret = new int[arr.length];
		for(int i=0;i<arr.length;i++){
			ret[i] = Integer.parseInt(arr[i]);
		}
		bean.ids="";
		HibernateUtil.update(bean);
		return ret;
	}

	@Override
	public void proc(Event param) {
		switch(param.id){
		case ED.junzhu_level_up:
			checkRedNotice(param);
			break;
		case ED.JUNZHU_LEVEL_RANK_REFRESH:
			checkRedNotice((JunZhu) param.param);
			break;
		}
	}

	/**
	 * 检查红点提醒
	 * @param param
	 */
	public void checkRedNotice(Event param) {
		// new Object[] { jz.id, jz.level, jz })
		Object[] arr = (Object[]) param.param;
//		Integer lv = (Integer) arr[1];
		JunZhu jz = (JunZhu) arr[2];
		checkRedNotice(jz);
	}
	public void checkRedNotice(JunZhu jz){
		if(jz == null){
			return;
		}
		JNBean bean = HibernateUtil.find(JNBean.class, jz.id);
		if(bean == null){
			bean = getDefaultBean();
		}
		//
		@SuppressWarnings("unchecked")
		List<JiNengPeiYang> jiNengPeiYangList = TempletService
				.listAll(JiNengPeiYang.class.getSimpleName());
		boolean hit = false;
		for (JiNengPeiYang p : jiNengPeiYangList) {
			if(p.getQuality()==0){
				if(p.needLv == jz.level){
					//品质为0，且等级刚好达到，则是新获得技能
					addNewJn(jz, p);
				}
				continue;//品质0的技能，按等级开启，不提示。
			}
			if(jz.level<p.needLv){
				continue;//君主等级未达到，跳过
			}
			int curUseId = getCurId(bean, p);
			if(p.id>curUseId){
				//等级够，且技能id大于当前使用的id，则可进阶。
				hit = true;
				break;
			}
		}
		if(!hit){//没有可以进阶的技能
			return;
		}
		IoSession session = SessionManager.getInst().getIoSession(jz.id);
		if(session == null){
			return;
		}
		FunctionID.pushCanShowRed(jz.id, session, FunctionID.JiNengJinJie);
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.junzhu_level_up, this);
		EventMgr.regist(ED.JUNZHU_LEVEL_RANK_REFRESH, this);
	}

}
