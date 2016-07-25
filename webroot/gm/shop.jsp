<%@page import="com.qx.huangye.shop.PublicShopDao"%>
<%@page import="com.qx.huangye.shop.WuBeiFangBean"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.qx.pvp.PvpMgr"%>
<%@page import="com.qx.huangye.shop.ShopMgr"%>
<%@page import="com.qx.huangye.shop.PublicShop"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="org.hibernate.Hibernate"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="org.apache.commons.lang.time.DateUtils"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
function go(act,myid){
	var node = document.getElementById(act);
	if(node != null) {
		var v = node.value;
		location.href = '?action='+act+"&v="+v+"&myid="+myid;
	}else {
		location.href = '?action='+act;
	}
}

</script>
<title>各大商店管理</title>
</head>
  <body>
  <%
  String action = request.getParameter("action");
   String name = request.getParameter("account");
   name = name == null ? "": name.trim();

   if(session.getAttribute("name") != null && name.length()==0){
        name = (String)session.getAttribute("name");
   }
   %>
    <form action="">
        账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
    <button type="submit">查询</button>
    </form>
<%

    Account account = null;
    if(name != null && name.length()>0){
        account = HibernateUtil.getAccount(name);
    }
    if(account == null){
%>没有找到
<%
    }else{
        session.setAttribute("name", name);
        %><br>注册账号：<%=account.accountName%><br> 账号id：<%=account.accountId%><%
         long junZhuId = account.accountId * 1000 + GameServer.serverId;
         JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
        
         
         if(junzhu == null){
            out.println("没有君主");
         }else{
            %><br> 君主id是：<%=junzhu.id%> <br>君主姓名是：<%=junzhu.name%><br/><br/>
          <%
           if(action != null && action.equals("updateMoneyrrr")){
        	   int v = Integer.parseInt(request.getParameter("v"));
               PublicShop shop = PublicShopDao.inst.getShopByType(junzhu.id, 1);
               if(shop == null){
                   shop = ShopMgr.inst.initShopInfo(junzhu.id, 1);
              }
              ShopMgr.inst.setMoney(1, junzhu.id, shop, v);
             // out("id is "+ 1 + "save1  v is " + v );
           }
           else if(action != null && action.equals("updateMoneyxxx")){
        	  int v = Integer.parseInt(request.getParameter("v"));
              ShopMgr.inst.setMoney(2, junzhu.id, null, v);
            //  out("save2  v is " +v );
             // out("id is "+ 2 + "save1  v is " + v );
          }
           else if(action != null && action.equals("updateMoneyccc")){
              int v = Integer.parseInt(request.getParameter("v"));
              PublicShop shop = PublicShopDao.inst.getShopByType(junzhu.id, 3);
              if(shop == null){
                  shop = ShopMgr.inst.initShopInfo(junzhu.id, 3);
             }
            ShopMgr.inst.setMoney(3, junzhu.id, shop, v);
        //  out("id is "+ 3 + "save1  v is " + v );
          }
           else if(action != null && action.equals("updateMoneyddd")){
        	   int v = Integer.parseInt(request.getParameter("v"));
               PublicShop shop = PublicShopDao.inst.getShopByType(junzhu.id, 4);
               if(shop == null){
                   shop = ShopMgr.inst.initShopInfo(junzhu.id, 4);
              }
              ShopMgr.inst.setMoney(4, junzhu.id, shop, v);
          } else if("resetWubeifang".equals(action)) {
        	  WuBeiFangBean wuBeiFang = ShopMgr.inst.getWuBeiFangBean(junzhu.id);
        	  wuBeiFang.type1UseTimes = 0;
  			wuBeiFang.type2UseTimes = 0;
  			wuBeiFang.type3UseTimes = 0;
  			wuBeiFang.type4UseTimes = 0;
  			HibernateUtil.save(wuBeiFang);
          }
          
          tableStart();
          trS();
          	td("");td("今日免费次数");td("今日花费元宝可购买次数");
          trE();
          WuBeiFangBean wuBeiFang = ShopMgr.inst.getWuBeiFangBean(junzhu.id);
          //type类型(与配置文件必须一样):1：装备铺,2：珍宝行,3：石料店,4：益精堂
          for(int i = 1; i <= 4; i++) {
        	  trS();
        	  switch(i) {
        	  case 1:
        		  td("装备铺");
        		  break;
        	  case 2:
        		  td("珍宝行");
        		  break;
        	  case 3:
        		  td("石料店");
        		  break;
        	  case 4:
        		  td("益精堂(精气)");
        		  break;
        	  }
        	  int freeTimes = ShopMgr.inst.getRemainFreeTimes(i, wuBeiFang);
    		  td(freeTimes);
    		  td(ShopMgr.inst.getRamainYuanBaoTimes(i, wuBeiFang, junzhu, freeTimes));
    		  trE();
          }
          trE();
          tableEnd();
          out.println("<input type='button' value='重置祭祀所有次数' onclick='go(\"resetWubeifang\")'/><br/>");
          
          out.println("<br/><br/>");
          
          String mo1 =  "0";
          String mo2 = "0";
          String mo3 = "0";
          String mo4 = "0";
          String mo5 = "0";
          String mo6 = "0";
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for(int index =1; index<=6; index++){
				//
				   // 荒野商店
                       // public static final int huangYe_shop_type = 1;
                       // 联盟商店
                       //  public static final int lianMeng_shop_type = 2;
                       // 联盟战商店
                        //   public static final int lianmeng_battle_shop_type = 3;
                        // 百战商店
				   // public static final int baizhan_shop_type=  4;
				//  // 普通商店
				 //   public static final int common_shop_type = 5;
				    // 神秘商店
				 //   public static final int mysterious_shop_type = 6;
				 PublicShop shop = PublicShopDao.inst.getShopByType(junzhu.id, index);
				 String s ="";
				 String sM = "";
				  if(shop == null){
	                    shop = ShopMgr.inst.initShopInfo(junzhu.id, index);
	               }else {
	            	   ShopMgr.inst.resetHYShopBean(shop);
	               }
				 switch(index){
					 case 1:s = "荒野商店";
					 sM = "荒野币"; 
					 mo1 = ShopMgr.inst.getMoney(index, junzhu, shop) + "";
					 break;
					 case 2:s = "联盟商店";sM = "联盟贡献";
					 mo2 = ShopMgr.inst.getMoney(index, junzhu, shop) + "";
					 break;
					 case 3:s ="联盟战商店";sM = "功勋";
					 mo3 = ShopMgr.inst.getMoney(index, junzhu, shop) + "";
					 break;
					 case 4:s ="百战威望商店";sM = "威望";
                     mo4 = ShopMgr.inst.getMoney(index, junzhu, shop) + "";
                     break;
					 case 5: s="普通商店";sM="元宝";
					  mo5 = junzhu.yuanBao+"";
					 break;
					 case 6: s="神秘商店";sM="铜币";
                     mo6 = junzhu.tongBi+"";
					 break;
				}
				 out(s + "数据:");
                tableStart();
                  trS();
                  td(sM);td("下次自动刷新商品列表时间");td("今日够买刷新次数");
                  td("上次购买刷新时间 "); td("商品信息");
                  trE();
                  trS();
                   switch(index){
	                case 1:
	                	td("<input type='text' id='updateMoneyrrr' value='"+ 
	                mo1+ "'/><input type='button' value='修改' onclick='go(\"updateMoneyrrr\", "+ index +")'/>");
	                     break;
	                case 2:
	                	   	td("<input type='text' id='updateMoneyxxx' value='"+
	                mo2+ "'/><input type='button' value='修改' onclick='go(\"updateMoneyxxx\", "+ index +")'/>");
	                	
	                break;
	                case 3:
	                	    td("<input type='text' id='updateMoneyccc' value='"
	                +mo3+ "'/><input type='button' value='修改' onclick='go(\"updateMoneyccc\", "+ index +")'/><br/>");
	                break;
	                case 4:
                        td("<input type='text' id='updateMoneyddd' value='"
                +mo4+ "'/><input type='button' value='修改' onclick='go(\"updateMoneyddd\", "+ index +")'/><br/>");
                break;
	                case 5:
                        td(mo5);
                break;
	                case 6:
	                	 td(mo6+"<br/>"+"(铜币数)(但是神秘商店的物品有的是花元宝买)");
                break;
	                }
                   if(index == 5){td("凌晨4点恢复每种物品购买次数");}else{
                td(sdf.format(shop.nextAutoRefreshTime));
                   }
                if(index == 5){td("不刷新");}else{
                td(shop.buyNumber);}
                if(index == 5){td("不提供购买刷新功能");}else{
                	td(sdf.format(shop.lastResetShopTime));}td(shop.goodsInfo);
                trE();
                tableEnd();
                br();
                br();
			}
         }
    }
        %>
  </body>
</html>