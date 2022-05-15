package com.coworkerteam.coworker.ui.dialog

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.coworkerteam.coworker.databinding.DialogChoicemoodEditactivityBinding
import com.coworkerteam.coworker.ui.yourday.moodPost.edit.EditMoodPostActivity
import com.coworkerteam.coworker.ui.yourday.moodPost.make.WriteMoodPostActivity

class MoodEmotionDialog_EditActivity  (context: EditMoodPostActivity) : Dialog(context){
    var mDialogView: View? = null
    var mBuilder: AlertDialog? = null
    lateinit var onClickOKButton: (Int, String?) -> Unit
    lateinit var bind : DialogChoicemoodEditactivityBinding
    var checkNum : Int = 0
    var context : EditMoodPostActivity = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable())
        window?.setDimAmount(0.3f)
        bind = DialogChoicemoodEditactivityBinding.inflate(this.layoutInflater)
        bind.dialog = this

        setContentView(bind.root)
    }

    class Builder(context: EditMoodPostActivity){
        private val dialog = MoodEmotionDialog_EditActivity(context)

        fun show(): MoodEmotionDialog_EditActivity {
            dialog.show()
            return dialog
        }
    }

    fun choiceEmoji(choice : Int){
        val mood_check_row1 = bind.moodCheckRow1
        val mood_check_row2 = bind.moodCheckRow2
        val mood_check_row3 = bind.moodCheckRow3

        // 모든 체크표시 안보이게
        for(i in 0..2){
            mood_check_row1?.getChildAt(i)?.visibility = View.INVISIBLE
            mood_check_row2?.getChildAt(i)?.visibility = View.INVISIBLE
            mood_check_row3?.getChildAt(i)?.visibility = View.INVISIBLE
        }

        if (choice<=3){
            mood_check_row1?.getChildAt(choice-1)?.visibility = View.VISIBLE
        }else if( choice >= 4 && choice <= 6){
            mood_check_row2?.getChildAt(choice-4)?.visibility = View.VISIBLE
        }else{
            mood_check_row3?.getChildAt(choice-7)?.visibility = View.VISIBLE
        }
        checkNum = choice
    }

    fun setMyMood(){
        context.setMyMoodEmoji(checkNum.toString())
        dismiss()
    }


}