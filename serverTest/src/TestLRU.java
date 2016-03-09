import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.OrderedMapIterator;
import org.apache.commons.collections.map.LRUMap;


public class TestLRU {
	public static LRUMap realMap = new LRUMap(10);
	public static Map cacheMap = Collections.synchronizedMap(realMap);
	public static void main(String[] args) throws Exception{
		cacheMap.put(1, "1");
		cacheMap.put(2, "2");
		LRUMap map = realMap;
		System.out.println("map.size():" + map.size());
		OrderedMapIterator it = map.orderedMapIterator();
		while(it.hasNext()){
			 Object n = it.next();
			 Object key = it.getKey();
			System.out.println("next:" + key+":" + it.getValue()+"--"+(n==key));
			//eventlist.add((HotEvent)map.get(key));
		}
	}
}
