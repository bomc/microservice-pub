package de.bomc.poc.publish.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.domain.model.GithubRepo;
import de.bomc.poc.publish.infrastructure.handler.GithubRequestHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GithubRouterConfig {

	private static final String LOG_PREFIX = GithubRouterConfig.class.getName() + "#";

	@Value("${server.servlet.context-path:/api}")
	private String contextRoot;
	
	@Bean
    @RouterOperations({
        @RouterOperation(
        		path = "/api/github/user/repo",
        		method = RequestMethod.GET, 
        		produces = MediaType.APPLICATION_JSON_VALUE, 
        		beanClass = GithubRequestHandler.class, 
        		beanMethod = "listGithubRepositories", 
        		headers = { "X-B3-TraceId=30f198ee56343ba864fe8b2a57d3eff7", "X-B3-ParentSpanId=35e3ac9a4f6e3b90" }, 
        		operation = @Operation(
        				operationId = "listGithubRepositories",
        				summary = "Read all remote repositories from bomc github.",
        				tags = { "Github" },
        				parameters = { @Parameter(in = ParameterIn.QUERY, name = "sort", required = false, description = "Sorting search result - stars, forks or updated"),
        						       @Parameter(in = ParameterIn.QUERY, name = "direction", required = false, description = "Direction of search results - asc, desc"),
        						       @Parameter(in = ParameterIn.QUERY, name = "name", required = false, description = "Name of repository - bomc")
        				},
        				responses = {
        						@ApiResponse(responseCode = "200", description = "List all repositories from remote github successful.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GithubRepo.class))),
        						@ApiResponse(responseCode = "404", description = "If there no repositories by the given name.", content = @Content(schema = @Schema(implementation = String.class))),
        						@ApiResponse(responseCode = "500", description = "Application error", content = @Content(schema = @Schema(implementation = String.class)))
        })),
        @RouterOperation(
        		path = "/api/github/user/repo/{owner}/{repo}", 
        		method = RequestMethod.GET,
                produces = MediaType.APPLICATION_JSON_VALUE, 
        		beanClass = GithubRequestHandler.class, 
        		beanMethod = "getGithubRepositoryByName", 
        		headers = { "X-B3-TraceId=71f198ee56343ba864fe8b2a57d3eff7", "X-B3-ParentSpanId=26e3ac9a4f6e3b90" },
        		operation = @Operation(
        				operationId = "getGithubRepositoryByName", 
        				summary = "Get repo metadata by given repository name.", 
        				tags = { "Github" },
        				parameters = { @Parameter(in = ParameterIn.PATH, name = "repo", required = true, description = "The given repository name.", schema = @Schema(implementation = String.class, example = "hack")),
        				               @Parameter(in = ParameterIn.PATH, name = "owner", required = true, description = "The given owner of the repository.", schema = @Schema(implementation = String.class, example = "bomc")) },
        				responses = {
        						@ApiResponse(responseCode = "200", description = "Get repository metadata by given repository name.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GithubRepo.class))),
        						@ApiResponse(responseCode = "404", description = "If there no repositories by the given name.", content = @Content(schema = @Schema(implementation = String.class))),
        						@ApiResponse(responseCode = "500", description = "Application error", content = @Content(schema = @Schema(implementation = String.class)))
		}))
    })
	public RouterFunction<ServerResponse> githubRouting(final GithubRequestHandler githubRequestHandler) {
		log.debug(LOG_PREFIX + "listGithubRepositories");
		
		return nest(path(this.contextRoot + "/github/user"),
				nest(accept(MediaType.APPLICATION_JSON),
						route(GET("/repo/{owner}/{repo}"), githubRequestHandler::getGithubRepositoryByName)
						.andRoute(GET("/repo"), githubRequestHandler::listGithubRepositories)
						)
					);
	}
}