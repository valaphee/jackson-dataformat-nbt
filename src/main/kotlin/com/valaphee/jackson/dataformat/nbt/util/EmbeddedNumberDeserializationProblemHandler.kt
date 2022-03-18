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

package com.valaphee.jackson.dataformat.nbt.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler

/**
 * @author Kevin Ludwig
 */
object EmbeddedNumberDeserializationProblemHandler : DeserializationProblemHandler() {
    override fun handleUnexpectedToken(context: DeserializationContext, targetType: JavaType, token: JsonToken, parser: JsonParser, message: String?): Any {
        if (token == JsonToken.VALUE_EMBEDDED_OBJECT) {
            val value = parser.currentValue
            if (value is Number) return value
        }
        return NOT_HANDLED
    }
}
