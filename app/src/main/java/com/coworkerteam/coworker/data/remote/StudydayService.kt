package com.coworkerteam.coworker.data.remote

import com.coworkerteam.coworker.data.model.api.*
import com.coworkerteam.coworker.data.model.api.MyStudyInfoResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface StudydayService {

    @FormUrlEncoded
    @POST("login/{email}")
    fun login(
        @Path("email") email: String,
        @Field("loginType") loginType: String,
        @Field("imgUrl") imgUrl: String
    ): Single<Response<LoginResponse>>

    @FormUrlEncoded
    @POST("token/silent/{nickname}")
    fun autoLogin(
        @Header("refreshToken") refreshToken: String,
        @Path("nickname") nickname: String,
        @Field("value") value: String? = null
    ): Single<Response<AutoLoginResponse>>

    @FormUrlEncoded
    @POST("logout/{nickname}")
    fun logout(
        @Header("refreshToken") refreshToken: String,
        @Path("nickname") nickname: String,
        @Field("value") value: String? = null
    ): Single<Response<ApiRequest>>

    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true, path = "users/{nickname}")
    fun withdrawal(
        @Header("Authorization") accessToken: String,
        @Header("refreshToken") refreshToken: String,
        @Path("nickname") nickname: String,
        @Field("leaveReason") leaveReason: String
    ): Single<Response<ApiRequest>>

    @FormUrlEncoded
    @POST("category/{nickname}")
    fun category(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Field("category") category: String,
    ): Single<Response<ApiRequest>>

    @FormUrlEncoded
    @POST("study")
    fun makeStudy(
        @Header("Authorization") accessToken: String,
        @Field("reqType") reqType: String,
        @Field("studyType") studyType: String,
        @Field("name") name: String,
        @Field("category") category: String,
        @Field("imgUrl") imgUrl: String,
        @Field("pw") pw: String?,
        @Field("maxNum") maxNum: Int,
        @Field("introduce") introduce: String,
    ): Single<Response<StudyRequest>>

    @GET("home/{nickname}")
    fun main(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String
    ): Single<Response<MainResponse>>

    @GET("home/rank/{nickname}")
    fun mainrank(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Query("period") period: String
    ): Single<Response<MainRankResponse>>


    @GET("home/my-study/{nickname}")
    fun mainMyStudyPaging(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Query("page") page: Int
    ): Single<Response<MainMyStudyPagingResponse>>

    @FormUrlEncoded
    @POST("token/{nickname}")
    fun token(
        @Header("refreshToken") refreshToken: String,
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Field("test") test: String? = null
    ): Single<Response<TokenResponse>>

    @GET("my-study/{nickname}")
    fun myStudy(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String
    ): Single<Response<MyStudyResponse>>

    @GET("my-study/group/{nickname}")
    fun myStudyGroupPaging(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Query("page") page: Int
    ): Single<Response<MyStudyGroupPagingResponse>>

    @GET("my-study/open/{nickname}")
    fun myStudyDailyPaging(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Query("page") page: Int
    ): Single<Response<MyStudyDailyPagingResponse>>

    @GET("my-study/manage/{nickname}")
    fun myStudyManage(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String
    ): Single<Response<MyStudyManageResponse>>

    @GET("my-study/manage/{nickname}")
    fun myStudyManagePaging(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Query("page") page: Int
    ): Single<Response<MyStudyManagePagingResponse>>

    @DELETE("my-study/manage/study/{index}")
    fun delteStudy(
        @Header("Authorization") accessToken: String,
        @Path("index") studyIdx: Int
    ): Single<Response<ApiRequest>>

    @DELETE("my-study/manage/leave/{index}")
    fun withdrawStudy(
        @Header("Authorization") accessToken: String,
        @Path("index") studyIdx: Int
    ): Single<Response<ApiRequest>>

    @GET("study/members")
    fun studyMember(
        @Header("Authorization") accessToken: String,
        @Query("studyIdx") studyIdx: Int,
        @Query("reqType") reqType: String
    ): Single<Response<StudyMemberResponse>>

    @DELETE("study/members/{userIdx}")
    fun forcedExit(
        @Header("Authorization") authoriation: String,
        @Path("userIdx") userIdx: Int,
        @Query("studyIdx") studyIdx: Int
    ): Single<Response<ApiRequest>>

    @GET("users/{nickname}")
    fun profileManage(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String
    ): Single<Response<ProfileManageResponse>>

    @FormUrlEncoded
    @PUT("users/{nickname}")
    fun editProfile(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Field("type") type: String,
        @Field("nickname") changNickname: String,
        @Field("category") category: String,
        @Field("imgUrl") imgUrl: String
    ): Single<Response<ApiRequest>>

    @FormUrlEncoded
    @PUT("users/{nickname}")
    fun editProfile_nicknameCheck(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Field("type") type: String,
        @Field("nickname") changNickname: String,
    ): Single<Response<NicknameCheckResponse>>

    @GET("study/search")
    fun studySerch(
        @Header("Authorization") accessToken: String,
        @Query("reqType") reqType: String,
        @Query("category") category: String?,
        @Query("studyType") studyType: String,
        @Query("isJoin") isJoin: Boolean,
        @Query("viewType") viewType: String,
        @Query("keyword") keyword: String?,
        @Query("page") page: Int
    ): Single<Response<StudySearchResponse>>

    @GET("study/search")
    fun studySerchStart(
        @Header("Authorization") accessToken: String,
        @Query("reqType") reqType: String,
        @Query("category") category: String?,
        @Query("studyType") studyType: String,
        @Query("isJoin") isJoin: Boolean,
        @Query("viewType") viewType: String,
        @Query("keyword") keyword: String?,
        @Query("page") page: Int
    ): Single<Response<StudySearchStartResponse>>

    @GET("study/{idx}")
    fun editStudyInfo(
        @Header("Authorization") accessToken: String,
        @Path("idx") studyIdx: Int
    ): Single<Response<EditStudyResponse>>

    @FormUrlEncoded
    @PUT("study/{idx}")
    fun editStudy(
        @Header("Authorization") accessToken: String,
        @Path("idx") studyIdx: Int,
        @Field("name") name: String,
        @Field("category") category: String,
        @Field("imgUrl") imgUrl: String,
        @Field("pw") pw: String?,
        @Field("maxNum") maxNum: Int,
        @Field("introduce") introduce: String
    ): Single<Response<ApiRequest>>

    @FormUrlEncoded
    @PATCH("my-study/manage/leader")
    fun leaderTransfer(
        @Header("Authorization") accessToken: String,
        @Field("newLeaderIdx") newLeaderIdx: Int,
        @Field("studyIdx") studyIdx: Int
    ): Single<Response<ApiRequest>>

    @GET("study-stats/{nickname}")
    fun statistics(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Query("reqType") reqType: String, //start : 처음 검색 페이지에 접속했을 때(사이드 메뉴 데이터까지 필요한 경우) old : 사이드 메뉴 외 데이터만 필요한 경우
        @Query("selectDate") selectDate: String,
        @Query("period") period: String //통계 기간. month : 월간 통계, week : 주간 통계
    ): Single<Response<StatisticsResponse>>

    @GET("todo-list/{nickname}")
    fun todolist(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Query("reqType") reqType: String, //요청 타입. start : 처음 투두 리스트에 접속했을 때 필요한 모든 데이터 리턴(사이드 메뉴 데이터, 목표 공부 시간 등) , old : 달력에서 선택한 날짜의 달성률, 할 일 등을 리턴
        @Query("selectDate") selectDate: String
    ): Single<Response<TodolistResponse>>

    @FormUrlEncoded
    @POST("todo-list/todo/{nickname}")
    fun addTodolist(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Field("selectDate") selectDate: String,
        @Field("todo") todo: String
    ): Single<Response<AddTodolistResponse>>

    @FormUrlEncoded
    @PATCH("todo-list/todo/{idx}")
    fun checkTodolist(
        @Header("Authorization") accessToken: String,
        @Path("idx") todoIdx: Int,
        @Field("selectDate") selectDate: String
    ): Single<Response<CheckTodolistRequest>>

    @FormUrlEncoded
    @PUT("todo-list/todo/{todoIdx}")
    fun editTodolist(
        @Header("Authorization") accessToken: String,
        @Path("todoIdx") todoIdx: Int,
        @Field("selectDate") selectDate: String,
        @Field("todo") todo: String,
    ): Single<Response<EditTodolistResponse>>

    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true, path = "todo-list/todo/{idx}")
    fun removeTodolist(
        @Header("Authorization") accessToken: String,
        @Path("idx") todoIdx: Int,
        @Field("selectDate") selectDate: String
    ): Single<Response<DeleteTodolistRequest>>

    @FormUrlEncoded
    @PUT("todo-list/dream/{nickname}")
    fun editgoal(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Field("aimTime") aimTime: String?,
        @Field("goal") goal: String?,
        @Field("ddayDate") ddayDate: String?,
        @Field("ddayName") ddayName: String?
    ): Single<Response<EditGoalResponse>>

    @GET("study/pre-entrance/{studyIdx}")
    fun enterCamStudy(
        @Header("Authorization") accessToken: String,
        @Path("studyIdx") studyIdx: Int,
        @Query("studyPw") reqType: String?
    ): Single<Response<EnterCamstudyResponse>>

    @GET("study/cam/instance-id")
    fun camStudyInstanceID(
        @Header("Authorization") accessToken: String,
        @Query("link") link: String
    ): Single<Response<InstanceIDResponse>>

    @FormUrlEncoded
    @POST("study/cam/{studyIdx}")
    fun camStudyJoin(
        @Header("Authorization") accessToken: String,
        @Path("studyIdx") studyIdx: Int,
        @Field("test") test: String?
    ): Single<Response<CamStudyJoinResponse>>

    @DELETE("study/cam/participants/{nickname}")
    fun camStudyLeave(
        @Header("Authorization") authoriation: String,
        @Path("nickname") nickname: String,
        @Query("studyIdx") studyIdx: Int,
        @Query("studyTime") studyTime: Int,
        @Query("restTime") restTime: Int
    ): Single<Response<CamstudyLeaveResponse>>

    @GET("study/cam/info/{studyIdx}")
    fun studyInfo(
        @Header("Authorization") accessToken: String,
        @Path("studyIdx") studyIdx: Int,
        @Query("test") test: String? = null
    ): Single<Response<StudyInfoResponse>>

    @GET("study/cam/time/{nickname}")
    fun myStudyInfo(
        @Header("Authorization") accessToken: String,
        @Path("nickname") nickname: String,
        @Query("test") test: String? = null
    ): Single<Response<MyStudyInfoResponse>>

    @FormUrlEncoded
    @POST("your-daily/success")
    fun addSuccessPost(
        @Header("Authorization") accessToken: String,
        @Field("contents") contents: String?
    ): Single<Response<ApiRequest>>

    @FormUrlEncoded
    @POST("your-daily/mood")
    fun addMoodPost(
        @Header("Authorization") accessToken: String,
        @Field("reqType") reqType: String?,
        @Field("mood") mood: Int,
        @Field("contents") contents: String?
    ): Single<Response<ApiRequest>>


    @GET("your-daily/success")
    fun getSuccessPost(
        @Header("Authorization") accessToken: String,
        @Query("sort") sort: String,
        @Query("page") page: Int
    ): Single<Response<SuccessPostResponse>>

    @DELETE("your-daily/success/{postNum}")
    fun removeSuccessPost(
        @Header("Authorization") accessToken: String,
        @Path("postNum") postNum: Int,
    ): Single<Response<PostDeleteResponse>>

    @GET("your-daily/mood")
    fun getMoodPost(
        @Header("Authorization") accessToken: String,
        @Query("sort") sort: String,
        @Query("page") page: Int
    ): Single<Response<MoodPostResponse>>

    @DELETE("your-daily/mood/{postNum}")
    fun removeMoodPost(
        @Header("Authorization") accessToken: String,
        @Path("postNum") postNum: Int,
    ): Single<Response<PostDeleteResponse>>

    @FormUrlEncoded
    @PATCH("your-daily/mood/{postNum}")
    fun editMoodPost(
        @Header("Authorization") accessToken: String,
        @Path("postNum") postNum: Int,
        @Field("mood") mood: Int,
        @Field("contents") contents: String?
    ): Single<Response<ApiRequest>>

    @GET("your-daily/mood/empathy/{postNum}")
    fun empathy(
        @Header("Authorization") accessToken: String,
        @Path("postNum") postNum: Int,
        @Query("empathy") empathy: Int
    ): Single<Response<EmpathyResponse>>



}