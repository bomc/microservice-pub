package de.bomc.poc.publish.infrastructure.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class InMemoryPublishMetaDataRepository {

	private static final String LOG_PREFIX = InMemoryPublishMetaDataRepository.class.getName() + "#";
	
	private final Map<String, PublishMetaData> publishMetaDataMap;
	
	public InMemoryPublishMetaDataRepository() {
		this.publishMetaDataMap = new ConcurrentHashMap<String, PublishMetaData>();
	}
	
	public Mono<PublishMetaData> findById(final String id) {
		log.debug(LOG_PREFIX + "findById [id=" + id + "]");
		
		return Mono.justOrEmpty(this.publishMetaDataMap.get(id));
	}

	public Mono<PublishMetaData> create(final PublishMetaData publishMetaData) {
		log.debug(LOG_PREFIX + "create [publishMetaData=" + publishMetaData + "]");
		
		this.publishMetaDataMap.put(publishMetaData.getId(), publishMetaData);

		return Mono.just(publishMetaData);
	}

	public Mono<Map<String, PublishMetaData>> findAll() {
		log.debug(LOG_PREFIX + "findAll");
		
		return Mono.just(this.publishMetaDataMap);
	}
	
	public Mono<PublishMetaData> delete(final String key) {
		log.debug(LOG_PREFIX + "delete [key=" + key + "]");
		
		final PublishMetaData deletedPublishMetaData = this.publishMetaDataMap.remove(key);
		
		System.out.println("------------------>" + deletedPublishMetaData);
		publishMetaDataMap.forEach((k, v) -> {
	        System.out.println("-----Key: " + k + ", -----Value: " + v);
	    });
	    
		if(deletedPublishMetaData != null) {
			return Mono.just(deletedPublishMetaData);	
		} else {
			return Mono.empty();
		}
	}

	public Mono<PublishMetaData> update(final PublishMetaData publishMetaData) {
		log.debug(LOG_PREFIX + "update [publishMetaData=" + publishMetaData + "]");

		if(this.publishMetaDataMap.containsKey(publishMetaData.getId())) {
			final PublishMetaData retPublishMetaData = this.publishMetaDataMap.put(publishMetaData.getId(), publishMetaData);
		
			return Mono.just(retPublishMetaData);
		} else {
			//
			// No object available to the given id.
			return Mono.empty();
		}
	}
}
