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

package com.valaphee.jackson.dataformat.nbt.io

import java.io.Closeable
import java.io.DataInput
import java.io.DataInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * @author Kevin Ludwig
 */
open class LittleEndianDataInputStream constructor(
    private val stream: DataInputStream,
) : DataInput by stream, Closeable by stream {
    constructor(stream: InputStream) : this(DataInputStream(stream))

    override fun readShort() = java.lang.Short.reverseBytes(stream.readShort())

    override fun readUnsignedShort() = java.lang.Short.toUnsignedInt(java.lang.Short.reverseBytes(stream.readShort()))

    override fun readChar() = Character.reverseBytes(stream.readChar())

    override fun readInt() = Integer.reverseBytes(stream.readInt())

    override fun readLong() = java.lang.Long.reverseBytes(stream.readLong())

    override fun readFloat() = java.lang.Float.intBitsToFloat(Integer.reverseBytes(stream.readInt()))

    override fun readDouble() = java.lang.Double.longBitsToDouble(java.lang.Long.reverseBytes(stream.readLong()))

    override fun readUTF(): String {
        val bytes = ByteArray(readUnsignedShort())
        readFully(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }
}
