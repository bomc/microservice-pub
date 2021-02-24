package de.bomc.poc.publish.application.validation;

public enum ErrorAttributeEnum {
	TIMESTAMP("timestamp"),
	PATH("path"),
	STATUS("status"),
	ERROR("error"),
	MESSAGE("message"),
	TRACE_ID("traceId"),
	REQUEST_ID("requestId");
	
	private final String value;

	ErrorAttributeEnum(final String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
	/**
	 * Return a string representation of this error attribute.
	 */
	@Override
	public String toString() {
		return this.value + " " + name();
	}
}
