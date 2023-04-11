package com.cristal.ble.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cristal.ble.R
import com.cristal.ble.adapter.PlaylistAdapter


/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistFragment : Fragment()  {

    private var playlist_source: Int = 0
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
            playlist_source = it.getInt("PLAYLIST_SOURCE")
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)

        val addSongs = view.findViewById<TextView>(R.id.tv_add_songs)


        addSongs.visibility = if (playlist_source == 5) View.VISIBLE else View.GONE

        addSongs.setOnClickListener {
            Toast.makeText(context, "Add songs", Toast.LENGTH_SHORT).show()

            val dialog = Dialog(context)

            dialog.setContentView(R.layout.layout_add_songs)
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setCancelable(false)
//            val cloudsongurls : EditText? = dialog.findViewById<View>(R.id.song_url) as EditText;

            var cloud_url: EditText = dialog.findViewById<View>(R.id.song_url) as EditText

//            dialog.getWindow().getAttributes().windowAnimations = R.style.animation



            dialog.findViewById<TextView>(R.id.bt_done).setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                val url = cloud_url.text.toString();
                System.out.println("user uld:"+cloud_url.text.toString());
                if (url != null) {
                    mListener?.sendCommand("set:$url")

                };
                Toast.makeText(context, "okay clicked ", Toast.LENGTH_SHORT).show()

            })

            dialog.show()
        }
        return view
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
                    mListener?.sendCommand("get:$song")

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

    fun update(playList: java.util.ArrayList<String>) {

        if (mPlaylistAdapter != null) {
            mPlaylistAdapter?.update(playList);
        }
    }

    interface FragmentInteractionListener {
        fun sendCommand(url: String)
    }


    companion object {
        @JvmStatic
        fun newInstance(playlist: ArrayList<String>, imageSource: Int) = PlaylistFragment()
            .apply {
                arguments = Bundle()
                    .apply {
                        putStringArrayList("PLAYLIST", playlist)
                        putInt("PLAYLIST_SOURCE", imageSource)
                    }
            }
    }


}