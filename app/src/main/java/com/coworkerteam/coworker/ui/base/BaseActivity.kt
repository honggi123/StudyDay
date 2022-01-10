package com.coworkerteam.coworker.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * BaseActivity<ActivitySbsMainBinding>
 * 와 같이 상속 받을 때, ActivitySbsMainBinding 과 같은 파일이 자동생성되지 않는다면
 * 1. 해당 엑티비티의 레이아웃이 <layout></layout> 으로 감싸져 있는지 확인
 * 2. 다시 빌드 수행 or 클린 빌드 후 다시 빌드 수행
 * 3. 이름 확인 : sbs_main_activity => ActivitySbsMainBinding
 */
open abstract class BaseActivity<T : ViewDataBinding, R: BaseViewModel> : AppCompatActivity() {

    lateinit var viewDataBinding: T
    abstract val layoutResourceID : Int

    // viewModel로 쓰일 변수
    abstract val viewModel: R

    //레이아웃을 띄운 직후 호출 - 뷰나 액티비티의 속성등을 초기화
    abstract fun initStartView()

    // 두번째로 호출. 데이터 바인딩 및 rxjava 설정
    abstract fun initDataBinding()

    // 바인딩 이후 해야할 것을 구현
    abstract fun initAfterBinding()

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        viewDataBinding = DataBindingUtil.setContentView(this, layoutResourceID)

        initStartView()
        initDataBinding()
        initAfterBinding()
    }
}