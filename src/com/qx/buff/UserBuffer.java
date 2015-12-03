package com.qx.buff;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UserBuffer {
	
	/** buff作用的君主Id */
	private long junzhuId;
	
	/** 君主身上的buff列表 */
	private List<Buffer> bufferList = Collections.synchronizedList(new LinkedList<Buffer>());
	
	public UserBuffer(long junzhuId) {
		super();
		this.junzhuId = junzhuId;
	}

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
		Buffer existBuff = null;
		for(Buffer buff : bufferList) {
			if(buff.getId() == buffer.getId()) {
				existBuff = buff;
			}
		}
		if(existBuff != null) {
			bufferList.remove(existBuff);
		}
		bufferList.add(buffer);
	}
	
}
