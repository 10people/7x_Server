package com.qx.world;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;

import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.ExitScene;

public abstract class VisionScene extends Scene{

	public float visibleDist = 20;
	public int visibleCnt = 30;

	public VisionScene(String key) {
		super(key);
	}
	public abstract IoBuffer buildEnterInfoCache(Player p) ;
	
	/**
	 * 返回p2是否是cur的联盟的盟主或副盟主
	 * @param cur
	 * @param p2
	 * @return
	 */
	public boolean isLMBoss(Player cur, Player p2){
		if(cur.allianceId == p2.allianceId//同联盟 
				 &&(
					//	 (cur.zhiWu==1 || cur.zhiWu == 2)
					//	 	|| 
						 (p2.zhiWu==1 || p2.zhiWu == 2)	 
					)
			)	 {
			return true;
		}
		return false;
	}
	/**
	 * @Description 广播某人 进/出场景
	 * @param skip 某人userId
	 */
	@Override
	public void bdEnterInfo(Player enterPlayer) {
		IoBuffer io = buildEnterInfoCache(enterPlayer);
//		IoBuffer io = pack(enterYBSc.build(), PD.Enter_YBScene);
		log.info(" 广播userId=={} 进场景--{}",enterPlayer.userId,this.name);
		for(Player player : players.values()){
			//镖车机器人 跳过
			if(player.roleId==YBRobot_RoleId){
				continue;
			}
			
			if(isLMBoss(player, enterPlayer)){//enterP 是player的联盟长官，则强制显示
			}else if(enterPlayer.roleId !=YBRobot_RoleId && player.roleId !=YBRobot_RoleId
					&& player != enterPlayer){
				if(inRange(enterPlayer, player)==false){
					continue;
				}
				checkVisibleSet(player);
				//对方的visbileUids的改变在上一步一步【告知当前玩家场景里有谁】里面。
				player.visbileUids.add(enterPlayer.userId);
			}
			
			//TODO 等梁霄做成客户端请求仇人列表
			IoBuffer dup = io.asReadOnlyBuffer();
			dup.position(0);
			player.session.write(dup);
		}
	}
	/**
	 * @Description 告诉进入某个场景的人当前场景中都有谁
	 * @param msgId 进入场景的协议ID
	 * @param enterPlayer 进入的人的 Player对象
	 */
	public void informComerOtherPlayers(IoSession session,Player enterPlayer) {
		log.info("告诉进入某个场景的--{}当前场景--{}中都有谁，人数--{}", enterPlayer.name,this.name, players.size());
		checkVisibleSet(enterPlayer);
		for(Player p : players.values()){
			if(p == enterPlayer){
				continue;//跳过自己。
			}
			if(p.currentLife<=0 && p.roleId == TOWER_RoleId){
				continue;//不给客户端发送已死亡的塔信息。
			}
			if(isLMBoss(enterPlayer, p)){//player 是enterPlayer的联盟长官，则强制显示
			}else if(enterPlayer.roleId !=YBRobot_RoleId && p.roleId !=YBRobot_RoleId){
				if(inRange(enterPlayer, p)==false){
					continue;
				}
				//对方的visbileUids的改变在下一步【告知在线玩家谁进来了】里面。
				enterPlayer.visbileUids.add(p.userId);
			}
			
//			EnterScene.Builder enterSc = buildEnterInfo(p);
//			EnterSceneCache.Builder enterSc = buildEnterInfoCache(p);
//			ProtobufMsg pm = new ProtobufMsg();
//			pm.id = msgId;
//			pm.builder = enterSc;
//			session.write(pm);
			show(p,enterPlayer);
		}
	}

	public boolean inRange(Player cur, Player p2) {
			if(cur==null ||p2==null){
				return false;
			}
			if(cur==p2){
				return true;
			}
			if(cur.roleId ==TOWER_RoleId || p2.roleId ==TOWER_RoleId){
				return true;
			}
			if(cur.roleId ==YBRobot_RoleId || p2.roleId ==YBRobot_RoleId){
				return true;
			}
			if(p2 instanceof FenShenNPC){
				FenShenNPC fs = (FenShenNPC)p2;
				if(fs.fakeJz != null && fs.fakeJz.id == cur.jzId){
					fs.parentUid = cur.userId;//防止真身重新上线uid change
					return true;
				}
			}
				
	//		visibleDist=20;
			float dx = Math.abs(cur.posX - p2.posX);
			float dz = Math.abs(cur.posZ - p2.posZ);
			boolean visible = false;
			if(dx>visibleDist || dz>visibleDist){
				
			}else{
				visible = true;
				if( cur.visbileUids != null && cur.visbileUids.size()>=visibleCnt){
					//可见个数控制。
					visible = cur.visbileUids.contains(p2.userId);
				}else if( p2.visbileUids != null && p2.visbileUids.size()>=visibleCnt){
					visible = p2.visbileUids.contains(cur.userId);
				}
			}
			return visible;
		}

