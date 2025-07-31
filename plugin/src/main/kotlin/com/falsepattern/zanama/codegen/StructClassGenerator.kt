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
import java.util.*

data class StructClassGenerator(val w: PicoWriter, val fnW: PicoWriter, val fieldW: PicoWriter, val fnDescW: PicoWriter?, val fieldDescW: PicoWriter?) {
    private fun createNegativeMask(ownerBitSize: Int, selfOffset: Int, selfBitSize: Int): String {
        val bits = BitSet()
        bits.set(0, ownerBitSize)
        bits.clear(selfOffset, selfOffset + selfBitSize)
        return bitsToString(ownerBitSize, bits)
    }

    private fun createPositiveMask(ownerBitSize: Int, selfOffset: Int, selfBitSize: Int): String {
        val bits = BitSet()
        bits.set(selfOffset, selfOffset + selfBitSize)
        return bitsToString(ownerBitSize, bits)
    }

    private fun bitsToString(bitSize: Int, bits: BitSet): String {
        var mask: ULong = 0uL
        for (i in 0..bitSize) {
            if (bits.get(i)) {
                mask = mask or (1uL shl i)
            }
        }
        val b = StringBuilder("0x")
        b.append(mask.toString(16))
        if (bitSize > 32) {
            b.append('L')
        }
        return b.toString()
    }

    private fun buildEnumFields(owner: ZigEnum, ctx: Context) {
        val fields = owner.fields
        if (fields == null)
            return
        val theInt = owner.tag_type.tryAs<ZigInt>(ctx) ?: return
        val javaType = theInt.toJavaTypeStr(ctx)
        val bits = theInt.bits
        val needsCast = bits <= 16
        val suffix = if (bits > 32) "L" else ""
        var b = StringBuilder()
        with(fieldW) {
            for (field in fields) {
                b.setLength(0)
                b.append("public static final ")
                b.append(javaType)
                b.append(" ")
                b.append(field.name)
                b.append(" = ")
                if (needsCast) {
                    b.append("(")
                    b.append(javaType)
                    b.append(")")
                }
                b.append("0x")
                b.append(field.value.toString(16))
                b.append(suffix)
                b.append(";")
                writeln(b.toString())
            }
        }
    }

