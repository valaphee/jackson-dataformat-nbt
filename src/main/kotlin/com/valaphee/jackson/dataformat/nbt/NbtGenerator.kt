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
    private val output: DataOutput
) : GeneratorBase(features, codec) {
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
        output.writeByte(NbtType.List.ordinal)
        if (outputContext.inRoot()) output.writeUTF("") else if (outputContext.inObject()) output.writeUTF(outputContext.currentName)
        _writeContext = _writeContext.createChildArrayContext()
    }

    override fun writeEndArray() {
        _writeContext = _writeContext.parent
    }

    override fun writeStartObject() {
        output.writeByte(NbtType.Compound.ordinal)
        if (outputContext.inRoot()) output.writeUTF("") else if (outputContext.inObject()) output.writeUTF(outputContext.currentName)
        _writeContext = _writeContext.createChildObjectContext()
    }

    override fun writeEndObject() {
        output.writeByte(NbtType.End.ordinal)
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
        output.writeByte(NbtType.String.ordinal)
        if (outputContext.inRoot()) output.writeUTF("") else if (outputContext.inObject()) output.writeUTF(outputContext.currentName)
        output.writeUTF(value)
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
        output.writeByte(NbtType.Byte.ordinal)
        if (outputContext.inRoot()) output.writeUTF("") else if (outputContext.inObject()) output.writeUTF(outputContext.currentName)
        output.writeBoolean(value)
    }

    override fun writeNull() = Unit

    override fun writeNumber(value: Int) {
        output.writeByte(NbtType.Int.ordinal)
        if (outputContext.inRoot()) output.writeUTF("") else if (outputContext.inObject()) output.writeUTF(outputContext.currentName)
        output.writeInt(value)
    }

    override fun writeNumber(value: Long) {
        output.writeByte(NbtType.Long.ordinal)
        if (outputContext.inRoot()) output.writeUTF("") else if (outputContext.inObject()) output.writeUTF(outputContext.currentName)
        output.writeLong(value)
    }

    override fun writeNumber(value: BigInteger) = TODO()

    override fun writeNumber(value: Double) {
        output.writeByte(NbtType.Double.ordinal)
        if (outputContext.inRoot()) output.writeUTF("") else if (outputContext.inObject()) output.writeUTF(outputContext.currentName)
        output.writeDouble(value)
    }

    override fun writeNumber(value: Float) {
        output.writeByte(NbtType.Float.ordinal)
        if (outputContext.inRoot()) output.writeUTF("") else if (outputContext.inObject()) output.writeUTF(outputContext.currentName)
        output.writeFloat(value)
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

    override fun _verifyValueWrite(typeMsg: String) = Unit
}
