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

const ZOptional = @import("ZOptional.zig");
const ZContainer = @import("ZContainer.zig");
const ZArray = @import("ZArray.zig");
const ZEnum = @import("ZEnum.zig");
const ZPointer = @import("ZPointer.zig");
const ZFn = @import("ZFn.zig");
const ZOpaque = @import("ZOpaque.zig");

const Pair = struct {
    T: type,
    ZT: ?ZType = null,
};

pub const InternPool = struct {
    Pool: []const *Pair = &[_]*Pair{},
};

pub const Ref = []const u8;

pub const ZType = union(enum) {
    Int: std.builtin.Type.Int,
    Float: std.builtin.Type.Float,
    Optional: ZOptional,
    Struct: ZContainer,
    Array: ZArray,
    Union: ZContainer,
    Enum: ZEnum,
    Pointer: ZPointer,
    Fn: ?ZFn,
    Bool,
    Opaque: ZOpaque,
    Void,
};

pub fn fromType2(comptime T: type, comptime IP: *InternPool, bind_functions: bool) Ref {
    @setEvalBranchQuota(10000);
    const ref = @typeName(T);
    comptime var self: *Pair = blk: for (IP.*.Pool) |entry| {
        if (entry.T == T) {
            if (bind_functions) {
                break :blk entry;
            }
            return ref;
        }
    } else {
        var self: Pair = .{ .T = T };
        IP.Pool = IP.Pool ++ &[_]*Pair{&self};
        break :blk &self;
    };
    const Type = @typeInfo(T);
    const zType: ZType = switch (Type) {
        .int => |Int| .{ .Int = Int },
        .float => |Float| .{ .Float = Float },
        .optional => |Optional| .{ .Optional = ZOptional.fromOptional(Optional, IP) },
        .@"struct" => |Struct| .{ .Struct = ZContainer.fromStruct(T, Struct, IP, bind_functions) },
        .array => |Array| .{ .Array = ZArray.fromArray(Array, IP) },
        .@"union" => |Union| .{ .Union = ZContainer.fromUnion(T, Union, IP, bind_functions) },
        .@"enum" => |Enum| .{ .Enum = ZEnum.fromEnum(T, Enum, IP, bind_functions) },
        .pointer => |Pointer| .{ .Pointer = ZPointer.fromPointer(Pointer, IP) },
        .@"fn" => |Fn| .{ .Fn = ZFn.fromFn(Fn, IP) },
        .bool => .Bool,
        .@"opaque" => |Opaque| .{ .Opaque = ZOpaque.fromOpaque(T, Opaque, IP, bind_functions) },
        .void => .Void,
        else => @compileError(@typeName(T)),
    };
    self.ZT = zType;
    return ref;
}

pub fn fromType(comptime T: type, comptime IP: *InternPool) Ref {
    return fromType2(T, IP, false);
}

pub fn Named(T: type) type {
    return struct { name: []const u8, value: T };
}
