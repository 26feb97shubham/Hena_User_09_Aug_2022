package com.heena.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AlphaAnimation
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.heena.user.R
import com.heena.user.`interface`.APIInterface.Companion.createBuilder
import com.heena.user.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.user.extras.AsteriskPasswordTransformationMethod
import com.heena.user.models.MyResponse
import com.heena.user.utils.LogUtils
import com.heena.user.utils.SharedPreferenceUtility
import com.heena.user.utils.Utility
import com.heena.user.utils.Utility.apiInterface
import com.heena.user.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_add_new_card.view.*
import kotlinx.android.synthetic.main.fragment_reschedule_service_bottom_sheet.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddNewCardFragment : Fragment() {
    private var mView : View ?= null
    private var cardTitle = ""
    private var cardNumber = ""
    private var cardCvv = ""
    private var cardExpiry = ""
    private val sdf = SimpleDateFormat("MM/YYYY", Locale.ENGLISH)
    private var expiryDate : Date?=null
    private var expired = false
    private var keyDel = 0
    private var a =""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_add_new_card, container, false)
        setUpViews()
        return mView
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setUpViews() {
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        requireActivity().tv_title.text = ""

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
        emptyFields()
        mView!!.tv_save_card.setSafeOnClickListener {
            validateAndSave()
        }
        addTextWatcher()

        if (sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""] == "en"){
            val drawable = requireContext().resources.getDrawable(R.drawable.card_bg)
            mView!!.ll_card_1.background = drawable
        }else{
            val drawable = requireContext().resources.getDrawable(R.drawable.arabic_card)
            mView!!.ll_card_1.background = drawable
        }
    }

    private fun validateAndSave() {
        cardTitle = mView!!.et_card_title.text.toString().trim()
        cardNumber = mView!!.et_card_number.text.toString().replace(" ", "", ignoreCase = false).trim()
        cardCvv = mView!!.et_cvv.text.toString().trim()
        cardExpiry = mView!!.et_expiry.text.toString().trim()

        if(cardExpiry.isEmpty()){
            Utility.showSnackBarValidationError(mView!!.addNewCardFragmentConstraintLayout,
                requireContext().getString(R.string.please_enter_valid_expiry_date),
                requireContext())
        }else{
            expiryDate=sdf.parse(cardExpiry)
            val simpleDateFormat = SimpleDateFormat("MM/YYYY", Locale.US)
            val currentDate = simpleDateFormat.format(Date())
            val currentDate1 = SimpleDateFormat("MM/YYYY").parse(currentDate)
            expired= expiryDate!!.before(currentDate1)
            when {
                TextUtils.isEmpty(cardTitle) -> {
                    Utility.showSnackBarValidationError(mView!!.addNewCardFragmentConstraintLayout,
                        requireContext().getString(R.string.please_enter_valid_card_title),
                        requireContext())
                }
                TextUtils.isEmpty(cardNumber) -> {
                    Utility.showSnackBarValidationError(mView!!.addNewCardFragmentConstraintLayout,
                        requireContext().getString(R.string.please_enter_valid_card_number),
                        requireContext())
                }
                cardNumber.length<16 -> {
                    Utility.showSnackBarValidationError(mView!!.addNewCardFragmentConstraintLayout,
                        requireContext().getString(R.string.please_enter_valid_card_number),
                        requireContext())
                }
                TextUtils.isEmpty(cardCvv) -> {
                    Utility.showSnackBarValidationError(mView!!.addNewCardFragmentConstraintLayout,
                        requireContext().getString(R.string.please_enter_valid_cvv),
                        requireContext())
                }
                cardCvv.length<3 -> {
                    Utility.showSnackBarValidationError(mView!!.addNewCardFragmentConstraintLayout,
                        requireContext().getString(R.string.please_enter_valid_cvv),
                        requireContext())
                }
                expired -> {
                    Utility.showSnackBarValidationError(mView!!.addNewCardFragmentConstraintLayout,
                        requireContext().getString(R.string.card_is_expired),
                        requireContext())
                }
                else -> {
                    save()
                }
            }
        }
    }

    private fun save() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_add_card.visibility= View.VISIBLE
        val builder = createBuilder(arrayOf("user_id", "name", "number","cvv", "expiry_date", "type", "card_id", "lang"),
            arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(),
                cardTitle,
                cardNumber,
                cardCvv,
                cardExpiry, "0", "", sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.addDeleteCard(builder.build())
        call!!.enqueue(object : Callback<MyResponse?> {
            override fun onResponse(
                call: Call<MyResponse?>,
                response: Response<MyResponse?>
            ) {
                mView!!.progressBar_add_card.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            Utility.showSnackBarOnResponseSuccess(mView!!.addNewCardFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                            findNavController().popBackStack()
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.addNewCardFragmentConstraintLayout,
                                response.body()!!.message.toString(),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.addNewCardFragmentConstraintLayout,
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

            override fun onFailure(call: Call<MyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                Utility.showSnackBarOnResponseError(mView!!.addNewCardFragmentConstraintLayout,
                    requireContext().getString(R.string.check_internet),
                    requireContext())
                mView!!.progressBar_add_card.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun emptyFields() {
        mView!!.et_card_title.clearFocus()
        mView!!.et_card_number.clearFocus()
        mView!!.et_cvv.clearFocus()
        mView!!.et_expiry.clearFocus()
        mView!!.tv_expiry_date_1.clearFocus()
        mView!!.tv_card_number_1.clearFocus()
        mView!!.tv_account_holdee_name_1.clearFocus()
    }

    private fun addTextWatcher(){
        mView!!.et_card_title.doOnTextChanged { text, _, _, _ ->
            mView!!.tv_account_holdee_name_1.text = text
        }

        mView!!.et_card_number.doOnTextChanged { text1, _, _, _ ->
            val text = mView!!.et_card_number
            var flag = true
            val eachBlock: List<String> = text.text.toString()?.split(" ")!!
            for (i in eachBlock.indices) {
                if (eachBlock[i].length > 4) {
                    flag = false
                }
            }
            if (flag) {
                text.setOnKeyListener(object : View.OnKeyListener {
                    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                        if (keyCode == KeyEvent.KEYCODE_DEL) keyDel = 1
                        return false
                    }
                })
                if (keyDel == 0) {
                    if ((text.text!!.length + 1) % 5 == 0) {
                        if (text.text.toString().split(" ").size <= 3) {
                            text.setText(text.text.toString() + " ")
                            text.setSelection(text?.text!!.length)
                        }
                    }
                    a = text?.text.toString()
                } else {
                    a = text?.text.toString()
                    keyDel = 0
                }
            } else {
                text.setText(a)
            }
            mView!!.tv_card_number_1.text = a
        }

        mView!!.et_expiry.doOnTextChanged { text, _, _, _ ->
            mView!!.tv_expiry_date_1.text = text
        }

        mView!!.et_expiry.doAfterTextChanged {
            if (it!!.isNotEmpty() && it.length == 3) {
                val c: Char = it[it.length-1]
                if ('/' == c) {
                    it.delete(it.length - 1, it.length)
                }
            }
            if (it.isNotEmpty() && it.length == 3) {
                val c: Char = it[it.length-1]
                if (Character.isDigit(c) && TextUtils.split(it.toString(), "/").size <= 2) {
                    it.insert(it.length - 1, "/")
                }
            }
        }

        mView!!.et_cvv.transformationMethod = AsteriskPasswordTransformationMethod()
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