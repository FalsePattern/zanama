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

const Query = std.Target.Query;

//Returned slice is freed by the caller
pub fn forEachModelAlloc(ally: std.mem.Allocator, base_query: std.Target.Query, models: []const std.Target.Query.CpuModel) []std.Target.Query {
    const out = try ally.alloc(std.Target.Query, models.len);
    errdefer ally.free(out);
    populate(base_query, out, models);
    return out;
}

pub fn forEachModel(base_query: std.Target.Query, comptime models: []const std.Target.Query.CpuModel) [models.len]std.Target.Query {
    var out: [models.len]std.Target.Query = undefined;
    populate(base_query, &out, models);
    return out;
}

fn populate(base_query: std.Target.Query, out: []std.Target.Query, models: []const std.Target.Query.CpuModel) void {
    std.debug.assert(out.len == models.len);
    for (models, out) |model, *out_query| {
        var q = base_query;
        q.cpu_model = model;
        out_query.* = q;
    }
}

pub const common = struct {
    pub const x86_64 = struct {
        pub const linux = fromOs(.linux);
        pub const windows = fromOs(.windows);
        pub const macos = fromOs(.macos);

        fn fromOs(os: std.Target.Os.Tag) type {
            return struct {
                pub const baseline = Query{
                    .cpu_arch = .x86_64,
                    .os_tag = os,
                    .cpu_model = .baseline,
                };

                pub const common = forEachModel(
                    Query{
                        .cpu_arch = .x86_64,
                        .os_tag = os,
                    },
                    &.{
                        .baseline,
                        .{ .explicit = &std.Target.x86.cpu.x86_64_v2 },
                        .{ .explicit = &std.Target.x86.cpu.x86_64_v3 },
                        .{ .explicit = &std.Target.x86.cpu.x86_64_v4 },
                    },
                );
            };
        }
    };
    pub const aarch64 = struct {
        pub const linux = Query{
            .cpu_arch = .aarch64,
            .os_tag = .linux,
            .cpu_model = .baseline,
        };

        pub const windows = Query{
            .cpu_arch = .aarch64,
            .os_tag = .windows,
            .cpu_model = .baseline,
        };

        pub const macos = Query{
            .cpu_arch = .aarch64,
            .os_tag = .macos,
            .cpu_model = .baseline,
        };
    };
};
