package com.manu.network.msg;

import com.manu.dynasty.chat.ChatChannel;

/**
 * 聊天消息类
 * @author 康建虎
 *
 */
public class ChatMsg extends AbstractMessage {
	public int senderId;
	public String senderName;
	//
	public int receiverId;
	public String receiverName;
	//
	public ChatChannel channel;
	//
	public String content;
}
