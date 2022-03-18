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

package com.valaphee.jackson.dataformat.nbt.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.BasicSerializerFactory
import com.fasterxml.jackson.databind.ser.std.NumberSerializers
import com.valaphee.jackson.dataformat.nbt.NbtGenerator

/**
 * @author Kevin Ludwig
 */
@JacksonStdImpl
object ByteSerializer : NumberSerializers.Base<Any?>(Byte::class.java, JsonParser.NumberType.INT, "number") {
    init {
        @Suppress("UNCHECKED_CAST")
        (BasicSerializerFactory::class.java.getDeclaredField("_concrete").apply { isAccessible = true }.get(null) as MutableMap<String, JsonSerializer<*>>).apply {
            this[java.lang.Byte::class.java.name] = ByteSerializer
            this[java.lang.Byte.TYPE::class.java.name] = ByteSerializer
        }
    }

    override fun serialize(value: Any?, generator: JsonGenerator, provider: SerializerProvider) {
        if (generator is NbtGenerator) generator.writeNumber(value as Byte) else generator.writeNumber((value as Byte).toInt()) // See com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntLikeSerializer
    }
}
