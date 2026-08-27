// Span identifier value used as stack keys.
package io.github.kotlinmania.tracingopentelemetry

public class Id private constructor(
    public val value: ULong,
) {
    override fun equals(other: Any?): Boolean = this === other || (other is Id && value == other.value)

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "Id(value=$value)"

    public companion object {
        public fun fromU64(value: ULong): Id = Id(value)
    }
}
