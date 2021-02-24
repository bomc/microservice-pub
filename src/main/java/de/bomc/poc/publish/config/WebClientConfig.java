package de.bomc.poc.publish.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class WebClientConfig {

	private static final Integer TIMEOUT = 5000;
	private static final Integer TRANFER_SIZE = 32;

	private static final String USER_AGENT = "Bomc Publisher WebClient";
	private static final String GITHUB_V3_MIME_TYPE = "application/vnd.github.v3+json";

	@Value("${bomc.consumer}")
	private String consumerBaseUrl;

	@Value("${bomc.github}")
	private String githubBaseUrl;
	
	private final WebClient.Builder webClientBuilder;
	
	private static final Logger requestLog = LoggerFactory.getLogger("de.bomc.poc.publish.request");

	public WebClientConfig(final WebClient.Builder webClientBuilder) {
		this.webClientBuilder = webClientBuilder;
	}
	
	/**
	 * The correct usage of the WebClient to automatically report metrics, so dont't construct the client manually like the following:
	 * 
	 * WebClient client = WebClient.builder().build();
	 * 
	 * Even though this works fine for making HTTP calls, it won't get metrics from such instances out-of-the-box.
	 * Instead of creating WebClient instances from scratch (like above), inject an instance of WebClient.Builder and start from there. 
	 * This builder instance is already configured to automatically report metrics.
	 * 
	 * By the way, the same works for the RestTemplate, just use RestTemplateBuilder then.
	 * 
	 * Given this pre-configured builder, it should be defined a WebClient bean for the whole application to add general timeouts. 
	 * This is optional, and it is also possible to use WebClient.Builder directly within in classes.
	 *  
	 * @param webClientBuilder
	 * @return
	 */
	@Bean // Inject this bean -> -at Qualifier("webClient")
	public WebClient webClient() {

		final TcpClient tcpClient = TcpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
				.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT)).addHandlerLast(new WriteTimeoutHandler(TIMEOUT)))
				.wiretap(true); // Helps logging, each request and response will be logged in full detail.

		@SuppressWarnings("deprecation")
		final ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(
				HttpClient.from(tcpClient).keepAlive(true));

		return webClientBuilder
				.baseUrl(this.consumerBaseUrl)
				.codecs(clientCodecConfigure -> clientCodecConfigure.defaultCodecs().enableLoggingRequestDetails(true))
				.clientConnector(clientHttpConnector)
//				.codecs(clientConfigurer -> clientConfigurer.defaultCodecs().maxInMemorySize(TRANFER_SIZE * 1024 * 1024)) // is set in application.properties -> spring.codec.max-in-memory-size
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
				.filters(exchangeFilterFunctions -> {
					exchangeFilterFunctions.add(logRequest());
//					exchangeFilterFunctions.add(logResponse());
				})
				.build();
	}

	/**
	 * This bean configures the webclient for github access.
	 * 
	 * NOTE: There two different webClient configurations in this class  to differeniate 
	 */
	@Bean // Inject this bean -> -at Qualifier("githubWebClient")
	public WebClient githubWebClient() {

		final DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(this.githubBaseUrl);
		defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
		
		final TcpClient tcpClient = TcpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
				.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT)).addHandlerLast(new WriteTimeoutHandler(TIMEOUT)))
				.wiretap(true); // Helps logging, each request and response will be logged in full detail.

		@SuppressWarnings("deprecation")
		final ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(
				HttpClient.from(tcpClient).keepAlive(true));

		return webClientBuilder
				.uriBuilderFactory(defaultUriBuilderFactory)
				.baseUrl(this.githubBaseUrl)
				.codecs(clientCodecConfigure -> clientCodecConfigure.defaultCodecs().enableLoggingRequestDetails(true))
				.clientConnector(clientHttpConnector).codecs(clientConfigurer -> clientConfigurer.defaultCodecs().maxInMemorySize(TRANFER_SIZE * 1024 * 1024))
				.defaultHeader(HttpHeaders.CONTENT_TYPE, GITHUB_V3_MIME_TYPE)
				.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .filter(ExchangeFilterFunctions
                        .basicAuthentication(/*appProperties.getGithub().getUsername(),
                                appProperties.getGithub().getToken())*/"username", "password"))
				.filters(exchangeFilterFunctions -> {
					exchangeFilterFunctions.add(logRequest());
//					exchangeFilterFunctions.add(logResponse());
				})
				.build();
	}
	
	private static ExchangeFilterFunction logRequest() {
		return (clientRequest, next) -> {
			if (requestLog.isDebugEnabled()) {
				requestLog.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
				clientRequest.headers()
						.forEach((name, values) -> values.forEach(value -> requestLog.debug("{}={}", name, value)));
			}
			return next.exchange(clientRequest);
		};
	}
}
