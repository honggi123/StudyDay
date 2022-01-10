package com.coworkerteam.coworker.ui.study.management

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.MyStudyManageResponse
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.databinding.ActivityCategoryBinding
import com.coworkerteam.coworker.databinding.ActivityManagementBinding
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

class ManagementActivity : BaseActivity<ActivityManagementBinding, ManagementViewModel>() {

    private val TAG = "ManagementActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_management
    override val viewModel: ManagementViewModel by viewModel()

    lateinit var myStudy: MyStudyManageResponse
    var newAdapter: ManagementAdapter? = null

    override fun initStartView() {
    }

    override fun initDataBinding() {
        viewModel.ManagementResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                myStudy = it.body()!!
                init_rv()
            }
        })
        viewModel.ApiResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                newAdapter!!.notifyDataSetChanged()
            }
        })
    }

    override fun initAfterBinding() {
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_management)


        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.management_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "스터디 관리"
    }

    override fun onPostResume() {
        super.onPostResume()
        viewModel.getManagementData()
    }


    fun init_rv() {
        //리사이클러뷰
        var recyclerNewStudy: RecyclerView =
            findViewById(R.id.management_rv)
        newAdapter= ManagementAdapter(this, viewModel)
        newAdapter!!.datas = myStudy.result.myStudy.toMutableList()
        recyclerNewStudy.adapter = newAdapter
    }

}