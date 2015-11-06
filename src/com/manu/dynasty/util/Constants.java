package com.manu.dynasty.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Constants {

	public static byte[] fixObject(Object obj) throws IOException {
	    if(obj == null) {
		return null;
	    }
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
	    objectOutputStream.writeObject(obj);
	    return byteArrayOutputStream.toByteArray();
	}
	
	public static Object fixByteStream(byte[] b) throws IOException, ClassNotFoundException {
	    	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b);
		ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
		return inputStream.readObject();
	}
	
	public static Object copyObject(Object obj) throws IOException, ClassNotFoundException {
	    byte[] b = Constants.fixObject(obj);
	    Object ro = Constants.fixByteStream(b);
	    return ro;
	}
}
