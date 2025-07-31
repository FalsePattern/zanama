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

package com.falsepattern.zanama

import com.falsepattern.zanama.codegen.RootClassGenerator
import com.falsepattern.zanama.codegen.StructClassGenerator
import com.falsepattern.zanama.json.Deserializer
import com.falsepattern.zanama.json.NativeData
import com.falsepattern.zanama.model.Context
import com.falsepattern.zanama.model.NameSpaced
import com.falsepattern.zanama.model.ZigType
import com.falsepattern.zanama.model.tryLet
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

object Translator {
    fun translate(rootPkg: String, bindRoot: String, json: Path, output: Path, className: String) {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(ZigType::class.java, Deserializer())
        builder.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        val gson = builder.create()
        val data = gson.fromJson(json.readText(), NativeData::class.java)
        val ctx = Context(data.types, HashMap(data.types))
        val bindings = LinkedHashMap(data.bindings)
        for ((alias, type) in bindings) {
            type.tryLet<NameSpaced>(ctx) {
                it.aliasGen(ctx, alias)
            }
        }
        for ((name, type) in data.types) {
            type.tryLet<NameSpaced>(ctx) {
                it.name = name
                if (it.alias == null) {
                    val theAlias = "$bindRoot.$name$${it.variantName.lowercase()}"
                    bindings[theAlias] = it
                }
            }
        }
        RootClassGenerator.generate(rootPkg, className, ctx).let { out ->
            output.resolve(Path(".", *rootPkg.split(".").toTypedArray())).let { dir ->
                dir.createDirectories()
                dir.resolve("$className.java").writeText(out.code)
            }
        }
        for ((name, type) in bindings) {
            val out = StructClassGenerator.generate(name, type, bindRoot, rootPkg, className, ctx)
            output.resolve(Path(".", *out.pkg.split(".").toTypedArray())).let { dir ->
                dir.createDirectories()
                dir.resolve("${out.simpleName}.java").writeText(out.code)
            }
        }
    }
}