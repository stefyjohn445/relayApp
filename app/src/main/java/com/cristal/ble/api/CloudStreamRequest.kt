package com.cristal.ble.api

/**
 * { 'deviceId': "deviceid",
 *  'userId':DUMMY_EMAIL,
 *  'coludSorce':'soundcloud',
 *  'coludUrl':'https://soundcloud.com/sophus-stein/sets/test_songs/s-1QkQvjMr9xt'
 * }
 * */
data class CloudStreamRequest(
    val coludSorce: String,
    val coludUrl: String,
    val deviceId: String,
    val userId: String
)