	public boolean checkVisibility(Player cur, Player p2) {
		if(cur == null){
			return true;
		}
		boolean v = inRange(cur, p2);
		if(cur.roleId ==YBRobot_RoleId || p2.roleId ==YBRobot_RoleId){
			//有马车参与，不做show、hidden的处理
			return v;
		}
		if(v){
			checkNeedShow(cur,p2);
		}else{
			//不在视野
			checkNeedHidden(cur,p2);
			if(isLMBoss(p2, cur)){
				v = true;
			}
		}
		return v;
	}

	public void checkNeedShow(Player cur, Player p2) {
		if(cur == p2){
			return;
		}
		// 原先不可见，现在可见了，则需要给客户端发送进入场景
		checkVisibleSet(cur);
		checkVisibleSet(p2);
//		if(isLMBoss(cur, p2) && ! p2.visbileUids.contains(cur.userId)){//p2是cur的联盟长官，但之前不可见
//		}else if(cur.visbileUids.contains(p2.userId)){
//			//之前就可见
//			return;
//		}
		//添加为互相可见。
		boolean a1 = cur.visbileUids.add(p2.userId);
		boolean a2 = p2.visbileUids.add(cur.userId);
		if(a1==false || a2 == false){//false就是之前添加过
			//log.warn("why?");
		}
		if(a2)
			show(cur,p2);
		if(a1)
			show(p2,cur);
		//cur的移动要回发给p2，p2的坐标要同步给cur
		/*发进入场景了，就不要这个了。
		SpriteMove.Builder move = SpriteMove.newBuilder();
		move.setDir(p2.userId%360);
		move.setUid(p2.userId);
		move.setPosX(p2.posX);
		move.setPosY(p2.posY);
		move.setPosZ(p2.posZ);
		SpriteMove build = move.build();
		cur.session.write(build);
		cur.session.write(build);
		*/
	}

	/**
	 *@Description 给对方发送进入场景 把 p2的信息发给 p1
	 * @param p2
	 * @param p1
	 */
	public void show(Player p2, Player p1) {
			//押镖场景有自己的实现，这个是给联盟战
		EnterScene.Builder msg = buildEnterInfo(p2);
			p1.session.write(msg.build());
	}

	public void checkVisibleSet(Player p1) {
		if(p1.visbileUids == null){
			p1.visbileUids = new HashSet<>();//打算在单线程里处理
		}
	}

	public void checkNeedHidden(Player p1, Player p2) {
		// 原先可见，现在不可见了，则需要给客户端发送离开场景。
		if(p1.visbileUids == null || p2.visbileUids == null){
			//以前不可见
			return;
		}
		if(p1.visbileUids.contains(p2.userId) == false){
			//以前不可见
			return;
		}
		if(isLMBoss(p2, p1)){//p1 是p2的联盟长官，则强制显示
		}else{
			hidden(p1,p2);
		}
		if(isLMBoss(p1, p2)){//p2 是p1的联盟长官，则强制显示
		}else{
			hidden(p2,p1);
		}
	}

	/**
	 * @Description 给a发送b离开场景消息
	 * @param b
	 * @param a
	 */
	public void hidden(Player b, Player a) {
		//押镖场景有自己的实现，这个是给联盟战
		a.visbileUids.remove(b.userId);
		ExitScene.Builder exitYBSc = ExitScene.newBuilder();
		exitYBSc.setUid(b.userId);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.Exit_Scene;
		pm.builder = exitYBSc;
		a.session.write(pm);
	}

	public void removeVisibleIds(Player p){
		if(p instanceof FightNPC){
			//战斗NPC不走exitScene，所以广播下离开。
			ExitScene.Builder exit = ExitScene.newBuilder();
			exit.setUid(p.userId);
			broadCastEvent(exit.build(), 0);
		}
		//
		if(p.visbileUids==null){
			return;
		}
		Iterator<Integer> it = p.visbileUids.iterator();
		while(it.hasNext()){
			//从其它玩家的可见id里移除当前player的id
			Integer pid = it.next();
			Player other = players.get(pid);
			if(other == null)continue;
			if(other.visbileUids==null)continue;
			other.visbileUids.remove(p.userId);
		}
	}
}
