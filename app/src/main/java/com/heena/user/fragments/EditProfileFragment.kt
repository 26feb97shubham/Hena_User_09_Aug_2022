package com.heena.user.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.user.BuildConfig.APPLICATION_ID
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.`interface`.APIInterface.Companion.createMultipartBodyBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.extras.FetchPath
import com.heena.user.models.MyResponse
import com.heena.user.models.UserProfileResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.IMAGE_DIRECTORY_NAME
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_contactus.view.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.android.synthetic.main.side_top_view.*
import kotlinx.android.synthetic.main.side_top_view.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileFragment : Fragment() {

    private var mView : View? = null
    private val PERMISSIONS =  arrayOf(Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var uri: Uri? = null
    private val MEDIA_TYPE_IMAGE = 1
    private val PICK_IMAGE_FROM_GALLERY = 10
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100
    private var imagePath = ""
    private var name = ""
    var profile_picture:String=""
    private var profile_pic_changed = false
    var status = 0
    val requestOption = RequestOptions().centerCrop()

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
            registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()) { result ->
                var allAreGranted = true
                for(b in result.values) {
                    allAreGranted = allAreGranted && b
                }
                if(allAreGranted) {
                    Log.e("Granted", "Permissions")
                    openCameraDialog()
                }else{
                    Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintLayout,
                        requireContext().getString(R.string.please_allow_permissions),
                        requireContext())
                }
            }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        if (status == CAMERA_CAPTURE_IMAGE_REQUEST_CODE){
            if (it.resultCode == Activity.RESULT_OK){
                if (uri != null) {
                    imagePath = ""
                    Log.e("uri", uri.toString())
                    imagePath = uri!!.path!!
                    Log.e("image_path", imagePath)
                    Glide.with(requireContext()).load("file:///$imagePath").placeholder(R.drawable.user).into(mView!!.editprofileimg)
                } else {
                    Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintLayout,
                        requireContext().getString(R.string.something_went_wrong),
                        requireContext())
                }
            }
        }else if (status.equals(PICK_IMAGE_FROM_GALLERY)){
            if (it.resultCode==Activity.RESULT_OK){
                val data: Intent? = it.data
                if (data!!.data != null) {
                    imagePath = ""
                    val uri = data.data
                    (if (uri.toString().startsWith("content")) {
                        FetchPath.getPath(requireContext(), uri!!)!!
                    } else {
                        uri!!.path!!
                    }).also { imagePath = it }
                    Log.e("image_path", imagePath)
                    Glide.with(requireContext()).applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.user)).load("file:///$imagePath").into(mView!!.editprofileimg)
                }
            }
        }
    }
    

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        getProfile()
        setUpViews()
        return mView
    }

    private fun setUpViews() {
        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(
                AlphaAnimation(1f, 0.5f)
            )
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        requireActivity().tv_title.text = ""

        mView!!.editProfile.setSafeOnClickListener {
            profile_pic_changed = true
            mView!!.editProfile.startAnimation(AlphaAnimation(1f, 0.5f))
            activityResultLauncher.launch(PERMISSIONS)
        }

        mView!!.btnSubmit.setSafeOnClickListener {
            mView!!.btnSubmit.startAnimation(AlphaAnimation(1f, .5f))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(),   mView!!.btnSubmit)
            validateAndEdit()
        }
    }

    private fun validateAndEdit() {
        name=mView!!.edtNameUpdate.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_your_full_name),
                requireContext())
        }
        else if (!sharedPreferenceInstance!!.isCharacterAllowed(name)) {
            Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintLayout,
                requireContext().getString(R.string.emojis_are_not_allowed),
                requireContext())
        }
        else {
            editProfile()
        }
    }

    private fun editProfile() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressbar.visibility= View.VISIBLE

        var call : Call<MyResponse?>?=null

        if (imagePath.equals("")){
            val builder = createBuilder(arrayOf("name","user_id","lang", "image"),
                arrayOf( name.trim { it <= ' ' },
                    sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString(),
                ""))
            call = apiInterface.editProfile(builder.build())
        }else{
            val builder = createMultipartBodyBuilder(arrayOf("name","user_id",  "lang"),
                arrayOf( name.trim { it <= ' ' },
                    sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                    sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""].toString()))


            if (imagePath != "") {
                if (profile_pic_changed){
                    profile_pic_changed = false
                    val file = File(imagePath)
                    Log.e("file name ", file.name)
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.ProfilePic,imagePath)
                    val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
                    builder!!.addFormDataPart("image", file.name, requestBody)
                }else{
                    sharedPreferenceInstance!!.save(SharedPreferenceUtility.ProfilePic,imagePath)
                    val requestBody = RequestBody.create(MediaType.parse("image/*"), imagePath)
                    builder!!.addFormDataPart("image", imagePath, requestBody)
                }
            }

            call = apiInterface.editProfile(builder!!.build())
        }

        call!!.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                mView!!.progressbar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.body() != null) {
                        if (response.body()!!.status== 1) {
                            Utility.showSnackBarOnResponseSuccess(mView!!.editProfileFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            sharedPreferenceInstance!!.save(SharedPreferenceUtility.ProfilePic, imagePath)
                            findNavController().navigate(R.id.homeFragment)
                        } else {
                            Utility.showSnackBarOnResponseError(mView!!.editProfileFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<MyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarValidationError(mView!!.editProfileFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.progressbar.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

    }


    private fun openCameraDialog() {
        val items = arrayOf<CharSequence>(getString(R.string.camera), getString(R.string.gallery), getString(R.string.cancel))
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_photo))
        builder.setItems(items) { dialogInterface, i ->
            when {
                items[i] == getString(R.string.camera) -> {
                    captureImage()
                }
                items[i] == getString(R.string.gallery) -> {
                    chooseImage()
                }
                items[i] == getString(R.string.cancel) -> {
                    dialogInterface.dismiss()
                }
            }
        }
        builder.show()
    }


    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        status = PICK_IMAGE_FROM_GALLERY
        resultLauncher.launch(intent)
    }


    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        uri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        status = CAMERA_CAPTURE_IMAGE_REQUEST_CODE
        resultLauncher.launch(intent)
    }


    fun getOutputMediaFileUri(type: Int): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(requireContext(), APPLICATION_ID.toString() + ".provider", getOutputMediaFile(type)!!)
        } else {
            Uri.fromFile(getOutputMediaFile(type))
        }
    }

    private fun getOutputMediaFile(type: Int): File? {
        val mediaStorageDir = File(
            Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME)
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault()).format(Date())
        val mediaFile: File = when (type) {
            MEDIA_TYPE_IMAGE -> {
                File(mediaStorageDir.path + File.separator
                        + "IMG_" + timeStamp + ".png")
            }
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                File(mediaStorageDir.path + File.separator
                        + "VID_" + timeStamp + ".mp4")
            }
            else -> {
                return null
            }
        }
        return mediaFile
    }

    private fun getProfile() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressbar.visibility= View.VISIBLE

        val call = apiInterface.getLoggedInUserProfile(sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0),
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        call?.enqueue(object : Callback<UserProfileResponse?> {
            override fun onResponse(call: Call<UserProfileResponse?>, response: Response<UserProfileResponse?>) {
                mView!!.progressbar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        if (response.body()!!.profile!!.image.toString().contains("default", false)) {
                            Glide.with(requireContext()).load(R.drawable.golden_logo)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        p0: GlideException?,
                                        p1: Any?,
                                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                                        p3: Boolean
                                    ): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        p0: Drawable?,
                                        p1: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        p4: Boolean
                                    ): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(mView!!.editprofileimg)
                            requireActivity().userIcon.borderWidth = 5
                        } else {
                            Glide.with(requireContext()).load(response.body()!!.profile!!.image.toString())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        p0: GlideException?,
                                        p1: Any?,
                                        p2: com.bumptech.glide.request.target.Target<Drawable>?,
                                        p3: Boolean
                                    ): Boolean {
                                        Log.e("err", p0?.message.toString())
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        p0: Drawable?,
                                        p1: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        p4: Boolean
                                    ): Boolean {
                                        requireActivity().headerView.user_icon_progress_bar_side_top_view.visibility =
                                            View.GONE
                                        return false
                                    }
                                }).placeholder(R.drawable.user)
                                .apply(requestOption).into(mView!!.editprofileimg)
                        }
                        mView!!.edtNameUpdate.setText(response.body()!!.profile!!.name)
                        val phoneNumber = "+971-"+response.body()!!.profile!!.phone
                        mView!!.tv_phone_update.setText(phoneNumber)
                        mView!!.tv_email_update.setText(response.body()!!.profile!!.email)
                        profile_picture=response.body()!!.profile!!.image.toString()
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.editProfileFragmentConstraintLayout,
                            response.body()!!.message.toString(),
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.editProfileFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<UserProfileResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.editProfileFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
                mView!!.progressbar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
    }
}