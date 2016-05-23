package com.google.protobuf;

public class DirectBytes extends LiteralByteString{

	public DirectBytes(byte[] bytes) {
		super(bytes);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public byte[] toByteArray() {
		return bytes;
	}

}
