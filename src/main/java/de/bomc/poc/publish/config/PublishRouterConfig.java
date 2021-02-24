package de.bomc.poc.publish.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.Map;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.domain.model.PublishMetaData;
import de.bomc.poc.publish.infrastructure.handler.AnnotatedPublishMetaDataRequestHandler;
import de.bomc.poc.publish.infrastructure.handler.Custom2PublishMetaDataRequestHandler;
import de.bomc.poc.publish.infrastructure.handler.CustomPublishMetaDataRequestHandler;
import de.bomc.poc.publish.infrastructure.handler.PublishMetaDataRequestHandler;
import de.bomc.poc.publish.infrastructure.handler.ServerRedirectRequestHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class PublishRouterConfig {

	private static final String LOG_PREFIX = PublishRouterConfig.class.getName() + "#";
	
	@Value("${server.servlet.context-path}")
	private String contextRoot;
	
	final ServerRedirectRequestHandler serverRedirectHandler = new ServerRedirectRequestHandler();
	
	@Bean
    @RouterOperations({
        @RouterOperation(
        		path = "/api/metadata/{id}",
        		method = RequestMethod.GET, 
        		produces = MediaType.APPLICATION_JSON_VALUE, 
        		beanClass = PublishMetaDataRequestHandler.class, 
        		beanMethod = "get", 
        		headers = { "X-B3-TraceId=60f198ee56343ba864fe8b2a57d3eff7", "X-B3-ParentSpanId=05e3ac9a4f6e3b90" }, 
        		operation = @Operation(
        				operationId = "get",
        				summary = "Read PublishMetaData by id.",
        				tags = { "Publish" }, 
        				parameters = { @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "PublishMetaData payload", schema = @Schema(implementation = String.class, example = "bomc")) },
        				responses = {
        						@ApiResponse(responseCode = "200", description = "Found the PublishMetaData", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        						@ApiResponse(responseCode = "500", description = "Application error", content = @Content(schema = @Schema(implementation = String.class)))
        })),
        @RouterOperation(
        		path = "/api/metadata/", 
        		method = RequestMethod.GET, 
        		beanClass = PublishMetaDataRequestHandler.class, 
        		beanMethod = "list", 
        		headers = { "X-B3-TraceId=70f198ee56343ba864fe8b2a57d3eff7", "X-B3-ParentSpanId=15e3ac9a4f6e3b90" },
        		operation = @Operation(
        				operationId = "list", 
        				summary = "List all PublishMetaData instances.", 
        				tags = { "Publish" },
        				responses = {
        						@ApiResponse(responseCode = "200", description = "Read successful all PublishMetaData instances", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Map.class)))),
        						@ApiResponse(responseCode = "500", description = "Application error", content = @Content(schema = @Schema(implementation = String.class)))
		})),
        @RouterOperation(
        		path = "/api/metadata/{id}", 
        		method = RequestMethod.DELETE, 
        		beanClass = PublishMetaDataRequestHandler.class, 
        		beanMethod = "delete", 
        		headers = { "X-B3-TraceId=70f198ee56343ba864fe8b2a57d3eff7", "X-B3-ParentSpanId=25e3ac9a4f6e3b90" },
        		operation = @Operation(
        				operationId = "delete", 
        				summary = "Delete instance by given id.", 
        				tags = { "Publish" },
        				parameters = { @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "PublishMetaData identifier to delete", schema = @Schema(implementation = String.class, example = "42")) },
        				responses = {
        						@ApiResponse(responseCode = "200", description = "Delete successful by given name", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Map.class)))),
        						@ApiResponse(responseCode = "500", description = "Application error", content = @Content(schema = @Schema(implementation = String.class)))
		})),
        @RouterOperation(
        		path = "/api/metadata/request-validation", 
        		method = RequestMethod.POST, 
        		consumes = MediaType.APPLICATION_JSON_VALUE, 
        		beanClass = Custom2PublishMetaDataRequestHandler.class, 
        		beanMethod = "handleRequest",
        		headers = { "X-B3-TraceId=80f198ee56343ba864fe8b2a57d3eff7", "X-B3-ParentSpanId=35e3ac9a4f6e3b90" },
        		operation = @Operation(
        				operationId = "metadata", 
        				summary = "Creates a PublishMetaData instance in memory map with request body validation. Validation ist manually executed.", 
        				tags = { "Publish" },
        				requestBody = @RequestBody(
        						required = true, 
        						content = @Content(schema = @Schema(implementation = PublishMetaData.class))),
        				responses = {
        						@ApiResponse(responseCode = "200", description = "successful finished", content = @Content(schema = @Schema(implementation = Void.class))),
        						@ApiResponse(responseCode = "500", description = "application error", content = @Content(schema = @Schema(implementation = Void.class)))
		})),
        @RouterOperation(
        		path = "/api/metadata/annotation-validation", 
        		method = RequestMethod.POST, 
        		consumes = MediaType.APPLICATION_JSON_VALUE, 
        		beanClass = AnnotatedPublishMetaDataRequestHandler.class, 
        		beanMethod = "handleRequest",
        		headers = { "X-B3-TraceId=82f198ee56343ba864fe8b2a57d3eff7", "X-B3-ParentSpanId=11e3ac9a4f6e3b90" },
        		operation = @Operation(
        				operationId = "metadata", 
        				summary = "Creates a PublishMetaData instance in in memory map with body validation. Validation is executed by framework.", 
        				tags = { "Publish" },
        				requestBody = @RequestBody(
        						required = true, 
        						content = @Content(schema = @Schema(implementation = PublishMetaData.class))),
        				responses = {
        						@ApiResponse(responseCode = "200", description = "successful finished", content = @Content(schema = @Schema(implementation = Void.class))),
        						@ApiResponse(responseCode = "500", description = "application error", content = @Content(schema = @Schema(implementation = Void.class)))
		})),
        @RouterOperation(
        		path = "/api/metadata/", 
        		method = RequestMethod.PUT, 
        		consumes = MediaType.APPLICATION_JSON_VALUE, 
        		beanClass = CustomPublishMetaDataRequestHandler.class, 
        		beanMethod = "handleRequest", 
        		headers = { "X-B3-TraceId=80f198ee56343ba864fe8b2a57d3eff7", "X-B3-ParentSpanId=45e3ac9a4f6e3b90" },
        		operation = @Operation(
        				operationId = "metadata", 
        				summary = "Updates a PublishMetaData instance in in memory map.", 
        				tags = { "Publish" },
        				requestBody = @RequestBody(
        						required = true, 
        						content = @Content(schema = @Schema(implementation = PublishMetaData.class))),
        				responses = {
        						@ApiResponse(responseCode = "200", description = "successful finished", content = @Content(schema = @Schema(implementation = Void.class))),
        						@ApiResponse(responseCode = "500", description = "application error", content = @Content(schema = @Schema(implementation = Void.class)))
		}))
    })
	public RouterFunction<ServerResponse> publishToConsumer(
			final PublishMetaDataRequestHandler publishMetaDataHandler, 
			final AnnotatedPublishMetaDataRequestHandler annotatedPublishMetaDataHandler, 
			final CustomPublishMetaDataRequestHandler customPublishMetaDataHandler,
			final Custom2PublishMetaDataRequestHandler custom2PublishMetaDataRequestHandler) {
		log.debug(LOG_PREFIX + "publishToConsumer");
		
		return nest(path(contextRoot + "/metadata"),
				nest(accept(APPLICATION_JSON),
						route(GET("/{id}"), publishMetaDataHandler::get)
						.andRoute(GET("/"), publishMetaDataHandler::list)
						.andRoute(DELETE("/{id}"), publishMetaDataHandler::delete)
						)
				.andNest(contentType(APPLICATION_JSON),
						route(POST("/annotation-validation"), annotatedPublishMetaDataHandler::handleRequest)
						.andRoute(POST("/request-validation"), custom2PublishMetaDataRequestHandler::handleRequest)
						)
				.andNest(contentType(APPLICATION_JSON),
						route(PUT("/"), customPublishMetaDataHandler::handleRequest)
//				.andNest((serverRequest) -> serverRequest.cookies().containsKey("Redirect-Traffic"), 
//						route(all(), serverRedirectHandler)
						)
					);
	}
	
}