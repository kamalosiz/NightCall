package com.example.kalam_android.view.fragments

import android.annotation.SuppressLint
import android.app.Activity.RECEIVER_VISIBLE_TO_INSTANT_APPS
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ProfileFragmentBinding
import com.example.kalam_android.repository.model.ProfileData
import com.example.kalam_android.repository.model.UserProfile
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.activities.EditUserProfileActivity
import com.example.kalam_android.view.adapter.AdapterForProfilePhotos
import com.example.kalam_android.view.adapter.AdapterForProfileVideos
import com.example.kalam_android.viewmodel.UserProfileViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.GlideDownloader
import kotlinx.android.synthetic.main.item_for_add_my_status.view.*
import kotlinx.android.synthetic.main.layout_for_user_profile_overview.view.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.layout_profile_header.view.tvName
import javax.inject.Inject

class ProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: ProfileFragmentBinding

    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: UserProfileViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var list: ArrayList<ProfileData>? = null
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.profile_fragment, container, false
        )
        MyApplication.getAppComponent(activity!!).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(UserProfileViewModel::class.java)
        viewModel.userProfileResponse().observe(this, Observer {
            consumeApiResponse(it)
        })

        onclickListener()
        initProfileVideosRecyclerView()
        initProfileImagesRecyclerView()
        binding.profileHeaderView.tvOverview.text = HtmlCompat.fromHtml(
                "<u>${getString(R.string.labelOverview)}</u>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.profileHeaderView.tvPhotos.text = HtmlCompat.fromHtml(
                "<u>${getString(R.string.labelPhotos)}</u>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.profileHeaderView.tvVideos.text = HtmlCompat.fromHtml(
                "<u>${getString(R.string.labelVideos)}</u>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.rvUserProfileVideos.isFocusable = false
        binding.rvUserProfilePhotos.isFocusable = false
        binding.nestedScroll.isFocusable = false

        val params = HashMap<String, String>()
        params["user_id"] = sharedPrefsHelper.getUser()?.id.toString()
        viewModel.hitUserProfile(sharedPrefsHelper.getUser()?.token!!, params)
        return binding.root
    }

    private fun consumeApiResponse(response: ApiResponse<UserProfile>) {
        when (response.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                Debugger.e("consumeApiResponse : ", "${response.data}")
                renderResponse(response.data?.data!!)

            }
            Status.ERROR -> {
                Debugger.e("data : ", "${response.error}")

            }
            else -> {

            }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun renderResponse(userList: ArrayList<ProfileData>) {
        list = userList
        binding.profileHeaderView.tvName.text = userList[0].firstname + " " + userList[0].lastname
        GlideDownloader.load(
                activity?.applicationContext,
                binding.profileHeaderView.ivProfile,
                userList[0].profile_image,
                R.color.grey,
                R.color.grey
        )
        GlideDownloader.load(
                activity?.applicationContext,
                binding.profileHeaderView.ivUserProfile,
                userList[0].wall_image,
                R.color.darkGrey,
                R.color.darkGrey
        )
        binding.overviewView.tvEmail.text = userList[0].email
        binding.overviewView.tvPhone.text = "+" + userList[0].country_code + userList[0].phone
        binding.overviewView.tvAddress.text = userList[0].address
        binding.profileHeaderView.tvLocation.text = userList[0].city + " " + userList[0].country
        binding.overviewView.tvEducation.text = userList[0].education
        binding.overviewView.tvFax.text = userList[0].fax
        binding.overviewView.tvDescription.text = userList[0].bio
        initShowMore()
        binding.overviewView.tvWork.text = userList[0].work
        binding.overviewView.tvWebsite.text = userList[0].website
        binding.overviewView.tvMartialStatus.text = userList[0].martial_status
        binding.overviewView.tvInterested.text = userList[0].intrests

    }

    private fun initProfileVideosRecyclerView() {

        binding.rvUserProfileVideos.layoutManager = GridLayoutManager(activity, 4)
        binding.rvUserProfileVideos.adapter = AdapterForProfileVideos()
    }

    private fun initProfileImagesRecyclerView() {

        binding.rvUserProfilePhotos.layoutManager = GridLayoutManager(activity, 4)
        binding.rvUserProfilePhotos.adapter = AdapterForProfilePhotos()
    }

    private fun initShowMore() {
        binding.overviewView.tvDescription.setShowingLine(3)
        binding.overviewView.tvDescription.addShowMoreText("Show more")
        binding.overviewView.tvDescription.addShowLessText("Show less")
        binding.overviewView.tvDescription.setShowLessTextColor(
                ContextCompat.getColor(
                        activity!!.applicationContext,
                        R.color.theme_color
                )
        )
        binding.overviewView.tvDescription.setShowMoreColor(
                ContextCompat.getColor(
                        activity!!.applicationContext,
                        R.color.theme_color
                )
        )

    }

    private fun onclickListener() {

        binding.profileHeaderView.tvOverview.setTextColor(
                ContextCompat.getColor(
                        activity!!.applicationContext,
                        R.color.theme_color
                )
        )
        binding.profileHeaderView.tvOverview.setOnClickListener(this)
        binding.profileHeaderView.tvPhotos.setOnClickListener(this)
        binding.profileHeaderView.tvVideos.setOnClickListener(this)
        binding.btnProfileEdit.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvOverview -> {
                binding.profileHeaderView.tvOverview.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.theme_color
                        )
                )
                binding.profileHeaderView.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.black
                        )
                )
                binding.profileHeaderView.tvVideos.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.black
                        )
                )
                binding.overviewView.visibility = View.VISIBLE
                binding.rvUserProfilePhotos.visibility = View.GONE
                binding.rvUserProfileVideos.visibility = View.GONE


            }
            R.id.tvPhotos -> {
                binding.profileHeaderView.tvOverview.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.black
                        )
                )
                binding.profileHeaderView.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.theme_color
                        )
                )
                binding.profileHeaderView.tvVideos.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.black
                        )
                )
                binding.overviewView.visibility = View.GONE
                binding.rvUserProfilePhotos.visibility = View.VISIBLE
                binding.rvUserProfileVideos.visibility = View.GONE

            }
            R.id.tvVideos -> {
                binding.profileHeaderView.tvOverview.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.black
                        )
                )
                binding.profileHeaderView.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.black
                        )
                )
                binding.profileHeaderView.tvVideos.setTextColor(
                        ContextCompat.getColor(
                                activity!!.applicationContext,
                                R.color.theme_color
                        )
                )
                binding.overviewView.visibility = View.GONE
                binding.rvUserProfilePhotos.visibility = View.GONE
                binding.rvUserProfileVideos.visibility = View.VISIBLE

            }
            R.id.btnProfileEdit -> {
                val intent = Intent(activity, EditUserProfileActivity::class.java)
                intent.putExtra(AppConstants.USER_DATA, list)
                startActivityForResult(intent, AppConstants.UPDATE_PROFILE)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.UPDATE_PROFILE) {

                val params = HashMap<String, String>()
                params["user_id"] = sharedPrefsHelper.getUser()?.id.toString()
                viewModel.hitUserProfile(sharedPrefsHelper.getUser()?.token!!, params)
            }
        }
    }
}