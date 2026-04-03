package com.exambrowser

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.exambrowser.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: AppPreferences

    // Triple tap detection for admin access
    private var tapCount = 0
    private var lastTapTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Block screenshots and screen recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        prefs = AppPreferences(this)

        // First run: go to config
        if (!prefs.isSetupDone) {
            startActivityForResult(Intent(this, ConfigActivity::class.java), REQUEST_CONFIG)
            return
        }

        initUI()
    }

    private fun initUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullscreen()
        setupWebView()
        setupAdminTapDetection()
        startKioskMode()

        val url = prefs.examUrl
        if (url.isNotEmpty()) {
            binding.webView.loadUrl(url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun setupWebView() {
        val webView = binding.webView

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            userAgentString = "Mozilla/5.0 (Linux; Android) ExamBrowser/1.0"
            cacheMode = WebSettings.LOAD_DEFAULT
            saveFormData = false
            @Suppress("DEPRECATION")
            savePassword = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        // Block long press (to prevent text selection popup)
        webView.setOnLongClickListener { true }
        webView.isLongClickable = false
        webView.isHapticFeedbackEnabled = false

        // Clear context menu
        webView.setOnCreateContextMenuListener { menu, _, _ -> menu.clear() }

        webView.webViewClient = ExamWebViewClient(
            context = this,
            allowedUrls = prefs.allowedUrls,
            allowAll = prefs.allowAllUrls,
            onBlockedUrl = { url ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Akses diblokir: $url",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = newProgress
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onJsAlert(
                view: WebView?, url: String?, message: String?, result: JsResult?
            ): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("OK") { _, _ -> result?.confirm() }
                    .setCancelable(false)
                    .show()
                return true
            }

            override fun onJsConfirm(
                view: WebView?, url: String?, message: String?, result: JsResult?
            ): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("Ya") { _, _ -> result?.confirm() }
                    .setNegativeButton("Tidak") { _, _ -> result?.cancel() }
                    .setCancelable(false)
                    .show()
                return true
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean = true
        }
    }

    // Triple tap on the lock indicator to open admin settings
    private fun setupAdminTapDetection() {
        binding.tvExamMode.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastTapTime > 2000) tapCount = 0
            tapCount++
            lastTapTime = now
            if (tapCount >= 5) {
                tapCount = 0
                openAdminSettings()
            }
        }
    }

    private fun openAdminSettings() {
        val intent = Intent(this, PinActivity::class.java)
        intent.putExtra(PinActivity.EXTRA_MODE, PinActivity.MODE_ADMIN)
        startActivityForResult(intent, REQUEST_ADMIN_PIN)
    }

    private fun setupFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun startKioskMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                try {
                    startLockTask()
                } catch (e: Exception) {
                    // Lock task not available without Device Owner, continue without it
                }
            }
        }
        // Keep screen on during exam
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun stopKioskMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                stopLockTask()
            } catch (e: Exception) {
                // Ignore
            }
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                showExitDialog()
                true
            }
            KeyEvent.KEYCODE_HOME -> true
            KeyEvent.KEYCODE_APP_SWITCH -> true
            KeyEvent.KEYCODE_MENU -> true
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP -> true // Block volume keys during exam
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Ujian")
            .setMessage("Pilih tindakan:")
            .setPositiveButton("Keluar Ujian") { _, _ ->
                val intent = Intent(this, PinActivity::class.java)
                intent.putExtra(PinActivity.EXTRA_MODE, PinActivity.MODE_EXIT)
                startActivityForResult(intent, REQUEST_EXIT_PIN)
            }
            .setNeutralButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CONFIG -> {
                if (resultCode == Activity.RESULT_OK) {
                    initUI()
                } else {
                    // User cancelled setup - exit app
                    finishAffinity()
                }
            }
            REQUEST_EXIT_PIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    stopKioskMode()
                    finishAffinity()
                }
            }
            REQUEST_ADMIN_PIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val intent = Intent(this, ConfigActivity::class.java)
                    intent.putExtra(ConfigActivity.EXTRA_IS_FIRST_SETUP, false)
                    startActivityForResult(intent, REQUEST_CONFIG_UPDATE)
                }
            }
            REQUEST_CONFIG_UPDATE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Reload WebView with updated settings
                    setupWebView()
                    val url = prefs.examUrl
                    if (url.isNotEmpty()) {
                        binding.webView.loadUrl(url)
                    }
                    Toast.makeText(this, "Pengaturan diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setupFullscreen()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) {
            setupFullscreen()
            startKioskMode()
        }
    }

    companion object {
        const val REQUEST_CONFIG = 100
        const val REQUEST_EXIT_PIN = 101
        const val REQUEST_ADMIN_PIN = 102
        const val REQUEST_CONFIG_UPDATE = 103
    }
}
