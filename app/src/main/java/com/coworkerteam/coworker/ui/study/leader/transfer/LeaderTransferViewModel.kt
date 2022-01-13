package com.coworkerteam.coworker.ui.study.leader.transfer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.ProfileManageResponse
import com.coworkerteam.coworker.data.model.api.StudyMemberResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class LeaderTransferViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "LeaderTransferViewModel"

    //내 스터디 멤버
    private val _StudyMemberResponseLiveData = MutableLiveData<Response<StudyMemberResponse>>()
    val StudyMemberResponseLiveData: LiveData<Response<StudyMemberResponse>>
        get() = _StudyMemberResponseLiveData

    //리더 양도
    private val _LeaderTransferResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val LeaderTransferResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _LeaderTransferResponseLiveData
    
    //멤버 추방
    private val _ForcedExitResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val ForcedExitResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _ForcedExitResponseLiveData

    fun getStudyMemberData(studyIdx: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getStudyMemberData(accessToken, studyIdx, "manageMember")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _StudyMemberResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getStudyMemberData:: accessToken 값이 없습니다.")
        }
    }

    fun setLeaderTransferData(newLeaderIdx: Int, studyIdx: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setLeaderTransferData(accessToken, newLeaderIdx, studyIdx)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _LeaderTransferResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setLeaderTransferData:: accessToken 값이 없습니다.")
        }
    }

    fun setForcedExitData(userIdx: Int, studyIdx: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setForcedExitData(accessToken, userIdx, studyIdx)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _ForcedExitResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setLeaderTransferData:: accessToken 값이 없습니다.")
        }
    }
}