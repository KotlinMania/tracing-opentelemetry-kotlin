// port-lint: tests tracing-opentelemetry/src/otel_context.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtelContextTest {
    @Test
    fun testRootContext() {
        val root = OtelContext.ROOT
        assertFalse(root.isValid())
        assertEquals(null, root.traceId())
        assertEquals(null, root.spanId())
    }

    @Test
    fun testValidContext() {
        val cx = OtelContext.of("trace-123", "span-456")
        assertTrue(cx.isValid())
        assertEquals("trace-123", cx.traceId())
        assertEquals("span-456", cx.spanId())
    }

    @Test
    fun testGetOtelContextFromData() {
        val cx = OtelContext.of("t1", "s1")
        val data = OtelData(OtelDataState.Context(cx))
        val extracted = getOtelContext(data)
        assertEquals("t1", extracted.traceId())
        assertEquals("s1", extracted.spanId())
    }
}
