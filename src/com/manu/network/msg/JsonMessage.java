package com.manu.network.msg;

import net.sf.json.JSONObject;


/**
 * @author 康建虎
 *
 */
public class JsonMessage extends AbstractMessage{
	public String rawMsg;
	public JSONObject json;
	
	public JsonMessage(String raw){
		rawMsg = raw;
		json = JSONObject.fromObject(raw);
	}
}