    private fun buildFields(owner: ZigContainer, ctx: Context) {
        val fields = owner.fields
        if (fields == null)
            return
        when (owner.layout) {
            ZigContainer.Layout.auto -> {}
            ZigContainer.Layout.packed -> {
                val ownerJavaType = owner.toJavaTypeStr(ctx)
                val ownerBitSize = owner.bitSize
                for (field in fields) {
                    val fieldType = field.type
                    if (fieldType.isZeroSized(ctx))
                        continue
                    val fieldJavaType = fieldType.toJavaTypeStr(ctx)
                    val fieldName = field.name
                    val neg = createNegativeMask(ownerBitSize, field.bit_offset, field.bit_size)
                    val pos = createPositiveMask(ownerBitSize, field.bit_offset, field.bit_size)
                    val mathType = if (ownerBitSize > 32) "long" else "int"
                    with(fieldW) {
                        writeln("")
                        block("public static $fieldJavaType $fieldName\$get($ownerJavaType self)") {
                            val noCast = fieldJavaType == ownerJavaType
                            val cast = if (noCast) "" else "($fieldJavaType)("
                            val cast2 = if (noCast) "" else ")"
                            if (field.bit_offset != 0) {
                                writeln("return $cast(self & $pos) >>> ${field.bit_offset}$cast2;")
                            } else if (field.bit_size != ownerBitSize) {
                                writeln("return ${cast}self & $pos$cast2;")
                            } else {
                                writeln("return ${if (noCast) "" else "($fieldJavaType)"}self;")
                            }
                        }
                        writeln("")
                        block("public static $ownerJavaType $fieldName\$set($ownerJavaType self, $fieldJavaType fieldValue)") {
                            val noCast = mathType == ownerJavaType
                            val cast = if (noCast) "" else "($ownerJavaType)("
                            val cast2 = if (noCast) "" else ")"
                            val mathCast = if (mathType == fieldJavaType) "" else "($mathType)"
                            if (field.bit_offset != 0) {
                                writeln("return $cast(self & $neg) | ((${mathCast}fieldValue << ${field.bit_offset}) & $pos)$cast2;")
                            } else if (field.bit_size != ownerBitSize) {
                                writeln("return $cast(self & $neg) | (${mathCast}fieldValue & $pos)$cast2;")
                            } else {
                                writeln("return ${if (noCast) "" else "($ownerJavaType)"}fieldValue;")
                            }
                        }
                    }
                }
            }

            ZigContainer.Layout.extern -> {
                fieldDescW!!
                for (field in fields) {
                    val fieldType = field.type
                    val fieldJavaType = fieldType.toJavaTypeStr(ctx)
                    val fieldLayout = fieldType.layoutTypeStr(ctx)
                    val fieldName = field.name
                    val isSlice = fieldType.isSlice(ctx)
                    val layoutName = "$fieldName\$LAYOUT"
                    val offsetName = "$fieldName\$OFFSET"
                    with(fieldDescW) {
                        writeln("")
                        writeln("public static final $fieldLayout $layoutName = ($fieldLayout)LAYOUT.select(MemoryLayout.PathElement.groupElement(\"$fieldName\"));")
                        writeln("public static final long $offsetName = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement(\"$fieldName\"));")
                    }
                    with(fieldW) {
                        writeln("")
                        block("public static $fieldJavaType $fieldName\$get(MemorySegment self)") {
                            if (isSlice) {
                                writeln("return self.asSlice($.$offsetName, $.$layoutName.byteSize());")
                            } else {
                                writeln("return self.get($.$layoutName, $.$offsetName);")
                            }
                        }
                        block("public static void $fieldName\$set(MemorySegment self, $fieldJavaType fieldValue)") {
                            if (isSlice) {
                                writeln("MemorySegment.copy(fieldValue, 0L, self, $.$offsetName, $.$layoutName.byteSize());")
                            } else {
                                writeln("self.set($.$layoutName, $.$offsetName, fieldValue);")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildFunctions(rootName: String, bindingName: String, functions: List<NameSpace.NamedFn>, ctx: Context) {
        fnDescW!!
        for (fn in functions) {
            val gen = FunctionDataGenerator.NamedFn(fn, ctx)
            with(fnDescW) {
                writeln("")
                write("public static final ")
                gen.generateDescriptor(this, rootName)
                writeln_r("public static final MethodHandle ${fn.name}\$handle = $rootName.\$LIB.downcallHandle(")
                writeln("\"$bindingName::${fn.name}\",")
                writeln("${fn.name}\$descriptor")
                writeln_l(");")
            }
            with(fnW) {
                writeln("")
                write("public static ")
                gen.genReturnType(this)
                write(" ${fn.name}(")
                gen.genParams(this, withType = true, hasBefore = false)
                block(")") {
                    tryCatch({
                        block("if ($rootName.TRACE_DOWNCALLS)") {
                            write("$rootName.traceDowncall(\"${fn.name}\"")
                            gen.genParams(this, withType = false, hasBefore = true)
                            writeln(");")
                        }
                        gen.maybeGenReturnAndCast(this)
                        write("$.${fn.name}\$handle.invokeExact(")
                        gen.genParams(this, withType = false, hasBefore = false)
                        writeln(");")
                    }, "Throwable ex$", {
                        writeln("throw new AssertionError(\"should not reach here\", ex$);")
                    })
                }
            }
        }
    }

    companion object {
        fun generate(name: String, type: ZigType, bindRoot: String, rootPkg: String, rootName: String, ctx: Context): Output {
            val bindName = if (name.startsWith(bindRoot)) rootPkg + name.substring(bindRoot.length) else name
            val simpleName = name.substring(name.lastIndexOf('.') + 1)
            val pkg = bindName.substring(0, bindName.lastIndexOf('.'))
            val skele = crate(rootPkg, rootName, pkg, simpleName, type, ctx)
            type.tryLet<ZigContainer>(ctx) { container ->
                skele.buildFields(container, ctx)
            }
            type.tryLet<ZigEnum>(ctx) { enum ->
                skele.buildEnumFields(enum, ctx)
            }
            type.tryLet<NameSpaced>(ctx) { struct ->
                val functions = struct.ns?.functions
                if (functions != null) {
                    skele.buildFunctions(rootName, struct.alias ?: struct.name?.let { name -> "__ZANAMA_NO_ALIAS__${name}" } ?: name, functions, ctx)
                }
            }
            return Output(code = skele.w.toString(), pkg = pkg, simpleName = simpleName)
        }

        private fun crate(rootPkg: String, mainClassName: String, selfPkg: String, className: String, type: ZigType, ctx: Context) = with(PicoWriter("    ")) {
            val fnW: PicoWriter
            val fieldW: PicoWriter
            val fnDescW: PicoWriter?
            val fieldDescW: PicoWriter?
            writeln("package $selfPkg;")
            writeln("")
            writeln("import java.lang.foreign.*;");
            writeln("import java.lang.invoke.*;");
            writeln("import java.util.function.Consumer;");
            if (rootPkg != selfPkg) {
                writeln("import $rootPkg.$mainClassName;")
            }
            writeln("")
            block("public final class $className") {
                block("private $className()") {}
                fnW = createDeferredWriter()
                fieldW = createDeferredWriter()
                val hasFn = type.tryLet<NameSpaced, Boolean>(ctx) { it.ns?.functions != null } ?: false
                val hasField = !type.isOpaque(ctx) && (type.tryLet<ZigContainer, Boolean>(ctx) { it.layout != ZigContainer.Layout.packed } ?: false)
                if (!hasFn && !hasField) {
                    fnDescW = null
                    fieldDescW = null
                    return@block
                }
                writeln("")
                block("public static final class $") dollar@{
                    if (!hasField) {
                        fnDescW = createDeferredWriter()
                        fieldDescW = null
                        return@dollar
                    }
                    writeln("/**")
                    writeln(" * The layout of this struct")
                    writeln(" */")
                    writeln("public static final ${type.layoutTypeStr(ctx)} LAYOUT = $mainClassName.${type.getRegistryName(ctx)};")
                    writeln("")
                    writeln("/**")
                    writeln(" * The size (in bytes) of this struct")
                    writeln(" */")
                    writeln("public static final long SIZEOF = LAYOUT.byteSize();")
                    fnDescW = if (hasFn) createDeferredWriter() else null
                    fieldDescW = createDeferredWriter()
                    writeln("")
                    writeln("/**")
                    writeln(" * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.")
                    writeln(" * The returned segment has address {@code arrayParam.address() + index * SIZEOF}")
                    writeln(" */")
                    block("public static MemorySegment asSlice(MemorySegment array, long index)") {
                        writeln("return array.asSlice(SIZEOF * index);")
                    }
                    writeln("")
                    writeln("/**")
                    writeln(" * Allocate a segment of size {@code SIZEOF} using {@code allocator}")
                    writeln(" */")
                    block("public static MemorySegment allocate(SegmentAllocator allocator)") {
                        writeln("return allocator.allocate(LAYOUT);")
                    }
                    writeln("")
                    writeln("/**")
                    writeln(" * Allocate an array of size {@code elementCount} using {@code allocator}.")
                    writeln(" * The returned segment has size {@code elementCount * SIZEOF}.")
                    writeln(" */")
                    block("public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator)") {
                        writeln("return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, LAYOUT));")
                    }
                    writeln("")
                    writeln("/**")
                    writeln(" * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).")
                    writeln(" * The returned segment has size {@code SIZEOF}")
                    writeln(" */")
                    block("public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup)") {
                        writeln("return reinterpret(addr, 1, arena, cleanup);")
                    }
                    writeln("")
                    writeln("/**")
                    writeln(" * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).")
                    writeln(" * The returned segment has size {@code elementCount * SIZEOF}")
                    writeln(" */")
                    block("public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup)") {
                        writeln("return addr.reinterpret(SIZEOF * elementCount, arena, cleanup);")
                    }
                }
            }
            return@with StructClassGenerator(this, fnW, fieldW, fnDescW, fieldDescW)
        }
    }

    data class Output(val code: String, val pkg: String, val simpleName: String)
}