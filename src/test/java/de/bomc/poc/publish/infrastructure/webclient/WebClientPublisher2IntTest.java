package de.bomc.poc.publish.infrastructure.webclient;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import de.bomc.poc.publish.application.core.RemoteConsumerService;
import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * This class uses {@link WebTestClient} for integration test.
 * 
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@ActiveProfiles("test")
public class WebClientPublisher2IntTest {

	private static final String LOG_PREFIX = WebClientPublisher2IntTest.class.getName() + "#";
	
	private static final String PUBLISH_META_DATA_ID_VALUE = "42";
	private static final String PUBLISH_META_DATA_ID_ATTRIBUTE = "id";
	private static final String PUBLISH_META_DATA_NAME_VALUE = "bomc";
	private static final String PUBLISH_META_DATA_NAME_ATTRIBUTE = "name";
	
	@Value("${server.servlet.context-path}")
	private String contextPath;
	
	@LocalServerPort
	protected int port;
	
	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private RemoteConsumerService remoteConsumerService;
	
	private static final PublishMetaData PUBLISH_META_DATA = new PublishMetaData(PUBLISH_META_DATA_ID_VALUE, PUBLISH_META_DATA_NAME_VALUE);
	
	@BeforeEach
	public void setUp() {
		this.webTestClient = this.webTestClient.mutate().responseTimeout(Duration.ofMillis(36000)).build();		
	}
	
	@Test
	@Order(10)
	@Description("Test POST method using org.springframework.test.web.reactive.server.WebTestClient.")
	public void test010_createPublishMetaData_pass() {
		log.debug(LOG_PREFIX + "test010_createPublishMetaData_pass");
		
		// GIVEN
		
		// WHEN
		when(this.remoteConsumerService.createPublishMetaData(PUBLISH_META_DATA)).thenReturn(Mono.just(PUBLISH_META_DATA));
		
		// THEN
		this.webTestClient
				.post()
				.uri("http://localhost:" + this.port + this.contextPath + "/metadata/annotation-validation")
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
				.bodyValue(PUBLISH_META_DATA)
				.exchange()            // Is a chained API workflow to verify responses.
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
		        .expectBody()
		        .consumeWith(response -> {
		        	try {
						final JSONObject responseJsonId = new JSONObject(new String(response.getResponseBody()));
						Assertions.assertThat(responseJsonId.getString(PUBLISH_META_DATA_ID_ATTRIBUTE)).isEqualTo(PUBLISH_META_DATA_ID_VALUE);
						
						final JSONObject responseJsonName = new JSONObject(new String(response.getResponseBody()));	
						Assertions.assertThat(responseJsonName.getString(PUBLISH_META_DATA_NAME_ATTRIBUTE)).isEqualTo(PUBLISH_META_DATA_NAME_VALUE);
						
		        	} catch (final JSONException e) {
						e.printStackTrace();
					}
		        });
	}
	
	@Test
	@Order(20)
	@Description("Test GET method using org.springframework.test.web.reactive.server.WebTestClient.")
	public void test020_readFromEndpoint_pass() {
		log.debug(LOG_PREFIX + "test020_readFromEndpoint_pass");
		
		// GIVEN
		
		// WHEN
		when(this.remoteConsumerService.getPublishMetaDataById(PUBLISH_META_DATA_ID_VALUE)).thenReturn(Mono.just(PUBLISH_META_DATA));
		
		// THEN
		this.webTestClient
				.get()
				.uri("http://localhost:" + this.port + this.contextPath + "/metadata/{id}", 42)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(Charset.forName(StandardCharsets.UTF_8.name()))
				.exchange()            // Is a chained API workflow to verify responses.
				.expectStatus().isOk()
				.expectBody()
				.consumeWith(response -> assertThat(response.getResponseBody(), notNullValue()));
	}

}
