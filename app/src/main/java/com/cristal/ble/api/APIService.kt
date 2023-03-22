package com.cristal.ble.api

import org.json.JSONObject
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
    @POST("/api/app/cloudstream")
    fun cloudStream(
        @Body req: CloudStreamRequest?
    ): Call<CloudStreamResponse>
}