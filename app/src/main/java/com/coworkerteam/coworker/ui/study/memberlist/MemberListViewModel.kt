package com.coworkerteam.coworker.ui.study.memberlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.StudyMemberResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MemberListViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "MemberListViewModel"

    //내 스터디 멤버
    private val _StudyMemberResponseLiveData = MutableLiveData<Response<StudyMemberResponse>>()
    val StudyMemberResponseLiveData: LiveData<Response<StudyMemberResponse>>
        get() = _StudyMemberResponseLiveData


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

}