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
import com.coworkerteam.coworker.utils.PatternUtils
import com.google.android.material.textfield.TextInputLayout
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class ProfileEditActivity : BaseActivity<ActivityProfileEditBinding, ProfileEditViewModel>() {

    val TAG = "ProfileEditActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_profile_edit
    override val viewModel: ProfileEditViewModel by viewModel()

    lateinit var profileManageResponse: ProfileManageResponse.Result.Profile
    var categorys = ArrayList<String>()

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
            }
        }

    override fun initStartView() {
        setSupportActionBar(viewDataBinding.myProfileEditToolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "프로필 수정"

        profileManageResponse =
            intent.getSerializableExtra("profile") as ProfileManageResponse.Result.Profile
        setLoginImage(viewDataBinding.myProfileLoginImg,profileManageResponse.loginType)

        viewDataBinding.activitiy = this

        init()
    }

    override fun initDataBinding() {
        viewModel.ProfileEditResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    finish()
                }
                it.code() == 400 -> {
                    //API 요청값을 제대로 다 전달하지 않은 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 스터디 수정이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"입력을 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 403 -> {
                    //이미 참여중인 스터디가 있을 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //403번대 에러로 스터디 수정이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"현재 공부중인 스터디가 존재해서 변경할 수 없습니다. 나중에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })

        viewModel.NicknameCheckResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    is_edit = true

                    //사용할 수 있는 닉네임 이므로 EditText가 Error처리 되어있었다면 해제
                    viewDataBinding.myProfileEditNickname.error = null
                    viewDataBinding.myProfileEditNickname.isErrorEnabled = false

                    //닉네임 사용 가능하다는 설명 셋팅
                    viewDataBinding.myProfileEditNickname.helperText = it.body()!!.message

                    //닉네임 사용 가능하다는 판별 전역변수에 값 수정
                    nickname_check = it.body()!!.isUse

                }
                it.code() == 400 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -1 -> {
                            //API 요청값을 제대로 다 전달하지 않은 경우
                            Toast.makeText(this,"입력을 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                        }
                        -10 -> {
                            //닉네임이 중복되어 사용할 수 없는 경우
                            viewDataBinding.myProfileEditNickname.error = errorMessage.getString("message")
                            nickname_check = errorMessage.getString("isUse")
                        }
                    }

                }
                it.code() == 403 -> {
                    //이미 참여중인 스터디가 있을 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //403번대 에러로 스터디 수정이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"현재 공부중인 스터디가 존재해서 변경할 수 없습니다. 나중에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })
    }

    override fun initAfterBinding() {
    }

    fun init() {
        
        //내 기존 프로필과 닉네임, 이메일 등의 값 세팅
        Glide.with(this)
            .load(profileManageResponse.img)
            .into(viewDataBinding.myProfileEditImg)

        viewDataBinding.myProfileEditNickname.editText?.setText(profileManageResponse.nickname)
        viewDataBinding.myProfileEmail.text = profileManageResponse.email
        var study = profileManageResponse

        
        //내가 이미 설정해놓았던 카테고리 UI 반영 및, 선택관련 ArrayList에 값 추가
        if (study.category.contains("시험공부")) {
            viewDataBinding.myProfileEditTxtTest.isSelected = true
            categorys.add("시험공부")
        }

        if (study.category.contains("수능")) {
            viewDataBinding.myProfileEditTxtSat.isSelected = true
            categorys.add("수능")
        }

        if (study.category.contains("취업")) {
            viewDataBinding.myProfileEditTxtEmp.isSelected = true
            categorys.add("취업")
        }

        if (study.category.contains("어학")) {
            viewDataBinding.myProfileEditTxtLaug.isSelected = true
            categorys.add("어학")
        }

        if (study.category.contains("자격증")) {
            viewDataBinding.myProfileEditTxtCre.isSelected = true
            categorys.add("자격증")
        }

        if (study.category.contains("고시/공시")) {
            viewDataBinding.myProfileEditTxtOff.isSelected = true
            categorys.add("고시/공시")
        }

        if (study.category.contains("이직")) {
            viewDataBinding.myProfileEditTxtTran.isSelected = true
            categorys.add("이직")
        }

        if (study.category.contains("자기개발")) {
            viewDataBinding.myProfileEditTxtSelf.isSelected = true
            categorys.add("자기개발")
        }

        if (study.category.contains("기타")) {
            viewDataBinding.myProfileEditTxtOther.isSelected = true
            categorys.add("기타")
        }

    }

    fun changTextNickname(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheNickName(s.toString(),viewModel.getNickName())
        Log.d(TAG,s.toString())

        if (result.isNotError) {
            viewDataBinding.myProfileEditNickname.isErrorEnabled = false
            viewDataBinding.myProfileEditNickname.error = null
            viewDataBinding.myProfileEditBtnNicknameCheck.isEnabled = true
        } else {
            viewDataBinding.myProfileEditNickname.error = result.ErrorMessge
            viewDataBinding.myProfileEditBtnNicknameCheck.isEnabled = false
        }
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
                when {
                    is_edit -> {
                        when (nickname_check) {
                            "true", "" -> {
                                if(fileName!=null) {
                                    uploadWithTransferUtilty(fileName!!, File(realpath), this)
                                }else{
                                    viewModel.setProfileEditData(viewDataBinding.myProfileEditNickname.editText?.text.toString(),categorys.joinToString("|"),profileManageResponse.img)
                                }
                            }
                            else -> {
                                Toast.makeText(this, "닉네임 중복 검사를 해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> {
                        Toast.makeText(this, "변경된 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun clickCategoryButton(v: View) {
        //카테고리 관련 클릭 이벤트
        
        val view = v as TextView
        //카테고리 이름
        val categoryName = view.text.toString()

        if (view.isSelected) {
            //선택되어있던 카테고리라면 선택해제
            view.setSelected(false)
            categorys.remove(categoryName)

            //카테고리가 변경되었다는 확인용 전역변수 값 설정
            is_edit = true

        } else {
            //선택되지 않았던 카테고리라면 선택추가
            if (categorys.size >= 3) {
                //카테고리가 3개 이상 이미 선택되어 있다면
                Toast.makeText(this, "카테고리는 최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                //카테고리가 3개 이하라면
                view.setSelected(true)
                categorys.add(categoryName)

                //카테고리가 변경되었다는 확인용 전역변수 값 설정
                is_edit = true
            }
        }
    }

    fun clickNicknameCheck(){
        //닉네임 중복 체크
        viewModel.getNicknameCheckData(viewDataBinding.myProfileEditNickname.editText?.text.toString())
    }

    fun startImagePick(){
        //프로필 사진 이미지 선택하러 갤러리로 보내기
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startForResult.launch(intent)
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
                    if(fileName != null){
                        profileManageResponse.img = getString(R.string.s3_coworker_study_url) + fileName
                    }
                    viewModel.setProfileEditData(viewDataBinding.myProfileEditNickname.editText?.text.toString(),categorys.joinToString("|"),profileManageResponse.img)
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