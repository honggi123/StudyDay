package com.coworkerteam.coworker.ui.yourday

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.FragmentYourdaySuccesspostBinding
import com.coworkerteam.coworker.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SuccessPostFragment
    : BaseFragment<FragmentYourdaySuccesspostBinding, YourdayViewModel>()  {

    override val layoutResourceID: Int
        get() = R.layout.fragment_yourday_successpost
    override val viewModel: YourdayViewModel by viewModel()
    lateinit var successPostAdapter: SuccessPostAdapter
    val datas = ArrayList<String?>()
    lateinit var rv_SuccessPost : RecyclerView
     var isLoading = false
    var datas_postbg = ArrayList<Int>()

    override fun initStartView(){
        successPostAdapter = SuccessPostAdapter()
        datas.add("Item ")
        datas.add("Item ")
        datas.add("Item ")
        datas.add("Item ")
        datas.add("Item ")
        datas_postbg.add(R.drawable.card_back1)
        datas_postbg.add(R.drawable.card_back2)
        datas_postbg.add(R.drawable.card_back3)
        datas_postbg.add(R.drawable.card_back4)
        datas_postbg.add(R.drawable.card_back5)

        successPostAdapter.setdata(datas,datas_postbg)
        rv_SuccessPost = view?.findViewById<RecyclerView>(R.id.fragment_yourday_successpost_rv)!!
        rv_SuccessPost?.adapter = successPostAdapter
        rv_SuccessPost?.layoutManager = LinearLayoutManager(activity)

        initScrollListener()
    }


    override fun initDataBinding(){

    }

    override fun initAfterBinding(){
    }

    fun newInstant(): SuccessPostFragment {
        val args = Bundle()
        val frag = SuccessPostFragment()
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
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }


    private fun loadMore() {
        datas.add(null)
        successPostAdapter.notifyItemInserted(datas.size - 1)
        val handler = Handler()
        handler.postDelayed(Runnable {
            datas.removeAt(datas.size - 1)
            val scrollPosition: Int = datas.size
            successPostAdapter.notifyItemRemoved(scrollPosition)
            var currentSize = scrollPosition
            val nextLimit = currentSize + 5
            while (currentSize < nextLimit) {
                datas.add("Item $currentSize")
                currentSize++
            }
            successPostAdapter.notifyDataSetChanged()
            isLoading = false
        }, 1000)
    }




}