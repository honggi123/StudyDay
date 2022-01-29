package com.coworkerteam.coworker.ui.statistics

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.StatisticsResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class StatisticsViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "StatisticsViewModel"

    //스터디 입장전 데이터
    private val _StatisticsResponseLiveData = MutableLiveData<Response<StatisticsResponse>>()
    val StatisticsResponseLiveData: LiveData<Response<StatisticsResponse>>
        get() = _StatisticsResponseLiveData

    fun getStatisticsData(reqType: String, selectDate: String, period: String) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getStatisticsData(accessToken, nickname, reqType, selectDate, period)
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
                                    getReissuanceToken(TAG,model,getStatisticsData(reqType, selectDate, period))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _StatisticsResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getStatisticsData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

    fun getLoginType():String?{
        return model.getCurrentUserLoggedInMode()
    }

    fun getUserProfile():String?{
        return model.getCurrentUserProfilePicUrl()
    }

    fun getUserNickname():String?{
        return model.getCurrentUserName()
    }
}