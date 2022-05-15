package com.coworkerteam.coworker.ui.yourday.moodPost.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class EditMoodPostViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "StudySearchViewModel"

    //글수정 데이터
    private  var _MoodEditPostData = MutableLiveData<Response<ApiRequest>>()
    val MoodEditPostData: LiveData<Response<ApiRequest>>
        get() = _MoodEditPostData

    fun setEditMoodPost(postnum: Int, mood: Int, contents: String) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.setEditMoodPost(accessToken, postnum, mood, contents)
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
                                    getReissuanceToken(TAG,model,setEditMoodPost(postnum, mood, contents))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _MoodEditPostData.postValue(it)
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setGoalCamstduyData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

}