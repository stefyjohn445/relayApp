package com.cristal.ble.api

/**
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
data class CloudStreamResponse(
    val `data`: Data,
    val success: Boolean
)

data class Data(
    val coludSorce:String ,
    val coludUrls: Array<String>,
    val songs: Array<String>
)