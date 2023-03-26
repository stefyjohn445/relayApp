package com.cristal.ble.ui.player

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.cristal.ble.R

class MenuPopupWindow(val context: AppCompatActivity) : View.OnClickListener{

    private val TAG_MENU: String = "Menu"
    private val mMenuPopupWindow: PopupWindow
    private lateinit var listener: MenuListener
    private lateinit var tvWifiConfig: AppCompatTextView
    private lateinit var tvSelectSource: AppCompatTextView
    private lateinit var tvAbout: AppCompatTextView

    interface MenuListener {
        fun onClick(view: View)
        fun onDismiss()
    }

    init {

        val popUpView: View = context.layoutInflater.inflate(R.layout.popup_menu, null)
        mMenuPopupWindow = PopupWindow(
            popUpView, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT, true
        )

        mMenuPopupWindow.animationStyle = android.R.style.Animation_Dialog
        mMenuPopupWindow.setBackgroundDrawable(BitmapDrawable())
        mMenuPopupWindow.isOutsideTouchable = true
        mMenuPopupWindow.setOnDismissListener { listener.onDismiss() }

        initViews(popUpView)
    }

    private fun initViews(popUpView: View) {
        with(popUpView) {

            tvWifiConfig = findViewById(R.id.menuWifiConfig)
            tvSelectSource = findViewById(R.id.menuSelectSource)
            tvAbout = findViewById(R.id.menuAbout)

            tvWifiConfig.setOnClickListener(this@MenuPopupWindow)
            tvSelectSource.setOnClickListener(this@MenuPopupWindow)
            tvAbout.setOnClickListener(this@MenuPopupWindow)
        }
    }

    fun showAsDropDown(view: View) {
        mMenuPopupWindow.showAsDropDown(view, -300, 30)
    }

    fun setMenuListener(onClickListener: MenuListener) {
        this.listener = onClickListener
    }

    fun isShowing(): Boolean {
        return mMenuPopupWindow.isShowing
    }

    fun show() {

        tvWifiConfig.visibility = View.VISIBLE
        tvSelectSource.visibility = View.VISIBLE
        tvAbout.visibility = View.VISIBLE
    }

    override fun onClick(view: View?) {
        view?.let {

            Log.e(
                TAG_MENU, when (view.id) {
                    R.id.menuWifiConfig -> "menuWifiConfig"
                    R.id.menuSelectSource -> "menuSelectSource"
                    R.id.menuAbout -> "menuAbout"
                    else -> ""
                }
            )

            listener.onClick(it)
        }
    }

    fun dismiss() {
        mMenuPopupWindow.dismiss()
    }
}