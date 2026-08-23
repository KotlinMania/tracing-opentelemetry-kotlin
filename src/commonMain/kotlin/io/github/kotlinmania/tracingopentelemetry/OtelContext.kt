// port-lint: source otel_context.rs
package io.github.kotlinmania.tracingopentelemetry

/**
 * An OpenTelemetry tracing context containing trace identifier and span identifier metadata.
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
        public val ROOT: OtelContext = OtelContext(null, null, false)

        public fun of(
            traceId: String,
            spanId: String,
        ): OtelContext = OtelContext(traceId, spanId, true)
    }
}

/**
 * Extracts the OpenTelemetry context from span data if available.
 */
public fun getOtelContext(data: OtelData): OtelContext =
    when (val state = data.state) {
        is OtelDataState.Context -> state.currentCx
        is OtelDataState.Builder -> state.parentCx
    }
