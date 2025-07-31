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
data class ZigPointer(
    val size: Size,
    val const: Boolean,
    val volatile: Boolean,
    val alignment: Int,
    val child: ZigType
) : ZigType {
    enum class Size(val alias: String) {
        one("1"),
        many("N"),
        slice("S"),
        c("C"),
    }

    override fun getRegistryName(ctx: Context): String {
        return "P_" + size.alias + (if (const) "c" else "") + (if (volatile) "v" else "") + "_" + alignment + "_" + child.getRegistryName(ctx)
    }

    override fun toRegistryMemoryLayout(ctx: Context): String {
        return "ValueLayout.ADDRESS" +
                if (child.isOpaque(ctx)) "/*opaque*/" else
                    (".withTargetLayout(" + child.getRegistryName(ctx) + ".withByteAlignment(" + alignment + "))")
    }

    override fun toJavaTypeStr(ctx: Context): String {
        return "MemorySegment"
    }

    override fun dependencies(ctx: Context): List<ZigType> {
        return listOf(child)
    }

    override fun isSlice(ctx: Context): Boolean {
        return false
    }

    override fun isVoid(ctx: Context): Boolean {
        return false
    }

    override fun isZeroSized(ctx: Context): Boolean {
        return false
    }

    override fun isOpaque(ctx: Context): Boolean {
        return false
    }

    override fun layoutTypeStr(ctx: Context): String {
        return "AddressLayout"
    }
}