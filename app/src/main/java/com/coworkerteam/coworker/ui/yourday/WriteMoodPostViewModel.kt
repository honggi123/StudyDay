package com.coworkerteam.coworker.ui.yourday

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.model.api.StudySearchStartResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class WriteMoodPostViewModel (private val model: UserRepository) : BaseViewModel() {
    private val TAG = "StudySearchViewModel"

    //검색 맨 처음 데이터
    val _StudySearchStartLiveData = MutableLiveData<Response<StudySearchStartResponse>>()
    val StudySearchStartLiveData: LiveData<Response<StudySearchStartResponse>>
        get() = _StudySearchStartLiveData

    //스터디 입장전 데이터
    private val _EnterCamstudyResponseLiveData = MutableLiveData<Response<EnterCamstudyResponse>>()
    val EnterCamstudyResponseLiveData: LiveData<Response<EnterCamstudyResponse>>
        get() = _EnterCamstudyResponseLiveData

    //페이징 데이터
    private lateinit var _StudySearchPagingData: MutableLiveData<PagingData<StudySearchResponse.Result.Study>>
    val StudySearchPagingData: LiveData<PagingData<StudySearchResponse.Result.Study>>
        get() = _StudySearchPagingData



}