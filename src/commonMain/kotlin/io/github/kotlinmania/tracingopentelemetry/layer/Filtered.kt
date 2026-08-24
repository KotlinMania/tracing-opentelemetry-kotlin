// port-lint: source layer/filtered.rs
package io.github.kotlinmania.tracingopentelemetry.layer

/**
 * Event count tracker for filtered events.
 */
public data class EventCount(
    public var count: UInt = 0u,
)

/**
 * Filtered OpenTelemetry layer that counts events and discards filtered ones.
 */
public class FilteredOpenTelemetryLayer<F>(
    private val inner: OpenTelemetryLayer,
    private val filter: F,
) {
    public fun inner(): OpenTelemetryLayer = inner

    public fun filter(): F = filter

    public fun mapInner(mapper: (OpenTelemetryLayer) -> OpenTelemetryLayer): FilteredOpenTelemetryLayer<F> =
        FilteredOpenTelemetryLayer(mapper(inner), filter)

    public fun <F2> withCountingEventFilter(filter: F2): FilteredOpenTelemetryLayer<F2> =
        FilteredOpenTelemetryLayer(inner, filter)

    public companion object {
        public fun <F> new(
            inner: OpenTelemetryLayer,
            filter: F,
        ): FilteredOpenTelemetryLayer<F> = FilteredOpenTelemetryLayer(inner, filter)
    }
}
