package com.example.kalam_android.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kalam_android.R
import com.example.kalam_android.base.MyApplication
import com.example.kalam_android.databinding.ActivityEditUserProfileBinding
import com.example.kalam_android.helper.JSONParser
import com.example.kalam_android.repository.model.ProfileData
import com.example.kalam_android.repository.model.UpdateUserProfile
import com.example.kalam_android.repository.net.ApiResponse
import com.example.kalam_android.repository.net.Status
import com.example.kalam_android.util.*
import com.example.kalam_android.view.adapter.AdapterForProfilePhotos
import com.example.kalam_android.view.adapter.AdapterForProfileVideos
import com.example.kalam_android.view.adapter.CityAdapter
import com.example.kalam_android.view.adapter.CountriesAdapter
import com.example.kalam_android.viewmodel.EditUserProfileViewModel
import com.example.kalam_android.viewmodel.factory.ViewModelFactory
import com.example.kalam_android.wrapper.GlideDownloader
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.yalantis.ucrop.UCrop
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_sign_up.view.tvCountry
import kotlinx.android.synthetic.main.layout_for_user_edit_profile_overview.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class EditUserProfileActivity : AppCompatActivity(), View.OnClickListener,
    CountriesAdapter.LocationItemClickListener, CityAdapter.CityItemClickListener {
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
    var wallImage = ""
    var profileImage = ""
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var viewModel: EditUserProfileViewModel
    @Inject
    lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_user_profile)
        MyApplication.getAppComponent(this).doInjection(this)
        viewModel = ViewModelProviders.of(this, factory).get(EditUserProfileViewModel::class.java)
        viewModel.userProfileResponse().observe(this, Observer {
            consumeApiResponse(it)
        })
        initShowMore()
        onclickListener()
        initProfileVideosRecyclerView()
        initProfileImagesRecyclerView()
        countriesJson = JSONParser.loadJSONFromAsset(this, "countriesandcities/countries.json")
        citiesJson = JSONParser.loadJSONFromAsset(this, "countriesandcities/cities.json")
        binding.rvUserProfileVideos.isFocusable = false
        binding.rvUserProfilePhotos.isFocusable = false
        binding.nestedScroll.isFocusable = false
        binding.overviewView.etEmail.isEnabled = false
        binding.overviewView.etPhone.isEnabled = false
        getIntentData()
    }

    private fun initProfileVideosRecyclerView() {

        binding.rvUserProfileVideos.layoutManager =
            GridLayoutManager(this, 4)
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

    @SuppressLint("SetTextI18n")
    private fun getIntentData() {
        if (intent != null) {
            val list = intent.getSerializableExtra(AppConstants.USER_DATA) as ArrayList<ProfileData>
            binding.etFirstName.setText(list[0].firstname)
            binding.etLastName.setText(list[0].lastname)
            binding.overviewView.etPhone.setText("+" + list[0].country_code + list[0].phone)
            binding.overviewView.etEmail.setText(list[0].email)
            binding.overviewView.etDescription.setText(list[0].bio)
            binding.overviewView.etAddress.setText(list[0].address)
//            binding.overviewView.etWebsite.setText(list[0].website)
            binding.overviewView.etEducation.setText(list[0].education)
//            binding.overviewView.etFax.setText(list[0].fax)
            binding.overviewView.etWork.setText(list[0].work)
            binding.overviewView.tvCity.text = list[0].city
            binding.overviewView.tvCountry.text = list[0].country
            binding.overviewView.spInterest.setSelection(
                getIndex(
                    binding.overviewView.spInterest,
                    list[0].intrests
                )
            )
            binding.overviewView.spStatus.setSelection(
                getIndex(
                    binding.overviewView.spStatus,
                    list[0].martial_status
                )
            )
            GlideDownloader.load(
                this,
                binding.ivProfile,
                list[0].profile_image,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
            )
            GlideDownloader.load(
                this,
                binding.ivProfileWall,
                list[0].wall_image,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
            )
        }
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
        binding.tvCancel.setOnClickListener(this)
        binding.ivCameraWall.setOnClickListener(this)
        binding.ivProfileCamera.setOnClickListener(this)
        binding.overviewView.tvCountry.setOnClickListener(this)
        binding.overviewView.tvCity.setOnClickListener(this)
        binding.tvSave.setOnClickListener(this)
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
            R.id.tvCancel -> {
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
            R.id.tvSave -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvSave.visibility = View.GONE
                updateProfile()
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
                        Uri.fromFile(File(returnValue?.get(0).toString())),isWallImage
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
            GlideDownloader.load(
                this,
                binding.ivProfile,
                resultUri.path,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
            )
            profileImage = profileImagePath as String
        } else {
            GlideDownloader.load(
                this,
                binding.ivProfileWall,
                resultUri.path,
                R.drawable.dummy_placeholder,
                R.drawable.dummy_placeholder
            )
            wallImage = profileImagePath as String

        }
    }

    private fun showCountriesDialog() {
        dialogCountry = Dialog(this@EditUserProfileActivity)
        dialogCountry.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCountry.setContentView(R.layout.countries_list)
        countries = JSONParser.getCountriesFromJSON(countriesJson, this)
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = CountriesAdapter(
            this@EditUserProfileActivity,
            countries!!,
            this@EditUserProfileActivity
        )
        dialogCountry.findViewById<RecyclerView>(R.id.rvList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        dialogCountry.findViewById<EditText>(R.id.etSearch)
            .addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString() != "") {
                        countries.apply {
                            this?.forEach {
                                if (it.contains(s.toString())) {
                                    val tempList: ArrayList<String> = ArrayList<String>()
                                    tempList.add(it)
                                    viewAdapter.updateList(tempList)
                                }
                            }
                        }
                    } else {
                        viewAdapter.updateList(countries!!)
                    }
                }
            })

        dialogCountry.show()
    }

    private fun showCitiesDialog() {
        Debugger.d("I_AM_BATMAN", "showCitiesDialog")
        dialogCity = Dialog(this@EditUserProfileActivity)
        dialogCity.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCity.setContentView(R.layout.countries_list)
        val viewManager = LinearLayoutManager(this)
        cities = JSONParser.getCitiesFromJSON(citiesJson, this, countryName)
        val viewAdapter =
            CityAdapter(this@EditUserProfileActivity, cities!!, this@EditUserProfileActivity)
        dialogCity.findViewById<RecyclerView>(R.id.rvList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        dialogCity.findViewById<EditText>(R.id.etSearch)
            .addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    char: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (char.toString() != "") {
                        cities.apply {
                            this?.forEach {
                                if (it.contains(char.toString())) {
                                    val tempList: ArrayList<String> = ArrayList()
                                    tempList.add(it)
                                    viewAdapter.updateList(tempList)
                                }
                            }
                        }
                    } else {
                        viewAdapter.updateList(cities!!)
                    }
                }

            })
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

    private fun updateProfile() {

        if (profileImage == "" && wallImage == "") {
            val params = HashMap<String, String>()
            params["user_id"] = sharedPrefsHelper.getUser()?.id.toString()
            params["bio"] = binding.overviewView.etDescription.text.toString()
            params["address"] = binding.overviewView.etAddress.text.toString()
            params["first_name"] = binding.etFirstName.text.toString()
            params["last_name"] = binding.etLastName.text.toString()
            params["website"] = binding.overviewView.etWebsite.text.toString()
            params["fax"] = binding.overviewView.etFax.text.toString()
            if (binding.overviewView.spInterest.selectedItem.toString() == "Select Interest") {
                params["intrests"] = ""
            } else {
                params["intrests"] = binding.overviewView.spInterest.selectedItem.toString()
            }
            if (binding.overviewView.spStatus.selectedItem.toString() == "Select Status") {
                params["martial_status"] = ""
            } else {
                params["martial_status"] = binding.overviewView.spStatus.selectedItem.toString()
            }
            params["work"] = binding.overviewView.etWork.text.toString()
            params["education"] = binding.overviewView.etEducation.text.toString()
            params["city"] = binding.overviewView.tvCity.text.toString()
            params["country"] = binding.overviewView.tvCountry.text.toString()
            viewModel.hitUpdateUserProfile(sharedPrefsHelper.getUser()?.token.toString(), params)

        } else if (profileImage != "" && wallImage != "") {
            viewModel.hitUpdateUserProfile(
                sharedPrefsHelper.getUser()?.token.toString(),
                multiPartParams(),
                convertImageMultipart(profileImage, "profile_image"),
                convertImageMultipart(wallImage, "wall_image")
            )

        } else if (profileImage != "" && wallImage == "") {
            viewModel.hitUpdateUserProfile(
                sharedPrefsHelper.getUser()?.token.toString(),
                multiPartParams(),
                convertImageMultipart(profileImage, "profile_image")
            )

        } else {
            viewModel.hitUpdateUserProfile(
                sharedPrefsHelper.getUser()?.token.toString(),
                multiPartParams(),
                convertImageMultipart(wallImage, "wall_image")
            )
        }
    }

    private fun multiPartParams(): HashMap<String, @JvmSuppressWildcards RequestBody> {
        val params = HashMap<String, @JvmSuppressWildcards RequestBody>()

        params["user_id"] = RequestBody.create(
            MediaType.parse("text/plain"),
            sharedPrefsHelper.getUser()?.id.toString()
        )
        params["bio"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.overviewView.etDescription.text.toString()
        )
        params["address"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.overviewView.etAddress.text.toString()
        )
        params["first_name"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.etFirstName.text.toString()
        )
        params["last_name"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.etLastName.text.toString()
        )
        params["website"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.overviewView.etWebsite.text.toString()
        )
        params["fax"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.overviewView.etFax.text.toString()
        )
        if (binding.overviewView.spInterest.selectedItem.toString() == "Select Interest") {
            params["intrests"] = RequestBody.create(MediaType.parse("text/plain"), "")
        } else {
            params["intrests"] = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.overviewView.spInterest.selectedItem.toString()
            )
        }
        if (binding.overviewView.spStatus.selectedItem.toString() == "Select Status") {
            params["martial_status"] = RequestBody.create(MediaType.parse("text/plain"), "")
        } else {
            params["martial_status"] = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.overviewView.spStatus.selectedItem.toString()
            )
        }
        params["work"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.overviewView.etWork.text.toString()
        )
        params["education"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.overviewView.etEducation.text.toString()
        )
        params["city"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.overviewView.tvCity.text.toString()
        )
        params["country"] = RequestBody.create(
            MediaType.parse("text/plain"),
            binding.overviewView.tvCountry.text.toString()
        )
        return params
    }

    private fun consumeApiResponse(response: ApiResponse<UpdateUserProfile>) {
        Debugger.e("Response : ", "${response}")
        when (response.status) {

            Status.LOADING -> {
            }
            Status.SUCCESS -> {
                Debugger.e("update profile response : ", "${response.data}")
                toast(response.data?.data.toString())
                binding.tvSave.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                setResult(Activity.RESULT_OK)
                finish()

            }
            Status.ERROR -> {
                Debugger.e("update profile error : ", "${response.error}")
                binding.progressBar.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE

            }
            else -> {

            }
        }
    }

    private fun getIndex(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == myString) {
                return i
            }
        }
        return 0
    }

    private fun convertImageMultipart(path: String, imageType: String): MultipartBody.Part {
        val imageFileBody: MultipartBody.Part?
//        val fileToUpload = File(path)
        val fileToUpload = Compressor(this).compressToFile(File(path))
        val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), fileToUpload)
        imageFileBody =
            MultipartBody.Part.createFormData(imageType, fileToUpload.name, requestBody)
        return imageFileBody
    }

}
