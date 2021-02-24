package de.bomc.poc.publish.application.exception.common;

import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;

public class ApplicationException extends RootException {

	private static final long serialVersionUID = 5443168111704225378L;

	public ApplicationException(final String message) {
		super(I_AM_A_TEAPOT, message);
	}

	public ApplicationException(final String message, final Throwable cause) {
		super(I_AM_A_TEAPOT, message, cause);
	}
}