package com.cristal.ble.api

import retrofit2.Call
import retrofit2.http.*


interface APIService {

    /**
     * post Tests /users/register API
     * url+"api/users/register"
     * req:
     * {
     * "username": DUMMY_USERNAME,
     * "email": DUMMY_EMAIL,
     * "password": DUMMY_PASS
     * }
     *
     * res:
     * {
     *  "success": false,
     *  "msg": "Email already taken"
     * }
     *
     * invalid:
     * {"success": false, "msg": "'' is too short"}
     *
     * --> POST http://192.168.0.114:3001/api/users/register
     * Content-Type: application/json; charset=UTF-8
     * Content-Length: 76
     * {"email":"binil.vrgs@gmail.com","password":"password","username":"username"}
     * --> END POST (76-byte body)
     * <-- 200 OK http://192.168.0.114:3001/api/users/register (378ms)
     * Server: Werkzeug/2.1.2 Python/3.10.0
     * Date: Sat, 25 Mar 2023 19:13:34 GMT
     * Content-Type: application/json
     * Content-Length: 92
     * Access-Control-Allow-Origin: *
     * Connection: close
     * {
     *  "success": true,
     *  "userID": 2,
     *  "msg": "The user was successfully registered"
     * }
     * <-- END HTTP (92-byte body)
     * */
    @POST("/api/users/register")
    fun register(
        @Body req: RegisterRequest?
    ): Call<RegisterResponse>

    /**
     * post Tests /users/login API
     * url+"api/users/login"
     * req:
     * {
     * "email": DUMMY_EMAIL,
     * "password": DUMMY_PASS
     * }
     *
     * res:
     * {
     *  "success": true,
     *  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFtbWFAbmVkZXJpZy5jb20iLCJleHAiOjE2Nzk1MDk2ODJ9.fZR3YlG8d_3lfPFY3cK3RpiGxNKMZB6QXAHqabmOK9M",
     *  "user": {
     *      "_id": 1,
     *      "username": "amma",
     *      "email": "amma@nederig.com"
     *  }
     * }
     *
     * invalid:
     * {
     *  "success": false,
     *  "msg": "Wrong credentials."
     * }
     *
     * --> POST http://192.168.0.114:3001/api/users/login
     * Content-Type: application/json; charset=UTF-8
     * Content-Length: 54
     * {"email":"binil.vrgs@gmail.com","password":"password"}
     * --> END POST (54-byte body)
     * <-- 200 OK http://192.168.0.114:3001/api/users/login (401ms)
     * Server: Werkzeug/2.1.2 Python/3.10.0
     * Date: Sat, 25 Mar 2023 19:27:54 GMT
     * Content-Type: application/json
     * Content-Length: 299
     * Access-Control-Allow-Origin: *
     * Connection: close
     * {
     *     "success": true,
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImJpbmlsLnZyZ3NAZ21haWwuY29tIiwiZXhwIjoxNjc5Nzc0Mjc0fQ.r9D4fuM4wtKmHcQgE8pSxJ407fhnhE2J-LHoLgye5u0",
     *     "user": {
     *         "_id": 2,
     *         "username": "username",
     *         "email": "binil.vrgs@gmail.com"
     *     }
     * }
     * <-- END HTTP (299-byte body)
     * */
    @POST("/api/users/login")
    fun login(
        @Body req: LoginRequest?
    ): Call<LoginResponse>

    /**
     * post Tests /users/cloudstream API
     * url+"api/users/cloudstream"
     *
     * req:
     * { 'deviceId': "deviceid",
     *  'userId':DUMMY_EMAIL,
     *  'coludSorce':'soundcloud',
     *  'coludUrl':'https://soundcloud.com/sophus-stein/sets/test_songs/s-1QkQvjMr9xt'
     * }
     *
     * res:
     * {
     *     "success": true,
     *     "data": {
     *         "deviceId": "deviceid",
     *         "userId": "amma@nederig.com",
     *         "coludSorce": "soundcloud",
     *         "coludUrl": "https://soundcloud.com/sophus-stein/sets/test_songs/s-1QkQvjMr9xt"
     *     }
     * }
     * */


@GET("/api/app/cloudstream")
    fun cloudStream(
        @Header("Authorization") userToken: String,
        @Query("devId") devId: String?,
        @Query("userId") userId: String?,
        @Query("coludSorce") coludSorce: String?,
        @Query("SetGet") SetGet:String?,
        @Query("coludUrl") coludUrl: String?,

    ): Call<CloudStreamResponse>


    @POST("/api/app/getwifiradio")
    fun geoWifiRadio(
        @Body req: GeoWifiRadioRequst?
    ): Call<GeoWifiRadioResponse>



    @GET("/api/app/getcristalcloudsonglist")
    fun getcristalcloudsonglist(
        @Header("Authorization") userToken: String,
        @Query("devId") devId: String?,
        @Query("userId") userId: String?,
        @Query("start") start: Int?,
        @Query("end") end: Int?,


        ): Call<CristalCloudSongListResponse>


//    @GET("/api/v1/resources/clinical-facilities/{cfId}/patients/{patientId}")
//    fun getcristalcloudimg(
//        @Header("Authorization") userToken: String,
//        @Path("userId") userId: String?,
//        @Path("deviceId") deviceId: String?
//    ): Call<cristalcloudImgResponce?>

    @GET("/api/app/getcristalcloudimg")
    fun getcristalcloudimg(

        @Header("Authorization") userToken: String,
        @Query("userId") userId: String?,
        @Query("devId") deviceId: String?,
        @Query("imagename") imagename: String?

    ): Call<cristalcloudImgResponce>

    @GET("/api/app/getcristalaudiobooks")
    fun getcristalaudiobooks(
        @Header("Authorization") userToken: String,
        @Query("userId") userId: String?

    ): Call<CristalaudioBookResponce>

    @GET("/api/app/getcurrentsong")
    fun getcurrentsong(

        @Header("Authorization") userToken: String,
        @Query("userId") userId: String?,
        @Query("devId") deviceId: String?,
        @Query("src") src: String?

        ): Call<CristallGetCurrentSongNameResponce>

    @GET("/api/app/nextsong")
    fun setnextsngfromapp(
        @Header("Authorization") userToken: String,
        @Query("userId") userId: String?,
        @Query("src") src: String?,
        @Query("devId") deviceId: String?,
        @Query("music_name") music_name: String?
    ): Call<CristalSetNextSongfromAppResponce>


    @GET("/api/app/setnextaudiobookfromapp")
    fun setnextaudiobookfromapp(
        @Header("Authorization") userToken: String,
        @Query("deviceId") deviceId: String?,
        @Query("devId") userId: String?,
        @Query("bookname") bookname: String?,
        @Query("bookId") bookId: Int?,
        @Query("audioid") audioid: Int?,
        @Query("audio") audio: String?,
    ): Call<CristalNextAudioBookFromAppResponce>

    @GET("/api/app/getcurrentaudiobookfromapp")
    fun getcurrentaudiobookfromapp(

        @Header("Authorization") userToken: String,
        @Query("userId") userId: String?,
        @Query("devId") deviceId: String?
    ): Call<GetCurrentAudiobookResponce>

}