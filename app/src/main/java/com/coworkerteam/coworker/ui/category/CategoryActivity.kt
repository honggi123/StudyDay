package com.coworkerteam.coworker.ui.category

import android.content.Intent
import android.view.View
import android.widget.Toast

import android.widget.TextView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityCategoryBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoryActivity : BaseActivity<ActivityCategoryBinding, CategoryViewModel>() {

    private val TAG = "CategoryActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_category
    override val viewModel: CategoryViewModel by viewModel()

    var categorys = ArrayList<String>()

    override fun initStartView() {
        viewDataBinding.activitiy = this@CategoryActivity
    }

    override fun initDataBinding() {
        viewModel.CategoryResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //카테고리가 성공적으로 선택
            if (it.isSuccessful) {
                //메인으로 이동
                moveMain()
            }
        })
    }

    override fun initAfterBinding() {
    }

    fun clickCategoryButton(v: View) {
        val view = v as TextView
        //카테고리 이름
        val categoryName = view.text.toString()

        if (view.isSelected) {
            //선택되어있던 카테고리라면 선택해제
            view.setSelected(false)
            categorys.remove(categoryName)

        } else {
            //선택되지 않았던 카테고리라면 선택추가
            if (categorys.size >= 3) {
                //카테고리가 3개 이상 이미 선택되어 있다면
                Toast.makeText(this, "카테고리는 최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                //카테고리가 3개 이하라면
                view.setSelected(true)
                categorys.add(categoryName)
            }
        }
    }

    fun clickNextButton() {
        if (categorys.size > 0) {
            val category = categorys.joinToString("|")
            viewModel.setCategoryData(category)
        } else {
            Toast.makeText(this, "카테고리를 1가지 이상 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    fun moveMain() {
        //메인으로 이동
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}