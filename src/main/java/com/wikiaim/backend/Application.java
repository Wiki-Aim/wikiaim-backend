package com.wikiaim.backend;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
    info = @Info(
            title = "WikiAim API",
            version = "0.1",
            description = "API du wiki collaboratif WikiAim"
    )
)
@SecurityScheme(
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class Application {

    static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
