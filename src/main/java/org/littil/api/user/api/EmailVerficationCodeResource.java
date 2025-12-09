package org.littil.api.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class EmailVerficationCodeResource {

	@NotEmpty
	@Email
	private String emailAddress;

}
