package com.qx.world;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.GuanQiaJunZhu;
import com.manu.dynasty.template.JCZCity;
import com.manu.dynasty.template.JCZNpcTemp;
import com.manu.dynasty.template.LMZBuildingTemp;
import com.manu.network.SessionAttKey;
import com.qx.alliancefight.AllianceFightMgr;
import com.qx.fight.FightMgr;
import com.qx.junzhu.JunZhu;
import com.qx.pve.PveMgr;
import com.qx.robot.RobotSession;

import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.Scene.SpriteMove.Builder;

public class FightNPCScene extends FightScene{
	public float maxChaseDist = 15;
	public long preUpdateTime;
	public long updateInterval = 80;
	public static int limitNPCCnt = 999;//默认不限制
	public FightNPCScene(String sceneName, long fightEndTime, int id) {
		super(sceneName, fightEndTime, id);
		visibleDist = 30;
	}
	
	@Override
	public void completeMission(Mission mission) {
		super.completeMission(mission);
	}
	public void update() {
		super.update();
		long ms = System.currentTimeMillis();
		if(ms - preUpdateTime>=updateInterval){
			preUpdateTime = ms;
		}else{
			return;
		}
		for(Player p : players.values()){
			if(p instanceof FightNPC == false){
				continue;
			}
			FightNPC fp = (FightNPC) p;
			if(fp.userId==2){
//				fp.posX = fp.posY =fp.posZ=0;
			}

			if(fp.state == 2){
				chaseTo(fp,fp.temp.positionX,fp.temp.positionY);
			}
			if(fp.target==null)continue;
			if(fp.target.currentLife<=0 
					|| players.containsKey(fp.target.userId)==false){
				fp.target=null;
				continue;
			}
			if(fp.target != null){
				npcAttack(fp);
			}
		}
	}

	public void npcAttack(FightNPC fp) {
		Player target = fp.target;
		checkOutRange(fp, target);
		if(fp.state == 2){
			return;
		}
		boolean v=  inAtkRange(fp, target);
		if(v==false){
			return;
		}
		///
		long ms = System.currentTimeMillis();
		int[] skillIds = {151,181,171,101};
		int[] coolIds = {   0,  0,  0,  0 };
		Map<Integer, Long> skillCDMap = AllianceFightMgr.inst.skillCDTimeMap.get(fp.fakeJz.id);
		int coolIdx = 0;
		for(int id : skillIds){
			if(skillCDMap==null){
				coolIds[coolIdx++] = id;
				continue;
			}
			Long endTime = skillCDMap.get(id);
			if(endTime == null || endTime < ms) {
				coolIds[coolIdx++] = id;
			}
		}
		if(coolIdx == 0){//没有可用的技能
			return;
		}
		int rnd = (int)(ms % coolIdx);
		int skillId = skillIds[rnd];
		
		if(skillId==0){
			return;
		}
		if(target==null)return;
		if(skillId == 151){
			target = fp;
		}
		//触发技能
		FightMgr.inst.processAttackPlayer(fp.fakeJz, fp.session, this, fp, target, skillId);
	}

	@Override
	public void bornAllNPC() {
		List<JCZCity> cityList = TempletService.listAll(JCZCity.class.getSimpleName());
		Optional<JCZCity> op = cityList.stream().filter(c->c.id==cityId).findAny();
		if(op.isPresent()==false){
			log.error("没有找到城池配置 {}",cityId);
			return;
		}
		JCZCity city = op.get();
		List<JCZNpcTemp> list = TempletService.listAll(JCZNpcTemp.class.getSimpleName());
		if(list == null){
			return;
		}
//		List<LMZBuildingTemp> buildList = TempletService.listAll(LMZBuildingTemp.class.getSimpleName());
//		Iterator<LMZBuildingTemp> siteIt = buildList.stream().filter(b->b.type==2 || b.type==3).iterator();
		int bornCnt = 0;
		for(JCZNpcTemp t : list){
			if(t.npcId != city.npcId){
				continue;
			}
			GuanQiaJunZhu jzConf = PveMgr.inst.id2GuanQiaJunZhu.get(t.enemyId);
			if(jzConf == null){
				continue;
			}
			FightNPC p = new FightNPC();
			p.pState = State.State_LEAGUEOFCITY;
			p.safeArea=-1;
			p.temp = t;
			p.name = "NPC:"+t.id;
			p.posX = t.positionX;
			p.posZ = t.positionY;
			p.lmName = "燃烧军团";
			RobotSession session = new RobotSession();
			session.setAttribute(SessionAttKey.Scene, this);
			final int userId = getUserId();
			p.session=session;
			p.userId = userId;
			p.roleId = (userId % 4) + 1;
			p.fakeJz = makeFakeJz(t);
			p.fakeJz.id = -userId;
			p.totalLife = p.currentLife = 100;
			p.allianceId = TEAM_RED;//守方
			Object uidObject = session.setAttribute(SessionAttKey.playerId_Scene, userId);
			players.put(userId, p);
			bornCnt ++;
			if(bornCnt == limitNPCCnt){
				break;
			}
		}
	}
	
