/*
 * Zanama Runtime
 *
 * Copyright (C) 2025 FalsePattern
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

package com.falsepattern.zanama;

import com.ventooth.vnativeloader.api.UnsatisfiedLinkException;
import com.ventooth.vnativeloader.api.VNativeLoader;
import com.ventooth.vnativeloader.api.VNativeLoaderAPI;
import com.ventooth.vnativeloader.api.VNativeUnpackingLoader;
import lombok.val;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

public final class NativeContext {
    private final Arena $ARENA;
    private final Linker $LINKER;

    private final VNativeLoader<?> $LOADER;

    private NativeContext(Arena $ARENA, Linker $LINKER, VNativeLoader<?> $LOADER) {
        this.$ARENA = $ARENA;
        this.$LINKER = $LINKER;
        this.$LOADER = $LOADER;
    }

    public static NativeContext createWithUnpacker(Arena arena, Linker linker, Path nativesDirectory, String rootClasspath) {
        val os = Platform.get();

        VNativeUnpackingLoader<?> loader = VNativeLoaderAPI.createUnpackingLoader(arena);

        loader.nativesDirectory(nativesDirectory);
        loader.rootClasspath(rootClasspath);

        loader.nameMapper(os::mapLibraryName);

        return new NativeContext(arena, linker, loader);
    }

    public static NativeContext createWithUnpacker(Path nativesDirectory, String rootClasspath) {
        return createWithUnpacker(Arena.ofAuto(), Linker.nativeLinker(), nativesDirectory, rootClasspath);
    }

    public static NativeContext create(Arena arena, Linker linker, Path nativesDirectory) {
        val os = Platform.get();

        VNativeLoader<?> loader = VNativeLoaderAPI.createLoader(arena);

        loader.nativesDirectory(nativesDirectory);

        loader.nameMapper(os::mapLibraryName);

        return new NativeContext(arena, linker, loader);
    }

    public static NativeContext create(Path nativesDirectory) {
        return create(Arena.ofAuto(), Linker.nativeLinker(), nativesDirectory);
    }

    public Lib load(Class<?> clazz, String nativeName) throws UnsatisfiedLinkException, IOException {
        return new Lib($LOADER.loadNative(clazz, nativeName));
    }

    public Lib loadUnchecked(Class<?> clazz, String nativeName) {
        try {
            return load(clazz, nativeName);
        } catch (UnsatisfiedLinkException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Linker linker() {
        return $LINKER;
    }

    public Arena arena() {
        return $ARENA;
    }

    public class Lib {
        private final SymbolLookup $LOOKUP;

        private Lib(SymbolLookup lookup) {
            this.$LOOKUP = lookup;
        }

        public MemorySegment findOrThrow(String symbol) {
            return $LOOKUP.find(symbol).orElseThrow(() -> new UnsatisfiedLinkError("unresolved symbol: " + symbol));
        }

        public MethodHandle downcallHandle(String symbol, FunctionDescriptor desc, Linker.Option... options) {
            return linker().downcallHandle(findOrThrow(symbol), desc, options);
        }

        public MethodHandle downcallHandle(FunctionDescriptor desc, Linker.Option... options) {
            return linker().downcallHandle(desc, options);
        }

        public MemorySegment upcallStub(MethodHandle target, FunctionDescriptor desc, Arena arena, Linker.Option... options) {
            return linker().upcallStub(target, desc, arena, options);
        }
    }
}
