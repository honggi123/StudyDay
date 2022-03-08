package com.coworkerteam.coworker.data

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.*
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response

//레퍼지토리 interface 파일. 이곳에 형식을 선언 후 UserRepositoryImpl에 구체적으로 생성한다.
interface UserRepository {

    //local 저장 관련 함수
    fun getAccessToken(): String?

    fun setAccessToken(accessToken: String)

    fun getRefreshToken(): String?

    fun setRefreshToken(refreshToken: String)

    fun getCurrentUserEmail(): String?

    fun setCurrentUserEmail(email: String)

    fun getCurrentUserLoggedInMode(): String?

    fun setCurrentUserLoggedInMode(mode: String)

    fun getCurrentUserName(): String?

    fun setCurrentUserName(userName: String)

    fun getCurrentUserProfilePicUrl(): String?

    fun setCurrentUserProfilePicUrl(profilePicUrl: String)

    fun setPreferencesData(
        accessToken: String,
        refreshToken: String,
        nickname: String,
        email: String,
        loginType: String,
        imageUri: String
    )

    fun deletePreferencesData()

    //api 관련 함수

    fun getTokenResetData(
        refreshToken: String,
        accessToken: String,
        nickname: String
    ): Single<Response<TokenResponse>>

    fun getLoginData(
        email: String,
        loginType: String,
        imgUrl: String
    ): Single<Response<LoginResponse>>

    fun getAutoLoginData(
        refreshToken: String,
        nickname: String
    ): Single<Response<AutoLoginResponse>>

    fun setLogoutData(refreshToken: String, nickname: String): Single<Response<ApiRequest>>

    fun setWithdrawalData(
        accessToken: String,
        refreshToken: String,
        nickname: String,
        leaveReason: String
    ): Single<Response<ApiRequest>>

    fun getNaverUserData(accessToken: String): Single<Response<NaverResponse>>

    fun setCategotyData(
        accessToken: String,
        nickname: String,
        category: String
    ): Single<Response<ApiRequest>>

    fun setMakeStudyData(
        accessToken: String,
        reqType: String,
        studyType: String,
        name: String,
        category: String,
        imgUrl: String,
        pw: String?,
        maxNum: Int,
        introduce: String
    ): Single<Response<StudyRequest>>

    fun getMainData(accessToken: String, nickname: String): Single<Response<MainResponse>>

    fun getMainMyStudyPagingData(): LiveData<PagingData<MainMyStudyPagingResponse.Result.MyStudy>>

    fun getMyStudyData(accessToken: String, nickname: String): Single<Response<MyStudyResponse>>

    fun getMyStudyGroupPagingData(): LiveData<PagingData<MyStudyGroupPagingResponse.Result.Group>>

    fun getMyStudyDailyPagingData(): LiveData<PagingData<MyStudyDailyPagingResponse.Result.Open>>

    fun getMyStudyManageData(
        accessToken: String,
        nickname: String
    ): Single<Response<MyStudyManageResponse>>

    fun getMyStudyManagePagingData(): LiveData<PagingData<MyStudyManagePagingResponse.Result.Group>>

    fun setDeleteStudyData(accessToken: String, studyIdx: Int): Single<Response<ApiRequest>>

    fun setWithdrawStudyData(accessToken: String, studyIdx: Int): Single<Response<ApiRequest>>

    fun getStudyMemberData(
        accessToken: String,
        studyIdx: Int,
        reqType: String
    ): Single<Response<StudyMemberResponse>>

    fun setForcedExitData(
        accessToken: String,
        userIdx: Int,
        studyIdx: Int
    ):Single<Response<ApiRequest>>

    fun getProfileManageData(
        accessToken: String,
        nickname: String
    ): Single<Response<ProfileManageResponse>>

    fun setEditProfileData(
        accessToken: String,
        nickname: String,
        type: String,
        changNickname: String,
        category: String,
        imgUrl: String
    ): Single<Response<ApiRequest>>

    fun getNicknameCheckData(
        accessToken: String,
        nickname: String,
        type: String,
        changNickname: String
    ): Single<Response<NicknameCheckResponse>>

    fun getStudySerchData(
        studyType: String,
    ): LiveData<PagingData<StudySearchResponse.Result.Study>>

    fun getStudySerchStartData(accessToken: String) : Single<Response<StudySearchStartResponse>>

    fun getEditStudyData(accessToken: String, studyIdx: Int): Single<Response<EditStudyResponse>>

    fun setEditStudyData(
        accessToken: String,
        studyIdx: Int,
        name: String,
        category: String,
        imgUrl: String,
        pw: String?,
        maxNum: Int,
        introduce: String
    ): Single<Response<ApiRequest>>

    fun setLeaderTransferData(
        accessToken: String,
        newLeaderIdx: Int,
        studyIdx: Int
    ): Single<Response<ApiRequest>>

    fun getStatisticsData(
        accessToken: String,
        nickname: String,
        reqType: String,
        selectDate: String,
        period: String
    ): Single<Response<StatisticsResponse>>

    fun getTodolistData(
        accessToken: String,
        nickname: String,
        reqType: String,
        selectDate: String
    ): Single<Response<TodolistResponse>>

    fun setAddTodolistData(
        accessToken: String,
        nickname: String,
        selectDate: String,
        todo: String
    ): Single<Response<AddTodolistResponse>>


    fun setCheckTodolist(
        accessToken: String,
        todoIdx: Int,
        selectDate: String
    ): Single<Response<CheckTodolistRequest>>

    fun setEditTodolist(
        accessToken: String,
        todoIdx: Int,
        selectDate: String,
        todo: String,
    ): Single<Response<EditTodolistResponse>>

    fun setRemoveTodolist(
        accessToken: String,
        todoIdx: Int,
        selectDate: String
    ): Single<Response<DeleteTodolistRequest>>

    fun setEditGoalData(
        accessToken: String,
        nickname: String,
        aimTime: String?,
        goal: String?,
        ddayDate: String?,
        ddayName: String?
    ): Single<Response<EditGoalResponse>>

    fun getEnterCamStudyData(
        accessToken: String,
        studyIdx: Int,
        password: String?
    ): Single<Response<EnterCamstudyResponse>>

    fun getCamStudystudyInstanceData(
        accessToken: String,
        link:String
    ): Single<Response<InstanceIDResponse>>

    fun getCamStudyJoinData(
        accessToken: String,
        studyIdx: Int
    ): Single<Response<CamStudyJoinResponse>>

    fun getCamStudyLeaveData(
        accessToken: String,
        nickname: String,
        studyIdx: Int,
        studyTime: Int,
        restTime: Int
    ): Single<Response<ApiRequest>>

    fun getStudyInfoData(accessToken: String, studyIdx: Int): Single<Response<StudyInfoResponse>>

    fun getMyStudyInfoData(
        accessToken: String,
        nickname: String
    ): Single<Response<MyStudyInfoResponse>>
}