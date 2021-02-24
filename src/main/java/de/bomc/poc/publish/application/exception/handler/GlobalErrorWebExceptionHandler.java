package de.bomc.poc.publish.application.exception.handler;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
	
	private static final String LOG_PREFIX = GlobalErrorWebExceptionHandler.class.getName() + "#";

	public GlobalErrorWebExceptionHandler(final ErrorAttributes errorAttributes, final Resources resources,
			final ErrorProperties errorProperties, final ApplicationContext applicationContext) {

		super(errorAttributes, resources, errorProperties, applicationContext);
	}

	/**
	 * The errorAttributes object will be the exact copy of the one that is passed
	 * in the Web Exception Handler's constructor. This should be ideally the
	 * customized Error Attributes class.
	 */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
    	log.debug(LOG_PREFIX + "getRoutingFunction");
    	
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * Decide on a standard response format for errorPropertiesMap.
     */
    @Override
    protected Mono<ServerResponse> renderErrorResponse(final ServerRequest serverRequest) {
    	log.debug(LOG_PREFIX + "renderErrorResponse [serverRequest=" + serverRequest + "]");

    	final Map<String, Object> errorPropertiesMap = getErrorAttributes(serverRequest,
                ErrorAttributeOptions.of(
                		ErrorAttributeOptions.Include.BINDING_ERRORS,
                        ErrorAttributeOptions.Include.EXCEPTION,
                        ErrorAttributeOptions.Include.STACK_TRACE, // trace
                        ErrorAttributeOptions.Include.MESSAGE));
    	
        return ServerResponse.status(HttpStatus.valueOf(super.getHttpStatus(errorPropertiesMap)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(getErrorResponse(serverRequest, errorPropertiesMap)));
    }

    private Map<String, Object> getErrorResponse(final ServerRequest serverRequest, final Map<String, Object> errorPropertiesMap) {
    	log.debug(LOG_PREFIX + "getErrorResponse [serverRequest=" + serverRequest + ", errorPropertiesMap=" + errorPropertiesMap + "]");
    	
    	// Add here some additional error attributes.
    	// errorPropertiesMap.put("myCustomAttribute", "value");
    	
        return errorPropertiesMap;

    }
    
}