package com.coworkerteam.coworker.ui.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.model.other.SearchStudy
import com.coworkerteam.coworker.databinding.FragmentGroupStudySerarchBinding
import com.coworkerteam.coworker.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class GroupStudySerarchFragment :
    BaseFragment<FragmentGroupStudySerarchBinding, StudySearchViewModel>() {
    var TAG = "GroupStudySerarchFragment"

    lateinit var studySearchResponse: StudySearchResponse

    override val layoutResourceID: Int
        get() = R.layout.fragment_group_study_serarch
    override val viewModel: StudySearchViewModel by viewModel()

    lateinit var pagingStudySearchAdapter: StudySearchPagingAdapter

    override fun initStartView() {
        pagingStudySearchAdapter = StudySearchPagingAdapter()
        pagingStudySearchAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && pagingStudySearchAdapter.itemCount < 1) {
                viewDataBinding.groupSearchEmptyView.visibility = View.VISIBLE
            } else {
                viewDataBinding.groupSearchEmptyView.visibility = View.GONE
            }
        }
        val rv_Search = view?.findViewById<RecyclerView>(R.id.fragment_group_study_rv)
        rv_Search?.adapter = pagingStudySearchAdapter

        viewModel.getStudySearchData("group")
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
        Log.d(TAG, "onCreateView")
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_group_study_serarch, container, false)
    }

    fun newInstant(): GroupStudySerarchFragment {
        Log.d(TAG, "newInstant")
        val args = Bundle()
        val frag = GroupStudySerarchFragment()
        frag.arguments = args
        return frag
    }

    fun searchEvent(studyInfo : SearchStudy){
        pagingStudySearchAdapter.refresh()
    }

}