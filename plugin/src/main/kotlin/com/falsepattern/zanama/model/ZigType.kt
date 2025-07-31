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

sealed interface ZigType {
    fun getRegistryName(ctx: Context): String

    fun toRegistryMemoryLayout(ctx: Context): String

    fun layoutTypeStr(ctx: Context): String

    fun toJavaTypeStr(ctx: Context): String

    fun dependencies(ctx: Context): List<ZigType>

    fun isSlice(ctx: Context): Boolean

    fun isVoid(ctx: Context): Boolean

    fun isZeroSized(ctx: Context): Boolean

    fun isOpaque(ctx: Context): Boolean

    fun <T : ZigType> tryAs(ctx: Context, type: Class<T>): T? {
        return if (type.isInstance(this)) {
            type.cast(this)
        } else {
            null
        }
    }
}