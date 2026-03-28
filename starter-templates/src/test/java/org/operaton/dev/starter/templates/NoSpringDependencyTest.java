package org.operaton.dev.starter.templates;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces zero Spring dependency constraint in starter-templates.
 * Any class importing from org.springframework.* fails the build.
 */
class NoSpringDependencyTest {

    @Test
    void starter_templates_must_not_depend_on_spring() {
        JavaClasses classes = new ClassFileImporter()
                .importPackages("org.operaton.dev.starter.templates");

        ArchRule rule = noClasses()
                .should()
                .dependOnClassesThat()
                .resideInAPackage("org.springframework..")
                .because("starter-templates must be a pure-Java library with zero Spring dependencies");

        rule.check(classes);
    }
}
