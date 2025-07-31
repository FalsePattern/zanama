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
data class ZigFn(
    val calling_convention: String,
    val params: List<ZigType>,
    val return_type: ZigType?
) : ZigType {
    override fun getRegistryName(ctx: Context): String {
        val sb = StringBuilder("FN_")
        sb.append(calling_convention).append('_')
        if (return_type == null || return_type.isVoid(ctx)) {
            sb.append("VOID")
        } else {
            sb.append(return_type.getRegistryName(ctx))
        }
        for (param in params) {
            sb.append(param.getRegistryName(ctx))
        }
        return sb.toString()
    }

    override fun toRegistryMemoryLayout(ctx: Context): String {
        return "null";
    }

    override fun layoutTypeStr(ctx: Context): String {
        return "MemoryLayout"
    }

    override fun toJavaTypeStr(ctx: Context): String {
        return "MemorySegment"
    }

    override fun dependencies(ctx: Context): List<ZigType> {
        return if (return_type != null)
            listOf(return_type) + params
        else
            params
    }

    override fun isSlice(ctx: Context): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isVoid(ctx: Context): Boolean {
        return false
    }

    override fun isZeroSized(ctx: Context): Boolean {
        return false
    }

    override fun isOpaque(ctx: Context): Boolean {
        return true
    }
}