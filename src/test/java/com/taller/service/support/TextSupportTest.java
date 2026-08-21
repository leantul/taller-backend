package com.taller.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextSupportTest {

    @Test
    void preservesOptionalNormalizationSemantics() {
        assertThat(TextSupport.normalizeOptional(null)).isNull();
        assertThat(TextSupport.normalizeOptional("   ")).isEmpty();
        assertThat(TextSupport.normalizeOptional("  value  ")).isEqualTo("value");
    }

    @Test
    void preservesRequiredFallbackSemantics() {
        assertThat(TextSupport.normalizeRequired(null, "fallback")).isEqualTo("fallback");
        assertThat(TextSupport.normalizeRequired("   ", "fallback")).isEqualTo("fallback");
        assertThat(TextSupport.normalizeRequired("  value  ", "fallback")).isEqualTo("value");
        assertThat(TextSupport.normalizeRequired(null, null)).isNull();
    }
}
