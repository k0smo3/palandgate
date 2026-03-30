package me.csystems.gategarage.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import me.csystems.palandgate.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.title_settings)

        val etToken       = findViewById<TextInputEditText>(R.id.etSessionToken)
        val etPhone       = findViewById<TextInputEditText>(R.id.etPhoneNumber)
        val actvTokenType = findViewById<AutoCompleteTextView>(R.id.actvTokenType)
        val etDeviceId    = findViewById<TextInputEditText>(R.id.etDeviceId)
        val btnSave       = findViewById<Button>(R.id.btnSave)

        val tokenTypeItems = resources.getStringArray(R.array.token_types)
        actvTokenType.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tokenTypeItems)
        )

        CredentialsStore(this).loadRaw().let { raw ->
            etToken.setText(raw.sessionToken)
            etPhone.setText(raw.phoneNumber)
            actvTokenType.setText(tokenTypeItems.getOrNull(raw.tokenType) ?: tokenTypeItems[1], false)
            etDeviceId.setText(raw.gateDeviceId)
        }

        btnSave.setOnClickListener {
            val tokenHex     = etToken.text?.toString()?.trim()?.uppercase() ?: ""
            val phone        = etPhone.text?.toString()?.trim() ?: ""
            val type         = tokenTypeItems.indexOf(actvTokenType.text.toString()).takeIf { it >= 0 } ?: 1
            val gateDeviceId = etDeviceId.text?.toString()?.trim() ?: ""

            when {
                tokenHex.length != 32 || !tokenHex.all { it.isLetterOrDigit() } ->
                    toast("Session token must be a 32-character hex string")
                phone.toLongOrNull() == null ->
                    toast("Phone number must be numeric (e.g. 972501234567)")
                gateDeviceId.isEmpty() ->
                    toast("Gate Device ID is required")
                else -> {
                    CredentialsStore(this).save(tokenHex, phone, type, gateDeviceId)
                    toast("Saved")
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
