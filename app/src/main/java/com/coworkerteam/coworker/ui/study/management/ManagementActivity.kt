package com.coworkerteam.coworker.ui.study.management

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.paging.LoadState
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.MyStudyManageResponse
import com.coworkerteam.coworker.databinding.ActivityManagementBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManagementActivity : BaseActivity<ActivityManagementBinding, ManagementViewModel>() {

    private val TAG = "ManagementActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_management
    override val viewModel: ManagementViewModel by viewModel()

    lateinit var myStudy: MyStudyManageResponse
    lateinit var pagingManagementAdapter: ManagementPagingAdapter

    override fun initStartView() {
        setSupportActionBar(viewDataBinding.managementToolbar as androidx.appcompat.widget.Toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "내 스터디 관리"

        pagingManagementAdapter = ManagementPagingAdapter(viewModel)
        viewDataBinding.managementRv.adapter = pagingManagementAdapter
        pagingManagementAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && pagingManagementAdapter.itemCount < 1) {
                viewDataBinding.managementEmptyView.visibility = View.VISIBLE
            } else {
                viewDataBinding.managementEmptyView.visibility = View.GONE
            }
        }
    }

    override fun initDataBinding() {
        viewModel.StudyDeleteResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    firebaseLog.addLog(TAG,"delete_study")
                    pagingManagementAdapter.refresh()
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 스터디 삭제, 탈퇴 실패했을 경우 사용자에게 알려준다.
                    Toast.makeText(this,"스터디 삭제, 탈퇴에 실패했습니다. 나중 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 403 -> {
                    //스터디 삭제 권한이 없을 경우 ( 리더가 아닐 경우 ), 리더인데 스터디를 탈퇴하려는 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -10 ->{
                            //403번대 에러로 스터디 삭제 실패했을 경우 사용자에게 알려준다.
                            Toast.makeText(this,"스터디 삭제 권한이 없습니다.",Toast.LENGTH_SHORT).show()
                        }
                        -11 ->{
                            //403번대 에러로 스터디 탈퇴 실패했을 경우 사용자에게 알려준다.
                            Toast.makeText(this,"리더는 탈퇴할 수 없습니다. 리더를 양도해주세요",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -2 ->{
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -3 ->{
                            //삭제, 탈퇴하고자 하는 스터디가 실제로 존재하지 않은 경우
                            Toast.makeText(this,"해당 스터디가 존재하지 않습니다.",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
        viewModel.MyStudyManagementPagingData.observe(this,androidx.lifecycle.Observer {
            pagingManagementAdapter.submitData(lifecycle,it)
        })
    }

    override fun initAfterBinding() {
    }

    override fun onRestart() {
        super.onRestart()
        pagingManagementAdapter.refresh()
    }

}