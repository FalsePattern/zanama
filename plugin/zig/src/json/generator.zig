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
const common = @import("common.zig");
const t = @import("t.zig");
const Binding = @import("../api.zig").Binding;

pub fn generateJson(comptime bindings: []const Binding) void {
    comptime var len = 1024;
    const data = common.prefix ++ (comptime while (true) {
        const res = tryGen(len, bindings) catch |err| switch (err) {
            error.WriteFailed => {
                len *= 2;
                continue;
            },
        };
        break res;
    }) ++ common.suffix;
    @export(data, .{ .name = "json" });
}

fn tryGen(comptime len: usize, comptime bindings: []const Binding) ![]const u8 {
    @setEvalBranchQuota(100000);
    var buf: [len]u8 = std.mem.zeroes([len]u8);
    var writer = std.Io.Writer.fixed(&buf);
    var ip: t.InternPool = .{};
    var ws = std.json.Stringify{
        .writer = &writer,
        .options = .{
            .whitespace = .minified,
            .emit_null_optional_fields = false,
            .escape_unicode = true,
        }
    };
    try ws.beginObject();
    try ws.objectField("bindings");
    try ws.beginObject();
    for (bindings) |binding| {
        const zt = t.fromType2(binding.Struct, &ip, true);
        if (binding.name) |name| {
            try ws.objectField(name);
            try ws.write(zt);
        }
    }
    try ws.endObject();
    try ws.objectField("types");
    try ws.beginObject();
    for (ip.Pool) |entry| {
        try ws.objectField(@typeName(entry.T));
        try ws.write(entry.ZT);
    }
    try ws.endObject();
    try ws.endObject();
    try writer.writeAll("\n");
    const buffered = writer.buffered();
    const final: [buffered.len]u8 = buf[0..buffered.len].*;
    return &final;
}
