package com.cristal.ble.api

data class GetCurrentAudiobookResponce(

    val data:currentaudio,
    val success : Boolean
)

data class currentaudio(
    val book_name :String,
    val audio_name:String,
    val img: String,
    val audios:Array<String>

)