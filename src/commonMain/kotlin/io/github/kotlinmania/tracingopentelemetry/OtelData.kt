package io.github.kotlinmania.tracingopentelemetry

import kotlin.time.Instant

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
            is OtelDataState.Builder -> null
        }

    /**
     * Gets the span ID of the span.
     *
     * Returns null if the context has not been built yet.
     */
    public fun spanId(): String? =
        when (val s = state) {
            is OtelDataState.Context -> s.currentCx.spanId()
            is OtelDataState.Builder -> null
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
