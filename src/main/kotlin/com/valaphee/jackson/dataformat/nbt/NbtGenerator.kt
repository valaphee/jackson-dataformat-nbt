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
import com.fasterxml.jackson.core.JsonStreamContext
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.base.GeneratorBase
import java.io.DataOutput
import java.io.Flushable
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Kevin Ludwig
 */
class NbtGenerator(
    features: Int,
    codec: ObjectCodec,
    private val output: DataOutput
) : GeneratorBase(features, codec) {
    private val outputContext get() = getOutputContext() as NbtWriteContext

    init {
        _writeContext = NbtWriteContext(JsonStreamContext.TYPE_ROOT, null, _writeContext.dupDetector, output)
    }

    override fun flush() {
        if (output is Flushable) output.flush()
    }

    override fun writeStartArray() {
        outputContext.writeEntry(NbtType.List)
        _writeContext = _writeContext.createChildArrayContext()
    }

    override fun writeEndArray() {
        outputContext.writeEnd()
        _writeContext = _writeContext.parent
    }

    override fun writeStartObject() {
        outputContext.writeEntry(NbtType.Compound)
        _writeContext = _writeContext.createChildObjectContext()
    }

    override fun writeEndObject() {
        outputContext.writeEnd()
        _writeContext = _writeContext.parent
    }

    override fun writeFieldName(name: String?) {
        _writeContext.writeFieldName(name)
    }

    override fun writeString(value: String) {
        outputContext.writeEntry(NbtType.String)
        output.writeUTF(value)
    }

    override fun writeString(chars: CharArray, offset: Int, length: Int) = writeString(String(chars, offset, length))

    override fun writeRawUTF8String(bytes: ByteArray, offset: Int, length: Int) = writeString(String(bytes, offset, length))

    override fun writeUTF8String(bytes: ByteArray, offset: Int, length: Int) = writeString(String(bytes, offset, length))

    override fun writeRaw(value: String) = writeString(value)

    override fun writeRaw(value: String, offset: Int, length: Int) = writeString(String(value.toCharArray(), offset, length))

    override fun writeRaw(chars: CharArray, offset: Int, length: Int) = writeString(String(chars, offset, length))

    override fun writeRaw(char: Char) = throw UnsupportedOperationException()

    override fun writeBinary(variant: Base64Variant, data: ByteArray, offset: Int, length: Int) = writeBinary(data.copyOfRange(offset, offset + length))

    override fun writeNumber(value: Int) {
        outputContext.writeEntry(NbtType.Int)
        output.writeInt(value)
    }

    override fun writeNumber(value: Long) {
        outputContext.writeEntry(NbtType.Long)
        output.writeLong(value)
    }

    override fun writeNumber(value: BigInteger) = TODO()

    override fun writeNumber(value: Double) {
        outputContext.writeEntry(NbtType.Double)
        output.writeDouble(value)
    }

    override fun writeNumber(value: Float) {
        outputContext.writeEntry(NbtType.Float)
        output.writeFloat(value)
    }

    override fun writeNumber(value: BigDecimal) = TODO()

    override fun writeNumber(value: String) = TODO()

    override fun writeBoolean(value: Boolean) {
        outputContext.writeEntry(NbtType.Byte)
        output.writeBoolean(value)
    }

    override fun writeNull() = Unit

    override fun _releaseBuffers() = Unit

    override fun _verifyValueWrite(typeMsg: String) = Unit
}
