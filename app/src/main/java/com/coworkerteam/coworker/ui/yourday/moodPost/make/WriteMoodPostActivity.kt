package com.coworkerteam.coworker.ui.yourday.moodPost.make

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityWritemoodBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.utils.PatternUtils
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class WriteMoodPostActivity (): BaseActivity<ActivityWritemoodBinding, WriteMoodPostViewModel>() {

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
    var emotinoNum : Int = 0
    var patternError = false

    override fun initStartView() {

        emotinoNum = intent.getIntExtra("emotinoNum",1)

        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.write_mood_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "글 작성"

        edit_writemood = findViewById(R.id.edit_writemood)
        view_txtnumber = findViewById(R.id.view_txtnumber)
        btn_showemotion = findViewById(R.id.view_MyEmotion)
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
                    patternError = false
                } else {
                    view_letter_errmsg.visibility = View.VISIBLE
                    view_letter_errmsg.setText(result.ErrorMessge)
                    patternError = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
                var userinput = edit_writemood.text.toString()
                view_txtnumber.setText(userinput.length.toString() + " / 1000")
            }
        })

        viewDataBinding.activitiy = this

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("M월 d일")
        val formatted = current.format(formatter)

        Log.d(TAG,"Current: $formatted")

        today_date.setText(formatted)
        setMyMoodEmoji(emotinoNum)

    }

    override fun initDataBinding(){
        viewModel.MoodPostData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                  finish()
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 검색 데이터 가져오기가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"검색 데이터를 가져오지 못했습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })


    }

    override fun initAfterBinding(){
    }


    fun setMyMoodEmoji(emotinoNum : Int){
        Log.d("setMyEmoji",emotinoNum.toString())
        this.emotinoNum = emotinoNum
        var emotionname = "mood_emoticon"+emotinoNum.toString()
        Log.d("setMyEmoji",emotionname)
        Glide.with(this)
            .load(this.getResources().getIdentifier(emotionname,"drawable",this.getPackageName()))
            .into(btn_showemotion)
    }

    fun setMoodPost(){
        if (patternError){
            Toast.makeText(this,"작성 글을 확인해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.setMoodPost("make",emotinoNum,edit_writemood.text.toString())
    }

}