package pct;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.JiNengPeiYang.GetJiNengPeiYangQuality;
import qxmobile.protobuf.JiNengPeiYang.HeroData;

import com.google.protobuf.MessageLite.Builder;

public class TestJiNengPeiYang {

	public static void readList(IoSession session, Builder builder) {
		GetJiNengPeiYangQuality.Builder res = (GetJiNengPeiYangQuality.Builder)builder;		
		int cnt = res.getListHeroDataCount();
		for(int i=0;i<cnt; i++){
			if(i%3==0){
				System.out.println();
			}
			HeroData data = res.getListHeroData(i);
			System.out.print(String.format("%8d , %d",data.getSkillId(), data.getIsUp()?1:0));
		}
	}

}
