package com.cristal.ble

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cristal.ble.api.ApiRepository
import com.cristal.ble.api.GeoWifiRadioResponse
import android.location.LocationManager
import com.cristal.ble.ui.LoginFragment
import com.cristal.ble.ui.PlaylistFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*





// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var currentCircle:Circle? =null
    var mMap: GoogleMap? = null
    private var mListener: FragmentInteractionListener? = null


    // below are the latitude and longitude
    // of 4 different locations.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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
        val mp= inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        return mp;
    }

    private fun addMarker(Geoapirespose : GeoWifiRadioResponse) {

        val height = 30
        val width = 30
        val b = BitmapFactory.decodeResource(resources,R.drawable.radio_location)
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        val smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker)



        for (i in Geoapirespose.data.wifiradio!!.indices){

//            System.out.println("---> GeoWifiRadio 444 "+Geoapirespose.data.wifiradio[i])
            System.out.println("---> GeoWifiRadio  "+LatLng(Geoapirespose.data.wifiradio[i].coord[0].toDouble(),
                Geoapirespose.data.wifiradio[i].coord[1].toDouble()
            ))

            mMap!!.addMarker(
                MarkerOptions().position(LatLng(Geoapirespose.data.wifiradio[i].coord[0].toDouble(),
                    Geoapirespose.data.wifiradio[i].coord[1].toDouble()
                ))
                    .title(Geoapirespose.data.wifiradio[i].ip).icon(smallMarkerIcon)
            )



        }

        //   below line is use to move camera.
//        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(TamWorth) )

        // adding on click listener to marker of google maps.
        mMap!!.setOnMarkerClickListener { marker -> // on marker click we are getting the title of our marker
            // which is clicked and displaying it in a toast message.
            val markerName = marker.title

            System.out.println("----------------->markerName"+marker.position)

            currentCircle?.remove()

             currentCircle = mMap!!.addCircle(
                CircleOptions()
                    .center(marker.position)
                    .radius(30000.0)
                    .strokeColor(Color.WHITE)

//                    .fillColor(Color.BLUE)
            )
            if (markerName != null) {
                mListener?.sendWifiRadioUrlToDevice(markerName)
            };


            false
        }
    }

    private fun GeoWifiRadio(deviceId: String, userId: String) {

        ApiRepository.GeoWifiRadio(deviceId, userId, object : Callback<GeoWifiRadioResponse> {
            override fun onResponse(call: Call<GeoWifiRadioResponse>, response: Response<GeoWifiRadioResponse>) {

                response.body()?.let {

                    System.out.println("---> GeoWifiRadio 3333"+it);
                    addMarker(it);


                } ?: Toast.makeText(context, "get GeoWifiRadio failed", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<GeoWifiRadioResponse>, t: Throwable) {

                System.out.println("---> GeoWifiRadio 444 ERROR" + t);


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
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        println("""-login onMapReady ${AppPreference.preference!!.loginResponse}""".trimIndent())
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(12.9716, 77.5946), 0f))
        GeoWifiRadio("amma","amma");


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
        fun sendWifiRadioUrlToDevice(url: String)
    }


}
