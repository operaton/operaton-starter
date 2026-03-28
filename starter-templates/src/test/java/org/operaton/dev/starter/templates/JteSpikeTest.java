package org.operaton.dev.starter.templates;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story 2.1: Validates that JTE templates precompile correctly and produce expected output.
 *
 * <p>Also validates the performance guarantee: 100 template invocations under 500ms.
 */
class JteSpikeTest {

    private static final Path JTE_ROOT = Path.of("src/main/jte");

    @Test
    void spike_template_renders_expected_output() {
        CodeResolver resolver = new DirectoryCodeResolver(JTE_ROOT);
        TemplateEngine jte = TemplateEngine.create(resolver, ContentType.Plain);

        StringOutput output = new StringOutput();
        jte.render("spike/hello.jte", "Operaton", output);

        String result = output.toString();
        assertTrue(result.contains("Hello from JTE, Operaton!"),
                "Expected greeting, got: " + result);
        assertTrue(result.contains("precompiled at build time"),
                "Expected precompiled note, got: " + result);
    }

    @Test
    void spike_100_invocations_complete_under_500ms() {
        CodeResolver resolver = new DirectoryCodeResolver(JTE_ROOT);
        TemplateEngine jte = TemplateEngine.create(resolver, ContentType.Plain);

        long startMs = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            StringOutput output = new StringOutput();
            jte.render("spike/hello.jte", "Operaton-" + i, output);
            assertFalse(output.toString().isBlank());
        }
        long elapsedMs = System.currentTimeMillis() - startMs;

        assertTrue(elapsedMs < 500,
                "100 template invocations took " + elapsedMs + "ms, expected < 500ms");
    }
}
