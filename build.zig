const std = @import("std");

const zanama = @import("zanama");

const test_targets = [_]std.Target.Query{
    zanama.target.common.aarch64.windows,
    zanama.target.common.aarch64.linux,
    zanama.target.common.aarch64.macos,
} ++ zanama.target.common.x86_64.windows.common ++ zanama.target.common.x86_64.linux.common ++ zanama.target.common.x86_64.macos.common;

pub fn build(b: *std.Build) void {
    const optimize = b.standardOptimizeOption(.{});

    const zanama_dep = b.dependency("zanama", .{});

    const zb = zanama.Build.init(b, optimize, zanama_dep);

    const root_module = b.createModule(.{ .root_source_file = b.path("src/test/zig/root.zig"), .imports = &.{
        .{ .name = "zanama", .module = zanama_dep.module("api") },
    } });
    //Self-test
    {
        const libs = zb.createZanamaLibsQuery("root", root_module, &test_targets);

        const test_step = b.step("selfTest", "Validates that the export and json generation works correctly");
        const install_test_step = b.step("installSelfTest", "Outputs the binaries and json file from the test step into the output directory.");

        for (libs.artifacts) |artifact| {
            test_step.dependOn(&artifact.step);
            install_test_step.dependOn(&b.addInstallArtifact(artifact, .{
                .dest_dir = .{ .override = .{ .custom = "test/lib" } },
                .h_dir = .disabled,
                .implib_dir = .disabled,
                .pdb_dir = .disabled,
            }).step);
        }
        test_step.dependOn(libs.json_step);
        const install_json = b.addInstallFileWithDir(libs.json, .{ .custom = "test" }, "root.json");
        install_json.step.dependOn(libs.json_step);
        install_test_step.dependOn(&install_json.step);
    }
    //FFI test binaries
    {
        const lib = zb.createZanamaLibsHost("root", root_module);

        const install_step = b.step("installHost", "Installs the host target compiled binary to the output directory.");

        for (lib.artifacts) |artifact| {
            install_step.dependOn(&b.addInstallArtifact(artifact, .{
                .dest_dir = .{ .override = .lib },
                .h_dir = .disabled,
                .implib_dir = .disabled,
                .pdb_dir = .disabled,
            }).step);
        }
        const install_json = b.addInstallFile(lib.json, "root.json");
        install_json.step.dependOn(lib.json_step);
        install_step.dependOn(&install_json.step);
    }
}
