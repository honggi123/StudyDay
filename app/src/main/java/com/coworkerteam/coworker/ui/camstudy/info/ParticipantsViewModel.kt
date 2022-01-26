package com.coworkerteam.coworker.ui.camstudy.info

import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.ui.base.BaseViewModel

class ParticipantsViewModel(private val model: UserRepository) : BaseViewModel()  {

    fun getUserNickName(): String? {
        return model.getCurrentUserName()
    }
}