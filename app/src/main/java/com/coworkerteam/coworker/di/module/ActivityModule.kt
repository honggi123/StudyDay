package com.coworkerteam.coworker.di.module

import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.UserRepositoryImpl
import com.coworkerteam.coworker.ui.base.BaseViewModel
import com.coworkerteam.coworker.ui.camstudy.cam.CamStudyViewModel
import com.coworkerteam.coworker.ui.camstudy.enter.EnterCamstudyViewModel
import com.coworkerteam.coworker.ui.camstudy.info.MyStudyInfoViewModel
import com.coworkerteam.coworker.ui.camstudy.info.ParticipantsViewModel
import com.coworkerteam.coworker.ui.camstudy.info.StudyInfoViewModel
import com.coworkerteam.coworker.ui.category.CategoryViewModel
import com.coworkerteam.coworker.ui.login.LoginViewModel
import com.coworkerteam.coworker.ui.main.MainViewModel
import com.coworkerteam.coworker.ui.mystudy.MyStudyViewModel
import com.coworkerteam.coworker.ui.search.StudySearchViewModel
import com.coworkerteam.coworker.ui.setting.SettingViewModel
import com.coworkerteam.coworker.ui.setting.account.WithdrawalViewModel
import com.coworkerteam.coworker.ui.setting.profile.MyProfileViewModel
import com.coworkerteam.coworker.ui.setting.profile.edit.ProfileEditViewModel
import com.coworkerteam.coworker.ui.setting.to.developer.ToDeveloperViewModel
import com.coworkerteam.coworker.ui.splash.SplashViewModel
import com.coworkerteam.coworker.ui.statistics.StatisticsViewModel
import com.coworkerteam.coworker.ui.study.edit.EditStudyViewModel
import com.coworkerteam.coworker.ui.study.leader.transfer.LeaderTransferViewModel
import com.coworkerteam.coworker.ui.study.make.MakeStudyViewModel
import com.coworkerteam.coworker.ui.study.management.ManagementViewModel
import com.coworkerteam.coworker.ui.todolist.TodoListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var viewModelModule = module {
    viewModel {
        BaseViewModel()
    }
    viewModel {
        SplashViewModel(get())
    }
    viewModel {
        LoginViewModel(get())
    }
    viewModel {
        CategoryViewModel(get())
    }
    viewModel{
        MainViewModel(get())
    }
    viewModel {
        MyStudyViewModel(get())
    }
    viewModel {
        StudySearchViewModel(get())
    }
    viewModel {
        WithdrawalViewModel(get())
    }
    viewModel {
        MyProfileViewModel(get())
    }
    viewModel{
        ProfileEditViewModel(get())
    }
    viewModel {
        ToDeveloperViewModel(get())
    }
    viewModel {
        SettingViewModel(get())
    }
    viewModel {
        StatisticsViewModel(get())
    }
    viewModel {
        EditStudyViewModel(get())
    }
    viewModel {
        LeaderTransferViewModel(get())
    }
    viewModel {
        MakeStudyViewModel(get())
    }
    viewModel {
        ManagementViewModel(get())
    }
    viewModel {
        TodoListViewModel(get())
    }
    viewModel{
        EnterCamstudyViewModel(get())
    }
    viewModel {
        CamStudyViewModel(get())
    }
    viewModel{
        StudyInfoViewModel(get())
    }
    viewModel{
        MyStudyInfoViewModel(get())
    }
    viewModel {
        ParticipantsViewModel(get())
    }
}

var dataModelModule = module {
    factory<UserRepository> {
        UserRepositoryImpl(get(),get(),get())
    }
}

var myActivityModule = listOf(viewModelModule,dataModelModule)