package com.example.kalam_android.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.databinding.StatusBottomSheetFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StatusBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: StatusBottomSheetFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.status_bottom_sheet_fragment,
            container,
            false
        )

        return binding.root
    }
}