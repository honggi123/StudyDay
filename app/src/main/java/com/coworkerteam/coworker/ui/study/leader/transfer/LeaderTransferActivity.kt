package com.coworkerteam.coworker.ui.study.leader.transfer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.StudyMemberResponse
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.databinding.ActivityCategoryBinding
import com.coworkerteam.coworker.databinding.ActivityLeaderTransferBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryViewModel
import com.google.android.gms.common.api.ApiException
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class LeaderTransferActivity : BaseActivity<ActivityLeaderTransferBinding, LeaderTransferViewModel>() {

    private val TAG = "LeaderTransferActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_leader_transfer
    override val viewModel: LeaderTransferViewModel by viewModel()

    var studyIdx:Int = -1
    lateinit var myStudy: StudyMemberResponse

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.leader_transfer_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "리더 양도"
    }

    override fun initDataBinding() {
        viewModel.StudyMemberResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                myStudy = it.body()!!
                init_rv()
            }
        })

        viewModel.LeaderTransferResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                finish()
            }
        })
    }

    override fun initAfterBinding() {
        studyIdx = intent.getIntExtra("study_idx",-1)
        viewModel.getStudyMemberData(studyIdx)
    }

    fun init_rv() {
        //새로운
        var recyclerNewStudy: RecyclerView =
            findViewById(R.id.leader_transfer_rv)
        var newAdapter: LeaderTransferAdapter = LeaderTransferAdapter(this, viewModel)

        newAdapter.datas = myStudy.result.toMutableList()
        newAdapter.study_idx = studyIdx

        recyclerNewStudy.adapter = newAdapter
    }

}