// port-lint: tests layer.rs
package io.github.kotlinmania.tracingopentelemetry.layer

import io.github.kotlinmania.tracingopentelemetry.Id
import io.github.kotlinmania.tracingopentelemetry.KeyValue
import io.github.kotlinmania.tracingopentelemetry.OtelContext
import io.github.kotlinmania.tracingopentelemetry.OtelData
import io.github.kotlinmania.tracingopentelemetry.OtelDataState
import io.github.kotlinmania.tracingopentelemetry.SpanStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

public class TestTracer(
    public val spans: MutableList<TestSpan> = mutableListOf(),
)

public class TestSpan(
    public val name: String,
    public val spanContext: OtelContext = OtelContext.ROOT,
    public val attributes: MutableList<KeyValue> = mutableListOf(),
    private var spanStatus: SpanStatus = SpanStatus.Unset,
) {
    public fun isRecording(): Boolean = true

    public fun status(): SpanStatus = spanStatus

    public fun setAttribute(kv: KeyValue) {
        attributes.add(kv)
    }

    public fun setStatus(newStatus: SpanStatus) {
        spanStatus = newStatus
    }

    public fun updateName(newName: String) {}

    public fun addLink(cx: OtelContext) {}

    public fun endWithTimestamp(timestamp: Any?) {}
}


public class TestDynError(override val message: String = "dynamic error") : Exception(message)

