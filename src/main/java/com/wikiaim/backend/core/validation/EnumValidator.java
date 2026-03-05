package com.wikiaim.backend.core.validation;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Introspected
public class EnumValidator implements ConstraintValidator<EnumValid, CharSequence> {

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(
            @Nullable CharSequence value,
            @NonNull AnnotationValue<EnumValid> annotationMetadata,
            @NonNull ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        return annotationMetadata.classValue("enumClass")
                .filter(Class::isEnum)
                .map(c -> {
                    Set<String> validNames = Arrays.stream(((Class<? extends Enum<?>>) c).getEnumConstants())
                            .map(Enum::name)
                            .collect(Collectors.toSet());
                    return validNames.contains(value.toString());
                })
                .orElse(false);
    }
}
