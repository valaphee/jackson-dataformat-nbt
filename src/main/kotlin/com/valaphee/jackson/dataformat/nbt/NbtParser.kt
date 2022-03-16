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
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.base.ParserBase
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.core.json.PackageVersion
import java.io.Closeable
import java.io.DataInput
import java.io.EOFException
import java.io.OutputStream
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Kevin Ludwig
 */
open class NbtParser(
    context: IOContext,
    features: Int,
    protected var _codec: ObjectCodec,
    protected val _input: DataInput,
    protected val _formatFeatures: Int
) : ParserBase(context, features) {
    protected class Context(
        val type: NbtType,
        var arrayState: Int = 1
    )

    protected var _contexts = mutableListOf<Context>().apply { if (NbtFactory.Feature.NoWrap.enabledIn(_formatFeatures)) add(Context(NbtType.Compound)) }
    protected var _objectStartState = false
    protected var _objectEndState = false

    protected var _numberFloat = 0.0f

    /*
     **********************************************************
     * Construction, configuration, initialization
     **********************************************************
     */

    override fun getCodec() = _codec

    override fun setCodec(codec: ObjectCodec) {
        this._codec = codec
    }

    /*
     **********************************************************
     * Versioned
     **********************************************************
     */

    override fun version(): Version = PackageVersion.VERSION

    /*
     **********************************************************
     * Public API, configuration
     **********************************************************
     */

    override fun getFormatFeatures() = _formatFeatures

    /*
     **********************************************************
     * Public API, traversal
     **********************************************************
     */

    override fun nextToken() = _nextToken().also { _currToken = it }

    private fun _nextToken(): JsonToken {
        _numTypesValid = NR_UNKNOWN
        _binaryValue = null
        currentValue = null

        if (_currToken == null && NbtFactory.Feature.NoWrap.enabledIn(_formatFeatures)) return JsonToken.START_OBJECT

        // For compounds to work properly, multiple runs are needed, therefore a state is required.
        if (!_objectStartState) when (_contexts.lastOrNull()?.type) {
            NbtType.Compound -> {
                val type = try {
                    NbtType.values()[_input.readByte().toInt()]
                } catch (_: EOFException) {
                    NbtType.End
                }
                if (type == NbtType.End) _objectEndState = true
                else {
                    _contexts += Context(type)
                    if (type == NbtType.Compound) _objectStartState = true
                    parsingContext.currentName = _input.readUTF()
                    return JsonToken.FIELD_NAME
                }
            }
            null -> {
                val type = NbtType.values()[_input.readByte().toInt()]
                if (type == NbtType.End) return JsonToken.VALUE_NULL
                else {
                    _contexts += Context(type)
                    parsingContext.currentName = _input.readUTF()
                }
            }
            else -> Unit
        }

        // Decrement tags, and remove when finished.
        _contexts.lastOrNull()?.let {
            if (it.arrayState == 0) {
                _contexts.removeLast()
                return when (it.type) {
                    /*NbtType.ByteArray, */NbtType.List, NbtType.IntArray, NbtType.LongArray -> JsonToken.END_ARRAY
                    NbtType.Compound -> {
                        _objectEndState = false
                        JsonToken.END_OBJECT
                    }
                    else -> _nextToken()
                }
            } else it.arrayState--
        }

        // When a list of compounds is present then a tag should not only end when there are no elements present in the list.
        if (_objectEndState) {
            _objectEndState = false
            return JsonToken.END_OBJECT
        }

        return when (val type = _contexts.lastOrNull()?.type) {
            NbtType.Byte -> {
                _numTypesValid = NR_INT
                _numberInt = _input.readByte().toInt()
                when (_numberInt) {
                    0 -> JsonToken.VALUE_FALSE
                    1 -> JsonToken.VALUE_TRUE
                    else -> JsonToken.VALUE_NUMBER_INT
                }
            }
            NbtType.Short -> {
                _numTypesValid = NR_INT
                _numberInt = _input.readShort().toInt()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Int -> {
                _numTypesValid = NR_INT
                _numberInt = _input.readInt()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Long -> {
                _numTypesValid = NR_LONG
                _numberLong = _input.readLong()
                JsonToken.VALUE_NUMBER_INT
            }
            NbtType.Float -> {
                _numTypesValid = NR_FLOAT
                _numberFloat = _input.readFloat()
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.Double -> {
                _numTypesValid = NR_DOUBLE
                _numberDouble = _input.readDouble()
                JsonToken.VALUE_NUMBER_FLOAT
            }
            NbtType.ByteArray -> {
                /*_contexts += Context(NbtType.Byte, _input.readInt())*/
                _binaryValue = ByteArray(_input.readInt())
                _input.readFully(_binaryValue)
                currentValue = _binaryValue
                JsonToken.VALUE_EMBEDDED_OBJECT
            }
            NbtType.String -> {
                currentValue = _input.readUTF()
                JsonToken.VALUE_STRING
            }
            NbtType.List -> {
                val listType = NbtType.values()[_input.readByte().toInt()]
                val length = _input.readInt()
                if (length > 0) {
                    if (listType == NbtType.Compound) _objectStartState = true
                    _contexts += Context(listType, length)
                }
                JsonToken.START_ARRAY
            }
            NbtType.Compound -> {
                _objectStartState = false
                JsonToken.START_OBJECT
            }
            NbtType.IntArray -> {
                _contexts += Context(NbtType.Int, _input.readInt())
                /*currentValue = IntArray(_input.readInt()) { _input.readInt() }*/
                JsonToken.START_ARRAY/*VALUE_EMBEDDED_OBJECT*/
            }
            NbtType.LongArray -> {
                _contexts += Context(NbtType.Long, _input.readInt())
                /*currentValue = LongArray(_input.readInt()) { _input.readLong() }*/
                JsonToken.START_ARRAY/*VALUE_EMBEDDED_OBJECT*/
            }
            else -> TODO("$type")
        }
    }

    /*
     **********************************************************
     * Public API, access to token information, text
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
     * Numeric accessors of public API
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
        _reportError("Current token (${currentToken()}) not numeric, can not use numeric value accessors")
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
     * Public API, access to token information, binary
     **********************************************************
     */

    override fun getBinaryValue(variant: Base64Variant): ByteArray {
        if (_currToken != JsonToken.VALUE_EMBEDDED_OBJECT) _reportError("Current token ($_currToken) not VALUE_EMBEDDED_OBJECT, can not access as binary")
        return _binaryValue
    }

    override fun getEmbeddedObject() = if (_currToken == JsonToken.VALUE_EMBEDDED_OBJECT) currentValue else null

    override fun readBinaryValue(variant: Base64Variant, stream: OutputStream): Int {
        if (_currToken != JsonToken.VALUE_EMBEDDED_OBJECT) _reportError("Current token ($_currToken) not VALUE_EMBEDDED_OBJECT, can not access as binary")

        return _binaryValue?.let {
            val length = _binaryValue.size
            stream.write(_binaryValue, 0, length)
            length
        } ?: 0
    }

    /*
     **********************************************************
     * Abstract methods for sub-classes to implement
     **********************************************************
     */

    override fun _closeInput() {
        if (_input is Closeable) _input.close()
    }
}
