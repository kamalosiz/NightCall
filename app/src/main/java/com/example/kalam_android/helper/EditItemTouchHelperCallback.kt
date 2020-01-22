package com.example.kalam_android.helper


import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.kalam_android.view.adapter.AdapterSelectedMedia

class EditItemTouchHelperCallback(private val adapterSelectedMedia: AdapterSelectedMedia) :
    ItemTouchHelper.Callback() {


    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        adapterSelectedMedia.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        adapterSelectedMedia.notifyItemChanged(viewHolder.adapterPosition)
        adapterSelectedMedia.notifyItemChanged(target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
    }


}