package com.coworkerteam.coworker.utils

import com.coworkerteam.coworker.data.model.other.PatternResult
import java.util.regex.Pattern

//EditText 입력값 유효성 검사하는 Util을 모아둔 클래스
// false를 반환하면 Error가 있다는 뜻으로 알맞은 양식이 아니거나, 잘못된 값을 포함하고 있다는 뜻이다.
object PatternUtils {

    /*메인화면 목표에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. 띄어쓰기만 있는 경우
     2. 30자가 초과했을 경우
     
     Null일때는 목표를 삭제한다는 의미이미로 Error 처리하면 안됨
    */
    fun matcheGoal(text: String): PatternResult {

        //띄어쓰기만 있는 경우
        if (text.isBlank() && text.isNotEmpty()) {
            return PatternResult(false, "공백문자만 입력할 수 없습니다.")
        }

        //한글, 숫자, 영어 대소문자, 특수문자 `~!@#$%^&*()_+[]{};':",./<>?|*-+. 외의 문자, 공백 외 입력시
        if (!text.matches(Regex("^[ㄱ-힣0-9a-zA-Z`~!@#\\$%^&*()_+\\[\\]{}; ':\",./<>?|*\\-+.]*$"))) {
            return PatternResult(false, "사용할 수 없는 문자가 포함되어 있습니다.")
        }

        //글자수 30자가 초과했을 경우
        if (text.length > 30) {
            return PatternResult(false, "목표는 30자를 초과할 수 없습니다.")
        }

        return PatternResult(true, null)
    }

    /*메인화면 디데이 이름에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. 띄어쓰기만 있는 경우
     2. 15자가 초과했을 경우

     Null일때는 목표를 삭제한다는 의미이미로 Error 처리하면 안됨
    */
    fun matcheDDay(text: String): PatternResult {

        //띄어쓰기만 있는 경우
        if (text.isBlank() && text.isNotEmpty()) {
            return PatternResult(false, "공백문자만 입력할 수 없습니다.")
        }

        //한글, 숫자, 영어 대소문자, 특수문자 `~!@#$%^&*()_+[]{};':",./<>?|*-+. 외의 문자, 공백 외 입력시
        if (!text.matches(Regex("^[ㄱ-힣0-9a-zA-Z`~!@#\\$%^&*()_+\\[\\]{}; ':\",./<>?|*\\-+.]*$"))) {
            return PatternResult(false, "사용할 수 없는 문자가 포함되어 있습니다.")
        }

        //글자수 15자가 초과했을 경우
        if (text.length > 15) {
            return PatternResult(false, "디데이 이름은 15자를 초과할 수 없습니다.")
        }

        return PatternResult(true, null)
    }

    /*스터디 이름에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. Null, 공백만 입력이 되어있을 경우(띄어쓰기만 있는 경우도 포함)
     2. 30자가 초과했을 경우
    */
    fun matcheStudyName(text: String): PatternResult {

        //Null, 공백만 입력이 되어있을 경우
        if (text.isNullOrBlank()) {
            return PatternResult(false, "미입력 또는 공백문자만 입력할 수 없습니다.")
        }

        //한글, 숫자, 영어 대소문자, 특수문자 `~!@#$%^&*()_+[]{};':",./<>?|*-+. 외의 문자, 공백 외 입력시
        if (!text.matches(Regex("^[ㄱ-힣0-9a-zA-Z`~!@#\\$%^&*()_+\\[\\]{}; ':\",./<>?|*\\-+.]*$"))) {
            return PatternResult(false, "사용할 수 없는 문자가 포함되어 있습니다.")
        }

        //글자수 30자가 초과했을 경우
        if (text.length > 30) {
            return PatternResult(false, "스터디 이름은 30자를 초과할 수 없습니다.")
        }

        return PatternResult(true, null)
    }

    /*스터디 비밀번호에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. Null, 공백만 입력이 되어있을 경우(띄어쓰기만 있는 경우도 포함)
     2. 영문 대소문자, 숫자, 특수문자 이외의 값이 포함되어 있을경우
     3. 8~16자 외일 경우

     비밀번호는 설정을 안할 수도 있음.
    */
    fun matcheStudyPassword(text: String): PatternResult {

        //Null, 공백만 입력이 되어있을 경우
        if (text.isNullOrBlank()) {
            return PatternResult(false, "미입력 또는 공백문자만 입력할 수 없습니다.")
        }

        //숫자, 영어 대소문자, 특수문자 `~!@#$%^&*()_+[]{};':",./<>?|*-+. 외의 문자, 공백 외 입력시
        if (!text.matches(Regex("^[0-9a-zA-Z`~!@#\\$%^&*()_+\\[\\]{}; ':\",./<>?|*\\-+.]*$"))) {
            return PatternResult(false, "사용할 수 없는 문자가 포함되어 있습니다.")
        }

        //8~16자 외일 경우
        if (text.length in 17..7) {
            return PatternResult(false, "스터디 비밀번호는 8~16자까지 가능합니다.")
        }

        return PatternResult(true, null)
    }

    /*스터디 인원에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. Null, 공백만 입력이 되어있을 경우(띄어쓰기만 있는 경우도 포함)
     2. 숫자 외의 값이 포함되어 있을 경우
     3. 입력값(숫자)가 1~16의 범위에서 벗어날 경우
    */
    fun matcheStudyNum(text: String): PatternResult {

        //Null, 공백만 입력이 되어있을 경우
        if (text.isNullOrBlank()) {
            return PatternResult(false, "필수입력 항목")
        }

        //숫자 외의 값이 포함되어 있을 경우
        if (!text.matches(Regex("^[0-9]*$"))) {
            return PatternResult(false, "숫자만 입력 가능")
        }

        //1~16명 외일 경우
        if (text.toInt() < 1 || text.toInt() > 16) {
            return PatternResult(false, "1~16명까지 가능")
        }

        return PatternResult(true, null)
    }

