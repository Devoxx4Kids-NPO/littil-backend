package org.littil.api.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ChangeEmailResource {

	@NotEmpty
	@Email
	private String oldEmailAddress;  // TODO not used yet

	@NotEmpty
	@Email
	private String newEmailAddress;
	
	@NotEmpty
    private String verificationCode;

}
