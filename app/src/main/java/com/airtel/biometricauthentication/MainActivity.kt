package com.airtel.biometricauthentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.airtel.biometricauthentication.codelab.CryptographyManager
import com.airtel.biometricauthentication.codelab.LoginActivity
import com.airtel.biometricauthentication.databinding.ActivityMainBinding
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val TAG = "Biometric"
    private lateinit var binding: ActivityMainBinding
    private lateinit var textInputView: AppCompatEditText
    private lateinit var textOutputView: AppCompatTextView
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var readyToEncrypt: Boolean = false
    private lateinit var cryptographyManager: CryptographyManager
    private lateinit var secretKeyName: String
    private lateinit var ciphertext:ByteArray
    private lateinit var initializationVector: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textInputView = binding.inputView
        textOutputView = binding.outputView
        checkBiometricInDevice()
    }

    private fun checkBiometricInDevice() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                initBiometric()
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.e("MY_APP_TAG", "The user hasn't associated " +
                        "any biometric credentials with their account.")
        }
    }

    private fun initBiometric() {
        cryptographyManager = CryptographyManager()
        // e.g. secretKeyName = "biometric_sample_encryption_key"
        secretKeyName = getString(R.string.secret_key_name)
        biometricPrompt = createBiometricPrompt()
        promptInfo = createPromptInfo()

        binding.encryptButton.setOnClickListener{ authenticateToEncrypt() }
        binding.decryptButton.setOnClickListener{ authenticateToDecrypt() }
        binding.loginActivity.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "$errorCode :: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                processData(result.cryptoObject)
            }
        }

        //The API requires the client/Activity context for displaying the prompt view
        val biometricPrompt = BiometricPrompt(this, executor, callback)
        return biometricPrompt
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_info_title)) // e.g. "Sign in"
            .setSubtitle(getString(R.string.prompt_info_subtitle)) // e.g. "Biometric for My App"
            .setDescription(getString(R.string.prompt_info_description)) // e.g. "Confirm biometric to continue"
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.prompt_info_use_app_password)) // e.g. "Use Account Password"
            //.setDeviceCredentialAllowed(true) // Allow PIN/pattern/password authentication.
            // Also note that setDeviceCredentialAllowed and setNegativeButtonText are
            // incompatible so that if you uncomment one you must comment out the other
            .build()
        return promptInfo
    }

    private fun authenticateToEncrypt() {
        readyToEncrypt = true
        if (BiometricManager.from(applicationContext).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS)
        {
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun authenticateToDecrypt() {
        readyToEncrypt = false
        if (BiometricManager.from(applicationContext).canAuthenticate() == BiometricManager
                .BIOMETRIC_SUCCESS) {
            val cipher = cryptographyManager.getInitializedCipherForDecryption(secretKeyName,initializationVector)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }

    }

    private fun processData(cryptoObject: BiometricPrompt.CryptoObject?) {
        val data = if (readyToEncrypt) {
            val text = textInputView.text.toString()
            val encryptedData = cryptographyManager.encryptData(text, cryptoObject?.cipher!!)
            ciphertext = encryptedData.ciphertext
            initializationVector = encryptedData.initializationVector

            String(ciphertext, Charset.forName("UTF-8"))
        } else {
            cryptographyManager.decryptData(ciphertext, cryptoObject?.cipher!!)
        }
        textOutputView.text = data
    }
}