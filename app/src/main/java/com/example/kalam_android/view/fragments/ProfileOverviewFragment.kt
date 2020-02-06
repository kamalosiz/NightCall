package com.example.kalam_android.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.kalam_android.R
import com.example.kalam_android.databinding.FragmentProfileOverviewBinding
import com.example.kalam_android.repository.model.ProfileData

private const val ARG_PARAM1 = "param1"

class ProfileOverviewFragment : Fragment() {
    private var list: ArrayList<ProfileData>? = null
    private lateinit var binding: FragmentProfileOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            list = it.getSerializable(ARG_PARAM1) as ArrayList<ProfileData>?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile_overview, container, false)
        setData()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {
        if (list != null && list?.size!! > 0) {
            binding.tvDescription.text = list?.get(0)?.bio
            binding.tvAddress.text = list?.get(0)?.address
            binding.tvWork.text = list?.get(0)?.work
            binding.tvPhone.text = list?.get(0)?.phone
            binding.tvEmail.text = list?.get(0)?.email
            binding.tvInterested.text = list?.get(0)?.intrests
            binding.tvStatus.text = list?.get(0)?.martial_status
            binding.tvEducation.text = list?.get(0)?.education
            initShowMore()
        }
    }

    private fun initShowMore() {

        binding.tvDescription.setShowingLine(2)
        binding.tvDescription.addShowMoreText("Show more")
        binding.tvDescription.addShowLessText("Show less")
        binding.tvDescription.setShowLessTextColor(
            ContextCompat.getColor(
                activity!!.applicationContext,
                R.color.theme_color
            )
        )
        binding.tvDescription.setShowMoreColor(
            ContextCompat.getColor(
                activity!!.applicationContext,
                R.color.theme_color
            )
        )

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: ArrayList<ProfileData>) =

            ProfileOverviewFragment().apply {
                this.arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }
}