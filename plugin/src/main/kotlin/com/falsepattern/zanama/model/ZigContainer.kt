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

package com.falsepattern.zanama.model

import java.util.stream.Collectors

sealed interface ZigContainer : NameSpaced {
    val fields: List<ZigField>?
    val bitSize: Int
    val layout: Layout
    val externLayoutType: String
    val externLayoutBuilder: String
    fun isABISized(): Boolean {
        return when (layout) {
            Layout.auto, Layout.extern -> false
            Layout.packed -> when (bitSize) {
                8, 16, 32, 64 -> true
                else -> false
            }
        }
    }

    override fun toRegistryMemoryLayout(ctx: Context): String {
        return when (layout) {
            Layout.auto -> "null"
            Layout.packed -> when (bitSize) {
                in 1..8 -> "ValueLayout.JAVA_BYTE"
                in 9..16 -> "ValueLayout.JAVA_SHORT"
                in 17..32 -> "ValueLayout.JAVA_INT"
                in 33..64 -> "ValueLayout.JAVA_LONG"
                else -> throw IllegalArgumentException("Packed container \"$name\" with a size of $bitSize bits cannot be converted to panama!")
            }

            Layout.extern -> "MemoryLayout.$externLayoutBuilder(${fieldLayouts(ctx)})"
        }
    }

    override fun toJavaTypeStr(ctx: Context): String {
        return when (layout) {
            Layout.auto, Layout.extern -> "MemorySegment"
            Layout.packed -> when (bitSize) {
                in 1..8 -> "byte"
                in 9..16 -> "short"
                in 17..32 -> "int"
                in 33..64 -> "long"
                else -> throw IllegalArgumentException("Packed container \"$name\" with a size of $bitSize bits cannot be converted to panama!")
            }
        }
    }

    override fun layoutTypeStr(ctx: Context): String {
        return when (layout) {
            Layout.auto -> "MemoryLayout"
            Layout.packed -> when (bitSize) {
                in 1..8 -> "ValueLayout.OfByte"
                in 9..16 -> "ValueLayout.OfShort"
                in 17..32 -> "ValueLayout.OfInt"
                in 33..64 -> "ValueLayout.OfLong"
                else -> throw IllegalArgumentException("Packed container \"$name\" with a size of $bitSize bits cannot be converted to panama!")
            }

            Layout.extern -> externLayoutType
        }
    }

    override fun dependencies(ctx: Context): List<ZigType> {
        return fields?.map { it.type } ?: emptyList()
    }

    override fun isSlice(ctx: Context): Boolean {
        return true
    }

    override fun isZeroSized(ctx: Context): Boolean {
        return fields?.all { it.type.isZeroSized(ctx) } ?: true
    }

    override fun isVoid(ctx: Context): Boolean {
        return false
    }

    override fun isOpaque(ctx: Context): Boolean {
        return layout == Layout.auto
    }

    fun fieldLayouts(ctx: Context): String {
        return fields?.stream()
            ?.map { field: ZigField -> field.toRegistryMemoryLayout(ctx) }
            ?.collect(Collectors.joining(",")) ?: ""
    }

    enum class Layout {
        auto,
        extern,
        packed,
    }
}