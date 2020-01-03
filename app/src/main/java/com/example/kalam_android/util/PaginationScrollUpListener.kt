package com.example.kalam_android.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class PaginationScrollUpListener(
    private var layoutManager: LinearLayoutManager

) : RecyclerView.OnScrollListener() {

    abstract val isLastPage: Boolean

    abstract val isLoading: Boolean

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        if (!isLoading && !isLastPage) {
            if (lastVisibleItemPosition + 1 == totalItemCount && totalItemCount >= visibleItemCount) {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
}
