package com.cristal.ble.ui.imageList.placeholder

import com.cristal.ble.api.ApiRepository
import com.cristal.ble.api.CristalaudioBookResponce
import com.cristal.ble.api.audiobook

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
    var ITEMS: MutableList<audiobook> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<Int, audiobook> = HashMap()

    private fun addItem(item: audiobook) {
        ITEMS.add(item)
        ITEM_MAP.put(item.bookId, item)
    }

    private fun createPlaceholderItem(bookId: Int,audioIds: Array<Int>, book_name: String,img: String): audiobook {
        return audiobook(bookId, audioIds, book_name, img)
    }

    interface GetItemListener {
        fun onUpdate(ITEMS: MutableList<audiobook>)
    }


    fun getItems(getItemListener: GetItemListener) {

        ITEMS = ArrayList()

        System.out.println("---> getCristalCloudImages");
        ApiRepository.getcristalaudiobooks( "userId","userToken", object :
            retrofit2.Callback<CristalaudioBookResponce> {
            override fun onResponse(call: retrofit2.Call<CristalaudioBookResponce>, response: retrofit2.Response<CristalaudioBookResponce>) {

                response.body()?.let {


                    if(it.success) {
                        it.data.forEachIndexed { index, element ->
                            System.out.println("---> getCristalCloudImages: "+ index +" - "+element.book_name);
                            addItem(
                                createPlaceholderItem(
                                    element.bookId,
                                    element.audioIds,
                                    element.book_name,
                                    element.img
                                ))
                        }
                    }

                    getItemListener.onUpdate(ITEMS)
                } ?: System.out.println("---> get GeoWifiRadio failed");
            }

            override fun onFailure(call: retrofit2.Call<CristalaudioBookResponce>, t: Throwable) {

                System.out.println("---> getCristalCloudImages 444 ERROR" + t);


            }
        })
        System.out.println("---> getCristalCloudImages end");
    }

}