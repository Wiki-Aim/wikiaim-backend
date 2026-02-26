package com.wikiaim.backend;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;

@OpenAPIDefinition(
    info = @Info(
            title = "wikiaim-backend",
            version = "0.0"
    )
)
public class Application {

    static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
