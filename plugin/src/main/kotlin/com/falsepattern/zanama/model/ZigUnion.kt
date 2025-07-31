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

data class ZigUnion(
    override val fields: List<ZigField>?,
    override val ns: NameSpace?,
    override val layout: ZigContainer.Layout,
    override val bitSize: Int,
    override var alias: String? = null,
    override var name: String? = null
) : ZigContainer {
    override val variantName get() = "UNION"
    override val externLayoutType get() = "UnionLayout"
    override val externLayoutBuilder get() = "unionLayout"
}