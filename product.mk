# SPDX-License-Identifier: Apache-2.0
# Installed whenever this repository is synced; WITH_ALXP_PARTS := false skips it.
ifneq ($(WITH_ALXP_PARTS),false)
PRODUCT_PACKAGES += ALXPParts
PRODUCT_COPY_FILES += \
    device/xiaomi/piano-alxp-parts/privapp-permissions-alxpparts.xml:$(TARGET_COPY_OUT_SYSTEM_EXT)/etc/permissions/privapp-permissions-alxpparts.xml \
    device/xiaomi/piano-alxp-parts/default-permissions-alxpparts.xml:$(TARGET_COPY_OUT_SYSTEM_EXT)/etc/default-permissions/default-permissions-alxpparts.xml
endif
