package com.wikiaim.backend.core.validation;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class EnumValidatorTest {

    private EnumValidator validator;
    private ConstraintValidatorContext context;

    enum TestStatus { OPEN, CLOSED }

    @BeforeEach
    void setUp() {
        validator = new EnumValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void shouldReturnTrueForValidEnumValue() {
        AnnotationValue<EnumValid> annotation = AnnotationValue.builder(EnumValid.class)
                .member("enumClass", TestStatus.class)
                .build();

        assertTrue(validator.isValid("OPEN", annotation, context));
        assertTrue(validator.isValid("CLOSED", annotation, context));
    }

    @Test
    void shouldReturnFalseForInvalidEnumValue() {
        AnnotationValue<EnumValid> annotation = AnnotationValue.builder(EnumValid.class)
                .member("enumClass", TestStatus.class)
                .build();

        assertFalse(validator.isValid("INVALID", annotation, context));
        assertFalse(validator.isValid("open", annotation, context));
    }

    @Test
    void shouldReturnTrueForNull() {
        AnnotationValue<EnumValid> annotation = AnnotationValue.builder(EnumValid.class)
                .member("enumClass", TestStatus.class)
                .build();

        assertTrue(validator.isValid(null, annotation, context));
    }
}
