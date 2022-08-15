package org.littil.api.auth.authz;

import org.littil.api.auth.service.AuthorizationType;

import javax.interceptor.InterceptorBinding;
import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface UserOwned { // todo rename to something more generic like SecuredAPI?

    AuthorizationType type();
}
