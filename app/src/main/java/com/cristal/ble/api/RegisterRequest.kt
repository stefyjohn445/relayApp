package com.cristal.ble.api

/**
 * {
 *  "username": DUMMY_USERNAME,
 *  "email": DUMMY_EMAIL,
 *  "password": DUMMY_PASS
 * }
 * */
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)