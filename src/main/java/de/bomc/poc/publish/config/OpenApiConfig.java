package de.bomc.poc.publish.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI consumerAPI(@Value("${bomc.application-description}") String appDescription, final VersionConfig versionConfig) {
		return new OpenAPI()
				.info(
						new Info()
						.title("Publish API")
		                .description(appDescription) // A API to demonstrate kubernetes / tekton integration
		                .version(versionConfig.getVersion())
		                .termsOfService("http://swagger.io/terms/")
		                .license(
		                		new License().name("MIT").url("https://opensource.org/licenses/MIT")
		                )
		        );
		}
}
