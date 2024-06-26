package com.coworkerteam.coworker.data

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.model.api.*
import com.coworkerteam.coworker.data.model.paging.*
import com.coworkerteam.coworker.data.remote.NaverService
import com.coworkerteam.coworker.data.remote.StudydayService
import io.reactivex.Single
import retrofit2.Response
import java.net.URLEncoder

class UserRepositoryImpl(
    private val service: StudydayService,
    private val naverService: NaverService,
    private val pref: PreferencesHelper
) : UserRepository {

    override fun getAccessToken(): String? {
        return pref.getAccessToken()
    }

    override fun setAccessToken(accessToken: String) {
        pref.setAccessToken(accessToken)
    }

    override fun getRefreshToken(): String? {
        return pref.getRefreshToken()
    }

    override fun setRefreshToken(refreshToken: String) {
        pref.setRefreshToken(refreshToken)
    }

    override fun getCurrentUserEmail(): String? {
        return pref.getCurrentUserEmail()
    }

    override fun setCurrentUserEmail(email: String) {
        pref.setCurrentUserEmail(email)
    }

    override fun getCurrentUserLoggedInMode(): String? {
        return pref.getCurrentUserLoggedInMode()
    }

    override fun setCurrentUserLoggedInMode(mode: String) {
        pref.setCurrentUserLoggedInMode(mode)
    }

    override fun getCurrentUserName(): String? {
        return pref.getCurrentUserName()
    }

    override fun setCurrentUserName(userName: String) {
        pref.setCurrentUserName(userName)
    }

    override fun getCurrentUserProfilePicUrl(): String? {
        return pref.getCurrentUserProfilePicUrl()
    }

    override fun setCurrentUserProfilePicUrl(profilePicUrl: String) {
        pref.setCurrentUserProfilePicUrl(profilePicUrl)
    }

    override fun getTokenResetData(
        refreshToken: String,
        accessToken: String,
        nickname: String
    ): Single<Response<TokenResponse>> {
        return service.token(
            refreshToken,
            accessToken,
            nickname
        )
    }

    override fun getLoginData(
        email: String,
        loginType: String,
        imgUrl: String
    ): Single<Response<LoginResponse>> {
        return service.login(email, loginType, imgUrl)
    }

    override fun setPreferencesData(
        accessToken: String,
        refreshToken: String,
        nickname: String,
        email: String,
        loginType: String,
        imageUri: String
    ) {
        pref.setPreferencesData(accessToken, refreshToken, nickname, email, loginType, imageUri)
    }

    override fun getIsGoalSuccess(): String? {
        return pref.getGoalIsSuccess()
    }

    override fun setPreferenceIsSuccess(
        goalIsSuccess: Boolean,
        goalSuccesstime: Int,
        goalPostIsWrite: Boolean
    ) {
        pref.setGoalIsSuccess(goalIsSuccess, goalSuccesstime, goalPostIsWrite)
    }

    override fun setnickname(nickname: String) {
        pref.setLocalNickname(nickname)
    }

    override fun deletePreferencesData() {
        pref.deletePreferencesData()
    }

    override fun getAutoLoginData(
        refreshToken: String,
        nickname: String
    ): Single<Response<AutoLoginResponse>> {
        return service.autoLogin(refreshToken, nickname)
    }

    override fun setLogoutData(
        refreshToken: String,
        nickname: String
    ): Single<Response<ApiRequest>> {
        return service.logout(refreshToken, nickname)
    }

    override fun setWithdrawalData(
        accessToken: String,
        refreshToken: String,
        nickname: String,
        leaveReason: String
    ): Single<Response<ApiRequest>> {
        return service.withdrawal(accessToken, refreshToken, nickname, leaveReason)
    }

    override fun getNaverUserData(accessToken: String): Single<Response<NaverResponse>> {
        return naverService.getUserData(accessToken)
    }

    override fun setCategotyData(
        accessToken: String,
        nickname: String,
        category: String
    ): Single<Response<ApiRequest>> {
        return service.category(accessToken, nickname, category)
    }

    override fun setMakeStudyData(
        accessToken: String,
        reqType: String,
        studyType: String,
        name: String,
        category: String,
        imgUrl: String,
        pw: String?,
        maxNum: Int,
        introduce: String
    ): Single<Response<StudyRequest>> {
        return service.makeStudy(
            accessToken,
            reqType,
            studyType,
            name,
            category,
            imgUrl,
            pw,
            maxNum,
            introduce
        )
    }

    override fun getMainData(
        accessToken: String,
        nickname: String
    ): Single<Response<MainResponse>> {
        return service.main(accessToken, nickname)
    }

    override fun getMainMyStudyPagingData(): LiveData<PagingData<MainMyStudyPagingResponse.Result.MyStudy>> {
        return Pager(
            config = PagingConfig(pageSize = 3, enablePlaceholders = false),
            pagingSourceFactory = {
                MainMyStudyPagingSource(service, pref)
            }
        ).liveData
    }

    override fun getMainRankData(
        accessToken: String,
        nickname: String,
        period: String
    ): Single<Response<MainRankResponse>> {
        return service.mainrank(accessToken, nickname,period)
    }

    override fun getMyStudyData(
        accessToken: String,
        nickname: String
    ): Single<Response<MyStudyResponse>> {
        return service.myStudy(accessToken, nickname)
    }

    override fun getMyStudyGroupPagingData(): LiveData<PagingData<MyStudyGroupPagingResponse.Result.Group>> {
        return Pager(
            config = PagingConfig(pageSize = 4, enablePlaceholders = false),
            pagingSourceFactory = {
                MyStudyGroupPagingSource(service, pref)
            }
        ).liveData
    }

    override fun getMyStudyDailyPagingData(): LiveData<PagingData<MyStudyDailyPagingResponse.Result.Open>> {
        return Pager(
            config = PagingConfig(pageSize = 4, enablePlaceholders = false),
            pagingSourceFactory = {
                MyStudyDailyPagingSource(service, pref)
            }
        ).liveData
    }

    override fun getMyStudyManageData(
        accessToken: String,
        nickname: String
    ): Single<Response<MyStudyManageResponse>> {
        return service.myStudyManage(accessToken, nickname)
    }

    override fun getMyStudyManagePagingData(): LiveData<PagingData<MyStudyManagePagingResponse.Result.Group>> {
        return Pager(
            config = PagingConfig(pageSize = 4, enablePlaceholders = false),
            pagingSourceFactory = {
                MyStudyManagePagingSource(service, pref)
            }
        ).liveData
    }

    override fun setDeleteStudyData(
        accessToken: String,
        studyIdx: Int
    ): Single<Response<ApiRequest>> {
        return service.delteStudy(accessToken, studyIdx)
    }

    override fun setWithdrawStudyData(
        accessToken: String,
        studyIdx: Int
    ): Single<Response<ApiRequest>> {
        return service.withdrawStudy(accessToken, studyIdx)
    }

    override fun getStudyMemberData(
        accessToken: String,
        studyIdx: Int,
        reqType: String
    ): Single<Response<StudyMemberResponse>> {
        return service.studyMember(accessToken, studyIdx, reqType)
    }

    override fun setForcedExitData(
        accessToken: String,
        userIdx: Int,
        studyIdx: Int
    ): Single<Response<ApiRequest>> {
        return service.forcedExit(accessToken, userIdx, studyIdx)
    }

    override fun getProfileManageData(
        accessToken: String,
        nickname: String
    ): Single<Response<ProfileManageResponse>> {
        return service.profileManage(accessToken, nickname)
    }

    override fun setEditProfileData(
        accessToken: String,
        nickname: String,
        type: String,
        changNickname: String,
        category: String,
        imgUrl: String
    ): Single<Response<ApiRequest>> {
        return service.editProfile(accessToken, nickname, type, changNickname, category, imgUrl)
    }

    override fun getNicknameCheckData(
        accessToken: String,
        nickname: String,
        type: String,
        changNickname: String
    ): Single<Response<NicknameCheckResponse>> {
        return service.editProfile_nicknameCheck(accessToken, nickname, type, changNickname)
    }

    override fun getStudySerchData(
        studyType: String
    ): LiveData<PagingData<StudySearchResponse.Result.Study>> {
        return Pager(
            config = PagingConfig(pageSize = 4, enablePlaceholders = false),
            pagingSourceFactory = {
                StudySearchPagingSource(
                    service,
                    pref,
                    studyType
                )
            }
        ).liveData
    }

    //네비게이션 정보만 단순히 사용하기 위한것..ㅠㅠ 효율적인 구도는 아니라 나중 수정 필요
    override fun getStudySerchStartData(accessToken: String): Single<Response<StudySearchStartResponse>> {
        return service.studySerchStart(accessToken, reqType = "start", category = null,studyType = "open",isJoin = false,viewType="latest",keyword = null,page = 1)
    }

    override fun getEditStudyData(
        accessToken: String,
        studyIdx: Int
    ): Single<Response<EditStudyResponse>> {
        return service.editStudyInfo(accessToken, studyIdx)
    }

    override fun setEditStudyData(
        accessToken: String,
        studyIdx: Int,
        name: String,
        category: String,
        imgUrl: String,
        pw: String?,
        maxNum: Int,
        introduce: String
    ): Single<Response<ApiRequest>> {
        return service.editStudy(
            accessToken,
            studyIdx,
            name,
            category,
            imgUrl,
            pw,
            maxNum,
            introduce
        )
    }

    override fun setLeaderTransferData(
        accessToken: String,
        newLeaderIdx: Int,
        studyIdx: Int
    ): Single<Response<ApiRequest>> {
        return service.leaderTransfer(accessToken, newLeaderIdx, studyIdx)
    }

    override fun getStatisticsData(
        accessToken: String,
        nickname: String,
        reqType: String,
        selectDate: String,
        period: String
    ): Single<Response<StatisticsResponse>> {
        return service.statistics(accessToken, nickname, reqType, selectDate, period)
    }

    override fun getTodolistData(
        accessToken: String,
        nickname: String,
        reqType: String,
        selectDate: String
    ): Single<Response<TodolistResponse>> {
        return service.todolist(accessToken, nickname, reqType, selectDate)
    }

    override fun setAddTodolistData(
        accessToken: String,
        nickname: String,
        selectDate: String,
        todo: String
    ): Single<Response<AddTodolistResponse>> {
        return service.addTodolist(accessToken, nickname, selectDate, todo)
    }


    override fun setCheckTodolist(
        accessToken: String,
        todoIdx: Int,
        selectDate: String
    ): Single<Response<CheckTodolistRequest>> {
        return service.checkTodolist(accessToken, todoIdx, selectDate)
    }

    override fun setEditTodolist(
        accessToken: String,
        todoIdx: Int,
        selectDate: String,
        todo: String,
    ): Single<Response<EditTodolistResponse>> {
        return service.editTodolist(accessToken, todoIdx, selectDate, todo)
    }

    override fun setRemoveTodolist(
        accessToken: String,
        todoIdx: Int,
        selectDate: String
    ): Single<Response<DeleteTodolistRequest>> {
        return service.removeTodolist(accessToken, todoIdx, selectDate)
    }

    override fun setEditGoalData(
        accessToken: String,
        nickname: String,
        aimTime: String?,
        goal: String?,
        ddayDate: String?,
        ddayName: String?
    ): Single<Response<EditGoalResponse>> {
        return service.editgoal(accessToken, nickname, aimTime, goal, ddayDate, ddayName)
    }

    override fun getEnterCamStudyData(
        accessToken: String,
        studyIdx: Int,
        password: String?
    ): Single<Response<EnterCamstudyResponse>> {
        if(password != null){
            var encoded_s = Base64.encodeToString(password?.toByteArray(),0)
            encoded_s = URLEncoder.encode(encoded_s, "UTF-8")
            return service.enterCamStudy(accessToken, studyIdx, encoded_s)
        }else{
            return service.enterCamStudy(accessToken, studyIdx, password)
        }
    }

    override fun getCamStudystudyInstanceData(
        accessToken: String,
        link: String
    ): Single<Response<InstanceIDResponse>> {
        return service.camStudyInstanceID(accessToken, link)
    }

    override fun getCamStudyJoinData(
        accessToken: String,
        studyIdx: Int
    ): Single<Response<CamStudyJoinResponse>> {
        return service.camStudyJoin(accessToken, studyIdx, null)
    }

    override fun getCamStudyLeaveData(
        accessToken: String,
        nickname: String,
        studyIdx: Int,
        studyTime: Int,
        restTime: Int
    ): Single<Response<CamstudyLeaveResponse>> {
        return service.camStudyLeave(accessToken, nickname, studyIdx, studyTime, restTime)
    }

    override fun getStudyInfoData(
        accessToken: String,
        studyIdx: Int
    ): Single<Response<StudyInfoResponse>> {
        return service.studyInfo(accessToken, studyIdx)
    }

    override fun getMyStudyInfoData(
        accessToken: String,
        nickname: String
    ): Single<Response<MyStudyInfoResponse>> {
        return service.myStudyInfo(accessToken, nickname)
    }

    override fun setSuccessPost(
        accessToken: String,
        contents: String?
    ): Single<Response<ApiRequest>> {
        return service.addSuccessPost(accessToken,contents)
    }

    override fun setMoodPost(
        accessToken: String,
        reqType: String,
        mood: Int,
        contents: String?
    ): Single<Response<ApiRequest>> {
        return service.addMoodPost(accessToken,reqType,mood,contents)
    }


    override fun getSuccessPost(
        accessToken: String,page : Int)
    : Single<Response<SuccessPostResponse>> {
        return service.getSuccessPost(accessToken,"latest",page)
    }

    override fun setRemoveSuccessPost(
        accessToken: String,
        postNum: Int
    ): Single<Response<PostDeleteResponse>> {
        return service.removeSuccessPost(accessToken,postNum)
    }

    override fun getMoodPost(
        accessToken: String,
        sort: String,
        page: Int
    ): Single<Response<MoodPostResponse>> {
        return service.getMoodPost(accessToken,sort,page)
    }

    override fun setRemoveMoodPost(
        accessToken: String,
        postNum: Int
    ): Single<Response<PostDeleteResponse>> {
        return service.removeMoodPost(accessToken,postNum)
    }

    override fun setEditMoodPost(
        accessToken: String,
        postNum: Int,
        mood: Int,
        contents: String
    ): Single<Response<ApiRequest>> {
        return service.editMoodPost(accessToken,postNum,mood,contents)
    }

    override fun setEmpathy(
        accessToken: String,
        postNum: Int,
        mood: Int
    ): Single<Response<EmpathyResponse>> {
        return service.empathy(accessToken,postNum,mood)

    }

    override fun getMyMoodPost(
        accessToken: String,
        nickname: String?,
        sort: String,
        page: Int
    ): Single<Response<MoodPostResponse>> {
        return service.getMyMoodPost(accessToken,nickname,sort,page)
    }

    override fun getMySuccessPost(
        accessToken: String,
        nickname: String?,
        page: Int
    ): Single<Response<SuccessPostResponse>> {
        return service.getMySuccessPost(accessToken,nickname,page)
    }
}