package org.littil.api.user;

import lombok.NoArgsConstructor;
import org.littil.api.exception.ServiceException;

public class UserAlreadyExistsException extends ServiceException {

    public UserAlreadyExistsException() {
        super();
    }
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
