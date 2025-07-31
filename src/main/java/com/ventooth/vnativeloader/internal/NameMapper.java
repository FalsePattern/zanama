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

import com.ventooth.vnativeloader.api.VNativeNameMapper;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Documentation
@NoArgsConstructor
public final class NameMapper implements VNativeNameMapper {
    private static final Logger LOG = LogManager.getLogger("VNativeLoader|NameMapper");

    @Override
    public String mapNativeToPlatformName(@NonNull String nativeName) {
        val platformNativeName = System.mapLibraryName(nativeName);
        LOG.trace("Mapped native name: {} to platform native name: {}", nativeName, platformNativeName);
        return platformNativeName;
    }
}
