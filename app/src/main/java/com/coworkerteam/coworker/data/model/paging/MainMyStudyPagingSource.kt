package com.coworkerteam.coworker.data.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.model.api.MainMyStudyPagingResponse
import com.coworkerteam.coworker.data.remote.StudydayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MainMyStudyPagingSource(
    private val service: StudydayService,
    private val pref: PreferencesHelper
) :
    PagingSource<Int, MainMyStudyPagingResponse.Result.MyStudy>() {
    val TAG = "MainMyStudyPagingSource"
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MainMyStudyPagingResponse.Result.MyStudy> {
        return try {
            val position = params.key ?: 1

            var response = withContext(Dispatchers.IO) {
                service.mainMyStudyPaging(
                    pref.getAccessToken()!!,
                    pref.getCurrentUserName()!!,
                    position
                ).execute()
            }

            Log.d("액세스토큰 : ",pref.getAccessToken().toString())

            val next = if (position >= response.body()!!.result.totalPage) null else position + 1
            Log.d("디버그태그",next.toString())

            LoadResult.Page(
                data = response.body()!!.result.myStudy,
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

    override fun getRefreshKey(state: PagingState<Int, MainMyStudyPagingResponse.Result.MyStudy>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}
