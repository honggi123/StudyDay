package com.coworkerteam.coworker.data.local.prefs

interface PreferencesHelper {
    fun getAccessToken(): String?

    fun setAccessToken(accessToken: String?)

    fun getRefreshToken(): String?

    fun setRefreshToken(refreshToken: String?)

    fun getCurrentUserEmail(): String?

    fun setCurrentUserEmail(email: String?)

    fun getCurrentUserLoggedInMode(): String?

    fun setCurrentUserLoggedInMode(mode: String?)

    fun getCurrentUserName(): String?

    fun setCurrentUserName(userName: String?)

    fun getCurrentUserProfilePicUrl(): String?

    fun setCurrentUserProfilePicUrl(profilePicUrl: String?)

    fun setPreferencesData(
        accessToken: String,
        refreshToken: String,
        nickname: String,
        email: String,
        loginType: String,
        imageUri: String
    )

    fun setLocalNickname(nickname: String)

    fun deletePreferencesData()
}