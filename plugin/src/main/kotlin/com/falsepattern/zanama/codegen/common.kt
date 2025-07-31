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

package com.falsepattern.zanama.codegen

import com.falsepattern.zanama.model.Context
import com.falsepattern.zanama.model.ZigType
import org.ainslec.picocog.PicoWriter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


fun ZigType?.isVoidOrNull(ctx: Context) = this?.isVoid(ctx) != false

@OptIn(ExperimentalContracts::class)
inline fun PicoWriter.block(prefix: String, crossinline block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    write(prefix)
    writeln_r(" {")
    block()
    writeln_l("}")
}

@OptIn(ExperimentalContracts::class)
inline fun PicoWriter.tryCatch(crossinline tryBlock: () -> Unit, catchCondition: String, crossinline catchBlock: () -> Unit) {
    contract {
        callsInPlace(tryBlock, InvocationKind.EXACTLY_ONCE)
        callsInPlace(catchBlock, InvocationKind.EXACTLY_ONCE)
    }
    writeln_r("try {")
    tryBlock()
    write("} catch (")
    write(catchCondition)
    writeln_lr(") {")
    catchBlock()
    writeln_l("}")
}