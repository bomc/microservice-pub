package de.bomc.poc.publish.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import de.bomc.poc.publish.domain.model.GithubRepo;
import de.bomc.poc.publish.infrastructure.handler.GithubRequestHandler;
import de.bomc.poc.publish.infrastructure.webclient.WebClientPublisher;
import reactor.core.publisher.Flux;


//@ExtendWith(MockitoExtension.class)
//@ContextConfiguration(classes = {GithubRequestHandler.class, GithubRouterConfig.class/*, WebFluxTestSecurityConfig.class*/})
//@WebFluxTest
//@TestMethodOrder(OrderAnnotation.class)
public class GithubRouterConfigTest {

//	@Value("${server.servlet.context-path}")
//	private String contextPath;
//	
//	@Autowired
//	private ApplicationContext applicationContext;
//	
//	@MockBean
//	private WebClientPublisher webClientPublisher;
//	@MockBean
//	private GithubRequestHandler githubRequestHandler;
//	@MockBean
//	private WebClient githubWebClient;
//	
//	private WebTestClient webTestClient;
//	
//	@BeforeEach
//	public void setup() {
//		this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
//		this.webTestClient = this.webTestClient.mutate().responseTimeout(Duration.ofMillis(36000)).build();
//	}
//	
//	@Test
//	@Order(10)
//	@DisplayName("Tests the github router method GET list repositories.")
//	public void test010_listRepos_pass() {
//		
//		// GIVEN
//		final String FULL_NAME = "bomc"; 
//		final GithubRepo gitHubRepoResponse = new GithubRepo();
//		gitHubRepoResponse.setFullName("bomc");
//		
//		// WHEN
//		when(this.webClientPublisher.listRepositories(any(), any(), any())).thenReturn(Flux.just(gitHubRepoResponse));
//		when(this.githubRequestHandler.listGithubRepositories(any())).thenReturn(ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(gitHubRepoResponse)));
//		
//		this.webTestClient
//			.get()
//			.uri(this.contextPath + "/github/user/repo?sort=stars&direction=asc&name=bomc")
//			.accept(MediaType.APPLICATION_JSON)
//			.exchange()
//			// THEN
//			.expectStatus().isOk()
//			.expectBody(GithubRepo.class)
//			.value(returnValue -> { 
//				assertThat(returnValue).isNotNull();
//				assertThat(returnValue.getFullName()).isEqualTo(FULL_NAME);
//			});
//	}
	
}
