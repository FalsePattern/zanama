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

calling_convention: []const u8,
params: []const t.Ref,
return_type: ?t.Ref = null,

pub fn fromFn(comptime Fn: std.builtin.Type.Fn, comptime IP: *t.InternPool) ?@This() {
    if (!Fn.calling_convention.eql(.C))
        return null;

    comptime var params: []const t.Ref = &[0]t.Ref{};
    inline for (Fn.params) |param| {
        if (param.type) |pt| {
            params = params ++ [_]t.Ref{t.fromType(pt, IP)};
        }
    }
    return .{
        .calling_convention = "C",
        .params = params,
        .return_type = if (Fn.return_type) |rt| t.fromType(rt, IP) else null,
    };
}
