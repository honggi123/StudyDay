package com.coworkerteam.coworker.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class StudySearchViewModel(private val model: UserRepository) : BaseViewModel()  {
    private val TAG = "StudySearchViewModel"

    //스터디 입장전 데이터
    private val _StudySearchResponseLiveData = MutableLiveData<Response<StudySearchResponse>>()
    val StudySearchResponseLiveData: LiveData<Response<StudySearchResponse>>
        get() = _StudySearchResponseLiveData

    fun getStudySearchData(reqType: String, category: String?, studyType: String, isJoin: Boolean, viewType: String, keword: String?, page: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getStudySerchData(accessToken, reqType, category, studyType, isJoin, viewType, keword, page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _StudySearchResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "setCategoryData:: accessToken 또는 nickname 값이 없습니다.")
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