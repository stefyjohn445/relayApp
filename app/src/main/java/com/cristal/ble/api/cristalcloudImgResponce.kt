package com.cristal.ble.api

data class cristalcloudImgResponce(


    val `data`: imagedata,
    @kotlin.jvm.JvmField
    val success: Boolean
)
data class imagedata(
    val name:String,
    val img:String
)