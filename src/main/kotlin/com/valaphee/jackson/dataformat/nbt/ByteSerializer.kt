package com.valaphee.jackson.dataformat.nbt

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.BasicSerializerFactory
import com.fasterxml.jackson.databind.ser.std.NumberSerializers

/**
 * @author Kevin Ludwig
 */
@JacksonStdImpl
object ByteSerializer : NumberSerializers.Base<Any?>(Byte::class.java, JsonParser.NumberType.INT, "number") {
    init {
        @Suppress("UNCHECKED_CAST")
        (BasicSerializerFactory::class.java.getDeclaredField("_concrete").apply { isAccessible = true }.get(null) as MutableMap<String, JsonSerializer<*>>).apply {
            this[Byte::class.java.name] = ByteSerializer
            this[java.lang.Byte.TYPE.name] = ByteSerializer
        }
    }

    override fun serialize(value: Any?, generator: JsonGenerator, provider: SerializerProvider) {
        if (generator is NbtGenerator) generator.writeNumber(value as Byte) else generator.writeNumber((value as Byte).toInt()) // See com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntLikeSerializer
    }
}
