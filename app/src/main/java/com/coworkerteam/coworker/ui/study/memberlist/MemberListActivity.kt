package com.coworkerteam.coworker.ui.study.memberlist

import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StudyMemberResponse
import com.coworkerteam.coworker.databinding.ActivityMemberListBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MemberListActivity : BaseActivity<ActivityMemberListBinding, MemberListViewModel>() {

    private val TAG = "MemberListActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_member_list
    override val viewModel: MemberListViewModel by viewModel()

    var studyIdx: Int = -1

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.member_list_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "가입 멤버"

        studyIdx = intent.getIntExtra("study_idx", -1)
    }

    override fun initDataBinding() {
        viewModel.StudyMemberResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    init_rv(it.body()!!)
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 멤버 데이터 가져오기 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"스터디 멤버 리스트를 가져오는 것을 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -2 ->{
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -3 ->{
                            //존재하지 않는 스터디일 경우
                            Toast.makeText(this,"더이상 존재하지 않는 스터디입니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        })
    }

    override fun initAfterBinding() {
        viewModel.getStudyMemberData(studyIdx)
    }

    fun init_rv(myStudy: StudyMemberResponse) {
        //새로운
        var recyclerNewStudy: RecyclerView =
            findViewById(R.id.member_list_rv)
        var newAdapter: MemberListAdapter = MemberListAdapter(this)

        newAdapter.datas = myStudy.result.toMutableList()
        newAdapter.study_idx = studyIdx
        recyclerNewStudy.adapter = newAdapter

    }
}