package com.qx.world;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.GuanQiaJunZhu;
import com.manu.dynasty.template.JCZCity;
import com.manu.dynasty.template.JCZNpcTemp;
import com.manu.network.SessionAttKey;
import com.qx.fight.FightMgr;
import com.qx.junzhu.JunZhu;
import com.qx.pve.PveMgr;
import com.qx.robot.RobotSession;

import qxmobile.protobuf.PlayerData.State;

public class FightNPCScene extends FightScene{
	public static int limitNPCCnt = 999;//默认不限制
	public FightNPCScene(String sceneName, long fightEndTime, int id) {
		super(sceneName, fightEndTime, id);
		visibleDist = 30;
	}
	
	@Override
	public void completeMission(Mission mission) {
		super.completeMission(mission);
	}

	@Override
	public void bornAllNPC() {
		super.bornAllNPC();
		//
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
			p.name = HeroService.getNameById(t.name);
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
			p.jzId = -userId;
			p.jzlevel = jzConf.level;
			p.totalLife = p.currentLife = jzConf.shengming;
			p.allianceId = TEAM_RED;//守方
			session.setAttribute(SessionAttKey.playerId_Scene, userId);
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
	public void playerDie(Player defender, int killerUid) {
		super.playerDie(defender, killerUid);
		Player p = defender;
		if(p instanceof FightNPC){
			players.remove(defender.userId);
			removeVisibleIds(defender);
		}
	}
}
