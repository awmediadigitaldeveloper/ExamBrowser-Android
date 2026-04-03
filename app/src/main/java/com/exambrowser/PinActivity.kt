package com.exambrowser

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.exambrowser.databinding.ActivityPinBinding

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private lateinit var prefs: AppPreferences
    private var mode: Int = MODE_EXIT
    private var enteredPin = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        mode = intent.getIntExtra(EXTRA_MODE, MODE_EXIT)

        setupUI()
    }

    private fun setupUI() {
        binding.tvTitle.text = when (mode) {
            MODE_EXIT -> "Masukkan PIN untuk Keluar Ujian"
            MODE_ADMIN -> "Masukkan PIN Admin"
            else -> "Masukkan PIN"
        }

        setupNumpad()
    }

    private fun setupNumpad() {
        binding.btn0.setOnClickListener { addDigit("0") }
        binding.btn1.setOnClickListener { addDigit("1") }
        binding.btn2.setOnClickListener { addDigit("2") }
        binding.btn3.setOnClickListener { addDigit("3") }
        binding.btn4.setOnClickListener { addDigit("4") }
        binding.btn5.setOnClickListener { addDigit("5") }
        binding.btn6.setOnClickListener { addDigit("6") }
        binding.btn7.setOnClickListener { addDigit("7") }
        binding.btn8.setOnClickListener { addDigit("8") }
        binding.btn9.setOnClickListener { addDigit("9") }

        binding.btnDelete.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
                updatePinDisplay()
            }
        }

        binding.btnOk.setOnClickListener { verifyPin() }

        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun addDigit(digit: String) {
        if (enteredPin.length < 12) {
            enteredPin.append(digit)
            updatePinDisplay()
        }
    }

    private fun updatePinDisplay() {
        binding.tvPinDisplay.text = "*".repeat(enteredPin.length)
    }

    private fun verifyPin() {
        if (enteredPin.isEmpty()) {
            Toast.makeText(this, "Masukkan PIN terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val correctPin = when (mode) {
            MODE_EXIT -> prefs.exitPin
            MODE_ADMIN -> prefs.adminPin
            else -> prefs.exitPin
        }

        if (enteredPin.toString() == correctPin) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "PIN salah! Coba lagi.", Toast.LENGTH_SHORT).show()
            enteredPin.clear()
            updatePinDisplay()
        }
    }

    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_EXIT = 0
        const val MODE_ADMIN = 1
    }
}
