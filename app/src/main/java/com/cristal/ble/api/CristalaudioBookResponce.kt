package com.cristal.ble.api

data class CristalaudioBookResponce(
    val `data`: Array<audiobook>,
    val success: Boolean
)


data class audiobook(

    val book_name:String,
    val img:String,
    val bookId: Int,
    val audioIds:Array<Int>

)