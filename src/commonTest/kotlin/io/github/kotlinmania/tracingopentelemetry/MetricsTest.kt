// port-lint: tests metrics.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.test.Test
import kotlin.test.assertEquals

class MetricsTest {
    @Test
    fun testMetricsLayer() {
        val layer = MetricsLayer.new()
        layer.updateMetric("requests", InstrumentType.CounterU64(10uL))
        layer.updateMetric("requests", InstrumentType.CounterU64(5uL))
        val recorded = layer.getRecordedMetrics("requests")
        assertEquals(2, recorded.size)
    }

    @Test
    fun testUpDownCounter() {
        val layer = MetricsLayer.new()
        layer.updateMetric("in_flight", InstrumentType.UpDownCounterI64(1L))
        layer.updateMetric("in_flight", InstrumentType.UpDownCounterI64(-1L))
        val recorded = layer.getRecordedMetrics("in_flight")
        assertEquals(2, recorded.size)
    }
}
