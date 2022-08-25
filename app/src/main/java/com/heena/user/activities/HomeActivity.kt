package com.heena.user.activities

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.heena.user.Dialogs.NoInternetDialog
import com.heena.user.R
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.broadcastreceiver.ConnectivityReceiver
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.custom_toast_layout.view.*
import java.lang.Exception

class HomeActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    var noInternetDialog : NoInternetDialog?= null
    private var networkChangeReceiver : ConnectivityReceiver?=null
    private var isInForeground = true
    private lateinit var navController: NavController
    private lateinit var cancelPaymentDialog: AlertDialog.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.changeLanguage(
            this,
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]
        )
        setContentView(R.layout.activity_home)
        setUpViews()
    }

    companion object{
        var type:Boolean = false
    }

    private fun setUpViews() {
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.nav_home_host_fragment) as NavHostFragment
        navController=navHostFragment.navController
        if(intent != null){
            type=intent.getBooleanExtra("notification", false)
        }

        if (type){
            type = false
            navController.navigate(R.id.notificationsFragment)
        }

        networkChangeReceiver = ConnectivityReceiver()
        networkChangeReceiver!!.NetworkChangeReceiver(this)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val params = navView.layoutParams
        params.width = (((size.x).toDouble()*(3.0/4))).toInt()
        params.height = size.y
        navView.layoutParams = params


        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START)

        iv_back.setSafeOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
            return
        }else{
            when(findNavController(R.id.nav_home_host_fragment).currentDestination?.id){
                R.id.homeFragment ->  Utility.exitApp(this, this)
                R.id.notificationsFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.settingsFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.mycardsFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.mylocationsFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.cmsFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.contactusFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.helpFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.favouritesFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.appointmentFragment -> {
                    itemHome.setImageResource(R.drawable.home_icon_active)
                    findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
                }
                R.id.paymentFragment -> {
                    cancelPaymentDialog = AlertDialog.Builder(this)

                    cancelPaymentDialog.setTitle(R.string.cancel_payment)
                    cancelPaymentDialog.setMessage(R.string.cancel_payment_message)
                    cancelPaymentDialog.setCancelable(false)
                    cancelPaymentDialog.setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            finish()
                            showBookingConfirmDialog(R.string.cancel_message.toString(), 0)
                            startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
                        }
                    })

                    cancelPaymentDialog.setNegativeButton(R.string.no
                    ) { p0, _ -> p0!!.cancel() }


                    val cancelAlert = cancelPaymentDialog.create()
                    cancelAlert.setTitle(R.string.cancel_payment)
                    cancelAlert.show()
                }
                else-> findNavController(R.id.nav_home_host_fragment).popBackStack()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
        if(isInBackgroundandNwavailable) {
            if(noInternetDialog!=null){
                noInternetDialog?.dismiss()
                isInBackgroundandNwavailable=false
            }
        }
    }
    private var isInBackgroundandNwavailable=false
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if(!isConnected){
            isInBackgroundandNwavailable=false
            noInternetDialog = NoInternetDialog()
            noInternetDialog?.isCancelable = false
            noInternetDialog?.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog?.dismiss()
                }

            })
            noInternetDialog?.show(supportFragmentManager, "Home Activity")
        }else{
            if (isInForeground){
                if (noInternetDialog!=null){
                    try {
                        noInternetDialog?.dismiss()
                        noInternetDialog = null
                    }catch (e:Exception){
                        Log.e("exception", e.localizedMessage)
                        Log.e("exception_1", e.message.toString())
                    }

                }
            }else{
                isInBackgroundandNwavailable=true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (networkChangeReceiver!=null) {
            unregisterReceiver(networkChangeReceiver)
        }
    }

    private fun showBookingConfirmDialog(message: String, status : Int) {
        val customToastLayout = layoutInflater.inflate(R.layout.custom_toast_layout,llCustomToastContainer)
        val customToast = Toast(applicationContext)
        customToast.view = customToastLayout
        customToast.setGravity(Gravity.CENTER,0,0)
        customToast.duration = Toast.LENGTH_LONG
        if (status==0) {
            customToastLayout.tv_booking_confirmed.text = getString(R.string.booking_status)
            customToastLayout.tv_booking_confirmed_message.text =message
        }else{
            customToastLayout.tv_booking_confirmed.text = getString(R.string.booking_status)
            customToastLayout.tv_booking_confirmed_message.text = getString(R.string.booking_confirmed)
        }
        customToast.show()
    }
}