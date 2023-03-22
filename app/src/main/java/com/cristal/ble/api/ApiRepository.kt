package com.cristal.ble.api

import retrofit2.Callback


class ApiRepository {

    companion object {

        fun register(
            email: String,
            password: String,
            username: String,
            listener: Callback<RegisterResponse>
        ) {

            val req = RegisterRequest(
                email = email,
                password = password,
                username = username
            )

            val apiInterface: APIService = RetrofitClient.getClient(RetrofitClient.url).create(APIService::class.java)
            apiInterface.register(req = req).enqueue(listener)
        }

        fun login(
            email: String,
            password: String,
            listener: Callback<LoginResponse>,
        ) {

            val req = LoginRequest(
                email = email,
                password = password
            )

            val apiInterface: APIService = RetrofitClient.getClient(RetrofitClient.url).create(APIService::class.java)
            apiInterface.login(req = req).enqueue(listener)
        }

        fun cloudStream(
            listener: Callback<CloudStreamResponse>
        ) {

            val req = CloudStreamRequest(
                coludSorce = "deviceid",
                coludUrl = "DUMMY_EMAIL",
                deviceId = "soundcloud",
                userId = "https://soundcloud.com/sophus-stein/sets/test_songs/s-1QkQvjMr9xt"
            )

            val apiInterface: APIService = RetrofitClient.getClient(RetrofitClient.url).create(APIService::class.java)
            apiInterface.cloudStream(req = req).enqueue(listener)
        }
    }
}