package com.cristal.ble

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.cristal.ble.api.LoginResponse
import com.google.gson.Gson

class AppPreference(context: Context?) {

    private var userPreference: SharedPreferences? = null

    private var mContext: Context? = context

    fun log() {
        Log.d("USERPROFILE", userPreference?.all?.toList()!!.joinToString())
    }

    fun logout() {

        userPreference!!.edit().clear().apply()
        Log.d("USERPROFILE", userPreference?.all?.toList()!!.joinToString())
    }

    var loginResponse: LoginResponse?
        get() {
            var obj: LoginResponse? = null
            val gson = Gson()
            val json = userPreference!!.getString(USER_DETAILS_LOGIN_RESPONSE, null)
            json?.let {
                obj = gson.fromJson(json, LoginResponse::class.java)
            }
            return obj
        }
        set(value) {
            val gson = Gson()
            val json = gson.toJson(value)
            userPreference!!.edit()!!.putString(USER_DETAILS_LOGIN_RESPONSE, json).apply()
        }

    val userProfileDetails: String
        get() {
            return """
--> UserProfile
${userPreference?.all?.toList()?.joinToString("\n")}
<-- UserProfile
        """.trimIndent()
        }

    companion object {

        //  User profile
        private const val USER_DETAILS = "USER_DETAILS"
        private const val USER_DETAILS_LOGIN_RESPONSE = "USER_DETAILS_LOGIN_RESPONSE"

        @JvmField
        var preference: AppPreference? = null
    }

    init {
        if (context != null) {
            userPreference = mContext?.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE)
            preference = this
        }
    }
}