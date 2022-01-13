package com.coworkerteam.coworker.ui.study.management
import android.util.Log

import android.content.Context
import android.content.Intent
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
import com.coworkerteam.coworker.data.model.dto.MyStudy
import com.coworkerteam.coworker.ui.main.StudyCategoryAdapter
import com.coworkerteam.coworker.ui.study.edit.EditStudyActivity
import com.coworkerteam.coworker.ui.study.leader.transfer.LeaderTransferActivity
import com.coworkerteam.coworker.ui.study.memberlist.MemberListActivity

class ManagementAdapter(private val context: Context, private val viewModel: ManagementViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = "ManagementAdapter"

    val viewLeader = 0
    val viewNotLeader = 1

    var datas = mutableListOf<MyStudy>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType) {
            viewLeader -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_study_management_leader,
                    parent,
                    false
                )
                ViewHolderLeader(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_study_management_member,
                    parent,
                    false
                )
                ViewHolderNotLeader(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun getItemViewType(position: Int): Int {
        if (datas[position].isLeader) {
            return viewLeader
        } else {
            return viewNotLeader
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (datas[position].isLeader) {
            (holder as ViewHolderLeader).bind(datas[position])
//            holder.setIsRecyclable(false)
        } else {
            (holder as ViewHolderNotLeader).bind(datas[position])
//            holder.setIsRecyclable(false)
        }
    }

    inner class ViewHolderLeader(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = itemView.findViewById(R.id.item_study_menage_img)
        private val studyName: TextView = itemView.findViewById(R.id.item_study_menage_txt_name)
        private val txt_context: TextView = itemView.findViewById(R.id.item_study_menage_context)
        private val btn_withdraw: Button =
            itemView.findViewById(R.id.item_study_menage_leader_btn_withdraw)
        private val btn_modify: Button = itemView.findViewById(R.id.item_study_menage_btn_modify)
        private val leader: Button = itemView.findViewById(R.id.item_study_menage_btn_leader)
        private val rvCategory: RecyclerView =
            itemView.findViewById(R.id.item_study_menage_rv_category)

        fun bind(item: MyStudy) {
            Glide.with(context).load(item.img).into(img)

            studyName.text = item.name
            txt_context.text = item.introduce

            leader.setOnClickListener(View.OnClickListener {
                var intent = Intent(context, LeaderTransferActivity::class.java)
                intent.putExtra("study_idx", item.idx)
                context.startActivity(intent)
            })

            btn_modify.setOnClickListener(View.OnClickListener {
                var intent = Intent(context, EditStudyActivity::class.java)
                intent.putExtra("study_idx", item.idx)
                context.startActivity(intent)
            })

            btn_withdraw.setOnClickListener(View.OnClickListener {
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_group_delete, null)
                val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                val builder = mBuilder.show()

                builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val btn_cancle = mDialogView.findViewById<Button>(R.id.dialog_group_delete_btn_cancle)
                val btn_logout = mDialogView.findViewById<Button>(R.id.dialog_group_delete_btn_delete)

                btn_cancle.setOnClickListener(View.OnClickListener {
                    builder.dismiss()
                })

                btn_logout.setOnClickListener(View.OnClickListener {
                    viewModel.setDeleteStudyData(item.idx)
                    datas.remove(item)
                    builder.dismiss()
                })

            })

            var studyCategoryAdapter: StudyCategoryAdapter =
                StudyCategoryAdapter(context)
            studyCategoryAdapter.datas = item.category.split("|").toMutableList()
            rvCategory.adapter = studyCategoryAdapter
        }
    }

    inner class ViewHolderNotLeader(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = itemView.findViewById(R.id.item_study_menage_img)
        private val studyName: TextView = itemView.findViewById(R.id.item_study_menage_txt_name)
        private val txt_context: TextView = itemView.findViewById(R.id.item_study_menage_context)
        private val btn_memberList: Button = itemView.findViewById(R.id.item_study_menage_btn_member_list)
        private val btn_studyWithdraw: Button =
            itemView.findViewById(R.id.item_study_menage_btn_withdraw)
        private val rvCategory: RecyclerView =
            itemView.findViewById(R.id.item_study_menage_rv_category)

        fun bind(item: MyStudy) {
            Glide.with(context).load(item.img).into(img)

            studyName.text = item.name
            txt_context.text = item.introduce

            btn_memberList.setOnClickListener(View.OnClickListener {
                var intent = Intent(context, MemberListActivity::class.java)
                intent.putExtra("study_idx", item.idx)
                context.startActivity(intent)
            })

            btn_studyWithdraw.setOnClickListener(View.OnClickListener {
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_group_withdrawal, null)
                val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                val builder = mBuilder.show()

                builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val btn_cancle = mDialogView.findViewById<Button>(R.id.dialog_group_withdrawal_btn_cancle)
                val btn_logout = mDialogView.findViewById<Button>(R.id.dialog_group_withdrawal_btn_withdrawal)

                btn_cancle.setOnClickListener(View.OnClickListener {
                    builder.dismiss()
                })

                btn_logout.setOnClickListener(View.OnClickListener {
                    viewModel.setWithdrawStudyData(item.idx)
                    datas.remove(item)
                    builder.dismiss()
                })
            })

            var studyCategoryAdapter: StudyCategoryAdapter =
                StudyCategoryAdapter(context)
            studyCategoryAdapter.datas = item.category.split("|").toMutableList()
            rvCategory.adapter = studyCategoryAdapter
        }
    }
}