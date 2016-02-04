package com.liferoles.rest.JSON;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.liferoles.model.Language;
import com.liferoles.model.User;

public class UserPartialDeserializer extends JsonDeserializer<User> {
	@Override
	public User deserialize(JsonParser jp, DeserializationContext arg1)throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		User u = new User();
		JsonNode userId = node.get("id");
		if(userId == null)
			u.setId(null);
		else
			u.setId(userId.asLong());
		JsonNode userPersonalMission = node.get("personalMission");
		if(userPersonalMission == null)
			u.setPersonalMission(null);
		else
			u.setPersonalMission(userPersonalMission.asText());
		u.setEmail(node.get("email").asText());
		
		u.setLanguage(Language.valueOf(node.get("language").asText()));
		
		JsonNode userPassword = node.get("password");
		if(userPassword == null)
			u.setPassword(null);
		else
			u.setPassword(userPassword.asText());
		return u;
	}
}