package com.cristal.ble.api

data class CristallGetCurrentSongNameResponce(
    val `data`: data_resp,
    val success: Boolean
)

data class data_resp(

    val music_name: String,
    val img:String

)