package de.bomc.poc.publish.infrastructure.handler;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ServerRedirectRequestHandler implements HandlerFunction<ServerResponse> {

	private static final String LOG_PREFIX = ServerRedirectRequestHandler.class.getName() + "#";

	private final WebClient webClient = WebClient.create();

	@Override
	public Mono<ServerResponse> handle(final ServerRequest serverRequest) {
		log.debug(LOG_PREFIX + "handle");

		// Checkit
		return webClient
				.method(serverRequest.method())
				.uri(serverRequest.headers().header("Redirect-Traffic").get(0))
				.headers((h) -> h.addAll(serverRequest.headers().asHttpHeaders()))
				.body(BodyInserters.fromDataBuffers(serverRequest.bodyToFlux(DataBuffer.class)))
				.cookies(c -> serverRequest
						.cookies().forEach((key, list) -> list.forEach(cookie -> c.add(key, cookie.getValue()))))
				.exchange()
				.flatMap(cr -> ServerResponse.status(cr.statusCode())
						.cookies(c -> c.addAll(cr.cookies()))
						.headers(hh -> hh.addAll(cr.headers().asHttpHeaders()))
						.body(BodyInserters.fromDataBuffers(cr.bodyToFlux(DataBuffer.class))));
	}
}
