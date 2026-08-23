// port-lint: source layer/filtered.rs
package io.github.kotlinmania.tracingopentelemetry.layer

/**
 * Filtered OpenTelemetry layer that counts events and discards filtered ones.
 */
public class FilteredOpenTelemetryLayer<F>(
    private val inner: OpenTelemetryLayer,
    private val filter: F,
) {
    public fun inner(): OpenTelemetryLayer = inner

    public fun filter(): F = filter

    public companion object {
        public fun <F> new(
            inner: OpenTelemetryLayer,
            filter: F,
        ): FilteredOpenTelemetryLayer<F> = FilteredOpenTelemetryLayer(inner, filter)
    }
}
