package org.littil.api.auth.authz;

import org.littil.api.auth.service.AuthorizationType;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SchoolSecured {

    AuthorizationType type() default AuthorizationType.SCHOOL;
}
