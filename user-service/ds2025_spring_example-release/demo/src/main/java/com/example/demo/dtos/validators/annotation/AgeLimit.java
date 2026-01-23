package com.example.demo.dtos.validators.annotation;

import com.example.demo.dtos.validators.AgeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
        ElementType.TYPE_USE,
        ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = AgeValidator.class)
public @interface AgeLimit {
    int value() default 18;                        // min age
    int max() default 100;                        // max age
    String message() default "Selecteaza o varsta intre {value} si {max} de ani";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
