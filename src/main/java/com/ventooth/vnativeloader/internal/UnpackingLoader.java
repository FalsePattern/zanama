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

import com.ventooth.vnativeloader.api.UnsatisfiedLinkException;
import com.ventooth.vnativeloader.api.VNativeLinker;
import com.ventooth.vnativeloader.api.VNativeNameMapper;
import com.ventooth.vnativeloader.api.VNativeUnpacker;
import com.ventooth.vnativeloader.api.VNativeUnpackingLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.SymbolLookup;
import java.net.URI;
import java.nio.file.Path;

// TODO: Documentation
@Accessors(fluent = true)
@Setter
@Getter
@AllArgsConstructor
public final class UnpackingLoader implements VNativeUnpackingLoader<UnpackingLoader> {
    private static final Logger LOG = LogManager.getLogger("VNativeLoader");

    @NonNull
    private VNativeNameMapper nameMapper;
    @NonNull
    private VNativeUnpacker unpacker;
    @NonNull
    private VNativeLinker linker;
    @NonNull
    private Path nativesDirectory;
    @NonNull
    private String rootClasspath;

    @Override
    public SymbolLookup loadNative(@NotNull Class<?> clazz, @NonNull String nativeName) throws IOException, UnsatisfiedLinkException {
        val mappedNativeName = nameMapper.mapNativeToPlatformName(nativeName);
        val nativeClasspath = rootClasspath + mappedNativeName;
        val nativeFilePath = nativesDirectory.resolve(mappedNativeName);

        val unpackedNativeFilePath = unpacker.unpackNative(clazz, nativeClasspath, nativeFilePath);
        val lookup = linker.linkNative(unpackedNativeFilePath);

        LOG.debug("Loaded native: {}", nativeName);
        return lookup;
    }

    @Override
    public SymbolLookup loadNative(@NonNull String nativeName, @NonNull URI uri) throws IOException, UnsatisfiedLinkException {
        val mappedNativeName = nameMapper.mapNativeToPlatformName(nativeName);
        val nativeFilePath = nativesDirectory.resolve(mappedNativeName);

        val unpackedNativeFilePath = unpacker.unpackNative(uri, nativeFilePath);
        val lookup = linker.linkNative(unpackedNativeFilePath);

        LOG.debug("Loaded native: {} from uri: {}", nativeName, uri);
        return lookup;
    }

    @Override
    public SymbolLookup loadNative(@NonNull String nativeName, @NonNull InputStream inputStream) throws IOException, UnsatisfiedLinkException {
        val mappedNativeName = nameMapper.mapNativeToPlatformName(nativeName);
        val nativeFilePath = nativesDirectory.resolve(mappedNativeName);

        val unpackedNativeFilePath = unpacker.unpackNative(inputStream, nativeFilePath);
        val lookup = linker.linkNative(unpackedNativeFilePath);

        LOG.debug("Loaded native: {} from input stream", nativeName);
        return lookup;
    }

    @Override
    public SymbolLookup loadNative(@NonNull String nativeName, byte @NonNull [] bytes) throws IOException, UnsatisfiedLinkException {
        val mappedNativeName = nameMapper.mapNativeToPlatformName(nativeName);
        val nativeFilePath = nativesDirectory.resolve(mappedNativeName);

        val unpackedNativeFilePath = unpacker.unpackNative(bytes, nativeFilePath);
        val lookup = linker.linkNative(unpackedNativeFilePath);

        LOG.debug("Loaded native: {} from bytes", nativeName);
        return lookup;
    }
}
