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

import com.ventooth.vnativeloader.api.VNativeUnpacker;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

// TODO: Documentation
@NoArgsConstructor
public final class Unpacker implements VNativeUnpacker {
    private static final Logger LOG = LogManager.getLogger("VNativeLoader|Unpacker");

    @Override
    public Path unpackNative(@NonNull Class<?> clazz, @NonNull String nativeClasspath, @NonNull Path nativeFilePath) throws IOException {
        val classLoader = clazz.getClassLoader();

        try (val nativeInputStream = classLoader.getResourceAsStream(nativeClasspath)) {
            if (nativeInputStream == null) {
                throw new IOException("Failed to get native from classpath: %s".formatted(nativeClasspath));
            }

            return unpackNative(nativeInputStream, nativeFilePath);
        }
    }

    @Override
    public Path unpackNative(@NonNull URI uri, @NonNull Path nativeFilePath) throws IOException {
        try (val nativeInputStream = uri.toURL().openStream()) {
            if (nativeInputStream == null) {
                throw new IOException("Failed to get native from URI: %s ".formatted(uri));
            }

            return unpackNative(nativeInputStream, nativeFilePath);
        }
    }

    @Override
    public Path unpackNative(@NonNull InputStream inputStream, @NonNull Path nativeFilePath) throws IOException {
        val nativeBytes = IOUtils.toByteArray(inputStream);
        return unpackNative(nativeBytes, nativeFilePath);
    }

    @Override
    public Path unpackNative(byte @NonNull [] bytes, @NonNull Path nativeFilePath) throws IOException {
        if (Files.isDirectory(nativeFilePath)) {
            throw new IOException("File path: %s is a directory".formatted(nativeFilePath.toAbsolutePath()));
        }

        val expectedHash = DigestUtils.sha256Hex(bytes);
        LOG.trace("Expected native hash: {}", expectedHash.toUpperCase());

        val existingBytes = PathHelper.readFile(nativeFilePath);
        if (existingBytes.isPresent()) {
            LOG.trace("Found unpacked native: {}", nativeFilePath);

            val existingHash = DigestUtils.sha256Hex(existingBytes.get());

            if (existingHash.equalsIgnoreCase(expectedHash)) {
                LOG.trace("Unpacked native: {} is valid, skipping unpack", nativeFilePath);
                return nativeFilePath;
            }

            LOG.trace("Unpacked native is invalid, deleting old native: {}", nativeFilePath);
            Files.delete(nativeFilePath);
        }

        LOG.trace("Unpacking native to: {}", nativeFilePath);
        PathHelper.writeFile(bytes, nativeFilePath);

        val writtenBytes = PathHelper.readFile(nativeFilePath);
        if (writtenBytes.isEmpty()) {
            throw new IOException("Failed to unpack native, expected file not created: %s".formatted(nativeFilePath));
        }

        val writtenFileHash = DigestUtils.sha256Hex(writtenBytes.get());
        if (!writtenFileHash.equalsIgnoreCase(expectedHash)) {
            throw new IOException("Hash mismatch on unpacked native; found: %s expected: %s".formatted(writtenFileHash.toUpperCase(), expectedHash.toUpperCase()));
        }

        return nativeFilePath;
    }
}
