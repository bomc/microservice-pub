package de.bomc.poc.publish.infrastructure.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.validation.ObjectError;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.JsonNode;

import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@TestMethodOrder(OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class RequestValidationProcessorTest {

	private static final String LOG_PREFIX = RequestValidationProcessorTest.class + "#";
	
	private static final String PUBLISH_META_DATA_KEY = "42";
	private static final String PUBLISH_META_DATA_VALUE_PASS = "bomc";
	private static final String PUBLISH_META_DATA_VALUE_FAIL = "bomc_";
	
	private static final PublishMetaData PUBLISH_META_DATA_PASS = new PublishMetaData(PUBLISH_META_DATA_KEY, PUBLISH_META_DATA_VALUE_PASS);
	private static final PublishMetaData PUBLISH_META_DATA_FAIL = new PublishMetaData(PUBLISH_META_DATA_KEY, PUBLISH_META_DATA_VALUE_FAIL);
	
	private static final Function<PublishMetaData, Mono<PublishMetaData>> MOCK_REQUEST_PROCESSOR_PASS = requestBody -> Mono.just(PUBLISH_META_DATA_PASS);
	private static final Function<PublishMetaData, Mono<PublishMetaData>> MOCK_REQUEST_PROCESSOR_FAIL = requestBody -> Mono.just(PUBLISH_META_DATA_FAIL);
	private static final Function<PublishMetaData, Mono<PublishMetaData>> MOCK_REQUEST_PROCESSOR_EMPTY = requestBody -> Mono.empty();
	
	@InjectMocks
	private RequestValidationProcessor sut;
	
	@Mock
	private RequestValidationProcessorHelper requestValidationProcessorHelper;
	
	@Mock
	private JsonNode jsonNode;

	@Test
	@Order(10)
	@DisplayName("Validates a PublishMetaData instance without validation errors.")
	public void test010_requestValidation_pass () {
		log.debug(LOG_PREFIX + "test010_requestValidation_pass");
		
		// GIVEN
		final ServerRequest serverRequest = MockServerRequest.builder().method(HttpMethod.POST).body(Mono.just(PUBLISH_META_DATA_PASS));

	    final List<ObjectError> objectErrorList = Collections.emptyList();
	            
		when(this.requestValidationProcessorHelper.hasValidationError(PUBLISH_META_DATA_PASS, PublishMetaData.class)).thenReturn(objectErrorList);
		
		// WHEN
		final Mono<ServerResponse> serverResponse = this.sut.validateRequest(MOCK_REQUEST_PROCESSOR_PASS, serverRequest, PublishMetaData.class);
		
		// THEN
		StepVerifier.create(serverResponse)
			.expectNextMatches(response -> {
				assertThat(response).isNotNull();
				assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
				return true;
			})
			.expectComplete()
			.verify();
		verify(this.requestValidationProcessorHelper, times(1)).hasValidationError(any(), any());
		verify(this.requestValidationProcessorHelper, times(0)).onValidationErrors(anyList(), any(), any());
	}
	
	@Test
	@Order(20)
	@DisplayName("Validates a PublishMetaData instance with a validation error.")
	public void test020_requestValidation_fail () {
		log.debug(LOG_PREFIX + "test020_requestValidation_fail");
		
		// GIVEN
		final ServerRequest serverRequest = MockServerRequest.builder().method(HttpMethod.POST).body(Mono.just(PUBLISH_META_DATA_FAIL));

	    final List<ObjectError> objectErrorList = Arrays.asList(
	            new ObjectError("notAFieldObject", "notAFieldError.getName()")
	    );
	            
		when(this.requestValidationProcessorHelper.hasValidationError(PUBLISH_META_DATA_FAIL, PublishMetaData.class)).thenReturn(objectErrorList);
		when(this.requestValidationProcessorHelper.onValidationErrors(objectErrorList, PUBLISH_META_DATA_FAIL, serverRequest)).thenReturn(Mono.just(jsonNode));
		
		// WHEN
		final Mono<ServerResponse> serverResponse = this.sut.validateRequest(MOCK_REQUEST_PROCESSOR_FAIL, serverRequest, PublishMetaData.class);
		
		// THEN
		StepVerifier.create(serverResponse)
			.expectNextMatches(response -> {
				assertThat(response).isNotNull();
				assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				return true;
			})
			.expectComplete()
			.verify();
		verify(this.requestValidationProcessorHelper, times(1)).hasValidationError(any(), any());
		verify(this.requestValidationProcessorHelper, times(1)).onValidationErrors(anyList(), any(), any());
	}
	
	@Test
	@Order(30)
	@DisplayName("Handle a PublishMetaData instance with a empty Mono from service invocation.")
	public void test030_requestValidationEmpty_fail () {
		log.debug(LOG_PREFIX + "test030_requestValidationEmpty_fail");
		
		// GIVEN
		final ServerRequest serverRequest = MockServerRequest.builder().method(HttpMethod.POST).body(Mono.just(PUBLISH_META_DATA_FAIL));

		final List<ObjectError> objectErrorList = Collections.emptyList();
	            
		when(this.requestValidationProcessorHelper.hasValidationError(PUBLISH_META_DATA_FAIL, PublishMetaData.class)).thenReturn(objectErrorList);
		
		// WHEN
		final Mono<ServerResponse> serverResponse = this.sut.validateRequest(MOCK_REQUEST_PROCESSOR_EMPTY, serverRequest, PublishMetaData.class);
		
		// THEN
		StepVerifier.create(serverResponse)
			.expectNextMatches(response -> {
				assertThat(response).isNotNull();
				assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
				return true;
			})
			.expectComplete()
			.verify();
		verify(this.requestValidationProcessorHelper, times(1)).hasValidationError(any(), any());
		verify(this.requestValidationProcessorHelper, times(0)).onValidationErrors(anyList(), any(), any());
	}
}
