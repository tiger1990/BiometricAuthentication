package com.airtel.biometricauthentication.codelab

/**
 * Data validation state of the login form.
 */

sealed class LoginFormState

data class FailedLoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null
) : LoginFormState()

data class SuccessfulLoginFormState(
    val isDataValid: Boolean = false
) : LoginFormState()