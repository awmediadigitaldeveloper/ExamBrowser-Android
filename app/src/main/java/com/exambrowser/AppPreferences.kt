package com.exambrowser

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "exam_browser_prefs"
        const val DEFAULT_EXIT_PIN = "1234"
        const val DEFAULT_ADMIN_PIN = "admin123"
    }

    var examUrl: String
        get() = prefs.getString("exam_url", "") ?: ""
        set(value) = prefs.edit().putString("exam_url", value).apply()

    var allowedUrls: List<String>
        get() {
            val raw = prefs.getString("allowed_urls", "") ?: ""
            return if (raw.isEmpty()) emptyList()
            else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        set(value) = prefs.edit().putString("allowed_urls", value.joinToString(",")).apply()

    var exitPin: String
        get() = prefs.getString("exit_pin", DEFAULT_EXIT_PIN) ?: DEFAULT_EXIT_PIN
        set(value) = prefs.edit().putString("exit_pin", value).apply()

    var adminPin: String
        get() = prefs.getString("admin_pin", DEFAULT_ADMIN_PIN) ?: DEFAULT_ADMIN_PIN
        set(value) = prefs.edit().putString("admin_pin", value).apply()

    var isSetupDone: Boolean
        get() = prefs.getBoolean("setup_done", false)
        set(value) = prefs.edit().putBoolean("setup_done", value).apply()

    var allowAllUrls: Boolean
        get() = prefs.getBoolean("allow_all_urls", false)
        set(value) = prefs.edit().putBoolean("allow_all_urls", value).apply()
}
