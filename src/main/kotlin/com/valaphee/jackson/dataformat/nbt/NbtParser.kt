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
import java.math.BigDecimal
import java.math.BigInteger

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

    private var _numberFloat = 0.0f

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
        JsonToken.VALUE_NUMBER_INT, JsonToken.VALUE_NUMBER_FLOAT -> numberValue.toString()
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

    override fun isNaN(): Boolean {
        return if (_currToken == JsonToken.VALUE_NUMBER_FLOAT)
            if (_numTypesValid and NR_DOUBLE != 0) _numberDouble.isNaN() || _numberDouble.isInfinite()
            else if (_numTypesValid and NR_FLOAT != 0) _numberFloat.isNaN() || _numberFloat.isInfinite()
            else false
        else false
    }

    override fun getNumberValue(): Number {
        if (_numTypesValid == NR_UNKNOWN) _checkNumericValue(NR_UNKNOWN)
        return if (_currToken == JsonToken.VALUE_NUMBER_INT)
            if (_numTypesValid and NR_INT != 0) _numberInt
            else if (_numTypesValid and NR_LONG != 0) _numberLong
            else if (_numTypesValid and NR_BIGINT != 0) _numberBigInt
            else _numberBigDecimal
        else if (_numTypesValid and NR_BIGDECIMAL != 0) _numberBigDecimal
        else if (_numTypesValid and NR_DOUBLE != 0) _numberDouble
        else _numberFloat
    }

    override fun getNumberValueExact() = numberValue

    override fun getNumberType(): NumberType {
        if (_numTypesValid == NR_UNKNOWN) _checkNumericValue(NR_UNKNOWN)
        return if (_currToken == JsonToken.VALUE_NUMBER_INT)
            if (_numTypesValid and NR_INT != 0) NumberType.INT
            else if (_numTypesValid and NR_LONG != 0) NumberType.LONG
            else NumberType.BIG_INTEGER
        else if (_numTypesValid and NR_BIGDECIMAL != 0) NumberType.BIG_DECIMAL
        else if (_numTypesValid and NR_DOUBLE != 0) NumberType.DOUBLE
        else NumberType.FLOAT
    }

    override fun getIntValue(): Int {
        if (_numTypesValid and NR_INT == 0) {
            if (_numTypesValid == NR_UNKNOWN) _checkNumericValue(NR_INT)
            if (_numTypesValid and NR_INT == 0) convertNumberToInt()
        }
        return _numberInt
    }

    override fun getLongValue(): Long {
        if (_numTypesValid and NR_LONG == 0) {
            if (_numTypesValid == NR_UNKNOWN) _checkNumericValue(NR_LONG)
            if (_numTypesValid and NR_LONG == 0) convertNumberToLong()
        }
        return _numberLong
    }

    override fun getBigIntegerValue(): BigInteger? {
        if (_numTypesValid and NR_BIGINT == 0) {
            if (_numTypesValid == NR_UNKNOWN) _checkNumericValue(NR_BIGINT)
            if (_numTypesValid and NR_BIGINT == 0) convertNumberToBigInteger()
        }
        return _numberBigInt
    }

    override fun getFloatValue(): Float {
        if (_numTypesValid and NR_FLOAT == 0) {
            if (_numTypesValid == NR_UNKNOWN) _checkNumericValue(NR_FLOAT)
            if (_numTypesValid and NR_FLOAT == 0) convertNumberToFloat()
        }
        return _numberFloat
    }

    override fun getDoubleValue(): Double {
        if (_numTypesValid and NR_DOUBLE == 0) {
            if (_numTypesValid == NR_UNKNOWN) _checkNumericValue(NR_DOUBLE)
            if (_numTypesValid and NR_DOUBLE == 0) convertNumberToDouble()
        }
        return _numberDouble
    }

    override fun getDecimalValue(): BigDecimal? {
        if (_numTypesValid and NR_BIGDECIMAL == 0) {
            if (_numTypesValid == NR_UNKNOWN) _checkNumericValue(NR_BIGDECIMAL)
            if (_numTypesValid and NR_BIGDECIMAL == 0) convertNumberToBigDecimal()
        }
        return _numberBigDecimal
    }

    /*
     **********************************************************
     * Numeric conversions
     **********************************************************
     */

    private fun _checkNumericValue(expType: Int) {
        if (_currToken == JsonToken.VALUE_NUMBER_INT || _currToken == JsonToken.VALUE_NUMBER_FLOAT) return
        _reportError("Current token (${currentToken()}) not numeric, can not use numeric value accessors");
    }

    private fun convertNumberToFloat() {
        if (_numTypesValid and NR_BIGDECIMAL != 0) _numberFloat = _numberBigDecimal.toFloat()
        else if (_numTypesValid and NR_BIGINT != 0) _numberFloat = _numberBigInt.toFloat()
        else if (_numTypesValid and NR_DOUBLE != 0) _numberFloat = _numberDouble.toFloat()
        else if (_numTypesValid and NR_LONG != 0) _numberFloat = _numberLong.toFloat()
        else if (_numTypesValid and NR_INT != 0) _numberFloat = _numberInt.toFloat()
        else _throwInternal()
        _numTypesValid = _numTypesValid or NR_FLOAT
    }

    /*
     **********************************************************
     * JsonParser implementation: traversal
     **********************************************************
     */

    override fun nextToken() = _nextToken().also { _currToken = it }

    private fun _nextToken(): JsonToken {
        _numTypesValid = NR_UNKNOWN;
        currentValue = null

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
                _numTypesValid = NR_INT
                _numberInt = input.readByte().toInt()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Short -> {
                _numTypesValid = NR_INT
                _numberInt = input.readShort().toInt()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Int -> {
                _numTypesValid = NR_INT
                _numberInt = input.readInt()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Long -> {
                _numTypesValid = NR_LONG
                _numberLong = input.readLong()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Float -> {
                _numTypesValid = NR_FLOAT
                _numberFloat = input.readFloat()
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.Double -> {
                _numTypesValid = NR_DOUBLE
                _numberDouble = input.readDouble()
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.ByteArray -> {
                contexts += Context(NbtType.Byte, input.readInt())
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
                JsonToken.START_ARRAY
            }
            NbtType.Compound -> {
                objectState = false
                JsonToken.START_OBJECT
            }
            NbtType.IntArray -> {
                contexts += Context(NbtType.Int, input.readInt())
                JsonToken.START_ARRAY
            }
            NbtType.LongArray -> {
                contexts += Context(NbtType.Long, input.readInt())
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
