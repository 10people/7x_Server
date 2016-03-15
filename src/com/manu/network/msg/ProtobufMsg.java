package com.manu.network.msg;
import com.google.protobuf.MessageLite.Builder;
/**
 * @author 康建虎
 *
 */
public class ProtobufMsg extends AbstractMessage{
	public Builder builder;
	@Override
	public String toString() {
		return "id"+id+" msg : "+(builder == null ? "null" : builder.getClass().getSimpleName());
	}
	public ProtobufMsg(){}
	public ProtobufMsg(short id){this.id = id;}
	public ProtobufMsg(short id,Builder b){
		this.id = id;
		builder = b;
		}
}
