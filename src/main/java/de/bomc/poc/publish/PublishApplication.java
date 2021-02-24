package de.bomc.poc.publish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Hooks;

@SpringBootApplication
public class PublishApplication {

	/**
	 * <pre>
	 * - To compile the code: ./gradlew clean build
	 * - To run this app: ./gradlew bootRun
	 * 
	 * - OpenAPI: http://localhost:8080/webjars/swagger-ui/index.html?configUrl=/api-docs/swagger-config#/
	 *            http://localhost:8080/webjars/swagger-ui/index.html?configUrl=/api-docs/swagger-config#/Publish/api
	 * </pre>
	 * @param args
	 */
	public static void main(final String[] args) {
		Hooks.onOperatorDebug();
		
		SpringApplication.run(PublishApplication.class, args);
	}

}
