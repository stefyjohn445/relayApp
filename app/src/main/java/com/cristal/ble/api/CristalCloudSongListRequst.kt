package com.cristal.ble.api

data class CristalCloudSongListRequst(

    val devId: String,
    val userId: String,
    val start:Int,
    val end:Int
)
