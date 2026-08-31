// port-lint: source tracing-opentelemetry/src/otel_context.rs
package io.github.kotlinmania.tracingopentelemetry

/**
 * An OpenTelemetry tracing context containing trace identifier and span identifier metadata.
 *
 * Utility functions allow tracing extensions to accept and return OpenTelemetry contexts.
 */
public class OtelContext(
    private val traceId: String? = null,
    private val spanId: String? = null,
    private val isValid: Boolean = true,
) {
    /**
     * Returns the trace ID.
     */
    public fun traceId(): String? = traceId

    /**
     * Returns the span ID.
     */
    public fun spanId(): String? = spanId

    /**
     * Returns true if the context is valid.
     */
    public fun isValid(): Boolean = isValid

    public companion object {
        /**
         * Root context with no active trace or span ID.
         */
        public val ROOT: OtelContext = OtelContext(null, null, false)

        /**
         * Constructs a valid context with the specified trace and span IDs.
         */
        public fun of(
            traceId: String,
            spanId: String,
        ): OtelContext = OtelContext(traceId, spanId, true)
    }
}

/**
 * Extracts the OpenTelemetry context from span data if available.
 *
 * This method retrieves the OpenTelemetry context data that has been stored
 * for the span by the OpenTelemetry layer. The context includes the span's
 * OpenTelemetry span context, which contains trace ID, span ID, and other
 * trace-related metadata.
 *
 * ### Use Cases
 *
 * - When working with multiple subscriber configurations
 * - When implementing advanced tracing middleware that manages multiple dispatches
 */
public fun getOtelContext(data: OtelData): OtelContext =
    when (val state = data.state) {
        is OtelDataState.Context -> state.currentCx
        is OtelDataState.Builder -> state.parentCx
    }
