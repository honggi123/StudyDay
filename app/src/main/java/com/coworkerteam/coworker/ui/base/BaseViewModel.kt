package com.coworkerteam.coworker.ui.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.TokenResponse
import com.coworkerteam.coworker.data.model.other.ServiceError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

open class BaseViewModel() : ViewModel() {
    val ERROR_REFRESH_TOKEN = "refreshToken"

    private val compositeDisposable = CompositeDisposable()

    //500번대 에러 관련 및 리프레쉬 토큰 만료
    val _ServerErrorLiveData = MutableLiveData<ServiceError>()
    val ServerErrorResponseLiveData: LiveData<ServiceError>
        get() = _ServerErrorLiveData

    fun addDisposable(disposavle: Disposable) {
        compositeDisposable.add(disposavle)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun setServiceError(TAG: String, errorBody: ResponseBody?) {
        //서비스 서버에 문제가 있을 경우
        val errorMessage = JSONObject(errorBody?.string())
        _ServerErrorLiveData.postValue(ServiceError(TAG, errorMessage.getString("message")))
    }

    //리프레쉬토큰 재발급
    fun getReissuanceToken(TAG: String, model: UserRepository, unit: Unit) {
        val refreshToken = model.getRefreshToken()
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!refreshToken.isNullOrEmpty() && !accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getTokenResetData(refreshToken, accessToken, nickname!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, "meta : $it")

                            when {
                                isSuccessful -> {
                                    Log.d(TAG, "액세스 토큰 재발급 성공")

                                    //재설정 받은 액세스토큰 다시 로컬에 저장
                                    model.setAccessToken(it.body()!!.accessToken)

                                    //액세스토큰 만료때문에 실행 실패했던 api요청 재실행
                                    unit
                                }
                                it.code() < 500 -> {
                                    //액세스토큰 갱신에 실패했을 경우
                                    Log.d(TAG, "액세스 토큰 재발급 실패")
                                    _ServerErrorLiveData.postValue(ServiceError(TAG, ERROR_REFRESH_TOKEN))
                                }
                                else -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                            }
                        }
                    }, {
                        Log.d("BaseViewModel", "response error, message : ${it.message}")
                    })
            )
        }
    }

}