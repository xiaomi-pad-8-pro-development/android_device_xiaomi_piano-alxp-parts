# SPDX-License-Identifier: Apache-2.0
# ALXP Features, with its Settings entry.
PRODUCT_SOONG_NAMESPACES += device/xiaomi/piano-alxp-parts
PRODUCT_PACKAGES += ALXPParts
PRODUCT_COPY_FILES += \
    device/xiaomi/piano-alxp-parts/privapp-permissions-alxpparts.xml:$(TARGET_COPY_OUT_SYSTEM_EXT)/etc/permissions/privapp-permissions-alxpparts.xml \
    device/xiaomi/piano-alxp-parts/default-permissions-alxpparts.xml:$(TARGET_COPY_OUT_SYSTEM_EXT)/etc/default-permissions/default-permissions-alxpparts.xml
