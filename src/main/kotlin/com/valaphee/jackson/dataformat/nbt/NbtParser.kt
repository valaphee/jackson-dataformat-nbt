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
import com.fasterxml.jackson.core.base.ParserBase
import com.fasterxml.jackson.core.io.IOContext
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
    override fun getCodec() = codec

    override fun setCodec(codec: ObjectCodec) {
        this.codec = codec
    }

    override fun nextToken(): JsonToken {
        TODO()
    }

    override fun getText(): String {
        TODO()
    }

    override fun getTextCharacters() = text.toCharArray()

    override fun getTextLength() = text.length

    override fun getTextOffset() = 0

    override fun _closeInput() {
        if (input is Closeable) input.close()
    }
}
