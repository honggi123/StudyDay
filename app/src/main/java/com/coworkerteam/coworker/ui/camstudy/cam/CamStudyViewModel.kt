package com.coworkerteam.coworker.ui.camstudy.cam

import android.util.Log
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CamStudyViewModel(private val model: UserRepository) : BaseViewModel() {
    val TAG = "CamStudyViewModel"

    fun getCamstduyLeaveData(studyIdx: Int, studyTime: Int, restTime: Int) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getCamStudyLeaveData(accessToken, nickname, studyIdx, studyTime, restTime)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, it.body()!!.message)
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG,"getCamstduyLeaveData:: accessTokenr값 또는 nickname 값이 없습니다.")
        }
    }

    fun getNickName(): String? {
        return model.getCurrentUserName()
    }
}