package com.cristal.ble.ui.imageList.placeholder

import android.util.Base64
import com.cristal.ble.api.ApiRepository
import com.cristal.ble.api.CristalaudioBookResponce
import com.cristal.ble.api.cristalcloudImgResponce
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<PlaceholderItem> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, PlaceholderItem> = HashMap()

    private val COUNT = 10

    init {
        getCristalCloudImages();

        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createPlaceholderItem(i,""))
        }
    }


    private fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createPlaceholderItem(position: Int,bookname: String): PlaceholderItem {
        return PlaceholderItem(position.toString(),bookname, makeDetails(position))
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(val id: String, val content: String, val details: String) {
        override fun toString(): String = content
    }


    public fun getCristalCloudImages(){



        System.out.println("---> getCristalCloudImages");
        ApiRepository.getcristalaudiobooks( "userId","userToken", object :
            retrofit2.Callback<CristalaudioBookResponce> {
            override fun onResponse(call: retrofit2.Call<CristalaudioBookResponce>, response: retrofit2.Response<CristalaudioBookResponce>) {

                response.body()?.let {


                    if(it.success) {
                        it.data.forEachIndexed { index, element ->
                            System.out.println("---> getCristalCloudImages: "+ index +" - "+element.book_name);
//                            addItem(createPlaceholderItem(index, element.book_name))
                        }
                    }
                } ?: System.out.println("---> get GeoWifiRadio failed");
            }

            override fun onFailure(call: retrofit2.Call<CristalaudioBookResponce>, t: Throwable) {

                System.out.println("---> getCristalCloudImages 444 ERROR" + t);


            }
        })
        System.out.println("---> getCristalCloudImages end");

    }

}