<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.bag.EquipGrid"%>
<%@page import="com.qx.bag.EquipMgr"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="qxmobile.protobuf.JunZhuProto.JunZhuInfoRet"%>
<%@page import="com.qx.persistent.MC"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.manu.dynasty.util.MathUtils"%>
<%@page import="com.qx.ranking.RankingGongJinMgr"%>
<%@page import="qxmobile.protobuf.Ranking.GongJinInfo"%>
<%@page import="com.qx.task.DailyTaskMgr"%>
<%@page import="com.qx.task.DailyTaskBean"%>
<%@page import="com.qx.task.WorkTaskBean"%>
<%@page import="com.qx.task.GameTaskMgr"%>
<%@page import="com.manu.dynasty.template.ZhuXian"%>
<%@page import="com.qx.mibao.MiBaoSkillDB"%>
<%@page import="java.util.Date"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.bag.BagGrid"%>
<%@page import="com.qx.bag.Bag"%>
<%@page import="com.qx.bag.BagMgr"%>
<%@page import="com.qx.fuwen.FuwenMgr"%>
<%@page import="com.qx.pvp.ZhanDouRecord"%>
<%@page import="com.qx.yuanbao.YuanBaoInfo"%>
<%@page import="com.qx.youxia.YouXiaRecord"%>
<%@page import="com.qx.youxia.YouXiaBean"%>
<%@page import="qxmobile.protobuf.XianShi"%>
<%@page import="com.qx.activity.XianShiBean"%>
<%@page import="com.qx.vip.VipRechargeRecord"%>
<%@page import="com.qx.equip.domain.UserEquip"%>
<%@page import="com.qx.timeworker.TimeWorker"%>
<%@page import="com.qx.purchase.TiLi"%>
<%@page import="com.qx.junzhu.TalentPoint"%>
<%@page import="com.qx.junzhu.TalentAttr"%>
<%@page import="com.qx.activity.ShouchongInfo"%>
<%@page import="com.qx.account.SettingsBean"%>
<%@page import="com.qx.pve.SaoDangBean"%>
<%@page import="com.qx.activity.QiandaoInfo"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="com.qx.pve.PveRecord"%>
<%@page import="com.qx.world.PosInfo"%>
<%@page import="com.qx.vip.PlayerVipInfo"%>
<%@page import="com.qx.junzhu.PlayerTime"%>
<%@page import="com.qx.alliance.MoBaiBean"%>
<%@page import="com.qx.mibao.MibaoLevelPoint"%>
<%@page import="com.qx.mibao.MiBaoDB"%>
<%@page import="com.qx.pvp.LveDuoBean"%>
<%@page import="com.qx.util.TableIDCreator"%>
<%@page import="com.qx.huangye.HYTreasureTimes"%>
<%@page import="com.qx.huangye.HYTreasure"%>
<%@page import="com.qx.alliance.HuanWu"%>
<%@page import="com.qx.alliance.HouseBean"%>
<%@page import="com.qx.equip.domain.EquipXiLian"%>
<%@page import="com.qx.email.Email"%>
<%@page import="com.qx.award.DropRateBean"%>
<%@page import="com.qx.junzhu.ChengHaoBean"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.youxia.BuZhenYouXia"%>
<%@page import="com.qx.pve.BuZhenMibaoBean"%>
<%@page import="com.qx.huangye.BuZhenHYPvp"%>
<%@page import="com.qx.huangye.BuZhenHYPve"%>
<%@page import="com.qx.alliance.AllianceGongXianRecord"%>
<%@page import="qxmobile.protobuf.ZhangHao.CreateRoleResponse"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="qxmobile.protobuf.ZhangHao.CreateRoleRequest"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.net.HttpURLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@include file="/myFuns.jsp"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>快速练号</title>
</head>
<body>
	<hr />
	<h2>一键复制帐号</h2>
	<hr />
	<script type="text/javascript">
		var password = "1";
		function checkCreateAccount() {
			var oldAccName = document.getElementById("accName").value;
			var oldAccPwd = document.getElementById("accPwd").value;
			var oldJunName = document.getElementById("oldJunName").value;
			if (oldAccName == null || oldAccName == '') {
				alert("登录帐号不能为空");
				return false;
			}
			if (oldAccPwd == null || oldAccPwd == '') {
				alert("登录密码不能为空");
				return false;
			}
			if (oldJunName == null || oldJunName == '') {
				alert("君主名字不能为空");
				return false;
			}
			var inputText = prompt("请输入权限密码", "");
			if(inputText!=password){
				alert("权限验证错误！");
				return false;
			}
			var btn = document.getElementById("createBtn");
			btn.disabled = true;
			return true;
		}
	</script>
	<%
		String action = request.getParameter("action");
		String accName = request.getParameter("accName");
		String accPwd = request.getParameter("accPwd");
		String oldJunName = request.getParameter("oldJunName");
		String serverId = request.getParameter("serverId");

		if (action != null) {
			if (action.equals("createAccount")) {
				Account account = new Account();
				oldJunName = new String(oldJunName.getBytes("ISO-8859-1"),"UTF-8"); 
				JunZhu junzhu = HibernateUtil.find(JunZhu.class,  " where name='" + oldJunName +"'", false);
				System.out.println(junzhu);
				if(junzhu==null){
					%>君主不存在<%
					out(oldJunName);
				}else{
					/**注册帐号**/
					int code = 0;
					URL url = new URL("http://192.168.3.80:8090/qxrouter/accountReg.jsp?name="+ accName + "&pwd=" + accPwd + "");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					InputStream in = conn.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
					StringBuffer buffer = new StringBuffer();
					do {
						String line = reader.readLine();
						if (line == null) {
							break;
						}
						buffer.append(line);
					} while (true);
					reader.close();
					System.out.println("create account:"+ buffer.toString());
					in.close();
					JSONObject obj = JSONObject.fromObject(buffer.toString());
					code = obj.getInt("code");
					if(code==0){
						%>用户名已被注册<%
					} else if(code==1){
						//account = HibernateUtil.find(Account.class, "where account_name='"+accName+"'");
						account = HibernateUtil.getAccount(accName);
						/**创建角色**/
						final IoSession fs = new RobotSession(){
							public WriteFuture write(Object message){
								setAttachment(message);
								synchronized(this){
									this.notify();
								}
								return null;
							}
						};
						fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account.getAccountId()*1000+GameServer.serverId));
						CreateRoleRequest.Builder builder = CreateRoleRequest.newBuilder();
						builder.setGuoJiaId(junzhu.guoJiaId);
						builder.setRoleId(junzhu.roleId);
						int junNameIndex = 1;
						String newJunzhuName = oldJunName;
						do{
							newJunzhuName = oldJunName+junNameIndex;
							junNameIndex++;
						}while(null!=HibernateUtil.find(JunZhu.class,  " where name='" + newJunzhuName +"'", false));
						builder.setRoleName(newJunzhuName);
						/* synchronized(fs){
							BigSwitch.inst.route(PD.CREATE_ROLE_REQUEST, builder, fs);
						//	fs.wait();
						} */
					//	CreateRoleResponse resp = (CreateRoleResponse)fs.getAttachment();
					//	resp.i
						if(true){
							JunZhu newJunZhu = new JunZhu();
									//HibernateUtil.find(JunZhu.class,  " where name='" + newJunzhuName +"'", false);
							try{
							/**复制角色信息**/
							long newId = account.getAccountId() * 1000 + GameServer.serverId;
							newJunZhu = junzhu.clone();
							newJunZhu.id = newId;
							newJunZhu.name = newJunzhuName;
							String newName = newJunZhu.name;
							newJunZhu.level = junzhu.level;
							out(newJunZhu.level);
							// 这里是save不是insert
							MC.add(newJunZhu, newId);
							HibernateUtil.insert(newJunZhu);
							out("00000000000000000-------------"+newJunZhu.level);
							out("元宝是：" + newJunZhu.yuanBao);
							out("经验是：" + newJunZhu.exp);
							// JunZhuInfoRet.Builder jzbuilder = JunZhuMgr.inst.buildMainInfo(newJunZhu,new RobotSession());
							// JunZhuMgr.jzInfoCache.put(newJunZhu.id, jzbuilder);
							
							// 主线
							List<WorkTaskBean> taskList = GameTaskMgr.inst.getTaskList(junzhu.id);
							int maxid = 0;
							for(WorkTaskBean b: taskList){
								maxid = MathUtils.getMax(b.tid, maxid);
								b.dbId = newId * GameTaskMgr.spaceFactor + b.dbId % GameTaskMgr.spaceFactor;
								HibernateUtil.insert(b);
							}
							MemcachedCRUD.getMemCachedClient().set(FunctionOpenMgr.awardRenWuOverIdKey+newId, maxid);
							MemcachedCRUD.getMemCachedClient().set(FunctionOpenMgr.REN_WU_OVER_ID+newId, maxid);
							// 每日任务
							List<DailyTaskBean> dtaskL = DailyTaskMgr.INSTANCE.getDailyTasks(junzhu.id);
                            for(DailyTaskBean b: dtaskL){
                                b.dbId = newId * DailyTaskMgr.space + (b.dbId % DailyTaskMgr.space);
                                HibernateUtil.insert(b);
                            }
							/*联盟*/
							AlliancePlayer oldA = HibernateUtil.find(AlliancePlayer.class,  junzhu.id);
							if(oldA != null){
								oldA.title = 0;
								oldA.junzhuId = newId;
								HibernateUtil.insert(oldA);
								
								AllianceBean alliance = HibernateUtil.find(AllianceBean.class, oldA.lianMengId);
								if(alliance != null) {
									alliance.members += 1;
									HibernateUtil.save(alliance);
								}
							}
							
							/**复制联盟贡献**/
							AllianceGongXianRecord allianceGongXianRecord = HibernateUtil.find(AllianceGongXianRecord.class, junzhu.id);
							if(allianceGongXianRecord!=null){
								AllianceGongXianRecord newAllianceGongXianRecord = new AllianceGongXianRecord();
								newAllianceGongXianRecord.setJunZhuId(newJunZhu.id);
								newAllianceGongXianRecord.setCurMonthFirstTime(allianceGongXianRecord.getCurMonthFirstTime());
								newAllianceGongXianRecord.setCurMonthGongXian(allianceGongXianRecord.getCurMonthGongXian());
								HibernateUtil.insert(newAllianceGongXianRecord);
							}
							/**复制荒野PVE布阵信息**/
							BuZhenHYPve buZhenHYPve = HibernateUtil.find(BuZhenHYPve.class, junzhu.id);
							if(buZhenHYPve!=null){
								buZhenHYPve.junzhuId = newJunZhu.id;
								HibernateUtil.insert(buZhenHYPve);
							}
							/**复制荒野PVP布阵信息**/
							BuZhenHYPvp buZhenHYPvp = HibernateUtil.find(BuZhenHYPvp.class, junzhu.id);
							if(buZhenHYPvp!=null){
								buZhenHYPvp.junzhuId = newJunZhu.id;
								HibernateUtil.insert(buZhenHYPvp);
							}
							/**复制密保布阵信息**/
							BuZhenMibaoBean buZhenMibao = HibernateUtil.find(BuZhenMibaoBean.class, junzhu.id);
							if(buZhenMibao!=null){
								buZhenMibao.id = newJunZhu.id;
								HibernateUtil.insert(buZhenMibao);
							}
							/**复制游侠布阵信息**/
							BuZhenYouXia buZhenYouXia = HibernateUtil.find(BuZhenYouXia.class, junzhu.id);
							if(buZhenYouXia!=null){
								buZhenYouXia.junzhuId = newJunZhu.id;
								HibernateUtil.insert(buZhenYouXia);
							}
							/**复制称号信息**/
							List<ChengHaoBean> chengHaoBeans = HibernateUtil.list(ChengHaoBean.class, "where jzId="+junzhu.id+"");
							if(chengHaoBeans!=null){
								for(ChengHaoBean bean:chengHaoBeans){
									bean.jzId = newJunZhu.id;
									HibernateUtil.insert(bean);
								}
							}
							/**复制DropRateBean信息**/
							List<DropRateBean> dropRateBeans = HibernateUtil.list(DropRateBean.class, "where jzId="+junzhu.id+"");
							if(dropRateBeans!=null){
								for(DropRateBean bean:dropRateBeans){
									bean.jzId = newJunZhu.id;
									HibernateUtil.insert(bean);
								}
							}
							/**复制email信息**/
							List<Email> emailBeans = HibernateUtil.list(Email.class, "where receiverId="+junzhu.id+"");
							if(emailBeans!=null){
								for(Email bean:emailBeans){
									bean.setId(TableIDCreator.getTableID(Email.class, 1));
									bean.setReceiverId(newJunZhu.id);
									HibernateUtil.insert(bean);
								}
							}
							/**复制EquipXiLian信息**/
							EquipXiLian equipXiLian = HibernateUtil.find(EquipXiLian.class, "where junZhuId="+junzhu.id+"");
							if(equipXiLian!=null){
								equipXiLian.setId(TableIDCreator.getTableID(EquipXiLian.class, 1));
								equipXiLian.setJunZhuId(newJunZhu.id);
								HibernateUtil.insert(equipXiLian);
							}
							/**复制HouseBean信息**/
							HouseBean houseBean = HibernateUtil.find(HouseBean.class, junzhu.id);
							if(houseBean!=null){
								houseBean.jzId = newJunZhu.id;
								HibernateUtil.insert(houseBean);
							}
							/**复制HuanWu信息**/
							HuanWu huanWu = HibernateUtil.find(HuanWu.class, junzhu.id);
							if(huanWu!=null){
								huanWu.jzId = newJunZhu.id;
								huanWu.jzName = newJunZhu.name;
								HibernateUtil.insert(huanWu);
							}
							/**hy_treasure_times**/
							HYTreasureTimes hyTreasureTimes = HibernateUtil.find(HYTreasureTimes.class, junzhu.id);
							if(hyTreasureTimes!=null){
								hyTreasureTimes.junzhuId = newJunZhu.id;
								HibernateUtil.insert(hyTreasureTimes);
							}
							/**复制lve_duo**/
							LveDuoBean lveDuoBean = HibernateUtil.find(LveDuoBean.class,junzhu.id);
							if(lveDuoBean!=null){
								lveDuoBean.junzhuId = newJunZhu.id;
								HibernateUtil.insert(lveDuoBean);
							}
							/**mibao**/
							List<MiBaoDB> mibaos = HibernateUtil.list(MiBaoDB.class,"where ownerId="+junzhu.id+"");
							for(MiBaoDB mibao:mibaos){
								mibao.setDbId(TableIDCreator.getTableID(MiBaoDB.class, 1));
								mibao.setOwnerId(newJunZhu.id);
								HibernateUtil.insert(mibao);
							}
							MiBaoSkillDB mk = HibernateUtil.find(MiBaoSkillDB.class,"where jId="+junzhu.id);
							if(mk != null){
								mk.id = TableIDCreator.getTableID(MiBaoSkillDB.class, 1);
								mk.jId = newId;
								HibernateUtil.insert(mk);
							}
							
							/**mibaolevelpoint**/
							MibaoLevelPoint mibaoLevelPoint = HibernateUtil.find(MibaoLevelPoint.class,junzhu.id);
							if(mibaoLevelPoint!=null){
								mibaoLevelPoint.junzhuId = newJunZhu.id;
								HibernateUtil.insert(mibaoLevelPoint);
							}
							/**mibaobean**/
							MoBaiBean moBaiBean = HibernateUtil.find(MoBaiBean.class,junzhu.id);
							if(moBaiBean!=null){
								moBaiBean.junZhuId = newJunZhu.id;
								HibernateUtil.insert(moBaiBean);
							}
							/**pawnshopBean**/
							/* PawnshopBean pawnshopBean = HibernateUtil.find(PawnshopBean.class,junzhu.id);
							if(pawnshopBean!=null){
								pawnshopBean.setJunzhuId(newJunZhu.id);
								HibernateUtil.insert(pawnshopBean);
							} */
							/**posinfo**/
							PosInfo posInfo = HibernateUtil.find(PosInfo.class,junzhu.id);
							if(posInfo!=null){
								posInfo.jzId = newJunZhu.id;
								HibernateUtil.insert(posInfo);
							}
							/**PveRecord**/
							List<PveRecord> pveRecords = HibernateUtil.list(PveRecord.class, "where uid="+junzhu.id+"");
							for(PveRecord record:pveRecords){
								record.dbId = TableIDCreator.getTableID(PveRecord.class, 1);
								record.uid = newJunZhu.id;
								HibernateUtil.insert(record);
							}
							/**PvpBean**/
							PvpBean pvpBean = HibernateUtil.find(PvpBean.class,junzhu.id);
							if(pvpBean!=null){
								pvpBean.junZhuId = newJunZhu.id;
								HibernateUtil.insert(pvpBean);
							}
							/**QiandaoInfo**/
							QiandaoInfo qiandaoInfo = HibernateUtil.find(QiandaoInfo.class,junzhu.id);
							if(qiandaoInfo!=null){
								qiandaoInfo.id=newJunZhu.id;
								MC.add(qiandaoInfo, newJunZhu.id);
								HibernateUtil.insert(qiandaoInfo);
							}
							/**SaoDangBean**/
							SaoDangBean saoDangBean = HibernateUtil.find(SaoDangBean.class,junzhu.id);
							if(saoDangBean!=null){
								saoDangBean.jzId = newJunZhu.id;
								HibernateUtil.insert(saoDangBean);
							}
							/**SettingsBean**/
							SettingsBean settingsBean = HibernateUtil.find(SettingsBean.class, junzhu.id);
							if(settingsBean!=null){
								settingsBean.id = newJunZhu.id;
								HibernateUtil.insert(settingsBean);
							}
							/**ShouchongInfo**/
							ShouchongInfo shouchongInfo = HibernateUtil.find(ShouchongInfo.class,"where junzhuId="+junzhu.id+"");
							if(shouchongInfo!=null){
								shouchongInfo.setJunzhuId(newJunZhu.id);
								HibernateUtil.insert(shouchongInfo);
							}
							/**TalentAttr**/
							TalentAttr talentAttr = HibernateUtil.find(TalentAttr.class, junzhu.id);
							if(talentAttr!=null){
								talentAttr.junId = newJunZhu.id;
								HibernateUtil.insert(talentAttr);
							}
							/**TalentPoint**/
							List<TalentPoint> talentPoints = HibernateUtil.list(TalentPoint.class, "where junZhuId="+junzhu.id+"");
							for(TalentPoint talentPoint:talentPoints){
								talentPoint.junZhuId = newJunZhu.id;
								HibernateUtil.insert(talentPoint);
							}
							/**TimeWorker**/
							TimeWorker timeWorker = HibernateUtil.find(TimeWorker.class, junzhu.id);
							if(timeWorker!=null){
								timeWorker.junzhuId = newJunZhu.id;
								HibernateUtil.insert(timeWorker);
							}
							/**UserEquip**/
							List<UserEquip> userEquips = HibernateUtil.list(UserEquip.class, "where userId="+junzhu.id+"");
							for(UserEquip userEquip:userEquips){
								userEquip.setUserId(newJunZhu.id);
								HibernateUtil.insert(userEquip);
							}
							/**VipRechargeRecord**/
							List<VipRechargeRecord> vipRechargeRecords = HibernateUtil.list(VipRechargeRecord.class, "where accId="+junzhu.id+"");
							for(VipRechargeRecord vipRechargeRecord:vipRechargeRecords){
								vipRechargeRecord.id = TableIDCreator.getTableID(VipRechargeRecord.class, 1);
								vipRechargeRecord.accId = newJunZhu.id;
								HibernateUtil.insert(vipRechargeRecord);
							}
							/**XianShiBean**/
							List<XianShiBean> xianShiBeans = HibernateUtil.list(XianShiBean.class, "where junZhuId="+junzhu.id+"");
							for(XianShiBean xianShiBean:xianShiBeans){
								xianShiBean.junZhuId = newJunZhu.id;
								xianShiBean.id = xianShiBean.junZhuId*100+xianShiBean.bigId;
								HibernateUtil.insert(xianShiBean);
							}
							/**YouXiaBean**/
							List<YouXiaBean> youXiaBeans = HibernateUtil.list(YouXiaBean.class, "where junzhuId="+junzhu.id+"");
							for(YouXiaBean youXiaBean:youXiaBeans){
								youXiaBean.id = TableIDCreator.getTableID(YouXiaBean.class, 1);
								youXiaBean.junzhuId = newJunZhu.id;
								HibernateUtil.insert(youXiaBean);
							}
							/**YouXiaRecord**/
							List<YouXiaRecord> youXiaRecords = HibernateUtil.list(YouXiaRecord.class, "where junZhuId="+junzhu.id+"");
							for(YouXiaRecord youXiaRecord:youXiaRecords){
								youXiaRecord.setId(TableIDCreator.getTableID(YouXiaRecord.class, 1));
								youXiaRecord.setJunzhuId(newJunZhu.id);
								HibernateUtil.insert(youXiaRecord);
							}
							/**YouXiaRecord**/
							List<YuanBaoInfo> yuanBaoInfos = HibernateUtil.list(YuanBaoInfo.class, "where ownerid="+junzhu.id+"");
							for(YuanBaoInfo yuanBaoInfo:yuanBaoInfos){
								yuanBaoInfo.setDbId(TableIDCreator.getTableID(YuanBaoInfo.class, 1));
								yuanBaoInfo.setOwnerid(newJunZhu.id);
								HibernateUtil.insert(yuanBaoInfo);
							}
							/**YouXiaRecord**/
							List<ZhanDouRecord> zhanDouRecords = HibernateUtil.list(ZhanDouRecord.class, "where junzhuId="+junzhu.id+"");
							for(ZhanDouRecord zhanDouRecord:zhanDouRecords){
								zhanDouRecord.zhandouId = (int)TableIDCreator.getTableID(ZhanDouRecord.class, 1);
								zhanDouRecord.junzhuId = newJunZhu.id;
								HibernateUtil.insert(zhanDouRecord);
							}
							/**背包**/
							Bag<BagGrid> oldBag = BagMgr.inst.loadBag(junzhu.id);
							Bag<BagGrid> newBag = BagMgr.inst.loadBag(newJunZhu.id);
							for(BagGrid grid : oldBag.grids){
								if(grid.itemId>0){
									IoSession newJunZhuSession = SessionManager.inst.getIoSession(newJunZhu.id);
									BagMgr.inst.addItem(newJunZhuSession, newBag, grid.itemId, grid.cnt, grid.instId, newJunZhu.level, "复制帐号背包信息");
								}
							}
							/*装备*/
							List<EquipGrid> listgird = EquipMgr.inst.loadEquips(junzhu.id).grids;
							Bag<EquipGrid> bag22 = EquipMgr.inst.initEquip(newJunZhu.id);
							// Bag<BagGrid> bag2 = BagMgr.inst.loadBag(newJunZhu.id);
							int i=0;
							for(EquipGrid grid : listgird){
								if(grid!=null){
									   System.out.println("1111##############################grid!=null");
									   EquipGrid eg =  bag22.grids.get(i);
		                                eg.instId = grid.instId;
		                                eg.itemId = grid.itemId;
		                                System.out.println(eg);
		                              HibernateUtil.save(eg);
								}
								i++;
                            }
							
							
							/**符文**/
							List<String> list = Redis.getInstance().lgetList(FuwenMgr.CACHE_FUWEN_LANWEI + junzhu.id);
							for(String id:list){
								Redis.getInstance().lpush_(FuwenMgr.CACHE_FUWEN_LANWEI + newJunZhu.id, id);
							}
							
							// 贡金
							RankingGongJinMgr.inst.addGongJin(newId, 1000);
							%>复制ok<%
							}catch(Exception e){
								e.printStackTrace();
								%>复制出错<%
							}
							%>
							<h3>一件复制账号成功</h3>
							<table>
								<tr>
									<th>帐号：</th><td><%=accName %></td>
								</tr>
								<tr>
									<th>密码：</th><td><%=accPwd %></td>
								</tr>
								<tr>
									<th>君主id：</th><td><%=newJunZhu.id %></td>
								</tr>
								<tr>
									<th>君主名：</th><td><%=newJunZhu.name %></td>
								</tr>
								<tr>
                                    <th>君主level：</th><td><%=newJunZhu.level %></td>
                                </tr>
							</table>
							<%
						} else{
							// 不能创建多个角色
							%>君主创建失败<%
						}
					}
				}
			}
		}
	%>
	<hr />
	<p><b><font color="red">注意：不要进行跨服复制帐号！不要进行跨服复制帐号！不要进行跨服复制帐号！</font></b></p>
	<form action="" id="createForm" method="get" onsubmit="return checkCreateAccount()">
		<table>
			<tr>
				<th>新登录名：</th>
				<td><input type="text" id="accName" name="accName" value="" placeHolder="请输入登录名" /></td>
			</tr>
			<tr>
				<th>新登录密码：</th>
				<td><input type="text" id="accPwd" name="accPwd" value="" placeHolder="请输入登录密码" /></td>
			</tr>
			<tr>
				<th>目标君主名：</th>
				<td><input type="text" id="oldJunName" name="oldJunName"
					value="" placeHolder="请输入君主名" /> <input type="hidden"
					name="serverId" value="<%=GameServer.serverId%>" /> <input
					type="hidden" name="action" value="createAccount" /></td>
			</tr>
			<tr>
				<td colspan="2"><button id="createBtn" type="submit">创建帐号</button></td>
			</tr>
		</table>
	</form>
	<hr />
</body>
</html>
