package com.heena.user.tasks

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.heena.user.R
import com.heena.user.`interface`.ClickInterface
import com.heena.user.adapters.ServiceListingAdapter
import com.heena.user.application.MyApp
import com.heena.user.models.*
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.side_top_view.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor

class HomeApiTasks(taskExecutor: Executor) {
    private var myTaskExecutor: Executor = taskExecutor
    fun callTasks(){
        myTaskExecutor.execute {getHomesAPI()}
    }
    companion object{
        lateinit var userProfileResponse : UserProfileResponse
        lateinit var categoryListResponse : CategoryListResponse
        lateinit var dashboardManagerListingResponse : ManagerListingResponse
    }


    private fun getHomesAPI(){
        getProfile()
        getHomes()
    }
    private fun getHomes() {
        getCategories()
        getDashboardManagerList()
    }

    private fun getProfile() {
        val call = Utility.apiInterface.getLoggedInUserProfile(
            MyApp.sharedPreferenceInstance!!.get(SharedPreferenceUtility.UserId, 0),
            MyApp.sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        call?.enqueue(object : Callback<UserProfileResponse?> {
            override fun onResponse(
                call: Call<UserProfileResponse?>,
                response: Response<UserProfileResponse?>
            ) {
                if (response.isSuccessful) {
                    userProfileResponse = response.body()!!
                }else{
                    LogUtils.e("ErrorMsg", response.message())
                }
            }

            override fun onFailure(call: Call<UserProfileResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
            }
        })
    }
    private fun getCategories() {
        val call =
            Utility.apiManagerInterface.categoryList(MyApp.sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])
        call!!.enqueue(object : Callback<CategoryListResponse?> {
            override fun onResponse(
                call: Call<CategoryListResponse?>,
                response: Response<CategoryListResponse?>
            ) {
                try {
                    if (response.isSuccessful) {
                        categoryListResponse = response.body()!!
                    } else {
                        LogUtils.e("ErrorMsg", response.message())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<CategoryListResponse?>, throwable: Throwable) {
                LogUtils.e("ErrorMsg", throwable.message)
            }
        })
    }
    private fun getDashboardManagerList() {
        val call = Utility.apiInterface.dashboardManagersListing(
            MyApp.sharedPreferenceInstance!!.get(
                SharedPreferenceUtility.SelectedLang,
                ""
            )
        )
        call?.enqueue(object : Callback<ManagerListingResponse?> {
            override fun onResponse(
                call: Call<ManagerListingResponse?>,
                response: Response<ManagerListingResponse?>
            ) {
                if (response.isSuccessful) {
                    dashboardManagerListingResponse = response.body()!!
                } else {
                    LogUtils.e("ErrorMsg", response.message())
                }
            }

            override fun onFailure(call: Call<ManagerListingResponse?>, throwable: Throwable) {
                LogUtils.e("ErrorMsg", throwable.message)
            }
        })
    }

}