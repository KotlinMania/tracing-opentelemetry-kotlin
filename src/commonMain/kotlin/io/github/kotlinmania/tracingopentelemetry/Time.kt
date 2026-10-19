package io.github.kotlinmania.tracingopentelemetry

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Time utility module for timestamp generation.
 */
public object TracingOpentelemetryTime {
    /**
     * Gets current instant timestamp.
     */
    public fun now(): Instant = Clock.System.now()
}

/**
 * Returns current timestamp.
 */
public fun now(): Instant = TracingOpentelemetryTime.now()
