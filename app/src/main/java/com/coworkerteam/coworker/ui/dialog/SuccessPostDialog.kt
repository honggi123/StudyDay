package com.coworkerteam.coworker.ui.dialog

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.coworkerteam.coworker.databinding.DialogSuccesspostBinding
import com.coworkerteam.coworker.ui.main.MainActivity
import com.coworkerteam.coworker.utils.DateFormatUtils
import com.coworkerteam.coworker.utils.PatternUtils

class SuccessPostDialog (context: MainActivity) : Dialog(context){
    var mDialogView: View? = null
    var mBuilder: AlertDialog? = null
    lateinit var onClickOKButton: (Int, String?) -> Unit
    lateinit var bind : DialogSuccesspostBinding
    var isOkContents = true
    var context : MainActivity = context
    lateinit var sgoalTime :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable())
        window?.setDimAmount(0.3f)
        bind = DialogSuccesspostBinding.inflate(this.layoutInflater)
        bind.dialog = this

        setContentView(bind.root)

        bind.dialogSuccesspostTime.setText(sgoalTime+"을 달성하셨습니다.")

        bind.dialogSuccesspostBtnShare.setOnClickListener(View.OnClickListener {
            // 특수문자 껴있는지 예외처리 해야함
            if (isOkContents){
                context.shareSuccessPost(bind.successpostContents.text.toString())
                dismiss()
            }else{
                Toast.makeText(context, "공부인증 소감을 확인해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }

        })

        bind.dialogSuccesspostBtnCancle.setOnClickListener(View.OnClickListener {
            dismiss()
        })
    }


    class Builder(context: MainActivity){
        private val dialog = SuccessPostDialog(context)

        fun setGoalTime (goalTime : Int): Builder {
            dialog.setgoalTime(goalTime)
            return this
        }

        fun show(): SuccessPostDialog {
            dialog.show()
            return dialog
        }

    }

    fun changTextSuccessContent(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matchSuccessPostContents(s.toString())

        if (result.isNotError){
            isOkContents = true
            bind.txtSuccPostDialogErrMsg.setText("")
        }else {
            isOkContents = false
            bind.txtSuccPostDialogErrMsg.setText(result.ErrorMessge)
        }
        bind.txtSuccPostDialogCount.setText((s.length).toString() + " / " + "20")
    }

    fun setgoalTime(goalTime: Int){
        sgoalTime = DateFormatUtils.secondsToHourMin(goalTime)
    }

}