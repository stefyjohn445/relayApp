package com.cristal.ble.api

data class CristalNextAudioBookFromAppResponce(

    val `data`: audiobookData,
    val success: Boolean
)

data class audiobookData(
    val listofaudios: Array<Int>
)