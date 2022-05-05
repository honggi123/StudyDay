package com.coworkerteam.coworker.ui.yourday


import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.FragmentYourdayMoodpostBinding
import com.coworkerteam.coworker.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoodPostFragment
    : BaseFragment<FragmentYourdayMoodpostBinding, YourdayViewModel>()  {

    override val layoutResourceID: Int
        get() = R.layout.fragment_yourday_moodpost
    override val viewModel: YourdayViewModel by viewModel()
    lateinit var moodPostAdapter: MoodPostAdapter
    var datas = ArrayList<String?>()
    lateinit var rv_MoodPost : RecyclerView
    var isLoading = false

    override fun initStartView() {
        moodPostAdapter = MoodPostAdapter()
        datas.add("Item ")
        datas.add("Item ")
        datas.add("Item ")
        datas.add("Item ")
        datas.add("Item ")

        moodPostAdapter.setdata(datas)
        rv_MoodPost = view?.findViewById<RecyclerView>(R.id.fragment_yourday_moodpost_rv)!!
        rv_MoodPost?.adapter = moodPostAdapter
        rv_MoodPost?.layoutManager = LinearLayoutManager(activity)

        initScrollListener()
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
    }

    fun newInstant(): MoodPostFragment {
        val args = Bundle()
        val frag = MoodPostFragment()
        frag.arguments = args
        return frag
    }



    private fun initScrollListener() {
        rv_MoodPost.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == datas.size - 1) {
                        //리스트 마지막
                        datas.add(null)
                        moodPostAdapter.notifyItemInserted(datas.size - 1)
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }


    private fun loadMore() {
        val handler = Handler()
        handler.postDelayed(Runnable {
            datas.removeAt(datas.size - 1)
            val scrollPosition: Int = datas.size
            moodPostAdapter.notifyItemRemoved(scrollPosition)
            var currentSize = scrollPosition
            val nextLimit = currentSize + 5
            while (currentSize < nextLimit) {
                datas.add("Item $currentSize")
                currentSize++
            }
            moodPostAdapter.notifyDataSetChanged()
            isLoading = false
        }, 1000)
    }


}