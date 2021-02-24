package de.bomc.poc.publish.application.validation;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.application.core.RequestTraceId;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractValidationHandler<T, V extends Validator> {

	private static final String LOG_PREFIX = AbstractValidationHandler.class.getName() + "#";

	private final Class<T> validationClass;

	private final V validator;

	@Autowired
	private RequestTraceId requestTraceId;

	protected AbstractValidationHandler(final Class<T> clazz, final V validator) {
		this.validationClass = clazz;
		this.validator = validator;
	}

	abstract protected Mono<ServerResponse> processBody(final T validBody, final ServerRequest originalRequest);

	public Mono<ServerResponse> handleRequest(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "handleRequest [serverRequest=" + serverRequest + "]");

		return serverRequest.bodyToMono(this.validationClass).flatMap(body -> {

			final Errors errors = new BeanPropertyBindingResult(body, this.validationClass.getName());
			this.validator.validate(body, errors);

			if (errors == null || errors.getAllErrors().isEmpty()) {
				log.debug(LOG_PREFIX + "handleRequest - processes processBody");

				return processBody(body, serverRequest);
			} else {
				log.debug(LOG_PREFIX + "handleRequest - processes onValidationErrors");

				// Method can be overridden by subclass for special custom error handling.
				return onValidationErrors(errors, body, serverRequest);
			}
		});
	}

	protected Mono<ServerResponse> onValidationErrors(final Errors errors, final T invalidBody, final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "onValidationErrors [errors=" + errors + ", invalidBody=" + invalidBody
				+ ", serverRequest=" + serverRequest + "]");

		final List<ValidationFieldError> responseErrorList = new ArrayList<ValidationFieldError>();

		final List<ObjectError> allErrorsList = errors.getAllErrors();

		allErrorsList.stream().forEach(objectError -> {
			final ValidationFieldError validationFieldError = new ValidationFieldError(objectError.getObjectName(),
					objectError.getCode(), objectError.getDefaultMessage());
			responseErrorList.add(validationFieldError);
		});

		return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(this.getErrorAttributes(serverRequest, responseErrorList)));
	}

	protected Map<String, Object> getErrorAttributes(final ServerRequest serverRequest, final List<ValidationFieldError> responseErrorList) {

		final Map<String, Object> errorAttributes = new LinkedHashMap<>();

		errorAttributes.put(ErrorAttributeEnum.TIMESTAMP.getValue(), new Date());
		errorAttributes.put(ErrorAttributeEnum.PATH.getValue(), serverRequest.path());
		errorAttributes.put(ErrorAttributeEnum.STATUS.getValue(), HttpStatus.BAD_REQUEST.value());
		// TODO
		errorAttributes.put(ErrorAttributeEnum.ERROR.getValue(),
				"Indicates that the server cannot will not process the request due to something that is perceived "
				+ "to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
		errorAttributes.put(ErrorAttributeEnum.MESSAGE.getValue(), responseErrorList);
		errorAttributes.put(ErrorAttributeEnum.TRACE_ID.getValue(), this.requestTraceId.traceId());
		// TODO request is not same 
		errorAttributes.put(ErrorAttributeEnum.REQUEST_ID.getValue(), serverRequest.exchange().getRequest().getId());

		return errorAttributes;
	}
}
