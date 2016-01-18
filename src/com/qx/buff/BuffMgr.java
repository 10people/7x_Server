package com.qx.buff;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceFightProtos.BufferInfo;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.Action;
import com.manu.dynasty.template.Buff;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.Skill;
import com.manu.network.BigSwitch;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.qx.junzhu.JunZhu;
import com.qx.util.RandomUtil;
import com.qx.world.Player;
import com.qx.world.Scene;

public class BuffMgr {
	private Logger logger = LoggerFactory.getLogger(BuffMgr.class); 
	
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

	private void initData() {
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
	private Runnable createWorkerThread() {
		return new Runnable() {
			public void run() {
				while(buffProcess) {
					try {
						processUserBuffer();
						Thread.sleep(100);
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
			for(Map.Entry<Long, UserBuffer> entry : entrySet) {
				UserBuffer userBuffer = entry.getValue();

				boolean removeFromCache = false;			// 是否从移除该buff
				List<Buffer> bufferList = userBuffer.getBufferList();
				for(Iterator<Buffer> iterator = bufferList.iterator(); iterator.hasNext();) {
					Buffer buffer = iterator.next();
					removeFromCache = processPlayerBuffer(buffer);
					if(removeFromCache) {
						iterator.remove();
					}
				}
			}
		}
	}
	
	/**
	 * 处理BUFF
	 * 
	 */
	private boolean processPlayerBuffer(final Buffer buffer) {
		if(buffer == null) {
			return true;
		}
		
		boolean flushable = false;
		try {
			//BUFF还没生效.
			if(!buffer.isStart()) {
				return false;
			}
			
			if(buffer.isTimeOut()) {
				return true;
			}
			
			Buff buff = getBuffById(buffer.getId());
			if(buff == null) {
				logger.error("buff计算失败，找不到配置，buffId:{}", buffer.getId());
				return true;
			}
			
			// TODO 计算一次必须效果，要是循环效果，则不进行此项计算
																//该buff作用的数量值
			JunZhu caster = null;
			JunZhu target = null;
			if(buff.Caster == 0) {			// 自己是施放者
				caster = buffer.getCarryJunzhu();
				target = buffer.getCastJunzhu();
			} else if(buff.Caster == 1) { 	// 给我加buff的那个人是施放者
				caster = buffer.getCastJunzhu();
				target = buffer.getCarryJunzhu();
			}
			
			int effectCycle = buff.EffectCycle;											//buff作用间隔
			long endTime = buffer.getEndTime();											//结束时间
			long currentTime = System.currentTimeMillis();								//当前时间
			long lastCalcTime = buffer.getLastCalcTime();								//上次计算的时间
			long effectTime = endTime <= currentTime ? endTime : lastCalcTime;			//当前计算用的时间
			int effectCount =  (int) ((currentTime - effectTime)/effectCycle); 			//至本次计算应该作用的次数
			Skill skill = getSkillById(buff.SkillId);
			if(skill == null) {
				return true;
			}
			
			IoSession session = SessionManager.getInst().getIoSession(target.id);
			if(session == null) {
				return true;
			}
			Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
			Player targetPlayer = scene.players.get(buffer.getSceneUid());
			if(targetPlayer == null) {
				logger.info("场景:{} 找不到uid:{}，君主id:{}的玩家", scene.name, buffer.getSceneUid(), buffer.getCarryJunzhu());
				return true;
			}
			int totalDamage = 0;	
			totalDamage += (calcSkillDamage(caster, target, skill, buffer.getSceneUid()) * effectCount);
			int totalTime = effectCount * effectCycle;
			//更新上次扣除的时间
			buffer.setLastCalcTime(lastCalcTime + totalTime);
			if(endTime <= currentTime) {
				flushable = true;
			}
			
			if(scene != null){ 
				processSkillEffect(totalDamage, targetPlayer, skill);
				if(effectCount > 0) {
					BufferInfo.Builder bufferInfo = BufferInfo.newBuilder();
					bufferInfo.setTargetId(targetPlayer.userId);
					bufferInfo.setBufferId(buffer.getId());
					bufferInfo.setValue(totalDamage);
					bufferInfo.setRemainLife(targetPlayer.currentLife);
					for(Player p : scene.players.values()) {
						IoSession sess = p.session;   
						if(sess != null) {
							sess.write(bufferInfo.build());
						}
					}
				}
			}
			
		} catch(Exception e) {
			logger.error("buffer处理发生异常:{}", e);
		}
		return flushable;
	}

	public Player processSkillEffect(int value, Player player, Skill skill) {
		Action action = getActionById(skill.Action1);
		switch(action.TypeKey) {
			case 1://1.武器伤害攻击
			case 2://2.技能伤害攻击
				player.currentLife -= value;
				player.currentLife = Math.max(player.currentLife, 0);
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
	
	public int calcSkillDamage(JunZhu attacker, JunZhu defender, Skill skill, int uid) {
		int damageValue = 0;
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
		long endTime = System.currentTimeMillis() + buffDuration;
		Buffer buffer = Buffer.valueOf(buff.BuffId, buff.IsDebuff, buff.EffectTime, buff.Attr_1_P1, buffDuration, endTime, attacker, defender, uid);
		BigSwitch.inst.buffMgr.addBuffer(defender.id, buffer);
	}
	
	private int calcSkillTreatLife(JunZhu attacker, JunZhu defender, Skill skill, Action action) {
		double j = action.Param2;
		double k = action.Param1 / 1000;
		int addLife = (int) (k * defender.shengMingMax + j);
		return addLife;
	}

	public int calcWeaponDamage4Skill(JunZhu attacker, JunZhu defender, Skill skill, Action action) {
		int damage = 0;
		//JC = (a*A攻击*(A攻击+k)/(A攻击+B防御+k)*((A生命/c+k)* (B生命/c+k))^0.5/(B防御+k)*H
		//H=If(A生命>B生命){arctan(A生命/ B生命)*1.083+0.15}  else{1}
		//浮点型四舍五入，保留2位小数。 a=1; c=20; k=10.
		double a = 1;
		double c = 20;
		double k = 10;
		double H = 1;
		if(attacker.shengMingMax > defender.shengMingMax) {
			H = Math.atan(attacker.shengMingMax / defender.shengMingMax) * 1.083 + 0.15;
		} 
		double JC = (a * attacker.gongJi * (attacker.gongJi + k)) / (attacker.gongJi + defender.fangYu + k)
				* Math.pow(((attacker.shengMingMax / c + k) * (defender.shengMingMax / c + k)), 0.5)
				/ (defender.fangYu + k)
				* H;
		JC = RandomUtil.getScaleValue(JC, 2);
		
		// WM = (L+A武器伤害加深)/( L +B武器伤害减免) ,L=500
		double L = 500;
		double WM = (L + attacker.wqSH) / (L + defender.wqJM);
		double SJ = RandomUtil.getRandomNum(0.9, 1.1);
		double X = action.Param1 / 1000;
		double GD = action.Param2;
		// 武器未暴击伤害=INT((JC*X+ GD) *WM*SJ)
		damage = (int) ((JC * X + GD) * WM * SJ); 
		damage = Math.max(1, damage);
//		logger.info("未暴击--a:{},c:{},k:{},H:{},jc:{},L:{},WM:{},SJ:{},X:{},GD:{},damage:{}",
//				a,c,k,H,JC,L,WM,SJ,X,GD,damage);
		
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
			int addValue = (int) (JC * X * WB * SJ);
			damage += addValue;
//			logger.info("暴击--M:{},WB:{},addValue:{},damage:{},",
//					M,WB,addValue,damage);
		}
		return damage;
	}
	
	public int calcSkillDamage4Skill(JunZhu attacker, JunZhu defender, Skill skill, Action action) {
		int damage = 0;
		//JC = (a*A攻击*(A攻击+k)/(A攻击+B防御+k)*((A生命/c+k)* (B生命/c+k))^0.5/(B防御+k)*If(A生命>B生命，arctan(A生命/ B生命)*1.083+0.15，1)
		//浮点型四舍五入，保留2位小数。 a=1; c=20; k=10.
		double a = 1;
		double c = 20;
		double k = 10;
		double H = 1;
		if(attacker.shengMingMax > defender.shengMingMax) {
			// H，A生命>B生命，H=arctan(A生命/ B生命)*1.083+0.15,否则H=1
			H = Math.atan(attacker.shengMingMax / defender.shengMingMax) * 1.083 + 0.15;
		} 
		double JC = (a * attacker.gongJi * (attacker.gongJi + k)) / (attacker.gongJi + defender.fangYu + k)
				* Math.pow(((attacker.shengMingMax / c + k) * (defender.shengMingMax / c + k)), 0.5)
				/ (defender.fangYu + k)
				* H;
		JC = RandomUtil.getScaleValue(JC, 2);
		
		// JM = (L +A技能伤害加深)/( L +B技能伤害减免),L=500
		double L = 500;
		double JM = (L + attacker.jnSH) / (L + defender.jnJM);
		double SJ = RandomUtil.getRandomNum(0.9, 1.1);
		double Y = action.Param1 / 1000;
		double GD = action.Param2;
		// 技能未暴击伤害=INT((JC*Y+ GD) *JM*SJ)
		damage = (int) ((JC * Y + GD) * JM * SJ); 
		damage = Math.max(1, damage);
//		logger.info("未暴击--a:{},c:{},k:{},H:{},jc:{},L:{},JM:{},SJ:{},Y:{},GD:{},damage:{}",
//				a,c,k,H,JC,L,JM	,SJ,Y,GD,damage);
		
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
			int addValue = (int) (JC * Y * JB * SJ);
			damage += addValue;
//			logger.info("暴击--M:{},JB:{},addValue:{},damage:{},",
//					M,JB,addValue,damage);
		}
		return damage; 
	}
	
}
