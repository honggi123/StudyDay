package com.coworkerteam.coworker.ui.setting.to.developer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityCategoryBinding
import com.coworkerteam.coworker.databinding.ActivityToDeveloperBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ToDeveloperActivity : BaseActivity<ActivityToDeveloperBinding, ToDeveloperViewModel>() {
    private val TAG = "ToDeveloperActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_to_developer
    override val viewModel: ToDeveloperViewModel by viewModel()

    override fun initStartView() {
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
    }

}