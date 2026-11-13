# ALXP Features for the Xiaomi Pad 8 Pro (piano)

| | |
| --- | --- |
| Path | `device/xiaomi/piano-alxp-parts` |
| Optional | yes |

`ALXPParts`: AirPods switching, iPhone notifications (ANCS) and Mac link,
one switch each under **ALXP Features** in Settings. Include `alxpparts.mk`;
the Bluetooth change it needs is in `rom-patches/`, applied by
`vendorsetup.sh`. The Mac side is in `mac/`.

Details: [docs/DETAILS.md](docs/DETAILS.md).
