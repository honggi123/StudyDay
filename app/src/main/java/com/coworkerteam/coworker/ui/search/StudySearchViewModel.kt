package com.coworkerteam.coworker.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.MyStudyDailyPagingResponse
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.model.dto.SearchStudy
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class StudySearchViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "StudySearchViewModel"

    //스터디 입장전 데이터
    private val _StudySearchResponseLiveData = MutableLiveData<Response<StudySearchResponse>>()
    val StudySearchResponseLiveData: LiveData<Response<StudySearchResponse>>
        get() = _StudySearchResponseLiveData

    //검색 데이터
    val _StudySearchLiveData = MutableLiveData<SearchStudy>()
    val StudySearchLiveData: LiveData<SearchStudy>
        get() = _StudySearchLiveData

    //페이징 데이터
    private lateinit var _StudySearchPagingData: MutableLiveData<PagingData<StudySearchResponse.Result.Study>>
    val StudySearchPagingData: LiveData<PagingData<StudySearchResponse.Result.Study>>
        get() = _StudySearchPagingData

    fun getStudySearchData(
        reqType: String,
        category: String?,
        studyType: String,
        isJoin: Boolean,
        viewType: String,
        keword: String?,
    ) {
        _StudySearchPagingData =
            model.getStudySerchData(reqType, category, studyType, isJoin, viewType, keword)
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