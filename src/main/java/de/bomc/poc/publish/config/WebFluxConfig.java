package de.bomc.poc.publish.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

	@Override
	public void addFormatters(final FormatterRegistry formatterRegistry) {
		
		final DateTimeFormatterRegistrar dateTimeFormatterRegistrar = new DateTimeFormatterRegistrar();
		dateTimeFormatterRegistrar.setUseIsoFormat(true);
		dateTimeFormatterRegistrar.registerFormatters(formatterRegistry);
	}

//    @Override
//    public void addCorsMappings(final CorsRegistry corsRegistry) {
//
//    	corsRegistry
//    		.addMapping("/**")
//    		.allowedOrigins("*")
//    		.allowedMethods("PUT", "DELETE", "POST", "GET", "DELETE", "OPTIONS", "PATCH", "HEAD")
//    		.allowedHeaders("*")
//    		.allowCredentials(true).maxAge(3600 * 24);
//    }
}
