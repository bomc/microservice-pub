package de.bomc.poc.publish.application.exception.handler;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import de.bomc.poc.publish.application.core.RequestTraceId;
import de.bomc.poc.publish.application.exception.common.RootException;
import de.bomc.poc.publish.application.validation.ErrorAttributeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles errors at a global level. To handle the WebFlux errors at a global level it only needs the two steps:
 * - Customize the Global Error Response Attributes, here in this class.
 * - Implement the Global Error Handler, see {@link GlobalErrorWebExceptionHandler}
 * 
 * The exception that the handler throws will be automatically translated to an HTTP status and a JSON error body. 
 * To customize these, simply extend the DefaultErrorAttributes class and override its getErrorAttributes() method.
 *
 * Alternative to the global handler is exception handling at functional handling.
 * 
 * public Mono<ServerResponse> handleRequest(ServerRequest request) {
 * 	return sayHello(request)
 * 		.flatMap(s -> ServerResponse.ok()
 * 		.contentType(MediaType.TEXT_PLAIN)
 * 		.syncBody(s))
 * 		.onErrorResume(e -> Mono.just("Error " + e.getMessage())
 * 		.flatMap(s -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).syncBody(s)));
 * }
 */
@Slf4j
@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

	private static final String LOG_PREFIX = GlobalErrorAttributes.class.getName() + "#";
	
	private final RequestTraceId requestTraceId;
	
	public GlobalErrorAttributes(final RequestTraceId requestTraceId) {
		
		this.requestTraceId = requestTraceId;
	}

	/**
	 * Implements special handling for type of {@link RootExceptions} and other unexpected types of exception. 
	 */
	@Override
	public Map<String, Object> getErrorAttributes(final ServerRequest request, final ErrorAttributeOptions options) {
		final var error = getError(request);
		final var errorAttributes = super.getErrorAttributes(request, options);
		
		log.info(LOG_PREFIX + "getErrorAttributes [error=" + error + ", errorAttributes=" + errorAttributes + "]");
		
		errorAttributes.put(ErrorAttributeEnum.TRACE_ID.getValue(), requestTraceId.traceId());
		
		if (error instanceof RootException) {
			//
			// Handle here application exception, in this case the RootException
			log.debug(LOG_PREFIX + "getErrorAttribute - Caught an instance of ->" + error.getClass().getName());
			
			final var errorStatus = ((RootException) error).getStatus();
			errorAttributes.replace(ErrorAttributeEnum.STATUS.getValue(), errorStatus.value());
			errorAttributes.replace(ErrorAttributeEnum.ERROR.getValue(), errorStatus.getReasonPhrase());
		} else {
			//
			// Handle here more specific exceptions.
			log.debug(LOG_PREFIX + "getErrorAttribute - Unexpected error, caught an instance of ->" + error.getClass().getName());
			
			errorAttributes.replace(ErrorAttributeEnum.MESSAGE.getValue(), "Unexpected error!");
		 }

		return errorAttributes;
	}
	
}
