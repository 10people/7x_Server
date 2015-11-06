package com.youxigu.route.app;

public class _package_ {
	/**
	 * AppEngine AppService Message WolfHandler 结合起来解决了一个AppService之间信息传递的问题
	 * 
	 * 这个结合可以让使用者不用关心传输协议问题，之用关心Service级别的协议。
	 * 
	 * AppService需要在ServiceLocator中进行注册，注册完的Service，只要调用NodeUtil.sendMessage2Node
	 * 就可以进行Service的通信了
	 * 
	 * 这是给WolfService机制的一个有效补充
	 * 
	 * Node和Router之间的通信是组件之间的通信，发出去的消息，并不知道是谁处理，AppService把这个通信细化到了Service级别了。
	 */
}
