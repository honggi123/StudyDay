package com.coworkerteam.coworker.ui.study.memberlist

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StudyMemberResponse
import com.coworkerteam.coworker.ui.study.leader.transfer.MemberManagementAdapter

class MemberListAdapter(private val context: Context) :
    RecyclerView.Adapter<MemberListAdapter.ViewHolder>() {

    val TAG = "MemberListAdapter"

    var datas = mutableListOf<StudyMemberResponse.Result>()
    var study_idx = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_study_member_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = itemView.findViewById(R.id.item_member_list_profile)
        private val nickname: TextView = itemView.findViewById(R.id.item_member_list_nickname)

        fun bind(item: StudyMemberResponse.Result) {
            Glide.with(context).load(item.img).into(img)

            nickname.text = item.nickname
        }
    }
}