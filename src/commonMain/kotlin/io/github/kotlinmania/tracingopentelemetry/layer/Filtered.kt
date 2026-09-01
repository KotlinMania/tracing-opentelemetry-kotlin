// port-lint: source layer/filtered.rs
package io.github.kotlinmania.tracingopentelemetry.layer

import io.github.kotlinmania.tracingopentelemetry.Id

/**
 * Event count tracker for filtered events.
 */
public data class EventCount(
    public var count: UInt = 0u,
)

/**
 * A layer wrapping a OpenTelemetryLayer, discarding all events filtered out by a given filter.
 *
 * Only events that are not filtered out will be saved as events on the span. All events, including
 * those filtered out, will be counted and the total will be provided in the tracing event count field.
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

    public fun onLayer(subscriber: Any? = null) {
        inner.onLayer(subscriber)
    }

    public fun registerCallsite(metadata: Any? = null): Any? =
        inner.registerCallsite(metadata)

    public fun enabled(metadata: Any? = null, ctx: Any? = null): Boolean =
        inner.enabled(metadata, ctx)

    public fun onNewSpan(attrs: Any? = null, id: Id? = null, ctx: Any? = null) {
        inner.onNewSpan(attrs, id, ctx)
    }

    public fun onRecord(span: Id? = null, values: Any? = null, ctx: Any? = null) {
        inner.onRecord(span, values, ctx)
    }

    public fun onFollowsFrom(span: Id? = null, follows: Id? = null, ctx: Any? = null) {
        inner.onFollowsFrom(span, follows, ctx)
    }

    public fun onEvent(event: Any? = null, ctx: Any? = null) {
        inner.onEvent(event, ctx)
    }

    public fun onEnter(id: Id? = null, ctx: Any? = null) {
        inner.onEnter(id, ctx)
    }

    public fun onExit(id: Id? = null, ctx: Any? = null) {
        inner.onExit(id, ctx)
    }

    public fun onClose(id: Id? = null, ctx: Any? = null) {
        inner.onClose(id, ctx)
    }

    public fun onIdChange(old: Id? = null, new: Id? = null, ctx: Any? = null) {
        inner.onIdChange(old, new, ctx)
    }

    public fun downcastRaw(typeId: Any? = null): Any? =
        if (typeId == this::class) this else inner.downcastRaw(typeId)

    public companion object {
        public fun <F> new(
            inner: OpenTelemetryLayer,
            filter: F,
        ): FilteredOpenTelemetryLayer<F> = FilteredOpenTelemetryLayer(inner, filter)
    }
}
