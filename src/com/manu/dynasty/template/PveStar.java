package com.manu.dynasty.template;

import java.util.List;

/*
 *   <PveStar starId="2011" desc="500001" condition="1" award="0:900002:10" /> 
 */
public class PveStar {
	public List<AwardTemp> parsedArr;
	public int starId;
	public String desc;
	public String condition;
	public String award;
}
