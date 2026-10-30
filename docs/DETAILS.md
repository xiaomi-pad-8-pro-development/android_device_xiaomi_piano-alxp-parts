# ALXP Features for the Xiaomi Pad 8 Pro (piano)

Personal extras for the Pad, as its own app with its own **ALXP Features**
entry in Settings, next to Xiaomi features. Each feature is one
switch on that page:

- **AirPods automatic switching** with iPhone and Mac.
- **iPhone notifications** on the Pad through Apple Notification Center
  Service (ANCS), the way a smartwatch gets them.
- **Mac shared clipboard**: text and images, both ways.
- **Mac second display**: the Pad as an extra Mac display, like Sidecar.

The Mac features need **ALXP for Mac**, a menu bar app in `mac/`. Build it
on the Mac with `mac/make-app.sh` and open `mac/build/ALXP.app`. It finds the
Pad over Bonjour on the same Wi-Fi, or over USB through `adb` when the
network blocks device discovery (USB debugging on, `adb` installed); the first time, the Mac menu shows a
six-digit code and the Pad asks you to confirm it. The display needs Screen
Recording and Accessibility permission on the Mac. **Open at Login** in its
menu starts it with the Mac.

The app is GPL-3.0-or-later. AirPods switching is trimmed from LibrePods (see UPSTREAM),
with the original copyright notices kept in the source.

Only the switching part of LibrePods is kept: the AACP control channel,
ownership messages, "moved to" requests and new device introductions. The
LibrePods app, its UI libraries, billing, Xposed, battery, noise control,
EQ and head tracking are not included.

## Build

Built from source with the ROM by Soong (`Android.bp`). Sync it to
`device/xiaomi/piano-alxp-parts`; the
[device tree](https://github.com/xiaomi-pad-8-pro-development/android_device_xiaomi_piano)
includes `alxpparts.mk` when it is present. Leave it unsynced to build
without it.

AirPods switching needs the Bluetooth change in
`rom-patches/packages_modules_Bluetooth`; `vendorsetup.sh` applies it to
`packages/modules/Bluetooth` when `build/envsetup.sh` is sourced.

## Use

Settings > ALXP Features > turn on **Automatic switching**. That enables
the service, turns on the Apple Bluetooth identity and restarts Bluetooth.
Pair the AirPods with the Pad: playing media or taking a call on the Pad
moves them to it, and playing on an iPhone or Mac moves them back. If the
AirPods dropped the Pad, it reconnects them first.

Turning it off stops the service, clears the identity and restarts
Bluetooth.

## Tested

Pad and Mac switching both ways with AirPods Max. AirPods Max drop the
Pad when the Mac takes them, which the reconnect handles.
