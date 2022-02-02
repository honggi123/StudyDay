package com.coworkerteam.coworker.ui.camstudy.enter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.CamStudyJoinResponse
import com.coworkerteam.coworker.data.model.api.InstanceIDResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class EnterCamstudyViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "EnterCamstudyViewModel"

    private val _EnterCamstudyResponseLiveData = MutableLiveData<Response<CamStudyJoinResponse>>()
    val EnterCamstudyResponseLiveData: LiveData<Response<CamStudyJoinResponse>>
        get() = _EnterCamstudyResponseLiveData

    private val _CamstduyInstanceResponseLiveData = MutableLiveData<Response<InstanceIDResponse>>()
    val CamstduyInstanceResponseLiveData: LiveData<Response<InstanceIDResponse>>
        get() = _CamstduyInstanceResponseLiveData

    fun getCamstduyInstanceData(link: String) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getCamStudystudyInstanceData(accessToken,link)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, "meta : $it")

                            when {
                                it.code() == 401 -> {
                                    //액세스토큰이 만료된 경우
                                    Log.d(TAG, "액세스토큰이 만료된 경우")

                                    //액세스 토큰 재발급
                                    getReissuanceToken(TAG,model,getCamstduyInstanceData(link))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _CamstduyInstanceResponseLiveData.postValue(this)
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "getCamstduyJoinData:: accessToken 값이 없습니다.")
        }
    }

    fun getCamstduyJoinData(studyIdx: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getCamStudyJoinData(accessToken, studyIdx)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, "meta : $it")

                            when {
                                it.code() == 401 -> {
                                    //액세스토큰이 만료된 경우
                                    Log.d(TAG, "액세스토큰이 만료된 경우")

                                    //액세스 토큰 재발급
                                    getReissuanceToken(TAG,model,getCamstduyJoinData(studyIdx))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _EnterCamstudyResponseLiveData.postValue(this)
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "getCamstduyJoinData:: accessToken 값이 없습니다.")
        }
    }
}