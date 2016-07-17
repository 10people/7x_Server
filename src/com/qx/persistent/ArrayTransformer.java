package com.qx.persistent;

import org.hibernate.transform.BasicTransformerAdapter;

public class ArrayTransformer extends BasicTransformerAdapter{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7535166853254177862L;
	public static ArrayTransformer inst = new ArrayTransformer();
	@Override
	public Object transformTuple(Object[] tuple, String[] aliases) {
		return tuple;
	}
}
