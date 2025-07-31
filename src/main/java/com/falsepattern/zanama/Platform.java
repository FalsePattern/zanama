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

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * The platforms supported by Zanama.
 */
public enum Platform {
    LINUX("Linux") {
        private final Pattern SO = Pattern.compile("(?:^|/)lib\\w+[.]so(?:[.]\\d+)*$");

        @Override
        String mapLibraryName(String name) {
            if (SO.matcher(name).find()) {
                return name;
            }

            return System.mapLibraryName(name);
        }
    },
    MACOS("macOS") {
        private final Pattern DYLIB = Pattern.compile("(?:^|/)lib\\w+(?:[.]\\d+)*[.]dylib$");

        @Override
        String mapLibraryName(String name) {
            if (DYLIB.matcher(name).find()) {
                return name;
            }

            return System.mapLibraryName(name);
        }
    },
    WINDOWS("Windows") {
        @Override
        String mapLibraryName(String name) {
            if (name.endsWith(".dll")) {
                return name;
            }

            return System.mapLibraryName(name);
        }
    };

    private static final Platform current;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            current = WINDOWS;
        } else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
            current = LINUX;
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
            current = MACOS;
        } else {
            throw new LinkageError("Unknown platform: " + osName);
        }
    }

    /**
     * -- GETTER --
     * Returns the platform name.
     */
    @Getter
    private final String name;

    Platform(String name) {
        this.name = name;
    }

    /**
     * Returns the platform on which the library is running.
     */
    public static Platform get() {
        return current;
    }

    /**
     * Returns the architecture on which the library is running.
     */
    public static Architecture getArchitecture() {
        return Architecture.current;
    }

    abstract String mapLibraryName(String name);

    /**
     * The architectures supported by Zanama.
     */
    public enum Architecture {
        X64(true),
        X86(false),
        ARM64(true),
        ARM32(false);

        static final Architecture current;

        static {
            String osArch = System.getProperty("os.arch");
            boolean is64Bit = osArch.contains("64") || osArch.startsWith("armv8");

            current = osArch.startsWith("arm") || osArch.startsWith("aarch64") ? (is64Bit ? Architecture.ARM64 : Architecture.ARM32)
                                                                               : (is64Bit ? Architecture.X64 : Architecture.X86);
        }

        final boolean is64Bit;

        Architecture(boolean is64Bit) {
            this.is64Bit = is64Bit;
        }
    }
}