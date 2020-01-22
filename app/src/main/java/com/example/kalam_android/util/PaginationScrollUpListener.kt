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
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition > 0) {
                Debugger.e("testing", "firstVisibleItemPosition : $firstVisibleItemPosition")
                Debugger.e("testing", "visibleItemCount : $visibleItemCount")
                Debugger.e("testing", "totalItemCount : $totalItemCount")
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
}
