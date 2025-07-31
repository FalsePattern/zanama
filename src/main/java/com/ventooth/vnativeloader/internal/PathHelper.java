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

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

// TODO: Documentation
public final class PathHelper {
    private static final Logger LOG = LogManager.getLogger("VNativeLoader|PathHelper");

    private PathHelper() {
    }

    public static Optional<byte[]> readFile(Path filePath) throws IOException {
        if (Files.isDirectory(filePath)) {
            throw new IOException("File path: %s is a directory".formatted(filePath.toAbsolutePath()));
        }

        if (!Files.isRegularFile(filePath)) {
            LOG.trace("File: {} not found", filePath.toAbsolutePath());
            return Optional.empty();
        }

        val bytes = FileUtils.readFileToByteArray(filePath.toFile());
        LOG.trace("Read: {} bytes from file: {}", bytes.length, filePath.toAbsolutePath());

        return Optional.of(bytes);
    }

    public static void writeFile(byte[] bytes, Path filePath) throws IOException {
        if (Files.isDirectory(filePath)) {
            throw new IOException("File path: %s is a directory".formatted(filePath.toAbsolutePath()));
        }

        if (Files.deleteIfExists(filePath)) {
            LOG.trace("Found and deleted existing file: {}", filePath.toAbsolutePath());
        }

        FileUtils.writeByteArrayToFile(filePath.toFile(), bytes);
        LOG.trace("Wrote: {} bytes to file: {}", bytes.length, filePath.toAbsolutePath());
    }
}
