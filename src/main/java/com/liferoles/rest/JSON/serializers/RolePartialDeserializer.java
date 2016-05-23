package com.liferoles.rest.JSON.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.liferoles.model.Role;

public class RolePartialDeserializer extends JsonDeserializer<Role> {
	@Override
	public Role deserialize(JsonParser jp, DeserializationContext arg1) throws IOException, JsonProcessingException {
		JsonNode mainNode = jp.getCodec().readTree(jp);
		JsonNode node;
		Role r = new Role();
		r.setId(mainNode.get("id").asLong());

		node = mainNode.get("name");
		if (node == null)
			r.setName(null);
		else
			r.setName(node.asText());
		return r;
	}
}
