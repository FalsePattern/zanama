/*
 * Zanama
 *
 * Copyright (C) 2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.falsepattern.zanama.testing;

import com.falsepattern.zanama.testing.natives.Packed;
import com.falsepattern.zanama.testing.natives.Pos;
import com.falsepattern.zanama.testing.natives.root_z;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestBase {
    @BeforeAll
    public static void load() {
        //Trigger static initializer
        var x = root_z.$LIB;
    }

    @Test
    public void downcall() {
        Assertions.assertDoesNotThrow(() -> Packed.hello(0));
    }

    @Test
    public void packedStruct() {
        var pos = 0L;
        pos = Pos.x$set(pos, 5);
        pos = Pos.y$set(pos, (byte) 10);
        pos = Pos.z$set(pos, 15);
        Assertions.assertEquals(5, Pos.x$get(pos));
        Assertions.assertEquals((byte) 10, Pos.y$get(pos));
        Assertions.assertEquals(15, Pos.z$get(pos));
    }

    @Test
    public void downcall2() {
        var pos = 0L;
        pos = Pos.x$set(pos, 5);
        pos = Pos.y$set(pos, (byte) 10);
        pos = Pos.z$set(pos, 15);
        var struct = Packed.pos$set(0L, pos);
        var pos2 = Assertions.assertDoesNotThrow(() -> Pos.swapXZ(struct));
        Assertions.assertEquals(Pos.x$get(pos), Pos.z$get(pos2));
        Assertions.assertEquals(Pos.y$get(pos), Pos.y$get(pos2));
        Assertions.assertEquals(Pos.z$get(pos), Pos.x$get(pos2));
    }
}
