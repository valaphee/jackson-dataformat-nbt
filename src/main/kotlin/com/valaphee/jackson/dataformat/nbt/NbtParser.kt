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

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.base.ParserBase
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.core.json.PackageVersion
import java.io.Closeable
import java.io.DataInput

/**
 * @author Kevin Ludwig
 */
class NbtParser(
    context: IOContext,
    features: Int,
    private var codec: ObjectCodec,
    private val input: DataInput
) : ParserBase(context, features) {
    private var type = mutableListOf<NbtType>()
    private var skip = false
    private var hold = -1

    /*
     **********************************************************
     * Life-cycle
     **********************************************************
     */

    override fun getCodec() = codec

    override fun setCodec(codec: ObjectCodec) {
        this.codec = codec
    }

    /*
     **********************************************************
     * Versioned
     **********************************************************
     */

    override fun version(): Version = PackageVersion.VERSION

    /*
     **********************************************************
     * JsonParser implementation: Text value access
     **********************************************************
     */

    override fun getText() = when (_currToken) {
        JsonToken.FIELD_NAME -> currentName
        else -> currentValue?.toString()
    }

    override fun getTextCharacters() = text?.toCharArray()

    override fun getTextLength() = text?.length ?: 0

    override fun getTextOffset() = 0

    /*
     **********************************************************
     * JsonParser implementation: Numeric value access
     **********************************************************
     */

    override fun getNumberValue() = currentValue as? Number

    /*
     **********************************************************
     * JsonParser implementation: traversal
     **********************************************************
     */

    override fun nextToken() = _nextToken().also { _currToken = it }

    private fun _nextToken(): JsonToken {
        if (!skip) when (type.lastOrNull()) {
            NbtType.Compound -> {
                val type = NbtType.values()[input.readByte().toInt()]
                return if (type != NbtType.End) {
                    this.type += type
                    if (type == NbtType.Compound) skip = true
                    parsingContext.currentName = input.readUTF()
                    JsonToken.FIELD_NAME
                } else {
                    this.type.removeLast()
                    JsonToken.END_OBJECT
                }
            }
            null -> {
                type += NbtType.values()[input.readByte().toInt()]
                parsingContext.currentName = input.readUTF()
            }
        }

        return when (val type = type.lastOrNull()) {
            NbtType.Byte -> {
                this.type.removeLast()
                currentValue = input.readByte()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Short -> {
                this.type.removeLast()
                currentValue = input.readShort()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Int -> {
                this.type.removeLast()
                currentValue = input.readInt()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Long -> {
                this.type.removeLast()
                currentValue = input.readLong()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Float -> {
                this.type.removeLast()
                currentValue = input.readFloat()
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.Double -> {
                this.type.removeLast()
                currentValue = input.readDouble()
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.ByteArray -> if (hold == -1) {
                hold = input.readInt()
                currentValue = null
                JsonToken.START_ARRAY
            } else if (hold-- == 0) {
                this.type.removeLast()
                JsonToken.END_ARRAY
            } else {
                currentValue = input.readByte()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.String -> {
                this.type.removeLast()
                currentValue = input.readUTF()
                JsonToken.VALUE_STRING
            }
            NbtType.Compound -> {
                skip = false
                currentValue = null
                JsonToken.START_OBJECT
            }
            NbtType.IntArray -> if (hold == -1) {
                hold = input.readInt()
                currentValue = null
                JsonToken.START_ARRAY
            } else if (hold-- == 0) {
                this.type.removeLast()
                JsonToken.END_ARRAY
            } else {
                currentValue = input.readInt()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.LongArray -> if (hold == -1) {
                hold = input.readInt()
                currentValue = null
                JsonToken.START_ARRAY
            } else if (hold-- == 0) {
                this.type.removeLast()
                JsonToken.END_ARRAY
            } else {
                currentValue = input.readLong()
                JsonToken.VALUE_NUMBER_INT
            }
            else -> TODO("$type")
        }
    }

    /*
     **********************************************************
     * Low-level reading, other
     **********************************************************
     */

    override fun _closeInput() {
        if (input is Closeable) input.close()
    }
}
