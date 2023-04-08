package com.cristal.ble.api

data class CristalSetNextSongfromAppResponce(

    val `data`: repdata,
    val success: Boolean
)
data class repdata(
    val music_name:String
)