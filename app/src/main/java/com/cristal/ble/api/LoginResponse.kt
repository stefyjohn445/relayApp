package com.cristal.ble.api

/**
 * {
 *     "success": true,
 *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFtbWFAbmVkZXJpZy5jb20iLCJleHAiOjE2Nzk1MDk2ODJ9.fZR3YlG8d_3lfPFY3cK3RpiGxNKMZB6QXAHqabmOK9M",
 *     "user": {
 *         "_id": 1,
 *         "username": "amma",
 *         "email": "amma@nederig.com"
 *     }
 * }
 * */
data class LoginResponse(
    val success: Boolean,
    val msg: String?,
    val token: String,
    val user: User
)

data class User(
    val _id: Int,
    val email: String,
    val username: String
)