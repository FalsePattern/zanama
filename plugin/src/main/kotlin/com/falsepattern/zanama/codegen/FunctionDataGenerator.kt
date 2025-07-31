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

package com.falsepattern.zanama.codegen

import com.falsepattern.zanama.model.Context
import com.falsepattern.zanama.model.NameSpace
import com.falsepattern.zanama.model.Named
import com.falsepattern.zanama.model.ZigType
import org.ainslec.picocog.PicoWriter
import javax.lang.model.SourceVersion

abstract class FunctionDataGenerator<FnType, ParamType>(val fn: FnType, val ctx: Context) {
    abstract val params: List<ParamType>
    abstract val returnType: ZigType?
    abstract val descriptorName: String
    abstract fun ParamType.registryName(ctx: Context): String
    abstract fun ParamType.javaType(ctx: Context): String
    abstract fun ParamType.name(): String?
    fun generateDescriptor(writer: PicoWriter, rootName: String): Unit = with(writer) {
        val voidReturn = returnType?.isVoid(ctx) != false
        val hasParams = params.isNotEmpty()
        write("FunctionDescriptor $descriptorName = FunctionDescriptor.")
        if (voidReturn) {
            if (hasParams) {
                writeln_r("ofVoid(")
            } else {
                writeln("ofVoid();")
                return
            }
        } else {
            writeln_r("of(")
            write("$rootName.${returnType!!.getRegistryName(ctx)}")
            if (hasParams) {
                writeln(",")
            } else {
                writeln("")
                writeln_l(");")
                return
            }
        }
        params.forEachIndexed { i, param ->
            write(rootName)
            write(".")
            if (i == params.size - 1) {
                writeln(param.registryName(ctx))
            } else {
                write(param.registryName(ctx))
                writeln(",")
            }
        }
        writeln_l(");")
    }

    fun genParams(writer: PicoWriter, withType: Boolean, hasBefore: Boolean = false): Unit = with(writer) {
        params.forEachIndexed { i, param ->
            val first = i == 0
            if (hasBefore or !first) {
                write(", ")
            }
            if (withType) {
                write("${param.javaType(ctx)} ${param.name() ?: "_x$i"}")
            } else {
                write(param.name() ?: "_x$i")
            }
        }
    }

    fun genReturnType(writer: PicoWriter): Unit = with(writer) {
        val voidReturn = returnType.isVoidOrNull(ctx)
        if (voidReturn) {
            write("void")
        } else {
            write(returnType!!.toJavaTypeStr(ctx))
        }
    }

    fun maybeGenReturnAndCast(writer: PicoWriter): Unit = with(writer) {
        val ret = returnType
        if (!ret.isVoidOrNull(ctx)) {
            write("return (${ret!!.toJavaTypeStr(ctx)}) ")
        }
    }

    class Fn(fn: com.falsepattern.zanama.model.ZigFn, ctx: Context) : FunctionDataGenerator<com.falsepattern.zanama.model.ZigFn, ZigType>(fn, ctx) {
        override val params get() = fn.params
        override val returnType get() = fn.return_type
        override val descriptorName get() = "\$DESC"

        override fun ZigType.registryName(ctx: Context): String {
            return getRegistryName(ctx)
        }

        override fun ZigType.javaType(ctx: Context): String {
            return toJavaTypeStr(ctx)
        }

        override fun ZigType.name(): String? {
            return null
        }
    }

    class NamedFn(fn: NameSpace.NamedFn, ctx: Context) : FunctionDataGenerator<NameSpace.NamedFn, Named<ZigType>>(fn, ctx) {
        override val params get() = fn.params
        override val returnType get() = fn.return_type
        override val descriptorName get() = "${fn.name}\$descriptor"

        override fun Named<ZigType>.registryName(ctx: Context): String {
            return "${value.getRegistryName(ctx)}.withName(\"$name\")"
        }

        override fun Named<ZigType>.javaType(ctx: Context): String {
            return value.toJavaTypeStr(ctx)
        }

        override fun Named<ZigType>.name(): String? {
            val n = name
            if (SourceVersion.isKeyword(n)) {
                return "$$n"
            }
            return n
        }
    }
}