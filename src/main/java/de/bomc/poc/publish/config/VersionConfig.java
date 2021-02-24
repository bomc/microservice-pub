package de.bomc.poc.publish.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
public class VersionConfig {
	
	@NotNull
	private String version;
	
	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}
	
}
