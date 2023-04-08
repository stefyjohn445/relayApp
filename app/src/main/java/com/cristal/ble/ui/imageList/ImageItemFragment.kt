package com.cristal.ble.ui.imageList

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cristal.ble.R
import com.cristal.ble.api.audiobook
import com.cristal.ble.ui.PlaylistFragment
import com.cristal.ble.ui.imageList.placeholder.PlaceholderContent

/**
 * A fragment representing a list of Items.
 */
class ImageItemFragment : Fragment() {

    private lateinit var myImageItemRecyclerViewAdapter: MyImageItemRecyclerViewAdapter
    private var columnCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
//                    else -> GridLayoutManager(context, columnCount)
                    else -> StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL)
                }
//                myImageItemRecyclerViewAdapter = MyImageItemRecyclerViewAdapter(PlaceholderContent.ITEMS)
                myImageItemRecyclerViewAdapter = MyImageItemRecyclerViewAdapter(listOf())
//                myImageItemRecyclerViewAdapter.callback = this@ImageItemFragment
                adapter = myImageItemRecyclerViewAdapter


                PlaceholderContent.getItems(object: PlaceholderContent.GetItemListener{
                    override fun onUpdate(ITEMS: MutableList<audiobook>) {
                        myImageItemRecyclerViewAdapter.update(PlaceholderContent.ITEMS)
                    }
                })
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ImageItemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

//    override fun setdaudiobook(data: String) {
//        TODO("Not yet implemented")
//        System.out.println("---->setdaudiobook"+data)
//    }
}