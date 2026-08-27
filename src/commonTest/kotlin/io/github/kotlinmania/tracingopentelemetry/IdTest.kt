// port-lint: tests span_ext.rs
package io.github.kotlinmania.tracingopentelemetry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IdTest {
    @Test
    fun testIdEqualityAndString() {
        val id1 = Id.fromU64(100u)
        val id2 = Id.fromU64(100u)
        val id3 = Id.fromU64(200u)

        assertEquals(id1, id2)
        assertNotEquals(id1, id3)
        assertEquals("Id(value=100)", id1.toString())
    }
}
