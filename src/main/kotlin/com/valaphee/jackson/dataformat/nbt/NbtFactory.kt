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
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.format.InputAccessor
import com.fasterxml.jackson.core.format.MatchStrength
import com.fasterxml.jackson.core.json.PackageVersion
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.net.URL

/**
 * @author Kevin Ludwig
 */
class NbtFactory : JsonFactory() {
    override fun readResolve() = NbtFactory()

    override fun version(): Version = PackageVersion.VERSION

    override fun getFormatName() = "NBT"

    override fun hasFormat(accessor: InputAccessor) = MatchStrength.INCONCLUSIVE

    override fun canUseCharArrays() = false

    override fun createParser(file: File) = NbtParser(_createContext(_createContentReference(file), true), _parserFeatures, _objectCodec, DataInputStream(FileInputStream(file)))

    override fun createParser(url: URL) = NbtParser(_createContext(_createContentReference(url), true), _parserFeatures, _objectCodec, DataInputStream(_optimizedStreamFromURL(url)))

    override fun createParser(stream: InputStream) = NbtParser(_createContext(_createContentReference(stream), false), _parserFeatures, _objectCodec, DataInputStream(stream))

    override fun createParser(reader: Reader) = TODO()

    override fun createParser(bytes: ByteArray) = createParser(bytes, 0, bytes.size)

    override fun createParser(bytes: ByteArray, offset: Int, length: Int) = NbtParser(_createContext(_createContentReference(bytes), true), _parserFeatures, _objectCodec, DataInputStream(ByteArrayInputStream(bytes, offset, length)))

    override fun createParser(content: String) = TODO()

    override fun createParser(chars: CharArray) = TODO()

    override fun createParser(chars: CharArray, offset: Int, length: Int) = TODO()

    override fun createGenerator(stream: OutputStream, encoding: JsonEncoding) = createGenerator(stream)

    override fun createGenerator(stream: OutputStream) = NbtGenerator(_generatorFeatures, _objectCodec, DataOutputStream(stream))

    override fun createGenerator(writer: Writer) = TODO()

    override fun createGenerator(file: File, encoding: JsonEncoding?) = NbtGenerator(_generatorFeatures, _objectCodec, DataOutputStream(FileOutputStream(file)))
}
