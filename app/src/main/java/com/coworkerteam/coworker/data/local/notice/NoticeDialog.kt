package com.coworkerteam.coworker.data.local.notice

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import com.coworkerteam.coworker.R
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.style.FadingCircle

class NoticeDialog {

    var mDialogView: View? = null
    var mBuilder: AlertDialog? = null

    fun showDialog(context: Context) {
        mDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_notice, null)
        mBuilder = AlertDialog.Builder(context).setView(mDialogView).show()

        //로딩창을 투명하게
        mBuilder!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //취소 못하게
        mBuilder!!.setCancelable(false)

        mDialogView?.findViewById<SpinKitView>(R.id.spin_kit)?.setIndeterminateDrawable(FadingCircle())
        mDialogView?.findViewById<Button>(R.id.dialog_camstudyout_btn_ok)?.setOnClickListener(
            View.OnClickListener {
                dismissDialog()
                ActivityCompat.finishAffinity(context as Activity)
                System.runFinalization();
                System.exit(0);
            }
        )
        mDialogView?.findViewById<TextView>(R.id.limit_time)?.setText("목요일 저녁 8시 ~ 금요일 새벽 6시")

    }
    fun dismissDialog() {
        mBuilder?.dismiss()
        mDialogView = null
        mBuilder = null
    }
}