package com.coworkerteam.coworker.ui.yourday

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.coworkerteam.coworker.ui.search.OpenStudySerarchFragment

class FragmentAdapter_YourDay (fm: FragmentManager):
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    //position 에 따라 원하는 Fragment로 이동시키기
    override fun getItem(position: Int): Fragment {
        Log.d("getItem",""+position)
        val fragment = when (position) {
            0 -> MoodPostFragment().newInstant()
            1 -> SuccessPostFragment().newInstant()
            else -> MoodPostFragment().newInstant()
        }
        return fragment
    }

    //tab의 개수만큼 return
    override fun getCount(): Int = 2

    //tab의 이름 fragment마다 바꾸게 하기
    override fun getPageTitle(position: Int): CharSequence? {
        val title = when (position) {
            0 -> "글 모아보기"
            1 -> "공부인증"
            else -> "글 모아보기"
        }
        return title
    }


}