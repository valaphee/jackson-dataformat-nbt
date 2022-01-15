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
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.base.GeneratorBase
import com.fasterxml.jackson.core.json.PackageVersion
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
    internal val output: DataOutput
) : GeneratorBase(features, codec) {
    private val writeContext get() = _writeContext as NbtWriteContext

    init {
        _writeContext = NbtWriteContext.createRootContext(_writeContext.dupDetector, this)
    }

    /*
     **********************************************************
     * Versioned
     **********************************************************
     */

    override fun version(): Version = PackageVersion.VERSION

    /*
     **********************************************************
     * Capability introspection
     **********************************************************
     */

    /*
     **********************************************************
     * Overridden methods, configuration
     **********************************************************
     */

    override fun useDefaultPrettyPrinter() = this

    override fun setPrettyPrinter(prettyPrinter: PrettyPrinter) = this

    /*
     **********************************************************
     * Output method implementations, structural
     **********************************************************
     */

    override fun writeStartArray() {
        _verifyValueWrite("")

        writeContext.writeValue(NbtType.List, null)

        _writeContext = _writeContext.createChildArrayContext()
    }

    override fun writeEndArray() {
        writeContext.writeEnd()

        _writeContext = _writeContext.parent
    }

    override fun writeStartObject() {
        _verifyValueWrite("")

        writeContext.writeValue(NbtType.Compound, null)

        _writeContext = _writeContext.createChildObjectContext()
    }

    override fun writeEndObject() {
        writeContext.writeEnd()

        _writeContext = _writeContext.parent
    }

    override fun writeFieldName(name: String) {
        _writeContext.writeFieldName(name)
    }

    /*
     **********************************************************
     * Output method implementations, textual
     **********************************************************
     */

    override fun writeString(value: String) {
        _verifyValueWrite("")

        if (writeContext.writeValue(NbtType.String, value)) output.writeUTF(value)
    }

    override fun writeString(chars: CharArray, offset: Int, length: Int) = writeString(String(chars, offset, length))

    override fun writeRawUTF8String(bytes: ByteArray, offset: Int, length: Int) = writeString(String(bytes, offset, length))

    override fun writeUTF8String(bytes: ByteArray, offset: Int, length: Int) = writeString(String(bytes, offset, length))

    /*
     **********************************************************
     * Output method implementations, unprocessed ("raw")
     **********************************************************
     */

    override fun writeRaw(value: String) = writeString(value)

    override fun writeRaw(value: String, offset: Int, length: Int) = writeString(String(value.toCharArray(), offset, length))

    override fun writeRaw(chars: CharArray, offset: Int, length: Int) = writeString(String(chars, offset, length))

    override fun writeRaw(char: Char) = throw UnsupportedOperationException()

    /*
     **********************************************************
     * Output method implementations, base64-encoded binary
     **********************************************************
     */

    override fun writeBinary(variant: Base64Variant, data: ByteArray, offset: Int, length: Int) = writeBinary(data.copyOfRange(offset, offset + length))

    /*
     **********************************************************
     * Output method implementations, primitive
     **********************************************************
     */

    override fun writeBoolean(value: Boolean) {
        _verifyValueWrite("")

        if (writeContext.writeValue(NbtType.Byte, value)) output.writeBoolean(value)
    }

    override fun writeNull() = Unit

    override fun writeNumber(value: Int) {
        _verifyValueWrite("")

        if (writeContext.writeValue(NbtType.Int, value)) output.writeInt(value)
    }

    override fun writeNumber(value: Long) {
        _verifyValueWrite("")

        if (writeContext.writeValue(NbtType.Long, value)) output.writeLong(value)
    }

    override fun writeNumber(value: BigInteger) = TODO()

    override fun writeNumber(value: Double) {
        _verifyValueWrite("")

        if (writeContext.writeValue(NbtType.Double, value)) output.writeDouble(value)
    }

    override fun writeNumber(value: Float) {
        _verifyValueWrite("")

        if (writeContext.writeValue(NbtType.Float, value)) output.writeFloat(value)
    }

    override fun writeNumber(value: BigDecimal) = TODO()

    override fun writeNumber(value: String) = writeString(value)

    /*
     **********************************************************
     * Low-level output handling
     **********************************************************
     */

    override fun flush() {
        if (output is Flushable) output.flush()
    }

    /*
     **********************************************************
     * Internal methods, buffer handling
     **********************************************************
     */

    override fun _releaseBuffers() = Unit

    override fun _verifyValueWrite(typeMsg: String) {
        _writeContext.writeValue()
    }
}
