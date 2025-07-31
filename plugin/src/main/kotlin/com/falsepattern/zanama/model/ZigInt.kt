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

@JvmRecord
data class ZigInt(val signedness: Signedness, val bits: Int) : ZigType {
    enum class Signedness {
        signed,
        unsigned
    }

    override fun getRegistryName(ctx: Context): String {
        return when (signedness) {
            Signedness.unsigned -> "U"
            Signedness.signed -> "I"
        } + bits
    }

    override fun toRegistryMemoryLayout(ctx: Context): String {
        return "/*" + signedness.name + "*/" + when (bits) {
            0 -> "null"
            in 1..8 -> "ValueLayout.JAVA_BYTE"
            in 9..16 -> "ValueLayout.JAVA_SHORT"
            in 17..32 -> "ValueLayout.JAVA_INT"
            in 33..64 -> "ValueLayout.JAVA_LONG"
            else -> throw IllegalArgumentException("Int with $bits bits cannot be converted to panama!")
        }
    }

    override fun toJavaTypeStr(ctx: Context): String {
        return when (bits) {
            in 1..8 -> "byte"
            in 9..16 -> "short"
            in 17..32 -> "int"
            in 33..64 -> "long"
            else -> throw IllegalArgumentException("Int with $bits bits cannot be converted to java type!")
        }
    }

    override fun dependencies(ctx: Context): List<ZigType> {
        return emptyList()
    }

    override fun isSlice(ctx: Context): Boolean {
        return false
    }

    override fun isVoid(ctx: Context): Boolean {
        return false
    }

    override fun layoutTypeStr(ctx: Context): String {
        return when (bits) {
            0 -> "MemoryLayout"
            in 1..8 -> "ValueLayout.OfByte"
            in 9..16 -> "ValueLayout.OfShort"
            in 17..32 -> "ValueLayout.OfInt"
            in 33..64 -> "ValueLayout.OfLong"
            else -> throw IllegalArgumentException("Int with $bits bits cannot be converted to panama!")
        }
    }

    override fun isZeroSized(ctx: Context): Boolean {
        return bits == 0
    }

    override fun isOpaque(ctx: Context): Boolean {
        return false
    }
}