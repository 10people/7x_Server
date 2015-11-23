package com.qx.buff;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang.math.RandomUtils;
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
import com.qx.persistent.HibernateUtil;
import com.qx.util.RandomUtil;
import com.qx.world.FightScene;
import com.qx.world.Scene;

public class BuffMgr {
	private Logger logger = LoggerFactory.getLogger(BuffMgr.class); 
	
	public BuffMgr inst;

	public Map<Integer, Buff> buffConfigMap = new HashMap<Integer, Buff>();
	
	public Map<Integer, Action> actionConfigMap = new HashMap<Integer, Action>();
	
	public Map<Integer, Skill> skillConfigMap = new HashMap<Integer, Skill>();
	
	/** 君主buff， <junzhuId, UserBuffer对象> */
	public Map<Integer, UserBuffer> userBufferMap = new ConcurrentHashMap<Integer, UserBuffer>(); 
	
	public final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);
	
	public BuffMgr() {
		inst = new BuffMgr();
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
		userBuffer.addBuffer(buffer);
	}
	
	public void startWork() {
		Thread thread = new Thread(createWorkerThread(), "buffer-worker-thread");
		thread.start();
	}
	
	/**
	 * 获得线程工作者线程
	 * 
	 * @return 
	 */
	private Runnable createWorkerThread() {
		return new Runnable() {
			public void run() {
				while(true) {
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
			Set<Map.Entry<Integer, UserBuffer>> entrySet = userBufferMap.entrySet();
			for(Map.Entry<Integer, UserBuffer> entry : entrySet) {
				long junzhuId = entry.getKey();
				UserBuffer userBuffer = entry.getValue();

				boolean removeFromCache = false;
				List<Buffer> bufferList = userBuffer.getBufferList();
				for(Iterator<Buffer> iterator = bufferList.iterator(); ;iterator.hasNext()) {
					Buffer buffer = iterator.next();
					removeFromCache = processPlayerBuffer(junzhuId, buffer);
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
	private boolean processPlayerBuffer(long junzhuId, final Buffer buffer) {
		if(buffer == null) {
			return false;
		}
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId);
		if(junzhu == null) {
			return false;
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
			
			int type = buffer.getType();												//buff类型
			int period = buffer.getCycle();												//buff的计算周期
			int damage = buffer.getDamage();											//buff的增量或者减量
			long endTime = buffer.getEndTime();											//结束时间
			long currentTime = System.currentTimeMillis();								//当前时间
			long lastReduceTime = buffer.getLastCalcTime();								//上次计算的时间
			long reduceTime = endTime <= currentTime ? endTime : lastReduceTime;		//扣减的时间
			int reduceCount = (int)((currentTime - reduceTime) / period);				//计算需要扣减的次数
			if(reduceCount <= 0 && endTime > currentTime) {								//未超时, 直接跳过
				return false;
			}
			
			IoSession session = SessionManager.getInst().getIoSession(junzhu.id);
			long castId = buffer.getCastId();
			int totalTimes = reduceCount * period;										//总共扣减的时间. 单位:秒
			int totalDamage = reduceCount * damage;
			
			//更新上次扣除的时间
			buffer.setLastCalcTime(lastReduceTime + totalTimes);
			if(endTime <= currentTime) {
				flushable = true;
			}
			Scene scene = (Scene) session.getAttribute(SessionAttKey.playerId_Scene);
			if(scene != null && scene instanceof FightScene) {
				FightScene fightScene = (FightScene) scene;
				Integer remainLife = fightScene.junZhuRemainLifeMap.get(junzhu.id);
				remainLife -= totalDamage;
				fightScene.junZhuRemainLifeMap.put(junzhu.id, remainLife);
				if(totalDamage > 0) {
					// 向客户端推送buff信息
					BufferInfo.Builder bufferInfo = BufferInfo.newBuilder();
					bufferInfo.setBufferId(buffer.getId());
					bufferInfo.setDamage(totalDamage);
					bufferInfo.setRemainLife(remainLife);
					bufferInfo.setIsDead(remainLife > 0 ? true : false);
					session.write(bufferInfo.build());
				}
				if(remainLife <= 0) {
					BigSwitch.inst.allianceFightMgr.processPlayerDead(fightScene, junzhu);
				}
			}
			
		} catch(Exception e) {
			logger.error("buffer处理发生异常:{}", e);
		}
		return flushable;
	}
	
	public int calcWeaponDamage4Skill(JunZhu attacker, JunZhu defender, Skill skill, Action action) {
		int damage = 0;
		//JC = (a*A攻击*(A攻击+k)/(A攻击+B防御+k)*((A生命/c+k)* (B生命/c+k))^0.5/(B防御+k)*If(A生命>B生命，arctan(A生命/ B生命)*1.083+0.15，1)
		//浮点型四舍五入，保留2位小数。 a=1; c=20; k=10.
		int a = 1;
		int c = 20;
		int k = 10;
		double H = 1;
		if(attacker.shengMing > defender.shengMing) {
			// H，A生命>B生命，H=arctan(A生命/ B生命)*1.083+0.15,否则H=1
			H = Math.atan(attacker.shengMing / defender.shengMing) * 1.083 + 0.15;
		} 
		double JC = (a * attacker.gongJi * (attacker.gongJi + k)) / (attacker.gongJi + defender.fangYu + k)
				* Math.pow(((attacker.shengMing / c + k) * (defender.shengMing / c + k)), 0.5)
				/ H;
		JC = RandomUtil.getScaleValue(JC, 2);
		
		// WM = (L+A武器伤害加深)/( L +B武器伤害减免) ,L=500
		int L = 500;
		int WM = (L + attacker.wqSH) / (L + defender.wqJM);
		double SJ = RandomUtil.getRandomNum(0.9, 1.1);
		int X = action.Param1;
		int GD = action.Param2;
		// 武器未暴击伤害=INT((JC*X+ GD) *WM*SJ)
		damage = (int) ((JC * X + GD) * WM * SJ); 
		damage = Math.max(1, damage);
		
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
			int M = 100;
			double WB = (M + attacker.wqBJ) / (M + defender.wqRX);
			int addValue = (int) (JC * X * WB * SJ);
			damage += addValue;
		}
		return damage;
	}
	
	public int calcSkillDamage4Skill(JunZhu attacker, JunZhu defender, Skill skill, Action action) {
		int damage = 0;
		//JC = (a*A攻击*(A攻击+k)/(A攻击+B防御+k)*((A生命/c+k)* (B生命/c+k))^0.5/(B防御+k)*If(A生命>B生命，arctan(A生命/ B生命)*1.083+0.15，1)
		//浮点型四舍五入，保留2位小数。 a=1; c=20; k=10.
		int a = 1;
		int c = 20;
		int k = 10;
		double H = 1;
		if(attacker.shengMing > defender.shengMing) {
			// H，A生命>B生命，H=arctan(A生命/ B生命)*1.083+0.15,否则H=1
			H = Math.atan(attacker.shengMing / defender.shengMing) * 1.083 + 0.15;
		} 
		double JC = (a * attacker.gongJi * (attacker.gongJi + k)) / (attacker.gongJi + defender.fangYu + k)
				* Math.pow(((attacker.shengMing / c + k) * (defender.shengMing / c + k)), 0.5)
				/ H;
		JC = RandomUtil.getScaleValue(JC, 2);
		
		// JM = (L +A技能伤害加深)/( L +B技能伤害减免),L=500
		int L = 500;
		int JM = (L + attacker.jnSH) / (L + defender.jnJM);
		double SJ = RandomUtil.getRandomNum(0.9, 1.1);
		int Y = action.Param1;
		int GD = action.Param2;
		// 技能未暴击伤害=INT((JC*Y+ GD) *JM*SJ)
		damage = (int) ((JC * Y + GD) * JM * SJ); 
		damage = Math.max(1, damage);
		
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
			int M = 100;
			double JB = (M + attacker.jnBJ) / (M + defender.jnRX);
			int addValue = (int) (JC * Y * JB * SJ);
			damage += addValue;
		}
		return damage; 
	}
	
}
