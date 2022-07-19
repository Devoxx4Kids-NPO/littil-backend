package org.littil.api.auth.authz;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@InterceptorBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface UserOwned { // todo rename to something more generic like SecuredAPI?

    SecurityType type() default SecurityType.DEFAULT; // todo determine correct default

    enum SecurityType {
        // School and teacher are now in default, might need to split
        DEFAULT
    }
}
