package com.cristal.ble.api

import android.media.session.MediaSession.Token
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

        fun CristalCloudSongList(
            deviceId : String,
            userId   : String,
            userToken: String,
            start:Int,
            end:Int,
            listener: Callback<CristalCloudSongListResponse>
        ) {

            val req = CristalCloudSongListRequst(

                 deviceId =deviceId,
                 userId = userId,
                 start = start,
                 end = end
            )

            val apiInterface: APIService = RetrofitClient.postClientToken(RetrofitClient.url,userToken).create(APIService::class.java)
            apiInterface.getcristalcloudsonglist(req = req).enqueue(listener)
        }

        fun GeoWifiRadio(
            deviceId : String,
            userId   : String,
            userToken: String,
            listener: Callback<GeoWifiRadioResponse>
        ) {

            val req = GeoWifiRadioRequst(

                deviceId = deviceId,
                userId = userId,

            )
//            with out toket
//            val apiInterface: APIService = RetrofitClient.getClient(RetrofitClient.url).create(APIService::class.java)

            val apiInterface: APIService = RetrofitClient.postClientToken(RetrofitClient.url,userToken).create(APIService::class.java)

            apiInterface.geoWifiRadio(req = req).enqueue(listener)
        }

        @JvmStatic
        fun GetCristalImg(
            deviceId : String,
            userId   : String,
            userToken: String,
            imagename : String,
            listener: Callback<cristalcloudImgResponce>
        ) {

            val req = cristalcloudImgRequst(
                deviceId = deviceId,
                userId   = userId,
                imagename = ""
            )


            val apiInterface: APIService = RetrofitClient.postClientToken(RetrofitClient.url,userToken).create(APIService::class.java)

//            apiInterface.getcristalcloudimg(req = req).enqueue(listener)
            apiInterface.getcristalcloudimg(deviceId=deviceId, userId = userId, userToken = userToken, imagename = imagename).enqueue(listener)

        }


        fun getcristalaudiobooks(
            userId   : String,
            userToken: String,
            listener: Callback<CristalaudioBookResponce>
        ) {

            val req = crstalaudioBookRequst(
                userId   = userId,
            )


            val apiInterface: APIService = RetrofitClient.postClientToken(RetrofitClient.url,userToken).create(APIService::class.java)

            apiInterface.getcristalaudiobooks(userId = userId, userToken = userToken).enqueue(listener)

        }

        @JvmStatic
        fun getcurrentsong(
            deviceId : String,
            userId   : String,
            userToken: String,
            listener: Callback<CristallGetCurrentSongNameResponce>
        ) {

            val apiInterface: APIService = RetrofitClient.postClientToken(RetrofitClient.url,userToken).create(APIService::class.java)

            apiInterface.getcurrentsong(userId = userId, deviceId = deviceId , userToken = userToken).enqueue(listener)

        }



    }
}

