package com.example.straymaps.ui.screens.sign_up

import android.util.Patterns
import java.util.regex.Pattern

/** These functions check whether the email and password that the user entered while
 *  creating their account are valid:
 *  email must not be blank and has to match the email pattern
 *  password must not be blank and has to be at least 6 characters long, and must contain one number,
 *  one lower case letter, one upper case letter, and can't contain whitespace characters
 *
 */
private const val MIN_PASSWORD_LENGTH = 6
private const val PASS_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=//S+$).{4,}$"

fun String.isEmailValid(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isPasswordValid(): Boolean {
    return this.isNotBlank() &&
            this.length >= MIN_PASSWORD_LENGTH &&
            Pattern.compile(PASS_PATTERN).matcher(this).matches()
}