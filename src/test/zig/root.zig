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
const zanama = @import("zanama");

pub const PackedStruct = packed struct {
    pos: Pos,
    padding: u16 = 1,
    pub const Pos = packed struct {
        x: u22,
        y: u4,
        z: u22,

        pub fn swapXZ(self: PackedStruct) callconv(.c) PackedStruct {
            return .{
                .pos = .{
                    .x = self.pos.z,
                    .y = self.pos.y,
                    .z = self.pos.x,
                },
                .padding = self.padding,
            };
        }
    };

    pub fn hello(param: PackedStruct) callconv(.c) void {
        _ = param;
    }

    pub fn whatever(_: ImplicitEnum, _: NonExhaustiveEnum, _: ExplicitEnum) callconv(.c) void {

    }
};

pub const NonPacked = extern struct {
    x: i32,
    y: i32,
    z: i32,

    pub fn whatever(_: NonPacked) callconv(.c) void {

    }

    pub fn bar() callconv(.c) NonPacked {
        return undefined;
    }
};

pub const AutoEnum = enum {
    foo,
    bar,
    baz,
};

pub const ImplicitEnum = enum(u8) {
    a,
    b,
    c,
};

pub const NonExhaustiveEnum = enum(u8) {
    a,
    b,
    c,
    d,
    _,
};

pub const ExplicitEnum = enum(u32) {
    a = 100,
    b = 200,
    c = 300,
};

comptime {
    zanama.genBindings(&.{
        .{ .name = "com.falsepattern.zanama.testing.Packed", .Struct = PackedStruct },
        .{ .name = "com.falsepattern.zanama.testing.Pos", .Struct = PackedStruct.Pos },
        .{ .name = "com.falsepattern.zanama.testing.NonPacked", .Struct = NonPacked },
        .{ .Struct = @This() },
    }) catch unreachable;
}
