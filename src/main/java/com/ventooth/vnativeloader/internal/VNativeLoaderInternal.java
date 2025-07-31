/*
 * VNativeLoader
 *
 * Copyright (C) 2025 Ventooth
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.ventooth.vnativeloader.internal;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.foreign.Arena;
import java.nio.file.Path;

@UtilityClass
public final class VNativeLoaderInternal {
    public static Loader createLoader(Arena arena) {
        val mapper = createNameMapper();
        val linker = createLinker(arena);
        val nativesDirectory = defaultNativesDirectory();

        return new Loader(mapper, linker, nativesDirectory);
    }

    public static UnpackingLoader createUnpackingLoader(Arena arena) {
        val mapper = createNameMapper();
        val unpacker = createUnpacker();
        val linker = createLinker(arena);
        val nativesDirectory = defaultNativesDirectory();
        val rootClasspath = defaultRootClasspath();

        return new UnpackingLoader(mapper, unpacker, linker, nativesDirectory, rootClasspath);
    }

    public static NameMapper createNameMapper() {
        return new NameMapper();
    }

    public static Unpacker createUnpacker() {
        return new Unpacker();
    }

    public static Linker createLinker(Arena arena) {
        return new Linker(arena);
    }

    public static Path defaultNativesDirectory() {
        return Path.of(System.getProperty("user.dir"), "natives");
    }

    public static String defaultRootClasspath() {
        return "/";
    }
}
