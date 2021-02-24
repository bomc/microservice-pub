package de.bomc.poc.publish.infrastructure.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.application.core.RemoteConsumerService;
import de.bomc.poc.publish.application.validation.AbstractValidationHandler;
import de.bomc.poc.publish.application.validation.CustomPublishMetaDataValidator;
import de.bomc.poc.publish.application.validation.ValidationFieldError;
import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * A handler that handles requests validation by annotation and overrides the onValidationErrors from parent class.
 * 
 */
@Slf4j
@Component
public class CustomPublishMetaDataRequestHandler extends AbstractValidationHandler<PublishMetaData, CustomPublishMetaDataValidator> {

	private static final String LOG_PREFIX = CustomPublishMetaDataRequestHandler.class.getName() + "#";
	
	private final RemoteConsumerService remoteConsumerService;
	
	/**
	 * The constructor is private, spring boot uses reflection during autowiring.
	 */
	private CustomPublishMetaDataRequestHandler(final RemoteConsumerService remoteConsumerService) {
        super(PublishMetaData.class, new CustomPublishMetaDataValidator());
 
        this.remoteConsumerService = remoteConsumerService;
    }

    @Override
    protected Mono<ServerResponse> processBody(final PublishMetaData publishMetaData, final ServerRequest serverRequest) {
    	log.debug(LOG_PREFIX + "processBody [publishMetaData=" + publishMetaData + "serverRequest]");

		return this.remoteConsumerService
				.updatePublishMetaData((PublishMetaData)publishMetaData)
				.flatMap(retPublishMetaData -> ServerResponse.ok().bodyValue(retPublishMetaData))
				.log();
    }

    @Override
    protected Mono<ServerResponse> onValidationErrors(final Errors errors, final PublishMetaData publishMetaData, final ServerRequest serverRequest) {
        log.debug(LOG_PREFIX + "onValidationErrors [errors=" + errors + ", publishMetaData=" + publishMetaData + ", serverRequest]");

		final List<ValidationFieldError> responseErrorList = new ArrayList<ValidationFieldError>();
		
		final List<ObjectError> allErrorsList = errors.getAllErrors();
		
		allErrorsList.stream().forEach(objectError -> {
			final ValidationFieldError validationFieldError = new ValidationFieldError(objectError.getObjectName(), objectError.getCode(), objectError.getDefaultMessage());
			responseErrorList.add(validationFieldError);
		});

        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(getErrorAttributes(serverRequest, responseErrorList)));
    }
    
    /**
     * Delegates the request to the parent class.
     */
	@Override
	public Mono<ServerResponse> handleRequest(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "handleRequest [serverRequest=" + serverRequest + "]");
		
		return super.handleRequest(serverRequest);
	}
}
