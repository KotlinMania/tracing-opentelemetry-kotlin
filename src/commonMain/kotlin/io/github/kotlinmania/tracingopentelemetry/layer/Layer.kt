// port-lint: source layer.rs
package io.github.kotlinmania.tracingopentelemetry.layer

import io.github.kotlinmania.tracingopentelemetry.KeyValue
import io.github.kotlinmania.tracingopentelemetry.SpanStatus

public const val SPAN_NAME_FIELD: String = "otel.name"
public const val SPAN_KIND_FIELD: String = "otel.kind"
public const val SPAN_STATUS_CODE_FIELD: String = "otel.status_code"
public const val SPAN_STATUS_DESCRIPTION_FIELD: String = "otel.status_description"
public const val SPAN_EVENT_COUNT_FIELD: String = "otel.tracing_event_count"

public const val EVENT_EXCEPTION_NAME: String = "exception"
public const val FIELD_EXCEPTION_MESSAGE: String = "exception.message"
public const val FIELD_EXCEPTION_STACKTRACE: String = "exception.stacktrace"

/**
 * OpenTelemetry span kind.
 */
public enum class SpanKind {
    Server,
    Client,
    Producer,
    Consumer,
    Internal,
}

/**
 * Converts string representation to SpanKind.
 */
public fun strToSpanKind(s: String): SpanKind? =
    when {
        s.equals("server", ignoreCase = true) -> SpanKind.Server
        s.equals("client", ignoreCase = true) -> SpanKind.Client
        s.equals("producer", ignoreCase = true) -> SpanKind.Producer
        s.equals("consumer", ignoreCase = true) -> SpanKind.Consumer
        s.equals("internal", ignoreCase = true) -> SpanKind.Internal
        else -> null
    }

/**
 * Converts string representation to SpanStatus.
 */
public fun strToStatus(s: String): SpanStatus =
    when {
        s.equals("ok", ignoreCase = true) -> SpanStatus.Ok
        s.equals("error", ignoreCase = true) -> SpanStatus.Error()
        else -> SpanStatus.Unset
    }

/**
 * Control over the mapping between tracing fields/events and OpenTelemetry conventional status/exception fields.
 */
public data class SemConvConfig(
    val errorFieldsToExceptions: Boolean = true,
    val errorRecordsToExceptions: Boolean = true,
    val errorEventsToStatus: Boolean = true,
    val errorEventsToExceptions: Boolean = true,
)

/**
 * Updates to span builder.
 */
public data class SpanBuilderUpdates(
    var name: String? = null,
    var spanKind: SpanKind? = null,
    var status: SpanStatus? = null,
    var attributes: List<KeyValue>? = null,
) {
    public fun updateStatus(newStatus: SpanStatus) {
        this.status = newStatus
    }

    public fun addAttribute(attribute: KeyValue) {
        attributes = (attributes ?: emptyList()) + attribute
    }
}

/**
 * OpenTelemetry layer for tracing spans and events.
 */
public class OpenTelemetryLayer(
    private var location: Boolean = true,
    private var trackedInactivity: Boolean = true,
    private var withThreads: Boolean = true,
    private var withLevel: Boolean = false,
    private var withTarget: Boolean = true,
    private var contextActivation: Boolean = true,
    private var semConvConfig: SemConvConfig = SemConvConfig(),
) {
    public fun location(): Boolean = location

    public fun trackedInactivity(): Boolean = trackedInactivity

    public fun withThreads(): Boolean = withThreads

    public fun withLevel(): Boolean = withLevel

    public fun withTarget(): Boolean = withTarget

    public fun contextActivation(): Boolean = contextActivation

    public fun semConvConfig(): SemConvConfig = semConvConfig

    public fun withLocation(location: Boolean): OpenTelemetryLayer {
        this.location = location
        return this
    }

    public fun withTrackedInactivity(trackedInactivity: Boolean): OpenTelemetryLayer {
        this.trackedInactivity = trackedInactivity
        return this
    }

    public fun withThreads(withThreads: Boolean): OpenTelemetryLayer {
        this.withThreads = withThreads
        return this
    }

    public fun withLevel(withLevel: Boolean): OpenTelemetryLayer {
        this.withLevel = withLevel
        return this
    }

    public fun withTarget(withTarget: Boolean): OpenTelemetryLayer {
        this.withTarget = withTarget
        return this
    }

    public fun withContextActivation(contextActivation: Boolean): OpenTelemetryLayer {
        this.contextActivation = contextActivation
        return this
    }

    public fun withErrorFieldsToExceptions(errorFieldsToExceptions: Boolean): OpenTelemetryLayer {
        this.semConvConfig = this.semConvConfig.copy(errorFieldsToExceptions = errorFieldsToExceptions)
        return this
    }

    public fun withErrorEventsToStatus(errorEventsToStatus: Boolean): OpenTelemetryLayer {
        this.semConvConfig = this.semConvConfig.copy(errorEventsToStatus = errorEventsToStatus)
        return this
    }

    public fun withErrorEventsToExceptions(errorEventsToExceptions: Boolean): OpenTelemetryLayer {
        this.semConvConfig = this.semConvConfig.copy(errorEventsToExceptions = errorEventsToExceptions)
        return this
    }

    public fun withErrorRecordsToExceptions(errorRecordsToExceptions: Boolean): OpenTelemetryLayer {
        this.semConvConfig = this.semConvConfig.copy(errorRecordsToExceptions = errorRecordsToExceptions)
        return this
    }

    public fun <F> withCountingEventFilter(filter: F): FilteredOpenTelemetryLayer<F> =
        FilteredOpenTelemetryLayer.new(this, filter)

    public companion object {
        public fun new(): OpenTelemetryLayer = OpenTelemetryLayer()
    }
}

/**
 * Constructs an OpenTelemetry layer.
 */
public fun layer(): OpenTelemetryLayer = OpenTelemetryLayer.new()
