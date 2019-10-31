package com.example.kalam_android.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)

        clickListener()
    }

    private fun clickListener() {

        binding.tvChangeLanguage.setOnClickListener {

            popupMenu =
                PopupMenu(this, binding.tvChangeLanguage, View.TEXT_ALIGNMENT_CENTER)
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
