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

const BufContext = struct {
    buf: []u8,
    written: usize = 0,

    pub fn write(self: *BufContext, bytes: []const u8) error{TooLarge}!usize {
        const end = self.written + bytes.len;
        if (end > self.buf.len) {
            return error.TooLarge;
        }
        @memcpy(self.buf[self.written..end], bytes);
        self.written += bytes.len;
        return bytes.len;
    }
};

pub fn generateJson(comptime bindings: []const Binding) void {
    comptime var len = 128;
    const data = common.prefix ++ (comptime while (true) {
        const res = tryGen(len, bindings) catch |err| switch (err) {
            error.TooLarge => {
                len *= 2;
                continue;
            },
        };
        break res;
    }) ++ common.suffix;
    @export(data, .{ .name = "json" });
}

fn tryGen(comptime len: usize, comptime bindings: []const Binding) ![]const u8 {
    var buf: [len]u8 = std.mem.zeroes([len]u8);
    var ctx = BufContext{
        .buf = &buf,
    };
    const writer: std.io.GenericWriter(*BufContext, error{TooLarge}, BufContext.write) = .{
        .context = &ctx,
    };
    var ip: t.InternPool = .{};
    var bw = std.io.bufferedWriter(writer);
    const stdout = bw.writer();
    var ws = std.json.writeStream(stdout, .{
        .whitespace = .minified,
        .emit_null_optional_fields = false,
        .escape_unicode = true,
    });
    defer ws.deinit();
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
    try stdout.print("\n", .{});
    try bw.flush();
    const final: [ctx.written]u8 = buf[0..ctx.written].*;
    return &final;
}
