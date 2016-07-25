package log.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.bag.HashBag;
import org.json.JSONObject;

/**
 * 分析登录、创建角色、完成第一关的时间间隔。
 * @author 康建虎
 *
 */
public class LiuShi {
	static class P{
		Date login;//首次登录时间
		Date createRole;
		Date finish1battle;
		long jzId;
	}
	public static Map<Long, P> pMap = new HashMap<Long, LiuShi.P>();
	public static void main(String[] args) throws Exception{
		String file = "C:/Users/kjh/Downloads/0825/gameServer.2015-08-25.log";
		BufferedReader br = new BufferedReader(new FileReader(file));
		int lineNo = 0;
		do{
			String line = br.readLine();
			if(line == null)break;
			lineNo ++;
			if(lineNo == 60103){
				lineNo += 0;
			}
			procLine(line);
		}while(true);
		br.close();
		/////////
		parseTimeGap(pMap);
	}
	public static void parseTimeGap(Map<Long, P> pMap2) {
		Iterator<P> it = pMap2.values().iterator();
		HashBag bag = new HashBag();
		while(it.hasNext()){
			P p = it.next();
			long createRoleGap;
			if(p.createRole == null){
				createRoleGap = 0;
			}else{
				createRoleGap = p.createRole.getTime() - p.login.getTime();
			}
			long firstBattleGap;
			if(p.finish1battle == null){
				firstBattleGap = 0;
			}else{
				firstBattleGap = p.finish1battle.getTime() - p.createRole.getTime();
			}
			/*
			System.out.println(p.jzId
					+","+fmt.format(p.login)
					+","+(p.createRole == null ? "" : fmt.format(p.createRole))
					+","+(p.finish1battle == null ? "" : fmt.format(p.finish1battle))
					+","+createRoleGap
					+","+firstBattleGap);
					*/
			bag.add(Math.round(firstBattleGap*1.0f/1000/60));
		}
		Set<Integer> set = bag.uniqueSet();
		for(Integer minute : set){
			System.out.println(minute+","+bag.getCount(minute));
		}
	}
	public static void procLine(String line)throws Exception {
		if(line.contains("\"accId\":")){//登录成功
			JSONObject o = new JSONObject(line.substring(line.indexOf("{")));
			Long accId = o.getLong("accId");
			Long jzId = accId * 1000 + 1;
			P p = pMap.get(jzId);
			if(p == null){
				p = new P();
				p.jzId = jzId;
				p.login = parseTime(line);
				pMap.put(p.jzId, p);
			}
//			System.out.println(o);
			breakPoint(p.jzId);
		}else if(line.contains(" 创建君主成功")){
			String arr[] = line.split(" ");
			Long jzId = Long.valueOf(arr[arr.length - 3]);
			breakPoint(jzId);
//			System.out.println(line);
//			System.out.println(jzId);
//			System.exit(0);
			P pre = pMap.get(jzId);
			if(pre == null){
				System.out.println("pre is null !!!:"+line);
				System.exit(0);
			}else{
				pre.createRole = parseTime(line);
			}
		}else if(line.contains("请求领取奖励100101")){
			String arr[] = line.split("- ");
			String jzIdStr = arr[1].split("请求")[0];
//			System.out.println(line);
//			System.out.println(jzIdStr);
			Long jzId = Long.valueOf(jzIdStr);
			breakPoint(jzId);
			P pre = pMap.get(jzId);
			if(pre == null){
				System.out.println("2222 pre is null !!!:"+line);
				//System.exit(0);
			}else if(pre.createRole == null){
				System.out.println("3333 pre is null !!!:"+line);
			}else{
				pre.finish1battle = parseTime(line);
			}
		}
	}
	public static void breakPoint(Long accId) {
		if(accId.longValue() == 947001){
			System.out.println();
		}
	}
	public static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Date parseTime(String line) throws Exception {
		String sub = line.substring(0,14);
		String sub2 = "2015-"+sub;
		Date t = fmt.parse(sub2);
//		System.out.println(fmt.format(t));
		return t;
	}
}
