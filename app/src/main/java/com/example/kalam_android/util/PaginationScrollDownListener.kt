package com.example.kalam_android.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationScrollDownListener(
        private var layoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener() {

    abstract val isFirstPage: Boolean

    abstract val isLoading: Boolean

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (!isLoading && !isFirstPage) {
            if (firstVisibleItemPosition == 0 && totalItemCount >= visibleItemCount) {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
}
