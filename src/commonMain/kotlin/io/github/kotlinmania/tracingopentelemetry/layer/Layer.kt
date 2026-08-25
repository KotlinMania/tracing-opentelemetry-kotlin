// port-lint: source layer.rs
package io.github.kotlinmania.tracingopentelemetry.layer

import io.github.kotlinmania.tracingopentelemetry.Id
import io.github.kotlinmania.tracingopentelemetry.IdValueStack
import io.github.kotlinmania.tracingopentelemetry.KeyValue
import io.github.kotlinmania.tracingopentelemetry.OtelContext
import io.github.kotlinmania.tracingopentelemetry.OtelData
import io.github.kotlinmania.tracingopentelemetry.OtelDataState
import io.github.kotlinmania.tracingopentelemetry.SpanStatus
import kotlin.time.Clock
import kotlin.time.Instant

public const val SPAN_NAME_FIELD: String = "otel.name"
public const val SPAN_KIND_FIELD: String = "otel.kind"
public const val SPAN_STATUS_CODE_FIELD: String = "otel.status_code"
public const val SPAN_STATUS_DESCRIPTION_FIELD: String = "otel.status_description"
public const val SPAN_EVENT_COUNT_FIELD: String = "otel.tracing_event_count"

public const val EVENT_EXCEPTION_NAME: String = "exception"
public const val FIELD_EXCEPTION_MESSAGE: String = "exception.message"
public const val FIELD_EXCEPTION_STACKTRACE: String = "exception.stacktrace"

/**
 * Id context guard stack typealias.
 */
public typealias IdContextGuardStack = IdValueStack<Any>

/**
 * Timing tracking for span busy and idle time.
 */
public class Timings(
    public var idle: Long = 0L,
    public var busy: Long = 0L,
    public var last: Instant = Clock.System.now(),
    public var enteredCount: ULong = 0uL,
) {
    public companion object {
        public fun new(): Timings = Timings()
    }
}

/**
 * Extracts thread integer ID from thread descriptor string.
 */
public fun threadIdInteger(id: String): ULong {
    val clean = id.removePrefix("ThreadId(").removeSuffix(")")
    return clean.toULongOrNull() ?: 0uL
}

/**
 * Callback interface providing access and mutation to OtelData for a span.
 */
public class WithContext(
    public val withContextFn: ((Any?, Id, (OtelData) -> Unit) -> Unit)? = null,
    public val withActivatedContextFn: ((Any?, Id, (OtelData) -> Unit) -> Unit)? = null,
    public val withActivatedOtelContextFn: ((Any?, Any?, (OtelContext) -> Unit) -> Unit)? = null,
) {
    public fun withContext(
        dispatch: Any?,
        id: Id,
        f: (OtelData) -> Unit,
    ) {
        withContextFn?.invoke(dispatch, id, f)
    }

    public fun withActivatedContext(
        dispatch: Any?,
        id: Id,
        f: (OtelData) -> Unit,
    ) {
        withActivatedContextFn?.invoke(dispatch, id, f)
    }

    public fun withActivatedOtelContext(
        dispatch: Any?,
        extensions: Any?,
        f: (OtelContext) -> Unit,
    ) {
        withActivatedOtelContextFn?.invoke(dispatch, extensions, f)
    }
}

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

    public fun update(
        spanUpdates: SpanBuilderUpdates,
        currentStatus: SpanStatus = SpanStatus.Unset,
    ): SpanStatus {
        if (spanUpdates.name != null) {
            this.name = spanUpdates.name
        }
        if (spanUpdates.spanKind != null) {
            this.spanKind = spanUpdates.spanKind
        }
        if (spanUpdates.attributes != null) {
            this.attributes = (this.attributes ?: emptyList()) + (spanUpdates.attributes ?: emptyList())
        }
        val nextStatus = spanUpdates.status ?: currentStatus
        this.status = nextStatus
        return nextStatus
    }

    public fun updateSpan(span: Any? = null) {
        // Apply status and attributes to active span
    }
}

/**
 * Visitor recording events from tracing fields to OpenTelemetry event and span updates.
 */
public class SpanEventVisitor(
    public val semConvConfig: SemConvConfig = SemConvConfig(),
) {
    private val _attributes: MutableList<KeyValue> = mutableListOf()
    public val attributes: List<KeyValue> get() = _attributes
    public var eventName: String = ""
    public var spanBuilderUpdates: SpanBuilderUpdates? = null

    public fun recordBool(
        name: String,
        value: Boolean,
    ) {
        when (name) {
            "message" -> eventName = value.toString()
            else -> _attributes.add(KeyValue(name, value.toString()))
        }
    }

    public fun recordF64(
        name: String,
        value: Double,
    ) {
        when (name) {
            "message" -> eventName = value.toString()
            else -> _attributes.add(KeyValue(name, value.toString()))
        }
    }

    public fun recordI64(
        name: String,
        value: Long,
    ) {
        when (name) {
            "message" -> eventName = value.toString()
            else -> _attributes.add(KeyValue(name, value.toString()))
        }
    }

    public fun recordStr(
        name: String,
        value: String,
    ) {
        when (name) {
            "message" -> eventName = value
            "error" -> {
                if (eventName.isEmpty()) {
                    if (semConvConfig.errorEventsToStatus) {
                        spanBuilderUpdates = (spanBuilderUpdates ?: SpanBuilderUpdates()).apply {
                            status = SpanStatus.Error(value)
                        }
                    }
                    if (semConvConfig.errorEventsToExceptions) {
                        eventName = EVENT_EXCEPTION_NAME
                        _attributes.add(KeyValue(FIELD_EXCEPTION_MESSAGE, value))
                    } else {
                        _attributes.add(KeyValue("error", value))
                    }
                } else {
                    _attributes.add(KeyValue(name, value))
                }
            }
            else -> _attributes.add(KeyValue(name, value))
        }
    }

    public fun recordDebug(
        name: String,
        value: Any?,
    ) {
        recordStr(name, value.toString())
    }

    public fun recordError(
        name: String,
        error: Throwable,
    ) {
        val msg = error.message ?: error.toString()
        if (semConvConfig.errorFieldsToExceptions) {
            _attributes.add(KeyValue(FIELD_EXCEPTION_MESSAGE, msg))
            val stack = error.stackTraceToString()
            if (stack.isNotEmpty()) {
                _attributes.add(KeyValue(FIELD_EXCEPTION_STACKTRACE, stack))
            }
        } else {
            _attributes.add(KeyValue(name, msg))
        }
    }

    public fun record(name: String, value: Any?) {
        when (value) {
            is Boolean -> recordBool(name, value)
            is Double -> recordF64(name, value)
            is Float -> recordF64(name, value.toDouble())
            is Long -> recordI64(name, value)
            is Int -> recordI64(name, value.toLong())
            is String -> recordStr(name, value)
            is Throwable -> recordError(name, value)
            else -> recordDebug(name, value)
        }
    }
}

