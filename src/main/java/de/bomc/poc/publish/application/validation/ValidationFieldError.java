package de.bomc.poc.publish.application.validation;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
@AllArgsConstructor 
public class ValidationFieldError implements Serializable {

	private static final long serialVersionUID = 6207741881177060565L;
	private static final String LOG_PREFIX = ValidationFieldError.class.getName() + "#";
	
	// Name of the field. Null in case of a form level error. 
	private String field;
	// Error code. Typically the I18n message-code.
	private String code;
	// Error message
	private String message;

	/**
	 * Converts a set of ConstraintViolations
	 * to a list of FieldErrors
	 * 
	 * @param constraintViolations
	 */
	public static List<ValidationFieldError> getErrors(final Set<ConstraintViolation<?>> constraintViolations) {
		log.debug(LOG_PREFIX + "getErrors [constraintViolations=" + constraintViolations + "]");
		
		return constraintViolations.stream()
				.map(ValidationFieldError::of).collect(Collectors.toList());	
	}
	

	/**
	 * Converts a ConstraintViolation
	 * to a FieldError
	 */
	private static ValidationFieldError of(final ConstraintViolation<?> constraintViolation) {
		log.debug(LOG_PREFIX + "of [constraintViolation=" + constraintViolation + "]");
		
		// Get the field name by removing the first part of the propertyPath.
		// (The first part would be the service method name)
		final String field = StringUtils.substringAfter(constraintViolation.getPropertyPath().toString(), ".");
		
		return new ValidationFieldError(field, constraintViolation.getMessageTemplate(), constraintViolation.getMessage());		
	}

	public static List<ValidationFieldError> getErrors(final WebExchangeBindException webExchangeBindException) {
		log.debug(LOG_PREFIX + "of [webExchangeBindException=" + webExchangeBindException + "]");
		
		final List<ValidationFieldError> errors = webExchangeBindException.getFieldErrors().stream().map(ValidationFieldError::of).collect(Collectors.toList());
		
		errors.addAll(webExchangeBindException.getGlobalErrors().stream().map(ValidationFieldError::of).collect(Collectors.toSet()));
		
		return errors;
	}

	private static ValidationFieldError of(final FieldError fieldError) {
		log.debug(LOG_PREFIX + "of [fieldError=" + fieldError + "]");
		
		return new ValidationFieldError(fieldError.getObjectName() + "." + fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
	}

	public static ValidationFieldError of(final ObjectError objectError) {
		log.debug(LOG_PREFIX + "of [error=" + objectError + "]");
		
		return new ValidationFieldError(objectError.getObjectName(), objectError.getCode(), objectError.getDefaultMessage());
	}

}