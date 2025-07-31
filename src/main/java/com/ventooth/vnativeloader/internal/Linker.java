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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Path;

// TODO: Documentation
@RequiredArgsConstructor
public final class Linker implements VNativeLinker {
    private static final Logger LOG = LogManager.getLogger("VNativeLoader|Linker");
    private final Arena arena;

    @Override
    public SymbolLookup linkNative(@NonNull Path nativeFilePath) throws UnsatisfiedLinkException {
        val fullPath = nativeFilePath.toAbsolutePath().toString();
        try {
            SymbolLookup lib = SymbolLookup.libraryLookup(nativeFilePath, arena);
            LOG.trace("Linked native using JNI: {}", fullPath);
            return lib;
        } catch (Exception e) {
            throw new UnsatisfiedLinkException("Failed to link native: %s".formatted(fullPath), e);
        }
    }
}
