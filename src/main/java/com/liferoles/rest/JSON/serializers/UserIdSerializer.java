package com.liferoles.rest.JSON.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.liferoles.model.User;

public class UserIdSerializer extends JsonSerializer<User> {
	@Override
	public void serialize(User value, JsonGenerator jgen, SerializerProvider provider) throws IOException{
		jgen.writeStartObject();
		jgen.writeNumberField("id", value.getId());
        jgen.writeEndObject();
    }
}
