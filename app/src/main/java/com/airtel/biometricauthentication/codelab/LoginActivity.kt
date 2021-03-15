package com.airtel.biometricauthentication.codelab

import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.airtel.biometricauthentication.MainActivity
import com.airtel.biometricauthentication.R
import com.airtel.biometricauthentication.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginActivity: AppCompatActivity() {
    private val TAG = "LoginActivity"
    private lateinit var biometricPrompt:BiometricPrompt
    private val cryptographyManager = CryptographyManager()
    private val ciphertextWrapper
     get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
         applicationContext,
         SHARED_PREFS_FILENAME,
         Context.MODE_PRIVATE,
         CIPHERTEXT_WRAPPER
     )
    private lateinit var binding: ActivityLoginBinding
    private val loginWithPasswordViewModel by viewModels<LoginWithPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkCanBiometricAuthenticate()
        if (ciphertextWrapper == null) {
            setupForLoginWithPassword()
        }
    }

    private fun checkCanBiometricAuthenticate() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS){
            binding.useBiometrics.visibility = View.VISIBLE
            setClickListeners()
        }else
        {
            binding.useBiometrics.visibility = View.INVISIBLE
        }
    }

    private fun setClickListeners() {
        binding.useBiometrics.setOnClickListener {
            if (ciphertextWrapper != null) {
                showBiometricPromptForDecryption()
            } else {
                startActivity(Intent(this, EnableBiometricLoginActivity::class.java))
            }
        }
        if (ciphertextWrapper == null) {
            setupForLoginWithPassword()
        }
    }

    override fun onResume() {
        super.onResume()
        if(ciphertextWrapper != null){
            if(SampleAppUser.fakeToken == null){
                showBiometricPromptForDecryption()
            }else{
                updateApp(getString(R.string.already_signedin))
            }
        }
    }

    // BIOMETRICS SECTION

    private fun showBiometricPromptForDecryption() {
        ciphertextWrapper?.let { textWrapper ->
            val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                val secretKeyName = getString(R.string.secret_key_name)
                val cipher = cryptographyManager.getInitializedCipherForDecryption(
                    secretKeyName, textWrapper.initializationVector
                )
                biometricPrompt =
                    BiometricPromptUtils.createBiometricPrompt(
                        this,
                        ::decryptServerTokenFromStorage
                    )
                val promptInfo = BiometricPromptUtils.createPromptInfo(this)
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plaintext = cryptographyManager.decryptData(textWrapper.ciphertext, it)
                SampleAppUser.fakeToken = plaintext
                // Now that you have the token, you can query server for everything else
                // the only reason we call this fakeToken is because we didn't really get it from
                // the server. In your case, you will have gotten it from the server the first time
                // and therefore, it's a real token.

                updateApp(getString(R.string.already_signedin))
            }
        }
    }

    // USERNAME + PASSWORD SECTION

    private fun setupForLoginWithPassword() {
        loginWithPasswordViewModel.loginWithPasswordFormState.observe(this, Observer { formState ->
            val loginState = formState ?: return@Observer
            when (loginState) {
                is SuccessfulLoginFormState -> binding.login.isEnabled = loginState.isDataValid
                is FailedLoginFormState -> {
                    loginState.usernameError?.let { binding.username.error = getString(it) }
                    loginState.passwordError?.let { binding.password.error = getString(it) }
                }
            }
        })
        loginWithPasswordViewModel.loginResult.observe(this, Observer {
            val loginResult = it ?: return@Observer
            if (loginResult.success) {
                updateApp(
                    "You successfully signed up using password as: user " +
                            "${SampleAppUser.username} with fake token ${SampleAppUser.fakeToken}"
                )
            }
        })
        binding.username.doAfterTextChanged {
            loginWithPasswordViewModel.onLoginDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
        }
        binding.password.doAfterTextChanged {
            loginWithPasswordViewModel.onLoginDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
        }
        binding.password.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE ->
                    loginWithPasswordViewModel.login(
                        binding.username.text.toString(),
                        binding.password.text.toString()
                    )
            }
            false
        }
        binding.login.setOnClickListener {
            loginWithPasswordViewModel.login(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
        }
        Log.d(TAG, "Username ${SampleAppUser.username}; fake token ${SampleAppUser.fakeToken}")
    }


    private fun updateApp(successMsg: String) {
        binding.success.text = successMsg
        Snackbar.make(binding.root,"App Authenticated Successfully",Snackbar.LENGTH_LONG).show()
    }
}