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
data class ZigOptional(val child: ZigType) : ZigType {
    override fun getRegistryName(ctx: Context): String {
        return "O_" + child.getRegistryName(ctx)
    }

    override fun toRegistryMemoryLayout(ctx: Context): String {
        return "/*Nullable*/" + child.getRegistryName(ctx)
    }

    override fun toJavaTypeStr(ctx: Context): String {
        return child.toJavaTypeStr(ctx)
    }

    override fun dependencies(ctx: Context): List<ZigType> {
        return listOf(child)
    }

    override fun layoutTypeStr(ctx: Context): String {
        return child.layoutTypeStr(ctx)
    }

    override fun isSlice(ctx: Context): Boolean {
        return child.isSlice(ctx)
    }

    override fun isVoid(ctx: Context): Boolean {
        return child.isVoid(ctx)
    }

    override fun isZeroSized(ctx: Context): Boolean {
        return child.isZeroSized(ctx)
    }

    override fun isOpaque(ctx: Context): Boolean {
        return child.isOpaque(ctx)
    }
}