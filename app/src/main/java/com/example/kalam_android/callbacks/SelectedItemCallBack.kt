package com.example.kalam_android.callbacks

import com.example.kalam_android.repository.model.MediaList
import java.util.ArrayList

interface SelectedItemCallBack {
    fun selectedItem(list: ArrayList<MediaList>, position: Int)
}