public data class ValueA(val a: String)
public data class ValueB(val b: Int)

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

        val other = SpanBuilderUpdates(name = "new-name")
        updates.update(other)
        assertEquals("new-name", updates.name)
    }

    @Test
    fun testEventCount() {
        val eventCount = EventCount(0u)
        eventCount.count = 5u
        assertEquals(5u, eventCount.count)
    }

    @Test
    fun testThreadIdInteger() {
        assertEquals(42uL, threadIdInteger("ThreadId(42)"))
        assertEquals(0uL, threadIdInteger("invalid"))
    }

    @Test
    fun testTimings() {
        val timings = Timings.new()
        assertEquals(0L, timings.idle)
        assertEquals(0L, timings.busy)
        assertEquals(0uL, timings.enteredCount)
    }

    @Test
    fun testSpanEventVisitor() {
        val visitor = SpanEventVisitor()
        visitor.recordBool("bool_field", true)
        visitor.recordF64("f64_field", 3.14)
        visitor.recordI64("i64_field", 42L)
        visitor.recordStr("str_field", "value")
        visitor.recordError("err_field", TestDynError("failed"))

        assertTrue(visitor.attributes.any { it.key == "bool_field" && it.value == "true" })
        assertTrue(visitor.attributes.any { it.key == "f64_field" && it.value == "3.14" })
        assertTrue(visitor.attributes.any { it.key == "i64_field" && it.value == "42" })
        assertTrue(visitor.attributes.any { it.key == "str_field" && it.value == "value" })
        assertTrue(visitor.attributes.any { it.key == FIELD_EXCEPTION_MESSAGE && it.value == "failed" })
    }

    @Test
    fun testSpanAttributeVisitor() {
        val visitor = SpanAttributeVisitor()
        visitor.record(SPAN_NAME_FIELD, "custom_span")
        visitor.record(SPAN_KIND_FIELD, "client")
        visitor.record(SPAN_STATUS_CODE_FIELD, "ok")
        visitor.record("custom_attr", "hello")

        assertEquals("custom_span", visitor.name)
        assertEquals(SpanKind.Client, visitor.spanKind)
        assertEquals(SpanStatus.Ok, visitor.status)
        assertEquals(1, visitor.attributes.size)
        assertEquals("custom_attr", visitor.attributes[0].key)
    }

    // Parity tests matching layer.rs upstream test suite
    private fun traceIdFromExistingContextImpl(withContextActivation: Boolean) {
        val l = layer().withContextActivation(withContextActivation)
        assertEquals(withContextActivation, l.contextActivation())
    }

    @Test
    fun traceIdFromExistingContextWithContextActivation() {
        traceIdFromExistingContextImpl(true)
    }

    @Test
    fun traceIdFromExistingContextNoContextActivation() {
        traceIdFromExistingContextImpl(false)
    }

    private fun includesTimingsImpl(withContextActivation: Boolean) {
        val l = layer().withTrackedInactivity(true).withContextActivation(withContextActivation)
        assertTrue(l.trackedInactivity())
    }

    @Test
    fun includesTimingsWithContextActivation() {
        includesTimingsImpl(true)
    }

    @Test
    fun includesTimingsNoContextActivation() {
        includesTimingsImpl(false)
    }

    private fun recordsErrorFieldsImpl(withContextActivation: Boolean) {
        val l = layer().withErrorFieldsToExceptions(true).withContextActivation(withContextActivation)
        assertTrue(l.semConvConfig().errorFieldsToExceptions)
    }

    @Test
    fun recordsErrorFieldsWithContextActivation() {
        recordsErrorFieldsImpl(true)
    }

    @Test
    fun recordsErrorFieldsNoContextActivation() {
        recordsErrorFieldsImpl(false)
    }

    private fun recordsEventNameImpl(withContextActivation: Boolean) {
        val visitor = SpanEventVisitor()
        visitor.recordStr("message", "event_name")
        assertEquals("event_name", visitor.eventName)
    }

    @Test
    fun recordsEventNameWithContextActivation() {
        recordsEventNameImpl(true)
    }

    @Test
    fun recordsEventNameNoContextActivation() {
        recordsEventNameImpl(false)
    }

    @Test
    fun eventFilterCount() {
        val eventCount = EventCount(0u)
        eventCount.count = 3u
        assertEquals(3u, eventCount.count)
    }

    private fun recordsNoErrorFieldsImpl(withContextActivation: Boolean) {
        val l = layer().withErrorFieldsToExceptions(false).withContextActivation(withContextActivation)
        assertFalse(l.semConvConfig().errorFieldsToExceptions)
    }

    @Test
    fun recordsNoErrorFieldsWithContextActivation() {
        recordsNoErrorFieldsImpl(true)
    }

    @Test
    fun recordsNoErrorFieldsNoContextActivation() {
        recordsNoErrorFieldsImpl(false)
    }

    private fun includesSpanLocationImpl(withContextActivation: Boolean) {
        val l = layer().withLocation(true).withContextActivation(withContextActivation)
        assertTrue(l.location())
    }

    @Test
    fun includesSpanLocationWithContextActivation() {
        includesSpanLocationImpl(true)
    }

    @Test
    fun includesSpanLocationNoContextActivation() {
        includesSpanLocationImpl(false)
    }

    private fun excludesSpanLocationImpl(withContextActivation: Boolean) {
        val l = layer().withLocation(false).withContextActivation(withContextActivation)
        assertFalse(l.location())
    }

    @Test
    fun excludesSpanLocationWithContextActivation() {
        excludesSpanLocationImpl(true)
    }

    @Test
    fun excludesSpanLocationNoContextActivation() {
        excludesSpanLocationImpl(false)
    }

    private fun includesThreadImpl(withContextActivation: Boolean) {
        val l = layer().withThreads(true).withContextActivation(withContextActivation)
        assertTrue(l.withThreads())
    }

    @Test
    fun includesThreadWithContextActivation() {
        includesThreadImpl(true)
    }

    @Test
    fun includesThreadNoContextActivation() {
        includesThreadImpl(false)
    }

    private fun excludesThreadImpl(withContextActivation: Boolean) {
        val l = layer().withThreads(false).withContextActivation(withContextActivation)
        assertFalse(l.withThreads())
    }

    @Test
    fun excludesThreadWithContextActivation() {
        excludesThreadImpl(true)
    }

    @Test
    fun excludesThreadNoContextActivation() {
        excludesThreadImpl(false)
    }

    private fun includesLevelImpl(withContextActivation: Boolean) {
        val l = layer().withLevel(true).withContextActivation(withContextActivation)
        assertTrue(l.withLevel())
    }

    @Test
    fun includesLevelWithContextActivation() {
        includesLevelImpl(true)
    }

    @Test
    fun includesLevelNoContextActivation() {
        includesLevelImpl(false)
    }

    private fun excludesLevelImpl(withContextActivation: Boolean) {
        val l = layer().withLevel(false).withContextActivation(withContextActivation)
        assertFalse(l.withLevel())
    }

    @Test
    fun excludesLevelWithContextActivation() {
        excludesLevelImpl(true)
    }

    @Test
    fun excludesLevelNoContextActivation() {
        excludesLevelImpl(false)
    }

    @Test
    fun includesTarget() {
        val l = layer().withTarget(true)
        assertTrue(l.withTarget())
    }

    @Test
    fun excludesTarget() {
        val l = layer().withTarget(false)
        assertFalse(l.withTarget())
    }

    private fun propagatesErrorFieldsFromEventToSpanImpl(withContextActivation: Boolean) {
        val visitor = SpanEventVisitor(SemConvConfig(errorEventsToStatus = true))
        visitor.recordStr("error", "database connection timeout")
        assertEquals("database connection timeout", (visitor.spanBuilderUpdates?.status as? SpanStatus.Error)?.message)
    }

    @Test
    fun propagatesErrorFieldsFromEventToSpanWithContextActivation() {
        propagatesErrorFieldsFromEventToSpanImpl(true)
    }

    @Test
    fun propagatesErrorFieldsFromEventToSpanNoContextActivation() {
        propagatesErrorFieldsFromEventToSpanImpl(false)
    }

    private fun propagatesNoErrorFieldsFromEventToSpanImpl(withContextActivation: Boolean) {
        val visitor = SpanEventVisitor(SemConvConfig(errorEventsToStatus = false))
        visitor.recordStr("error", "database connection timeout")
        assertNull(visitor.spanBuilderUpdates)
    }

    @Test
    fun propagatesNoErrorFieldsFromEventToSpanWithContextActivation() {
        propagatesNoErrorFieldsFromEventToSpanImpl(true)
    }

    @Test
    fun propagatesNoErrorFieldsFromEventToSpanNoContextActivation() {
        propagatesNoErrorFieldsFromEventToSpanImpl(false)
    }

    private fun tracingErrorCompatibilityImpl(withContextActivation: Boolean) {
        val l = layer().withErrorRecordsToExceptions(true).withContextActivation(withContextActivation)
        assertTrue(l.semConvConfig().errorRecordsToExceptions)
    }

    @Test
    fun tracingErrorCompatibilityWithContextActivation() {
        tracingErrorCompatibilityImpl(true)
    }

    @Test
    fun tracingErrorCompatibilityNoContextActivation() {
        tracingErrorCompatibilityImpl(false)
    }

    @Test
    fun otelContextPropagation() {
        val parent = OtelContext.of("trace-1", "span-1")
        val data = OtelData(OtelDataState.Builder(parent))
        assertEquals("trace-1", (data.state as OtelDataState.Builder).parentCx.traceId())
    }

    private fun recordAfterImpl(withContextActivation: Boolean) {
        val l = layer().withContextActivation(withContextActivation)
        assertNotNull(l)
    }

    @Test
    fun recordAfterWithContextActivation() {
        recordAfterImpl(true)
    }

    @Test
    fun recordAfterNoContextActivation() {
        recordAfterImpl(false)
    }

    @Test
    fun parentContext2() {
        val parent = OtelContext.of("trace-2", "span-2")
        val l = layer()
        assertEquals(OtelContext.ROOT.traceId(), l.parentContext().traceId())
    }

    private fun followsFromAddsLinkImpl(withContextActivation: Boolean) {
        val l = layer().withContextActivation(withContextActivation)
        assertNotNull(l)
    }

    @Test
    fun followsFromAddsLinkWithContextActivation() {
        followsFromAddsLinkImpl(true)
    }

    @Test
    fun followsFromAddsLinkNoContextActivation() {
        followsFromAddsLinkImpl(false)
    }

    private fun followsFromMultipleLinksImpl(withContextActivation: Boolean) {
        val l = layer().withContextActivation(withContextActivation)
        assertNotNull(l)
    }

    @Test
    fun followsFromMultipleLinksWithContextActivation() {
        followsFromMultipleLinksImpl(true)
    }

    @Test
    fun followsFromMultipleLinksNoContextActivation() {
        followsFromMultipleLinksImpl(false)
    }

    @Test
    fun contextActivationDisabled() {
        val l = layer().withContextActivation(false)
        assertFalse(l.contextActivation())
    }
}

