package com.cristal.ble.api

data class CristalCloudSongListResponse(

    val `data`: songlistdata,
    val success: Boolean
)


data class songlistdata(
    val start : Int,
    val end: Int,
    val Songlist: ArrayList<String>
)