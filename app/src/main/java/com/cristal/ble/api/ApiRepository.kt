package com.cristal.ble.api

import retrofit2.Callback
import retrofit2.http.Query


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
        @JvmStatic

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


        @JvmStatic
        fun cloudStream(
            devId: String,
            userId : String,
            coludSorce: String,
            coludUrl: String,
            SetGet:String,
            userToken:String,
            listener: Callback<CloudStreamResponse>
        ) {

            val req = CloudStreamRequest(
                coludSorce = coludSorce,
                coludUrl = userId,
                deviceId = coludSorce,
                userId = coludUrl
            )

            val apiInterface: APIService = RetrofitClient.getClient(RetrofitClient.url).create(APIService::class.java)
            apiInterface.cloudStream(userId=userId, devId = devId, coludSorce = coludSorce, coludUrl = coludUrl, userToken = userToken, SetGet = SetGet).enqueue(listener)
        }

        @JvmStatic
        fun CristalCloudSongList(
            devId : String,
            userId   : String,
            userToken: String,
            start:Int,
            end:Int,
            listener: Callback<CristalCloudSongListResponse>
        ) {

            val req = CristalCloudSongListRequst(

                 devId = devId,
                 userId = userId,
                 start = start,
                 end = end
            )

            val apiInterface: APIService = RetrofitClient.postClientToken(RetrofitClient.url,userToken).create(APIService::class.java)
            apiInterface.getcristalcloudsonglist(userId=userId, devId=devId, start =start, end = end, userToken = userToken ).enqueue(listener)
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
                imagename = imagename
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
            src:String,
            userToken: String,
            listener: Callback<CristallGetCurrentSongNameResponce>
        ) {

            val apiInterface: APIService = RetrofitClient.postClientToken(RetrofitClient.url,userToken).create(APIService::class.java)

            apiInterface.getcurrentsong(userId = userId, deviceId = deviceId , userToken = userToken, src = src).enqueue(listener)

        }

        @JvmStatic
        fun setnextsngfromapp(
            deviceId : String,
            music_name: String,
            src       :String,
            userId   : String,
            userToken: String,
            listener: Callback<CristalSetNextSongfromAppResponce>
        ) {

            val apiInterface: APIService = RetrofitClient.getClient(RetrofitClient.url).create(APIService::class.java)

            apiInterface.setnextsngfromapp(userId = userId, deviceId = deviceId , src = src, userToken = userToken, music_name = music_name).enqueue(listener)

        }
        @JvmStatic
        fun setnextaudiobookfromapp(
            deviceId: String,
            userId: String,
            bookname: String,
            bookId: Int,
            audioid: Int,
            audio: String?,
            userToken:String,
            listener: Callback<CristalNextAudioBookFromAppResponce>
        ) {

            val apiInterface: APIService = RetrofitClient.getClient(RetrofitClient.url).create(APIService::class.java)

            apiInterface.setnextaudiobookfromapp(userId = userId, deviceId = deviceId , userToken = userToken, bookname = bookname , bookId = bookId, audioid = audioid ,audio =audio).enqueue(listener)

        }

        @JvmStatic
        fun Getcurrentaudiobookfromapp(
            deviceId : String,
            userId   : String,
            userToken: String,
            listener: Callback<GetCurrentAudiobookResponce>
        ) {

            val req = cristalcloudImgRequst(
                deviceId = deviceId,
                userId   = userId,
                imagename = ""
            )


            val apiInterface: APIService = RetrofitClient.postClientToken(RetrofitClient.url,userToken).create(APIService::class.java)
            apiInterface.getcurrentaudiobookfromapp(deviceId=deviceId, userId = userId, userToken = userToken).enqueue(listener)

        }


    }
}

