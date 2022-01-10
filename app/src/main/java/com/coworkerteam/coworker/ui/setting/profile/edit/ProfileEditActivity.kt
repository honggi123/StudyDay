package com.coworkerteam.coworker.ui.setting.profile.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.coworkerteam.coworker.data.model.api.ProfileManageResponse
import com.coworkerteam.coworker.databinding.ActivityProfileEditBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import de.hdodenhof.circleimageview.CircleImageView
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class ProfileEditActivity : BaseActivity<ActivityProfileEditBinding, ProfileEditViewModel>() {

    val TAG = "ProfileEditActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_profile_edit
    override val viewModel: ProfileEditViewModel by viewModel()

    lateinit var profileManageResponse: ProfileManageResponse.Result.Profile
    var category = ArrayList<String>()

    var realpath: String? = null
    var fileName: String? = null
    var nickname_check = ""
    var is_edit = false

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var uri = result.data!!.data
                profileManageResponse.img = uri.toString()
                is_edit = true

                if (uri != null) {
                    realpath = getPathFromUri(uri)
                    Log.d(TAG, realpath.toString())

                    fileName = getFileName(realpath!!)
                }
                Glide.with(this).load(uri)
                    .into(findViewById(R.id.my_profile_edit_img))

//                uploadWithTransferUtilty("testAndroid",File(uri.toString()))

            }
        }

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.my_profile_edit_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "프로필 수정"

        profileManageResponse =
            intent.getSerializableExtra("profile") as ProfileManageResponse.Result.Profile
        setLoginImage(viewDataBinding.myProfileLoginImg,profileManageResponse.loginType)

        init()
    }

    override fun initDataBinding() {
        viewModel.ProfileEditResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                finish()
            }
        })

        viewModel.NicknameCheckResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                is_edit = true

                val check_text = findViewById<TextView>(R.id.my_profile_edit_txt_nickname_check)
                check_text.visibility = View.VISIBLE
                if(it.body().toString() != null) {
                    check_text.text = it.body()!!.message
                    nickname_check = it.body()!!.isUse
                }else{
                    check_text.text = "중복된 닉네임입니다."
                    nickname_check = "false"
                }
            }
        })
    }

    override fun initAfterBinding() {
    }

    fun init() {
        val profile_img = findViewById<CircleImageView>(R.id.my_profile_edit_img)
        val profile_img_chang = findViewById<CircleImageView>(R.id.profile_edit_img_chang)
        val edt_nickname = findViewById<EditText>(R.id.my_profile_edit_nickname)
        val btn_nickname_check = findViewById<Button>(R.id.my_profile_edit_btn_nickname_check)
        val txt_email = findViewById<TextView>(R.id.my_profile_email)

        profile_img_chang.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE

            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            startForResult.launch(intent)
        })

        btn_nickname_check.setOnClickListener(View.OnClickListener {
            viewModel.getNicknameCheckData(edt_nickname.text.toString())
        })

        Glide.with(this) //해당 환경의 Context나 객체 입력
            .load(profileManageResponse.img) //URL, URI 등등 이미지를 받아올 경로
            .into(profile_img) //받아온 이미지를 받을 공간(ex. ImageView)

        edt_nickname.setText(profileManageResponse.nickname)
        txt_email.text = profileManageResponse.email
        var study = profileManageResponse

        val btn_category_test_study: TextView = findViewById(R.id.my_profile_edit_txt_test) //시험공부
        val btn_category_sat: TextView = findViewById(R.id.my_profile_edit_txt_sat)   //수능
        val btn_category_employment: TextView = findViewById(R.id.my_profile_edit_txt_emp) //취업
        val btn_category_language_study: TextView = findViewById(R.id.my_profile_edit_txt_laug) //어학
        val btn_category_certificate: TextView = findViewById(R.id.my_profile_edit_txt_cre)   //자격증
        val btn_category_official: TextView = findViewById(R.id.my_profile_edit_txt_off) //고시,공시
        val btn_category_Turnover: TextView = findViewById(R.id.my_profile_edit_txt_tran) //이직
        val btn_category_self_development: TextView =
            findViewById(R.id.my_profile_edit_txt_self) //자기개발
        val btn_category_other: TextView = findViewById(R.id.my_profile_edit_txt_other)   //기타

        if (study.category.contains("시험공부")) {
            btn_category_test_study.isSelected = true
            category.add("시험공부")
        }

        if (study.category.contains("수능")) {
            btn_category_sat.isSelected = true
            category.add("수능")
        }

        if (study.category.contains("취업")) {
            btn_category_employment.isSelected = true
            category.add("취업")
        }

        if (study.category.contains("어학")) {
            btn_category_language_study.isSelected = true
            category.add("어학")
        }

        if (study.category.contains("자격증")) {
            btn_category_certificate.isSelected = true
            category.add("자격증")
        }

        if (study.category.contains("고시/공시")) {
            btn_category_official.isSelected = true
            category.add("고시/공시")
        }

        if (study.category.contains("이직")) {
            btn_category_Turnover.isSelected = true
            category.add("이직")
        }

        if (study.category.contains("자기개발")) {
            btn_category_self_development.isSelected = true
            category.add("자기개발")
        }

        btn_category_test_study.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "시험공부")
        })

        btn_category_sat.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "수능")
        })

        btn_category_employment.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "취업")
        })

        btn_category_language_study.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "어학")
        })

        btn_category_certificate.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "자격증")
        })

        btn_category_official.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "고시/공시")
        })

        btn_category_Turnover.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "이직")
        })

        btn_category_self_development.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "자기계발")
        })

        btn_category_other.setOnClickListener(View.OnClickListener {
            categoryEvent(it, "기타")
        })
    }

    fun setLoginImage(v: View, loginType: String) {
        val imageView = v as CircleImageView

        if (loginType.equals("google")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.google_icon)
        } else if (loginType.equals("kakao")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.kakao_icon)
        } else if (loginType.equals("naver")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.naver_icon)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.profile_edit_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_modify_ok -> {
                if(is_edit){
                    if(nickname_check == "true" || nickname_check == ""){
                        if(fileName!=null) {
                            uploadWithTransferUtilty(fileName!!, File(realpath), this)
                        }else{
                            var nickname = findViewById<EditText>(R.id.my_profile_edit_nickname)
                            viewModel.setProfileEditData(nickname.text.toString(),category.joinToString("|"),profileManageResponse.img)
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "닉네임 중복 검사를 해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "변경된 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun categoryEvent(view: View, categoryName: String) {
        if (view.isSelected) {
            view.setSelected(false)
            category.remove(categoryName)
        } else {
            if (category.size >= 3) {
                category_much()
                return
            }
            view.setSelected(true)
            category.add(categoryName)
        }
        is_edit = true
    }

    fun category_much() {
        Toast.makeText(this, "카테고리는 최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
    }

    fun uploadWithTransferUtilty(fileName: String, file: File, content: Context) {
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
            "coworker-profile",
            fileName,
            file
        ) // (bucket api, file이름, file객체)


        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state === TransferState.COMPLETED) {
                    // Handle a completed upload
                    var nickname = findViewById<EditText>(R.id.my_profile_edit_nickname)
                    if(fileName != null){
                        profileManageResponse.img = getString(R.string.s3_coworker_study_url) + fileName
                    }
                    viewModel.setProfileEditData(nickname.text.toString(),category.joinToString("|"),profileManageResponse.img)
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