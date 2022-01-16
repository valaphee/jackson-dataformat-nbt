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

import com.fasterxml.jackson.core.json.DupDetector
import com.fasterxml.jackson.core.json.JsonWriteContext

/**
 * @author Kevin Ludwig
 */
open class NbtWriteContext(
    type: Int,
    parent: NbtWriteContext?,
    dups: DupDetector?,
    protected val _generator: NbtGenerator
) : JsonWriteContext(type, parent, dups) {
    /*
     **********************************************************
     * Factory methods
     **********************************************************
     */

    override fun createChildArrayContext() = _child?.reset(TYPE_ARRAY) as NbtWriteContext? ?: let { NbtWriteContext(TYPE_ARRAY, this, _dups?.child(), _generator).also { _child = it } }

    override fun createChildObjectContext() = _child?.reset(TYPE_OBJECT) as NbtWriteContext? ?: let { NbtWriteContext(TYPE_OBJECT, this, _dups?.child(), _generator).also { _child = it } }

    /*
     **********************************************************
     * Write methods
     **********************************************************
     */

    fun writeValue(type: NbtType) {
        when (_type) {
            TYPE_ROOT -> {
                _generator._output.writeByte(type.ordinal)
                _generator._output.writeUTF("")
            }
            TYPE_ARRAY -> Unit
            TYPE_OBJECT -> {
                _generator._output.writeByte(type.ordinal)
                _generator._output.writeUTF(_currentName)
            }
            else -> TODO(typeDesc())
        }
    }

    fun writeEnd() {
        when (_type) {
            TYPE_ROOT, TYPE_OBJECT -> _generator._output.writeByte(NbtType.End.ordinal)
        }
    }

    companion object {
        fun createRootContext(dups: DupDetector?, generator: NbtGenerator) = NbtWriteContext(TYPE_ROOT, null, dups, generator)
    }
}
