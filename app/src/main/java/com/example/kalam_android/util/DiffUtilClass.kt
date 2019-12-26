import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.example.kalam_android.localdb.entities.ChatListData

/*
class DiffUtilClass(
    private val mOldEmployeeList: List<ChatListData>,
    private val mNewEmployeeList: List<ChatListData>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return mOldEmployeeList.size
    }

    override fun getNewListSize(): Int {
        return mNewEmployeeList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldEmployeeList[oldItemPosition].chat_id == mNewEmployeeList[newItemPosition].chat_id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEmployee = mOldEmployeeList[oldItemPosition]
        val newEmployee = mNewEmployeeList[newItemPosition]
        return oldEmployee == newEmployee
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}*/
