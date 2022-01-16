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

import java.io.DataOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/**
 * @author Kevin Ludwig
 */
class LittleEndianVarIntDataOutputStream : LittleEndianDataOuputStream {
    constructor(stream: DataOutputStream) : super(stream)

    constructor(stream: OutputStream) : super(stream)

    override fun writeInt(value: Int) {
        writeVarUInt((value shl 1) xor (value shr 31))
    }

    override fun writeLong(value: Long) {
        @Suppress("NAME_SHADOWING") var value = (value shl 1) xor (value shr 63)
        while (true) {
            if (value and 0x7FL.inv() == 0L) {
                writeByte(value.toInt())
                return
            } else {
                writeByte((value.toInt() and 0x7F) or 0x80)
                value = value ushr 7
            }
        }
    }

    override fun writeUTF(value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        writeVarUInt(bytes.size)
        write(bytes)
    }

    private fun writeVarUInt(value: Int) {
        @Suppress("NAME_SHADOWING") var value = value
        while (true) {
            if (value and 0x7F.inv() == 0) {
                writeByte(value)
                return
            } else {
                writeByte((value and 0x7F) or 0x80)
                value = value ushr 7
            }
        }
    }
}
