package com.manu.dynasty.util;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.deser.StdScalarDeserializer;

public class JacksonIntegerDeserializer extends StdScalarDeserializer<Integer> {

	public JacksonIntegerDeserializer(){
		super(Integer.class);
	}
	public JacksonIntegerDeserializer(Class<?> vc) {
		super(vc);

	}

	@Override
	public Integer deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken t = jp.getCurrentToken();
		if (t == JsonToken.VALUE_TRUE) {
			return 1;
		}
		if (t == JsonToken.VALUE_FALSE) {
			return 0;
		}
		return _parseInteger(jp, ctxt);
	}

	@Override
	public Integer deserializeWithType(JsonParser jp,
			DeserializationContext ctxt, TypeDeserializer typeDeserializer)
			throws IOException, JsonProcessingException {
		return deserialize(jp, ctxt);
	}

}
