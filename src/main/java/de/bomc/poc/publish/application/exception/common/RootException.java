package de.bomc.poc.publish.application.exception.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public abstract class RootException extends RuntimeException {

	private static final long serialVersionUID = -2045645810397567890L;

	@Getter
	private final HttpStatus status;

	public RootException(final HttpStatus status, final String message) {
		super(message);
		this.status = status;
	}

	public RootException(final HttpStatus status, final String message, final Throwable cause) {
		super(message, cause);
		
		this.status = status;
	}

}
