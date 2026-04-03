package com.exambrowser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.exambrowser.databinding.ActivityConfigBinding

class ConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigBinding
    private lateinit var prefs: AppPreferences
    private var isFirstSetup = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        isFirstSetup = intent.getBooleanExtra(EXTRA_IS_FIRST_SETUP, !prefs.isSetupDone)

        setupUI()
    }

    private fun setupUI() {
        if (isFirstSetup) {
            binding.tvTitle.text = "Setup Awal Exam Browser"
            binding.btnCancel.visibility = View.GONE
        } else {
            binding.tvTitle.text = "Pengaturan Admin"
            binding.btnCancel.visibility = View.VISIBLE
        }

        loadCurrentSettings()

        binding.btnSave.setOnClickListener { saveSettings() }

        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.cbAllowAllUrls.setOnCheckedChangeListener { _, isChecked ->
            binding.tilAllowedUrls.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
    }

    private fun loadCurrentSettings() {
        binding.etExamUrl.setText(prefs.examUrl)
        binding.etAllowedUrls.setText(prefs.allowedUrls.joinToString("\n"))
        binding.etExitPin.setText(prefs.exitPin)
        binding.etAdminPin.setText(prefs.adminPin)
        binding.cbAllowAllUrls.isChecked = prefs.allowAllUrls

        binding.tilAllowedUrls.visibility =
            if (prefs.allowAllUrls) View.GONE else View.VISIBLE
    }

    private fun saveSettings() {
        val examUrl = binding.etExamUrl.text.toString().trim()
        val allowedUrlsText = binding.etAllowedUrls.text.toString().trim()
        val exitPin = binding.etExitPin.text.toString().trim()
        val adminPin = binding.etAdminPin.text.toString().trim()
        val allowAll = binding.cbAllowAllUrls.isChecked

        if (examUrl.isEmpty()) {
            binding.etExamUrl.error = "URL ujian tidak boleh kosong!"
            return
        }

        if (exitPin.length < 4) {
            binding.etExitPin.error = "PIN keluar minimal 4 digit!"
            return
        }

        if (adminPin.length < 4) {
            binding.etAdminPin.error = "PIN admin minimal 4 digit!"
            return
        }

        // Auto-add https:// if no protocol
        val finalUrl = when {
            examUrl.startsWith("http://") || examUrl.startsWith("https://") -> examUrl
            else -> "https://$examUrl"
        }

        prefs.examUrl = finalUrl
        prefs.allowedUrls = allowedUrlsText
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        prefs.exitPin = exitPin
        prefs.adminPin = adminPin
        prefs.allowAllUrls = allowAll
        prefs.isSetupDone = true

        Toast.makeText(this, "Pengaturan disimpan!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    companion object {
        const val EXTRA_IS_FIRST_SETUP = "is_first_setup"
    }
}
