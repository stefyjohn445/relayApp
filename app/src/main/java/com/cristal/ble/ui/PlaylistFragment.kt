package com.cristal.ble.ui

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cristal.ble.R
import com.cristal.ble.adapter.PlaylistAdapter
import com.cristal.ble.ui.player.OperationActivity


/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistFragment : Fragment()  {

    private lateinit var playlist: java.util.ArrayList<String>
    private val TAG = "MainActivity"

    private var mListener: FragmentInteractionListener? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    var mPlaylistAdapter: PlaylistAdapter? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
           playlist = it.getStringArrayList("PLAYLIST") ?: ArrayList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi(view)
    }

    private fun initUi(view: View) {

        progressDialog = ProgressDialog(context)

        // Pull down to refresh
        swipeRefreshLayout = view.findViewById<View>(R.id.device_swipe_refresh) as SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary)
        swipeRefreshLayout.setVisibility(View.VISIBLE)
        swipeRefreshLayout.setOnRefreshListener { //Pull down to refresh to achieve BLE scanning function

        }


        recyclerView = view.findViewById<View>(R.id.device_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        mPlaylistAdapter = PlaylistAdapter(playlist)
        mPlaylistAdapter?.setOnDeviceClickListener(object : PlaylistAdapter.OnPlaylistClickListener {
            override fun onSelect(song: String?) {
                // TODO: 26/03/23 play this user selected sone
                System.out.println("selected song ->> "+song);
                if (song != null) {
                    mListener?.sendCommand(song)

                };
            }
        })
        recyclerView.adapter = mPlaylistAdapter
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is FragmentInteractionListener) {
            mListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface FragmentInteractionListener {
        fun sendCommand(url: String)
    }

    companion object {
        @JvmStatic
        fun newInstance(playlist: ArrayList<String>) = PlaylistFragment()
            .apply {
                arguments = Bundle()
                    .apply {
                        putStringArrayList("PLAYLIST", playlist)
                    }
            }
    }
}