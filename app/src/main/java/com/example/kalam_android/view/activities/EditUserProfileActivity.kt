package com.example.kalam_android.view.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.databinding.ActivityEditUserProfileBinding
import com.example.kalam_android.helper.JSONParser
import com.example.kalam_android.util.AppConstants
import com.example.kalam_android.util.CropHelper
import com.example.kalam_android.util.Debugger
import com.example.kalam_android.util.SharedPrefsHelper
import com.example.kalam_android.view.adapter.AdapterForProfilePhotos
import com.example.kalam_android.view.adapter.AdapterForProfileVideos
import com.example.kalam_android.view.adapter.CityAdapter
import com.example.kalam_android.view.adapter.CountriesAdapter
import com.example.kalam_android.wrapper.GlideDownloder
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_sign_up.view.tvCountry
import kotlinx.android.synthetic.main.layout_for_user_edit_profile_overview.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject

class EditUserProfileActivity : AppCompatActivity(), View.OnClickListener, CountriesAdapter.LocationItemClickListener, CityAdapter.CityItemClickListener {
    private lateinit var binding: ActivityEditUserProfileBinding
    private var isWallImage: Boolean = false
    private var profileImagePath: String? = null
    lateinit var dialogCountry: Dialog
    lateinit var dialogCity: Dialog
    var countryName = ""
    var cityName = ""
    var countries: ArrayList<String>? = null
    var cities: ArrayList<String>? = null
    var countriesJson = ""
    var citiesJson = ""
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_profile)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_user_profile)
        initShowMore()
        onclickListener()
        initProfileVideosRecyclerView()
        initProfileImagesRecyclerView()
        countriesJson = JSONParser.loadJSONFromAsset(this, "countriesandcities/countries.json")
        citiesJson = JSONParser.loadJSONFromAsset(this, "countriesandcities/cities.json")
        binding.rvUserProfileVideos.isFocusable = false
        binding.rvUserProfilePhotos.isFocusable = false
        binding.nestedScroll.isFocusable = false
    }

    private fun initProfileVideosRecyclerView() {

        binding.rvUserProfileVideos.layoutManager = GridLayoutManager(this, 4) as RecyclerView.LayoutManager?
        binding.rvUserProfileVideos.adapter = AdapterForProfileVideos()
    }

    private fun initProfileImagesRecyclerView() {

        binding.rvUserProfilePhotos.layoutManager = GridLayoutManager(this, 4)
        binding.rvUserProfilePhotos.adapter = AdapterForProfilePhotos()
    }

    private fun initShowMore() {

        binding.tvOverview.text = HtmlCompat.fromHtml(
                "<u>${getString(R.string.labelOverview)}</u>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvPhotos.text = HtmlCompat.fromHtml(
                "<u>${getString(R.string.labelPhotos)}</u>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvVideos.text = HtmlCompat.fromHtml(
                "<u>${getString(R.string.labelVideos)}</u>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )

    }

    private fun onclickListener() {

        binding.tvOverview.setTextColor(
                ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color
                )
        )
        binding.tvOverview.setOnClickListener(this)
        binding.tvPhotos.setOnClickListener(this)
        binding.tvVideos.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivCameraWall.setOnClickListener(this)
        binding.ivProfileCamera.setOnClickListener(this)
        binding.overviewView.tvCountry.setOnClickListener(this)
        binding.overviewView.tvCity.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvOverview -> {
                binding.tvOverview.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.theme_color
                        )
                )
                binding.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                        )
                )
                binding.tvVideos.setTextColor(
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
                binding.tvOverview.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                        )
                )
                binding.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.theme_color
                        )
                )
                binding.tvVideos.setTextColor(
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
                binding.tvOverview.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                        )
                )
                binding.tvPhotos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.black
                        )
                )
                binding.tvVideos.setTextColor(
                        ContextCompat.getColor(
                                applicationContext,
                                R.color.theme_color
                        )
                )
                binding.overviewView.visibility = View.GONE
                binding.rvUserProfilePhotos.visibility = View.GONE
                binding.rvUserProfileVideos.visibility = View.VISIBLE

            }
            R.id.ivBack -> {
                onBackPressed()
            }
            R.id.ivProfileCamera -> {
                isWallImage = false
                checkPermissions()
            }
            R.id.ivCameraWall -> {
                isWallImage = true
                checkPermissions()

            }
            R.id.tvCountry -> {
                showCountriesDialog()
            }
            R.id.tvCity -> {
                showCitiesDialog()
            }

        }
    }

    private fun checkPermissions() {
        Dexter.withActivity(this).withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    Pix.start(
                            this@EditUserProfileActivity,
                            Options.init().setRequestCode(AppConstants.PROFILE_IMAGE_CODE)
                    )


                } else {
                    Debugger.e("Capturing Image", "onPermissionDenied")
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).check()
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.PROFILE_IMAGE_CODE -> {
                    val returnValue = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
//                    logE("onActivityResult returnValue: $returnValue")
                    CropHelper.startCropActivity(
                            sharedPrefsHelper,
                            this,
                            Uri.fromFile(File(returnValue?.get(0).toString()))
                    )
                }
                UCrop.REQUEST_CROP -> {
                    handleCropResult(data)
                }
            }

        }

    }

    private fun handleCropResult(result: Intent?) {
        profileImagePath = ""
        val resultUri =/* result?.let {  }*/result?.let { UCrop.getOutput(it) }
        if (resultUri == null) {
            Toast.makeText(this, "Error in Image file", Toast.LENGTH_SHORT).show()
            return
        }
        profileImagePath = resultUri.path.toString()

        if (!isWallImage) {
            GlideDownloder.load(
                    this,
                    binding.ivProfileCamera,
                    resultUri.path,
                    R.drawable.dummy_placeholder,
                    R.drawable.dummy_placeholder
            )
        } else {
            GlideDownloder.load(
                    this,
                    binding.ivProfileWall,
                    resultUri.path,
                    R.drawable.dummy_placeholder,
                    R.drawable.dummy_placeholder
            )
        }
    }

    private fun showCountriesDialog() {
        dialogCountry = Dialog(this@EditUserProfileActivity)
        dialogCountry.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCountry.setContentView(R.layout.countries_list)
        countries = JSONParser.getCountriesFromJSON(countriesJson, this)
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = CountriesAdapter(this@EditUserProfileActivity, countries!!, this@EditUserProfileActivity)
        dialogCountry.findViewById<RecyclerView>(R.id.rvList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        dialogCountry.show()
    }

    private fun showCitiesDialog() {
        Debugger.d("I_AM_BATMAN", "showCitiesDialog")
        dialogCity = Dialog(this@EditUserProfileActivity)
        dialogCity.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCity.setContentView(R.layout.countries_list)
        cities = JSONParser.getCitiesFromJSON(citiesJson, this, countryName)
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = CityAdapter(this@EditUserProfileActivity, cities!!, this@EditUserProfileActivity)
        dialogCity.findViewById<RecyclerView>(R.id.rvList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        dialogCity.show()
    }


    override fun onItemClick(item: String, position: Int) {
        dialogCountry.dismiss()
        countryName = item
        binding.overviewView.tvCountry.text = countryName
    }

    override fun onCityItemClick(item: String, position: Int) {
        dialogCity.dismiss()
        cityName = item
        binding.overviewView.tvCity.text = cityName
    }


}
