package org.littil.api.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class EmailVerficationResource {

	@NotEmpty
	@Email
	private String emailAddress;
	
}
