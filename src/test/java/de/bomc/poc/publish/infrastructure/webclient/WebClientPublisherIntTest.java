package de.bomc.poc.publish.infrastructure.webclient;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import de.bomc.poc.publish.config.WebClientConfig;
import de.bomc.poc.publish.domain.model.PublishMetaData;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.RecordedRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * This class shows how to implement integration tests with the okhttp3 server.
 * 
 * Sample test data is loaded via {@link AbstractWebClientTest}.
 *
 */
@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodName.class)
@Import({ WebClientConfig.class, WebClientPublisher.class, WebClientAutoConfiguration.class })
public class WebClientPublisherIntTest extends AbstractWebClientTest {

	private final String LOG_PREFIX = WebClientPublisherIntTest.class.getName() + "#";

	// File that contains a well formed json, the id with value 42 indicates a failure test.
	private static final String SIMPLE_PAYLOAD_JSON_FAIL_FILE = "simple_payload_fail.json";
	
	private static final String RESOURCE_PUBLISH_META_DATA_ID_PASS = "1";
	private static final String RESOURCE_PUBLISH_META_DATA_ID_FAIL = "42";
	private static final String RESOURCE_PUBLISH_META_DATA_NAME = "bomc";
	private static final String EXPECTED_PASS_URI = "/api/metadata/1";
	private static final String EXPECTED_FAIL_URI = "/api/metadata/42";
	private static final String EXPECTED_URI = "/api/metadata/annotation-validation/";
	
	// Class under test.
	@Autowired
	private WebClientPublisher sut;
	
	@Test
	@Description("test010_callGETWebClientRequest_pass - a simple webClient GET request.")
	public void test010_callGETWebClientRequest_pass() throws InterruptedException {
		log.debug(LOG_PREFIX + "test010_callGETWebClientRequest_pass - Current mock server url: " + mockWebServer.url("/").toString());
	
		// GIVEN
		
		// WHEN
		final Mono<PublishMetaData> result = this.sut.getPublishMetaDataById(RESOURCE_PUBLISH_META_DATA_ID_PASS);
		//
		// THEN
		//
		// Asserting response
		//
	    StepVerifier.create(result)
	    	.assertNext(resp -> {
	    		assertThat(resp.getId(), equalTo(RESOURCE_PUBLISH_META_DATA_ID_PASS));
	    		assertThat(resp.getName(), equalTo(RESOURCE_PUBLISH_META_DATA_NAME));
	    	})
	    	.verifyComplete();
	    
	    // ___________________________________________
	    // NOTE: mockWebServer.takeRequest() must be invoked 
	    final RecordedRequest recordedRequest = mockWebServer.takeRequest();
	    
	    assertThat(mockWebServer.getRequestCount(), equalTo(1));
	    assertThat(HttpMethod.GET.name(), equalTo(recordedRequest.getMethod()));
	    assertThat(EXPECTED_PASS_URI, equalTo(recordedRequest.getPath()));
	}
	
	@Test
	@Description("test020_callGETWebClientRequest_fail - a simple webClient GET request that fails.")
	public void test020_callGETWebClientRequest_fail() throws InterruptedException {
		log.debug(LOG_PREFIX + "test020_callGETWebClientRequest_fail - Current mock server url: " + mockWebServer.url("/").toString());
		
		// GIVEN
		
		// WHEN
		final Mono<PublishMetaData> result = this.sut.getPublishMetaDataById(RESOURCE_PUBLISH_META_DATA_ID_FAIL);
		
		// THEN
		//
		// Asserting response
		//
	    StepVerifier.create(result).expectErrorMessage("webClient.getPublishMetaDataById failed with 4xx client error.").verify();
	    
	    // ___________________________________________
	    // NOTE: mockWebServer.takeRequest() must be invoked 
	    final RecordedRequest recordedRequest = mockWebServer.takeRequest();
	    
	    assertThat(HttpMethod.GET.name(), equalTo(recordedRequest.getMethod()));
	    assertThat(EXPECTED_FAIL_URI, equalTo(recordedRequest.getPath()));
	}
	
