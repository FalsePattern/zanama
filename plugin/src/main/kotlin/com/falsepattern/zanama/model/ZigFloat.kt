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
data class ZigFloat(val bits: Int) : ZigType {
    override fun getRegistryName(ctx: Context): String {
        return "F$bits"
    }

    override fun toRegistryMemoryLayout(ctx: Context): String {
        return when (bits) {
            32 -> "ValueLayout.JAVA_FLOAT"
            64 -> "ValueLayout.JAVA_DOUBLE"
            else -> throw IllegalArgumentException("f$bits cannot be converted to panama!")
        }
    }

    override fun toJavaTypeStr(ctx: Context): String {
        return when (bits) {
            32 -> "float"
            64 -> "double"
            else -> throw IllegalArgumentException("f$bits cannot be converted to panama!")
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

    override fun isZeroSized(ctx: Context): Boolean {
        return bits == 0
    }

    override fun isOpaque(ctx: Context): Boolean {
        return false
    }

    override fun layoutTypeStr(ctx: Context): String {
        return when (bits) {
            32 -> "ValueLayout.OfFloat"
            64 -> "ValueLayout.OfDouble"
            else -> throw IllegalArgumentException("f$bits cannot be converted to panama!")
        }
    }
}