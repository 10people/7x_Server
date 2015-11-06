package com.qx.bag;

import java.util.Collections;
import java.util.List;

/**
 * 玩家背包。
 * @author 康建虎
 *
 */
public class Bag<T> {
	public long ownerId;
	public List<T> grids;
	public Bag(){
		grids = Collections.EMPTY_LIST;
	}
}
