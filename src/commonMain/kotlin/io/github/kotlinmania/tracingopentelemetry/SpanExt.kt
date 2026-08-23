// port-lint: source span_ext.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.time.Instant

/**
 * An error returned if setting the parent OpenTelemetry context fails.
 */
public sealed class SetParentError(
    override val message: String,
) : Exception(message) {
    public data object LayerNotFound : SetParentError("OpenTelemetry layer not found")

    public data object AlreadyStarted : SetParentError("Span has already been started, cannot set parent")

    public data object SpanDisabled : SetParentError("Span disabled")
}

/**
 * Key-value pair attribute.
 */
public data class KeyValue(
    val key: String,
    val value: String,
)

/**
 * Event recorded on an OpenTelemetry span.
 */
public data class SpanEvent(
    val name: String,
    val timestamp: Instant,
    val attributes: List<KeyValue> = emptyList(),
)

/**
 * Span link representation.
 */
public data class SpanLink(
    val context: OtelContext,
    val attributes: List<KeyValue> = emptyList(),
)

/**
 * Extension trait on spans to allow managing OpenTelemetry context, attributes, and events.
 */
public interface OpenTelemetrySpanExt {
    public fun setParent(cx: OtelContext): Result<Unit>

    public fun addLink(cx: OtelContext)

    public fun addLinkWithAttributes(
        cx: OtelContext,
        attributes: List<KeyValue>,
    )

    public fun context(): OtelContext

    public fun setAttribute(
        key: String,
        value: String,
    )

    public fun setStatus(status: SpanStatus)

    public fun addEvent(
        name: String,
        attributes: List<KeyValue> = emptyList(),
    )

    public fun addEventWithTimestamp(
        name: String,
        timestamp: Instant,
        attributes: List<KeyValue> = emptyList(),
    )
}
