package com.coworkerteam.coworker.data.remote

import com.coworkerteam.coworker.data.model.api.NaverResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface NaverService {

    @GET("v1/nid/me")
    fun getUserData(
        @Header("Authorization") accessToken: String
    ): Single<Response<NaverResponse>>

}