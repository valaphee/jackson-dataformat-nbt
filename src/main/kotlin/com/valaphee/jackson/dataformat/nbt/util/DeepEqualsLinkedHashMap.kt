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

import java.util.Objects

/**
 * @author Kevin Ludwig
 */
class DeepEqualsLinkedHashMap<K, V> : LinkedHashMap<K, V> {
    constructor(initialCapacity: Int, loadFactor: Float) : super(initialCapacity, loadFactor)

    constructor(initialCapacity: Int) : super(initialCapacity)

    constructor() : super()

    constructor(other: Map<out K, V>?) : super(other)

    constructor(initialCapacity: Int, loadFactor: Float, accessOrder: Boolean) : super(initialCapacity, loadFactor, accessOrder)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Map<*, *>) return false

        if (size != other.size) return false

        return all { Objects.deepEquals(it.value, other[it.key]) }
    }
}
