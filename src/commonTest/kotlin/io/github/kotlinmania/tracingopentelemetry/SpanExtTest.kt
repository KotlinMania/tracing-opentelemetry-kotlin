// port-lint: tests tracing-opentelemetry/src/span_ext.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpanExtTest {
    private class MockSpan : OpenTelemetrySpanExt {
        var parent: OtelContext? = null
        val links: MutableList<SpanLink> = mutableListOf()
        val attributes: MutableMap<String, String> = mutableMapOf()
        var spanStatus: SpanStatus = SpanStatus.Unset
        val events: MutableList<SpanEvent> = mutableListOf()

        override fun setParent(cx: OtelContext): SetParentOutcome {
            parent = cx
            return SetParentOutcome.Ok
        }

        override fun addLink(cx: OtelContext) {
            links.add(SpanLink(cx))
        }

        override fun addLinkWithAttributes(
            cx: OtelContext,
            attributes: List<KeyValue>,
        ) {
            links.add(SpanLink(cx, attributes))
        }

        override fun context(): OtelContext = parent ?: OtelContext.ROOT

        override fun setAttribute(
            key: String,
            value: String,
        ) {
            attributes[key] = value
        }

        override fun setStatus(status: SpanStatus) {
            this.spanStatus = status
        }

        override fun addEvent(
            name: String,
            attributes: List<KeyValue>,
        ) {
            events.add(
                SpanEvent(
                    name,
                    kotlin.time.TimeSource.Monotonic
                        .markNow()
                        .elapsedNow()
                        .let { kotlin.time.Instant.fromEpochMilliseconds(0) },
                    attributes,
                ),
            )
        }

        override fun addEventWithTimestamp(
            name: String,
            timestamp: kotlin.time.Instant,
            attributes: List<KeyValue>,
        ) {
            events.add(SpanEvent(name, timestamp, attributes))
        }
    }

    @Test
    fun setStatusOk() {
        val span = MockSpan()
        span.setStatus(SpanStatus.Ok)
        assertEquals(SpanStatus.Ok, span.spanStatus)
    }

    @Test
    fun setStatusError() {
        val expectedError = SpanStatus.Error("Elon put in too much fuel in his rocket!")
        val span = MockSpan()
        span.setStatus(expectedError)
        assertEquals(expectedError, span.spanStatus)
    }

    @Test
    fun testSetParentAndAttributes() {
        val span = MockSpan()
        val parent = OtelContext.of("trace-1", "span-1")
        assertEquals(SetParentOutcome.Ok, span.setParent(parent))
        assertEquals(parent, span.context())

        span.setAttribute("http.status_code", "200")
        assertEquals("200", span.attributes["http.status_code"])
    }
}
