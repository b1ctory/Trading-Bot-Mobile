package com.exception.tradingbot

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ETFItemDecoration: RecyclerView.ItemDecoration() {
    private val topMargin = 10

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = topMargin
    }
}