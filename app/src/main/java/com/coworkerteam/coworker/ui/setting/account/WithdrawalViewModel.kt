package com.coworkerteam.coworker.ui.setting.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.MyStudyResponse
import com.coworkerteam.coworker.data.model.other.ServiceError
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class WithdrawalViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "WithdrawalViewModel"

    //회원 탈퇴
    private val _WithdrawalResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val WithdrawalResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _WithdrawalResponseLiveData

    fun setWithdrawalData(leaveReason: String) {
        val accessToken = model.getAccessToken()
        val refreshToken = model.getRefreshToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.setWithdrawalData(accessToken, refreshToken, nickname, leaveReason)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, "meta : $it")

                            if (isSuccessful) {
                                model.deletePreferencesData()
                            }

                            when {
                                it.code() == 401 -> {
                                    //액세스토큰이 만료된 경우
                                    Log.d(TAG, "액세스토큰이 만료된 경우")

                                    //액세스 토큰 재발급
                                    getReissuanceToken(TAG,model,setWithdrawalData(leaveReason))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _WithdrawalResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setWithdrawalData:: accessToken 또는 refreshToken 또는 nickname 값이 없습니다.")
        }
    }

    fun getLoginTypeData(): String? {
        return model.getCurrentUserLoggedInMode()
    }
}