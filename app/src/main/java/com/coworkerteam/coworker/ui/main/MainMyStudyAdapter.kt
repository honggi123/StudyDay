package com.coworkerteam.coworker.ui.main

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.data.local.Service.CamStudyService
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.MainResponse
import com.coworkerteam.coworker.utils.ScreenSizeUtils

class MainMyStudyAdapter(private val context: Context, private val viewModel: MainViewModel) : RecyclerView.Adapter<MainMyStudyAdapter.ViewHolder>(){

    var datas = mutableListOf<MainResponse.Result.MyStudy>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_main_my_study,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])

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

        fun bind(item: MainResponse.Result.MyStudy) {
            Glide.with(context).load(item.img).into(img)

            if (!item.isLeader) {
                leader.visibility = View.GONE
            }

            studyName.text = item.name

            item_layout.setOnClickListener(View.OnClickListener {
                if(item.isLeader){
                    CamStudyService.isLeader = true
                }else{
                    CamStudyService.isLeader = false
                }

                viewModel.getEnterCamstduyData(item.idx, null)
            })
        }
    }
}