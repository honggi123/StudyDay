package com.coworkerteam.coworker.ui.todolist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.*
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class TodoListViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "TodoListViewModel"

    //투두리스트 목록
    private val _TodoListResponseLiveData = MutableLiveData<Response<TodolistResponse>>()
    val TodoListResponseLiveData: LiveData<Response<TodolistResponse>>
        get() = _TodoListResponseLiveData

    //투두리스트 추가
    private val _AddTodoListResponseLiveData = MutableLiveData<Response<AddTodolistResponse>>()
    val AddTodoListResponseLiveData: LiveData<Response<AddTodolistResponse>>
        get() = _AddTodoListResponseLiveData

    //투두리스트 체크
    private val _CheckTodoListResponseLiveData = MutableLiveData<Response<CheckTodolistRequest>>()
    val CheckTodoListResponseLiveData: LiveData<Response<CheckTodolistRequest>>
        get() = _CheckTodoListResponseLiveData

    //투두리스트 삭제
    private val _DeleteTodoListResponseLiveData = MutableLiveData<Response<DeleteTodolistRequest>>()
    val DeleteTodoListResponseLiveData: LiveData<Response<DeleteTodolistRequest>>
        get() = _DeleteTodoListResponseLiveData

    //투두리스트 수정
    private val _EditTodoListResponseLiveData = MutableLiveData<Response<EditTodolistResponse>>()
    val EditTodoListResponseLiveData: LiveData<Response<EditTodolistResponse>>
        get() = _EditTodoListResponseLiveData

    fun getTodoListData(reqType: String, selectDate: String) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getTodolistData(accessToken, nickname, reqType, selectDate)
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
                                    getReissuanceToken(TAG,model,getTodoListData(reqType, selectDate))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _TodoListResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getTodoListData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

    fun setAddTodoListData(selectDate: String, todo: String) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.setAddTodolistData(accessToken, nickname, selectDate, todo)
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
                                    getReissuanceToken(TAG,model,setAddTodoListData(selectDate, todo))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _AddTodoListResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setAddTodoListData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

    fun setCheckTodoListData(todoIdx: Int, selectDate: String) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setCheckTodolist(accessToken, todoIdx, selectDate)
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
                                    getReissuanceToken(TAG,model,setCheckTodoListData(todoIdx, selectDate))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _CheckTodoListResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setCheckTodoListData:: accessToken 값이 없습니다.")
        }
    }

    fun deleteTodoListData(todoIdx: Int, selectDate: String) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setRemoveTodolist(accessToken, todoIdx, selectDate)
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
                                    getReissuanceToken(TAG,model,deleteTodoListData(todoIdx, selectDate))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _DeleteTodoListResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "removeTodoListData:: accessToken 값이 없습니다.")
        }
    }

    fun setEditTodoListData(selectDate: String, todo: String, todoIdx: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setEditTodolist(accessToken, todoIdx, selectDate, todo)
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
                                    getReissuanceToken(TAG,model,setEditTodoListData(selectDate,todo, todoIdx))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _EditTodoListResponseLiveData.postValue(this)
                                }
                            }

                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setEditTodoListData:: accessToken 값이 없습니다.")
        }
    }


}