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
const WeaklyTypedValue = @import("WeaklyTypedValue.zig");

ns: ?ZNameSpace,
fields: ?[]const ZField = null,
layout: std.builtin.Type.ContainerLayout,
bitSize: u32,

pub fn fromStruct(comptime T: type, comptime Struct: std.builtin.Type.Struct, comptime IP: *t.InternPool, bind_functions: bool) @This() {
    const ns = ZNameSpace.fromDecls(T, Struct.decls, IP, bind_functions);
    return switch (Struct.layout) {
        .auto => .{
            .ns = ns,
            .layout = Struct.layout,
            .bitSize = @bitSizeOf(T),
        },
        else => blk: {
            comptime var fields: []const ZField = &[0]ZField{};
            inline for (Struct.fields) |field| {
                if (ZField.fromStruct(T, field, IP)) |zField| {
                    fields = fields ++ [_]ZField{zField};
                }
            }
            break :blk .{
                .ns = ns,
                .fields = fields,
                .layout = Struct.layout,
                .bitSize = @bitSizeOf(T),
            };
        },
    };
}
pub fn fromUnion(comptime T: type, comptime Union: std.builtin.Type.Union, comptime IP: *t.InternPool, bind_functions: bool) @This() {
    const ns = ZNameSpace.fromDecls(T, Union.decls, IP, bind_functions);
    return switch (Union.layout) {
        .auto => .{
            .ns = ns,
            .layout = Union.layout,
            .bitSize = @bitSizeOf(T),
        },
        else => blk: {
            comptime var fields: []const ZField = &[0]ZField{};
            inline for (Union.fields) |field| {
                fields = fields ++ [_]ZField{.fromUnion(field, IP)};
            }
            break :blk .{
                .ns = ns,
                .fields = fields,
                .layout = Union.layout,
                .bitSize = @bitSizeOf(T),
            };
        },
    };
}

const ZField = struct {
    name: [:0]const u8,
    type: t.Ref,
    alignment: u32,
    bit_offset: u32,
    bit_size: u32,
    default_value: ?WeaklyTypedValue,

    pub fn fromStruct(comptime Owner: type, comptime Field: std.builtin.Type.StructField, comptime IP: *t.InternPool) ?ZField {
        if (Field.is_comptime)
            return null;

        return .{
            .name = Field.name,
            .type = t.fromType(Field.type, IP),
            .alignment = Field.alignment,
            .bit_offset = @bitOffsetOf(Owner, Field.name),
            .bit_size = @bitSizeOf(Field.type),
            .default_value = .of(Field.default_value_ptr, Field.type),
        };
    }

    pub fn fromUnion(comptime Field: std.builtin.Type.UnionField, comptime IP: *t.InternPool) ZField {
        return .{
            .name = Field.name,
            .type = t.fromType(Field.type, IP),
            .alignment = Field.alignment,
            .bit_offset = 0,
            .bit_size = @bitSizeOf(Field.type),
            .default_value = null,
        };
    }
};
