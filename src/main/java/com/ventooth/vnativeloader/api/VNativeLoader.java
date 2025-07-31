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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.SymbolLookup;
import java.net.URI;
import java.nio.file.Path;

// TODO: Documentation
@SuppressWarnings("UnusedReturnValue")
public interface VNativeLoader<SELF extends VNativeLoader<? extends SELF>> {
    SymbolLookup loadNative(@NotNull Class<?> clazz, @NotNull String nativeName) throws IOException, UnsatisfiedLinkException;

    SymbolLookup loadNative(@NotNull String nativeName, @NotNull URI uri) throws IOException, UnsatisfiedLinkException;

    SymbolLookup loadNative(@NotNull String nativeName, @NotNull InputStream inputStream) throws IOException, UnsatisfiedLinkException;

    SymbolLookup loadNative(@NotNull String nativeName, byte @NotNull [] bytes) throws IOException, UnsatisfiedLinkException;

    SELF nameMapper(@NotNull VNativeNameMapper nameMapper);

    VNativeNameMapper nameMapper();

    SELF linker(@NotNull VNativeLinker linker);

    VNativeLinker linker();

    Path nativesDirectory();

    SELF nativesDirectory(@NotNull Path nativesDirectory);
}
