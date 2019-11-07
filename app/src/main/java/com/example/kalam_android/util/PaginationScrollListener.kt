package com.example.kalam_android.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class PaginationScrollListener
    (
    internal var layoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener() {
    private val TAG = this.javaClass.simpleName
//    private var mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE

    abstract val isLastPage: Boolean

    abstract val isLoading: Boolean

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        logE("isLoading $isLoading  islastPage $isLastPage")
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
    private fun logE(message: String) {
        Debugger.e(TAG, message)
    }
}
