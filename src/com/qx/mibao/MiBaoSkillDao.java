package com.qx.mibao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.qx.persistent.HibernateUtil;
import com.sun.faces.util.LRUMap;

public class MiBaoSkillDao {
	public static MiBaoSkillDao inst = new MiBaoSkillDao();
	public static Map<Long, Map<Integer, MiBaoSkillDB>> cache = Collections.synchronizedMap(new LRUMap(5000));

	public Map<Integer, MiBaoSkillDB> getMap(long pid) {
		Map<Integer, MiBaoSkillDB> ret = cache.get(pid);
		if (ret != null) {
			return ret;
		}
		List<MiBaoSkillDB> list = HibernateUtil.list(MiBaoSkillDB.class, " where jId=" + pid);
		ret = Collections.synchronizedMap(new LinkedHashMap<Integer, MiBaoSkillDB>(list.size()));
		for (MiBaoSkillDB miBaoSkillDB : list) {
			ret.put(miBaoSkillDB.zuHeId, miBaoSkillDB);
		}
		cache.put(pid, ret);
		return ret;
	}

	public MiBaoSkillDB getMibaoSkillDBySkillId(Long jzId, int zuheId) {
		Optional<MiBaoSkillDB> op = getMap(jzId).values().stream().filter(t -> t.zuHeId == zuheId).findAny();
		if (op.isPresent()) {
			return op.get();
		}
		return null;
	}
	
	public void save(MiBaoSkillDB miBaoSkillDB) {
		Map<Integer, MiBaoSkillDB> ret = cache.get(miBaoSkillDB.jId);
		if(ret == null){
			ret = Collections.synchronizedMap(new LinkedHashMap<Integer, MiBaoSkillDB>());
		}
		ret.put(miBaoSkillDB.zuHeId, miBaoSkillDB);
	}

}
