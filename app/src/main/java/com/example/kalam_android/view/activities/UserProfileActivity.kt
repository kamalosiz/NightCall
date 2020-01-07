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
import kotlinx.android.synthetic.main.layout_for_user_profile_overview.view.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
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
        initShowMore()
        onclickListener()
        initProfileVideosRecyclerView()
        initProfileImagesRecyclerView()
        binding.profile.rvUserProfileVideos.isFocusable = false
        binding.profile.rvUserProfilePhotos.isFocusable = false
        binding.profile.nestedScroll.isFocusable = false
        binding.profile.btnProfileEdit.visibility = View.GONE

        val params = HashMap<String, String>()
        params["user_id"] = userId
        viewModel.hitUserProfile(sharedPrefsHelper.getUser()?.token!!, params)
    }


    private fun initProfileVideosRecyclerView() {

        binding.profile.rvUserProfileVideos.layoutManager = GridLayoutManager(this, 4)
        binding.profile.rvUserProfileVideos.adapter = AdapterForProfileVideos()
    }

    private fun initProfileImagesRecyclerView() {

        binding.profile.rvUserProfilePhotos.layoutManager = GridLayoutManager(this, 4)
        binding.profile.rvUserProfilePhotos.adapter = AdapterForProfilePhotos()
    }

    private fun initShowMore() {
        binding.profile.overviewView.tvDescription.text =
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
        binding.profile.overviewView.tvDescription.setShowingLine(3)
        binding.profile.overviewView.tvDescription.addShowMoreText("Show more")
        binding.profile.overviewView.tvDescription.addShowLessText("Show less")
        binding.profile.overviewView.tvDescription.setShowLessTextColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.theme_color
            )
        )
        binding.profile.overviewView.tvDescription.setShowMoreColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.theme_color
            )
        )
        binding.profile.profileHeaderView.tvOverview.text = HtmlCompat.fromHtml(
            "<u>${getString(R.string.labelOverview)}</u>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.profile.profileHeaderView.tvPhotos.text = HtmlCompat.fromHtml(
            "<u>${getString(R.string.labelPhotos)}</u>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.profile.profileHeaderView.tvVideos.text = HtmlCompat.fromHtml(
            "<u>${getString(R.string.labelVideos)}</u>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

    }

    private fun onclickListener() {

        binding.profile.profileHeaderView.tvOverview.setTextColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.theme_color
            )
        )
        binding.profile.profileHeaderView.tvOverview.setOnClickListener(this)
        binding.profile.profileHeaderView.tvPhotos.setOnClickListener(this)
        binding.profile.profileHeaderView.tvVideos.setOnClickListener(this)
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
                binding.profile.profileHeaderView.tvOverview.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color
                    )
                )
                binding.profile.profileHeaderView.tvPhotos.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black
                    )
                )
                binding.profile.profileHeaderView.tvVideos.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black
                    )
                )
                binding.profile.overviewView.visibility = View.VISIBLE
                binding.profile.rvUserProfilePhotos.visibility = View.GONE
                binding.profile.rvUserProfileVideos.visibility = View.GONE
                binding.profile.nestedScroll.scrollTo(0, 0)
                binding.profile.rvUserProfilePhotos.isNestedScrollingEnabled = false
                binding.profile.rvUserProfileVideos.isNestedScrollingEnabled = false

            }
            R.id.tvPhotos -> {
                binding.profile.profileHeaderView.tvOverview.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black
                    )
                )
                binding.profile.profileHeaderView.tvPhotos.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color
                    )
                )
                binding.profile.profileHeaderView.tvVideos.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black
                    )
                )
                binding.profile.overviewView.visibility = View.GONE
                binding.profile.rvUserProfilePhotos.visibility = View.VISIBLE
                binding.profile.rvUserProfileVideos.visibility = View.GONE

            }
            R.id.tvVideos -> {
                binding.profile.profileHeaderView.tvOverview.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black
                    )
                )
                binding.profile.profileHeaderView.tvPhotos.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black
                    )
                )
                binding.profile.profileHeaderView.tvVideos.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color
                    )
                )
                binding.profile.overviewView.visibility = View.GONE
                binding.profile.rvUserProfilePhotos.visibility = View.GONE
                binding.profile.rvUserProfileVideos.visibility = View.VISIBLE

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

        binding.profile.profileHeaderView.tvName.text = userList[0].nickname
        GlideDownloader.load(
            this,
            binding.profile.profileHeaderView.ivProfile,
            userList[0].profile_image,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        GlideDownloader.load(
            this,
            binding.profile.profileHeaderView.ivUserProfile,
            userList[0].profile_image,
            R.drawable.dummy_placeholder,
            R.drawable.dummy_placeholder
        )
        binding.profile.overviewView.tvEmail.text = userList[0].email
        binding.profile.overviewView.tvPhone.text =
            "+" + userList[0].country_code + userList[0].phone
        binding.profile.overviewView.tvAddress.text = userList[0].country
        binding.profile.profileHeaderView.tvLocation.text = userList[0].country

    }

}
