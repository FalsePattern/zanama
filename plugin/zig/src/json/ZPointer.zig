// Zanama
//
// Copyright (C) 2025 FalsePattern
// All Rights Reserved
//
// The above copyright notice and this permission notice
// shall be included in all copies or substantial portions of the Software.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, only version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
//
// SPDX-License-Identifier: GPL-3.0-only
const std = @import("std");
const t = @import("t.zig");

size: std.builtin.Type.Pointer.Size,
@"const": bool,
@"volatile": bool,
alignment: u32,
child: t.Ref,

pub fn fromPointer(comptime Pointer: std.builtin.Type.Pointer, IP: *t.InternPool) @This() {
    return .{
        .size = Pointer.size,
        .@"const" = Pointer.is_const,
        .@"volatile" = Pointer.is_volatile,
        .alignment = Pointer.alignment,
        .child = t.fromType(Pointer.child, IP),
    };
}
