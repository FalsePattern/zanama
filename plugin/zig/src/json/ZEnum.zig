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
const ZNameSpace = @import("ZNameSpace.zig");

tag_type: t.Ref,
ns: ?ZNameSpace,
is_exhaustive: bool,
fields: []const std.builtin.Type.EnumField,

pub fn fromEnum(comptime T: type, comptime Enum: std.builtin.Type.Enum, comptime IP: *t.InternPool, bind_functions: bool) @This() {
    const tag_type = t.fromType(Enum.tag_type, IP);
    const ns = ZNameSpace.fromDecls(T, Enum.decls, IP, bind_functions);
    return .{
        .tag_type = tag_type,
        .ns = ns,
        .is_exhaustive = Enum.is_exhaustive,
        .fields = Enum.fields,
    };
}
