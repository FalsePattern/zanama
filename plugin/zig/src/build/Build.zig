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

dep: *std.Build.Dependency,
opts: struct {
    gen_json: *std.Build.Module,
    no_gen_json: *std.Build.Module,
},
b: *std.Build,
optimize: std.builtin.OptimizeMode,
json_stripper_binary: *std.Build.Step.Compile,

const Self = @This();

const api_file = "src/api.zig";
const root_file = "src/root.zig";
const stripper_file = "src/stripper.zig";

pub fn BinParams(RootType: type) type {
    return struct {
        query: std.Target.Query,
        name: []const u8,
        root: RootType,
        optimize: std.builtin.OptimizeMode,

        pub fn resolveRoot(src: BinParams([]const u8), b: *std.Build) BinParams(std.Build.LazyPath) {
            return .{
                .query = src.query,
                .name = src.name,
                .root = b.path(src.root),
                .optimize = src.optimize,
            };
        }
    };
}

pub const LibResult = struct {
    artifacts: []const *std.Build.Step.Compile,
    json: std.Build.LazyPath,
    json_step: *std.Build.Step,
};

pub fn init(b: *std.Build, optimize: std.builtin.OptimizeMode, self_dep: *std.Build.Dependency) Self {
    const gen_json = b.addOptions();
    gen_json.addOption(bool, "genJson", true);
    const no_gen_json = b.addOptions();
    no_gen_json.addOption(bool, "genJson", false);
    const json_stripper_binary = b.addExecutable(.{
        .name = "json_stripper",
        .root_module = b.createModule(.{
            .target = b.graph.host,
            .root_source_file = self_dep.path(stripper_file),
        }),
    });
    return .{
        .dep = self_dep,
        .opts = .{
            .gen_json = gen_json.createModule(),
            .no_gen_json = no_gen_json.createModule(),
        },
        .b = b,
        .optimize = optimize,
        .json_stripper_binary = json_stripper_binary,
    };
}

pub fn createZanamaLibsHost(self: Self, name: []const u8, root_module: *std.Build.Module) LibResult {
    return createZanamaLibsResolved(self, name, root_module, &.{self.b.graph.host});
}

pub fn createZanamaLibsQuery(self: Self, name: []const u8, root_module: *std.Build.Module, targets: []const std.Target.Query) LibResult {
    const resolveds = self.b.allocator.alloc(std.Build.ResolvedTarget, targets.len) catch @panic("OOM");
    defer self.b.allocator.free(resolveds);
    for (targets, resolveds) |target, *resolved| {
        resolved.* = self.b.resolveTargetQuery(target);
    }
    return createZanamaLibsResolved(self, name, root_module, resolveds);
}

pub fn createZanamaLibsResolved(self: Self, name: []const u8, root_module: *std.Build.Module, targets: []const std.Build.ResolvedTarget) LibResult {
    const strip_json = self.b.addRunArtifact(self.json_stripper_binary);
    const strip_out = strip_json.captureStdOut();

    var artifacts = std.ArrayList(*std.Build.Step.Compile).initCapacity(self.b.allocator, targets.len) catch @panic("OOM");
    defer artifacts.deinit();
    for (targets) |resolved| {
        const final_name = blk: {
            const is_native = resolved.query.isNative();
            const base_triple = if (is_native) "native" else resolved.query.zigTriple(self.b.allocator) catch @panic("OOM");
            defer if (!is_native) self.b.allocator.free(base_triple);
            if (resolved.query.cpu_model == .explicit) {
                break :blk std.fmt.allocPrint(self.b.allocator, "{s}-{s}-{s}", .{ name, base_triple, resolved.query.cpu_model.explicit.name }) catch @panic("OOM");
            } else {
                break :blk std.fmt.allocPrint(self.b.allocator, "{s}-{s}", .{ name, base_triple }) catch @panic("OOM");
            }
        };
        self.createJsonGenStep(root_module, final_name, resolved, strip_json);
        artifacts.appendAssumeCapacity(self.createSharedLibrary(root_module, final_name, resolved));
    }
    return .{
        .artifacts = artifacts.toOwnedSlice() catch @panic("OOM"),
        .json = strip_out,
        .json_step = &strip_json.step,
    };
}

fn createJsonGenStep(self: Self, module: *std.Build.Module, name: []const u8, target: std.Build.ResolvedTarget, strip_json: *std.Build.Step.Run) void {
    const b = self.b;
    const zanama_json = b.addLibrary(.{
        .name = std.fmt.allocPrint(b.allocator, "zanama_json_{s}", .{name}) catch @panic("OOM"),
        .root_module = b.createModule(.{
            .root_source_file = self.dep.path(root_file),
            .target = target,
            .optimize = .ReleaseSmall,
            .strip = true,
            .imports = &.{
                .{ .name = "gen_options", .module = self.opts.gen_json },
                .{ .name = "target", .module = module },
            },
        }),
    });
    strip_json.addFileArg(zanama_json.getEmittedBin());
}

fn createSharedLibrary(self: Self, module: *std.Build.Module, name: []const u8, target: std.Build.ResolvedTarget) *std.Build.Step.Compile {
    const b = self.b;
    const artifact = b.addSharedLibrary(.{
        .name = name,
        .root_module = b.createModule(.{
            .root_source_file = self.dep.path(root_file),
            .target = target,
            .optimize = self.optimize,
            .strip = self.optimize != .Debug,
            .imports = &.{
                .{ .name = "gen_options", .module = self.opts.no_gen_json },
                .{ .name = "target", .module = module },
            },
        }),
    });
    if (self.optimize != .Debug) {
        artifact.link_data_sections = true;
        artifact.link_function_sections = true;
        artifact.bundle_compiler_rt = target.result.os.tag == .windows;
    }
    return artifact;
}
