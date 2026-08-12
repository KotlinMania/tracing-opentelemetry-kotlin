// port-lint: source stack.rs
package io.github.kotlinmania.tracingopentelemetry

private data class IdValue<T>(
    val id: Id,
    val value: T,
)

internal class IdValueStack<T> {
    private val stack: MutableList<IdValue<T>> = mutableListOf()

    companion object {
        fun <T> new(): IdValueStack<T> = IdValueStack()
    }

    fun push(id: Id, value: T) {
        stack.add(IdValue(id, value))
    }

    fun pop(id: Id): T? {
        val index = stack.indexOfLast { ctxId -> ctxId.id == id }
        if (index >= 0) {
            val (_, value) = stack.removeAt(index)
            return value
        }
        return null
    }

    fun len(): Int = stack.size
}
