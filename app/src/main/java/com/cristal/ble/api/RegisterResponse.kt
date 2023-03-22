package com.cristal.ble.api

/**
 * {
 *  "success": false,
 *  "msg": "Email already taken"
 * }
 * */
data class RegisterResponse(
    val msg: String,
    val success: Boolean
)