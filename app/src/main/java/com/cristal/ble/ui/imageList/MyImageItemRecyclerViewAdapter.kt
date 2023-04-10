package com.cristal.ble.ui.imageList

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.cristal.ble.AppPreference
import com.cristal.ble.R
import com.cristal.ble.api.ApiRepository
import com.cristal.ble.api.CristalNextAudioBookFromAppResponce
import com.cristal.ble.api.audiobook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * [RecyclerView.Adapter] that can display a [audiobook].
 * TODO: Replace the implementation with code for your data type.
 */
class MyImageItemRecyclerViewAdapter(
    private var values: List<audiobook>,
) : RecyclerView.Adapter<MyImageItemRecyclerViewAdapter.ViewHolder>() {

    var callback: audionbookInterface? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.bookId.toString()
        holder.contentView.text = item.book_name

        Glide.with(holder.image)
            .load( Base64.decode(item.img, Base64.DEFAULT))
//            .load("data:image/jpeg;base64,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")
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).placeholder(R.drawable.device_item_row)
                .error(R.drawable.device_item_row))
            .into(holder.image)


        holder.image.setOnClickListener {
            Toast.makeText(holder.image.context, "Clicked $position", Toast.LENGTH_SHORT).show()
            System.out.println("----> audio boos: "+values[position]);
            setnextAudioBook(holder,item.book_name,item.bookId,item.audioIds[0])

        }
    }

    override fun getItemCount(): Int = values.size
    fun update(items: MutableList<audiobook>) {
        values = items
        notifyDataSetChanged()
    }


    class ViewHolder(var view: View) : RecyclerView.ViewHolder(
        view) {
        val idView: TextView
        val contentView: TextView
        val image: ImageView

        init {
            idView = view.findViewById(R.id.item_number)
            contentView = view.findViewById(R.id.content)
            image = view.findViewById(R.id.im_image)
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

  fun setnextAudioBook(holder: ViewHolder,bookname : String, bookId  : Int,audioid: Int){


      AppPreference.preference!!.loginResponse?.user?.let {
          AppPreference.preference!!.loginResponse?.let { it1 ->
              ApiRepository.setnextaudiobookfromapp("abcd", it.email,bookname,bookId,audioid,null,
                  it1.token, object :
                      Callback<CristalNextAudioBookFromAppResponce> {
                      override fun onResponse(call: Call<CristalNextAudioBookFromAppResponce>, response: Response<CristalNextAudioBookFromAppResponce>) {

                          response.body()?.let {

                              System.out.println("---> GeoWifiRadio 3333"+it);
                              callback?.setdaudiobook(it.data.bookname,it.data.listofaudios)


                          } ?: Toast.makeText(holder.image.context, "aded to server", Toast.LENGTH_SHORT).show()
                      }

                      override fun onFailure(call: Call<CristalNextAudioBookFromAppResponce>, t: Throwable) {

                          System.out.println("---> GeoWifiRadio 444 ERROR" + t);
                          Toast.makeText(holder.image.context, "erorr with ading the next audio", Toast.LENGTH_SHORT).show()

                      }
                  })
          }
      }

  }

}