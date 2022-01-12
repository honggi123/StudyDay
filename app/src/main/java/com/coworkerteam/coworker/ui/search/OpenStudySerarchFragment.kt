package com.coworkerteam.coworker.ui.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.model.dto.SearchStudy
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.databinding.FragmentGroupStudySerarchBinding
import com.coworkerteam.coworker.databinding.FragmentOpenStudySerarchBinding
import com.coworkerteam.coworker.ui.base.BaseFragment
import com.coworkerteam.coworker.ui.study.management.ManagementPagingAdapter
import com.coworkerteam.coworker.utils.RecyclerViewUtils
import com.google.android.gms.common.api.ApiException
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class OpenStudySerarchFragment :
    BaseFragment<FragmentOpenStudySerarchBinding, StudySearchViewModel>() {
    var TAG = "OpenStudySerarchFragment"

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var studySearchResponse: StudySearchResponse

    override val layoutResourceID: Int
        get() = R.layout.fragment_open_study_serarch
    override val viewModel: StudySearchViewModel by viewModel()

    lateinit var pagingStudySearchAdapter: StudySearchPagingAdapter

    override fun initStartView() {
        pagingStudySearchAdapter = StudySearchPagingAdapter()
        val rv_Search = view?.findViewById<RecyclerView>(R.id.fragment_open_study_rv)
        rv_Search?.adapter = pagingStudySearchAdapter
    }

    override fun initDataBinding() {
        viewModel.StudySearchResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //검색결과를 성공적으로 반환
            if (it.isSuccessful) {
                studySearchResponse = it.body()!!
                rv_init()
            }
        })

        viewModel.StudySearchLiveData.observe(this, androidx.lifecycle.Observer {
            //검색결과를 성공적으로 반환
            searchEvent(it)
        })
    }

    override fun initAfterBinding() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_open_study_serarch, container, false)
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    fun newInstant(): OpenStudySerarchFragment {
        val args = Bundle()
        val frag = OpenStudySerarchFragment()
        frag.arguments = args
        return frag
    }

    fun searchEvent(studyInfo : SearchStudy) {
        viewModel.getStudySearchData(
            "search",
            studyInfo.category,
            "open",
            studyInfo.isJoin,
            studyInfo.viewType,
            studyInfo.keyword
        )
    }

    fun rv_init() {
        //새로운
        var recyclerNewStudy =
            view?.findViewById<RecyclerView>(R.id.fragment_open_study_rv)
        var newAdapter: SearchAdapter = SearchAdapter(requireContext())

        newAdapter.datas = studySearchResponse.result.study.toMutableList()
        recyclerNewStudy!!.adapter = newAdapter
    }
}