// port-lint: tests layer.rs
package io.github.kotlinmania.tracingopentelemetry.layer

import kotlin.test.Test
import kotlin.test.assertNotNull

class LayerTest {
    @Test
    fun testCreateLayer() {
        val l =
            layer()
                .withLocation(true)
                .withThreads(true)
                .withLevel(true)
                .withTarget(true)
                .withContextActivation(true)
        assertNotNull(l)
    }

    @Test
    fun testFilteredLayer() {
        val l = layer()
        val filtered = FilteredOpenTelemetryLayer.new(l, "mock-filter")
        assertNotNull(filtered.inner())
        assertNotNull(filtered.filter())
    }
}
