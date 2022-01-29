package com.coworkerteam.coworker.ui.category

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast

import android.widget.TextView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityCategoryBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.main.MainActivity
import org.json.JSONObject
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
            when {
                it.isSuccessful -> {
                    //메인으로 이동
                    moveMain()
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 카테고리 설정이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"카테고리 설정을 실패했습니다.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
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