package com.coworkerteam.coworker.ui.study.make

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isNotEmpty
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.StudyRequest
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.databinding.ActivityCategoryBinding
import com.coworkerteam.coworker.databinding.ActivityMakeStudyBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.camstudy.enter.EnterCamstudyActivity
import com.coworkerteam.coworker.ui.category.CategoryViewModel
import com.coworkerteam.coworker.utils.PatternUtils
import com.google.android.gms.common.api.ApiException
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class MakeStudyActivity : BaseActivity<ActivityMakeStudyBinding, MakeStudyViewModel>() {

    private val TAG = "MakeStudyActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_make_study
    override val viewModel: MakeStudyViewModel by viewModel()

    lateinit var mDialogView: View

    var realpath: String? = null
    var fileName: String? = null

    var imageUrl: String? = null
    var studyType: String? = null
    var isStudyName = false
    var isPassword = false
    var isStudyNum = false
    var categorys = ArrayList<String>()
    var isIntroduce = false

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var uri = result.data!!.data
                imageUrl = uri.toString()
                if (uri != null) {
                    realpath = getPathFromUri(uri)
                    Log.d(TAG, realpath.toString())

                    fileName = getFileName(realpath!!)
                }
                Glide.with(this).load(uri)
                    .into(mDialogView.findViewById(R.id.dialog_select_image_selet_image))
            }
        }

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.make_study_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "스터디 만들기"

        viewDataBinding.activity = this

    }

    override fun initDataBinding() {
        viewModel.MakeStudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //스터디 만들기 성공
            if (it.isSuccessful) {
                //오디오 세팅 페이지로 이동
                viewModel.getEnterCamstduyData(it.body()!!.result.studyIdx,it.body()!!.result.pw)
            } else{
                Log.d(TAG,it.errorBody()!!.string())
            }
        })

        viewModel.EnterCamstudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                var intent = Intent(this, EnterCamstudyActivity::class.java)
                intent.putExtra("studyInfo", it.body()!!)

                startActivity(intent)
                finish()
            } else if (it.code() == 403) {
                val errorMessage = JSONObject(it.errorBody()?.string())

                when(errorMessage.getInt("code")){
                    -12 -> {
                        //비밀번호를 틀린 경우
                        Log.d(TAG,"스터디 만든 후, 오디오/비디오 비밀번호 불일치로 세팅 페이지로 이동 실패")
                    }
                }
            } else{
                Log.d(TAG,it.errorBody()!!.string())
            }
        })
    }

    override fun initAfterBinding() {
    }


    fun checkStudyTypeRadio() {
        val checkedId = viewDataBinding.makeStudyType.checkedRadioButtonId

        if (checkedId == R.id.make_study_radio_open) {
            studyType = "open"
            Log.d(TAG, "open")
        } else if (checkedId == R.id.make_study_radio_group) {
            studyType = "group"
            Log.d(TAG, "group")
        } else {
            studyType = null
        }
    }

    fun clickCategoryButton(v: View) {
        val view = v as TextView
        //카테고리 이름
        val categoryName = view.text.toString()

        if (view.isSelected) {
            //선택되어있던 카테고리라면 선택해제
            view.setSelected(false)
            categorys.remove(categoryName)

        } else {
            //선택되지 않았던 카테고리라면 선택추가
            if (categorys.size >= 3) {
                //카테고리가 3개 이상 이미 선택되어 있다면
                Toast.makeText(this, "카테고리는 최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                //카테고리가 3개 이하라면
                view.setSelected(true)
                categorys.add(categoryName)
            }
        }
    }

    fun onCheckedChangedPassword(checked: Boolean) {
        if(checked){
            viewDataBinding.makeStudyEdtPw.visibility = View.VISIBLE
        }else{
            viewDataBinding.makeStudyEdtPw.visibility = View.GONE
        }
    }

    fun changTextStudyName(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheStudyName(s.toString())

        if (result.isNotError) {
            isStudyName = true
            viewDataBinding.makeStudyEdtName.isErrorEnabled = false
            viewDataBinding.makeStudyEdtName.error = null
        } else {
            isStudyName = false
            viewDataBinding.makeStudyEdtName.error = result.ErrorMessge
        }
    }

    fun changTextStudyPassword(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheStudyPassword(s.toString())

        if (result.isNotError) {
            isPassword = true
            viewDataBinding.makeStudyEdtPw.isErrorEnabled = false
            viewDataBinding.makeStudyEdtPw.error = null
        } else {
            isPassword = false
            viewDataBinding.makeStudyEdtPw.error = result.ErrorMessge
        }
    }

    fun changTextStudyNum(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheStudyNum(s.toString())

        if (result.isNotError) {
            isStudyNum = true
            viewDataBinding.makeStudyEdtNum.isErrorEnabled = false
            viewDataBinding.makeStudyEdtNum.error = null
        } else {
            isStudyNum = false
            viewDataBinding.makeStudyEdtNum.error = result.ErrorMessge
        }
    }

    fun changTextIntroduce(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheDescript(s.toString())

        if (result.isNotError) {
            isIntroduce = true
            viewDataBinding.makeStudyEdtIntroduce.isErrorEnabled = false
            viewDataBinding.makeStudyEdtIntroduce.error = null
        } else {
            isIntroduce = false
            viewDataBinding.makeStudyEdtIntroduce.error = result.ErrorMessge
        }
    }

    fun showImageDialog() {
        val view = viewDataBinding.makeStudyImg
        val baseImages: List<String> = listOf(
            "https://coworker-study.s3.ap-northeast-2.amazonaws.com/basicImage1.jpg",
            "https://coworker-study.s3.ap-northeast-2.amazonaws.com/basicImage2.jpg",
            "https://coworker-study.s3.ap-northeast-2.amazonaws.com/basicImage3.jpg"
        )

        mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_image, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        val builder = mBuilder.show()
        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val finish = mDialogView.findViewById<ImageView>(R.id.dialog_select_image_finish)
        val btn_import =
            mDialogView.findViewById<Button>(R.id.dialog_select_image_btn_image_Import)
        val btn_pick = mDialogView.findViewById<Button>(R.id.dialog_select_image_btn_pick_ok)

        val selectImage =
            mDialogView.findViewById<ImageView>(R.id.dialog_select_image_selet_image)

        val baseImage1 = mDialogView.findViewById<ImageView>(R.id.dialog_select_image_basic_one)
        val baseImage2 = mDialogView.findViewById<ImageView>(R.id.dialog_select_image_basic_two)
        val baseImage3 =
            mDialogView.findViewById<ImageView>(R.id.dialog_select_image_basic_three)

        Glide.with(this).load(baseImages[0]).into(baseImage1)
        Glide.with(this).load(baseImages[1]).into(baseImage2)
        Glide.with(this).load(baseImages[2]).into(baseImage3)

        baseImage1.setOnClickListener(View.OnClickListener {
            Glide.with(this).load(baseImages[0]).into(selectImage)
            imageUrl = baseImages[0]
        })

        baseImage2.setOnClickListener(View.OnClickListener {
            Glide.with(this).load(baseImages[1]).into(selectImage)
            imageUrl = baseImages[1]
        })

        baseImage3.setOnClickListener(View.OnClickListener {
            Glide.with(this).load(baseImages[2]).into(selectImage)
            imageUrl = baseImages[2]
        })

        btn_import.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE

            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            startForResult.launch(intent)
        })

        btn_pick.setOnClickListener(View.OnClickListener {
            Glide.with(this).load(imageUrl).into(view)
            builder.dismiss()
        })

        finish.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })
    }

    fun makeStudy() {
        if (imageUrl == null) {
            Toast.makeText(this, "이미지를 설정해주세요.", Toast.LENGTH_SHORT).show()
        } else if (studyType == null) {
            Toast.makeText(this, "스터디 종류를 선택해주세요.", Toast.LENGTH_SHORT).show()
        } else if (!isStudyName) {
            Toast.makeText(this, "스터디 이름을 확인해주세요.", Toast.LENGTH_SHORT).show()
        } else if (viewDataBinding.makeStudyCheckPw.isChecked && !isPassword) {
            Toast.makeText(this, "스터디 비밀번호을 확인해주세요.", Toast.LENGTH_SHORT).show()
        } else if (!isStudyNum) {
            Toast.makeText(this, "스터디 인원을 확인해주세요.", Toast.LENGTH_SHORT).show()
        } else if (categorys.size == 0) {
            Toast.makeText(this, "스터디 카테고리를 확인해주세요.", Toast.LENGTH_SHORT).show()
        } else if (!isIntroduce) {
            Toast.makeText(this, "스터디 설명을 확인해주세요.", Toast.LENGTH_SHORT).show()
        } else {

            if (fileName != null) {
                uploadWithTransferUtilty(fileName!!, File(realpath))
                imageUrl = getString(R.string.s3_coworker_study_url) + fileName
            }


            var categorys = categorys.joinToString("|")
            var password =
                if (viewDataBinding.makeStudyCheckPw.isChecked) viewDataBinding.makeStudyEdtPw.editText?.text.toString() else null

            viewModel.setMakeStudyData(
                studyType!!,
                viewDataBinding.makeStudyEdtName.editText?.text.toString(),
                categorys,
                imageUrl!!,
                password,
                viewDataBinding.makeStudyEdtNum.editText?.text.toString().toInt(),
                viewDataBinding.makeStudyEdtIntroduce.editText?.text.toString()
            )
        }
    }

    fun uploadWithTransferUtilty(fileName: String, file: File) {
        val awsCredentials: AWSCredentials =
            BasicAWSCredentials(
                getString(R.string.s3_accesskey_id),
                getString(R.string.s3_accesskey_secret)
            ) // IAM 생성하며 받은 것 입력

        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))

        val transferUtility = TransferUtility.builder().s3Client(s3Client)
            .context(getApplicationContext()).build()
        TransferNetworkLossHandler.getInstance(getApplicationContext())

        val uploadObserver = transferUtility.upload(
            "coworker-study",
            fileName,
            file
        ) // (bucket api, file이름, file객체)


        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state === TransferState.COMPLETED) {
                    // Handle a completed upload
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (current.toDouble() / total * 100.0).toInt()
                Log.d("MYTAG", "UPLOAD - - ID: \$id, percent done = \$done")
            }

            override fun onError(id: Int, ex: Exception) {
                Log.d("MYTAG", "UPLOAD ERROR - - ID: \$id - - EX:$ex")
            }
        })
    }

    // 이미지 uri를 절대 경로로 바꾸고 이미지 gps return
    // 절대경로 변환
    open fun getPathFromUri(uri: Uri?): String? {
        val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToNext()
        val path: String = cursor.getString(cursor.getColumnIndex("_data"))
        cursor.close()
        return path
    }

    fun getFileName(path: String): String {
        val timestamp = System.currentTimeMillis().toString()

        var extension = path.lastIndexOf(".")

        var result = timestamp + path.substring(extension, path.length)

        return result
    }

}