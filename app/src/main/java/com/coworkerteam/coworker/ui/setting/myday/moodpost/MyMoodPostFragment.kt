package com.coworkerteam.coworker.ui.setting.myday.moodpost

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.MoodPostResponse
import com.coworkerteam.coworker.databinding.FragmentMydayMoodpostBinding
import com.coworkerteam.coworker.ui.base.BaseFragment
import com.coworkerteam.coworker.ui.setting.myday.MydayViewModel
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList

class MyMoodPostFragment()
    : BaseFragment<FragmentMydayMoodpostBinding, MydayViewModel>()  {
    val TAG = "MyMoodPostFragment"
    override val layoutResourceID: Int
        get() = R.layout.fragment_myday_moodpost
    override val viewModel: MydayViewModel by viewModel()
    lateinit var moodPostAdapter: MyMoodPostAdapter
    var datas = ArrayList<MoodPostResponse.Result.MoodPost?>()
    lateinit var rv_MoodPost : RecyclerView
    var isLoading = false

    private var page = 0       // 현재 페이지
    private var totalpage = 0     // 한 번에 가져올 아이템 수
    private var sort = "latest"       // 현재 페이지


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.MoodPostPagingData.observe(this,  androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    if (isLoading){
                        datas.removeAt(datas.size - 1)
                        val scrollPosition: Int = datas.size
                        moodPostAdapter.notifyItemRemoved(scrollPosition)
                        isLoading = false
                    }else{
                        rv_MoodPost.scrollToPosition(0)
                    }

                    totalpage = it.body()?.result?.get(0)?.totalPage!!
                    moodPostAdapter.adddata(it.body()?.result?.get(0)?.moodPosts)
                   if (totalpage == 0){
                       viewDataBinding.mydayMoodpostEmptyView.visibility = View.VISIBLE
                   }else{
                       viewDataBinding.mydayMoodpostEmptyView.visibility = View.GONE
                   }


                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 검색 데이터 가져오기가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(activity,"검색 데이터를 가져오지 못했습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })

        viewModel.EmpathyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    Log.d(TAG,"empathydata : "+it.body())
                    if (it.body()!!.empathy.moodPost.get(0).totalEmpathy == 0){
                        datas.get(moodPostAdapter.postPosition)?.empathy_kinds = null.toString()
                    }else{
                        datas.get(moodPostAdapter.postPosition)?.empathy_kinds = it.body()!!.empathy.moodPost.get(0).empathyKinds
                    }
                    datas.get(moodPostAdapter.postPosition)?.my_empathy = it.body()!!.empathy.moodPost.get(0).my_empathy
                    datas.get(moodPostAdapter.postPosition)?.total_empathy = it.body()!!.empathy.moodPost.get(0).totalEmpathy
                    datas.get(moodPostAdapter.postPosition)?.is_empathy = it.body()!!.empathy.moodPost.get(0).isEmpathy

                    moodPostAdapter.notifyItemChanged(moodPostAdapter.postPosition)

                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))
                    Toast.makeText(activity,"검색 데이터를 가져오지 못했습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT).show()

                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })


        viewModel.DeletePostResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {

                    if (moodPostAdapter.removeitem?.idx == it.body()?.deleteIdx){
                        var position = datas.indexOf(moodPostAdapter.removeitem)
                        datas.remove(moodPostAdapter.removeitem)

                        moodPostAdapter.notifyItemRemoved(position)
                    }

                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 검색 데이터 가져오기가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(activity,"검색 데이터를 가져오지 못했습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
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

    override fun initStartView() {
        moodPostAdapter = MyMoodPostAdapter(viewModel)
        datas.clear()
        moodPostAdapter.setdata(datas)

        rv_MoodPost = view?.findViewById<RecyclerView>(R.id.fragment_yourday_moodpost_rv)!!
        rv_MoodPost?.adapter = moodPostAdapter
        rv_MoodPost?.layoutManager = LinearLayoutManager(activity)

        initScrollListener()

        val btn_latest = viewDataBinding.yourdayMoodpostTxtLatest
        val btn_empathyNum = viewDataBinding.yourdayMoodpostTxtEmpathyNum

        btn_latest.setOnClickListener(View.OnClickListener {
            sortEvent(it,btn_empathyNum,"latest")
            sort = "latest"
            firebaseLog.addLog(TAG,"sort_latest")
        })

        btn_empathyNum.setOnClickListener(View.OnClickListener {
            sortEvent(it,btn_latest,"empathy")
            sort = "empathy"
            firebaseLog.addLog(TAG,"sort_empathy")
        })

        sortEvent(btn_latest,btn_empathyNum,"latest")
        sort = "latest"
    }

    override fun initDataBinding() {


    }

    override fun initAfterBinding() {
    }

    fun newInstant(): MyMoodPostFragment {
        val args = Bundle()
        val frag = MyMoodPostFragment()
        frag.arguments = args
        return frag
    }



    private fun initScrollListener() {
        rv_MoodPost.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged( recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled( recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == datas.size - 1) {
                        //리스트 마지막
                        if (page <= totalpage){
                            loadMore()
                            isLoading = true
                        }
                    }
                }
            }
        })
    }

    private fun loadMore() {
        firebaseLog.addLog(TAG,"load_more")
        datas.add(null)
        moodPostAdapter.notifyItemInserted(datas.size - 1)
        val handler = android.os.Handler()
        isLoading = true
        handler.postDelayed({
            viewModel.getMyMoodPost(sort,getPage())
        },1000)
    }

    private fun getPage(): Int {
        page++
        return page
    }

    fun searchEvent(sort: String) {
        page = 0
        datas.clear()

        viewModel.getMyMoodPost(sort,getPage())
        isLoading = false
    }

    fun sortEvent(view: View, otherView: View, sort: String) {
        if (view.isSelected) {
        } else {
            view.isSelected = true
            otherView.isSelected = false
           // StudySearchActivity.viewType = sort
        }
        rv_MoodPost.layoutManager = LinearLayoutManager(context)
        rv_MoodPost.itemAnimator = null
        searchEvent(sort)
    }


}