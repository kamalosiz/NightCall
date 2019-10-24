package com.example.kalam_android.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ChatsFragmentBinding

class ChatsFragment : Fragment() {
    private lateinit var binding: ChatsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.chats_fragment, container, false
        )

        return binding.root
    }
}