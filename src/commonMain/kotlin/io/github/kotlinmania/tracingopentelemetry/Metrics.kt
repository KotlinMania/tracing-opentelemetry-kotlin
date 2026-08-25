// port-lint: source metrics.rs
package io.github.kotlinmania.tracingopentelemetry

import io.github.kotlinmania.tracingopentelemetry.layer.FilteredOpenTelemetryLayer

public const val METRIC_PREFIX_MONOTONIC_COUNTER: String = "monotonic_counter."
public const val METRIC_PREFIX_COUNTER: String = "counter."
public const val METRIC_PREFIX_HISTOGRAM: String = "histogram."
public const val METRIC_PREFIX_GAUGE: String = "gauge."
public const val INSTRUMENTATION_LIBRARY_NAME: String = "tracing/tracing-opentelemetry"

/**
 * Metric map collection type.
 */
public typealias MetricsMap<T> = Map<String, T>

/**
 * Metric instruments collection.
 */
public class Instruments {
    private val _u64Counter: MutableMap<String, ULong> = mutableMapOf()
    public val u64Counter: MetricsMap<ULong> get() = _u64Counter

    private val _f64Counter: MutableMap<String, Double> = mutableMapOf()
    public val f64Counter: MetricsMap<Double> get() = _f64Counter

    private val _i64UpDownCounter: MutableMap<String, Long> = mutableMapOf()
    public val i64UpDownCounter: MetricsMap<Long> get() = _i64UpDownCounter

    private val _f64UpDownCounter: MutableMap<String, Double> = mutableMapOf()
    public val f64UpDownCounter: MetricsMap<Double> get() = _f64UpDownCounter

    private val _u64Histogram: MutableMap<String, ULong> = mutableMapOf()
    public val u64Histogram: MetricsMap<ULong> get() = _u64Histogram

    private val _f64Histogram: MutableMap<String, Double> = mutableMapOf()
    public val f64Histogram: MetricsMap<Double> get() = _f64Histogram

    private val _u64Gauge: MutableMap<String, ULong> = mutableMapOf()
    public val u64Gauge: MetricsMap<ULong> get() = _u64Gauge

    private val _i64Gauge: MutableMap<String, Long> = mutableMapOf()
    public val i64Gauge: MetricsMap<Long> get() = _i64Gauge

    private val _f64Gauge: MutableMap<String, Double> = mutableMapOf()
    public val f64Gauge: MetricsMap<Double> get() = _f64Gauge

    private fun <T> updateOrInsert(
        map: MutableMap<String, T>,
        name: String,
        insert: () -> T,
        update: (T) -> T,
    ) {
        val current = map[name]
        if (current != null) {
            map[name] = update(current)
        } else {
            map[name] = insert()
        }
    }

    public fun updateMetric(
        instrumentType: InstrumentType,
        metricName: String,
        attributes: List<KeyValue> = emptyList(),
    ) {
        when (instrumentType) {
            is InstrumentType.CounterU64 ->
                updateOrInsert(_u64Counter, metricName, { instrumentType.value }, { it + instrumentType.value })
            is InstrumentType.CounterF64 ->
                updateOrInsert(_f64Counter, metricName, { instrumentType.value }, { it + instrumentType.value })
            is InstrumentType.UpDownCounterI64 ->
                updateOrInsert(_i64UpDownCounter, metricName, { instrumentType.value }, { it + instrumentType.value })
            is InstrumentType.UpDownCounterF64 ->
                updateOrInsert(_f64UpDownCounter, metricName, { instrumentType.value }, { it + instrumentType.value })
            is InstrumentType.HistogramU64 ->
                updateOrInsert(_u64Histogram, metricName, { instrumentType.value }, { instrumentType.value })
            is InstrumentType.HistogramF64 ->
                updateOrInsert(_f64Histogram, metricName, { instrumentType.value }, { instrumentType.value })
            is InstrumentType.GaugeU64 ->
                updateOrInsert(_u64Gauge, metricName, { instrumentType.value }, { instrumentType.value })
            is InstrumentType.GaugeI64 ->
                updateOrInsert(_i64Gauge, metricName, { instrumentType.value }, { instrumentType.value })
            is InstrumentType.GaugeF64 ->
                updateOrInsert(_f64Gauge, metricName, { instrumentType.value }, { instrumentType.value })
        }
    }
}

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

    public fun enabled(metadata: Any? = null, ctx: Any? = null): Boolean =
        if (metadata is String) isMetricsEvent(metadata) else true

    public fun callsiteEnabled(metadata: Any? = null): Boolean =
        enabled(metadata)
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

    public fun recordDebug(
        fieldName: String,
        value: Any?,
    ) {
        attributes.add(KeyValue(fieldName, value.toString()))
    }

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
 * Underlying instrument layer for tracking metrics.
 */
