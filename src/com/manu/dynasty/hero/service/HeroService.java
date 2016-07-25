package com.manu.dynasty.hero.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.NameId;

public class HeroService {
	public static Logger log = LoggerFactory.getLogger(HeroService.class);
	public static Map<String, NameId> heroNameMap = new HashMap<String, NameId>();
	
	public static HeroService heroService ;
	public HeroService(){
	}

	public static void initNameMap() {
		List<NameId> list = TempletService.listAll(NameId.class.getSimpleName());
		for(NameId t : list){
			heroNameMap.put(t.nameId, t);
		}
	}
	
	public static String getNameById(String key){
		NameId nameId = heroNameMap.get(key);
		if(nameId == null){
			log.error("找不到key:{}的NameId配置", key);
			return key;
		}else if(nameId.Name == null){
			log.error("名字配置错误 {}",key);
			return key;
		}else{
			return nameId.Name;
		}
	}
	
	public static HeroService getInstance(){
		if(heroService == null){
			heroService = new HeroService();
		}
		return heroService;
	}

}
