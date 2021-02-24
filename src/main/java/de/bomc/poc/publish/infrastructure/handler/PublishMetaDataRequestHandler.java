package de.bomc.poc.publish.infrastructure.handler;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.application.core.RemoteConsumerService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
//@Log4j2
@Service
public class PublishMetaDataRequestHandler {

	private static final String LOG_PREFIX = PublishMetaDataRequestHandler.class.getName() + "#";

	private static final String PATH_VARIABLE_NAME = "id";
	
	private final RemoteConsumerService remoteConsumerService;
	
	public PublishMetaDataRequestHandler(final RemoteConsumerService remoteConsumerService) {
		this.remoteConsumerService = remoteConsumerService;
	}

	public Mono<ServerResponse> get(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "get");

		return this.remoteConsumerService.getPublishMetaDataById(serverRequest.pathVariable(PATH_VARIABLE_NAME))
				.flatMap(publishMetaData -> ServerResponse.ok().bodyValue(publishMetaData)).log()
				.switchIfEmpty(ServerResponse.notFound().build()).log();
	}

	public Mono<ServerResponse> list(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "list");

		return this.remoteConsumerService.listPublishMetaData().flatMap(map -> ServerResponse.ok().bodyValue(map)).log();
	}

	public Mono<ServerResponse> delete(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "delete");

		return this.remoteConsumerService.deletePublishMetaData(serverRequest.pathVariable(PATH_VARIABLE_NAME))
				.flatMap(deletedPublishMetaData -> ServerResponse.ok().bodyValue(deletedPublishMetaData)).log()
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	/**
	 * Outsourced to {@link AnnotatedPublishMetaDataHandler} to handle parameter validation by annotations.
	 */
//	public Mono<ServerResponse> create(final ServerRequest serverRequest) {
//	log.debug(LOG_PREFIX + "create");
//	
//	return serverRequest
//			.bodyToMono(PublishMetaData.class)
//			.flatMap(this.inMemoryPublishMetaDataService::create)
//			.log()
//			.flatMap(publishMetaData -> ServerResponse.created(URI.create("/metadata/" + publishMetaData.getName())).build())
//			.log();
//	}

}
