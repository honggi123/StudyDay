package com.coworkerteam.coworker.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.DialogWhiteboardSketchChoiceBinding
import com.coworkerteam.coworker.ui.unity.WhiteBoardActivity
import com.coworkerteam.coworker.ui.unity.data.SketchURL

class SketchChoiceDialog (ctx: Context,dialogListener:DialogListener,sketchNum: Int) : Dialog(ctx){

    var mDialogView: View? = null
    var mBuilder: AlertDialog? = null
    lateinit var onClickOKButton: (Int, String?) -> Unit
    lateinit var bind : DialogWhiteboardSketchChoiceBinding
    var ctx : Context = context
    var imageURL : String = ""
    var page = 1        // 페이지
    var img_num = 0         // 페이지 내 몇번째 이미지를 클릭했는지

    var totalpage = 5
    var imgListURL = ArrayList<String>()
    var imgStrokeList = ArrayList<View>()
    var imgLayoutList = ArrayList<ImageView>()

    var selectimgURL = ""
    var dialogListener : DialogListener = dialogListener
    var sketchURL : SketchURL? = SketchURL()

    var sketchNum = sketchNum
    var selectedSketchPage :Int = 0

    var selectImgColor = context.resources.getColor(R.color.purple_color)
    var defaultImgColor = context.resources.getColor(R.color.light_gray_color)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(true)
        window?.setBackgroundDrawable(ColorDrawable())
        window?.setDimAmount(0.3f)
        bind = DialogWhiteboardSketchChoiceBinding.inflate(this.layoutInflater)
        bind.dialog = this

        imgStrokeList.add(bind.dialogWhiteboardClickstrokeOne)
        imgStrokeList.add(bind.dialogWhiteboardClickstrokeTwo)
        imgStrokeList.add(bind.dialogWhiteboardClickstrokeThree)

        imgLayoutList.add(bind.dialogWhiteboardImageOne)
        imgLayoutList.add(bind.dialogWhiteboardImageTwo)
        imgLayoutList.add(bind.dialogWhiteboardImageThree)

       selectedSketchPage = (sketchNum / 3)+1

        imageURL = ctx.getString(R.string.s3_sketch_url)
        setContentView(bind.root)

        hideStrokeImage()
        setPageImage()
        setImage()
        setArrowVisible()
        bind.whiteboardDialogPage.setText(page.toString()+"/"+totalpage.toString())

    }

    class Builder(context: Context, dialogListener: DialogListener,sketchNum:Int){
        private val dialog = SketchChoiceDialog(context,dialogListener,sketchNum)
        fun show(): SketchChoiceDialog {
            dialog.show()
            return dialog
        }
    }


    interface DialogListener {
        fun clickBtn(data: String?,sketchNum : Int)
    }

    fun nextPage(){
        if (page < totalpage){
            page++
            bind.whiteboardDialogPage.setText(page.toString()+"/"+totalpage.toString())
        }
        hideStrokeImage()
        setPageImage()
        setImage()
        setArrowVisible()
    }

    fun prevPage(){
        if (1<page){
            page--
            bind.whiteboardDialogPage.setText(page.toString()+"/"+totalpage.toString())
        }
        hideStrokeImage()
        setPageImage()
        setImage()
        setArrowVisible()
    }

    fun setArrowVisible(){
        if (page == 1){
            bind.dialogWhiteboardPrev.visibility = View.INVISIBLE
        }else if(page == totalpage){
            bind.dialogWhiteboardNext.visibility = View.INVISIBLE
        }else{
            bind.dialogWhiteboardPrev.visibility = View.VISIBLE
            bind.dialogWhiteboardNext.visibility = View.VISIBLE
        }
    }


    fun closeDialog(){
        dismiss()
    }

    fun setImage(){
        //setImageVisible()
        Log.d("page","page:"+page)

        for (i in 0..2){
            imgStrokeList.get(i).setBackgroundColor(defaultImgColor)
        }

        for (i in 0..imgListURL.size-1){
            imgLayoutList.get(i).visibility = View.VISIBLE
            imgStrokeList.get(i).visibility = View.VISIBLE
            Glide.with(context).load(imageURL+imgListURL.get(i)).into(imgLayoutList.get(i))
        }


        if(page == selectedSketchPage){
            var num = sketchNum % 3 + 1
            if (page ==1){
                num = sketchNum +1
            }
            when(num){
                1 -> {
                    bind.dialogWhiteboardClickstrokeOne.setBackgroundColor(selectImgColor)
                }
                2->{
                    bind.dialogWhiteboardClickstrokeTwo.setBackgroundColor(selectImgColor)
                }
                3->{
                    bind.dialogWhiteboardClickstrokeThree.setBackgroundColor(selectImgColor)
                }
            }
        }
    }

    fun setImageVisible(){
        bind.dialogWhiteboardImageOne.visibility = View.VISIBLE
        bind.dialogWhiteboardImageTwo.visibility = View.VISIBLE
        bind.dialogWhiteboardImageThree.visibility = View.VISIBLE
    }

    fun onClickImage(num : Int){
        selectedSketchPage = page
        sketchNum = ((page-1)*3)+num-1

       for (i in 0..2){
            imgStrokeList.get(i).setBackgroundColor(defaultImgColor)
       }

        when(num){
            1 -> {
                bind.dialogWhiteboardClickstrokeOne.setBackgroundColor(selectImgColor)
            }
            2->{
                bind.dialogWhiteboardClickstrokeTwo.setBackgroundColor(selectImgColor)
            }
            3->{
                bind.dialogWhiteboardClickstrokeThree.setBackgroundColor(selectImgColor)
            }
        }
        img_num = num
        selectimgURL = imgListURL.get(num-1)
    }

    fun hideStrokeImage(){
        for(i in 0..2){
            imgLayoutList.get(i).visibility = View.INVISIBLE
            imgStrokeList.get(i).visibility = View.INVISIBLE
        }
    }

    fun selectComplete(){
        if(!selectimgURL.equals("")){
            dialogListener.clickBtn(imageURL+selectimgURL,sketchNum)
        }else{
            dialogListener.clickBtn("",sketchNum)
        }
        dismiss()
    }


    fun setPageImage(){
        imgListURL.clear()
        when(page){
            1 -> {
                imgListURL.add("")
                imgListURL.add(sketchURL!!.imgListURL1)
                imgListURL.add(sketchURL!!.imgListURL2)
            }
            2-> {
                imgListURL.add(sketchURL!!.imgListURL3)
                imgListURL.add(sketchURL!!.imgListURL4)
                imgListURL.add(sketchURL!!.imgListURL5)
            }
            3-> {
                imgListURL.add(sketchURL!!.imgListURL6)
                imgListURL.add(sketchURL!!.imgListURL7)
                imgListURL.add(sketchURL!!.imgListURL8)

            }
            4->{
                imgListURL.add(sketchURL!!.imgListURL9)
                imgListURL.add(sketchURL!!.imgListURL10)
                imgListURL.add(sketchURL!!.imgListURL11)
            }
            5->{
                imgListURL.add(sketchURL!!.imgListURL12)
            }
        }
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        sketchURL = null
    }



}