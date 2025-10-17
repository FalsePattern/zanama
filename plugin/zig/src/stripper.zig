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

const common = @import("json/common.zig");

pub fn main() !void {
    var allocator = std.heap.DebugAllocator(.{}).init;
    defer _ = allocator.deinit();
    const alloc = allocator.allocator();

    var first_json_path: ?[]const u8 = null;
    var first_json: ?[]const u8 = null;
    defer {
        if (first_json_path) |first| {
            alloc.free(first);
        }
        if (first_json) |first| {
            alloc.free(first);
        }
    }

    var args = try std.process.argsWithAllocator(alloc);
    defer args.deinit();

    if (!args.skip()) {
        @panic("No args???");
    }

    const cwd = std.fs.cwd();

    while (args.next()) |arg| {
        const file = try cwd.openFile(arg, .{});
        defer file.close();
        const data = try file.readToEndAlloc(alloc, std.math.maxInt(usize));
        defer alloc.free(data);
        const prefix_index = (std.mem.indexOf(u8, data, common.prefix) orelse @panic(try std.fmt.allocPrint(alloc, "{s}: Prefix not found\n", .{arg}))) + common.prefix.len;
        const suffix_index = std.mem.indexOfScalarPos(u8, data, prefix_index, 0) orelse @panic(try std.fmt.allocPrint(alloc, "{s}: Suffix not found\n", .{arg}));
        const actual_range = data[prefix_index..suffix_index];
        if (first_json) |first| {
            if (std.mem.indexOfDiff(u8, first, actual_range)) |difference| {
                const context = 40;
                const offset = @max(0, difference - context);
                const shift = difference - offset;
                const context_a = first[offset..@min(first.len, difference + context)];
                const context_b = actual_range[offset..@min(actual_range.len, difference + context)];
                const shift_text = try alloc.alloc(u8, shift);
                @memset(shift_text, ' ');
                @panic(try std.fmt.allocPrint(alloc,
                    \\JSON MISMATCH!
                    \\{s} (Reference):
                    \\{s}
                    \\
                    \\{s}:
                    \\{s}
                    \\
                    \\A: {s}
                    \\B: {s}
                    \\   {s}^here
                , .{ first_json_path.?, first, arg, actual_range, context_a, context_b, shift_text }));
            }
        } else {
            first_json_path = try alloc.dupe(u8, arg);
            first_json = try alloc.dupe(u8, actual_range);
        }
    }
    if (first_json) |json| {
        try std.fs.File.stdout().writeAll(json);
    } else {
        @panic("No input specified!");
    }
}
