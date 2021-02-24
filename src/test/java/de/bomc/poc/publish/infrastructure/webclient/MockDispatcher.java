package de.bomc.poc.publish.infrastructure.webclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

/**
 * This class defines responses for different requests with MockWebServer. Used in class {@link WebClientPublisherIntTest}.
 *
 */
@Slf4j
public class MockDispatcher extends Dispatcher {

	public static final String LOG_PREFIX = MockDispatcher.class.getName() + "#";
	
	// Describes GET path with path variable 1 as id.
	public static final String PATH_GET_PUBLISH_META_DATA_200 = "/api/metadata/1";
	public static final String PATH_GET_PUBLISH_META_DATA_404 = "/api/metadata/42";
	public static final String PATH_POST_PUBLISH_META_DATA = "/api/metadata/annotation-validation/";

	private String expectedResponsePayload;
	private String jsonPayloadAttributeId;
	
	public MockDispatcher(final String expectedResponsePayload, final String jsonPayloadAttributeId) {
		this.expectedResponsePayload = expectedResponsePayload;
		this.jsonPayloadAttributeId = jsonPayloadAttributeId;
	}
 
	/**
	 * Returns a response to satisfy {@code request}. This method may block (for
	 * instance, to wait on a CountdownLatch).
	 */
	@Override
	public MockResponse dispatch(final RecordedRequest recordedRequest) throws InterruptedException {

		if (recordedRequest.getMethod().equals(HttpMethod.GET.name()) && recordedRequest.getPath().equals(PATH_GET_PUBLISH_META_DATA_200)) {
			return new MockResponse().setBody(expectedResponsePayload)
					.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.setResponseCode(HttpStatus.OK.value());

		} else if (recordedRequest.getMethod().equals(HttpMethod.GET.name()) && recordedRequest.getPath().equals(PATH_GET_PUBLISH_META_DATA_404)) {
			return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.setResponseCode(HttpStatus.NOT_FOUND.value());

		} else if (recordedRequest.getMethod().equals(HttpMethod.POST.name()) && recordedRequest.getPath().equals(PATH_POST_PUBLISH_META_DATA)) {
			// Read request payload as json, to execute different test cases.
			// The indicator is the attribute 'id'.
			//
			// Buffer the request body.
			final Buffer buffer = recordedRequest.getBody().buffer();
			final OutputStream outputStream = new ByteArrayOutputStream();
			
			try {
				buffer.copyTo(outputStream);
				outputStream.flush();
				
				final JSONObject jsonObject = new JSONObject(outputStream.toString());
				
				if(jsonObject.get(jsonPayloadAttributeId).equals("42")) {
					return new MockResponse().setBody(expectedResponsePayload)
							.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
							.setResponseCode(HttpStatus.CREATED.value());
					
				} 
			} catch (IOException | JSONException ex) {
				log.error(LOG_PREFIX + "dispatch - reading attribute from request - failed.", ex);
			}

			return new MockResponse().setBody(expectedResponsePayload)
					.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.setResponseCode(HttpStatus.CREATED.value());

		} else {
			return new MockResponse().setResponseCode(404);
		}
	}
}
