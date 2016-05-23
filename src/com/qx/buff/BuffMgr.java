package com.qx.buff;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.Action;
import com.manu.dynasty.template.Buff;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.Skill;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.util.RandomUtil;
import com.qx.world.CallbacMission;
import com.qx.world.FightNPC;
import com.qx.world.FightScene;
import com.qx.world.Player;
import com.qx.world.Scene;

import qxmobile.protobuf.AllianceFightProtos.BufferInfo;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

public class BuffMgr {
	public Logger logger = LoggerFactory.getLogger(BuffMgr.class); 
	
	public static BuffMgr inst;
	
	public boolean buffProcess = true;

	public Map<Integer, Buff> buffConfigMap = new HashMap<Integer, Buff>();
	
	public Map<Integer, Action> actionConfigMap = new HashMap<Integer, Action>();
	
	public Map<Integer, Skill> skillConfigMap = new HashMap<Integer, Skill>();
	
	/** 君主buff， <junzhuId, UserBuffer对象> */
	public Map<Long, UserBuffer> userBufferMap = new ConcurrentHashMap<Long, UserBuffer>(); 
	
	public final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);
	
	public BuffMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		List<Buff> list = TempletService.listAll(Buff.class.getSimpleName());
		Map<Integer, Buff> buffConfigMap = new HashMap<Integer, Buff>();
		for(Buff buff : list) {
			buffConfigMap.put(buff.BuffId, buff);
		}
		this.buffConfigMap = buffConfigMap;
		
		List<Action> actionList = TempletService.listAll(Action.class.getSimpleName());
		Map<Integer, Action> actionConfigMap = new HashMap<Integer, Action>();
		for(Action action : actionList) {
			actionConfigMap.put(action.Id, action);
		}
		this.actionConfigMap = actionConfigMap;

		List<Skill> skillList = TempletService.listAll(Skill.class.getSimpleName());
		Map<Integer, Skill> skillConfigMap = new HashMap<Integer, Skill>();
		for(Skill skill : skillList) {
			skillConfigMap.put(skill.SkillId, skill);
		}
		this.skillConfigMap = skillConfigMap;
	}
	
	public Skill getSkillById(int skillId) {
		return skillConfigMap.get(skillId);
	}

	public Action getActionById(int actionId) {
		return actionConfigMap.get(actionId);
	}

	public Buff getBuffById(int buffId) {
		return buffConfigMap.get(buffId);
	}
	
	public void addBuffer(long junzhuId, Buffer buffer) {
		UserBuffer userBuffer = userBufferMap.get(junzhuId);
		if(userBuffer == null) {
			synchronized (userBufferMap) {
				if(userBuffer == null) {
					userBuffer = new UserBuffer(junzhuId);
					userBufferMap.put(junzhuId, userBuffer);
				}
			}
		}
		userBuffer.addBuffer(buffer);
	}
	
	public void removeBuff(long junzhuId) {
		synchronized (userBufferMap) {
			userBufferMap.remove(junzhuId);
		}
	}
	
	public void startWork() {
		Thread thread = new Thread(createWorkerThread(), "buffer-worker-thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void shutDown() {
		buffProcess = false;
	}
	
	/**
	 * 获得线程工作者线程
	 * 
	 * @return 
	 */
	public Runnable createWorkerThread() {
		return new Runnable() {
			public void run() {
				while(buffProcess) {
					try {
						processUserBuffer();
						Thread.sleep(50);
					} catch (Exception e) {
						logger.error("buffer工作者线程执行处理异常:{}", e);
					}
				}
			}

		};
	}
	
	protected void processUserBuffer() {
		synchronized (userBufferMap) {
			Set<Map.Entry<Long, UserBuffer>> entrySet = userBufferMap.entrySet();
			Iterator<Entry<Long, UserBuffer>> it = entrySet.iterator();
			while(it.hasNext()){
				Map.Entry<Long, UserBuffer> entry = it.next(); 
				UserBuffer userBuffer = entry.getValue();

				List<Buffer> bufferList = userBuffer.getBufferList();
				
				for(Iterator<Buffer> iterator = bufferList.iterator(); iterator.hasNext();) {
					Buffer buffer = iterator.next();
					processPlayerBuffer(buffer);
					if(buffer.stop) {
						iterator.remove();
					}
				}
				if(bufferList.isEmpty()){
					it.remove();
				}
			}
		}
	}
	
	/**
	 * 处理BUFF
	 * 
	 */
	public void processPlayerBuffer(final Buffer buffer) {
		if(buffer == null) {
			return ;
		}
		
		try {
			
			Buff buff = buffer.buffConf;
			
			// TODO 计算一次必须效果，要是循环效果，则不进行此项计算
																//该buff作用的数量值
			int effectCycle = buff.EffectCycle;											//buff作用间隔
			long endTime = buffer.endTime;											//结束时间
			long currentTime = System.currentTimeMillis();								//当前时间
			long lastCalcTime = buffer.lastCalcTime;								//上次计算的时间
			long timeDiff = currentTime - lastCalcTime;
			if(timeDiff<effectCycle){
				return;//不够一个时间间隔
			}//当前计算用的时间
			Skill skill = getSkillById(buff.SkillId);
			if(skill == null) {
				buffer.stop = true;
				return ;
			}
			//
			JunZhu caster = null;
			JunZhu target = null;
			if(buff.SkillId== 191){//绝影星光斩
				caster = buffer.castJunzhu;
				target = buffer.carryJunzhu;
			}else if(buff.Caster == 0) {			// 自己是施放者
				caster = buffer.carryJunzhu;
				target = buffer.castJunzhu;
			} else if(buff.Caster == 1) { 	// 给我加buff的那个人是施放者
				caster = buffer.castJunzhu;
				target = buffer.carryJunzhu;
			}
			
			IoSession session = SessionManager.getInst().getIoSession(target.id);
			if(session == null) {
				buffer.stop = true;
				return ;
			}
			//更新上次扣除的时间
			buffer.lastCalcTime += effectCycle;
			Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
			if(endTime <= buffer.lastCalcTime) {
				buffer.stop = true;
				notifyStopBuff(buffer, scene);
			}else{
				switch(buffer.buffConf.BuffId){
				case 151:
					CallbacMission e = new CallbacMission();
					e.c = ()->{procAOE(buffer, skill, scene);};
					scene.missions.add(e);
					break;
				default:
					procForTarget(buffer, caster, target, 1, skill, scene);
					break;
				}
			}
		} catch(Exception e) {
			logger.error("buffer处理发生异常:{}", e);
		}
	}

	public void notifyStopBuff(final Buffer buffer, Scene scene) {
		ErrorMessage.Builder stop = ErrorMessage.newBuilder();
		int who = buffer.carryPlayer != null ? buffer.carryPlayer.userId : buffer.sceneUid;
		stop.setErrorCode(who);
		ProtobufMsg msg = new ProtobufMsg(PD.SKILL_STOP, stop);
		scene.broadCastEvent(msg, 0);
	}

	public void procAOE(Buffer buffer, Skill skill, Scene scene) {
//		logger.info("处理AOE {}",buffer);
		JunZhu caster = buffer.carryJunzhu;
		Player cp = buffer.carryPlayer;
		if(cp == null){
			cp = buffer.carryPlayer = scene.players.get(buffer.sceneUid);
		}
		if(cp == null){
			buffer.stop = true;
			return;
		}
		float range = 5f;//skill.ET_P1;
		for(Player p : scene.players.values()){
			if(p==cp)continue;
			if(p.currentLife<=0){
				continue;
			}
			if(cp.allianceId == p.allianceId){
				//友方
				continue;
			}
			float dx = Math.abs(p.posX - cp.posX);
			float dz = Math.abs(p.posZ - cp.posZ);
			if(dx>range || dz>range){
				continue;
			}
			buffer.sceneUid = p.userId;
			JunZhu target = null;
			if(p instanceof FightNPC){
				target = ((FightNPC)p).fakeJz;
			}else{
				target = JunZhuMgr.inst.getJunZhu(p.session);
			}
			if(target == null){
				continue;
			}
			procForTarget(buffer, caster, target, 1, skill, scene);
		}
	}

	public void procForTarget(final Buffer buffer, JunZhu caster, JunZhu target, int effectCount, Skill skill,
			Scene scene) {
		if(effectCount == 0){
			return;
		}
		Player targetPlayer = scene.players.get(buffer.sceneUid);
		if(targetPlayer == null) {
			logger.info("场景:{} 找不到uid:{}，君主id:{}的玩家", scene.name, buffer.sceneUid, buffer.carryJunzhu);
			buffer.stop = true;
			return;
		}
		int totalDamage = 0;	
		totalDamage += (calcSkillDamage(caster, target, skill, buffer.sceneUid) * effectCount);
		
		if(scene != null){ 
			processSkillEffect(totalDamage, targetPlayer, skill);
			if(effectCount > 0) {
				BufferInfo build = buildDmgInfo(buffer, targetPlayer, totalDamage);
				scene.broadCastEvent(build, 0);
			}
			if(targetPlayer.currentLife <= 0) {
				Player attackPlayer = scene.getPlayerByJunZhuId(caster.id);
				int atkUid = 0;
				if(attackPlayer!=null){
					atkUid = attackPlayer.userId;
				}
				scene.playerDie(target, targetPlayer.userId, atkUid);
			}
		}
	}

	public BufferInfo buildDmgInfo(final Buffer buffer, Player targetPlayer, int totalDamage) {
		BufferInfo.Builder bufferInfo = BufferInfo.newBuilder();
		bufferInfo.setTargetId(targetPlayer.userId);
		//171是联盟战基地buff
		bufferInfo.setBufferId(buffer==null ? 171 : buffer.buffConf.BuffId);
		bufferInfo.setValue(totalDamage);
		bufferInfo.setRemainLife(targetPlayer.currentLife);
		BufferInfo build = bufferInfo.build();
		return build;
	}

	public Player processSkillEffect(long value, Player player, Skill skill) {
		Action action = getActionById(skill.Action1);
		switch(action.TypeKey) {
			case 1://1.武器伤害攻击
			case 2://2.技能伤害攻击
				player.currentLife -= value;
				player.currentLife = Math.max(player.currentLife, 0);
				if(player.currentLife > player.totalLife) {
					player.currentLife = player.totalLife;
				}
				break;
			case 3://回复血量
				player.currentLife += value;
				player.currentLife = Math.min(player.currentLife, player.totalLife);
				break;
			case 4://buff
			case 5://debuff
				break;
			default:
				logger.error("buff处理失败，找不到对用的actiontype, actionId:{},actionTypeKey:{}",
						action.Id, action.TypeKey);
				break;
		}
		return player;
	}
	
	public long calcSkillDamage(JunZhu attacker, JunZhu defender, Skill skill, int uid) {
		long damageValue = 0;
		Action action = getActionById(skill.Action1);
		if(action == null) {
			return 0;
		}
		SkillActionType skillActionType = SkillActionType.getSkillActionType(action.TypeKey);
		if(skillActionType == null) {
			return 0;
		}
		// 计算技能效果概率
		int prob = RandomUtil.getRandomNum(1000);
		if(prob > action.Prob) {
			return 0;
		}
		
		if(4 == action.TypeKey || 5 == action.TypeKey) {// 表示是buff效果
			processBuff4Skill(attacker, defender, action, uid);
		} else if(1 == action.TypeKey) {
			damageValue = calcWeaponDamage4Skill(attacker, defender, skill, action);
		} else if(2 == action.TypeKey) {
			damageValue = calcSkillDamage4Skill(attacker, defender, skill, action);
		} else if(3 == action.TypeKey) {
			damageValue = calcSkillTreatLife(attacker, defender, skill, action);
		}
		return damageValue;
	}

	protected void processBuff4Skill(JunZhu attacker, JunZhu defender,
			Action action, int uid) {
		int buffId = (int) action.Param1;									// buffId
		int buffDuration = (int) action.Param2;									// buff持续时间
		Buff buff = getBuffById(buffId);
		if(buff == null) {
			return;
		}
		if(buffDuration == 0) {
			buffDuration = buff.BuffDuration;
		}
		// buff结束时间
		Buffer buffer = Buffer.valueOf(buff, buffDuration, attacker, defender, uid);
		long buffOnPid = 0;
		if(action.Id == 151){
			buffOnPid = attacker.id;
			buffer.carryJunzhu = attacker;
		}else{
			buffOnPid = defender.id;
		}
		BigSwitch.inst.buffMgr.addBuffer(buffOnPid, buffer);
	}
	
	public int calcSkillTreatLife(JunZhu attacker, JunZhu defender, Skill skill, Action action) {
		double j = action.Param2;
		double k = action.Param1 / 1000;
		int addLife = (int) (k * defender.shengMingMax + j);
		return addLife;
	}

	public long calcWeaponDamage4Skill(JunZhu attacker, JunZhu defender, Skill skill, Action action) {
		long damage = 0;
		//JC = (a*A攻击*(A攻击+k)/(A攻击+B防御+k)*((A生命/c+k)* (B生命/c+k))^0.5/(B防御+k)*H
		//H=If(A生命>B生命){arctan(A生命/ B生命)*1.083+0.15}  else{1}
		//浮点型四舍五入，保留2位小数。 a=1; c=20; k=10.
		double a = 1;
		double c = 20;
		double k = 10;
		double H = 1;
		int atkHpMax = attacker.shengMingMax;
		int defHpMax = defender.shengMingMax;
		if(atkHpMax > defHpMax) {
			H = Math.atan(atkHpMax / defHpMax) * 1.083 + 0.15;
		} 
		int atkGJ = attacker.gongJi;
		int defFY = defender.fangYu;
		//联盟战buff
		int p = getLMZBuff(attacker);
		atkHpMax += atkHpMax * p /100;
		atkGJ += atkGJ * p / 100;
		
		p = getLMZBuff(defender);
		defHpMax += defHpMax * p / 100;
		defFY += defFY * p /100;
		
		double JC = (a * atkGJ * (atkGJ + k)) / (atkGJ + defFY + k)
				* Math.pow(((atkHpMax / c + k) * (defHpMax / c + k)), 0.5)
				/ (defFY + k)
				* H;
		JC = RandomUtil.getScaleValue(JC, 2);
		
		// WM = (L+A武器伤害加深)/( L +B武器伤害减免) ,L=500
		double L = 500;
		double WM = (L + attacker.wqSH) / (L + defender.wqJM);
		double SJ = RandomUtil.getRandomNum(0.9, 1.1);
		double X = action.Param1 / 1000;
		double GD = action.Param2;
		// 武器未暴击伤害=INT((JC*X+ GD) *WM*SJ)
		damage = (long) ((JC * X + GD) * WM * SJ); 
		damage = Math.max(1, damage);
//		logger.info("攻击者君主:{},被攻击者:{}",attacker.id, defender.id);
//		logger.info("普通攻击,攻击者id:{},被攻击者id:{},未暴击--a:{},c:{},k:{},H:{},jc:{},L:{},WM:{},SJ:{},X:{},GD:{},damage:{},attacker.wqSH:{},defender.wqJM:{}",
//				attacker.id, defender.id, a,c,k,H,JC,L,WM,SJ,X,GD,damage,attacker.wqSH,defender.wqJM);
		
		// 计算武器是否暴击了
		boolean critical = false; 						// 武器是否暴击
		double criticalProb = CanShu.WUQI_BAOJILV;			
		double ranCriticalProb = RandomUtil.getScaleValue(Math.random(), 2);
		int criticalProbInt = (int) (criticalProb * 100);
		int ranCriticalProbInt = (int) (ranCriticalProb * 100);
		if(ranCriticalProbInt < criticalProbInt) {
			critical = true;
		}
		if(critical) {
			//武器暴击伤害=武器未暴击伤害+INT(JC*X*WB*SJ)
			//WB = (M +A武器暴击加深)/( M +B武器暴击减免), M=100
			double M = 100;
			double WB = (M + attacker.wqBJ) / (M + defender.wqRX);
			long addValue = (long) (JC * X * WB * SJ);
			damage += addValue;
//			logger.info("普通攻击,攻击者id:{},被攻击者id:{},造成暴击--M:{},WB:{},addValue:{},damage:{},,attacker.wqSH:{},defender.wqJM:{}",
//					attacker.id, defender.id,M,WB,addValue,damage,attacker.wqSH,defender.wqJM);
		}
		return damage;
	}
	public int getLMZBuff(JunZhu jz){
		SessionUser ss = SessionManager.inst.sessionMap.get(jz.id);
		if(ss != null){
			Integer percent = (Integer)ss.session.getAttribute("fixTowerBuff");
			if(percent != null){
				return percent;
			}
		}
		return 0;
	}
	public long calcSkillDamage4Skill(JunZhu attacker, JunZhu defender, Skill skill, Action action) {
		long damage = 0;
		//JC = (a*A攻击*(A攻击+k)/(A攻击+B防御+k)*((A生命/c+k)* (B生命/c+k))^0.5/(B防御+k)*If(A生命>B生命，arctan(A生命/ B生命)*1.083+0.15，1)
		//浮点型四舍五入，保留2位小数。 a=1; c=20; k=10.
		double a = 1;
		double c = 20;
		double k = 10;
		double H = 1;
		int atkHpMax = attacker.shengMingMax;
		int defHpMax = defender.shengMingMax;
		int atkGJ = attacker.gongJi;
		int defFY = defender.fangYu;
		//联盟战buff
		int p = getLMZBuff(attacker);
		atkHpMax += atkHpMax * p /100;
		atkGJ += atkGJ * p / 100;
		
		p = getLMZBuff(defender);
		defHpMax += defHpMax * p / 100;
		defFY += defFY * p /100;
		
		if(atkHpMax > defHpMax) {
			// H，A生命>B生命，H=arctan(A生命/ B生命)*1.083+0.15,否则H=1
			H = Math.atan(atkHpMax / defHpMax) * 1.083 + 0.15;
		} 
		double JC = (a * atkGJ * (atkGJ + k)) / (atkGJ + defFY + k)
				* Math.pow(((atkHpMax / c + k) * (defHpMax / c + k)), 0.5)
				/ (defFY + k)
				* H;
		JC = RandomUtil.getScaleValue(JC, 2);
		
		// JM = (L +A技能伤害加深)/( L +B技能伤害减免),L=500
		double L = 500;
		double JM = (L + attacker.jnSH) / (L + defender.jnJM);
		double SJ = RandomUtil.getRandomNum(0.9, 1.1);
		double Y = action.Param1 / 1000;
		double GD = action.Param2;
		// 技能未暴击伤害=INT((JC*Y+ GD) *JM*SJ)
		damage = (long) ((JC * Y + GD) * JM * SJ); 
		damage = Math.max(1, damage);
//		logger.info("致命一击，攻击者id:{},被攻击者id:{},未暴击--a:{},c:{},k:{},H:{},jc:{},L:{},JM:{},SJ:{},Y:{},GD:{},damage:{}",
//				attacker.id, defender.id,a,c,k,H,JC,L,JM	,SJ,Y,GD,damage);
		
		// 计算武器是否暴击了
		boolean critical = false; 				// 武器是否暴击
		double criticalProb = CanShu.JINENG_BAOJILV;
		double ranCriticalProb = RandomUtil.getScaleValue(Math.random(), 2);
		int criticalProbInt = (int) (criticalProb * 100);
		int ranCriticalProbInt = (int) (ranCriticalProb * 100);
		if(ranCriticalProbInt < criticalProbInt) {
			critical = true;
		}
		if(critical) {
			//技能暴击伤害 = 技能未暴击伤害+INT((JC*Y+ GD) *JB*SJ)
			//JB = (M+A技能暴击加深)/(M+B技能暴击减免), M=100
			double M = 100;
			double JB = (M + attacker.jnBJ) / (M + defender.jnRX);
			long addValue = (long) (JC * Y * JB * SJ);
			damage += addValue;
//			logger.info("致命一击，攻击者id:{},被攻击者id:{},造成暴击--M:{},JB:{},addValue:{},damage:{},",
//					attacker.id, defender.id,M,JB,addValue,damage);
		}
		return damage; 
	}
	
}
