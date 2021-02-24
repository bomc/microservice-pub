package de.bomc.poc.publish.infrastructure.handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.bomc.poc.publish.application.core.RequestTraceId;
import de.bomc.poc.publish.application.validation.ErrorAttributeEnum;
import de.bomc.poc.publish.application.validation.ValidationFieldError;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RequestValidationProcessorHelper {

	private static final String LOG_PREFIX = RequestValidationProcessorHelper.class.getCanonicalName() + "#";
	
	private static final String ERROR_VALUE = "Indicates that the server cannot will not process the request due to something that is perceived to be a client error "
			+ "(e.g., malformed request syntax, invalid request message framing, or deceptive request routing).";
	
	private final Validator validator;
	private final ObjectMapper objectMapper;
	private final RequestTraceId requestTraceId;
	
	public RequestValidationProcessorHelper(final Validator validator, final ObjectMapper objectMapper, final RequestTraceId requestTraceId) {
		this.validator = validator;
		this.objectMapper = objectMapper;
		this.requestTraceId = requestTraceId;
	}
	
	public <T_REQUEST_BODY> List<ObjectError> hasValidationError(final Object requestBody, Class<T_REQUEST_BODY> bodyClass) {
		log.debug(LOG_PREFIX + "hasValidationError");
		
		final Errors errors = new BeanPropertyBindingResult(requestBody, bodyClass.getName());
		// Uses spring boot validator for validating the request body.
		this.validator.validate(requestBody, errors);
		
		return errors.getAllErrors();
	}
	
	public <T_RESPONSE_BODY> Mono<JsonNode> onValidationErrors(final List<ObjectError> objectErrorList, final T_RESPONSE_BODY invalidBody, final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "onValidationErrors [objectErrorList=" + objectErrorList + ", invalidBody=" + invalidBody
				+ ", serverRequest=" + serverRequest + "]");

		final List<ValidationFieldError> responseErrorList = new ArrayList<ValidationFieldError>();

		objectErrorList.stream().forEach(objectError -> {
			final ValidationFieldError validationFieldError = new ValidationFieldError(objectError.getObjectName(),
					objectError.getCode(), objectError.getDefaultMessage());
			responseErrorList.add(validationFieldError);
		});

		final JsonNode jsonNode = objectMapper.valueToTree(this.getErrorAttributes(serverRequest, HttpStatus.BAD_REQUEST.value(), responseErrorList));
		
		// To write json node as string.
		// final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
		
		return Mono.just(jsonNode);
	}
	
	public Map<String, Object> getErrorAttributes(final ServerRequest serverRequest, final int httpStatus, final List<ValidationFieldError> responseErrorList) {
		log.debug(LOG_PREFIX + "getErrorAttributes [serverRequest=" + serverRequest + ", httpStatus=" + httpStatus + ", responseErrorList=" + responseErrorList + "]");

		final Map<String, Object> errorAttributes = new LinkedHashMap<>();

		errorAttributes.put(ErrorAttributeEnum.TIMESTAMP.getValue(), LocalDateTime.now());
		errorAttributes.put(ErrorAttributeEnum.PATH.getValue(), serverRequest.path());
		errorAttributes.put(ErrorAttributeEnum.STATUS.getValue(), httpStatus);
		errorAttributes.put(ErrorAttributeEnum.ERROR.getValue(), ERROR_VALUE);
		
		if(responseErrorList != null) {
			errorAttributes.put(ErrorAttributeEnum.MESSAGE.getValue(), responseErrorList);
		}
		
		errorAttributes.put(ErrorAttributeEnum.TRACE_ID.getValue(), this.requestTraceId.traceId());
		// TODO request is not same 
		errorAttributes.put(ErrorAttributeEnum.REQUEST_ID.getValue(), serverRequest.exchange().getRequest().getId());

		return errorAttributes;
	}
}
