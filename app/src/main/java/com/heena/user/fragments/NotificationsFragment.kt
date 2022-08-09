package com.heena.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.adapters.NotificationsAdapter
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.models.Notification
import com.heena.user.models.NotificationResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_all_managers_list.view.*
import kotlinx.android.synthetic.main.fragment_my_cards.view.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class NotificationsFragment : Fragment() {
    private var mView : View?=null
    private var notificationList = ArrayList<Notification>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_notifications, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        requireActivity().tv_title.text = ""

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F, 0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }


        if (!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                    getNotifications(true)
                }

            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "Notifications Fragment")
        }else{
            getNotifications(true)
        }

        mView!!.swipeRefreshNotification.setOnRefreshListener {
            if (!Utility.hasConnection(requireContext())){
                val noInternetDialog = NoInternetDialog()
                noInternetDialog.isCancelable = false
                noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                    override fun retry() {
                        noInternetDialog.dismiss()
                        getNotifications(true)
                    }

                })
                noInternetDialog.show(requireActivity().supportFragmentManager, "Notifications Fragment")
            }else{
                getNotifications(true)
            }
        }
    }

    private fun getNotifications(isRefresh:Boolean){
        if(!isRefresh) {
            mView!!.swipeRefreshNotification.visibility = View.VISIBLE
        }
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.notificationProgressBar.visibility= View.VISIBLE
        val call = Utility.apiInterface.getNotification(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0],
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""])

        call!!.enqueue(object : Callback<NotificationResponse?>{
            override fun onResponse(
                call: Call<NotificationResponse?>,
                response: Response<NotificationResponse?>
            ) {
                mView!!.notificationProgressBar.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (mView!!.swipeRefreshNotification.isRefreshing) {
                    mView!!.swipeRefreshNotification.isRefreshing = false
                }
                try {
                    if (response.isSuccessful){
                        if(response.body()!=null){
                            if (response.body()!!.status==1){
                                notificationList.clear()
                                notificationList = response.body()!!.notification
                                if (notificationList.size==0){
                                    mView!!.noNotificationView.visibility = View.VISIBLE
                                    mView!!.rvNotificationsList.visibility = View.GONE
                                }else{
                                    mView!!.noNotificationView.visibility = View.GONE
                                    mView!!.rvNotificationsList.visibility = View.VISIBLE
                                    setNotificationAdapter(notificationList)
                                }
                            }else{
                                mView!!.noNotificationView.visibility = View.VISIBLE
                                mView!!.rvNotificationsList.visibility = View.GONE
                            }
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.notificationsFragmentConstraintLayout,
                                response.message(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.notificationsFragmentConstraintLayout,
                            requireContext().getString(R.string.response_isnt_successful),
                            requireContext())
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<NotificationResponse?>, t: Throwable) {
                if (mView!!.swipeRefreshNotification.isRefreshing) {
                    mView!!.swipeRefreshNotification.isRefreshing = false
                }
                mView!!.noNotificationView.visibility = View.VISIBLE
                mView!!.rvNotificationsList.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                Utility.showSnackBarOnResponseError(mView!!.notificationsFragmentConstraintLayout,
                    requireContext().getString(R.string.response_isnt_successful),
                    requireContext())
            }

        })
    }

    private fun setNotificationAdapter(notificationList: ArrayList<Notification>) {
        mView!!.rvNotificationsList.apply {
            this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.adapter = NotificationsAdapter(requireContext(), notificationList)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().iv_notification.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        requireActivity().iv_notification.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().iv_notification.visibility = View.VISIBLE
    }

    companion object{
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
}