#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# Apply the Bluetooth change AirPods switching needs whenever
# build/envsetup.sh is sourced.
(cd "$(gettop)" && bash device/xiaomi/piano-alxp-parts/rom-patches/apply-patches.sh \
    piano-alxp-parts device/xiaomi/piano-alxp-parts/rom-patches) ||
    echo "piano-alxp-parts: applying ROM changes failed, see the errors above" >&2
