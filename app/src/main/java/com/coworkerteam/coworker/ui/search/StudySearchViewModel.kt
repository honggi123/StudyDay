package com.coworkerteam.coworker.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.model.api.StudySearchStartResponse
import com.coworkerteam.coworker.data.model.other.SearchStudy
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class StudySearchViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "StudySearchViewModel"

    //검색 맨 처음 데이터
    val _StudySearchStartLiveData = MutableLiveData<Response<StudySearchStartResponse>>()
    val StudySearchStartLiveData: LiveData<Response<StudySearchStartResponse>>
        get() = _StudySearchStartLiveData

    //페이징 데이터
    private lateinit var _StudySearchPagingData: MutableLiveData<PagingData<StudySearchResponse.Result.Study>>
    val StudySearchPagingData: LiveData<PagingData<StudySearchResponse.Result.Study>>
        get() = _StudySearchPagingData

    fun getStudySearchStartData() {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getStudySerchStartData(accessToken)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _StudySearchStartLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getStudySearchStartData:: accessToken 값이 없습니다.")
        }
    }

    fun getStudySearchData(
        studyType: String
    ) {
        _StudySearchPagingData =
            model.getStudySerchData(studyType)
                .cachedIn(viewModelScope) as MutableLiveData<PagingData<StudySearchResponse.Result.Study>>
    }

    fun getLoginType(): String? {
        return model.getCurrentUserLoggedInMode()
    }

    fun getUserProfile(): String? {
        return model.getCurrentUserProfilePicUrl()
    }

    fun getUserNickname(): String? {
        return model.getCurrentUserName()
    }
}