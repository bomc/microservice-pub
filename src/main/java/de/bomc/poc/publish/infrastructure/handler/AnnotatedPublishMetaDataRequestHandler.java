package de.bomc.poc.publish.infrastructure.handler;

import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.application.core.RemoteConsumerService;
import de.bomc.poc.publish.application.validation.AbstractValidationHandler;
import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * A handler that handles requests validation by annotation. 
 */
@Slf4j
@Service
public class AnnotatedPublishMetaDataRequestHandler extends AbstractValidationHandler<PublishMetaData, Validator> {

	private static final String LOG_PREFIX = AnnotatedPublishMetaDataRequestHandler.class.getName() + "#";
	
	private final RemoteConsumerService remoteConsumerService;
	
	/**
	 * The constructor is private, spring boot uses reflection during autowiring.
	 */
	private AnnotatedPublishMetaDataRequestHandler(final Validator validator, final RemoteConsumerService remoteConsumerService) {
        super(PublishMetaData.class, validator);
        
        this.remoteConsumerService = remoteConsumerService;
    }

	@Override
	public Mono<ServerResponse> processBody(final PublishMetaData publishMetaData, final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "processBody [publishMetaData=" + publishMetaData + ", serverRequest]");
		
		return this.remoteConsumerService
				.createPublishMetaData((PublishMetaData)publishMetaData)
				.flatMap(retPublishMetaData -> ServerResponse.ok().bodyValue(retPublishMetaData))
				.log();
	}
	
	/**
	 * This method is only overwritten because OpenAPI does not call the handler method in a super class -> issue by OpenAPI.
	 *   
	 * @param serverRequest
	 * @return
	 */
	@Override
	public Mono<ServerResponse> handleRequest(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "handleRequest [serverRequest=" + serverRequest + "]");
		
		return super.handleRequest(serverRequest);
	}
	
}
