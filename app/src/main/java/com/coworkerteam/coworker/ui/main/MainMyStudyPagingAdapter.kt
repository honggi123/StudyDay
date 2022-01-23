package com.coworkerteam.coworker.ui.main

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.service.CamStudyService
import com.coworkerteam.coworker.data.model.api.MainMyStudyPagingResponse
import com.coworkerteam.coworker.utils.ScreenSizeUtils

class MainMyStudyPagingAdapter(private val viewModel: MainViewModel) :
    PagingDataAdapter<MainMyStudyPagingResponse.Result.MyStudy, MainMyStudyPagingAdapter.ViewHolder>(
        differ
    ) {
    lateinit var context: Context

    companion object {
        private val differ =
            object : DiffUtil.ItemCallback<MainMyStudyPagingResponse.Result.MyStudy>() {
                override fun areItemsTheSame(
                    oldItem: MainMyStudyPagingResponse.Result.MyStudy,
                    newItem: MainMyStudyPagingResponse.Result.MyStudy
                ): Boolean {
                    return false
                }

                override fun areContentsTheSame(
                    oldItem: MainMyStudyPagingResponse.Result.MyStudy,
                    newItem: MainMyStudyPagingResponse.Result.MyStudy
                ): Boolean {
                    return false
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_main_my_study, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        // 간격 설정
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = (ScreenSizeUtils().getScreenWidthSize(context as Activity)/2.25).toInt()
        holder.itemView.requestLayout()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val item_layout: ConstraintLayout = itemView.findViewById(R.id.item_main_my_study)
        private val img: ImageView = itemView.findViewById(R.id.item_my_study_img)
        private val leader: TextView = itemView.findViewById(R.id.item_my_study_leader)
        private val studyName: TextView = itemView.findViewById(R.id.item_my_study_txt_name)
        private val studyNum: TextView = itemView.findViewById(R.id.item_my_study_txt_num)

        fun bind(item: MainMyStudyPagingResponse.Result.MyStudy?) {
            Glide.with(context).load(item!!.img).into(img)

            if (!item.isLeader) {
                leader.visibility = View.GONE
            }

            studyName.text = item.name
            studyNum.text = "참여인원 " + item.userNum.toString() + "/" + item.maxNum.toString()

            item_layout.setOnClickListener(View.OnClickListener {
                CamStudyService.isLeader = item.isLeader

                viewModel.getEnterCamstduyData(item.idx, null)
            })
        }
    }
}