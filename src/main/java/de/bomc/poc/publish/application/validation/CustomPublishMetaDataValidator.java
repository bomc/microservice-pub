package de.bomc.poc.publish.application.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import de.bomc.poc.publish.domain.model.PublishMetaData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomPublishMetaDataValidator implements Validator {

	private static final String LOG_PREFIX = CustomPublishMetaDataValidator.class.getName() + "#";
	
    private static final int MINIMUM_CODE_LENGTH = 2;
    private static final int MAXIMUM_CODE_LENGTH = 4;
    
    @Override
    public boolean supports(Class<?> clazz) {
        return PublishMetaData.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
    	log.debug(LOG_PREFIX + "validate [target=" + target + ", errors=" + errors + "]");
    	
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required");
        
        final PublishMetaData request = (PublishMetaData) target;
        
        if(request.getName() != null && request.getName().trim().length() < MINIMUM_CODE_LENGTH) {
        	
            errors.rejectValue("name", "field.min.length", new Object[] { Integer.valueOf(MINIMUM_CODE_LENGTH) }, "The name must be minimun [" + MINIMUM_CODE_LENGTH + "] characters in length.");
            
        } else if(request.getName() != null && request.getName().trim().length() > MAXIMUM_CODE_LENGTH) {
        	
            errors.rejectValue("name", "field.max.length", new Object[] { Integer.valueOf(MAXIMUM_CODE_LENGTH) }, "The name must be maximum [" + MAXIMUM_CODE_LENGTH + "] characters in length.");
        }
    }
}