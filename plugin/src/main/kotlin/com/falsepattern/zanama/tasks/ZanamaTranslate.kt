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

package com.falsepattern.zanama.tasks

import com.falsepattern.zanama.Translator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

abstract class ZanamaTranslate : DefaultTask() {
    @get:InputFile
    abstract val from: RegularFileProperty

    @get:OutputDirectory
    abstract val into: DirectoryProperty

    @get:Input
    abstract val clearTarget: Property<Boolean>

    @get:Input
    abstract val rootPkg: Property<String>

    @get:Input
    abstract val bindRoot: Property<String>

    @get:Input
    abstract val className: Property<String>

    init {
        clearTarget.convention(true)
        group = "zanama"
    }

    @OptIn(ExperimentalPathApi::class)
    @TaskAction
    fun generate() {
        val tgt = into.get().asFile.toPath()
        if (clearTarget.get() && tgt.exists()) {
            tgt.deleteRecursively()
        }
        tgt.createDirectories()
        Translator.translate(rootPkg.get(), bindRoot.get(), from.get().asFile.toPath(), tgt, className.get())
    }
}