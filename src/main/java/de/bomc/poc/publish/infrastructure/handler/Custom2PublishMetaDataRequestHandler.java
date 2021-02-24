package de.bomc.poc.publish.infrastructure.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.application.core.RemoteConsumerService;
import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * A handler that handles requests validation. 
 */
@Slf4j
@Component
public class Custom2PublishMetaDataRequestHandler {

	private static final String LOG_PREFIX = Custom2PublishMetaDataRequestHandler.class.getName() + "#";
	
	private final RequestValidationProcessor requestValidationProcessor;
	private final RemoteConsumerService remoteConsumerService;
	
	private Custom2PublishMetaDataRequestHandler(final RequestValidationProcessor requestValidationProcessor, final RemoteConsumerService remoteConsumerService) {
		this.requestValidationProcessor = requestValidationProcessor;
		this.remoteConsumerService = remoteConsumerService;
	}
	
	/**
     * Delegates the request to the parent class.
     */
	public Mono<ServerResponse> handleRequest(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "handleRequest [handleRequest=" + serverRequest + "]");

		return this.requestValidationProcessor.validateRequest(this.remoteConsumerService::createPublishMetaData, serverRequest, PublishMetaData.class);
	}
}
