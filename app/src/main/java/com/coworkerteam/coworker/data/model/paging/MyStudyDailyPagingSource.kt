package com.coworkerteam.coworker.data.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.model.api.MyStudyDailyPagingResponse
import com.coworkerteam.coworker.data.remote.StudydayService
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MyStudyDailyPagingSource(
    private val service: StudydayService,
    private val pref: PreferencesHelper
) :
    PagingSource<Int, MyStudyDailyPagingResponse.Result.Open>() {
    val TAG = "MyStudyDailyPagingSource"
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MyStudyDailyPagingResponse.Result.Open> {
        return try {
            val position = params.key ?: 1

            var response =
                service.myStudyDailyPaging(
                    pref.getAccessToken()!!,
                    pref.getCurrentUserName()!!,
                    position
                ).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .blockingGet()


            val next = if (position >= response.body()!!.result.totalPage) null else position + 1

            LoadResult.Page(
                data = response.body()!!.result.open,
                prevKey = if (position <= 1) null else position - 1,
                nextKey = next
            )

        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MyStudyDailyPagingResponse.Result.Open>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}
