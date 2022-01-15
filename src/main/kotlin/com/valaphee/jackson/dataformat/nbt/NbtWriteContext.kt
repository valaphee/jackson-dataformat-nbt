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

import com.fasterxml.jackson.core.JsonStreamContext
import com.fasterxml.jackson.core.json.DupDetector
import com.fasterxml.jackson.core.json.JsonWriteContext
import java.io.DataOutput

/**
 * @author Kevin Ludwig
 */
class NbtWriteContext(
    type: Int,
    parent: NbtWriteContext?,
    dups: DupDetector?,
    private val output: DataOutput
) : JsonWriteContext(type, parent, dups) {
    override fun reset(type: Int, value: Any) = TODO()

    override fun createChildArrayContext() = _child?.reset(JsonStreamContext.TYPE_ARRAY) ?: let { NbtWriteContext(TYPE_ARRAY, this, _dups?.child(), output).also { _child = it } }

    override fun createChildArrayContext(value: Any) = TODO()

    override fun createChildObjectContext() = _child?.reset(JsonStreamContext.TYPE_OBJECT) ?: let { NbtWriteContext(TYPE_OBJECT, this, _dups?.child(), output).also { _child = it } }

    override fun createChildObjectContext(value: Any) = TODO()

    fun writeEntry(type: NbtType) {
        output.writeByte(type.ordinal)
        output.writeUTF(_currentName ?: "")
    }

    fun writeEnd() {
        output.writeByte(NbtType.End.ordinal)
    }
}
