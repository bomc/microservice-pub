package de.bomc.poc.publish.domain.model;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublishMetaData {

	@NotEmpty
	@DecimalMax(value = "999")
	private String id;
	@NotEmpty
	@Size(min = 2, max = 4, message = "'name' must be between 2 and 4 characters")
	private String name;
}
