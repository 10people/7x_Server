package com.qx.event;

/**
 * 事件处理器。
 * @author 康建虎
 *
 */
public abstract class EventProc {
	public boolean disable;
	public EventProc(){
		doReg();
	}
	public abstract void proc(Event param);
	protected abstract void doReg();
}
