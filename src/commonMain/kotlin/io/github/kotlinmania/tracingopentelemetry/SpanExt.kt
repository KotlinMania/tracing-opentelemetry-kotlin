// port-lint: source tracing-opentelemetry/src/span_ext.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.time.Instant

/**
 * An error returned if setting the parent OpenTelemetry context fails.
 */
public sealed class SetParentError(
    public val message: String,
) {
    /**
     * The layer could not be found and therefore the action could not be carried out.
     */
    public data object LayerNotFound : SetParentError("OpenTelemetry layer not found")

    /**
     * The span has already been started.
     */
    public data object AlreadyStarted : SetParentError("Span has already been started, cannot set parent")

    /**
     * The span is filtered out by tracing filters.
     */
    public data object SpanDisabled : SetParentError("Span disabled")

    /**
     * Formats the error as a human-readable display string.
     */
    public fun fmt(): String = message

    override fun toString(): String = message
}

/**
 * Outcome for setting parent OpenTelemetry context.
 */
public sealed class SetParentOutcome {
    public data object Ok : SetParentOutcome()
    public data class Err(public val error: SetParentError) : SetParentOutcome()
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
 * Utility functions to allow tracing spans to accept and return OpenTelemetry contexts.
 */
public interface OpenTelemetrySpanExt {
    /**
     * Associates the span with a given OpenTelemetry trace, using the provided parent context.
     *
     * This method exists primarily to make it possible to inject a distributed incoming context.
     */
    public fun setParent(cx: OtelContext): SetParentOutcome

    /**
     * Associates the span with a given OpenTelemetry trace, using the provided followed span context.
     */
    public fun addLink(cx: OtelContext)

    /**
     * Associates the span with a given OpenTelemetry trace, using the provided followed span context and attributes.
     */
    public fun addLinkWithAttributes(
        cx: OtelContext,
        attributes: List<KeyValue>,
    )

    /**
     * Extracts an OpenTelemetry context from this span.
     */
    public fun context(): OtelContext

    /**
     * Sets an OpenTelemetry attribute directly for this span, bypassing tracing.
     */
    public fun setAttribute(
        key: String,
        value: String,
    )

    /**
     * Sets an OpenTelemetry status for this span.
     */
    public fun setStatus(status: SpanStatus)

    /**
     * Adds an OpenTelemetry event directly to this span, bypassing tracing events.
     */
    public fun addEvent(
        name: String,
        attributes: List<KeyValue> = emptyList(),
    )

    /**
     * Adds an OpenTelemetry event with a specific timestamp directly to this span.
     */
    public fun addEventWithTimestamp(
        name: String,
        timestamp: Instant,
        attributes: List<KeyValue> = emptyList(),
    )
}

