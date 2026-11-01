/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.alxp.parts

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
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

/** The feature cards; each shows its state and opens its own page. */
class AlxpSettingsFragment : PreferenceFragmentCompat() {

    private val statusListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        activity?.runOnUiThread { refresh() }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.alxp_settings, rootKey)
        for (feature in Feature.entries) {
            findPreference<Preference>(feature.key)!!.intent =
                Intent(requireContext(), FeatureActivity::class.java)
                    .putExtra(FeatureActivity.EXTRA_FEATURE, feature.key)
        }
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
        for (feature in Feature.entries) {
            findPreference<Preference>(feature.key)?.summary = feature.statusLine(context)
        }
    }
}
