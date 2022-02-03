package com.coworkerteam.coworker.ui.study.leader.transfer

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

class MemberManagementAdapter(private val context: Context, private val viewModel:LeaderTransferViewModel) :
    RecyclerView.Adapter<MemberManagementAdapter.ViewHolder>() {

    val TAG = "LeaderTransferAdapter"

    var datas = mutableListOf<StudyMemberResponse.Result>()
    var emptyView: View? = null
    var study_idx = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_study_member_management, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if(datas.size <= 0){
            emptyView!!.visibility = View.VISIBLE
        }else{
            emptyView!!.visibility = View.GONE
        }
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = itemView.findViewById(R.id.item_study_member_management_profile)
        private val leader: Button = itemView.findViewById(R.id.item_study_member_management_btn)
        private val nickname: TextView = itemView.findViewById(R.id.item_study_member_management_nickname)

        fun bind(item: StudyMemberResponse.Result) {
            Glide.with(context).load(item.img).into(img)

            nickname.text = item.nickname

            leader.setOnClickListener(View.OnClickListener {
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_leader_transfer, null)
                val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                val builder = mBuilder.show()

                builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val txt_discript = mDialogView.findViewById<TextView>(R.id.textView62)

                txt_discript.text = "정말로 "+item.nickname+"님을 스터디에서 추방하겠습니까?"
                val btn_cancle = mDialogView.findViewById<Button>(R.id.dialog_leader_transfer_btn_cancle)
                val btn_logout = mDialogView.findViewById<Button>(R.id.dialog_leader_transfer_btn_leader)
                
                btn_logout.text = "추방"

                btn_cancle.setOnClickListener(View.OnClickListener {
                    builder.dismiss()
                })

                btn_logout.setOnClickListener(View.OnClickListener {
                    viewModel.setForcedExitData(item.idx,study_idx)
                    datas.remove(item)

                    notifyDataSetChanged()
                    builder.dismiss()
                })
            })
        }
    }
}