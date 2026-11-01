/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.alxp.parts

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import com.android.settingslib.widget.FooterPreference

/** One feature: its switch, live status, what it needs and how to set it up. */
class FeatureActivity : CollapsingToolbarBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val feature = Feature.entries.first { it.key == intent.getStringExtra(EXTRA_FEATURE) }
        title = getString(FeatureGuide.of(feature).title)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                    FeatureFragment().apply {
                        arguments = Bundle().apply { putString(EXTRA_FEATURE, feature.key) }
                    })
                .commit()
        }
    }

    companion object {
        const val EXTRA_FEATURE = "feature"
    }
}

class FeatureFragment : PreferenceFragmentCompat() {
    private lateinit var feature: Feature
    private lateinit var guide: FeatureGuide
    private lateinit var switch: SwitchPreferenceCompat
    private lateinit var status: Preference
    private val checks = mutableListOf<Pair<Check, Preference>>()

    private val statusListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        activity?.runOnUiThread { refresh() }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        feature = Feature.entries.first { it.key == requireArguments().getString(FeatureActivity.EXTRA_FEATURE) }
        guide = FeatureGuide.of(feature)
        val screen = preferenceManager.createPreferenceScreen(context)
        preferenceScreen = screen

        switch = SwitchPreferenceCompat(context).apply {
            isPersistent = false
            title = getString(R.string.feature_use, getString(guide.title))
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                val on = value as Boolean
                if (on) requestMissingPermissions()
                feature.setOn(requireContext(), on)
                refresh()
                true
            }
        }
        screen.addPreference(switch)

        status = Preference(context).apply {
            title = getString(R.string.status_title)
            isSelectable = false
        }
        screen.addPreference(status)

        val needs = PreferenceCategory(context).apply { title = getString(R.string.checks_title) }
        screen.addPreference(needs)
        for (check in guide.checks) {
            val item = Preference(context).apply {
                title = getString(check.title)
                setOnPreferenceClickListener {
                    when {
                        check.permissions.isNotEmpty() -> requestMissingPermissions()
                        check.fix != null -> startActivity(check.fix.invoke(requireContext()))
                    }
                    true
                }
            }
            needs.addPreference(item)
            checks += check to item
        }

        val steps = PreferenceCategory(context).apply { title = getString(R.string.steps_title) }
        screen.addPreference(steps)
        guide.steps.forEachIndexed { index, step ->
            steps.addPreference(Preference(context).apply {
                title = getString(R.string.step_number, index + 1)
                summary = getString(step, deviceName())
                isSelectable = false
            })
        }

        screen.addPreference(FooterPreference(context).apply {
            key = "footer"
            title = getString(guide.description, deviceName())
        })
    }

    private fun deviceName(): String = runCatching {
        requireContext().getSystemService(android.bluetooth.BluetoothManager::class.java).adapter.name
    }.getOrNull() ?: "Pad"

    private fun requestMissingPermissions() {
        val missing = guide.checks.flatMap { it.permissions }.distinct().filter {
            requireContext().checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 0)
    }

    override fun onRequestPermissionsResult(code: Int, permissions: Array<String>, results: IntArray) {
        refresh()
    }

    override fun onResume() {
        super.onResume()
        Status.listen(requireContext(), statusListener)
        refresh()
    }

    override fun onPause() {
        Status.unlisten(requireContext(), statusListener)
        super.onPause()
    }

    private fun refresh() {
        val context = context ?: return
        switch.isChecked = feature.isOn(context)
        status.summary = feature.statusLine(context)
        for ((check, item) in checks) {
            val ok = check.ok(context)
            item.summary = getString(if (ok) R.string.check_ok else R.string.check_needed)
            item.isSelectable = !ok && (check.fix != null || check.permissions.isNotEmpty())
        }
        // AirPods switching cannot work without the ROM's Bluetooth patch.
        if (feature == Feature.AIRPODS_SWITCHING) {
            NativeIntegration.init(context)
            switch.isEnabled = NativeIntegration.isAvailable()
            if (!switch.isEnabled) status.summary = getString(R.string.airpods_unavailable)
        }
    }
}
