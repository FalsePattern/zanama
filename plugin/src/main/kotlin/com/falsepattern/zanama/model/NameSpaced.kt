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

sealed interface NameSpaced : ZigType {
    val ns: NameSpace?
    var alias: String?
    var name: String?
    val variantName: String

    override fun getRegistryName(ctx: Context): String {
        return "_${variantName.uppercase()}_${name!!.replace(".", "_")}"
    }

    fun aliasGen(ctx: Context, alias: String) {
        if (this.alias != null)
            return
        this.alias = alias
    }
}