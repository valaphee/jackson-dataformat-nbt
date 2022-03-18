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

@file:Suppress("NOTHING_TO_INLINE")

package com.valaphee.jackson.dataformat.nbt

inline fun Map<String, Any?>.getByteOrNull(key: String) = get(key)?.let { if (it is Boolean) if (it) 1 else 0 else (it as? Number)?.toByte() }

inline fun Map<String, Any?>.getBooleanOrNull(key: String) = get(key) as? Boolean

inline fun Map<String, Any?>.getShortOrNull(key: String) = (get(key) as? Number)?.toShort()

inline fun Map<String, Any?>.getIntOrNull(key: String) = (get(key) as? Number)?.toInt()

inline fun Map<String, Any?>.getLongOrNull(key: String) = (get(key) as? Number)?.toLong()

inline fun Map<String, Any?>.getFloatOrNull(key: String) = (get(key) as? Number)?.toFloat()

inline fun Map<String, Any?>.getDoubleOrNull(key: String) = (get(key) as? Number)?.toDouble()

inline fun Map<String, Any?>.getByteArrayOrNull(key: String) = (get(key) as? ByteArray)

inline fun Map<String, Any?>.getStringOrNull(key: String) = get(key) as? String

inline fun Map<String, Any?>.getListOrNull(key: String) = get(key) as? List<Any?>

@Suppress("UNCHECKED_CAST")
inline fun Map<String, Any?>.getMapOrNull(key: String) = get(key) as? Map<String, Any?>

inline fun Map<String, Any?>.getIntArrayOrNull(key: String) = get(key) as? IntArray

inline fun Map<String, Any?>.getLongArrayOrNull(key: String) = get(key) as? LongArray
