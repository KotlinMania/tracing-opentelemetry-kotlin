// port-lint: source lib.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.time.Instant

/**
 * Module descriptor for the tracing-opentelemetry crate.
 */
public object TracingOpentelemetryLib {
    public const val MODULE_NAME: String = "tracing-opentelemetry"
    public const val CRATE_NAME: String = "tracing_opentelemetry"
}

/**
 * Per-span OpenTelemetry data tracked by this crate.
 */
public class OtelData internal constructor(
    internal var state: OtelDataState,
    internal var endTime: Instant? = null,
) {
    /**
     * Gets the trace ID of the span if context has been built.
     */
    public fun traceId(): String? =
        when (val s = state) {
            is OtelDataState.Context -> s.currentCx.traceId()
            else -> null
        }

    /**
     * Gets the span ID of the span if context has been built.
     */
    public fun spanId(): String? =
        when (val s = state) {
            is OtelDataState.Context -> s.currentCx.spanId()
            else -> null
        }
}

/**
 * The state of the OpenTelemetry data for a span.
 */
internal sealed class OtelDataState {
    data class Builder(
        var parentCx: OtelContext,
        var status: SpanStatus = SpanStatus.Unset,
    ) : OtelDataState()

    data class Context(
        var currentCx: OtelContext,
    ) : OtelDataState()
}

/**
 * OpenTelemetry status of a span.
 */
public sealed class SpanStatus {
    public data object Unset : SpanStatus()

    public data object Ok : SpanStatus()

    public data class Error(
        val message: String = "",
    ) : SpanStatus()
}
