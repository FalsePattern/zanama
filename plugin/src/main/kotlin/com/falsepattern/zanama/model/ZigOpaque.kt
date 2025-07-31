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

data class ZigOpaque(
    override val ns: NameSpace?,
    override var alias: String? = null,
    override var name: String? = null
) : NameSpaced {
    override val variantName get() = "OPAQUE"

    override fun getRegistryName(ctx: Context): String {
        return alias!!
    }

    override fun toRegistryMemoryLayout(ctx: Context): String {
        throw UnsupportedOperationException()
    }

    override fun toJavaTypeStr(ctx: Context): String {
        throw UnsupportedOperationException()
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
        return false
    }

    override fun layoutTypeStr(ctx: Context): String {
        throw UnsupportedOperationException()
    }

    override fun isOpaque(ctx: Context): Boolean {
        return true
    }
}