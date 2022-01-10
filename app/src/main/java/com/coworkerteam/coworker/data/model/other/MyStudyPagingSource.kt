package com.coworkerteam.coworker.data.model.other

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.model.api.MyStudyGroupPagingResponse
import com.coworkerteam.coworker.data.remote.StudydayService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MyStudyPagingSource(
    private val service: StudydayService,
    private val pref: PreferencesHelper
) :
    PagingSource<Int, MyStudyGroupPagingResponse.Result.Group>() {
    val TAG = "MyStudyPagingSource"
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MyStudyGroupPagingResponse.Result.Group> {
        return try {
            Log.d("잠시 검문이 있겠습니다.", "^^")
            val position = params.key ?: 1

            var response = withContext(Dispatchers.IO) {
                service.myStudyGroupPaging(
                    pref.getAccessToken()!!,
                    pref.getCurrentUserName()!!,
                    position
                ).execute()
            }
            Log.d("잠시 검문이 있겠습니다.123", pref.getAccessToken()!!)
//            service.myStudyGroupPaging(
//                pref.getAccessToken()!!,
//                pref.getCurrentUserName()!!,
//                position
//            ).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    it.run {
//                        response = this
//                        Log.d(TAG, "meta : " + it.toString())
//                    }
//                }, {
//                    Log.d(TAG, "response error, message : ${it.message}")
//                })

            val next = if (position == response.body()!!.result.totalPage) null else position + 1
            Log.d("디버그태그",next.toString())
//            val next = position + 1

            LoadResult.Page(
                data = response.body()!!.result.group,
                prevKey = if (position == 1) null else position - 1,
                nextKey = null
            )

        } catch (e: IOException) {
            Log.d("잠시 검문이 있겠습니다.", "^^IOException")
            LoadResult.Error(e)
        } catch (e: HttpException) {
            Log.d("잠시 검문이 있겠습니다.", "^^HttpException")
            LoadResult.Error(e)
        } catch (e: Exception) {
            Log.d("잠시 검문이 있겠습니다.", "^^Exception" + e.toString())
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MyStudyGroupPagingResponse.Result.Group>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}
