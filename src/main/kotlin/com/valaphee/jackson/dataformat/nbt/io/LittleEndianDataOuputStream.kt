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
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/**
 * @author Kevin Ludwig
 */
open class LittleEndianDataOuputStream(
    private val stream: DataOutputStream
) : DataOutput by stream, Closeable by stream {
    constructor(stream: OutputStream) : this(DataOutputStream(stream))

    override fun writeShort(value: Int) = stream.writeShort(java.lang.Short.reverseBytes(value.toShort()).toInt())

    override fun writeChar(value: Int) = stream.writeChar(Character.reverseBytes(value.toChar()).code)

    override fun writeInt(value: Int) = stream.writeInt(Integer.reverseBytes(value))

    override fun writeLong(value: Long) = stream.writeLong(java.lang.Long.reverseBytes(value))

    override fun writeFloat(value: Float) = stream.writeInt(Integer.reverseBytes(java.lang.Float.floatToIntBits(value)))

    override fun writeDouble(value: Double) = stream.writeLong(java.lang.Long.reverseBytes(java.lang.Double.doubleToLongBits(value)))

    override fun writeUTF(value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        writeShort(bytes.size)
        write(bytes)
    }
}