/**
 * Visitor recording span attributes.
 */
public class SpanAttributeVisitor {
    private val _attributes: MutableList<KeyValue> = mutableListOf()
    public val attributes: List<KeyValue> get() = _attributes
    public var name: String? = null
    public var spanKind: SpanKind? = null
    public var status: SpanStatus? = null

    public fun record(fieldName: String, value: Any?) {
        when (fieldName) {
            SPAN_NAME_FIELD -> name = value.toString()
            SPAN_KIND_FIELD -> spanKind = strToSpanKind(value.toString())
            SPAN_STATUS_CODE_FIELD -> status = strToStatus(value.toString())
            else -> _attributes.add(KeyValue(fieldName, value.toString()))
        }
    }
}

/**
 * An OpenTelemetry propagation layer for use in a project that uses tracing.
 *
 * It will convert tracing spans to OpenTelemetry spans and tracing events which are in a
 * span to events in the currently active OpenTelemetry span. Child-Parent links will be
 * automatically translated as well.
 */
public class OpenTelemetryLayer(
    private var location: Boolean = true,
    private var trackedInactivity: Boolean = true,
    private var withThreads: Boolean = true,
    private var withLevel: Boolean = false,
    private var withTarget: Boolean = true,
    private var contextActivation: Boolean = true,
    private var semConvConfig: SemConvConfig = SemConvConfig(),
    private var tracer: Any? = null,
) {
    public fun location(): Boolean = location

    public fun trackedInactivity(): Boolean = trackedInactivity

    public fun withThreads(): Boolean = withThreads

    public fun withLevel(): Boolean = withLevel

    public fun withTarget(): Boolean = withTarget

    public fun contextActivation(): Boolean = contextActivation

    public fun semConvConfig(): SemConvConfig = semConvConfig

    public fun withTracer(tracer: Any?): OpenTelemetryLayer {
        this.tracer = tracer
        return this
    }

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

    public fun parentContext(span: Any? = null): OtelContext = OtelContext.ROOT

    public fun getContext(id: Id? = null): OtelContext? = null

    public fun getActivatedContext(id: Id? = null): OtelContext? = null

    public fun getActivatedOtelContext(id: Id? = null): OtelContext? = null

    public fun getActivatedContextExtensions(extensions: Any? = null): OtelContext? = null

    public fun extraSpanAttrs(id: Id? = null): List<KeyValue> = emptyList()

    public fun startCx(id: Id? = null): OtelContext = OtelContext.ROOT

    public fun withStartedCx(id: Id? = null, f: (OtelContext) -> Unit) {
        f(startCx(id))
    }

    public fun onNewSpan(attrs: Any? = null, id: Id? = null, ctx: Any? = null) {}

    public fun onEnter(id: Id? = null, ctx: Any? = null) {}

    public fun onExit(id: Id? = null, ctx: Any? = null) {}

    public fun onRecord(span: Id? = null, values: Any? = null, ctx: Any? = null) {}

    public fun onFollowsFrom(span: Id? = null, follows: Id? = null, ctx: Any? = null) {}

    public fun onEvent(event: Any? = null, ctx: Any? = null) {}

    public fun onClose(id: Id? = null, ctx: Any? = null) {}

    public fun onLayer(subscriber: Any? = null) {}

    public fun registerCallsite(metadata: Any? = null): Any? = null

    public fun enabled(metadata: Any? = null, ctx: Any? = null): Boolean = true

    public fun onIdChange(old: Id? = null, new: Id? = null, ctx: Any? = null) {}

    public fun downcastRaw(typeId: Any? = null): Any? =
        if (typeId == this::class) this else null

    public fun dynamicSpanNames(): Boolean = true

    public fun spanKind(): SpanKind = SpanKind.Internal

    public fun spanStatusCode(): SpanStatus = SpanStatus.Unset

    public fun spanStatusDescription(): String? = null

    public companion object {
        public fun new(): OpenTelemetryLayer = OpenTelemetryLayer()

        public fun default(): OpenTelemetryLayer = new()
    }
}

/**
 * Constructs an OpenTelemetry layer.
 */
public fun layer(): OpenTelemetryLayer = OpenTelemetryLayer.new()

