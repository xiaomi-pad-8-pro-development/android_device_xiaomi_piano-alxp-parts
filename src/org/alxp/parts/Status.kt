/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.alxp.parts

import android.app.Service
import android.content.Context
import android.content.SharedPreferences

/**
 * What each feature service is doing right now, for the settings pages. The
 * services write the same text they show in their notification; the pages
 * listen for changes.
 */
object Status {
    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences("status", Context.MODE_PRIVATE)

    fun set(context: Context, service: Class<out Service>, text: String, connected: Boolean) {
        prefs(context).edit()
            .putString(service.name, text)
            .putBoolean(service.name + ".connected", connected)
            .apply()
    }

    fun clear(context: Context, service: Class<out Service>) {
        prefs(context).edit().remove(service.name).remove(service.name + ".connected").apply()
    }

    fun text(context: Context, service: Class<out Service>): String? =
        prefs(context).getString(service.name, null)

    fun connected(context: Context, service: Class<out Service>) =
        prefs(context).getBoolean(service.name + ".connected", false)

    fun listen(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs(context).registerOnSharedPreferenceChangeListener(listener)

    fun unlisten(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs(context).unregisterOnSharedPreferenceChangeListener(listener)
}
