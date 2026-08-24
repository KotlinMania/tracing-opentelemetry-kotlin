// port-lint: source metrics.rs
package io.github.kotlinmania.tracingopentelemetry

public const val METRIC_PREFIX_MONOTONIC_COUNTER: String = "monotonic_counter."
public const val METRIC_PREFIX_COUNTER: String = "counter."
public const val METRIC_PREFIX_HISTOGRAM: String = "histogram."
public const val METRIC_PREFIX_GAUGE: String = "gauge."
public const val INSTRUMENTATION_LIBRARY_NAME: String = "tracing/tracing-opentelemetry"

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
 * Filter to determine whether a given field or event name is a metric event.
 */
public object MetricsFilter {
    public fun isMetricsEvent(fieldName: String): Boolean =
        fieldName.startsWith(METRIC_PREFIX_COUNTER) ||
            fieldName.startsWith(METRIC_PREFIX_MONOTONIC_COUNTER) ||
            fieldName.startsWith(METRIC_PREFIX_HISTOGRAM) ||
            fieldName.startsWith(METRIC_PREFIX_GAUGE)
}

/**
 * Metric entry recorded by visitor.
 */
public data class MetricEntry(
    val name: String,
    val instrument: InstrumentType,
)

/**
 * Visitor that parses metrics from tracing event fields.
 */
public class MetricVisitor {
    private val attributes: MutableList<KeyValue> = mutableListOf()
    private val visitedMetrics: MutableList<MetricEntry> = mutableListOf()

    public fun recordU64(
        fieldName: String,
        value: ULong,
    ) {
        when {
            fieldName.startsWith(METRIC_PREFIX_GAUGE) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_GAUGE)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.GaugeU64(value)))
            }
            fieldName.startsWith(METRIC_PREFIX_MONOTONIC_COUNTER) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_MONOTONIC_COUNTER)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.CounterU64(value)))
            }
            fieldName.startsWith(METRIC_PREFIX_COUNTER) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_COUNTER)
                if (value <= Long.MAX_VALUE.toULong()) {
                    visitedMetrics.add(MetricEntry(metricName, InstrumentType.UpDownCounterI64(value.toLong())))
                }
            }
            fieldName.startsWith(METRIC_PREFIX_HISTOGRAM) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_HISTOGRAM)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.HistogramU64(value)))
            }
            else -> {
                attributes.add(KeyValue(fieldName, value.toString()))
            }
        }
    }

    public fun recordF64(
        fieldName: String,
        value: Double,
    ) {
        when {
            fieldName.startsWith(METRIC_PREFIX_GAUGE) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_GAUGE)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.GaugeF64(value)))
            }
            fieldName.startsWith(METRIC_PREFIX_MONOTONIC_COUNTER) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_MONOTONIC_COUNTER)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.CounterF64(value)))
            }
            fieldName.startsWith(METRIC_PREFIX_COUNTER) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_COUNTER)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.UpDownCounterF64(value)))
            }
            fieldName.startsWith(METRIC_PREFIX_HISTOGRAM) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_HISTOGRAM)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.HistogramF64(value)))
            }
            else -> {
                attributes.add(KeyValue(fieldName, value.toString()))
            }
        }
    }

    public fun recordI64(
        fieldName: String,
        value: Long,
    ) {
        when {
            fieldName.startsWith(METRIC_PREFIX_GAUGE) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_GAUGE)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.GaugeI64(value)))
            }
            fieldName.startsWith(METRIC_PREFIX_MONOTONIC_COUNTER) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_MONOTONIC_COUNTER)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.CounterU64(value.toULong())))
            }
            fieldName.startsWith(METRIC_PREFIX_COUNTER) -> {
                val metricName = fieldName.removePrefix(METRIC_PREFIX_COUNTER)
                visitedMetrics.add(MetricEntry(metricName, InstrumentType.UpDownCounterI64(value)))
            }
            else -> {
                attributes.add(KeyValue(fieldName, value.toString()))
            }
        }
    }

    public fun recordStr(
        fieldName: String,
        value: String,
    ) {
        attributes.add(KeyValue(fieldName, value))
    }

    public fun recordBool(
        fieldName: String,
        value: Boolean,
    ) {
        attributes.add(KeyValue(fieldName, value.toString()))
    }

    public fun attributes(): List<KeyValue> = attributes

    public fun visitedMetrics(): List<MetricEntry> = visitedMetrics
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
