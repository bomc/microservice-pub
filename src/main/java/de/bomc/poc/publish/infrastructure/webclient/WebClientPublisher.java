package de.bomc.poc.publish.infrastructure.webclient;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import de.bomc.poc.publish.application.exception.common.ApplicationException;
import de.bomc.poc.publish.domain.model.GithubRepo;
import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Restendpoint for webClient -> http://localhost:8080/monitor/metrics/http.client.requests
 * 
 * To show values the webClient has to be running a request.
 *   
 * NOTE: The base path /monitor is set in application.properties.
 * 
 */
@Slf4j
@Component
public class WebClientPublisher {

	private static final String LOG_PREFIX = WebClientPublisher.class.getName() + "#";
    
	@Value("${server.servlet.context-path}")
	private String contextPath;
	
	private final WebClient webClient;
	private final WebClient githubWebClient;
	
	public WebClientPublisher(@Qualifier("webClient") final WebClient webClient, @Qualifier("githubWebClient") final WebClient githubWebClient) {
		this.webClient = webClient;
		this.githubWebClient = githubWebClient;
	}
	
	public Mono<PublishMetaData> getPublishMetaDataById(final String id) {
		log.debug(LOG_PREFIX + "getPublishMetaDataById [id=" + id + ", contextPath=" + this.contextPath + "]");

		return this.webClient
				.get()
				.uri(this.contextPath + "/metadata/{id}", id)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
				.retrieve()                        // Provides access to the response status and headers via {@link ResponseEntity} along with error status handling.
				                                   // The retrieve method throws an WebClientResponseException when there will be a 4xx and 5xx series exception received.
				.onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new ApplicationException("webClient.getPublishMetaDataById failed with 4xx client error.")))
				.onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ApplicationException("webClient.getPublishMetaDataById failed with 5xx server error.")))
				.bodyToMono(PublishMetaData.class) // Extracts the response body to a Mono.
				.switchIfEmpty(Mono.empty())
				.log();
	}

	public Mono<PublishMetaData> createPublishMetaData(final PublishMetaData publishMetaData) {
		log.debug(LOG_PREFIX + "createPublishMetaData [publishMetaData=" + publishMetaData + ", contextPath=" + this.contextPath + "]");
	
		return this.webClient
				.post()
				.uri(this.contextPath + "/metadata/annotation-validation/")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
				.bodyValue(publishMetaData)
				.retrieve()                        // Provides access to the response status and headers via {@link ResponseEntity} along with error status handling.
												   // The retrieve method throws an WebClientResponseException when there will be a 4xx and 5xx series exception received.
				.onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new ApplicationException("webClient.createPublishMetaData failed with 4xx client error.")))
				.onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ApplicationException("webClient.createPublishMetaData failed with 5xx server error.")))
				.bodyToMono(PublishMetaData.class) // Extracts the response body to a Mono.
				.switchIfEmpty(Mono.empty())
				.log();
	}
	
	public Mono<PublishMetaData> updatePublishMetaData(final PublishMetaData publishMetaData) {
		log.debug(LOG_PREFIX + "updatePublishMetaData [publishMetaData=" + publishMetaData + ", contextPath=" + this.contextPath + "]");
	
		return this.webClient
				.put()
				.uri(this.contextPath + "/metadata/")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
				.bodyValue(publishMetaData)
				.retrieve()                        // Provides access to the response status and headers via {@link ResponseEntity} along with error status handling.
												   // The retrieve method throws an WebClientResponseException when there will be a 4xx and 5xx series exception received.
				.onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new ApplicationException("webClient.createPublishMetaData failed with 4xx client error.")))
				.onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ApplicationException("webClient.createPublishMetaData failed with 5xx server error.")))
				.bodyToMono(PublishMetaData.class) // Extracts the response body to a Mono.
				.switchIfEmpty(Mono.empty())
				.log();
	}
	
	public Mono<String> listPublishMetaData() {
		log.debug(LOG_PREFIX + "listPublishMetaData");
		
		return this.webClient
				.get()
				.uri(this.contextPath + "/metadata/")
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
				.retrieve()                        // Provides access to the response status and headers via {@link ResponseEntity} along with error status handling.
				                                   // The retrieve method throws an WebClientResponseException when there will be a 4xx and 5xx series exception received.
				.onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new ApplicationException("webClient.getPublishMetaDataById failed with 4xx client error.")))
				.onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ApplicationException("webClient.getPublishMetaDataById failed with 5xx server error.")))
				.bodyToMono(String.class) // Extracts the response body to a Mono.
				.switchIfEmpty(Mono.empty())
				.log();
	}
	
	public Mono<PublishMetaData> deletePublishMetaData(final String id) {
		log.debug(LOG_PREFIX + "deletePublishMetaData [id=" + id + "]");
		
		return this.webClient
				.delete()
				.uri(this.contextPath + "/metadata/{id}", id)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
				.retrieve()						// Provides access to the response status and headers via {@link ResponseEntity} along with error status handling.
												// The retrieve method throws an WebClientResponseException when there will be a 4xx and 5xx series exception received.
				.onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new ApplicationException("webClient.getPublishMetaDataById failed with 4xx client error.")))
				.onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ApplicationException("webClient.getPublishMetaDataById failed with 5xx server error.")))
				.bodyToMono(PublishMetaData.class) // Extracts the response body to a Mono.
				.switchIfEmpty(Mono.empty())
				.log();
	}
	
	public Flux<GithubRepo> listRepositories(final String sortField, final String sortDirection, final String repositoryName) {
		log.debug(LOG_PREFIX + "listRepositories [sortField=" + sortField + ", sortDirection=" + sortDirection + ", repositoryName=" + repositoryName + "]");

		// https://api.github.com/users/bomc/repos?sort=updated&direction=asc
		
		return this.githubWebClient
				.get()
				.uri("/users/" + repositoryName + "/repos?sort={sortField}&direction={sortDirection}", sortField, sortDirection)
				//.uri(uriBuilder -> uriBuilder.path("/users/" + repositoryName + "/repos").queryParam("sort", "{sortField}").queryParam("direction", "{sortDirection}").build(sortField, sortDirection))
				.accept(MediaType.ALL)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
				.retrieve()                        // Provides access to the response status and headers via {@link ResponseEntity} along with error status handling.
				.onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new ApplicationException("githubWebClient.listRepositories failed with 4xx client error.")))
				.onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ApplicationException("githubWebClient.listRepositories failed with 5xx server error.")))
				.bodyToFlux(GithubRepo.class)      // Extracts the response body to a Mono.
				.log()
				.switchIfEmpty(Mono.empty())
				.log();
	}
	
	public Mono<GithubRepo> getRepoByName(final String owner, final String repoName) {
		log.debug(LOG_PREFIX + "getRepoByName [owner=" + owner + ", repoName=" + repoName + "]");
		
		
        return this.githubWebClient
        		.get()
                .uri("/repos/{owner}/{repo}", owner, repoName)
				.accept(MediaType.ALL)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
                .retrieve()
				.onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new ApplicationException("githubWebClient.getRepoByName failed with 4xx client error.")))
				.onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ApplicationException("githubWebClient.getRepoByName failed with 5xx server error.")))
                .bodyToMono(GithubRepo.class)
                .log();
	}
}
