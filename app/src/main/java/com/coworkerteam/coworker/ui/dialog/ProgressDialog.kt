package com.coworkerteam.coworker.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.coworkerteam.coworker.R
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.style.FadingCircle

class ProgressDialog{
    var mDialogView: View? = null
    var mBuilder: AlertDialog? = null

    fun showDialog(context: Context) {
        mDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_progress, null)
        mBuilder = AlertDialog.Builder(context).setView(mDialogView).show()

        //로딩창을 투명하게
        mBuilder!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //취소 못하게
        mBuilder!!.setCancelable(false)

        mDialogView?.findViewById<SpinKitView>(R.id.spin_kit)?.setIndeterminateDrawable(FadingCircle())
    }

    fun dismissDialog() {
        mBuilder?.dismiss()
        mDialogView = null
        mBuilder = null
    }

}