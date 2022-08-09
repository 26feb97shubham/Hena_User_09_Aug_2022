package com.heena.user.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.heena.user.R
import com.heena.user.utils.LogUtils
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_no_internet.*

class NoInternetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        retry.setSafeOnClickListener {
            if (Utility.hasConnection(this)){
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
            }else{
                LogUtils.shortToast(this, "Still No Internet")
            }
        }

        close.setSafeOnClickListener {
            finish()
        }
    }
}