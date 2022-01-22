package com.coworkerteam.coworker.ui.base

import android.util.Log
import androidx.lifecycle.ViewModel
import com.coworkerteam.coworker.data.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

open class BaseViewModel() : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    fun addDisposable(disposavle: Disposable) {
        compositeDisposable.add(disposavle)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun getReissuanceToken(model: UserRepository, unit: Unit) {
        val refreshToken = model.getRefreshToken()
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!refreshToken.isNullOrEmpty() && !accessToken.isNullOrEmpty() && nickname.isNullOrEmpty()) {
            addDisposable(
                model.getTokenResetData(refreshToken, accessToken, nickname!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d("BaseViewModel", "meta : " + it.toString())
                            
                            //재설정 받은 액세스토큰 다시 로컬에 저장
                            model.setAccessToken(it.body()!!.accessToken)
                            
                            //액세스토큰 만료때문에 실행 실패했던 api요청 재실행
                            unit
                        }
                    }, {
                        Log.d("BaseViewModel", "response error, message : ${it.message}")
                    })
            )
        }
    }

}