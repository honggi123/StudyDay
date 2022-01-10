package com.coworkerteam.coworker.ui.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.AutoLoginResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class CategoryViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "CategoryViewModel"

    //스터디 입장전 데이터
    private val _CategoryResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val CategoryResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _CategoryResponseLiveData

    fun setCategoryData(category: String) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.setCategotyData(accessToken, nickname,category)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _CategoryResponseLiveData.postValue(this)
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
}