package com.coworkerteam.coworker.ui.study.leader.transfer

import android.widget.Toast

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.google.android.material.button.MaterialButtonToggleGroup
import okhttp3.OkHttpClient
import org.json.JSONObject
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

class LeaderTransferActivity :
    BaseActivity<ActivityLeaderTransferBinding, LeaderTransferViewModel>() {

    private val TAG = "LeaderTransferActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_leader_transfer
    override val viewModel: LeaderTransferViewModel by viewModel()

    var studyIdx: Int = -1

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.leader_transfer_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "가입 멤버"

        viewDataBinding.toggleButton.check(R.id.member_check_toogle_member)
        viewDataBinding.toggleButton.addOnButtonCheckedListener(MaterialButtonToggleGroup.OnButtonCheckedListener { group, checkedId, isChecked ->
            viewModel.getStudyMemberData(studyIdx)
        })
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

        viewModel.ForcedExitResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    Toast.makeText(this, "성공적으로 추방했습니다.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 강제 탈퇴 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"해당 스터디 멤버 강제추방 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 403 -> {
                    //리더가 아니라서 강제 탈퇴 시킬 수 없는 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //403번대 에러로 강제 탈퇴 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"강제추방 권한이 없습니다.", Toast.LENGTH_SHORT).show()
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

        viewModel.LeaderTransferResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    finish()
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 리더 양도 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"스터디 리더 양도하는 것을 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 403 -> {
                    //리더 양도 권한이 없는 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //403번대 에러로 리더 양도 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"리더 양도할 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -3 ->{
                            //존재하지 않는 스터디일 경우
                            Toast.makeText(this,"더이상 존재하지 않는 스터디입니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        -5 ->{
                            //위임 받을 회원이 해당 스터디 멤버가 아닌 경우
                            Toast.makeText(this,"해당 회원은 더이상 해당 스터디 멤버가 아닙니다. 다른사람에게 위임해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    override fun initAfterBinding() {
        studyIdx = intent.getIntExtra("study_idx", -1)
        viewModel.getStudyMemberData(studyIdx)
    }

    fun init_rv(myStudy: StudyMemberResponse) {
        //새로운
        if (viewDataBinding.toggleButton.checkedButtonId == R.id.member_check_toogle_leader) {
            var recyclerNewStudy: RecyclerView =
                findViewById(R.id.leader_transfer_rv)
            var newAdapter: LeaderTransferAdapter = LeaderTransferAdapter(this, viewModel)

            newAdapter.datas = myStudy.result.toMutableList()
            newAdapter.emptyView  = viewDataBinding.leaderTransferEmpty

            newAdapter.study_idx = studyIdx
            recyclerNewStudy.adapter = newAdapter
        } else {
            var recyclerNewStudy: RecyclerView =
                findViewById(R.id.leader_transfer_rv)
            var newAdapter: MemberManagementAdapter = MemberManagementAdapter(this, viewModel)

            newAdapter.datas = myStudy.result.toMutableList()
            newAdapter.emptyView  = viewDataBinding.leaderTransferEmpty

            newAdapter.study_idx = studyIdx
            recyclerNewStudy.adapter = newAdapter
        }

    }

}