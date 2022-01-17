package com.coworkerteam.coworker.utils

import android.os.Bundle
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class FirebaseAnalyticsUtils(private val pref: PreferencesHelper) {
    private var firebaseAnalytics = Firebase.analytics

    fun addLog(screen: String, event: String) {
        val params = Bundle()
        params.putString("login_type", pref.getCurrentUserLoggedInMode())
        params.putString("email", pref.getCurrentUserEmail())
        params.putString(FirebaseAnalytics.Param.SCREEN_NAME, screen)
        firebaseAnalytics.logEvent(event,params)
    }
}