// port-lint: tests layer.rs
package io.github.kotlinmania.tracingopentelemetry.layer

import io.github.kotlinmania.tracingopentelemetry.KeyValue
import io.github.kotlinmania.tracingopentelemetry.SpanStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LayerTest {
    @Test
    fun testCreateLayer() {
        val l =
            layer()
                .withLocation(true)
                .withTrackedInactivity(true)
                .withThreads(true)
                .withLevel(true)
                .withTarget(true)
                .withContextActivation(true)
                .withErrorFieldsToExceptions(true)
                .withErrorEventsToStatus(true)
                .withErrorEventsToExceptions(true)
                .withErrorRecordsToExceptions(true)

        assertTrue(l.location())
        assertTrue(l.trackedInactivity())
        assertTrue(l.withThreads())
        assertTrue(l.withLevel())
        assertTrue(l.withTarget())
        assertTrue(l.contextActivation())
        assertTrue(l.semConvConfig().errorFieldsToExceptions)
        assertTrue(l.semConvConfig().errorEventsToStatus)
        assertTrue(l.semConvConfig().errorEventsToExceptions)
        assertTrue(l.semConvConfig().errorRecordsToExceptions)
    }

    @Test
    fun testFilteredLayer() {
        val l = layer()
        val filtered = FilteredOpenTelemetryLayer.new(l, "mock-filter")
        assertNotNull(filtered.inner())
        assertEquals("mock-filter", filtered.filter())

        val mapped = filtered.mapInner { it.withLevel(true) }
        assertTrue(mapped.inner().withLevel())

        val refiltered = filtered.withCountingEventFilter(42)
        assertEquals(42, refiltered.filter())
    }

    @Test
    fun testStrToSpanKind() {
        assertEquals(SpanKind.Server, strToSpanKind("server"))
        assertEquals(SpanKind.Server, strToSpanKind("SERVER"))
        assertEquals(SpanKind.Client, strToSpanKind("client"))
        assertEquals(SpanKind.Producer, strToSpanKind("producer"))
        assertEquals(SpanKind.Consumer, strToSpanKind("consumer"))
        assertEquals(SpanKind.Internal, strToSpanKind("internal"))
        assertNull(strToSpanKind("unknown"))
    }

    @Test
    fun testStrToStatus() {
        assertEquals(SpanStatus.Ok, strToStatus("ok"))
        assertEquals(SpanStatus.Ok, strToStatus("OK"))
        assertTrue(strToStatus("error") is SpanStatus.Error)
        assertEquals(SpanStatus.Unset, strToStatus("other"))
    }

    @Test
    fun testSpanBuilderUpdates() {
        val updates = SpanBuilderUpdates()
        updates.name = "my-span"
        updates.spanKind = SpanKind.Server
        updates.updateStatus(SpanStatus.Ok)
        updates.addAttribute(KeyValue("k", "v"))

        assertEquals("my-span", updates.name)
        assertEquals(SpanKind.Server, updates.spanKind)
        assertEquals(SpanStatus.Ok, updates.status)
        assertEquals(1, updates.attributes?.size)
        assertEquals(KeyValue("k", "v"), updates.attributes?.first())
    }

    @Test
    fun testEventCount() {
        val eventCount = EventCount(0u)
        eventCount.count = 5u
        assertEquals(5u, eventCount.count)
    }
}
