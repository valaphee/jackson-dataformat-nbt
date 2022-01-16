 /*
 * Copyright (c) 2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.jackson.dataformat.nbt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Kevin Ludwig
 */
class NbtFactoryTest {
    @Test
    fun basicSelf() {
        data class BasicValue(
            val byte: Byte,
            val short: Short,
            val int: Int,
            val long: Long,
            val float: Float,
            val double: Double,
            val byteArray: ByteArray,
            val string: String,
            /*val list: List<Byte>,*/
            val compound: Map<String, Any>,
            val intArray: IntArray,
            val longArray: LongArray
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as BasicValue

                if (byte != other.byte) return false
                if (short != other.short) return false
                if (int != other.int) return false
                if (long != other.long) return false
                if (float != other.float) return false
                if (double != other.double) return false
                if (!byteArray.contentEquals(other.byteArray)) return false
                if (string != other.string) return false
                /*if (list != other.list) return false*/
                if (compound != other.compound) return false
                if (!intArray.contentEquals(other.intArray)) return false
                if (!longArray.contentEquals(other.longArray)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = byte.toInt()
                result = 31 * result + short
                result = 31 * result + int
                result = 31 * result + long.hashCode()
                result = 31 * result + float.hashCode()
                result = 31 * result + double.hashCode()
                result = 31 * result + byteArray.contentHashCode()
                result = 31 * result + string.hashCode()
                /*result = 31 * result + list.hashCode()*/
                result = 31 * result + compound.hashCode()
                result = 31 * result + intArray.contentHashCode()
                result = 31 * result + longArray.contentHashCode()
                return result
            }
        }

        val objectMapper = ObjectMapper(NbtFactory()).apply { registerKotlinModule() }
        val value = BasicValue(Byte.MAX_VALUE, Short.MAX_VALUE, Int.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE, byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte()), "Hello World"/*, listOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())*/, mapOf("Hello" to "World"), intArrayOf(0xDE, 0xAD, 0xBE, 0xEF), longArrayOf(0xDE, 0xAD, 0xBE, 0xEF))
        assertEquals(value, objectMapper.readValue<BasicValue>(objectMapper.writeValueAsBytes(value)))
    }

    @Test
    fun nestedSelf() {
        data class NestedValue(
            val value: String,
            val children: List<NestedValue>
        )

        val objectMapper = ObjectMapper(NbtFactory()).apply { registerKotlinModule() }
        val value = NestedValue("Hello", listOf(NestedValue("World", emptyList()), NestedValue("Hello2", listOf(NestedValue("World2", emptyList())))))
        assertEquals(value, objectMapper.readValue<NestedValue>(objectMapper.writeValueAsBytes(value)))
    }
}
