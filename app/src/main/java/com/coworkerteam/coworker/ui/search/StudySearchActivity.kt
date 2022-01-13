package com.coworkerteam.coworker.ui.search

import android.view.View
import android.widget.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.model.other.SearchStudy
import com.coworkerteam.coworker.databinding.ActivityStudySearchBinding
import com.coworkerteam.coworker.ui.base.NavigationAcitivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudySearchActivity :
    NavigationAcitivity<ActivityStudySearchBinding, StudySearchViewModel>() {
    val TAG = "StudySearchActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_study_search
    override val viewModel: StudySearchViewModel by viewModel()

    override val drawerLayout: DrawerLayout
        get() = findViewById(R.id.drawer_layout)
    override val navigatinView: NavigationView
        get() = findViewById(R.id.navigationView)

    companion object {
        //검색 데이터
        val _StudySearchLiveData = MutableLiveData<SearchStudy>()
        val StudySearchLiveData: LiveData<SearchStudy>
            get() = _StudySearchLiveData

        var keword: String? = null

        var isJoin = false //바로 참여 가능 여부
        var viewType = "latest" // 정렬 방식. latest : 최신순, studyTime : 공부 시간순
        var category = ArrayList<String>()

        fun getCategory(): String? {
            var categorys: String? = category.joinToString("|")
            if (categorys.isNullOrBlank()) {
                categorys = null
            }
            return categorys
        }
    }

    lateinit var studySearchResponse: StudySearchResponse

    override fun initStartView() {
        super.initStartView()
        initSearchOption()

        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.search_toolber)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경
        supportActionBar?.title = "스터디 찾기"

        viewDataBinding.activitiy = this

        setNavigaionLoginImage(viewModel.getLoginType()!!)
        setNavigaionProfileImage(viewModel.getUserProfile()!!)
        setNavigaionNickname(viewModel.getUserNickname()!!)

        init()
        fragment_init()
    }

    override fun initDataBinding() {

    }

    override fun initAfterBinding() {
    }

    fun init() {
        val searchView = findViewById<SearchView>(R.id.study_search_searchview)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                keword = query
                searchEvent()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        val check_join = findViewById<CheckBox>(R.id.study_search_check_join)

        val btn_latest = findViewById<TextView>(R.id.study_search_txt_latest)
        val btn_studyTime = findViewById<TextView>(R.id.study_search_txt_studyTime)

        btn_latest.setSelected(true)

        btn_latest.setOnClickListener(View.OnClickListener {
            sortEvent(it, btn_studyTime, "latest")
        })

        btn_studyTime.setOnClickListener(View.OnClickListener {
            sortEvent(it, btn_latest, "studyTime")
        })

        check_join.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                isJoin = true
                searchEvent()
            } else {
                isJoin = false
                searchEvent()
            }
        })
    }

    fun initSearchOption() {
        keword = null

        isJoin = false //바로 참여 가능 여부
        viewType = "latest" // 정렬 방식. latest : 최신순, studyTime : 공부 시간순
        category.clear()
    }

    fun fragment_init() {
        val pagerAdapter = FragmentAdapter(supportFragmentManager)
        val pager = findViewById<ViewPager>(R.id.study_serarch_viewPager)
        pager.visibility = View.VISIBLE
        pager.adapter = pagerAdapter
        val tab = findViewById<TabLayout>(R.id.study_serarch_tab)
        tab.setupWithViewPager(pager)
    }

    fun showFilter() {
        if (viewDataBinding.studySerarchFilter.visibility == View.VISIBLE) {
            viewDataBinding.studySerarchFilter.visibility = View.GONE
        } else {
            viewDataBinding.studySerarchFilter.visibility = View.VISIBLE
        }
    }

    fun searchEvent() {
        var categorys: String? = category.joinToString("|")
        if (categorys.isNullOrBlank()) {
            categorys = null
        }
        var searchStudy = SearchStudy(categorys, isJoin, viewType, keword)
        _StudySearchLiveData.postValue(searchStudy)
    }

    fun sortEvent(view: View, otherView: View, sort: String) {
        if (view.isSelected) {
        } else {
            view.setSelected(true)
            otherView.setSelected(false)
            viewType = sort
        }
        searchEvent()
    }

    fun clickCategoryButton(v: View) {
        val view = v as TextView
        val categoryName = v.text.toString()

        if (view.isSelected) {
            view.setSelected(false)
            category.remove(categoryName)
        } else {
            view.setSelected(true)
            category.add(categoryName)
        }
        searchEvent()
    }

}
