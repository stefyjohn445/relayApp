package com.cristal.ble.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cristal.ble.AppPreference
import com.cristal.ble.R
import com.cristal.ble.adapter.PlaylistAdapter
import com.cristal.ble.api.ApiRepository
import com.cristal.ble.api.CristalCloudSongListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CloudPlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CloudPlaylistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var progressDialog: ProgressDialog? = null

    var cloudplayList = ArrayList<String>()
    private lateinit var recyclerView: RecyclerView
    var mPlaylistAdapter: PlaylistAdapter? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        getCristalSongList("","","${AppPreference.preference!!.loginResponse?.token}");
//        cloudplayList.add("test.mp3");

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cloud_playlist, container, false)
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
////        initUi(view)
//    }

    private fun initUi(view: View) {

        progressDialog = ProgressDialog(context)

        // Pull down to refresh
        swipeRefreshLayout = view.findViewById<View>(R.id.cloud_device_swipe_refresh) as SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary)
        swipeRefreshLayout.setVisibility(View.VISIBLE)
        swipeRefreshLayout.setOnRefreshListener { //Pull down to refresh to achieve BLE scanning function

        }


        recyclerView = view.findViewById<View>(R.id.cloud_device_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        mPlaylistAdapter = PlaylistAdapter(cloudplayList)
//        mPlaylistAdapter?.setOnDeviceClickListener(object : PlaylistAdapter.OnPlaylistClickListener {
//            override fun onSelect(song: String?) {
//                // TODO: 26/03/23 play this user selected sone
//                System.out.println("selected song ->> "+song);
////                if (song != null) {
////                    mListener?.sendCommand(song)
////
////                };
//            }
//        })
        recyclerView.adapter = mPlaylistAdapter
    }

    private fun getCristalSongList(deviceId: String, userId: String,userToken:String) {

        System.out.println("---> getCristalSongList 444"+userToken);

        ApiRepository.CristalCloudSongList(deviceId, userId,userToken,1,3, object :
            Callback<CristalCloudSongListResponse> {
            override fun onResponse(call: Call<CristalCloudSongListResponse>, response: Response<CristalCloudSongListResponse>) {

                response.body()?.let {

//                    System.out.println("---> getCristalSongList 3333"+it.data.Songlist);
                    for(i in it?.data.Songlist) {

                        cloudplayList.add(i);

                    }

//                    System.out.println(cloudplayList)
                    System.out.println("---> getCristalSongList 3333"+cloudplayList);

//                    cloudplayList.add(String(data).substring(1))


                } ?: Toast.makeText(context, "get getCristalSongList failed", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<CristalCloudSongListResponse>, t: Throwable) {

                System.out.println("---> getCristalSongList 444 ERROR" + t);


            }
        })
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CloudPlaylistFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CloudPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}