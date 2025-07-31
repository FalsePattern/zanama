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
type: type,
value_ptr: *const anyopaque,

const Self = @This();

pub fn jsonStringify(self: *const Self, jws: anytype) !void {
    const dp: *const self.type = @ptrCast(@alignCast(self.value_ptr));
    try jws.write(dp.*);
}

pub fn of(value_ptr: ?*const anyopaque, comptime T: type) ?Self {
    if (value_ptr) |value| {
        return .{
            .type = T,
            .value_ptr = value,
        };
    }
    return null;
}
