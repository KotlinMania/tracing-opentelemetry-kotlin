// port-lint: tests tracing-opentelemetry/src/stack.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.test.Test
import kotlin.test.assertEquals

private typealias IdStringStack = IdValueStack<String>

class StackTest {
    @Test
    fun popLastValue() {
        val stack: IdStringStack = IdValueStack.new()
        val id1 = Id.fromU64(4711uL)
        stack.push(id1, "one")
        val id2 = Id.fromU64(1729uL)
        stack.push(id2, "two")
        assertEquals(2, stack.len())

        assertEquals("two", stack.pop(id2))
        assertEquals(1, stack.len())
        assertEquals("one", stack.pop(id1))
        assertEquals(0, stack.len())
    }

    @Test
    fun popFirstValue() {
        val stack: IdStringStack = IdValueStack.new()
        val id1 = Id.fromU64(4711uL)
        stack.push(id1, "one")
        val id2 = Id.fromU64(1729uL)
        stack.push(id2, "two")

        assertEquals("one", stack.pop(id1))
        assertEquals(1, stack.len())
        assertEquals("two", stack.pop(id2))
        assertEquals(0, stack.len())
    }

    @Test
    fun popMiddleValue() {
        val stack: IdStringStack = IdValueStack.new()
        val id1 = Id.fromU64(4711uL)
        stack.push(id1, "one")
        val id2 = Id.fromU64(1729uL)
        stack.push(id2, "two")
        val id3 = Id.fromU64(1001uL)
        stack.push(id3, "three")

        assertEquals("three", stack.pop(id3))
        assertEquals(2, stack.len())
        assertEquals("two", stack.pop(id2))
        assertEquals(1, stack.len())
        assertEquals("one", stack.pop(id1))
        assertEquals(0, stack.len())
    }
}
