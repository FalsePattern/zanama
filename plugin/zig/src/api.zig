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
const genJson = @import("root").genJson;
const generator = @import("json/generator.zig");
pub const Binding = struct {
    name: ?[]const u8 = null,
    Struct: type,
};

pub fn genBindings(comptime bindings: []const Binding) !void {
    if (genJson) {
        generator.generateJson(bindings);
    } else {
        inline for (bindings) |binding| {
            if (binding.name) |name| {
                exportStruct(binding.Struct, name);
            } else {
                exportStruct(binding.Struct, "__ZANAMA_NO_ALIAS__" ++ @typeName(binding.Struct));
            }
        }
    }
}

fn exportStruct(comptime Struct: type, comptime name: []const u8) void {
    const decls = switch (@typeInfo(Struct)) {
        inline .@"struct", .@"union", .@"opaque", .@"enum" => |x| x.decls,
        else => return,
    };
    for (decls) |decl| {
        const declName = decl.name;
        const declRef = @field(Struct, declName);
        const info = @typeInfo(@TypeOf(declRef));
        if (info == .@"fn" and !info.@"fn".calling_convention.eql(.Unspecified)) {
            @export(&declRef, .{ .name = name ++ "::" ++ declName });
        }
    }
}
