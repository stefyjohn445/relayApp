package com.cristal.ble.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cristal.ble.AppPreference
import com.cristal.ble.R
import com.cristal.ble.api.ApiRepository
import com.cristal.ble.api.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private var mListener: FragmentInteractionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi(view)
    }

    private fun initUi(view: View) {

        val email: EditText = view.findViewById(R.id.ed_email)
        val password: EditText = view.findViewById(R.id.ed_password)

        view.findViewById<View>(R.id.bt_login).setOnClickListener {
            login(email.text.toString(), password.text.toString(), )
        }

        view.findViewById<View>(R.id.bt_signup).setOnClickListener {
            mListener?.signup()
        }
    }

    private fun login(email: String, password: String) {

        ApiRepository.login(email, password, object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                response.body()?.let {
                    AppPreference.preference?.loginResponse = it
                    mListener?.onLoginSuccess()
                } ?: Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
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
        fun onLoginSuccess()
        fun signup()
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}