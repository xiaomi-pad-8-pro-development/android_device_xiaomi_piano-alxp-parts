// SPDX-License-Identifier: GPL-3.0-or-later
import Foundation
import Network

/// Reaches the Pad over USB when Bonjour can't, such as on networks that block
/// device discovery: forwards the Pad's fixed port with adb and connects to it
/// on localhost.
enum Usb {
    static let port: UInt16 = 47823

    static var endpoint: NWEndpoint {
        .hostPort(host: "127.0.0.1", port: NWEndpoint.Port(rawValue: port)!)
    }

    /// Sets up the forward; false when adb or a USB-debugging Pad is missing.
    static func forward() -> Bool {
        let candidates = ["/opt/homebrew/bin/adb", "/usr/local/bin/adb",
                          NSHomeDirectory() + "/Library/Android/sdk/platform-tools/adb"]
        guard let adb = candidates.first(where: FileManager.default.isExecutableFile) else {
            return false
        }
        let task = Process()
        task.executableURL = URL(fileURLWithPath: adb)
        task.arguments = ["forward", "tcp:\(port)", "tcp:\(port)"]
        task.standardOutput = FileHandle.nullDevice
        task.standardError = FileHandle.nullDevice
        do { try task.run() } catch { return false }
        task.waitUntilExit()
        return task.terminationStatus == 0
    }
}
