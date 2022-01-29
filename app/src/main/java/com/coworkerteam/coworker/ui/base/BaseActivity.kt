package com.coworkerteam.coworker.ui.base

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.coworkerteam.coworker.utils.NetworkUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.get

/**
 * BaseActivity<ActivitySbsMainBinding>
 * 와 같이 상속 받을 때, ActivitySbsMainBinding 과 같은 파일이 자동생성되지 않는다면
 * 1. 해당 엑티비티의 레이아웃이 <layout></layout> 으로 감싸져 있는지 확인
 * 2. 다시 빌드 수행 or 클린 빌드 후 다시 빌드 수행
 * 3. 이름 확인 : sbs_main_activity => ActivitySbsMainBinding
 */
open abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel> : AppCompatActivity() {
    lateinit var viewDataBinding: T
    abstract val layoutResourceID: Int

    // viewModel로 쓰일 변수
    abstract val viewModel: R

    //네트워크 상태 반환해주는 Utils 클래스
    val network: NetworkUtils = get()

    //레이아웃을 띄운 직후 호출 - 뷰나 액티비티의 속성등을 초기화
    abstract fun initStartView()

    // 두번째로 호출. 데이터 바인딩 및 rxjava 설정
    abstract fun initDataBinding()

    // 바인딩 이후 해야할 것을 구현
    abstract fun initAfterBinding()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        //네트워크 체크
        checkNetwork()

        //데이터 바인딩 초기화
        viewDataBinding = DataBindingUtil.setContentView(this, layoutResourceID)

        //선언한 추상메서드 순서대로 실행 및 서비스 에러 데이터바인딩
        initStartView()
        initDataBinding()
        getErrorDataBinding()
        initAfterBinding()
    }

    private fun getErrorDataBinding() {
        //스테이터스 코드 500번대가 뜰 경우 및 리프레쉬 토큰 만료
        viewModel.ServerErrorResponseLiveData.observe(this, androidx.lifecycle.Observer {
            Log.e(it.TAG, it.message)

            if(it.message == viewModel.ERROR_REFRESH_TOKEN){
                moveLogin()
            }

            showServerErrorDialog()
        })
    }

    //네트워크 상태 체크
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkNetwork() {
        network.observe(this, androidx.lifecycle.Observer {
            if (it) {

            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("네트워크 연결 안됨")
                    .setMessage("네크워크에 연결되어 있지않습니다. Wi-Fi를 통해 연결하시거나, 무선네트워크 차단 상태인지 확인해주세요.")
                    .setNegativeButton("종료", DialogInterface.OnClickListener { dialog, which ->
                        moveTaskToBack(true); // 태스크를 백그라운드로 이동
                        finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                        android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
                    })
                    .setPositiveButton("재연결", DialogInterface.OnClickListener { dialog, which ->
                        network.updateConnection()
                    }).show()
            }
        })
    }

    //API 서버 에러시 띄우는 다이얼로그 ( Status 코드 500번대 )
    fun showServerErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("시스템 에러")
            .setMessage("죄송합니다. 현재 시스템 오류가 발생했습니다. 나중에 다시 시도해주세요.")
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                moveTaskToBack(true); // 태스크를 백그라운드로 이동
                finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
            }).show()
    }

    fun moveLogin(){
        Toast.makeText(this,"다시 로그인 해주세요.",Toast.LENGTH_SHORT).show()

        val loginIntent = Intent(this,LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}