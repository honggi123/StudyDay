package com.coworkerteam.coworker.ui.setting.profile.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.MyStudyResponse
import com.coworkerteam.coworker.data.model.api.NicknameCheckResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import com.google.android.gms.common.api.Api
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class ProfileEditViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "ProfileEditViewModel"

    //내 스터디
    private val _ProfileEditResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val ProfileEditResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _ProfileEditResponseLiveData

    private val _NicknameCheckResponseLiveData = MutableLiveData<Response<NicknameCheckResponse>>()
    val NicknameCheckResponseLiveData: LiveData<Response<NicknameCheckResponse>>
        get() = _NicknameCheckResponseLiveData

    fun setProfileEditData(changNickname: String, category: String, imgUrl: String) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()
        val type = "put"

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.setEditProfileData(accessToken, nickname,type, changNickname, category, imgUrl)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            if(isSuccessful){
                                model.setCurrentUserName(changNickname)
                                model.setCurrentUserProfilePicUrl(imgUrl)
                            }
                            _ProfileEditResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getProfileEditData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

    fun getNicknameCheckData(changNickname: String) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()
        val type = "is_duplicate"

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getNicknameCheckData(accessToken, nickname,type, changNickname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _NicknameCheckResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getNicknameCheckData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }
}