	public JunZhu makeFakeJz(JCZNpcTemp t) {
		JunZhu jz = new JunZhu();
		GuanQiaJunZhu jzConf = PveMgr.inst.id2GuanQiaJunZhu.get(t.enemyId);
		jz.shengMingMax = jz.shengMing = jzConf.shengming;
		jz.gongJi = jzConf.gongji;
		jz.fangYu = jzConf.fangyu;
		jz.wqBJ = jzConf.wqBJ;
		jz.wqSH = jzConf.wqSH;
		jz.wqJM = jzConf.wqJM;
		jz.wqRX = jzConf.wqRX;
		jz.jnBJ = jzConf.jnBJ;
		jz.jnSH = jzConf.jnSH;
		jz.jnJM = jzConf.jnJM;
		jz.jnRX = jzConf.jnRX;
		jz.id = -getUserId();
		return jz;
	}

	@Override
	public Player spriteMove(IoSession session, Builder move) {
		Player p = super.spriteMove(session, move);
		if(p instanceof FightNPC == false){
			//真实的玩家，则检查是否进入NPC的视野。
			checkNPCVision(p);
		}
		return p;
	}
	public void checkNPCVision(Player movedP) {
		for(Player p : players.values()){
			if(p instanceof FightNPC == false){
				continue;
			}
			FightNPC fp = (FightNPC) p;
			//p是NPC
			if(fp.target == movedP){
				checkOutRange(fp, movedP);
			}else if(fp.state == 2){
				
			}else if(fp.target==null){
				checkDist(fp, movedP);
			}
		}
	}
	public static int atkDist = 10;
	public boolean inAtkRange(Player cur, Player p2){
		if(cur==null ||p2==null){
			return false;
		}
		if(cur==p2){
			return true;
		}
		if(cur.roleId ==YBRobot_RoleId || p2.roleId ==YBRobot_RoleId){
			return true;
		}
			
//		visibleDist=20;
		float dx = Math.abs(cur.posX - p2.posX);
		float dz = Math.abs(cur.posZ - p2.posZ);
		boolean visible = false;
		if(dx>atkDist || dz>atkDist){
			
		}else{
			visible = true;
		}
		return visible;
	}
	public void checkDist(FightNPC fp, Player movedP) {
		if(movedP.visbileUids==null){
			return;
		}
		if(movedP.visbileUids.contains(fp.userId)==false){
			return;
		}
		if(Math.abs(fp.temp.positionX - movedP.posX)>maxChaseDist 
				|| Math.abs(fp.temp.positionY - movedP.posZ)>maxChaseDist){
			//超过最远追击距离，不追击。
			return;
		}
		fp.state=1;
		fp.target = movedP;
	}
	public void checkOutRange(FightNPC fp, Player movedP) {
		//追击
		float tgtX;// = movedP.posX;
		float tgtZ;// = movedP.posZ;
		if(fp.state == 2){
			tgtX = fp.temp.positionX;
			tgtZ = fp.temp.positionY;
		}else{
			tgtX = movedP.posX;
			tgtZ = movedP.posZ;
		}
		chaseTo(fp,tgtX,tgtZ);
	}
	
	/**
	 * 距离太远就会归位
	 * @param fp
	 * @param tgtX
	 * @param tgtZ
	 */
	public void chaseTo(FightNPC fp, float tgtX, float tgtZ) {
		double dx = Math.abs(tgtX - fp.posX);
		double dy = Math.abs(tgtZ - fp.posZ);
		if(fp.state ==1 && 
				(
					Math.abs(fp.temp.positionX - fp.posX)>maxChaseDist 
					|| Math.abs(fp.temp.positionY - fp.posZ)>maxChaseDist)
				){
			fp.target = null;
			fp.state = 2;//归位
			return;
		}
		
		double atan = Math.atan(dy/dx);
		double r = 0.5f;
		double sin = Math.sin(atan);
		double y = r * sin;
		double x = r * Math.cos(atan);
		double dir = atan/(Math.PI/2)*90;

		int xiangWei = 00;
		if(tgtZ<fp.posZ){
			y = -y;
			xiangWei += 01;
		}
		if(tgtX<fp.posX){
			x = -x;
			xiangWei += 10;
		}
//		xiangWei=99;//屏蔽相位处理
		switch(xiangWei){
		case 00:break;
		case 10:{
			dir = 180- dir;
			break;
		}
		case 11:{
			dir += 180;
			break;
		}
		case 01:{
			dir = 360-dir;
			break;
		}
		}
		float atkDis = 2;
		if(fp.state == 2){
			atkDis = 0.3f;//要返回原位
		}
		if(dx<atkDis && dy<atkDis){
			x = y = 0;//位于攻击范围内。
			if(fp.state == 2){
				fp.state = 0;
			}
		}
//		dir = 360-dir;
		///
		SpriteMove.Builder move = SpriteMove.newBuilder();
		move.setUid(fp.userId);
		float nx = (float) (fp.posX+x);
		move.setPosX(nx);
		move.setPosY(fp.posY);
		float nz = (float) (fp.posZ+y);
		move.setPosZ(nz);
		move.setDir((float) dir);
		spriteMove(fp.session, move);
//		fp.target = null;
//		log.info("{}chase {} {} to {},{}",fp.userId,x,y,move.getPosX(), move.getPosZ());		
	}
	@Override
	public void playerDie(JunZhu defender, int uid, int killerUid) {
		super.playerDie(defender, uid, killerUid);
		Player p = players.get(uid);
		if(p instanceof FightNPC){
			players.remove(uid);
		}
	}
}
