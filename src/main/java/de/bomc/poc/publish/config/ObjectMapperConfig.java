package de.bomc.poc.publish.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class ObjectMapperConfig {

	@Bean
	@Primary
	public ObjectMapper objectMapper(final Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {

		final ObjectMapper objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
		objectMapper
			.registerModule(new ParameterNamesModule())
			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule())
			// StdDateFormat is ISO8601
			.setDateFormat(new StdDateFormat().withColonInTimeZone(true))
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		return objectMapper;
	}
}
