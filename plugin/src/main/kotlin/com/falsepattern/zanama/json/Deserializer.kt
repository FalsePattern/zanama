/*
 * Zanama
 *
 * Copyright (C) 2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.falsepattern.zanama.json

import com.falsepattern.zanama.model.*
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class Deserializer : JsonDeserializer<ZigType> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ZigType {
        if (json.isJsonPrimitive) {
            val str = json.asString
            return TypeRef(str)
        }
        val obj = json.asJsonObject
        check(obj.size() == 1) { "Zig union must be a single key object!" }

        val key = obj.keySet().iterator().next()
        val klass = registry[key]
        checkNotNull(klass) { "Unknown Zig type key \"$key\"" }
        val value = obj[key]!!
        return context.deserialize(value, klass)
    }

    companion object {
        private val registry: MutableMap<String, Class<out ZigType>> = HashMap()

        private fun add(klass: Class<out ZigType>) {
            registry[klass.simpleName.removePrefix("Zig")] = klass
        }

        init {
            add(ZigInt::class.java)
            add(ZigFloat::class.java)
            add(ZigOptional::class.java)
            add(ZigStruct::class.java)
            add(ZigArray::class.java)
            add(ZigUnion::class.java)
            add(ZigEnum::class.java)
            add(ZigPointer::class.java)
            add(ZigFn::class.java)
            add(ZigBool::class.java)
            add(ZigOpaque::class.java)
            add(ZigVoid::class.java)
        }
    }
}