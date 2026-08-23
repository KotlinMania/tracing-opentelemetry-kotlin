// port-lint: source metrics.rs
package io.github.kotlinmania.tracingopentelemetry

/**
 * Metric instruments representation for OpenTelemetry metrics layer.
 */
public sealed class InstrumentType {
    public data class CounterU64(
        val value: ULong,
    ) : InstrumentType()

    public data class CounterF64(
        val value: Double,
    ) : InstrumentType()

    public data class UpDownCounterI64(
        val value: Long,
    ) : InstrumentType()

    public data class UpDownCounterF64(
        val value: Double,
    ) : InstrumentType()

    public data class HistogramU64(
        val value: ULong,
    ) : InstrumentType()

    public data class HistogramF64(
        val value: Double,
    ) : InstrumentType()

    public data class GaugeU64(
        val value: ULong,
    ) : InstrumentType()

    public data class GaugeI64(
        val value: Long,
    ) : InstrumentType()

    public data class GaugeF64(
        val value: Double,
    ) : InstrumentType()
}

/**
 * OpenTelemetry metrics layer publishing metrics from tracing events.
 */
public class MetricsLayer {
    private val recordedMetrics: MutableMap<String, MutableList<InstrumentType>> = mutableMapOf()

    public fun updateMetric(
        name: String,
        instrument: InstrumentType,
    ) {
        recordedMetrics.getOrPut(name) { mutableListOf() }.add(instrument)
    }

    public fun getRecordedMetrics(name: String): List<InstrumentType> = recordedMetrics[name] ?: emptyList()

    public companion object {
        public fun new(): MetricsLayer = MetricsLayer()
    }
}
