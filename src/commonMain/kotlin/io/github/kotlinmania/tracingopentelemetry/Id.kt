// port-lint: ignore
// Span identifier value used as stack keys.
package io.github.kotlinmania.tracingopentelemetry

internal class Id private constructor(
    val value: ULong,
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (other is Id && value == other.value)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "Id(value=$value)"
    }

    companion object {
        fun fromU64(value: ULong): Id = Id(value)
    }
}
