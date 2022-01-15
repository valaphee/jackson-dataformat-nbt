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
    private var contexts = mutableListOf<Context>()
    private var objectState = false

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
        if (!objectState) when (contexts.lastOrNull()?.type) {
            NbtType.Compound -> {
                val type = NbtType.values()[input.readByte().toInt()]
                if (type != NbtType.End) {
                    this.contexts += Context(type)
                    if (type == NbtType.Compound) objectState = true
                    parsingContext.currentName = input.readUTF()
                    return JsonToken.FIELD_NAME
                }
            }
            null -> {
                contexts += Context(NbtType.values()[input.readByte().toInt()])
                parsingContext.currentName = input.readUTF()
            }
        }

        contexts.lastOrNull()?.let {
            if (it.arrayState == 0) {
                contexts.removeLast()
                return when (it.type) {
                    NbtType.ByteArray, NbtType.List, NbtType.IntArray, NbtType.LongArray -> JsonToken.END_ARRAY
                    NbtType.Compound -> JsonToken.END_OBJECT
                    else -> _nextToken()
                }
            } else it.arrayState--
        }

        return when (val type = contexts.lastOrNull()?.type) {
            NbtType.Byte -> {
                currentValue = input.readByte()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Short -> {
                currentValue = input.readShort()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Int -> {
                currentValue = input.readInt()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Long -> {
                currentValue = input.readLong()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Float -> {
                currentValue = input.readFloat()
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.Double -> {
                currentValue = input.readDouble()
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.ByteArray -> {
                contexts += Context(NbtType.Byte, input.readInt())
                currentValue = null
                JsonToken.START_ARRAY
            }
            NbtType.String -> {
                currentValue = input.readUTF()
                JsonToken.VALUE_STRING
            }
            NbtType.List -> {
                val type = NbtType.values()[input.readByte().toInt()]
                contexts += Context(type, input.readInt())
                if (type == NbtType.Compound) objectState = true
                currentValue = null
                JsonToken.START_ARRAY
            }
            NbtType.Compound -> {
                objectState = false
                currentValue = null
                JsonToken.START_OBJECT
            }
            NbtType.IntArray -> {
                contexts += Context(NbtType.Int, input.readInt())
                currentValue = null
                JsonToken.START_ARRAY
            }
            NbtType.LongArray -> {
                contexts += Context(NbtType.Long, input.readInt())
                currentValue = null
                JsonToken.START_ARRAY
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

    private class Context(
        val type: NbtType,
        var arrayState: Int = 1
    )
}
