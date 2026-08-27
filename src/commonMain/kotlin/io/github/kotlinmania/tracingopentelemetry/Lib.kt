// port-lint: source lib.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Tracing OpenTelemetry connects spans from multiple systems into a trace and emits
 * them to OpenTelemetry-compatible distributed tracing systems for processing and visualization.
 *
 * ### Special Fields
 *
 * Fields with an `otel.` prefix are reserved for this library and have specific meaning:
 * - `otel.name`: Override the span name sent to OpenTelemetry exporters.
 * - `otel.kind`: Set the span kind to one of the supported OpenTelemetry span kinds ("client", "server", etc.).
 * - `otel.status`: Set the span status code.
 * - `otel.message`: Set the span description of the status.
 *
 * ### Semantic Conventions
 *
 * OpenTelemetry defines conventional names for attributes of common operations.
 *
 * ### Feature Flags
 *
 * - `metrics`: Enables the MetricsLayer type, exporting OpenTelemetry metrics from specifically-named events.
 */
public object TracingOpentelemetryLib {
    public const val MODULE_NAME: String = "tracing-opentelemetry"
    public const val CRATE_NAME: String = "tracing_opentelemetry"
}

/**
 * Time utility module for timestamp generation.
 */
public object TracingOpentelemetryTime {
    /**
     * Gets current instant timestamp.
     */
    public fun now(): Instant = Clock.System.now()
}

/**
 * Returns current timestamp.
 */
public fun now(): Instant = TracingOpentelemetryTime.now()

/**
 * Per-span OpenTelemetry data tracked by this crate.
 */
public class OtelData internal constructor(
    internal var state: OtelDataState,
    internal var endTime: Instant? = null,
) {
    /**
     * Gets the trace ID of the span.
     *
     * Returns null if the context has not been built yet.
     */
    public fun traceId(): String? =
        when (val s = state) {
            is OtelDataState.Context -> s.currentCx.traceId()
            else -> null
        }

    /**
     * Gets the span ID of the span.
     *
     * Returns null if the context has not been built yet.
     */
    public fun spanId(): String? =
        when (val s = state) {
            is OtelDataState.Context -> s.currentCx.spanId()
            else -> null
        }
}

/**
 * The state of the OpenTelemetry data for a span, which can either be a builder or a context.
 */
public sealed class OtelDataState {
    /**
     * The span is being built, with a parent context and status.
     */
    public data class Builder(
        public var parentCx: OtelContext,
        public var status: SpanStatus = SpanStatus.Unset,
    ) : OtelDataState()

    /**
     * The span has been started or accessed and is now in a context.
     */
    public data class Context(
        public var currentCx: OtelContext,
    ) : OtelDataState()

    public companion object {
        /**
         * Returns the default OtelDataState in Context mode with root context.
         */
        public fun default(): OtelDataState = Context(OtelContext.ROOT)
    }
}

/**
 * OpenTelemetry status of a span.
 */
public sealed class SpanStatus {
    /**
     * The default status of a span where no status has been explicitly set.
     */
    public data object Unset : SpanStatus()

    /**
     * Indicates that the span completed successfully.
     */
    public data object Ok : SpanStatus()

    /**
     * Indicates that an error occurred during span execution.
     */
    public data class Error(
        public val message: String = "",
    ) : SpanStatus()
}
