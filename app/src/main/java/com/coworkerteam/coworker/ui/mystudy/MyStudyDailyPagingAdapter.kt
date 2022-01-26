package com.coworkerteam.coworker.ui.mystudy

import android.app.Activity
import android.content.Context
import android.media.Image
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
import com.coworkerteam.coworker.data.model.api.MyStudyDailyPagingResponse
import com.coworkerteam.coworker.ui.dialog.PasswordDialog
import com.coworkerteam.coworker.ui.main.MainViewModel
import com.coworkerteam.coworker.ui.main.StudyCategoryAdapter
import com.coworkerteam.coworker.utils.ScreenSizeUtils

class MyStudyDailyPagingAdapter(private val dialog: PasswordDialog) :
    PagingDataAdapter<MyStudyDailyPagingResponse.Result.Open, MyStudyDailyPagingAdapter.ViewHolder>(differ) {
    lateinit var context: Context

    companion object {
        private val differ =
            object : DiffUtil.ItemCallback<MyStudyDailyPagingResponse.Result.Open>() {
                override fun areItemsTheSame(
                    oldItem: MyStudyDailyPagingResponse.Result.Open,
                    newItem: MyStudyDailyPagingResponse.Result.Open
                ): Boolean {
                    return false
                }

                override fun areContentsTheSame(
                    oldItem: MyStudyDailyPagingResponse.Result.Open,
                    newItem: MyStudyDailyPagingResponse.Result.Open
                ): Boolean {
                    return false
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_my_study, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        // 간격 설정
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = (ScreenSizeUtils().getScreenWidthSize(context as Activity)/2.5).toInt()
        holder.itemView.requestLayout()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val item_layout: ConstraintLayout = itemView.findViewById(R.id.item_my_study)
        private val img: ImageView = itemView.findViewById(R.id.item_my_study_img)
        private val leader: TextView = itemView.findViewById(R.id.item_my_study_leader)
        private val studyName: TextView = itemView.findViewById(R.id.item_my_study_txt_name)
        private val studyNum: TextView = itemView.findViewById(R.id.item_my_study_txt_num)
        private val password: ImageView = itemView.findViewById(R.id.txt_item_main_new_study_pw2)
        private val rvCategory: RecyclerView = itemView.findViewById(R.id.item_my_study_rv_category)

        fun bind(item: MyStudyDailyPagingResponse.Result.Open?) {
            Glide.with(context).load(item!!.img).into(img)

            if (!item.isLeader) {
                leader.visibility = View.GONE
            }

            studyName.text = item.name
            studyNum.text = "참여인원 " + item.userNum.toString() + "/" + item.maxNum.toString()

            if(item.isPw){
                password.visibility = View.VISIBLE
            }

            var studyCategoryAdapter: StudyCategoryAdapter = StudyCategoryAdapter(context)
            studyCategoryAdapter.datas = item.category.split("|").toMutableList()
            rvCategory.adapter = studyCategoryAdapter

            item_layout.setOnClickListener(View.OnClickListener {
                if (item.pw.equals("private")) {
                    //가입, 참여를 하지 않았던 비밀번호가 걸려있는 스터디를 선택했을 경우 비밀번호 입력 dialog가 노출
                    dialog.showDialog(context, item.idx)
                } else {
                    dialog.onClickOKButton(item.idx,null)
                }
            })
        }
    }
}