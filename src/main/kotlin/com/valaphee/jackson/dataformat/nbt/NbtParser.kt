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
    private var same = false

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
        if (!same) when (type.lastOrNull()) {
            NbtType.Compound -> {
                val type = NbtType.values()[input.readByte().toInt()]
                return if (type != NbtType.End) {
                    this.type += type
                    if (type == NbtType.Compound) same = true
                    parsingContext.currentName = input.readUTF()
                    println("FIELD_NAME $currentName ${this.type.joinToString()}")
                    JsonToken.FIELD_NAME
                } else JsonToken.END_OBJECT
            }
            null -> {
                type += NbtType.values()[input.readByte().toInt()]
                parsingContext.currentName = input.readUTF()
                println("INIT $currentName ${type.joinToString()}")
            }
        }

        return when (type.lastOrNull()) {
            NbtType.Byte -> {
                type.removeLast()
                currentValue = input.readByte()
                println("VALUE_NUMBER_INT")
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Short -> {
                type.removeLast()
                currentValue = input.readShort()
                println("VALUE_NUMBER_INT")
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Int -> {
                type.removeLast()
                currentValue = input.readInt()
                println("VALUE_NUMBER_INT")
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Long -> {
                type.removeLast()
                currentValue = input.readLong()
                println("VALUE_NUMBER_INT")
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Float -> {
                type.removeLast()
                currentValue = input.readFloat()
                println("VALUE_NUMBER_FLOAT")
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.Double -> {
                type.removeLast()
                currentValue = input.readDouble()
                println("VALUE_NUMBER_FLOAT")
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.String -> {
                type.removeLast()
                currentValue = input.readUTF()
                println("VALUE_STRING")
                JsonToken.VALUE_STRING
            }
            NbtType.List -> {
                same = false
                currentValue = null
                println("START_ARRAY")
                JsonToken.START_ARRAY
            }
            NbtType.Compound -> {
                same = false
                currentValue = null
                println("START_OBJECT")
                JsonToken.START_OBJECT
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
