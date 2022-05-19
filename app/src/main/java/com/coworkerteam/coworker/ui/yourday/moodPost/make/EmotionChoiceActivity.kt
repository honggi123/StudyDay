package com.coworkerteam.coworker.ui.yourday.moodPost.make

import android.content.Intent
import android.util.Log
import android.view.View
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityEmotionchoiceBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmotionChoiceActivity() : BaseActivity<ActivityEmotionchoiceBinding, WriteMoodPostViewModel>() {

    private val TAG = "EmotionChoiceActivity"
    override val layoutResourceID: Int
    get() = R.layout.activity_emotionchoice
    override val viewModel: WriteMoodPostViewModel by viewModel()
    var checkNum : Int = 0

    override fun initStartView() {
        /*
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.write_mood_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "글 작성"
        */
        viewDataBinding.activity = this
    }

    override fun initDataBinding(){
    }

    override fun initAfterBinding(){

    }

    fun choiceEmoji(choice : Int){
        checkNum = choice
        val mood_check_row1 = viewDataBinding.moodCheckRow1
        val mood_check_row2 = viewDataBinding.moodCheckRow2
        val mood_check_row3 = viewDataBinding.moodCheckRow3

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
        Log.d("emoji!!!",checkNum.toString())
    }

    fun setMyMood(){
        var intent = Intent(this, WriteMoodPostActivity::class.java)
        intent.putExtra("emotinoNum",checkNum)
        startActivity(intent)
        finish()
    }


}