    /*스터디 설명에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. Null, 공백만 입력이 되어있을 경우(띄어쓰기만 있는 경우도 포함)
     2. 500자 초과한 경우
    */
    fun matcheDescript(text: String): PatternResult {

        //Null, 공백만 입력이 되어있을 경우
        if (text.isNullOrBlank()) {
            return PatternResult(false, "미입력 또는 공백문자만 입력할 수 없습니다.")
        }

        //한글, 숫자, 영어 대소문자, 특수문자 `~!@#$%^&*()_+[]{};':",./<>?|*-+. 외의 문자, 공백, 엔터 외 입력시
        if (!text.matches(Regex("^[ㄱ-힣0-9a-zA-Z`~!@#\\$%^&*()_+\\[\\]{}; \\n':\",./<>?|*\\-+.]*$"))) {
            return PatternResult(false, "사용할 수 없는 문자가 포함되어 있습니다.")
        }

        //글자수 500자를 초과했을 경우
        if (text.length > 500) {
            return PatternResult(false, "스터디 설명은 500자를 초과할 수 없습니다.")
        }

        return PatternResult(true, null)
    }

    /*투두리스트에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. Null, 공백만 입력이 되어있을 경우(띄어쓰기만 있는 경우도 포함)
     2. 50자 초과한 경우
    */
    fun matcheTodo(text: String): PatternResult {

        //Null, 공백만 입력이 되어있을 경우
        if (text.isNullOrBlank()) {
            return PatternResult(false, "미입력 또는 공백문자만 입력할 수 없습니다.")
        }

        //한글, 숫자, 영어 대소문자, 특수문자 `~!@#$%^&*()_+[]{};':",./<>?|*-+. 외의 문자, 공백 외 입력시
        if (!text.matches(Regex("^[ㄱ-힣0-9a-zA-Z`~!@#\\$%^&*()_+\\[\\]{}; ':\",./<>?|*\\-+.]*$"))) {
            return PatternResult(false, "사용할 수 없는 문자가 포함되어 있습니다.")
        }

        //글자수 50자를 초과했을 경우
        if (text.length > 50) {
            return PatternResult(false, "투두리스트는 50자를 초과할 수 없습니다.")
        }

        return PatternResult(true, null)
    }

    /*닉네임에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. Null, 공백만 입력이 되어있을 경우(띄어쓰기만 있는 경우도 포함)
     2. user+숫자 형식의 입력값일 경우
     3. 30자 이상일 경우
    */
    fun matcheNickName(text: String): PatternResult {

        //Null, 공백만 입력이 되어있을 경우
        if (text.isNullOrBlank()) {
            return PatternResult(false, "미입력 또는 공백문자만 입력할 수 없습니다.")
        }

        //한글, 숫자, 영어 대소문자, 특수문자 `~!@#$%^&*()_+[]{};':",./<>?|*-+. 외의 문자, 공백 외 입력시
        if (!text.matches(Regex("^[ㄱ-힣0-9a-zA-Z]*$"))) {
            return PatternResult(false, "한글, 영문 대소문자, 숫자만 입력 가능합니다.")
        }

        //user+숫자 형식의 입력값일 경우
        if (text.matches(Regex(""))) {
            return PatternResult(false, "user+숫자 형식의 닉네임은 사용할 수 없습니다.")
        }

        //글자수 30자가 초과했을 경우
        if (text.length > 30) {
            return PatternResult(false, "스터디 이름은 30자를 초과할 수 없습니다.")
        }

        return PatternResult(true, null)
    }

    /*회원탈퇴에 대한 유효성 검사
     허용되지 않는 입력값의 경우
     1. Null, 공백만 입력이 되어있을 경우(띄어쓰기만 있는 경우도 포함)
     2. 500자 초과한 경우
    */
    fun matcheWithdrawal(text: String): PatternResult {

        //Null, 공백만 입력이 되어있을 경우
        if (text.isNullOrBlank()) {
            return PatternResult(false, "미입력 또는 공백문자만 입력할 수 없습니다.")
        }

        //한글, 숫자, 영어 대소문자, 특수문자 `~!@#$%^&*()_+[]{};':",./<>?|*-+. 외의 문자, 공백, 엔터 외 입력시
        if (!text.matches(Regex("^[ㄱ-힣0-9a-zA-Z`~!@#\\$%^&*()_+\\[\\]{}; \\n':\",./<>?|*\\-+.]*$"))) {
            return PatternResult(false, "사용할 수 없는 문자가 포함되어 있습니다.")
        }

        //글자수 500자를 초과했을 경우
        if (text.length > 500) {
            return PatternResult(false, "회원탈퇴 사유는 500자를 초과할 수 없습니다.")
        }

        return PatternResult(true, null)
    }

    fun matcheEmoji(text: String): PatternResult {
        if (text.matches(Regex(".*[\\uD83C-\\uDBFF\\uDC00-\\uDFFF].*"))) {
            return PatternResult(false, "이모지는 사용할 수 없습니다.")
        }
        return PatternResult(true, null)
    }

    //이모지 공백으로 변환, 그러나 -도 변환되는 문제 존재
    fun replaceEmojiSearch(text: String): String {
        if (text.matches(Regex(".*[\\uD83C-\\uDBFF\\uDC00-\\uDFFF].*"))) {
            val emoji = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+");
            val emojiMatcher = emoji.matcher(text);
            val textNoEmoji = emojiMatcher.replaceAll("");
            return textNoEmoji
        }
        return text
    }
}