public class InstrumentLayer(
    public val instruments: Instruments = Instruments(),
) {
    public fun onEvent(event: Any? = null, ctx: Any? = null) {}

    public fun onLayer(subscriber: Any? = null) {}

    public fun registerCallsite(metadata: Any? = null): Any? = null

    public fun enabled(metadata: Any? = null, ctx: Any? = null): Boolean = true

    public fun onNewSpan(attrs: Any? = null, id: Id? = null, ctx: Any? = null) {}

    public fun maxLevelHint(): Any? = null

    public fun onRecord(span: Id? = null, values: Any? = null, ctx: Any? = null) {}

    public fun onFollowsFrom(span: Id? = null, follows: Id? = null, ctx: Any? = null) {}

    public fun onEnter(id: Id? = null, ctx: Any? = null) {}

    public fun onExit(id: Id? = null, ctx: Any? = null) {}

    public fun onClose(id: Id? = null, ctx: Any? = null) {}

    public fun onIdChange(old: Id? = null, new: Id? = null, ctx: Any? = null) {}
}

/**
 * A test layer that panics on event.
 */
public class PanicLayer {
    public fun onEvent(event: Any? = null, ctx: Any? = null) {
        error("panic")
    }
}

/**
 * A layer that publishes metrics via the OpenTelemetry SDK.
 *
 * To publish a new metric, add a key-value pair to your tracing event that contains one of the metric prefixes (monotonic counter, counter, histogram, gauge).
 */
public class MetricsLayer(
    public val instrumentLayer: InstrumentLayer = InstrumentLayer(),
) {
    private val recordedMetrics: MutableMap<String, MutableList<InstrumentType>> = mutableMapOf()

    public fun updateMetric(
        name: String,
        instrument: InstrumentType,
    ) {
        recordedMetrics.getOrPut(name) { mutableListOf() }.add(instrument)
        instrumentLayer.instruments.updateMetric(instrument, name)
    }

    public fun getRecordedMetrics(name: String): List<InstrumentType> = recordedMetrics[name] ?: emptyList()

    public fun onEvent(event: Any? = null, ctx: Any? = null) {
        instrumentLayer.onEvent(event, ctx)
    }

    public fun onLayer(subscriber: Any? = null) {
        instrumentLayer.onLayer(subscriber)
    }

    public fun registerCallsite(metadata: Any? = null): Any? =
        instrumentLayer.registerCallsite(metadata)

    public fun enabled(metadata: Any? = null, ctx: Any? = null): Boolean =
        instrumentLayer.enabled(metadata, ctx)

    public fun onNewSpan(attrs: Any? = null, id: Id? = null, ctx: Any? = null) {
        instrumentLayer.onNewSpan(attrs, id, ctx)
    }

    public fun maxLevelHint(): Any? =
        instrumentLayer.maxLevelHint()

    public fun onRecord(span: Id? = null, values: Any? = null, ctx: Any? = null) {
        instrumentLayer.onRecord(span, values, ctx)
    }

    public fun onFollowsFrom(span: Id? = null, follows: Id? = null, ctx: Any? = null) {
        instrumentLayer.onFollowsFrom(span, follows, ctx)
    }

    public fun onEnter(id: Id? = null, ctx: Any? = null) {
        instrumentLayer.onEnter(id, ctx)
    }

    public fun onExit(id: Id? = null, ctx: Any? = null) {
        instrumentLayer.onExit(id, ctx)
    }

    public fun onClose(id: Id? = null, ctx: Any? = null) {
        instrumentLayer.onClose(id, ctx)
    }

    public fun onIdChange(old: Id? = null, new: Id? = null, ctx: Any? = null) {
        instrumentLayer.onIdChange(old, new, ctx)
    }

    public companion object {
        public fun new(): MetricsLayer = MetricsLayer()
    }
}

