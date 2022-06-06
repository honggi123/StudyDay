package com.coworkerteam.coworker.data.model.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.ui.search.StudySearchActivity
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class StudySearchPagingSource(
    private val service: StudydayService,
    private val pref: PreferencesHelper,
    private val studyType: String,
) :
    PagingSource<Int, StudySearchResponse.Result.Study>() {
    val TAG = "StudySearchPagingSource"
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StudySearchResponse.Result.Study> {
        return try {
            val position = params.key ?: 1
            var response =
                service.studySerch(
                    pref.getAccessToken()!!,
                    "search",
                    StudySearchActivity.getCategory(),
                    studyType,
                    StudySearchActivity.isJoin,
                    StudySearchActivity.viewType,
                    StudySearchActivity.keword,
                    position
                ).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .blockingGet()

            val next = if (position >= response.body()!!.result.totalPage) null else position + 1

            LoadResult.Page(
                data = response.body()!!.result.study,
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

    override fun getRefreshKey(state: PagingState<Int, StudySearchResponse.Result.Study>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}
