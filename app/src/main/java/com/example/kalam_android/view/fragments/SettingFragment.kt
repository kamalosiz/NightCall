package com.example.kalam_android.view.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil

import com.example.kalam_android.R
import com.example.kalam_android.databinding.FragmentSettingBinding
import com.example.kalam_android.view.activities.FindsFriendsActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var popupMenu: PopupMenu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListener()
    }

    private fun clickListener() {

        binding.tvFindFriends.setOnClickListener {

            startActivity(Intent(activity, FindsFriendsActivity::class.java))
        }

        binding.tvChangeLanguage.setOnClickListener {

            popupMenu =
                PopupMenu(activity, binding.tvChangeLanguage, View.TEXT_ALIGNMENT_CENTER)
            popupMenu.menuInflater.inflate(R.menu.menu_for_lng, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {

                    R.id.menuEnglish -> {

                        binding.tvChangeLanguage.text = it.title
                        popupMenu.dismiss()

                    }
                    R.id.menuArabic -> {

                        binding.tvChangeLanguage.text = it.title
                        popupMenu.dismiss()

                    }
                }
                true
            }
            popupMenu.show()
        }
    }


}
