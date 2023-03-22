package com.cristal.ble.api

/**
 * {
 *  "email": DUMMY_EMAIL,
 *  "password": DUMMY_PASS
 * }
 * */
data class LoginRequest(
    val email: String,
    val password: String
)