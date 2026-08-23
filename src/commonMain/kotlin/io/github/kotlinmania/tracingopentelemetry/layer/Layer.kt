// port-lint: source layer.rs
package io.github.kotlinmania.tracingopentelemetry.layer

/**
 * OpenTelemetry layer for tracing spans and events.
 */
public class OpenTelemetryLayer {
    private var location: Boolean = false
    private var trackedInactivity: Boolean = false
    private var withThreads: Boolean = true
    private var withLevel: Boolean = true
    private var withTarget: Boolean = true
    private var contextActivation: Boolean = true

    public fun withLocation(location: Boolean): OpenTelemetryLayer {
        this.location = location
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

    public companion object {
        public fun new(): OpenTelemetryLayer = OpenTelemetryLayer()
    }
}

/**
 * Constructs an OpenTelemetry layer.
 */
public fun layer(): OpenTelemetryLayer = OpenTelemetryLayer.new()
