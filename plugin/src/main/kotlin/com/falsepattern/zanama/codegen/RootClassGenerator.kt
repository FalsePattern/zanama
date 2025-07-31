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

import com.falsepattern.zanama.model.*
import org.ainslec.picocog.PicoWriter

data class RootClassGenerator(val w: PicoWriter, val typesW: PicoWriter, val functionW: PicoWriter) {
    private fun translateFunctionPointerType(className: String, fn: ZigFn, ctx: Context) = with(functionW) {
        val name = fn.getRegistryName(ctx)
        writeln("")
        writeln("@FunctionalInterface")
        block("public interface $name") {
            FunctionDataGenerator.Fn(fn, ctx).generateDescriptor(this, className)
            writeln("")
            writeln("MethodHandle UP\$MH = $className.upCallHandle($name.class, \"apply\", \$DESC);")
            writeln("MethodHandle DOWN\$MH = $className.\$LIB.downcallHandle(\$DESC);")
            writeln("")
            genApplyFn(this, fn, ctx)
            writeln("")
            block("static MemorySegment allocate($name fi, Arena arena)") {
                writeln("return $className.\$LIB.upcallStub(UP\$MH.bindTo(fi), \$DESC, arena);")
            }
            writeln("")
            genInvokeFn(this, fn, ctx)
        }
    }

    private fun genApplyFn(writer: PicoWriter, fn: ZigFn, ctx: Context) = with(writer) {
        val gen = FunctionDataGenerator.Fn(fn, ctx)
        gen.genReturnType(writer)
        write(" apply(")
        gen.genParams(writer, withType = true)
        writeln(");")
    }

    private fun genInvokeFn(writer: PicoWriter, fn: ZigFn, ctx: Context) = with(writer) {
        val gen = FunctionDataGenerator.Fn(fn, ctx)
        write("static ")
        gen.genReturnType(writer)
        write(" invoke(MemorySegment funcPtr")
        gen.genParams(writer, withType = true, hasBefore = true)
        block(") {") {
            tryCatch({
                gen.maybeGenReturnAndCast(writer)
                write("DOWN\$MH.invokeExact(funcPtr")
                gen.genParams(writer, withType = false, hasBefore = true)
                writeln(");")
            }, "Throwable ex$", {
                writeln("throw new AssertionError(\"should not reach here\", ex$);")
            })
        }
    }

    private fun translateRegularType(type: ZigType, ctx: Context) {
        with(typesW) {
            write("public static final ")
            write(type.layoutTypeStr(ctx))
            write(" ")
            write(type.getRegistryName(ctx))
            write(" = ")
            write(type.toRegistryMemoryLayout(ctx))
            writeln(";")
        }
    }

    companion object {
        fun generate(rootPkg: String, className: String, ctx: Context): Output {
            val skeleton = create(rootPkg, className)
            val entries = ctx.typesSorted.sequencedEntrySet().reversed().toMutableList()
            val registered = HashSet<String>()
            while (entries.isNotEmpty()) {
                val iter = entries.listIterator()
                while (iter.hasNext()) {
                    val entry = iter.next()
                    val (_, type) = entry
                    if (type.isVoid(ctx)) {
                        iter.remove()
                        continue
                    }
                    val deps = type.dependencies(ctx).map { it.getRegistryName(ctx) }
                    if (!registered.containsAll(deps)) {
                        continue
                    }
                    type.tryLet<ZigPointer>(ctx) { ptr ->
                        ptr.child.tryLet<ZigFn>(ctx) { fn ->
                            skeleton.translateFunctionPointerType("baller6_z", fn, ctx)
                        }
                    }
                    if (!type.isOpaque(ctx)) {
                        skeleton.translateRegularType(type, ctx)
                    }
                    registered.add(type.getRegistryName(ctx))
                    iter.remove()
                    if (iter.hasPrevious()) {
                        iter.previous()
                    }
                }
            }
            return Output(skeleton.w.toString())
        }

        private fun create(rootPkg: String, className: String) = with(PicoWriter("    ")) {
            val typesW: PicoWriter
            val functionW: PicoWriter
            writeln("package $rootPkg;")
            writeln("")
            writeln("import com.falsepattern.zanama.NativeContext;");
            writeln("import java.lang.foreign.*;");
            writeln("import java.lang.invoke.*;");
            writeln("import java.util.Arrays;");
            writeln("import java.util.stream.Collectors;");
            writeln("")
            block("public class $className") {
                writeln("public static final NativeContext.Lib \$LIB = ${className}_init.createLib();")
                writeln("public static final boolean TRACE_DOWNCALLS = Boolean.getBoolean(\"zanama.trace.downcalls\");")
                typesW = createDeferredWriter()
                writeln("")
                block("$className()") {
                    writeln("// Should not be called directly")
                }
                writeln("")
                block("public static void traceDowncall(String name, Object... args)") {
                    writeln("String traceArgs = Arrays.stream(args).map(Object::toString).collect(Collectors.joining(\", \"));")
                    writeln("System.out.printf(\"%s(%s)\\n\", name, traceArgs);")
                }
                writeln("")
                block("static MethodHandle upCallHandle(Class<?> fi, String name, FunctionDescriptor fdesc)") {
                    tryCatch({
                        writeln("return MethodHandles.lookup().findVirtual(fi, name, fdesc.toMethodType());")
                    }, "ReflectiveOperationException ex", {
                        writeln("throw new AssertionError(ex);")
                    })
                }
                functionW = createDeferredWriter()
            }
            return@with RootClassGenerator(this, typesW, functionW)
        }
    }

    data class Output(val code: String)
}