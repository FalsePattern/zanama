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

package com.ventooth.vnativeloader.api;

import com.ventooth.vnativeloader.internal.VNativeLoaderInternal;
import lombok.experimental.UtilityClass;

import java.lang.foreign.Arena;
import java.nio.file.Path;

// TODO: Documentation
@SuppressWarnings("unused")
@UtilityClass
public final class VNativeLoaderAPI {
    public static VNativeLoader<?> createLoader(Arena arena) {
        return VNativeLoaderInternal.createLoader(arena);
    }

    public static VNativeUnpackingLoader<?> createUnpackingLoader(Arena arena) {
        return VNativeLoaderInternal.createUnpackingLoader(arena);
    }

    public static VNativeNameMapper createNameMapper() {
        return VNativeLoaderInternal.createNameMapper();
    }

    public static VNativeUnpacker createUnpacker() {
        return VNativeLoaderInternal.createUnpacker();
    }

    public static VNativeLinker createLinker(Arena arena) {
        return VNativeLoaderInternal.createLinker(arena);
    }

    public static Path defaultNativesDirectory() {
        return VNativeLoaderInternal.defaultNativesDirectory();
    }

    public static String defaultRootClasspath() {
        return VNativeLoaderInternal.defaultRootClasspath();
    }
}
