package com.liferoles.rest.JSON.serializers;

import java.io.IOException;
import java.time.LocalTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalTimeSerializer extends JsonSerializer<LocalTime> {

	@Override
	public void serialize(LocalTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException{
		jgen.writeStartObject();
        jgen.writeStringField("hours", String.format("%02d", value.getHour()));
        jgen.writeStringField("minutes", String.format("%02d", value.getMinute()));
        jgen.writeEndObject();
    }
}
