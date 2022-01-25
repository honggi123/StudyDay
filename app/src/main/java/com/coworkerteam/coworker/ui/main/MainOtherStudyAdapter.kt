package com.coworkerteam.coworker.ui.main

import android.app.Activity
import android.util.Log

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.data.local.service.CamStudyService
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.ErrorResponse
import com.coworkerteam.coworker.data.model.api.MainResponse
import com.coworkerteam.coworker.utils.ScreenSizeUtils
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainOtherStudyAdapter(private val context: Context, private val viewModel: MainViewModel) :
    RecyclerView.Adapter<MainOtherStudyAdapter.ViewHolder>() {

    var datas = mutableListOf<MainResponse.Result.Study>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_main_other_study, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])

        // 간격 설정
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width =
            (ScreenSizeUtils().getScreenWidthSize(context as Activity) / 2.25).toInt()
        holder.itemView.requestLayout()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val item_layout: ConstraintLayout = itemView.findViewById(R.id.item_main_new_study)
        private val img: ImageView = itemView.findViewById(R.id.item_my_study_img)
        private val leader: TextView = itemView.findViewById(R.id.item_my_study_leader)
        private val studyName: TextView = itemView.findViewById(R.id.item_my_study_txt_name)
        private val studyNum: TextView = itemView.findViewById(R.id.item_my_study_txt_num)
        private val pw: ImageView = itemView.findViewById(R.id.txt_item_main_new_study_pw)
        private val rvCategory: RecyclerView =
            itemView.findViewById(R.id.item_my_study_rv_category)

        fun bind(item: MainResponse.Result.Study) {
            Glide.with(context).load(item.img).into(img)

            if (!item.isLeader) {
                leader.visibility = View.GONE
            }

            studyName.text = item.name
            studyNum.text = "참여인원 " + item.userNum.toString() + "/" + item.maxNum.toString()

            if (!item.isPw) {
                pw.visibility = View.GONE
            }

            var studyCategoryAdapter: StudyCategoryAdapter =
                StudyCategoryAdapter(context)
            studyCategoryAdapter.datas = item.category.split("|").toMutableList()
            rvCategory.adapter = studyCategoryAdapter

            item_layout.setOnClickListener(View.OnClickListener {
                if (item.pw.equals("private")) {
                    //가입, 참여를 하지 않았던 비밀번호가 걸려있는 스터디를 선택했을 경우 비밀번호 입력 dialog가 노출
                    val mDialogView = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_camstudy_password, null)
                    val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                    val builder = mBuilder.show()

                    builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val btn_cancle =
                        mDialogView.findViewById<Button>(R.id.dialog_camstudy_password_btn_cancle)
                    val btn_ok =
                        mDialogView.findViewById<Button>(R.id.dialog_camstudy_password_btn_ok)

                    btn_cancle.setOnClickListener(View.OnClickListener {
                        builder.dismiss()
                    })

                    btn_ok.setOnClickListener(View.OnClickListener {
                        var password =
                            mDialogView.findViewById<TextInputLayout>(R.id.edit_dialog_study_password)
                        viewModel.getEnterCamstduyData(item.idx, password.editText?.text.toString())
                    })

                } else {

                    CamStudyService.isLeader = item.isLeader
                    viewModel.getEnterCamstduyData(item.idx, null)

                }
            })
        }
    }
}