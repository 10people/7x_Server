package com.qx.alliancefight;

public enum FightProgress {
	 //联盟战状态：赛程，0-无，1-32强，2-16强，3-8强，4-4强，5-半决赛，6-三四名比赛，7-决赛，8-报名
	 NONE(0),
	 RANK_IN32(1),
	 RANK_IN16(2),
	 RANK_IN8(3),
	 RANK_IN4(4),
	 RANK_IN3_4(5),
	 RANK_INSFL(6),
	 RANK_INFNL(7),
	 APPLY(8);
	 
	 private int code;
	 
	 private FightProgress(int code) {
		 this.code = code;
	 }

	public int getCode() {
		return code;
	}

}
