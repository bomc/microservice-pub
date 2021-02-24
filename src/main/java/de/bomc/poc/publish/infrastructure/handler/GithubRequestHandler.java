package de.bomc.poc.publish.infrastructure.handler;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.domain.model.GithubRepo;
import de.bomc.poc.publish.infrastructure.webclient.WebClientPublisher;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
//@Log4j2
@Service
public class GithubRequestHandler {

	private static final String LOG_PREFIX = GithubRequestHandler.class.getName() + "#";
	
	private final WebClientPublisher webClientPublisher;
	
	private static final String QUERY_PARAM_SORT = "sort";
	private static final String QUERY_PARAM_DIRECTION = "direction";
	private static final String QUERY_PARAM_NAME = "name";
	private static final String DEFAULT_QUERY_PARAM_SORT = "updated";
	private static final String DEFAULT_QUERY_PARAM_DIRECTION = "asc";
	private static final String DEFAULT_QUERY_PARAM_NAME = "bomc";
	
	private static final String PATH_VARIABLE_REPO = "repo"; 
	private static final String PATH_VARIABLE_OWNER = "owner"; 
	
	public GithubRequestHandler(final WebClientPublisher webClientPublisher) {
		this.webClientPublisher = webClientPublisher;
	}

	public Mono<ServerResponse> listGithubRepositories(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "listGithubRepositories");

		final String sortField = serverRequest.queryParam(QUERY_PARAM_SORT).orElse(DEFAULT_QUERY_PARAM_SORT);
		final String sortDirection = serverRequest.queryParam(QUERY_PARAM_DIRECTION).orElse(DEFAULT_QUERY_PARAM_DIRECTION);
		final String repositoryName = serverRequest.queryParam(QUERY_PARAM_NAME).orElse(DEFAULT_QUERY_PARAM_NAME);
	
		// NOTE: listRepositories method returns a flux.
		return ServerResponse
				.ok()
				.body(this.webClientPublisher.listRepositories(sortField, sortDirection, repositoryName), GithubRepo.class)
				.log()
				.switchIfEmpty(Mono.empty())
				.log();
	}

	public Mono<ServerResponse> getGithubRepositoryByName(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "getGithubRepositoryByName");

		final String repoName = serverRequest.pathVariable(PATH_VARIABLE_REPO);
		final String owner = serverRequest.pathVariable(PATH_VARIABLE_OWNER);
	
		return ServerResponse
				.ok()
				.body(this.webClientPublisher.getRepoByName(owner, repoName), GithubRepo.class)
				.log()
				.switchIfEmpty(Mono.empty())
				.log();
	}

}
