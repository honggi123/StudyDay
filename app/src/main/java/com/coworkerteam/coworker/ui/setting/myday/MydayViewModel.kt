package com.coworkerteam.coworker.ui.setting.myday

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.*
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MydayViewModel (private val model: UserRepository) : BaseViewModel() {
    private val TAG = "YourdayViewModel"

    //공부인증 데이터
    private var _SuccessPostPagingData = MutableLiveData<Response<SuccessPostResponse>>()
    val SuccessPostPagingData: LiveData<Response<SuccessPostResponse>>
        get() = _SuccessPostPagingData

    //감정글 데이터
    private var _MoodPostPagingData = MutableLiveData<Response<MoodPostResponse>>()
    val MoodPostPagingData: LiveData<Response<MoodPostResponse>>
        get() = _MoodPostPagingData

    //공부인증 데이터 게시물 삭제
    private var _DeleteSuccessPostResponseLiveData = MutableLiveData<Response<PostDeleteResponse>>()
    val DeletePostResponseLiveData: LiveData<Response<PostDeleteResponse>>
        get() = _DeleteSuccessPostResponseLiveData

    //공부인증 데이터 게시물 삭제
    private var _DeleteMoodPostResponseLiveData = MutableLiveData<Response<PostDeleteResponse>>()
    val DeleteMoodResponseLiveData: LiveData<Response<PostDeleteResponse>>
        get() = _DeleteMoodPostResponseLiveData

    //공감하기
    private var _EmpathyResponseLiveData = MutableLiveData<Response<EmpathyResponse>>()
    val EmpathyResponseLiveData: LiveData<Response<EmpathyResponse>>
        get() = _EmpathyResponseLiveData


    fun getMySuccessPost(page: Int) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getMySuccessPost(accessToken, nickname,page)
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
                                    getReissuanceToken(TAG,model,getMySuccessPost(page))
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

    fun getMyMoodPost(sort: String,page: Int) {

        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getMyMoodPost(accessToken,nickname,sort, page)
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
                                    getReissuanceToken(TAG,model,getMyMoodPost(sort,page))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _MoodPostPagingData.postValue(it)
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

    fun deleteSuccessPostdData(postNum: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setRemoveSuccessPost(accessToken, postNum)
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
                                    getReissuanceToken(TAG,model,deleteSuccessPostdData(postNum))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _DeleteSuccessPostResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "removeTodoListData:: accessToken 값이 없습니다.")
        }


    }
    fun deleteMoodPostdData(postNum: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setRemoveMoodPost(accessToken, postNum)
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
                                    getReissuanceToken(TAG, model, deleteMoodPostdData(postNum))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _DeleteSuccessPostResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "removeTodoListData:: accessToken 값이 없습니다.")
        }
    }

    fun empathy(postNum: Int,mood: Int) {
        val accessToken = model.getAccessToken()
        Log.d(TAG,"accesstokent : " + accessToken)
        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setEmpathy(accessToken,postNum, mood)
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
                                    getReissuanceToken(TAG,model,empathy(postNum,mood))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _EmpathyResponseLiveData.postValue(it)
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