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

class YourdayViewModel (private val model: UserRepository) : BaseViewModel() {
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

    fun getStudySearchStartData() {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getStudySerchStartData(accessToken)
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
                                    getReissuanceToken(TAG,model,getStudySearchStartData())
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _StudySearchStartLiveData.postValue(this)
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getStudySearchStartData:: accessToken 값이 없습니다.")
        }
    }

    fun getEnterCamstduyData(studyIdx: Int, password: String?) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getEnterCamStudyData(accessToken, studyIdx, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, "meta : $it")

                            if (isSuccessful) {
                                it.body()!!.result.studyInfo.idx = studyIdx
                            }

                            when {
                                it.code() == 401 -> {
                                    //액세스토큰이 만료된 경우
                                    Log.d(TAG, "액세스토큰이 만료된 경우")

                                    //액세스 토큰 재발급
                                    getReissuanceToken(TAG,model,getEnterCamstduyData(studyIdx,password))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _EnterCamstudyResponseLiveData.postValue(it)
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

    fun getStudySearchData(
        studyType: String
    ) {
        _StudySearchPagingData =
            model.getStudySerchData(studyType)
                .cachedIn(viewModelScope) as MutableLiveData<PagingData<StudySearchResponse.Result.Study>>
    }
}