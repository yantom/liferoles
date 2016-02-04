package com.liferoles.rest.JSON;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

	@Override
	public LocalDate deserialize(JsonParser jp, DeserializationContext arg1)throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		return LocalDate.of(node.get("year").asInt(), node.get("month").asInt(), node.get("day").asInt());
	}

}
