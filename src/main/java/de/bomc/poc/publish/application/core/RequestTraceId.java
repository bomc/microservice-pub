package de.bomc.poc.publish.application.core;

import org.springframework.stereotype.Component;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestTraceId {

	private static final String LOG_PREFIX = RequestTraceId.class.getName() + "#";
	
	private final Tracer tracer;
	
	public String traceId() {
		final String traceId = tracer.currentSpan().context().traceIdString();
		
		log.debug(LOG_PREFIX + "traceId [traceId=" + traceId + "]");
		
		return this.tracer.currentSpan().context().traceIdString();
	}
}
