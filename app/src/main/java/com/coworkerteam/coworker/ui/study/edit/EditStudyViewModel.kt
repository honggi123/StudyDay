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
        Log.d("setEditStudyData","$studyIdx,$name, $category, $imgUrl, $pw, $maxNum, $introduce")

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setEditStudyData(accessToken,studyIdx, name, category, imgUrl, pw, maxNum, introduce)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _EditStudyResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
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
                            _EditStudyInfoResponseLiveData.postValue(this)
                            Log.d(TAG, it.body()!!.message)
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