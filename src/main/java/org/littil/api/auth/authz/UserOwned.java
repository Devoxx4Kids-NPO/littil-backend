package org.littil.api.auth.authz;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import javax.ws.rs.container.ContainerRequestFilter;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@InterceptorBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface UserOwned {
    @Nonbinding Class<? extends ContainerRequestFilter> filter();
}
