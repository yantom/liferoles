package com.liferoles.rest.JSON;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.liferoles.model.User;

public class UserIdDeserializer extends JsonDeserializer<User> {
	@Override
	public User deserialize(JsonParser jp, DeserializationContext arg1)throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		User u = new User();
		u.setId(node.get("id").asLong());
		return u;
	}
}
