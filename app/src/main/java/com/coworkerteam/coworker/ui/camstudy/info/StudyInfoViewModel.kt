package com.coworkerteam.coworker.ui.camstudy.info

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.StudyInfoResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class StudyInfoViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "StudyInfoViewModel"

    //스터디 입장전 데이터
    private val _StudyInfoResponseLiveData = MutableLiveData<Response<StudyInfoResponse>>()
    val StudyInfoResponseLiveData: LiveData<Response<StudyInfoResponse>>
        get() = _StudyInfoResponseLiveData

    fun getStudyInfoData(studyIdx: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getStudyInfoData(accessToken, studyIdx)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _StudyInfoResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getStudyInfoData:: accessToken 값이 없습니다.")
        }
    }
}