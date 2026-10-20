/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.alxp.parts

import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.POST_NOTIFICATIONS
import android.bluetooth.BluetoothManager
import org.alxp.parts.mac.MacLinkService
import org.alxp.parts.services.AncsService
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity

class AlxpSettingsActivity : CollapsingToolbarBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                    AlxpSettingsFragment())
                .commit()
        }
    }
}

private const val ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"

class AlxpSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.alxp_settings, rootKey)
        val context = requireContext()
        NativeIntegration.init(context)

        for (feature in Feature.entries) {
            findPreference<SwitchPreferenceCompat>(feature.key)!!.apply {
                isChecked = feature.isOn(context)
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                    if (value as Boolean) {
                        val wanted = mutableListOf(POST_NOTIFICATIONS)
                        // Bonjour advertising for the Mac needs local network access.
                        if (feature.service == MacLinkService::class.java) wanted += ACCESS_LOCAL_NETWORK
                        // ANCS advertises and connects as a Bluetooth LE peripheral.
                        if (feature.service == AncsService::class.java) {
                            wanted += listOf(BLUETOOTH_ADVERTISE, BLUETOOTH_CONNECT, BLUETOOTH_SCAN)
                        }
                        val missing = wanted.filter {
                            context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
                        }
                        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 0)
                    }
                    feature.setOn(context, value)
                    true
                }
            }
        }

        findPreference<SwitchPreferenceCompat>(Feature.AIRPODS_SWITCHING.key)!!.apply {
            if (!NativeIntegration.isAvailable()) {
                isEnabled = false
                setSummary(R.string.airpods_unavailable)
            }
        }
        val name = context.getSystemService(BluetoothManager::class.java).adapter.name
        findPreference<SwitchPreferenceCompat>(Feature.IPHONE_NOTIFICATIONS.key)!!.summary =
            getString(R.string.iphone_summary, name)
    }
}
