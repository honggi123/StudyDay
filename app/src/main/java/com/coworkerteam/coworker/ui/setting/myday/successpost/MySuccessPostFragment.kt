package com.coworkerteam.coworker.ui.setting.myday.successpost

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.SuccessPostResponse
import com.coworkerteam.coworker.databinding.FragmentMydaySuccesspostBinding
import com.coworkerteam.coworker.ui.base.BaseFragment
import com.coworkerteam.coworker.ui.setting.myday.MydayViewModel
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MySuccessPostFragment()
    : BaseFragment<FragmentMydaySuccesspostBinding, MydayViewModel>()  {
    val TAG = "MySuccessPostFragment"
    override val layoutResourceID: Int
        get() = R.layout.fragment_myday_successpost
    override val viewModel: MydayViewModel by viewModel()
    lateinit var mySuccessPostAdapter: MySuccessPostAdapter
    val datas = ArrayList<SuccessPostResponse.Result.SuccessPost?>()
    lateinit var rv_SuccessPost : RecyclerView
     var isLoading = false

    private var page = 0       // 현재 페이지
    private var totalpage = 0     // 한 번에 가져올 아이템 수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.SuccessPostPagingData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    if (isLoading){
                        datas.removeAt(datas.size - 1)
                        val scrollPosition: Int = datas.size
                        mySuccessPostAdapter.notifyItemRemoved(scrollPosition)
                        mySuccessPostAdapter.notifyDataSetChanged()
                        isLoading = false
                    }
                    totalpage = it.body()?.result?.get(0)?.totalPage!!
                    mySuccessPostAdapter.adddata(it.body()?.result?.get(0)?.successPosts)
                    Log.d(TAG,"SIZE" + it.body()?.result?.get(0)?.successPosts)
                    if (totalpage ==0){
                        viewDataBinding.mydaySuccesspostEmptyView.visibility = View.VISIBLE
                    }else{
                        viewDataBinding.mydaySuccesspostEmptyView.visibility = View.GONE
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

        viewModel.DeletePostResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    if (mySuccessPostAdapter.removeitem?.idx == it.body()?.deleteIdx){
                        var position = datas.indexOf(mySuccessPostAdapter.removeitem)
                        datas.remove(mySuccessPostAdapter.removeitem)
                        mySuccessPostAdapter.notifyItemRemoved(position)
                    }
                    if (datas.size == 0){
                        viewDataBinding.mydaySuccesspostEmptyView.visibility = View.VISIBLE
                    }else{
                        viewDataBinding.mydaySuccesspostEmptyView.visibility = View.GONE
                    }
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 검색 데이터 가져오기가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(activity,"검색 데이터를 가져오지 못했습니다. 나중에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않는 게시물 또는 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -6 -> {
                            //존재하지 않는 게시물일 경우
                            Toast.makeText(context, "해당 게시물이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                it.code() == 500 -> {
                    // 서버에서 게시물 삭제에 실패할 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, "게시물 삭제에 실패했습니다. 다시 시도해주세요.")
                    moveLogin()
                }
            }
        })
    }

    override fun initStartView(){
        mySuccessPostAdapter = MySuccessPostAdapter(viewModel)

        viewModel.getMySuccessPost(getPage())
        mySuccessPostAdapter.setdata(datas)

        rv_SuccessPost = view?.findViewById<RecyclerView>(R.id.fragment_myday_successpost_rv)!!
        rv_SuccessPost?.adapter = mySuccessPostAdapter
        rv_SuccessPost?.layoutManager = LinearLayoutManager(activity)

        initScrollListener()
    }


    override fun initDataBinding(){

    }

    override fun initAfterBinding(){
    }

    fun newInstant(): MySuccessPostFragment {
        val args = Bundle()
        val frag = MySuccessPostFragment()
        frag.arguments = args
        return frag
    }


    private fun initScrollListener() {
        rv_SuccessPost.setOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        datas.add(null)
        mySuccessPostAdapter.notifyItemInserted(datas.size - 1)
        val handler = android.os.Handler()
        isLoading = true
        handler.postDelayed({
            viewModel.getMySuccessPost(getPage())
        },1000)
    }

    private fun getPage(): Int {
        page++
        return page
    }
}