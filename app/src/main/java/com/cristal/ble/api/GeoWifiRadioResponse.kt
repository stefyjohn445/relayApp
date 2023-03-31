package com.cristal.ble.api

data class GeoWifiRadioResponse(
    val data:data,
    val succsess : Boolean

)



data class data(
    val wifiradio: Array<wifiradio>,
)


data class wifiradio(
    val coord : FloatArray,
    val url   : String,
    val ip    : String,
    val img   : String

)
