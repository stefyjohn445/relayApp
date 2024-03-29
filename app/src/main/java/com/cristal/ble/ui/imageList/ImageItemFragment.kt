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
import com.cristal.ble.ui.imageList.placeholder.PlaceholderContent

/**
 * A fragment representing a list of Items.
 */
class ImageItemFragment : Fragment(), audionbookInterface {

    private var mListener: audionbookInterface? = null
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
                myImageItemRecyclerViewAdapter.callback = this@ImageItemFragment // change this to mListener if you don't want to get callback in this fragment and only ants in operation activity
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is audionbookInterface){
            mListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
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


    override fun setdaudiobook(data: String, listofaudios: Array<Int>) {
        System.out.println("---->setdaudiobook"+ data)
        mListener?.setdaudiobook(data,listofaudios)
    }
}