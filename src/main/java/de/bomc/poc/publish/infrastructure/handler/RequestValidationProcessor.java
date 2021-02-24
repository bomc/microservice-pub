package de.bomc.poc.publish.infrastructure.handler;

import java.util.List;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RequestValidationProcessor {

	private static final String LOG_PREFIX = RequestValidationProcessor.class.getName() + "#";
	
	private final RequestValidationProcessorHelper requestValidationProcessorHelper;
	
	public RequestValidationProcessor(final RequestValidationProcessorHelper requestValidationProcessorHelper) {
		this.requestValidationProcessorHelper = requestValidationProcessorHelper;
	}
	
	public <T_REQUEST_BODY, T_RESPONSE_BODY> Mono<ServerResponse> validateRequest(final Function<T_REQUEST_BODY, Mono<T_RESPONSE_BODY>> inMemoryPublishMetaDataServiceProcessor, final ServerRequest serverRequest, Class<T_REQUEST_BODY> bodyClass) {
		log.debug(LOG_PREFIX + "validateRequest");
		
		return serverRequest
				.bodyToMono(bodyClass)
				.flatMap(requestBody -> {
					final List<ObjectError> objectErrorList = this.requestValidationProcessorHelper.hasValidationError(requestBody, bodyClass);
					
					if (objectErrorList.isEmpty()) {
						return inMemoryPublishMetaDataServiceProcessor.apply(requestBody);
					} else {
						return this.requestValidationProcessorHelper.onValidationErrors(objectErrorList, requestBody, serverRequest);
					} // end if
					})
				.flatMap(this::mapToResponse)
				.switchIfEmpty(handleEmptyReturnValueFromService(serverRequest));
	}
	
	private Mono<ServerResponse> mapToResponse(final Object responseBody) {
		log.debug(LOG_PREFIX + "mapToResponse [responseBody=" + responseBody + "]");
		
		if(responseBody instanceof PublishMetaData) {
			return this.mapToOKServerResponse(responseBody);
		} else {
			return this.mapToNOKServerResponse(responseBody);
		}
	}
	
	private <T_RESPONSE_BODY> Mono<ServerResponse> mapToOKServerResponse(final T_RESPONSE_BODY responseBody) {
		log.debug(LOG_PREFIX + "mapToOKServerResponse [responseBody=" + responseBody + "]");
		
		return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(responseBody));
	}

	private <T_RESPONSE_BODY> Mono<ServerResponse> mapToNOKServerResponse(final T_RESPONSE_BODY responseBody) {
		log.debug(LOG_PREFIX + "mapToNOKServerResponse [responseBody=" + responseBody + "]");
		
		return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(responseBody));
	}
	
	private Mono<ServerResponse> handleEmptyReturnValueFromService(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "handleEmptyReturnValueFromService [serverRequest=" + serverRequest + "]");
		
		return ServerResponse
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(this.requestValidationProcessorHelper.getErrorAttributes(serverRequest, HttpStatus.BAD_REQUEST.value(), null)));
	}
	
}
