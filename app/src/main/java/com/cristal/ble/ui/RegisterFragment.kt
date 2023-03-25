package com.cristal.ble.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cristal.ble.R
import com.cristal.ble.api.ApiRepository
import com.cristal.ble.api.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {

    private var mListener: FragmentInteractionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi(view)
    }

    private fun initUi(view: View) {

        val username: EditText = view.findViewById(R.id.ed_username)
        val email: EditText = view.findViewById(R.id.ed_email)
        val password: EditText = view.findViewById(R.id.ed_password)

        view.findViewById<View>(R.id.bt_signup).setOnClickListener {
            register(username.text.toString(), email.text.toString(), password.text.toString(), )
        }

        view.findViewById<View>(R.id.bt_login).setOnClickListener {
            mListener?.signin()
        }
    }

    private fun register(username: String, email: String, password: String) {

        ApiRepository.register(username, email, password, object : Callback<RegisterResponse>{
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {

                response.body()?.let {
                    mListener?.signin()
                } ?: Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
            }
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is FragmentInteractionListener){
            mListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface FragmentInteractionListener{
        fun signin()
    }

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }
}