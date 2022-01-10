package com.coworkerteam.coworker.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewUtils {
    fun setVerticalSpaceDecration(recyclerView: RecyclerView, space: Int) {
        val spaceDecoration = VerticalSpaceItemDecoration(space)
        recyclerView.addItemDecoration(spaceDecoration)
    }

    fun setHorizonSpaceDecration(recyclerView: RecyclerView, space: Int) {
        val spaceDecoration = HorizonSpaceItemDecoration(space)
        recyclerView.addItemDecoration(spaceDecoration)
    }

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }

    inner class HorizonSpaceItemDecoration(private val horizonSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = horizonSpaceHeight
            outRect.right = horizonSpaceHeight
        }
    }
}