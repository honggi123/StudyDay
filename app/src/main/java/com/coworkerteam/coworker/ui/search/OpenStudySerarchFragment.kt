package com.coworkerteam.coworker.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.model.other.SearchStudy
import com.coworkerteam.coworker.databinding.FragmentOpenStudySerarchBinding
import com.coworkerteam.coworker.ui.base.BaseFragment
import com.coworkerteam.coworker.ui.camstudy.enter.EnterCamstudyActivity
import com.coworkerteam.coworker.ui.dialog.PasswordDialog
import com.coworkerteam.coworker.utils.RecyclerViewUtils
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OpenStudySerarchFragment :
    BaseFragment<FragmentOpenStudySerarchBinding, StudySearchViewModel>() {
    var TAG = "OpenStudySerarchFragment"

    lateinit var studySearchResponse: StudySearchResponse

    override val layoutResourceID: Int
        get() = R.layout.fragment_open_study_serarch
    override val viewModel: StudySearchViewModel by viewModel()

    lateinit var pagingStudySearchAdapter: StudySearchPagingAdapter

    override fun initStartView() {
        pagingStudySearchAdapter = StudySearchPagingAdapter()
        pagingStudySearchAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && pagingStudySearchAdapter.itemCount < 1) {
                requireView().findViewById<TextView>(R.id.open_search_empty_view).visibility = View.VISIBLE
            } else {
                requireView().findViewById<TextView>(R.id.open_search_empty_view).visibility = View.GONE
            }
        }
        val rv_Search = view?.findViewById<RecyclerView>(R.id.fragment_open_study_rv)
        rv_Search?.adapter = pagingStudySearchAdapter
        RecyclerViewUtils().setHorizonSpaceDecration(rv_Search!!,10)

        viewModel.getStudySearchData("open")

    }

    override fun initDataBinding() {

        StudySearchActivity.StudySearchLiveData.observe(this, androidx.lifecycle.Observer {
            //검색결과를 성공적으로 반환
            searchEvent(it)
        })

        viewModel.StudySearchPagingData.observe(this,androidx.lifecycle.Observer {
            pagingStudySearchAdapter.submitData(lifecycle,it)
        })

    }

    override fun initAfterBinding() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_open_study_serarch, container, false)
    }

    fun newInstant(): OpenStudySerarchFragment {
        val args = Bundle()
        val frag = OpenStudySerarchFragment()
        frag.arguments = args
        return frag
    }

    fun searchEvent(studyInfo : SearchStudy) {
        pagingStudySearchAdapter.refresh()
    }
}