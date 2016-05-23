package com.liferoles.rest.JSON.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.liferoles.model.Day;
import com.liferoles.model.User;

public class UserPartialDeserializer extends JsonDeserializer<User> {
	@Override
	public User deserialize(JsonParser jp, DeserializationContext arg1) throws IOException, JsonProcessingException {
		JsonNode mainNode = jp.getCodec().readTree(jp);
		JsonNode node;
		User u = new User();

		node = mainNode.get("id");
		if (node == null)
			u.setId(null);
		else
			u.setId(node.asLong());

		node = mainNode.get("email");
		if (node == null)
			u.setEmail(null);
		else
			u.setEmail(node.asText());

		node = mainNode.get("password");
		if (node == null)
			u.setPassword(null);
		else
			u.setPassword(node.asText());

		node = mainNode.get("personalMission");
		if (node == null)
			u.setPersonalMission("");
		else
			u.setPersonalMission(node.asText());

		node = mainNode.get("firstDayOfWeek");
		if (node == null)
			u.setFirstDayOfWeek(Day.MON);
		else
			u.setFirstDayOfWeek(Day.values()[node.asInt()]);
		return u;
	}
}