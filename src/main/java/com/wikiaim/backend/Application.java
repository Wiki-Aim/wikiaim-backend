package com.wikiaim.backend;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;

@OpenAPIDefinition(
    info = @Info(
            title = "WikiAim API",
            version = "0.1",
            description = "API du wiki collaboratif WikiAim"
    )
)
public class Application {

    static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
