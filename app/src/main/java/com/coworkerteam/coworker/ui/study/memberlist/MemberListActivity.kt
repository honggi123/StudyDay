package com.coworkerteam.coworker.ui.study.memberlist

import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StudyMemberResponse
import com.coworkerteam.coworker.databinding.ActivityMemberListBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
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
    }

    override fun initDataBinding() {
        viewModel.StudyMemberResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                init_rv(it.body()!!)
            }
        })
    }

    override fun initAfterBinding() {
        studyIdx = intent.getIntExtra("study_idx", -1)
        viewModel.getStudyMemberData(studyIdx)
    }

    fun init_rv(myStudy: StudyMemberResponse) {
        //새로운
        var recyclerNewStudy: RecyclerView =
            findViewById(R.id.leader_transfer_rv)
        var newAdapter: MemberListAdapter = MemberListAdapter(this)

        newAdapter.datas = myStudy.result.toMutableList()
        newAdapter.study_idx = studyIdx
        recyclerNewStudy.adapter = newAdapter

    }
}