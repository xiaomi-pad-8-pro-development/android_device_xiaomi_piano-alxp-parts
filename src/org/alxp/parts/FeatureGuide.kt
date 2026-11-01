/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.alxp.parts

import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.POST_NOTIFICATIONS
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.annotation.StringRes

private const val ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"

/** Something the feature needs before it can work, and where to fix it. */
class Check(
    @StringRes val title: Int,
    val ok: (Context) -> Boolean,
    val fix: ((Context) -> Intent)? = null,
    val permissions: List<String> = emptyList(),
)

/** The page text, checks and setup steps of one feature. */
class FeatureGuide(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val checks: List<Check>,
    val steps: List<Int>,
) {
    companion object {
        private fun bluetooth(context: Context) =
            context.getSystemService(BluetoothManager::class.java).adapter

        private val bluetoothOn = Check(R.string.check_bluetooth,
            { bluetooth(it).isEnabled },
            { Intent(Settings.ACTION_BLUETOOTH_SETTINGS) })

        private fun permissions(vararg names: String) = Check(R.string.check_permissions,
            { context -> names.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED } },
            permissions = names.toList())

        private val network = Check(R.string.check_network, { context ->
            val manager = context.getSystemService(ConnectivityManager::class.java)
            manager.getNetworkCapabilities(manager.activeNetwork)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        }, { Intent(Settings.ACTION_WIFI_SETTINGS) })

        private val airPodsPaired = Check(R.string.check_airpods_paired, { context ->
            runCatching {
                bluetooth(context).bondedDevices.any { (it.name ?: "").contains("AirPods", true) }
            }.getOrDefault(false)
        }, { Intent(Settings.ACTION_BLUETOOTH_SETTINGS) })

        private val romPatch = Check(R.string.check_rom_patch,
            { NativeIntegration.init(it); NativeIntegration.isAvailable() })

        private val mac = listOf(network, permissions(POST_NOTIFICATIONS, ACCESS_LOCAL_NETWORK))

        fun of(feature: Feature) = when (feature) {
            Feature.AIRPODS_SWITCHING -> FeatureGuide(R.string.airpods_title,
                R.string.airpods_summary,
                listOf(romPatch, bluetoothOn, permissions(POST_NOTIFICATIONS), airPodsPaired),
                listOf(R.string.airpods_step_1, R.string.airpods_step_2,
                    R.string.airpods_step_3, R.string.airpods_step_4))
            Feature.IPHONE_NOTIFICATIONS -> FeatureGuide(R.string.iphone_title,
                R.string.iphone_description,
                listOf(bluetoothOn, permissions(POST_NOTIFICATIONS, BLUETOOTH_ADVERTISE,
                    BLUETOOTH_CONNECT, BLUETOOTH_SCAN)),
                listOf(R.string.iphone_step_1, R.string.iphone_step_2,
                    R.string.iphone_step_3, R.string.iphone_step_4))
            Feature.MAC_CLIPBOARD -> FeatureGuide(R.string.mac_clipboard_title,
                R.string.mac_clipboard_summary, mac,
                listOf(R.string.mac_step_install, R.string.mac_step_open,
                    R.string.mac_step_pair, R.string.mac_clipboard_step))
            Feature.MAC_DISPLAY -> FeatureGuide(R.string.mac_display_title,
                R.string.mac_display_summary, mac,
                listOf(R.string.mac_step_install, R.string.mac_step_open,
                    R.string.mac_step_pair, R.string.mac_display_step_permissions,
                    R.string.mac_display_step))
        }
    }
}

/** One line for the feature's card: off, waiting or connected. */
fun Feature.statusLine(context: Context): String {
    if (!isOn(context)) return context.getString(R.string.status_off)
    val text = Status.text(context, service) ?: return context.getString(R.string.status_starting)
    return context.getString(
        if (Status.connected(context, service)) R.string.status_connected else R.string.status_waiting,
        text)
}
