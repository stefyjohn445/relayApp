package com.cristal.ble.api

import retrofit2.Callback


class ApiRepository {

    companion object {

        fun register(
            username: String,
            email: String,
            password: String,
            listener: Callback<RegisterResponse>
        ) {

            val req = RegisterRequest(
                username = username,
                email = email,
                password = password
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

        fun GeoWifiRadio(
            deviceId : String,
            userId   : String,
            listener: Callback<GeoWifiRadioResponse>
        ) {

            val req = GeoWifiRadioRequst(

                deviceId = deviceId,
                userId = userId
            )

            val apiInterface: APIService = RetrofitClient.getClient(RetrofitClient.url).create(APIService::class.java)
            apiInterface.geoWifiRadio(req = req).enqueue(listener)
        }
    }
}

