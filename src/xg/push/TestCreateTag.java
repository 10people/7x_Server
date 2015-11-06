package xg.push;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tencent.xinge.TagTokenPair;
import com.tencent.xinge.XingeApp;

public class TestCreateTag extends XG{
	Logger log = LoggerFactory.getLogger(XG.class);
	public static void main(String[] args) {
		XGParam xgParam = XGParam.channels.get("XY");
//		XGParam xgParam = XGParam.channels.get("PP");
		String token = "dcf1b1b00f3d28aa7659ca4c21476603c42fe612ab0c7aac3b935588c5745491";
		new TestCreateTag().test(xgParam, token);
	}
	void test(XGParam xgParam, String token){
		XingeApp push = new XingeApp(xgParam.accessId, xgParam.secretKey);
		//客户端没有保存过设置，全部开启.
		List<TagTokenPair> pairs = new ArrayList<TagTokenPair>();
		pairs.add(new TagTokenPair(tag1TiLiGive,token));
		pairs.add(new TagTokenPair(tag2TiLiFull,token));
		pairs.add(new TagTokenPair(tag3BiaoJuYunSong,token));
		pairs.add(new TagTokenPair(tag4DangPuShuaXin,token));
		JSONObject ret = push.BatchSetTag(pairs);
		log.info(" 添加4个tag结果{}",   ret);
	}
}
