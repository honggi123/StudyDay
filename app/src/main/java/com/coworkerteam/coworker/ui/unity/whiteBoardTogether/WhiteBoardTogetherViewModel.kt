package com.coworkerteam.coworker.ui.unity.whiteBoardTogether

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.EmpathyResponse
import com.coworkerteam.coworker.data.model.api.MoodPostResponse
import com.coworkerteam.coworker.data.model.api.PostDeleteResponse
import com.coworkerteam.coworker.data.model.api.SuccessPostResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class WhiteBoardTogetherViewModel (private val model: UserRepository) : BaseViewModel() {
    private val TAG = "WhiteBoardTogetherViewModel"

    //공부인증 데이터
    private var _SuccessPostPagingData = MutableLiveData<Response<SuccessPostResponse>>()
    val SuccessPostPagingData: LiveData<Response<SuccessPostResponse>>
        get() = _SuccessPostPagingData


    fun getSuccessPost(page: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getSuccessPost(accessToken, page)
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
                                    getReissuanceToken(TAG,model,getSuccessPost(page))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _SuccessPostPagingData.postValue(it)
                                    Log.d(TAG,"page : "+page + " RESPONSE : " + it.body())
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getEnterCamstduyData:: accessToken 값이 없습니다.")
        }
    }

    fun getUserName(): String? {
        return model.getCurrentUserName()
    }



}