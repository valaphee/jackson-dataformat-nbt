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

import com.fasterxml.jackson.core.Base64Variant
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.PrettyPrinter
import com.fasterxml.jackson.core.SerializableString
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.base.GeneratorBase
import com.fasterxml.jackson.core.json.JsonWriteContext
import com.fasterxml.jackson.core.json.PackageVersion
import com.valaphee.jackson.dataformat.nbt.util.ByteSerializer
import java.io.Closeable
import java.io.DataOutput
import java.io.Flushable
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Kevin Ludwig
 */
open class NbtGenerator(
    features: Int,
    codec: ObjectCodec,
    internal val _output: DataOutput,
    protected val _formatFeatures: Int
) : GeneratorBase(features, codec) {
    protected val _nbtWriteContext get() = _writeContext as NbtWriteContext

    init {
        _writeContext = NbtWriteContext.createRootContext(_writeContext.dupDetector, this)
    }

    /*
     **********************************************************
     * Construction, initialization
     **********************************************************
     */

    override fun version(): Version = PackageVersion.VERSION

    /*
     **********************************************************
     * Public API, Feature configuration
     **********************************************************
     */

    override fun getFormatFeatures() = _formatFeatures

    /*
     **********************************************************
     * Public API, other configuration
     **********************************************************
     */

    override fun setPrettyPrinter(prettyPrinter: PrettyPrinter) = this

    override fun useDefaultPrettyPrinter() = this

    /*
     **********************************************************
     * Public API, write methods, structural
     **********************************************************
     */

    override fun writeStartArray() = _reportUnsupportedOperation()

    override fun writeStartArray(forValue: Any, size: Int) {
        _verifyValueWrite("start of array")

        _nbtWriteContext.writeValue(NbtType.List)
        _writeContext = _writeContext.createChildArrayContext()

        when (forValue) {
            is BooleanArray -> {
                _output.writeByte(NbtType.Byte.ordinal)
                _output.writeInt(forValue.size)
            }
            is ByteArray -> {
                _output.writeByte(NbtType.Byte.ordinal)
                _output.writeInt(forValue.size)
            }
            is ShortArray -> {
                _output.writeByte(NbtType.Short.ordinal)
                _output.writeInt(forValue.size)
            }
            is IntArray -> {
                _output.writeByte(NbtType.Int.ordinal)
                _output.writeInt(forValue.size)
            }
            is LongArray -> {
                _output.writeByte(NbtType.Long.ordinal)
                _output.writeInt(forValue.size)
            }
            is FloatArray -> {
                _output.writeByte(NbtType.Float.ordinal)
                _output.writeInt(forValue.size)
            }
            is DoubleArray -> {
                _output.writeByte(NbtType.Double.ordinal)
                _output.writeInt(forValue.size)
            }
            is List<*> -> {
                val type = when (forValue.firstOrNull()) {
                    is Boolean -> NbtType.Byte
                    is Byte -> NbtType.Byte
                    is Short -> NbtType.Short
                    is Int -> NbtType.Int
                    is Long -> NbtType.Long
                    is Float -> NbtType.Float
                    is Double -> NbtType.Double
                    is ByteArray -> NbtType.ByteArray
                    is String -> NbtType.String
                    is List<*> -> NbtType.List
                    is Map<*, *> -> NbtType.Compound
                    is IntArray -> NbtType.IntArray
                    is LongArray -> NbtType.LongArray
                    null -> NbtType.End
                    else -> NbtType.Compound
                }
                _nbtWriteContext.nbtType = type

                _output.writeByte(type.ordinal)
                _output.writeInt(forValue.size)
            }
        }

    }

    override fun writeEndArray() {
        if (!_writeContext.inArray()) _reportError("Not an array: ${_writeContext.typeDesc()}");

        _nbtWriteContext.writeEnd()

        _writeContext = _writeContext.parent
    }

    override fun writeStartObject() {
        _verifyValueWrite("start of object")

        _nbtWriteContext.writeValue(NbtType.Compound)

        _writeContext = _writeContext.createChildObjectContext()
    }

    override fun writeEndObject() {
        if (!_writeContext.inObject()) _reportError("Not an object: ${_writeContext.typeDesc()}");

        if (!(_writeContext.parent.inRoot() && NbtFactory.Feature.NoWrap.enabledIn(_formatFeatures))) _nbtWriteContext.writeEnd()

        _writeContext = _writeContext.parent
    }

    override fun writeFieldName(name: String) {
        _writeContext.writeFieldName(name)
    }

    /*
     **********************************************************
     * Public API, write methods, scalar arrays (2.8)
     **********************************************************
     */

    override fun writeArray(array: IntArray, offset: Int, length: Int) {
        _verifyValueWrite("write int array")

        val array = array.copyOfRange(offset, offset + length);
        _nbtWriteContext.writeValue(NbtType.IntArray)
        _output.writeInt(array.size)
        array.forEach(_output::writeInt)
    }

    override fun writeArray(array: LongArray, offset: Int, length: Int) {
        _verifyValueWrite("write long array")

        val array = array.copyOfRange(offset, offset + length);
        _nbtWriteContext.writeValue(NbtType.LongArray)
        _output.writeInt(array.size)
        array.forEach(_output::writeLong)
    }

    /*
     **********************************************************
     * Public API, write methods, text/String values
     **********************************************************
     */

    override fun writeString(value: String) {
        _verifyValueWrite("write string")

        _nbtWriteContext.writeValue(NbtType.String)
        _output.writeUTF(value)
    }

    override fun writeString(chars: CharArray, offset: Int, length: Int) = writeString(String(chars, offset, length))

    override fun writeRawUTF8String(bytes: ByteArray, offset: Int, length: Int) = writeString(String(bytes, offset, length))

    override fun writeUTF8String(bytes: ByteArray, offset: Int, length: Int) = writeString(String(bytes, offset, length))

    /*
     **********************************************************
     * Public API, write methods, binary/raw content
     **********************************************************
     */

    override fun writeRaw(value: String) = _reportUnsupportedOperation();

    override fun writeRaw(value: String, offset: Int, length: Int) = _reportUnsupportedOperation()

    override fun writeRaw(chars: CharArray, offset: Int, length: Int) = _reportUnsupportedOperation()

    override fun writeRaw(char: Char) = _reportUnsupportedOperation()

    override fun writeRawValue(value: String) = _reportUnsupportedOperation()

    override fun writeRawValue(value: String, offset: Int, length: Int) = _reportUnsupportedOperation()

    override fun writeRawValue(chars: CharArray, offset: Int, length: Int) = _reportUnsupportedOperation()

    override fun writeRawValue(value: SerializableString) = _reportUnsupportedOperation()

    override fun writeBinary(variant: Base64Variant, data: ByteArray, offset: Int, length: Int) = writeBinary(data, offset, length)

    override fun writeBinary(data: ByteArray, offset: Int, length: Int) {
        _verifyValueWrite("write byte array")

        _nbtWriteContext.writeValue(NbtType.ByteArray)
        _output.writeInt(length)
        _output.write(data, offset, length)
    }

    override fun writeBinary(data: ByteArray) = writeBinary(data, 0, data.size)

    /*
     **********************************************************
     * Public API, write methods, numeric
     **********************************************************
     */

    fun writeNumber(value: Byte) {
        _verifyValueWrite("write byte")

        _nbtWriteContext.writeValue(NbtType.Byte)
        _output.writeByte(value.toInt())
    }

    override fun writeNumber(value: Short) {
        _verifyValueWrite("write short")

        _nbtWriteContext.writeValue(NbtType.Short)
        _output.writeShort(value.toInt())
    }

    override fun writeNumber(value: Int) {
        _verifyValueWrite("write int")

        _nbtWriteContext.writeValue(NbtType.Int)
        _output.writeInt(value)
    }

    override fun writeNumber(value: Long) {
        _verifyValueWrite("write long")

        _nbtWriteContext.writeValue(NbtType.Long)
        _output.writeLong(value)
    }

    override fun writeNumber(value: BigInteger) = _reportUnsupportedOperation()

    override fun writeNumber(value: Double) {
        _verifyValueWrite("write double")

        _nbtWriteContext.writeValue(NbtType.Double)
        _output.writeDouble(value)
    }

    override fun writeNumber(value: Float) {
        _verifyValueWrite("write float")

        _nbtWriteContext.writeValue(NbtType.Float)
        _output.writeFloat(value)
    }

    override fun writeNumber(value: BigDecimal) = _reportUnsupportedOperation()

    override fun writeNumber(value: String) = writeString(value)

    /*
     **********************************************************
     * Public API, write methods, other value types
     **********************************************************
     */

    override fun writeBoolean(value: Boolean) {
        _verifyValueWrite("write boolean")

        _nbtWriteContext.writeValue(NbtType.Byte)
        _output.writeBoolean(value)
    }

    override fun writeNull() {
        _verifyValueWrite("write null")

        if (_writeContext.inRoot()) _output.writeByte(NbtType.End.ordinal)
    }

    /*
     **********************************************************
     * Public API, buffer handling
     **********************************************************
     */

    override fun flush() {
        if (_output is Flushable) _output.flush()
    }

    /*
     **********************************************************
     * Closeable implementation
     **********************************************************
     */

    override fun close() {
        if (isClosed) return

        super.close()
        flush()
        if (_output is Closeable) _output.close()
    }

    /*
     **********************************************************
     * Package methods for this, sub-classes
     **********************************************************
     */

    override fun _releaseBuffers() = Unit

    override fun _verifyValueWrite(typeMsg: String) {
        if (_writeContext.writeValue() == JsonWriteContext.STATUS_EXPECT_NAME) _reportError("Can not $typeMsg, expecting field name.")
    }

    companion object {
        init {
            ByteSerializer
        }
    }
}
