package com.liferoles.rest.JSON.serializers;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateSerializer extends JsonSerializer<LocalDate> {

	@Override
	public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeStartObject();
		jgen.writeStringField("year", String.valueOf(value.getYear()));
		jgen.writeStringField("month", String.format("%02d", value.getMonthValue()));
		jgen.writeStringField("day", String.format("%02d", value.getDayOfMonth()));
		jgen.writeEndObject();
	}
}
