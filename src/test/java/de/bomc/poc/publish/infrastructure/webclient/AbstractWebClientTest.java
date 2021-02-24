package de.bomc.poc.publish.infrastructure.webclient;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import okhttp3.mockwebserver.MockWebServer;

@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class AbstractWebClientTest {

	static final String LOG_PREFIX = AbstractWebClientTest.class.getName() + "#";

	static final String SIMPLE_PAYLOAD_JSON_PASS_FILE = "simple_payload_pass.json";

	static final String REQUEST_PAYLOAD_JSON_ATTRIBUTE_ID = "id";
	
	static final int PORT = 8080;
	static MockWebServer mockWebServer;

	@BeforeAll
	static void before() throws IOException {
		try {
			// Read mock response payload from json file.
			final String responsePayload = IOUtils.toString(
					new ClassPathResource(SIMPLE_PAYLOAD_JSON_PASS_FILE).getInputStream(), Charset.defaultCharset());

			// NOTE: This class describes the mocked rqequest and response handling for the endpoints.
			final MockDispatcher mockDispatcher = new MockDispatcher(responsePayload, REQUEST_PAYLOAD_JSON_ATTRIBUTE_ID);

			mockWebServer = new MockWebServer();
			mockWebServer.setDispatcher(mockDispatcher);
			// _______________________________________
			// Set user defined port.
			mockWebServer.start(PORT);
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

	@AfterAll
	static void afterAll() throws IOException {
		try {
			mockWebServer.close();
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}

}