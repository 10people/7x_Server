package com.qx.buff;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UserBuffer {
	
	/** buff作用的君主Id */
	private long junzhuId;
	
	/** 君主身上的buff列表 */
	private List<Buffer> bufferList = Collections.synchronizedList(new LinkedList<Buffer>());

	public long getJunzhuId() {
		return junzhuId;
	}

	public void setJunzhuId(long junzhuId) {
		this.junzhuId = junzhuId;
	}

	public List<Buffer> getBufferList() {
		return bufferList;
	}

	public void setBufferList(List<Buffer> bufferList) {
		this.bufferList = bufferList;
	}

	public void addBuffer(Buffer buffer) {
		bufferList.add(buffer);
	}
	
}
