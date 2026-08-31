// port-lint: tests tracing-opentelemetry/src/metrics.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun testMetricsFilter() {
        assertTrue(MetricsFilter.isMetricsEvent("monotonic_counter.foo"))
        assertTrue(MetricsFilter.isMetricsEvent("counter.baz"))
        assertTrue(MetricsFilter.isMetricsEvent("histogram.qux"))
        assertTrue(MetricsFilter.isMetricsEvent("gauge.bar"))
        assertFalse(MetricsFilter.isMetricsEvent("other_field"))
    }

    @Test
    fun testMetricVisitor() {
        val visitor = MetricVisitor()
        visitor.recordU64("monotonic_counter.requests", 100uL)
        visitor.recordF64("counter.latency", 1.25)
        visitor.recordI64("gauge.memory", 1024L)
        visitor.recordU64("histogram.response_size", 500uL)
        visitor.recordStr("service", "web")
        visitor.recordBool("active", true)

        val metrics = visitor.visitedMetrics()
        assertEquals(4, metrics.size)
        assertEquals(MetricEntry("requests", InstrumentType.CounterU64(100uL)), metrics[0])
        assertEquals(MetricEntry("latency", InstrumentType.UpDownCounterF64(1.25)), metrics[1])
        assertEquals(MetricEntry("memory", InstrumentType.GaugeI64(1024L)), metrics[2])
        assertEquals(MetricEntry("response_size", InstrumentType.HistogramU64(500uL)), metrics[3])

        val attributes = visitor.attributes()
        assertEquals(2, attributes.size)
        assertEquals(KeyValue("service", "web"), attributes[0])
        assertEquals(KeyValue("active", "true"), attributes[1])
    }

    @Test
    fun filterLayerShouldFilterNonMetricsEvent() {
        assertFalse(MetricsFilter.isMetricsEvent("key"))
        assertFalse(MetricsFilter.isMetricsEvent("foo"))
        assertTrue(MetricsFilter.isMetricsEvent("monotonic_counter.requests"))
    }
}
