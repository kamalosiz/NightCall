package com.example.kalam_android.helper


import androidx.annotation.NonNull
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.example.kalam_android.view.adapter.AdapterForMediaView
import com.example.kalam_android.view.adapter.AdapterSelectedMedia

 class EditItemTouchHelperCallback(val adapterSelectedMedia: AdapterSelectedMedia) :
    ItemTouchHelper.Callback() {


    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, swipeFlags)
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
         TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
     }


 }