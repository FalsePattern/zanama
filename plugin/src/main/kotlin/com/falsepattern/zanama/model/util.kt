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

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
inline fun <reified T : ZigType> ZigType.tryLet(ctx: Context, block: (T) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    tryAs(ctx, T::class.java)?.let(block)
}

@OptIn(ExperimentalContracts::class)
inline fun <reified T : ZigType, R> ZigType.tryLet(ctx: Context, block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return tryAs(ctx, T::class.java)?.let(block)
}

inline fun <reified T : ZigType> ZigType.isType(ctx: Context): Boolean {
    return tryAs(ctx, T::class.java) != null
}

inline fun <reified T : ZigType> ZigType.tryAs(ctx: Context): T? {
    return tryAs(ctx, T::class.java)
}