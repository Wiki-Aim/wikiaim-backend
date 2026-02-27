package com.wikiaim.backend;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.inject.Singleton;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.wikiaim.backend")
public class ArchitectureTest {

    // LOI N°1 : Séparation des pouvoirs (Controller -> Service)
    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories =
        noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .because("Les Controllers ne doivent pas taper directement en base. Ils doivent déléguer aux Services.");

    // LOI N°2 : La Frontière (Pas d'entités exposées)
    @ArchTest
    static final ArchRule entities_should_not_leak_to_controllers =
        noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
            .because("Les Entités JPA sont privées à la base de données. Les Controllers doivent utiliser des DTOs (Records).");

    // LOI N°3 : Le standard d'injection (Services)
    @ArchTest
    static final ArchRule services_must_be_singletons =
        classes()
            .that().haveSimpleNameEndingWith("Service")
            .should().beAnnotatedWith(Singleton.class)
            .because("Dans Micronaut, un Service doit toujours être déclaré comme un @Singleton.");

    // LOI N°4 : Le standard d'accès aux données (Repositories)
    @ArchTest
    static final ArchRule repositories_must_be_interfaces =
        classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().beInterfaces()
            .andShould().beAnnotatedWith("io.micronaut.data.annotation.Repository")
            .because("Les Repositories doivent être des interfaces Micronaut Data gérées par le framework.");
    // LOI N°5 : Convention de nommage stricte
    @ArchTest
    static final ArchRule controllers_must_be_annotated =
        classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith(io.micronaut.http.annotation.Controller.class)
            .because("Toute classe se terminant par 'Controller' doit être reconnue par Micronaut comme un endpoint.");
}