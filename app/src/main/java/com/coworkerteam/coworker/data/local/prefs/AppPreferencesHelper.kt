package com.coworkerteam.coworker.data.local.prefs
import android.util.Log

import android.content.Context
import android.content.SharedPreferences
import com.coworkerteam.coworker.di.PreferenceInfo

class AppPreferencesHelper() : PreferencesHelper {

    private val PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN"

    private val PREF_KEY_REFRESH_TOKEN = "PREF_KEY_REFRESH_TOKEN"

    private val PREF_KEY_CURRENT_USER_EMAIL = "PREF_KEY_CURRENT_USER_EMAIL"

//    private val PREF_KEY_CURRENT_USER_ID = "PREF_KEY_CURRENT_USER_ID"

    private val PREF_KEY_CURRENT_USER_NAME = "PREF_KEY_CURRENT_USER_NAME"

    private val PREF_KEY_CURRENT_USER_PROFILE_PIC_URL = "PREF_KEY_CURRENT_USER_PROFILE_PIC_URL"

    private val PREF_KEY_USER_LOGGED_IN_MODE = "PREF_KEY_USER_LOGGED_IN_MODE"


    private var mPrefs: SharedPreferences? = null

    constructor(context: Context, @PreferenceInfo prefFileName: String) : this() {
        mPrefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)
    }

    override fun getAccessToken(): String? {
        return "bearer " + mPrefs!!.getString(PREF_KEY_ACCESS_TOKEN, null)
    }

    override fun setAccessToken(accessToken: String?) {
        mPrefs!!.edit().putString(PREF_KEY_ACCESS_TOKEN, accessToken).apply()
    }

    override fun getRefreshToken(): String? {
        return mPrefs!!.getString(PREF_KEY_REFRESH_TOKEN, null)
    }

    override fun setRefreshToken(refreshToken: String?) {
        mPrefs!!.edit().putString(PREF_KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    override fun getCurrentUserEmail(): String? {
        return mPrefs!!.getString(PREF_KEY_CURRENT_USER_EMAIL, null)
    }

    override fun setCurrentUserEmail(email: String?) {
        mPrefs!!.edit().putString(PREF_KEY_CURRENT_USER_EMAIL, email).apply()
    }

    override fun getCurrentUserLoggedInMode(): String? {
        return mPrefs!!.getString(PREF_KEY_USER_LOGGED_IN_MODE, null)
    }

    override fun setCurrentUserLoggedInMode(mode: String?) {
        mPrefs!!.edit().putString(PREF_KEY_USER_LOGGED_IN_MODE, mode)
            .apply()
    }

    override fun getCurrentUserName(): String? {
        return mPrefs!!.getString(PREF_KEY_CURRENT_USER_NAME, null)
    }

    override fun setCurrentUserName(userName: String?) {
        mPrefs!!.edit().putString(PREF_KEY_CURRENT_USER_NAME, userName).apply()
    }

    override fun getCurrentUserProfilePicUrl(): String? {
        return mPrefs!!.getString(PREF_KEY_CURRENT_USER_PROFILE_PIC_URL, null)
    }

    override fun setCurrentUserProfilePicUrl(profilePicUrl: String?) {
        mPrefs!!.edit()
            .putString(PREF_KEY_CURRENT_USER_PROFILE_PIC_URL, profilePicUrl)
            .apply()
    }

    override fun setLocalNickname(nickname: String) {
        setCurrentUserName(nickname)
    }


    override fun setPreferencesData(
        accessToken: String,
        refreshToken: String,
        nickname: String,
        email: String,
        loginType: String,
        imageUri: String
    ) {
        setAccessToken(accessToken)
        setRefreshToken(refreshToken)
        setCurrentUserName(nickname)
        setCurrentUserEmail(email)
        setCurrentUserLoggedInMode(loginType)
        setCurrentUserProfilePicUrl(imageUri)
    }

    override fun deletePreferencesData() {
        setAccessToken(null)
        setRefreshToken(null)
        setCurrentUserName(null)
        setCurrentUserEmail(null)
        setCurrentUserLoggedInMode(null)
        setCurrentUserProfilePicUrl(null)
    }
}