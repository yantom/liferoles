package com.liferoles.rest.JSON;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.liferoles.model.Role;

public class RolePartialSerializer extends JsonSerializer<Role> {
	@Override
	public void serialize(Role value, JsonGenerator jgen, SerializerProvider provider) throws IOException{
		jgen.writeStartObject();
		jgen.writeNumberField("id", value.getId());
		jgen.writeStringField("name", value.getName());
        jgen.writeEndObject();
    }
}
