package com.coworkerteam.coworker.ui.study.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.EditStudyResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class EditStudyViewModel(private val model: UserRepository) : BaseViewModel()  {
    val TAG = "EditStudyViewModel"

    //스터디 수정
    private val _EditStudyResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val EditStudyResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _EditStudyResponseLiveData

    //수정할 스터디 데이터
    private val _EditStudyInfoResponseLiveData = MutableLiveData<Response<EditStudyResponse>>()
    val EditStudyInfoResponseLiveData: LiveData<Response<EditStudyResponse>>
        get() = _EditStudyInfoResponseLiveData

    fun setEditStudyData(studyIdx: Int, name: String, category: String, imgUrl: String, pw: String?, maxNum: Int, introduce: String) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setEditStudyData(accessToken,studyIdx, name, category, imgUrl, pw, maxNum, introduce)
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
                                    getReissuanceToken(TAG,model,setEditStudyData(studyIdx, name, category, imgUrl, pw, maxNum, introduce))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _EditStudyResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG,"setEditStudyData:: accessToken 값이 없습니다.")
        }
    }

    fun getEditStudyData(studyIdx: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getEditStudyData(accessToken,studyIdx)
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
                                    getReissuanceToken(TAG,model,getEditStudyData(studyIdx))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _EditStudyInfoResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG,"getEditStudyData:: accessToken 값이 없습니다.")
        }
    }
}