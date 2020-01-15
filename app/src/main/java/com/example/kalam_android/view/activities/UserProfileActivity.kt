package com.example.kalam_android.view.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kalam_android.R
import com.example.kalam_android.base.BaseActivity
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityUserProfileBinding
import com.example.kalam_android.repository.model.ProfileData
import com.example.kalam_android.repository.model.UserProfile
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.adapter.AdapterForProfilePhotos
import com.example.kalam_android.view.adapter.AdapterForProfileVideos
import com.example.kalam_android.viewmodel.UserProfileViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.GlideDownloader
import kotlinx.android.synthetic.main.content_profile.view.*
import kotlinx.android.synthetic.main.layout_for_user_edit_profile_overview.view.*
import kotlinx.android.synthetic.main.layout_for_user_profile_overview.view.*
import kotlinx.android.synthetic.main.layout_for_user_profile_overview.view.tvEmail
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.layout_profile_header.view.tvName
import javax.inject.Inject


class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private var userName: String = ""
    private var userImage: String = ""
    private var userId: String = ""
    private lateinit var binding: ActivityUserProfileBinding
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: UserProfileViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(UserProfileViewModel::class.java)
        viewModel.userProfileResponse().observe(this, Observer {
            consumeApiResponse(it)
        })
        setUserData()
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
        params["user_id"] = userId
        viewModel.hitUserProfile(sharedPrefsHelper.getUser()?.token!!, params)
    }


    private fun initProfileVideosRecyclerView() {

        binding.rvUserProfileVideos.layoutManager = GridLayoutManager(this, 4)
        binding.rvUserProfileVideos.adapter = AdapterForProfileVideos()
    }

    private fun initProfileImagesRecyclerView() {

        binding.rvUserProfilePhotos.layoutManager = GridLayoutManager(this, 4)
        binding.rvUserProfilePhotos.adapter = AdapterForProfilePhotos()
    }

    private fun initShowMore() {

        binding.overviewView.tvDescription.setShowingLine(3)
        binding.overviewView.tvDescription.addShowMoreText("Show more")
        binding.overviewView.tvDescription.addShowLessText("Show less")
        binding.overviewView.tvDescription.setShowLessTextColor(
                ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color
                )
        )
        binding.overviewView.tvDescription.setShowMoreColor(
                ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color
                )
        )


    }

    private fun onclickListener() {

        binding.profileHeaderView.tvOverview.setTextColor(
                ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color
                )
        )
        binding.profileHeaderView.tvOverview.setOnClickListener(this)
        binding.profileHeaderView.tvPhotos.setOnClickListener(this)
        binding.profileHeaderView.tvVideos.setOnClickListener(this)
        binding.llBack.setOnClickListener(this)
    }

    private fun setUserData() {
        if (intent != null) {
            val bundle = intent.extras
            userName = bundle?.getString(AppConstants.CHAT_USER_NAME).toString()
            userImage = bundle?.getString(AppConstants.CHAT_USER_PICTURE).toString()
            userId = bundle?.getString(AppConstants.CALLER_USER_ID).toString()

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvOverview -> {
                binding.profileHeaderView.tvOverview.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.theme_color
                        )
                )
                binding.profileHeaderView.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                        )
                )
                binding.profileHeaderView.tvVideos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                        )
                )
                binding.overviewView.visibility = View.VISIBLE
                binding.rvUserProfilePhotos.visibility = View.GONE
                binding.rvUserProfileVideos.visibility = View.GONE
                binding.nestedScroll.scrollTo(0, 0)
                binding.rvUserProfilePhotos.isNestedScrollingEnabled = false
                binding.rvUserProfileVideos.isNestedScrollingEnabled = false

            }
            R.id.tvPhotos -> {
                binding.profileHeaderView.tvOverview.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                        )
                )
                binding.profileHeaderView.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.theme_color
                        )
                )
                binding.profileHeaderView.tvVideos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
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
                                applicationContext,
                                R.color.black
                        )
                )
                binding.profileHeaderView.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                        )
                )
                binding.profileHeaderView.tvVideos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.theme_color
                        )
                )
                binding.overviewView.visibility = View.GONE
                binding.rvUserProfilePhotos.visibility = View.GONE
                binding.rvUserProfileVideos.visibility = View.VISIBLE

            }
            R.id.llBack -> {
                onBackPressed()
            }

        }
    }

    private fun consumeApiResponse(response: ApiResponse<UserProfile>) {
        Debugger.e("Response : ", "${response}")
        when (response.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                Debugger.e("data : ", "${response.data}")
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

        binding.profileHeaderView.tvName.text = userList[0].firstname+" "+userList[0].lastname
        GlideDownloader.load(
                this,
                binding.profileHeaderView.ivProfile,
                userList[0].profile_image,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
        )
        GlideDownloader.load(
                this,
                binding.profileHeaderView.ivUserProfile,
                userList[0].wall_image,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
        )
        binding.overviewView.tvEmail.text = userList[0].email
        binding.overviewView.tvPhone.text = "+" + userList[0].country_code + userList[0].phone
        binding.overviewView.tvAddress.text = userList[0].address
        binding.profileHeaderView.tvLocation.text = userList[0].city + ", " + userList[0].country
        binding.overviewView.tvWebsite.text = userList[0].website
        binding.overviewView.tvFax.text = userList[0].fax
        binding.overviewView.tvMartialStatus.text = userList[0].martial_status
        binding.overviewView.tvWork.text = userList[0].work
        binding.overviewView.tvInterested.text = userList[0].intrests
        binding.overviewView.tvEducation.text = userList[0].education
        binding.overviewView.tvDescription.text = userList[0].bio
        initShowMore()
    }

}
