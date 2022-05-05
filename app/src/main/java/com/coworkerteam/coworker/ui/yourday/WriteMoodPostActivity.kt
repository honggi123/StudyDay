package com.coworkerteam.coworker.ui.yourday

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityWritemoodBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.utils.PatternUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class WriteMoodPostActivity : BaseActivity<ActivityWritemoodBinding, WriteMoodPostViewModel>() {

    private val TAG = "WriteMoodPostActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_writemood
    override val viewModel: WriteMoodPostViewModel by viewModel()

    lateinit var edit_writemood : EditText
    lateinit var view_txtnumber : TextView
    lateinit var btn_showemotion : ImageView
    lateinit var today_date : TextView
    lateinit var view_letter_errmsg : TextView
     var todaydate : String? = "123"

    fun showEmotionDialog(){
        val mDialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_writemoodpost_selectemotion, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        val builder = mBuilder.show()

        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btn_ok = mDialogView.findViewById<ImageView>(R.id.dialog_btn_ok)

        btn_ok.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })
    }

    override fun initStartView() {

        edit_writemood = findViewById(R.id.edit_writemood)
        view_txtnumber = findViewById(R.id.view_txtnumber)
        btn_showemotion = findViewById(R.id.btn_showemotion)
        today_date = findViewById(R.id.today_date)
        view_letter_errmsg = findViewById(R.id.view_letter_errmsg)

        edit_writemood.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var userinput = edit_writemood.text.toString()
                view_txtnumber.setText(userinput.length.toString() + " / 1000")
                val result = PatternUtils.matchMoodDescription(s.toString())
                if (result.isNotError){
                    view_letter_errmsg.visibility = View.GONE
                } else {
                    view_letter_errmsg.visibility = View.VISIBLE
                    view_letter_errmsg.setText(result.ErrorMessge)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                var userinput = edit_writemood.text.toString()
                view_txtnumber.setText(userinput.length.toString() + " / 1000")
            }
        })

        btn_showemotion.setOnClickListener(View.OnClickListener {
            showEmotionDialog()
        })


        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MM월 dd일")
        val formatted = current.format(formatter)

        Log.d(TAG,"Current: $formatted")

        today_date.setText(formatted)
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
    }


}