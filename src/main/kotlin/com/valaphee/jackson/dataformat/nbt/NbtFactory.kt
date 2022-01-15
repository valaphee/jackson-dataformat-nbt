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

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.format.InputAccessor
import com.fasterxml.jackson.core.format.MatchStrength
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.core.json.PackageVersion
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.net.URL

/**
 * @author Kevin Ludwig
 */
class NbtFactory : JsonFactory {
    constructor() : this(null)

    constructor(codec: ObjectCodec?) : super(codec)

    constructor(factory: NbtFactory, codec: ObjectCodec) : super(factory, codec)

    /*
     **********************************************************
     * Serializable overrides
     **********************************************************
     */

    override fun readResolve() = NbtFactory(this, _objectCodec)

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

    override fun canUseCharArrays() = false

    /*
     **********************************************************
     * Format detection functionality
     **********************************************************
     */

    override fun getFormatName() = "NBT"

    override fun hasFormat(accessor: InputAccessor) = MatchStrength.INCONCLUSIVE

    /*
     **********************************************************
     * Overridden parser factory methods
     **********************************************************
     */

    override fun createParser(file: File): NbtParser {
        val context = _createContext(_createContentReference(file), true)
        return _createParser(_decorate(FileInputStream(file), context), context)
    }

    override fun createParser(url: URL): NbtParser {
        val context = _createContext(_createContentReference(url), true)
        return _createParser(_decorate(_optimizedStreamFromURL(url), context), context)
    }

    override fun createParser(stream: InputStream): NbtParser {
        val context = _createContext(_createContentReference(stream), false)
        return _createParser(_decorate(stream, context), context)
    }

    override fun createParser(bytes: ByteArray): NbtParser {
        val context = _createContext(_createContentReference(bytes), true)
        _inputDecorator?.let { it.decorate(context, bytes, 0, bytes.size)?.let { _createParser(it, context) } }
        return _createParser(bytes, 0, bytes.size, context)
    }

    override fun createParser(bytes: ByteArray, offset: Int, length: Int): NbtParser {
        val context = _createContext(_createContentReference(bytes, offset, length), true)
        _inputDecorator?.let { it.decorate(context, bytes, offset, length)?.let { _createParser(it, context) } }
        return _createParser(bytes, offset, length, context)
    }

    /*
     **********************************************************
     * Overridden generator factory methods
     **********************************************************
     */

    override fun createGenerator(stream: OutputStream, encoding: JsonEncoding): NbtGenerator {
        val context = _createContext(_createContentReference(stream), false)
        return _createUTF8Generator(_decorate(stream, context), context)
    }

    override fun createGenerator(stream: OutputStream): NbtGenerator {
        val context = _createContext(_createContentReference(stream), false)
        return _createUTF8Generator(_decorate(stream, context), context)
    }

    /*
     **********************************************************
     * Overridden internal factory methods
     **********************************************************
     */

    override fun _createParser(stream: InputStream, context: IOContext) = NbtParser(context, _parserFeatures, _objectCodec, DataInputStream(stream))

    override fun _createParser(reader: Reader, context: IOContext): NbtParser = _nonByteSource()

    override fun _createParser(chars: CharArray, offset: Int, length: Int, context: IOContext, recyclable: Boolean): NbtParser = _nonByteSource()

    override fun _createParser(bytes: ByteArray, offset: Int, length: Int, context: IOContext) = NbtParser(context, _parserFeatures, _objectCodec, DataInputStream(ByteArrayInputStream(bytes, offset, length)))

    override fun _createGenerator(writer: Writer, context: IOContext): NbtGenerator = _nonByteTarget()

    override fun _createUTF8Generator(stream: OutputStream, context: IOContext) = NbtGenerator(_parserFeatures, _objectCodec, DataOutputStream(stream))

    override fun _createWriter(stream: OutputStream, encoding: JsonEncoding, context: IOContext): Writer = _nonByteTarget();

    private fun <T> _nonByteSource(): T = throw UnsupportedOperationException("Can not create parser for non-byte-based source")

    private fun <T> _nonByteTarget(): T = throw UnsupportedOperationException("Can not create generator for non-byte-based target")
}
