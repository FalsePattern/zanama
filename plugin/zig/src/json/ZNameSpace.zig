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

const NamedRef = t.Named(t.Ref);

functions: ?[]const ZNamedFn,
children: ?[]const NamedRef,

pub fn fromDecls(comptime Parent: type, comptime decls: []const std.builtin.Type.Declaration, comptime IP: *t.InternPool, bind_functions: bool) ?@This() {
    comptime var functions: []const ZNamedFn = &[0]ZNamedFn{};
    comptime var children: []const NamedRef = &[0]NamedRef{};
    inline for (decls) |decl| {
        const resolvedDecl = @field(Parent, decl.name);
        const dTypeInfo = @typeInfo(@TypeOf(resolvedDecl));
        switch (dTypeInfo) {
            .@"fn" => |Fn| {
                if (!bind_functions)
                    continue;
                if (std.mem.endsWith(u8, decl.name, "$PARAMNAMES")) {
                    continue;
                }
                const paramNames = if (@hasDecl(Parent, decl.name ++ "$PARAMNAMES")) @field(Parent, decl.name ++ "$PARAMNAMES") else null;
                if (ZNamedFn.fromNamedFn(Parent, decl.name, Fn, paramNames, IP)) |zfn| {
                    functions = functions ++ [_]ZNamedFn{zfn};
                }
            },
            .type => {
                children = children ++ [_]NamedRef{.{ .name = decl.name, .value = t.fromType(resolvedDecl, IP) }};
            },
            else => @compileError(std.fmt.comptimePrint("{s}.{s} ({s}) {any}", .{ @typeName(Parent), decl.name, @tagName(dTypeInfo), resolvedDecl })),
        }
    }
    if (functions.len == 0 and children.len == 0) {
        return null;
    }
    return .{
        .functions = if (functions.len == 0) null else functions,
        .children = if (children.len == 0) null else children,
    };
}

const ZNamedFn = struct {
    method: bool,
    name: []const u8,
    calling_convention: []const u8,
    params: []const NamedRef,
    return_type: ?t.Ref = null,

    pub fn fromNamedFn(comptime Parent: type, name: []const u8, comptime Fn: std.builtin.Type.Fn, paramNames: ?[]const []const u8, comptime IP: *t.InternPool) ?ZNamedFn {
        if (!Fn.calling_convention.eql(.c))
            return null;

        const method = if (Fn.params.len >= 1 and Fn.params[0].type != null)
            switch (@typeInfo(Fn.params[0].type.?)) {
                .pointer => |P| switch (P.size) {
                    .one => P.child == Parent,
                    .many, .slice, .c => false,
                },
                else => Fn.params[0].type == type,
            }
        else
            false;
        comptime var params: []const NamedRef = &[0]NamedRef{};
        inline for (Fn.params, 0..) |param, i| {
            if (param.type) |pt| {
                params = params ++ [_]NamedRef{.{
                    .name = if (paramNames) |pn| pn[i] else if (method and i == 0) "self" else std.fmt.comptimePrint("p{}", .{i}),
                    .value = t.fromType(pt, IP),
                }};
            } else {
                @compileError("Function parameter without type!");
            }
        }
        return .{
            .method = method,
            .name = name,
            .calling_convention = "C",
            .params = params,
            .return_type = if (Fn.return_type) |rt| t.fromType(rt, IP) else null,
        };
    }
};