	@Test
	@Description("test030_callPOSTWebClientRequest_pass - a simple webClient POST request with asserting request and response.")
	public void test030_callPOSTWebClientRequest_pass() throws InterruptedException {
		log.debug(LOG_PREFIX + "test030_callPOSTWebClientRequest_pass - Current mock server url: " + mockWebServer.url("/").toString());
		
		// GIVEN
		//
		// Configure response
		final PublishMetaData publishMetaData = new PublishMetaData(RESOURCE_PUBLISH_META_DATA_ID_PASS, RESOURCE_PUBLISH_META_DATA_NAME);
		
		// WHEN
		final Mono<PublishMetaData> result = this.sut.createPublishMetaData(publishMetaData);
		
		// THEN
		//
		// Asserting response
		//
	    StepVerifier.create(result)
	    	.assertNext(resp -> {
	    		assertThat(resp.getId(), equalTo(RESOURCE_PUBLISH_META_DATA_ID_PASS));
	    		assertThat(resp.getName(), equalTo(RESOURCE_PUBLISH_META_DATA_NAME));
	    	})
	    	.verifyComplete();

	    //
	    // Asserting request
	    //
	    final RecordedRequest recordedRequest = mockWebServer.takeRequest();
	    
	    assertThat(HttpMethod.POST.name(), equalTo(recordedRequest.getMethod()));
	    assertThat(EXPECTED_URI, equalTo(recordedRequest.getPath()));
	    
	    // Use method provided by MockWebServer to assert the request header.
	    recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE).equals(MediaType.APPLICATION_JSON_VALUE);
	    
	    final DocumentContext context = JsonPath.parse(recordedRequest.getBody().inputStream());
	    // Use JsonPath library to assert the request body.
	    assertThat(context, isJson(allOf(
	            withJsonPath("$.id", equalTo(RESOURCE_PUBLISH_META_DATA_ID_PASS)),
	            withJsonPath("$.name", equalTo(RESOURCE_PUBLISH_META_DATA_NAME)))));
	}
	
	@Test
	@Description("test040_callPOSTWebClientRequest_fail - a simple webClient POST request with asserting request and response.")
	public void test040_callPOSTWebClientRequest_fail() throws InterruptedException, IOException {
		log.debug(LOG_PREFIX + "test040_callPOSTWebClientRequest_fail - Current mock server url: " + mockWebServer.url("/").toString());

		// GIVEN
		// ___________________________________________
		// Overwrite default response payload to create a fail test case.
		//
		// Read mock response payload from json file.
		final String responsePayload = IOUtils.toString(
				new ClassPathResource(SIMPLE_PAYLOAD_JSON_FAIL_FILE).getInputStream(), Charset.defaultCharset());

		final MockDispatcher mockDispatcher = new MockDispatcher(responsePayload, REQUEST_PAYLOAD_JSON_ATTRIBUTE_ID);
		mockWebServer.setDispatcher(mockDispatcher);
		
		//
		// Configure response
		//
		final PublishMetaData publishMetaData = new PublishMetaData(RESOURCE_PUBLISH_META_DATA_ID_FAIL, RESOURCE_PUBLISH_META_DATA_NAME);
		
		// WHEN
		final Mono<PublishMetaData> respMono = this.sut.createPublishMetaData(publishMetaData);
		
		// THEN
		//
		// Asserting response
		//
	    StepVerifier.create(respMono)
	    	.assertNext(resp -> {
	    		assertThat(resp.getId(), equalTo(RESOURCE_PUBLISH_META_DATA_ID_FAIL));
	    		assertThat(resp.getName(), equalTo(RESOURCE_PUBLISH_META_DATA_NAME));
	    	})
	    	.verifyComplete();

	    //
	    // Asserting request
	    //
	    final RecordedRequest recordedRequest = mockWebServer.takeRequest();
	    
	    assertThat(HttpMethod.POST.name(), equalTo(recordedRequest.getMethod()));
	    assertThat(EXPECTED_URI, equalTo(recordedRequest.getPath()));
	    
	    // Use method provided by MockWebServer to assert the request header.
	    recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE).equals(MediaType.APPLICATION_JSON_VALUE);
	    
	    final DocumentContext context = JsonPath.parse(recordedRequest.getBody().inputStream());
	    // Use JsonPath library to assert the request body.
	    assertThat(context, isJson(allOf(
	            withJsonPath("$.id", equalTo(RESOURCE_PUBLISH_META_DATA_ID_FAIL)),
	            withJsonPath("$.name", equalTo(RESOURCE_PUBLISH_META_DATA_NAME)))));
	}
}
