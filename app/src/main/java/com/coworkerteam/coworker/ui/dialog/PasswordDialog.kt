package com.coworkerteam.coworker.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.coworkerteam.coworker.R
import com.google.android.material.textfield.TextInputLayout

open class PasswordDialog() {
    var mDialogView: View? = null
    var mBuilder: AlertDialog? = null
    lateinit var onClickOKButton: (Int, String?) -> Unit

    fun showDialog(context: Context, studyIdx: Int) {
        mDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_camstudy_password, null)
        mBuilder = AlertDialog.Builder(context).setView(mDialogView).show()

        mBuilder!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        mDialogView?.findViewById<Button>(R.id.dialog_camstudy_password_btn_cancle)
            ?.setOnClickListener(View.OnClickListener {
                dismissDialog()
            })

        mDialogView?.findViewById<Button>(R.id.dialog_camstudy_password_btn_ok)
            ?.setOnClickListener(View.OnClickListener {
                if (::onClickOKButton.isInitialized) {
                    onClickOKButton(studyIdx, mDialogView?.findViewById<TextInputLayout>(R.id.edit_dialog_study_password)?.editText?.text.toString())
                }
            })
    }

    fun setErrorMessage(message: String) {
        var password = mDialogView?.findViewById<TextInputLayout>(R.id.edit_dialog_study_password)
        password?.error = message
    }

    fun dismissDialog() {
        mBuilder?.dismiss()
        mDialogView = null
        mBuilder = null
    }
}