package com.example.demo.dtos.validators;


import com.example.demo.dtos.validators.annotation.AgeLimit;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeValidator implements ConstraintValidator<AgeLimit, Integer> {
    private int min;
    private int max;
    @Override public void initialize(AgeLimit ann) { this.min = ann.value(); this.max = ann.max(); }
    @Override public boolean isValid(Integer age, ConstraintValidatorContext ctx) {
        if (age == null) return true;               // let @NotNull enforce presence
        return age >= min && age <= max;
    }
}
