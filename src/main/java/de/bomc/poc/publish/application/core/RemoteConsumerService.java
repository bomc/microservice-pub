package de.bomc.poc.publish.application.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.bomc.poc.publish.domain.model.PublishMetaData;
import de.bomc.poc.publish.infrastructure.webclient.WebClientPublisher;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RemoteConsumerService {

	private static final String LOG_PREFIX = RemoteConsumerService.class.getName() + "#";
	
	@Value("${bomc.consumer}")
	private String consumerBaseUrl;
	
	private final WebClientPublisher webClientPublisher;
	
	public RemoteConsumerService(final WebClientPublisher webClientPublisher) {
		this.webClientPublisher = webClientPublisher;
	}
	
	public Mono<PublishMetaData> createPublishMetaData(final PublishMetaData publishMetaData) {
		log.debug(LOG_PREFIX + "createRemotePublishMetaData [publishMetaData=" + publishMetaData + "]");
	
		System.out.println("###########################################");
		System.out.println("###########################################");
		System.out.println("###########################################" + this.consumerBaseUrl);
		System.out.println("###########################################");
		System.out.println("###########################################");
		
		final Mono<PublishMetaData> mono = this.webClientPublisher.createPublishMetaData(publishMetaData);
		
		return mono;
	}
	
	public Mono<PublishMetaData> getPublishMetaDataById(final String id) {
		log.debug(LOG_PREFIX + "getPublishMetaDataById [id=" + id + "]");
		
		final Mono<PublishMetaData> mono = this.webClientPublisher.getPublishMetaDataById(id);
		
		return mono;
	}
	
	public Mono<String> listPublishMetaData() {
		log.debug(LOG_PREFIX + "listPublishMetaData");
		
		final Mono<String> mono = this.webClientPublisher.listPublishMetaData();
		
		return mono;
	}
	
	public Mono<PublishMetaData> deletePublishMetaData(final String id) {
		log.debug(LOG_PREFIX + "deletePublishMetaData [id=" + id + "]");
		
		final Mono<PublishMetaData> mono = this.webClientPublisher.deletePublishMetaData(id);
		
		return mono; 
	}
	
	public Mono<PublishMetaData> updatePublishMetaData(final PublishMetaData publishMetaData) {
		log.debug(LOG_PREFIX + "updatePublishMetaData [publishMetaData=" + publishMetaData + "]");
		
		final Mono<PublishMetaData> mono = this.webClientPublisher.updatePublishMetaData(publishMetaData);
		
		return mono;
	}
}
