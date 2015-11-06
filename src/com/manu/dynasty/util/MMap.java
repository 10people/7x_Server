package com.manu.dynasty.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import com.manu.dynasty.boot.GameServer;

/**
 * 使用文件来保存玩家ID序号和联盟ID序号。
 *
 */
public class MMap {
	private static MappedByteBuffer memory;
	public static final int size = 4 * 1024;
	
	public static void create() throws IOException{
		// 获得一个只读的随机存取文件对象 		
		RandomAccessFile RAFile = new RandomAccessFile(GameServer.mmapFile,"rw");
		// 获得相应的文件通道 
		FileChannel fc = RAFile.getChannel();
		// 获得共享内存缓冲区，该共享内存读写
		memory = fc.map(FileChannel.MapMode.READ_WRITE,0,size);
		
		load();
	}
	
	public synchronized static void saveUserId(int userId){
		memory.position(0);
		memory.putInt(userId);
	}
	
	public synchronized static void saveGuildId(int guildId){
		memory.position(4);
		memory.putInt(guildId);
	}

	public static void save(){
		memory.position(0);
		
		memory.putInt(IDTools.userIdG.get());
		memory.putInt(IDTools.guildIdG.get());
	}
	
	public static void load(){
		memory.position(0);
		IDTools.userIdG.set(memory.getInt());
		IDTools.guildIdG.set(memory.getInt());
	}
}
