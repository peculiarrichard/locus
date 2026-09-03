package com.locus.auth.web.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Found during Phase 12's security review: a client-supplied timezone was never validated at the
// boundary, so garbage would reach UserProfileUpdated and crash Analytics/Notification Services'
// ZoneId.of(...) calls downstream, not here — enforcing it at the point of entry stops the bad
// value from ever propagating.
// Broad target set, matching Jakarta's own built-in constraints — records apply an annotation
// placed on a component to the generated field, accessor, and constructor parameter alike, and
// Bean Validation needs to see it on whichever of those it actually introspects.
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTimezoneValidator.class)
public @interface ValidTimezone {

  String message() default "must be a valid IANA timezone id";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
